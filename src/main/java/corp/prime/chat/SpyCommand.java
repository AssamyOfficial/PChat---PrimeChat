package corp.prime.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpyCommand implements CommandExecutor {
    private final PrimeChat plugin;
    private final SpyManager spyManager;
    private final boolean social;

    public SpyCommand(PrimeChat plugin, SpyManager spyManager, boolean social) {
        this.plugin = plugin;
        this.spyManager = spyManager;
        this.social = social;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, social ? "commands.socialspy.messages.player-only" : "commands.commandspy.messages.player-only",
                    "<red>Эта команда доступна только игрокам.</red>");
            return true;
        }

        String root = social ? "commands.socialspy" : "commands.commandspy";
        String permission = plugin.getCommandConfig().getString(
                root + ".permission",
                social ? "primechat.socialspy" : "primechat.commandspy"
        );

        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            send(player, root + ".messages.no-permission",
                    "<gradient:#FF4D6D:#7C4DFF>◆</gradient> <gray>У вас нет прав для использования этой функции.</gray>");
            return true;
        }

        boolean enabled = social
                ? spyManager.toggleSocialSpy(player)
                : spyManager.toggleCommandSpy(player);

        send(player,
                root + (enabled ? ".messages.enabled" : ".messages.disabled"),
                enabled
                        ? "<gradient:#00E5FF:#7C4DFF>◆</gradient> <gray>Наблюдение включено.</gray>"
                        : "<gradient:#FF4D6D:#7C4DFF>◆</gradient> <gray>Наблюдение выключено.</gray>");
        return true;
    }

    private void send(CommandSender sender, String path, String fallback) {
        String message = plugin.getCommandConfig().getString(path, fallback);
        sender.sendMessage(plugin.getChatFormatRenderer().parseFormat(message));
    }
}
