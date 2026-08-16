package corp.prime.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatChannelCommand implements CommandExecutor {
    private final PrimeChat plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatChannelCommand(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭту команду может использовать только игрок.");
            return true;
        }

        Player player = (Player) sender;
        ChatChannel channel = plugin.getChatChannelManager().getChannelByCommand(label);

        if (channel == null) {
            player.sendMessage("§cКанал не найден или отключён.");
            return true;
        }

        String permission = channel.getPermission();
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            player.sendMessage("§cУ вас нет доступа к этому чату.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§7Использование: §f/" + label + " <сообщение>");
            return true;
        }

        String message = String.join(" ", args);
        String format = channel.getFormat()
                .replace("%player%", "__PRIMECHAT_PLAYER__")
                .replace("%displayname%", "__PRIMECHAT_DISPLAYNAME__")
                .replace("%display_name%", "__PRIMECHAT_DISPLAYNAME__")
                .replace("%message%", "__PRIMECHAT_MESSAGE__");

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        format = format
                .replace("__PRIMECHAT_PLAYER__", player.getName())
                .replace("__PRIMECHAT_DISPLAYNAME__", player.getDisplayName())
                .replace("__PRIMECHAT_MESSAGE__", message)
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

        Component component = miniMessage.deserialize(format);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (plugin.getChatChannelRouter().canReceiveMessage(player, viewer, channel)) {
                viewer.sendMessage(component);
            }
        }

        return true;
    }
}
