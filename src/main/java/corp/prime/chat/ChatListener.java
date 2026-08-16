package corp.prime.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;

public class ChatListener implements Listener {

    private final PrimeChat plugin;

    public ChatListener(PrimeChat plugin) {
        this.plugin = plugin;
    }

    /*
     * ============================================================
     * MENTION FINDER
     * ============================================================
     */

    private Set<String> findMentionedPlayers(String message) {

        Set<String> mentions = new HashSet<>();

        boolean enabled =
                plugin.getConfig().getBoolean(
                        "mentions.enabled",
                        true
                );

        if (!enabled || message == null || message.isEmpty()) {
            return mentions;
        }

        String symbol =
                plugin.getConfig().getString(
                        "mentions.symbol",
                        "@"
                );

        if (symbol == null || symbol.isEmpty()) {
            symbol = "@";
        }

        /*
         * Minecraft-ник:
         *
         * буквы, цифры и _
         *
         * Например:
         * @Assamy_
         * @Assamy_2
         * @Player123
         */
        Pattern pattern = Pattern.compile(
                Pattern.quote(symbol) + "([A-Za-z0-9_]+)"
        );

        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {

            String playerName =
                    matcher.group(1);

            if (playerName != null
                    && !playerName.isEmpty()) {

                mentions.add(playerName);
            }
        }

        return mentions;
    }

    /*
     * ============================================================
     * MENTIONS
     * ============================================================
     */

    private Component processMentions(
            Component message
    ) {

        boolean enabled =
                plugin.getConfig().getBoolean(
                        "mentions.enabled",
                        true
                );

        if (!enabled) {
            return message;
        }

        String symbol =
                plugin.getConfig().getString(
                        "mentions.symbol",
                        "@"
                );

        String color =
                plugin.getConfig().getString(
                        "mentions.color",
                        "<aqua>"
                );

        boolean clickable =
                plugin.getConfig().getBoolean(
                        "mentions.clickable",
                        true
                );

        boolean hoverEnabled =
                plugin.getConfig().getBoolean(
                        "mentions.hover",
                        true
                );

        Component result =
                message;

        List<Player> onlinePlayers =
                new java.util.ArrayList<>(
                        Bukkit.getOnlinePlayers()
                );

        onlinePlayers.sort(
                (first, second) ->
                        Integer.compare(
                                second.getName().length(),
                                first.getName().length()
                        )
        );

        for (Player player :
                onlinePlayers) {

            String mention =
                    symbol + player.getName();

            Component mentionComponent =
                    plugin.getChatFormatRenderer()
                            .parseFormat(
                                    color + mention
                            );

            if (hoverEnabled) {

                Component hover =
                        Component.text()
                                .append(
                                        Component.text("👤 ")
                                )
                                .append(
                                        Component.text(
                                                player.getName()
                                        )
                                )
                                .append(
                                        Component.newline()
                                )
                                .append(
                                        Component.text(
                                                "Игрок сервера"
                                        )
                                )
                                .append(
                                        Component.newline()
                                )
                                .append(
                                        Component.text(
                                                "Нажмите, чтобы написать сообщение"
                                        )
                                )
                                .build();

                mentionComponent =
                        mentionComponent.hoverEvent(
                                HoverEvent.showText(
                                        hover
                                )
                        );
            }

            if (clickable) {

                mentionComponent =
                        mentionComponent.clickEvent(
                                ClickEvent.suggestCommand(
                                        "/msg "
                                                + player.getName()
                                                + " "
                                )
                        );
            }

            Component finalMention =
                    mentionComponent;

            result =
                    result.replaceText(
                            builder -> builder
                                    .matchLiteral(
                                            mention
                                    )
                                    .replacement(
                                            finalMention
                                    )
                    );
        }

        return result;
    }

    /*
     * ============================================================
     * PROCESS MENTION NOTIFICATIONS
     * ============================================================
     */

    private void processMentionNotifications(
            String message,
            Player sender
    ) {

        boolean enabled =
                plugin.getConfig().getBoolean(
                        "mentions.notification.enabled",
                        true
                );

        if (!enabled) {
            return;
        }

        Set<String> mentionedNames =
                findMentionedPlayers(message);

        if (mentionedNames.isEmpty()) {
            return;
        }

        for (String mentionedName : mentionedNames) {

            /*
             * ====================================================
             * ONLINE PLAYER
             * ====================================================
             */

            Player onlinePlayer = null;

            for (Player player : Bukkit.getOnlinePlayers()) {

                if (player.getName().equalsIgnoreCase(mentionedName)) {

                    onlinePlayer = player;
                    break;
                }
            }

            if (onlinePlayer != null) {

                /*
                 * Если игрок упомянул сам себя —
                 * уведомление ему не отправляем.
                 */
                if (onlinePlayer.equals(sender)) {
                    continue;
                }

                plugin.getLogger().info(
                        "Обнаружено упоминание: "
                                + onlinePlayer.getName()
                                + " от "
                                + sender.getName()
                );

                sendMentionNotification(
                        onlinePlayer,
                        sender
                );

                continue;
            }

            /*
             * ====================================================
             * OFFLINE / NOT FOUND
             * ====================================================
             *
             * Bukkit.getOfflinePlayer() может создать OfflinePlayer
             * даже для несуществующего ника, поэтому проверяем
             * hasPlayedBefore().
             */

            org.bukkit.OfflinePlayer offlinePlayer =
                    Bukkit.getOfflinePlayer(mentionedName);

            if (offlinePlayer.hasPlayedBefore()) {

                sendMentionStatusNotification(
                        sender,
                        mentionedName,
                        "mentions.notification.offline",
                        "<yellow>⚠ Игрок <white>%player%</white> сейчас не в сети.</yellow>"
                );

            } else {

                sendMentionStatusNotification(
                        sender,
                        mentionedName,
                        "mentions.notification.not-found",
                        "<red>⚠ Игрок <white>%player%</white> не найден.</red>"
                );
            }
        }
    }

    private void sendMentionNotification(
            Player mentionedPlayer,
            Player sender
    ) {

        /*
         * Если игрок упомянул сам себя —
         * уведомление не отправляем.
         */
        if (mentionedPlayer.equals(sender)) {
            return;
        }

        boolean enabled =
                plugin.getConfig().getBoolean(
                        "mentions.notification.enabled",
                        true
                );

        if (!enabled) {
            return;
        }

        String message =
                plugin.getConfig().getString(
                        "mentions.notification.message",
                        "<yellow>🔔 <white>%player%</white> упомянул вас в чате.</yellow>"
                );

        if (message == null || message.isEmpty()) {
            return;
        }

        message =
                message.replace(
                        "%player%",
                        sender.getName()
                );

        message =
                message.replace(
                        "%displayname%",
                        sender.getDisplayName()
                );

        message =
                message.replace(
                        "%display_name%",
                        sender.getDisplayName()
                );

        if (Bukkit.getPluginManager()
                .isPluginEnabled("PlaceholderAPI")) {

            message =
                    PlaceholderAPI.setPlaceholders(
                            sender,
                            message
                    );
        }

        Component notification =
                plugin.getChatFormatRenderer()
                        .parseFormat(message);

        Bukkit.getScheduler().runTask(
                plugin,
                () -> mentionedPlayer.sendMessage(notification)
        );
    }

    /*
     * ============================================================
     * MENTION STATUS NOTIFICATION
     * ============================================================
     */

    private void sendMentionStatusNotification(
            Player sender,
            String mentionedName,
            String configPath,
            String defaultMessage
    ) {

        String message =
                plugin.getConfig().getString(
                        configPath,
                        defaultMessage
                );

        if (message == null || message.isEmpty()) {
            return;
        }

        message =
                message.replace(
                        "%player%",
                        mentionedName
                );

        message =
                message.replace(
                        "%displayname%",
                        mentionedName
                );

        message =
                message.replace(
                        "%display_name%",
                        mentionedName
                );

        if (Bukkit.getPluginManager()
                .isPluginEnabled("PlaceholderAPI")) {

            message =
                    PlaceholderAPI.setPlaceholders(
                            sender,
                            message
                    );
        }

        Component notification =
                plugin.getChatFormatRenderer()
                        .parseFormat(message);

        Bukkit.getScheduler().runTask(
                plugin,
                () -> sender.sendMessage(notification)
        );
    }

    /*
     * ============================================================
     * CHAT
     * ============================================================
     */

    @EventHandler
    public void onChat(
            AsyncChatEvent event
    ) {

        Player sender =
                event.getPlayer();

        /*
         * Получаем обычный текст сообщения.
         */
        String messageText =
                PlainTextComponentSerializer
                        .plainText()
                        .serialize(
                                event.message()
                        );

        /*
         * ========================================================
         * MENTIONS
         * ========================================================
         */

        processMentionNotifications(
                messageText,
                sender
        );

        /*
         * ========================================================
         * CHANNEL
         * ========================================================
         */

        ChatChannel channel =
                plugin.getChatChannelRouter()
                        .getChannelForMessage(
                                sender,
                                messageText
                        );

        /*
         * Канал не найден —
         * сообщение отменяется.
         */
        if (channel == null) {

            event.setCancelled(true);
            return;
        }

        /*
         * ========================================================
         * PERMISSION
         * ========================================================
         */

        String permission =
                channel.getPermission();

        if (permission != null
                && !permission.isEmpty()
                && !sender.hasPermission(permission)) {

            event.setCancelled(true);

            Bukkit.getScheduler().runTask(
                    plugin,
                    () -> sender.sendMessage(
                            plugin.getChatFormatRenderer()
                                    .parseFormat(
                                            "<red>У вас нет прав для использования этого чата.</red>"
                                    )
                    )
            );

            return;
        }

        /*
         * ========================================================
         * TRIGGER
         * ========================================================
         */

        String processedMessage =
                plugin.getChatChannelRouter()
                        .removeTrigger(
                                channel,
                                messageText
                        );

        /*
         * "!" / "!   "
         */
        if (processedMessage
                .trim()
                .isEmpty()) {

            event.setCancelled(true);
            return;
        }

        /*
         * ========================================================
         * RENDER
         * ========================================================
         */

        Component finalFormatted =
                plugin.getChatFormatRenderer()
                        .render(
                                sender,
                                channel,
                                processedMessage,
                                event.message()
                        );

        /*
         * ========================================================
         * MENTIONS
         * ========================================================
         */

        finalFormatted =
                processMentions(
                        finalFormatted
                );

        /*
         * ========================================================
         * VIEWERS
         * ========================================================
         */

        event.viewers().removeIf(
                audience -> {

                    if (!(audience instanceof Player)) {
                        return false;
                    }

                    Player target =
                            (Player) audience;

                    return !plugin
                            .getChatChannelRouter()
                            .canReceiveMessage(
                                    sender,
                                    target,
                                    channel
                            );
                }
        );

        /*
         * ========================================================
         * UNHEARD MESSAGE
         * ========================================================
         */

        boolean someoneHeard = event.viewers().stream()
                .filter(audience -> audience instanceof Player)
                .map(audience -> (Player) audience)
                .anyMatch(player -> !player.equals(sender));

        if (!someoneHeard) {

            boolean globallyEnabled = plugin.getConfig().getBoolean(
                    "unheard-message.enabled",
                    true
            );

            if (globallyEnabled) {

                Boolean channelEnabled =
                        channel.getUnheardMessageEnabled();

                boolean enabled =
                        channelEnabled == null
                                || channelEnabled;

                if (enabled) {

                    String message =
                            channel.getUnheardMessage();

                    if (message == null || message.isEmpty()) {

                        message = plugin.getConfig().getString(
                                "unheard-message.message",
                                "<yellow>⚠ Вас никто не услышал.</yellow>"
                        );
                    }

                    message = message
                            .replace("%player%", sender.getName())
                            .replace(
                                    "%displayname%",
                                    sender.getDisplayName()
                            );

                    if (Bukkit.getPluginManager()
                            .isPluginEnabled("PlaceholderAPI")) {

                        message =
                                PlaceholderAPI.setPlaceholders(
                                        sender,
                                        message
                                );
                    }

                    Component notification =
                            plugin.getChatFormatRenderer()
                                    .parseFormat(message);

                    Bukkit.getScheduler().runTask(
                            plugin,
                            () -> sender.sendMessage(notification)
                    );
                }
            }
        }

        /*
         * ========================================================
         * RENDERER
         * ========================================================
         */

        Component result =
                finalFormatted;

        event.renderer(
                (
                        source,
                        sourceDisplayName,
                        messageComponent,
                        viewer
                ) -> result
        );
    }
}
