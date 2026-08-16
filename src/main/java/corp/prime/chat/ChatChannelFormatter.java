package corp.prime.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class ChatChannelFormatter {
    private final PrimeChat plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatChannelFormatter(PrimeChat plugin) {
        this.plugin = plugin;
    }

    public Component format(Player player, ChatChannel channel, String message) {
        String format = channel.getFormat();
        if (format == null || format.isEmpty()) {
            format = "<white>%player%</white> <gray>»</gray> <white>%message%</white>";
        }

        format = format
                .replace("%player%", "__PRIMECHAT_PLAYER__")
                .replace("%displayname%", "__PRIMECHAT_DISPLAYNAME__")
                .replace("%message%", "__PRIMECHAT_MESSAGE__");

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        format = format
                .replace("__PRIMECHAT_PLAYER__", "<player>")
                .replace("__PRIMECHAT_DISPLAYNAME__", "<displayname>")
                .replace("__PRIMECHAT_MESSAGE__", "<message>");

        format = translateColors(format);
        Component result = miniMessage.deserialize(format);

        boolean hoverEnabled = plugin.getConfig().getBoolean("player-interaction.hover.enabled", true);
        boolean clickEnabled = plugin.getConfig().getBoolean("player-interaction.click.enabled", true);
        String clickAction = plugin.getConfig().getString("player-interaction.click.action", "suggest-message");
        Component playerHover = createPlayerHover(player);

        Component playerComponent = Component.text(player.getName());
        if (hoverEnabled) {
            playerComponent = playerComponent.hoverEvent(HoverEvent.showText(playerHover));
        }
        if (clickEnabled && clickAction.equalsIgnoreCase("suggest-message")) {
            playerComponent = playerComponent.clickEvent(
                    ClickEvent.suggestCommand("/msg " + player.getName() + " ")
            );
        }

        Component finalPlayerComponent = playerComponent;
        result = result.replaceText(builder -> builder
                .matchLiteral("<player>")
                .replacement(finalPlayerComponent));

        Component displayNameComponent = player.displayName();
        if (hoverEnabled) {
            displayNameComponent = displayNameComponent.hoverEvent(HoverEvent.showText(playerHover));
        }
        if (clickEnabled && clickAction.equalsIgnoreCase("suggest-message")) {
            displayNameComponent = displayNameComponent.clickEvent(
                    ClickEvent.suggestCommand("/msg " + player.getName() + " ")
            );
        }

        Component finalDisplayNameComponent = displayNameComponent;
        result = result.replaceText(builder -> builder
                .matchLiteral("<displayname>")
                .replacement(finalDisplayNameComponent));

        Component messageComponent = Component.text(message);
        return result.replaceText(builder -> builder
                .matchLiteral("<message>")
                .replacement(messageComponent));
    }

    private Component createPlayerHover(Player player) {
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

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            Component lineComponent = miniMessage.deserialize(translateColors(line));
            hover = hover.append(lineComponent);
            if (i < lines.size() - 1) {
                hover = hover.append(Component.newline());
            }
        }
        return hover;
    }

    private String translateColors(String text) {
        return text
                .replace("&0", "<black>").replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>")
                .replace("&8", "<dark_gray>").replace("&9", "<blue>")
                .replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>")
                .replace("&e", "<yellow>").replace("&f", "<white>")
                .replace("&k", "<obfuscated>").replace("&l", "<bold>")
                .replace("&m", "<strikethrough>").replace("&n", "<underlined>")
                .replace("&o", "<italic>").replace("&r", "<reset>");
    }
}
