package corp.prime.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class ChatFormatRenderer {

    private final PrimeChat plugin;

    private final MiniMessage miniMessage =
            MiniMessage.miniMessage();

    public ChatFormatRenderer(PrimeChat plugin) {
        this.plugin = plugin;
    }

    /*
     * ============================================================
     * FORMAT
     * ============================================================
     */

    public Component parseFormat(String format) {

        if (format == null || format.isEmpty()) {
            return Component.empty();
        }

        format = format
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");

        return miniMessage.deserialize(format);
    }

    /*
     * ============================================================
     * PLACEHOLDER API
     * ============================================================
     */

    private String processPlaceholderAPI(
            Player player,
            String format
    ) {

        if (!Bukkit.getPluginManager()
                .isPluginEnabled("PlaceholderAPI")) {

            return format;
        }

        /*
         * Защищаем PrimeChat placeholders.
         */
        format = format
                .replace(
                        "%player%",
                        "__PRIMECHAT_PLAYER__"
                )
                .replace(
                        "%displayname%",
                        "__PRIMECHAT_DISPLAYNAME__"
                )
                .replace(
                        "%display_name%",
                        "__PRIMECHAT_DISPLAYNAME__"
                )
                .replace(
                        "%message%",
                        "__PRIMECHAT_MESSAGE__"
                );

        /*
         * Обрабатываем PlaceholderAPI.
         */
        format = PlaceholderAPI.setPlaceholders(
                player,
                format
        );

        /*
         * Возвращаем внутренние placeholders.
         */
        return format
                .replace(
                        "__PRIMECHAT_PLAYER__",
                        "<player>"
                )
                .replace(
                        "__PRIMECHAT_DISPLAYNAME__",
                        "<displayname>"
                )
                .replace(
                        "__PRIMECHAT_MESSAGE__",
                        "<message>"
                );
    }

    /*
     * ============================================================
     * PLAYER HOVER
     * ============================================================
     */

    public Component createPlayerHover(
            Player player
    ) {

        List<String> lines =
                plugin.getConfig().getStringList(
                        "player-interaction.hover.text"
                );

        if (lines.isEmpty()) {

            lines = List.of(
                    "<aqua>👤 <bold>%player%</bold></aqua>",
                    "<gray>Игрок сервера</gray>",
                    "",
                    "<yellow>Нажмите, чтобы написать сообщение</yellow>"
            );
        }

        Component hover =
                Component.empty();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

            line = line
                    .replace(
                            "%player%",
                            player.getName()
                    )
                    .replace(
                            "%displayname%",
                            player.getDisplayName()
                    )
                    .replace(
                            "%display_name%",
                            player.getDisplayName()
                    );

            if (Bukkit.getPluginManager()
                    .isPluginEnabled("PlaceholderAPI")) {

                line = PlaceholderAPI.setPlaceholders(
                        player,
                        line
                );
            }

            hover = hover.append(
                    parseFormat(line)
            );

            if (i < lines.size() - 1) {
                hover = hover.append(
                        Component.newline()
                );
            }
        }

        return hover;
    }

    /*
     * ============================================================
     * PLAYER COMPONENT
     * ============================================================
     */

    private Component createPlayerComponent(
            Player player,
            Component hover,
            boolean hoverEnabled,
            boolean clickEnabled,
            String clickAction
    ) {

        Component component =
                Component.text(
                        player.getName()
                );

        if (hoverEnabled) {

            component =
                    component.hoverEvent(
                            HoverEvent.showText(
                                    hover
                            )
                    );
        }

        if (clickEnabled
                && clickAction.equalsIgnoreCase(
                "suggest-message"
        )) {

            component =
                    component.clickEvent(
                            ClickEvent.suggestCommand(
                                    "/msg "
                                            + player.getName()
                                            + " "
                            )
                    );
        }

        return component;
    }

    /*
     * ============================================================
     * DISPLAY NAME COMPONENT
     * ============================================================
     */

    private Component createDisplayNameComponent(
            Player player,
            Component hover,
            boolean hoverEnabled,
            boolean clickEnabled,
            String clickAction
    ) {

        Component component =
                player.displayName();

        if (hoverEnabled) {

            component =
                    component.hoverEvent(
                            HoverEvent.showText(
                                    hover
                            )
                    );
        }

        if (clickEnabled
                && clickAction.equalsIgnoreCase(
                "suggest-message"
        )) {

            component =
                    component.clickEvent(
                            ClickEvent.suggestCommand(
                                    "/msg "
                                            + player.getName()
                                            + " "
                            )
                    );
        }

        return component;
    }

    /*
     * ============================================================
     * RENDER CHANNEL
     * ============================================================
     */

    public Component render(
            Player player,
            ChatChannel channel,
            String messageText,
            Component originalMessage
    ) {

        if (channel == null) {
            return Component.empty();
        }

        String format =
                channel.getFormat();

        /*
         * PlaceholderAPI.
         */
        format =
                processPlaceholderAPI(
                        player,
                        format
                );

        /*
         * Парсим формат канала.
         */
        Component formatted =
                parseFormat(format);

        /*
         * Настройки Hover / Click.
         */
        boolean hoverEnabled =
                plugin.getConfig().getBoolean(
                        "player-interaction.hover.enabled",
                        true
                );

        boolean clickEnabled =
                plugin.getConfig().getBoolean(
                        "player-interaction.click.enabled",
                        true
                );

        String clickAction =
                plugin.getConfig().getString(
                        "player-interaction.click.action",
                        "suggest-message"
                );

        /*
         * Hover игрока.
         */
        Component playerHover =
                createPlayerHover(player);

        /*
         * ========================================================
         * %player%
         * ========================================================
         */

        Component playerComponent =
                createPlayerComponent(
                        player,
                        playerHover,
                        hoverEnabled,
                        clickEnabled,
                        clickAction
                );

        formatted =
                formatted.replaceText(
                        builder -> builder
                                .matchLiteral(
                                        "<player>"
                                )
                                .replacement(
                                        playerComponent
                                )
                );

        /*
         * ========================================================
         * %displayname%
         * ========================================================
         */

        Component displayNameComponent =
                createDisplayNameComponent(
                        player,
                        playerHover,
                        hoverEnabled,
                        clickEnabled,
                        clickAction
                );

        formatted =
                formatted.replaceText(
                        builder -> builder
                                .matchLiteral(
                                        "<displayname>"
                                )
                                .replacement(
                                        displayNameComponent
                                )
                );

        /*
         * ========================================================
         * %message%
         * ========================================================
         */

        Component messageComponent;

        /*
         * Если текст сообщения не изменялся,
         * сохраняем оригинальный Adventure Component.
         *
         * Это позволяет не терять форматирование
         * самого сообщения игрока.
         */
        String originalText =
                net.kyori.adventure.text.serializer.plain
                        .PlainTextComponentSerializer
                        .plainText()
                        .serialize(
                                originalMessage
                        );

        if (originalText.equals(messageText)) {

            messageComponent =
                    originalMessage;

        } else {

            /*
             * Например:
             *
             * !Привет
             *
             * превращается в:
             *
             * Привет
             */
            messageComponent =
                    Component.text(
                            messageText
                    );
        }

        formatted =
                formatted.replaceText(
                        builder -> builder
                                .matchLiteral(
                                        "<message>"
                                )
                                .replacement(
                                        messageComponent
                                )
                );

        return formatted;
    }
}

