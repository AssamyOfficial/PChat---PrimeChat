package corp.prime.chat;

import org.bukkit.entity.Player;

public class ChatChannelRouter {
    private final PrimeChat plugin;

    public ChatChannelRouter(PrimeChat plugin) {
        this.plugin = plugin;
    }

    public ChatChannel getChannelForMessage(Player player, String message) {
        ChatChannelManager manager = plugin.getChatChannelManager();

        if (message == null || message.isEmpty()) {
            return null;
        }

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
                return null;
            }

            return channel;
        }

        ChatChannel local = manager.getChannel("local");
        if (local == null || !local.isEnabled()) {
            return null;
        }

        String permission = local.getPermission();
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            return null;
        }

        return local;
    }

    public String removeTrigger(ChatChannel channel, String message) {
        if (channel == null || message == null) {
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

        if (channel.isGlobal() || channel.isCommand()) {
            return true;
        }

        if (channel.isLocal()) {
            if (!sender.getWorld().equals(viewer.getWorld())) {
                return false;
            }

            double radius = channel.getRadius();
            if (radius <= 0) {
                return sender.equals(viewer);
            }

            return sender.getLocation().distanceSquared(viewer.getLocation()) <= radius * radius;
        }

        return sender.equals(viewer);
    }
}
