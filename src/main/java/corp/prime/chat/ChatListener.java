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

    private Player findMentionedPlayer(
            String message
    ) {

        boolean enabled =
                plugin.getConfig().getBoolean(
                        "mentions.enabled",
                        true
                );

        if (!enabled) {
            return null;
        }

        String symbol =
                plugin.getConfig().getString(
                        "mentions.symbol",
                        "@"
                );

        for (Player player :
                Bukkit.getOnlinePlayers()) {

            String mention =
                    symbol + player.getName();

            if (message.contains(mention)) {
                return player;
            }
        }

        return null;
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

        for (Player player :
                Bukkit.getOnlinePlayers()) {

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
     * MENTION NOTIFICATION
     * ============================================================
     */

    private void sendMentionNotification(
            Player mentionedPlayer,
            Player sender
    ) {

        Bukkit.getScheduler().runTask(
                plugin,
                () -> {

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
                            .isPluginEnabled(
                                    "PlaceholderAPI"
                            )) {

                        message =
                                PlaceholderAPI.setPlaceholders(
                                        sender,
                                        message
                                );
                    }

                    mentionedPlayer.sendMessage(
                            plugin.getChatFormatRenderer()
                                    .parseFormat(message)
                    );
                }
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

        Player mentionedPlayer =
                findMentionedPlayer(
                        messageText
                );

        if (mentionedPlayer != null) {

            plugin.getLogger().info(
                    "Обнаружено упоминание: "
                            + mentionedPlayer.getName()
                            + " от "
                            + sender.getName()
            );

            sendMentionNotification(
                    mentionedPlayer,
                    sender
            );
        }

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
