package corp.prime.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatCommand implements CommandExecutor {
    private final PrimeChat plugin;

    public ChatCommand(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "commands.chat.messages.player-only", "<red>◆</red> <gray>Только игрок может использовать эту команду.</gray>");
            return true;
        }

        String permission = plugin.getCommandConfig().getString("commands.chat.permission", "primechat.chat.toggle");
        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            send(player, "commands.chat.messages.no-permission", "<red>◆</red> <gray>У вас нет прав на управление чатом.</gray>");
            return true;
        }

        if (args.length == 0) {
            boolean enabled = plugin.getChatManager().isEnabled();
            send(player, enabled ? "commands.chat.messages.status-enabled" : "commands.chat.messages.status-disabled",
                    enabled ? "<green>◆</green> <gray>Чат сейчас включён.</gray>" : "<red>◆</red> <gray>Чат сейчас выключен.</gray>");
            return true;
        }

        if (args.length != 1 || (!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off"))) {
            send(player, "commands.chat.messages.usage", "<red>◆</red> <gray>Использование: <white>/chat <on|off></white></gray>");
            return true;
        }

        boolean requested = args[0].equalsIgnoreCase("on");
        boolean current = plugin.getChatManager().isEnabled();
        if (requested != current) {
            plugin.getChatManager().toggle();
        }

        send(player, requested ? "commands.chat.messages.enabled" : "commands.chat.messages.disabled",
                requested ? "<green>◆</green> <gray>Чат включён.</gray>" : "<red>◆</red> <gray>Чат выключен.</gray>");
        return true;
    }

    private void send(CommandSender sender, String path, String fallback) {
        sender.sendMessage(plugin.getChatFormatRenderer().parseFormat(
                plugin.getCommandConfig().getString(path, fallback)
        ));
    }
}
