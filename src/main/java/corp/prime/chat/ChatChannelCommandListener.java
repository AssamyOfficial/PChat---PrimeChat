package corp.prime.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatChannelCommandListener implements Listener {
    private final PrimeChat plugin;

    public ChatChannelCommandListener(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message == null || message.length() <= 1 || !message.startsWith("/")) {
            return;
        }

        String commandLine = message.substring(1).trim();
        if (commandLine.isEmpty()) {
            return;
        }

        String[] parts = commandLine.split("\\s+");
        if (parts.length == 0) {
            return;
        }

        String commandName = parts[0];
        ChatChannel channel = plugin.getChatChannelManager().getChannelByCommand(commandName);
        if (channel == null) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        String permission = channel.getPermission();

        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            player.sendMessage("§cУ вас нет прав для использования этого чата.");
            return;
        }

        if (parts.length == 1) {
            player.sendMessage("§7Использование: §f/" + commandName + " <сообщение>");
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(parts[i]);
        }

        plugin.getChatChannelMessageSender().send(
                player,
                channel,
                messageBuilder.toString()
        );
    }
}
