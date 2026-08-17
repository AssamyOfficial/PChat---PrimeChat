package corp.prime.chat;

import org.bukkit.entity.Player;

public class ChatManager {
    private final PrimeChat plugin;
    private boolean enabled = true;

    public ChatManager(PrimeChat plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public boolean canBypass(Player player) {
        String permission = plugin.getCommandConfig().getString(
                "commands.chat.bypass-permission",
                "primechat.chat.bypass"
        );
        return permission.isEmpty() || player.hasPermission(permission);
    }
}
