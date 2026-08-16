package corp.prime.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatChannelCommandHandler implements TabExecutor {

    private final PrimeChat plugin;

    public ChatChannelCommandHandler(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам.");
            return true;
        }

        ChatChannel channel =
                plugin.getChatChannelManager()
                        .getChannelByCommand(label);

        if (channel == null) {
            return false;
        }

        if (!channel.isEnabled()) {
            player.sendMessage("§cЭтот чат отключён.");
            return true;
        }

        String permission = channel.getPermission();

        if (permission != null
                && !permission.isEmpty()
                && !player.hasPermission(permission)) {

            player.sendMessage(
                    "§cУ вас нет прав для использования этого чата."
            );

            return true;
        }

        if (args.length == 0) {

            player.sendMessage(
                    "§7Использование: §f/"
                            + label
                            + " <сообщение>"
            );

            return true;
        }

        String message = String.join(" ", args);

        plugin.getChatChannelManager()
                .sendCommandChannelMessage(
                        player,
                        channel,
                        message
                );

        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        return Collections.emptyList();
    }
}