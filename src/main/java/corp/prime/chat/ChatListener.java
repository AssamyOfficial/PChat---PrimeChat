package corp.prime.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {
    private final PrimeChat plugin;

    public ChatListener(PrimeChat plugin) {
        this.plugin = plugin;
    }

    private Set<String> findMentionedPlayers(String message) {
        Set<String> mentions = new HashSet<>();

        if (!plugin.getConfig().getBoolean("mentions.enabled", true)
                || message == null || message.isEmpty()) {
            return mentions;
        }

        String symbol = plugin.getConfig().getString("mentions.symbol", "@");
        if (symbol == null || symbol.isEmpty()) {
            symbol = "@";
        }

        Matcher matcher = Pattern.compile(
                Pattern.quote(symbol) + "([A-Za-z0-9_]+)"
        ).matcher(message);

        while (matcher.find()) {
            String name = matcher.group(1);
            if (name != null && !name.isEmpty()) {
                mentions.add(name);
            }
        }

        return mentions;
    }

    private Component processMentions(Component message) {
        if (!plugin.getConfig().getBoolean("mentions.enabled", true)) {
            return message;
        }

        String symbol = plugin.getConfig().getString("mentions.symbol", "@");
        String color = plugin.getConfig().getString("mentions.color", "<#00E5FF>");
        boolean clickable = plugin.getConfig().getBoolean("mentions.clickable", true);
        boolean hoverEnabled = plugin.getConfig().getBoolean("mentions.hover", true);

        if (symbol == null || symbol.isEmpty()) {
            symbol = "@";
        }

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.sort((a, b) -> Integer.compare(b.getName().length(), a.getName().length()));

        Component result = message;

        for (Player player : onlinePlayers) {
            String mention = symbol + player.getName();
            Component mentionComponent = plugin.getChatFormatRenderer().parseFormat(color + mention);

            if (hoverEnabled) {
                Component hover = Component.text()
                        .append(Component.text("👤 "))
                        .append(Component.text(player.getName()))
                        .append(Component.newline())
                        .append(Component.text("Игрок сервера"))
                        .append(Component.newline())
                        .append(Component.text("Нажмите, чтобы написать сообщение"))
                        .build();
                mentionComponent = mentionComponent.hoverEvent(HoverEvent.showText(hover));
            }

            if (clickable) {
                mentionComponent = mentionComponent.clickEvent(
                        ClickEvent.suggestCommand("/msg " + player.getName() + " ")
                );
            }

            Component finalMention = mentionComponent;
            result = result.replaceText(builder -> builder
                    .matchLiteral(mention)
                    .replacement(finalMention));
        }

        return result;
    }

    private void processMentionNotifications(
            String message,
            Player sender,
            Set<net.kyori.adventure.audience.Audience> viewers
    ) {
        if (!plugin.getConfig().getBoolean("mentions.notification.enabled", true)) {
            return;
        }

        for (String mentionedName : findMentionedPlayers(message)) {
            Player onlinePlayer = null;

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().equalsIgnoreCase(mentionedName)) {
                    onlinePlayer = player;
                    break;
                }
            }

            if (onlinePlayer != null) {
                if (onlinePlayer.equals(sender) || !viewers.contains(onlinePlayer)) {
                    continue;
                }
                sendMentionNotification(onlinePlayer, sender);
                continue;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(mentionedName);
            if (offlinePlayer.hasPlayedBefore()) {
                sendMentionStatusNotification(
                        sender,
                        mentionedName,
                        "mentions.notification.offline",
                        "<yellow>⚠</yellow> <gray>Игрок <white>%player%</white> сейчас не в сети.</gray>"
                );
            } else {
                sendMentionStatusNotification(
                        sender,
                        mentionedName,
                        "mentions.notification.not-found",
                        "<red>⚠</red> <gray>Игрок <white>%player%</white> не найден.</gray>"
                );
            }
        }
    }

    private void sendMentionNotification(Player mentionedPlayer, Player sender) {
        if (mentionedPlayer.equals(sender)
                || !plugin.getConfig().getBoolean("mentions.notification.enabled", true)) {
            return;
        }

        String message = plugin.getConfig().getString(
                "mentions.notification.message",
                "<gradient:#00E5FF:#7C4DFF>◆</gradient> <gray>%player% упомянул Вас в чате.</gray>"
        );

        if (message == null || message.isEmpty()) {
            return;
        }

        message = message
                .replace("%player%", sender.getName())
                .replace("%displayname%", sender.getDisplayName())
                .replace("%display_name%", sender.getDisplayName());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(sender, message);
        }

        Component notification = plugin.getChatFormatRenderer().parseFormat(message);
        Bukkit.getScheduler().runTask(plugin, () -> {
            mentionedPlayer.sendMessage(notification);
            playMentionSound(mentionedPlayer);
        });
    }

    private void playMentionSound(Player player) {
        if (!plugin.getConfig().getBoolean("mentions.notification.sound.enabled", true)) {
            return;
        }

        String name = plugin.getConfig().getString(
                "mentions.notification.sound.name",
                "ENTITY_EXPERIENCE_ORB_PICKUP"
        );
        float volume = (float) plugin.getConfig().getDouble(
                "mentions.notification.sound.volume",
                1.0
        );
        float pitch = (float) plugin.getConfig().getDouble(
                "mentions.notification.sound.pitch",
                1.0
        );

        try {
            player.playSound(
                    player.getLocation(),
                    Sound.valueOf(name.toUpperCase(Locale.ROOT)),
                    volume,
                    pitch
            );
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void sendMentionStatusNotification(
            Player sender,
            String mentionedName,
            String configPath,
            String defaultMessage
    ) {
        String message = plugin.getConfig().getString(configPath, defaultMessage);
        if (message == null || message.isEmpty()) {
            return;
        }

        message = message
                .replace("%player%", mentionedName)
                .replace("%displayname%", mentionedName)
                .replace("%display_name%", mentionedName);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(sender, message);
        }

        Component notification = plugin.getChatFormatRenderer().parseFormat(message);
        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(notification));
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String messageText = PlainTextComponentSerializer.plainText().serialize(event.message());

        ChatChannel channel = plugin.getChatChannelRouter().getChannelForMessage(sender, messageText);
        if (channel == null) {
            event.setCancelled(true);
            return;
        }

        String permission = channel.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(
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
        finalFormatted = processMentions(finalFormatted);

        event.viewers().removeIf(audience -> audience instanceof Player
                && !plugin.getChatChannelRouter().canReceiveMessage(
                sender, (Player) audience, channel
        ));

        processMentionNotifications(messageText, sender, event.viewers());

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
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(notification));
            }
        }

        Component result = finalFormatted;
        event.renderer((source, sourceDisplayName, messageComponent, viewer) -> result);
    }
}
