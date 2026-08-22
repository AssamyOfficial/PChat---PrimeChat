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
    private final ChatChannelRouter channelRouter;
    private final ChatFormatRenderer formatRenderer;
    private final ChatManager chatManager;

    public ChatListener(PrimeChat plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getPrimeScheduler();
        this.mentionManager = new MentionManager(plugin);
        this.channelRouter = plugin.getChatChannelRouter();
        this.formatRenderer = plugin.getChatFormatRenderer();
        this.chatManager = plugin.getChatManager();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();

        if (!chatManager.isEnabled() && !chatManager.canBypass(sender)) {
            event.setCancelled(true);
            String blockedMessage = plugin.getCommandConfig().getString(
                    "commands.chat.messages.blocked",
                    "<red>◆</red> <gray>Чат сейчас отключён.</gray>"
            );
            scheduler.run(sender, () -> sender.sendMessage(formatRenderer.parseFormat(blockedMessage)));
            return;
        }

        String messageText = PlainTextComponentSerializer.plainText().serialize(event.message());
        ChatChannel channel = channelRouter.getChannelForMessage(sender, messageText);

        if (channel == null) {
            event.setCancelled(true);
            return;
        }

        String permission = channel.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            event.setCancelled(true);
            scheduler.run(sender, () -> sender.sendMessage(
                    formatRenderer.parseFormat(
                            "<red>◆</red> <gray>У вас нет прав для использования этого чата.</gray>"
                    )
            ));
            return;
        }

        String processedMessage = channelRouter.removeTrigger(channel, messageText);
        if (processedMessage.isBlank()) {
            event.setCancelled(true);
            return;
        }

        Component finalFormatted = formatRenderer.render(
                sender,
                channel,
                processedMessage,
                event.message()
        );
        finalFormatted = mentionManager.processMentions(finalFormatted);

        event.viewers().removeIf(audience -> audience instanceof Player player
                && !channelRouter.canReceiveMessage(sender, player, channel));

        mentionManager.processNotifications(messageText, sender, event.viewers());

        boolean someoneHeard = false;
        for (var audience : event.viewers()) {
            if (audience instanceof Player player && !player.equals(sender)) {
                someoneHeard = true;
                break;
            }
        }

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

                Component notification = formatRenderer.parseFormat(unheardMessage);
                scheduler.run(sender, () -> sender.sendMessage(notification));
            }
        }

        Component result = finalFormatted;
        event.renderer((source, sourceDisplayName, messageComponent, viewer) -> result);
    }
}
