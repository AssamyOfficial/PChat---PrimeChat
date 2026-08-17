package corp.prime.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand implements CommandExecutor {
    private final PrimeChat plugin;
    private final AfkManager afkManager;

    public AfkCommand(PrimeChat plugin, AfkManager afkManager) {
        this.plugin = plugin;
        this.afkManager = afkManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getChatFormatRenderer().parseFormat(
                    plugin.getCommandConfig().getString("commands.afk.messages.player-only", "<red>Только игрок может использовать эту команду.</red>")));
            return true;
        }

        String permission = plugin.getCommandConfig().getString("commands.afk.permission", "primechat.afk");
        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            player.sendMessage(plugin.getChatFormatRenderer().parseFormat(
                    plugin.getCommandConfig().getString("commands.afk.messages.no-permission", "<red>У вас нет прав.</red>")));
            return true;
        }

        boolean enabled = afkManager.toggle(player);
        String path = enabled ? "commands.afk.messages.enabled" : "commands.afk.messages.disabled";
        player.sendMessage(plugin.getChatFormatRenderer().parseFormat(
                plugin.getCommandConfig().getString(path, enabled
                        ? "<gradient:#00E5FF:#7C4DFF>◆</gradient> <gray>Вы отошли от клавиатуры.</gray>"
                        : "<gradient:#00E5FF:#7C4DFF>◆</gradient> <gray>Вы снова активны.</gray>")));
        return true;
    }
}
