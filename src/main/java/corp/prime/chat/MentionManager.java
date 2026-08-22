package corp.prime.chat;

import corp.prime.lib.PrimeScheduler;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MentionManager {
    private final PrimeChat plugin;
    private final PrimeScheduler scheduler;

    public MentionManager(PrimeChat plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getPrimeScheduler();
    }

    public Set<String> findMentionedPlayers(String message) {
        Set<String> mentions = new HashSet<>();
        if (!plugin.getConfig().getBoolean("mentions.enabled", true)
                || message == null || message.isEmpty()) {
            return mentions;
        }

        String symbol = getSymbol();
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

    public Component processMentions(Component message) {
        if (!plugin.getConfig().getBoolean("mentions.enabled", true)) {
            return message;
        }

        String plainText = PlainTextComponentSerializer.plainText().serialize(message);
        Set<String> mentionedNames = findMentionedPlayers(plainText);
        if (mentionedNames.isEmpty()) {
            return message;
        }

        String symbol = getSymbol();
        String color = plugin.getConfig().getString("mentions.color", "<#00E5FF>");
        boolean clickable = plugin.getConfig().getBoolean("mentions.clickable", true);
        boolean hoverEnabled = plugin.getConfig().getBoolean("mentions.hover", true);

        Component result = message;
        for (String mentionedName : mentionedNames) {
            Player player = Bukkit.getPlayerExact(mentionedName);
            if (player == null) {
                continue;
            }

            String mention = symbol + player.getName();
            if (!plainText.contains(mention)) {
                continue;
            }

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

    public void processNotifications(
            String message,
            Player sender,
            Set<net.kyori.adventure.audience.Audience> viewers
    ) {
        if (!plugin.getConfig().getBoolean("mentions.notification.enabled", true)) {
            return;
        }

        for (String mentionedName : findMentionedPlayers(message)) {
            Player onlinePlayer = Bukkit.getPlayerExact(mentionedName);

            if (onlinePlayer != null) {
                if (onlinePlayer.equals(sender) || !viewers.contains(onlinePlayer)) {
                    continue;
                }
                sendNotification(onlinePlayer, sender);
                continue;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(mentionedName);
            if (offlinePlayer.hasPlayedBefore()) {
                sendStatusNotification(
                        sender,
                        mentionedName,
                        "mentions.notification.offline",
                        "<yellow>⚠</yellow> <gray>Игрок <white>%player%</white> сейчас не в сети.</gray>"
                );
            } else {
                sendStatusNotification(
                        sender,
                        mentionedName,
                        "mentions.notification.not-found",
                        "<red>⚠</red> <gray>Игрок <white>%player%</white> не найден.</gray>"
                );
            }
        }
    }

    private void sendNotification(Player mentionedPlayer, Player sender) {
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
        scheduler.run(mentionedPlayer, () -> {
            mentionedPlayer.sendMessage(notification);
            playSound(mentionedPlayer);
        });
    }

    private void playSound(Player player) {
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

    private void sendStatusNotification(
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
        scheduler.run(sender, () -> sender.sendMessage(notification));
    }

    private String getSymbol() {
        String symbol = plugin.getConfig().getString("mentions.symbol", "@");
        return symbol == null || symbol.isEmpty() ? "@" : symbol;
    }
}
