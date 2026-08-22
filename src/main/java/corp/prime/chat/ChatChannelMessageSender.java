package corp.prime.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatChannelMessageSender {
    private final PrimeChat plugin;

    public ChatChannelMessageSender(PrimeChat plugin) {
        this.plugin = plugin;
    }

    public void send(Player sender, ChatChannel channel, String message) {
        if (channel == null || !channel.isEnabled() || message == null || message.isBlank()) {
            return;
        }

        String permission = channel.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            plugin.getPrimeScheduler().run(sender, () ->
                    sender.sendMessage("§cУ вас нет прав для использования этого чата.")
            );
            return;
        }

        Component formatted = plugin.getChatChannelFormatter().format(sender, channel, message);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!plugin.getChatChannelRouter().canReceiveMessage(sender, viewer, channel)) {
                continue;
            }

            plugin.getPrimeScheduler().run(viewer, () -> viewer.sendMessage(formatted));
        }
    }
}
