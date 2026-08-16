package corp.prime.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PrimeChatCommand implements CommandExecutor {
    private final PrimeChat plugin;

    public PrimeChatCommand(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§b§lPrimeChat");
            sender.sendMessage("§7/primechat reload §8- §fперезагрузить конфигурацию");
            sender.sendMessage("§7/primechat version §8- §fпоказать версию");
            sender.sendMessage("§7/primechat help §8- §fпоказать помощь");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getChatChannelManager().loadChannels();
            sender.sendMessage("§aPrimeChat: конфигурация и чат-каналы перезагружены!");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
            sender.sendMessage("§b§lPrimeChat");
            sender.sendMessage("§7Версия: §f1.2.0");
            sender.sendMessage("§7Разработчик: §fPrimeDev");
            return true;
        }

        sender.sendMessage("§7Использование: §f/primechat help");
        return true;
    }
}
