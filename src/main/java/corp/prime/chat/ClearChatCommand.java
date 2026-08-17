package corp.prime.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearChatCommand implements CommandExecutor {
    private final PrimeChat plugin;

    public ClearChatCommand(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String permission = plugin.getCommandConfig().getString("commands.clearchat.permission", "primechat.clearchat");
        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            sender.sendMessage(plugin.getChatFormatRenderer().parseFormat(
                    plugin.getCommandConfig().getString("commands.clearchat.messages.no-permission", "<red>У вас нет прав.</red>")));
            return true;
        }

        String blank = " ";
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) player.sendMessage(blank);
        }

        sender.sendMessage(plugin.getChatFormatRenderer().parseFormat(
                plugin.getCommandConfig().getString("commands.clearchat.messages.success", "<gradient:#00E5FF:#7C4DFF>◆</gradient> <gray>Чат очищен.</gray>")));
        return true;
    }
}
