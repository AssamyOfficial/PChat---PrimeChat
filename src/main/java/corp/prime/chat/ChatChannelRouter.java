package corp.prime.chat;

import org.bukkit.entity.Player;

public class ChatChannelRouter {
    private final PrimeChat plugin;

    public ChatChannelRouter(PrimeChat plugin) {
        this.plugin = plugin;
    }

    public ChatChannel getChannelForMessage(Player player, String message) {
        ChatChannelManager manager = plugin.getChatChannelManager();

        for (ChatChannel channel : manager.getChannels()) {
            if (!channel.isEnabled()) {
                continue;
            }

            String trigger = channel.getTrigger();
            if (trigger == null || trigger.isEmpty() || !message.startsWith(trigger)) {
                continue;
            }

            String permission = channel.getPermission();
            if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
                player.sendMessage("§cУ вас нет прав для использования этого чата.");
                return null;
            }

            return channel;
        }

        ChatChannel local = manager.getChannel("local");
        if (local != null && local.isEnabled()) {
            String permission = local.getPermission();
            if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
                return null;
            }
            return local;
        }

        return null;
    }

    public String removeTrigger(ChatChannel channel, String message) {
        if (channel == null) {
            return message;
        }

        String trigger = channel.getTrigger();
        if (trigger == null || trigger.isEmpty() || !message.startsWith(trigger)) {
            return message;
        }

        return message.substring(trigger.length());
    }

    public boolean canReceiveMessage(Player sender, Player viewer, ChatChannel channel) {
        if (channel == null || !channel.isEnabled()) {
            return false;
        }

        String permission = channel.getPermission();
        if (permission != null && !permission.isEmpty() && !viewer.hasPermission(permission)) {
            return false;
        }

        if (channel.isGlobal()) {
            return true;
        }

        if (channel.isLocal()) {
            if (!sender.getWorld().equals(viewer.getWorld())) {
                return false;
            }

            double radius = channel.getRadius();
            return sender.getLocation().distanceSquared(viewer.getLocation()) <= radius * radius;
        }

        if (channel.isCommand()) {
            return true;
        }

        return sender.equals(viewer);
    }
}
