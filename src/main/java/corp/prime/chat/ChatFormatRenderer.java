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
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final boolean placeholderApiEnabled;

    public ChatFormatRenderer(PrimeChat plugin) {
        this.plugin = plugin;
        this.placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public Component parseFormat(String format) {
        if (format == null || format.isEmpty()) {
            return Component.empty();
        }

        if (format.indexOf('&') >= 0) {
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
        }

        return miniMessage.deserialize(format);
    }

    private String processPlaceholderAPI(Player player, String format) {
        if (!placeholderApiEnabled) {
            return format;
        }

        format = format
                .replace("%player%", "__PRIMECHAT_PLAYER__")
                .replace("%displayname%", "__PRIMECHAT_DISPLAYNAME__")
                .replace("%display_name%", "__PRIMECHAT_DISPLAYNAME__")
                .replace("%message%", "__PRIMECHAT_MESSAGE__");

        format = PlaceholderAPI.setPlaceholders(player, format);

        return format
                .replace("__PRIMECHAT_PLAYER__", "<player>")
                .replace("__PRIMECHAT_DISPLAYNAME__", "<displayname>")
                .replace("__PRIMECHAT_MESSAGE__", "<message>");
    }

    public Component createPlayerHover(Player player) {
        List<String> lines = plugin.getConfig().getStringList("player-interaction.hover.text");

        if (lines.isEmpty()) {
            lines = List.of(
                    "<aqua>👤 <bold>%player%</bold></aqua>",
                    "<gray>Игрок сервера</gray>",
                    "",
                    "<yellow>Нажмите, чтобы написать сообщение</yellow>"
            );
        }

        Component hover = Component.empty();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i)
                    .replace("%player%", player.getName())
                    .replace("%displayname%", player.getDisplayName())
                    .replace("%display_name%", player.getDisplayName());

            if (placeholderApiEnabled) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            hover = hover.append(parseFormat(line));

            if (i < lines.size() - 1) {
                hover = hover.append(Component.newline());
            }
        }

        return hover;
    }

    private Component createPlayerComponent(Player player, Component hover, boolean hoverEnabled, boolean clickEnabled, String clickAction) {
        Component component = Component.text(player.getName());

        if (hoverEnabled) {
            component = component.hoverEvent(HoverEvent.showText(hover));
        }

        if (clickEnabled && clickAction.equalsIgnoreCase("suggest-message")) {
            component = component.clickEvent(
                    ClickEvent.suggestCommand("/msg " + player.getName() + " ")
            );
        }

        return component;
    }

    private Component createDisplayNameComponent(Player player, Component hover, boolean hoverEnabled, boolean clickEnabled, String clickAction) {
        Component component = player.displayName();

        if (hoverEnabled) {
            component = component.hoverEvent(HoverEvent.showText(hover));
        }

        if (clickEnabled && clickAction.equalsIgnoreCase("suggest-message")) {
            component = component.clickEvent(
                    ClickEvent.suggestCommand("/msg " + player.getName() + " ")
            );
        }

        return component;
    }

    public Component render(Player player, ChatChannel channel, String messageText, Component originalMessage) {
        if (channel == null) {
            return Component.empty();
        }

        String format = processPlaceholderAPI(player, channel.getFormat());
        Component formatted = parseFormat(format);

        boolean hoverEnabled = plugin.getConfig().getBoolean(
                "player-interaction.hover.enabled",
                true
        );

        boolean clickEnabled = plugin.getConfig().getBoolean(
                "player-interaction.click.enabled",
                true
        );

        String clickAction = plugin.getConfig().getString(
                "player-interaction.click.action",
                "suggest-message"
        );

        Component playerHover = createPlayerHover(player);

        Component playerComponent = createPlayerComponent(
                player,
                playerHover,
                hoverEnabled,
                clickEnabled,
                clickAction
        );

        formatted = formatted.replaceText(builder -> builder
                .matchLiteral("<player>")
                .replacement(playerComponent)
        );

        Component displayNameComponent = createDisplayNameComponent(
                player,
                playerHover,
                hoverEnabled,
                clickEnabled,
                clickAction
        );

        formatted = formatted.replaceText(builder -> builder
                .matchLiteral("<displayname>")
                .replacement(displayNameComponent)
        );

        Component messageComponent = parsePlayerMessage(player, messageText);

        return formatted.replaceText(builder -> builder
                .matchLiteral("<message>")
                .replacement(messageComponent)
        );
    }

    private Component parsePlayerMessage(Player player, String message) {
        boolean enabled = plugin.getConfig().getBoolean(
                "chat-color.enabled",
                true
        );

        if (!enabled) {
            return Component.text(message);
        }

        String permission = plugin.getConfig().getString(
                "chat-color.permission",
                "primechat.chatcolor"
        );

        if (permission != null
                && !permission.isEmpty()
                && !player.hasPermission(permission)) {
            return Component.text(message);
        }

        return parseFormat(message);
    }
}
