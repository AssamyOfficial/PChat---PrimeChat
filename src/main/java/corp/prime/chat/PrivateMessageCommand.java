package corp.prime.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PrivateMessageCommand implements CommandExecutor {
    private final PrimeChat plugin;
    private final Map<UUID, UUID> lastReply = new HashMap<>();

    public PrivateMessageCommand(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "commands.msg.messages.player-only", "<red>Эта команда доступна только игрокам.</red>");
            return true;
        }

        if (command.getName().equalsIgnoreCase("r")) {
            return handleReply(player, args);
        }

        return handleMessage(player, args);
    }

    private boolean handleMessage(Player sender, String[] args) {
        if (!plugin.getCommandConfig().getBoolean("commands.msg.enabled", true)) {
            sendMessage(sender, "commands.msg.messages.disabled", "<red>Личные сообщения отключены.</red>");
            return true;
        }

        if (!hasPermission(sender)) {
            sendMessage(sender, "commands.msg.messages.no-permission", "<red>У вас нет прав для использования личных сообщений.</red>");
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "commands.msg.messages.usage", "<gray>Использование: <white>/msg <игрок> <сообщение></white></gray>");
            return true;
        }

        Player target = findPlayer(args[0]);

        if (target == null) {
            sendMessage(sender, "commands.msg.messages.player-not-found", "<red>Игрок <white>%player%</white> не найден или не в сети.</red>", args[0], sender);
            return true;
        }

        if (target.equals(sender)) {
            sendMessage(sender, "commands.msg.messages.self", "<yellow>Вы не можете отправить сообщение самому себе.</yellow>");
            return true;
        }

        String message = join(args, 1);
        sendPrivateMessage(sender, target, message);
        return true;
    }

    private boolean handleReply(Player sender, String[] args) {
        if (!plugin.getCommandConfig().getBoolean("commands.r.enabled", true)) {
            sendMessage(sender, "commands.r.messages.disabled", "<red>Ответы на личные сообщения отключены.</red>");
            return true;
        }

        if (!hasPermission(sender)) {
            sendMessage(sender, "commands.r.messages.no-permission", "<red>У вас нет прав для использования личных сообщений.</red>");
            return true;
        }

        if (args.length == 0) {
            sendMessage(sender, "commands.r.messages.usage", "<gray>Использование: <white>/r <сообщение></white></gray>");
            return true;
        }

        UUID targetId = lastReply.get(sender.getUniqueId());

        if (targetId == null) {
            sendMessage(sender, "commands.r.messages.no-target", "<yellow>У вас нет собеседника для ответа.</yellow>");
            return true;
        }

        Player target = Bukkit.getPlayer(targetId);

        if (target == null) {
            sendMessage(sender, "commands.r.messages.target-offline", "<yellow>Ваш последний собеседник сейчас не в сети.</yellow>");
            return true;
        }

        if (target.equals(sender)) {
            lastReply.remove(sender.getUniqueId());
            sendMessage(sender, "commands.r.messages.no-target", "<yellow>У вас нет собеседника для ответа.</yellow>");
            return true;
        }

        String message = join(args, 0);
        sendPrivateMessage(sender, target, message);
        return true;
    }

    private void sendPrivateMessage(Player sender, Player target, String message) {
        lastReply.put(sender.getUniqueId(), target.getUniqueId());
        lastReply.put(target.getUniqueId(), sender.getUniqueId());

        sendFormatted("commands.msg.messages.format.outgoing", sender, target, message);
        sendFormatted("commands.msg.messages.format.incoming", target, sender, message);
    }

    private void sendFormatted(String path, Player recipient, Player other, String message) {
        List<String> lines = plugin.getCommandConfig().getStringList(path);

        if (lines.isEmpty()) {
            String fallback = path.endsWith("outgoing")
                    ? "<gray>Вы → <white>%recipient%</white></gray>|<white>%message%</white>"
                    : "<gray>От <white>%sender%</white></gray>|<white>%message%</white>";
            lines = List.of(fallback);
        }

        for (String line : lines) {
            String formatted = line
                    .replace("%sender%", other.getName())
                    .replace("%sender_displayname%", other.getDisplayName())
                    .replace("%recipient%", recipient.getName())
                    .replace("%recipient_displayname%", recipient.getDisplayName())
                    .replace("%message%", message);

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                formatted = PlaceholderAPI.setPlaceholders(recipient, formatted);
            }

            Component component = plugin.getChatFormatRenderer().parseFormat(formatted);
            recipient.sendMessage(component);
        }
    }

    private boolean hasPermission(Player player) {
        String permission = plugin.getCommandConfig().getString("commands.msg.permission", "primechat.msg");
        return permission.isEmpty() || player.hasPermission(permission);
    }

    private Player findPlayer(String name) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    private String join(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private void sendMessage(CommandSender recipient, String path, String fallback) {
        sendMessage(recipient, path, fallback, null, recipient instanceof Player player ? player : null);
    }

    private void sendMessage(CommandSender recipient, String path, String fallback, String playerName, Player placeholderPlayer) {
        String message = plugin.getCommandConfig().getString(path, fallback);
        if (playerName != null) {
            message = message.replace("%player%", playerName);
        }
        if (placeholderPlayer != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(placeholderPlayer, message);
        }
        recipient.sendMessage(plugin.getChatFormatRenderer().parseFormat(message));
    }
}
