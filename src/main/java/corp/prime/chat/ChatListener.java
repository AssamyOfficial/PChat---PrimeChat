package corp.prime.chat;

import corp.prime.lib.PrimeScheduler;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private final PrimeChat plugin;
    private final PrimeScheduler scheduler;
    private final MentionManager mentionManager;

    public ChatListener(PrimeChat plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getPrimeScheduler();
        this.mentionManager = new MentionManager(plugin);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();

        if (!plugin.getChatManager().isEnabled() && !plugin.getChatManager().canBypass(sender)) {
            event.setCancelled(true);
            scheduler.run(sender, () -> sender.sendMessage(
                    plugin.getChatFormatRenderer().parseFormat(
                            plugin.getCommandConfig().getString(
                                    "commands.chat.messages.blocked",
                                    "<red>◆</red> <gray>Чат сейчас отключён.</gray>"
                            )
                    )
            ));
            return;
        }

        String messageText = PlainTextComponentSerializer.plainText().serialize(event.message());
        ChatChannel channel = plugin.getChatChannelRouter().getChannelForMessage(sender, messageText);

        if (channel == null) {
            event.setCancelled(true);
            return;
        }

        String permission = channel.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            event.setCancelled(true);
            scheduler.run(sender, () -> sender.sendMessage(
                    plugin.getChatFormatRenderer().parseFormat(
                            "<red>◆</red> <gray>У вас нет прав для использования этого чата.</gray>"
                    )
            ));
            return;
        }

        String processedMessage = plugin.getChatChannelRouter().removeTrigger(channel, messageText);
        if (processedMessage.trim().isEmpty()) {
            event.setCancelled(true);
            return;
        }

        Component finalFormatted = plugin.getChatFormatRenderer()
                .render(sender, channel, processedMessage, event.message());
        finalFormatted = mentionManager.processMentions(finalFormatted);

        event.viewers().removeIf(audience -> audience instanceof Player
                && !plugin.getChatChannelRouter().canReceiveMessage(
                sender,
                (Player) audience,
                channel
        ));

        mentionManager.processNotifications(messageText, sender, event.viewers());

        boolean someoneHeard = event.viewers().stream()
                .filter(audience -> audience instanceof Player)
                .map(audience -> (Player) audience)
                .anyMatch(player -> !player.equals(sender));

        if (!someoneHeard && plugin.getConfig().getBoolean("unheard-message.enabled", true)) {
            Boolean channelEnabled = channel.getUnheardMessageEnabled();
            boolean enabled = channelEnabled == null || channelEnabled;

            if (enabled) {
                String unheardMessage = channel.getUnheardMessage();
                if (unheardMessage == null || unheardMessage.isEmpty()) {
                    unheardMessage = plugin.getConfig().getString(
                            "unheard-message.message",
                            "<red>⚠</red> <gray>Вас никто не услышал.</gray>"
                    );
                }

                unheardMessage = unheardMessage
                        .replace("%player%", sender.getName())
                        .replace("%displayname%", sender.getDisplayName());

                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    unheardMessage = PlaceholderAPI.setPlaceholders(sender, unheardMessage);
                }

                Component notification = plugin.getChatFormatRenderer().parseFormat(unheardMessage);
                scheduler.run(sender, () -> sender.sendMessage(notification));
            }
        }

        Component result = finalFormatted;
        event.renderer((source, sourceDisplayName, messageComponent, viewer) -> result);
    }
}
