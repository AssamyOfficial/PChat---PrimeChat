package corp.prime.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpyManager implements Listener {
    private final PrimeChat plugin;
    private final Set<UUID> socialSpy = ConcurrentHashMap.newKeySet();
    private final Set<UUID> commandSpy = ConcurrentHashMap.newKeySet();

    public SpyManager(PrimeChat plugin) {
        this.plugin = plugin;
    }

    public boolean toggleSocialSpy(Player player) {
        if (!socialSpy.add(player.getUniqueId())) {
            socialSpy.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public boolean toggleCommandSpy(Player player) {
        if (!commandSpy.add(player.getUniqueId())) {
            commandSpy.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public boolean hasSocialSpy(Player player) {
        return socialSpy.contains(player.getUniqueId());
    }

    public boolean hasCommandSpy(Player player) {
        return commandSpy.contains(player.getUniqueId());
    }

    public void notifyPrivateMessage(Player sender, Player recipient, String message) {
        if (!plugin.getCommandConfig().getBoolean("commands.socialspy.enabled", true)) {
            return;
        }

        String format = plugin.getCommandConfig().getString(
                "commands.socialspy.format",
                "<gradient:#00E5FF:#7C4DFF>◆ SOCIALSPY</gradient> <gray>%sender% <dark_gray>→</dark_gray> %recipient%:</gray> <white>%message%</white>"
        );

        for (UUID uuid : socialSpy) {
            Player viewer = Bukkit.getPlayer(uuid);
            if (viewer == null || viewer.equals(sender) || viewer.equals(recipient)) {
                continue;
            }

            sendToPlayer(viewer, plugin.getChatFormatRenderer().parseFormat(
                    replace(format, sender, recipient, message)
            ));
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getCommandConfig().getBoolean("commands.commandspy.enabled", true)) {
            return;
        }

        Player sender = event.getPlayer();
        String command = event.getMessage();

        if (command.equalsIgnoreCase("/socialspy")
                || command.equalsIgnoreCase("/ss")
                || command.equalsIgnoreCase("/commandspy")
                || command.equalsIgnoreCase("/cs")) {
            return;
        }

        String format = plugin.getCommandConfig().getString(
                "commands.commandspy.format",
                "<gradient:#00E5FF:#7C4DFF>◆ COMMANDSPY</gradient> <gray>%player% <dark_gray>→</dark_gray></gray> <white>%command%</white>"
        );

        for (UUID uuid : commandSpy) {
            Player viewer = Bukkit.getPlayer(uuid);
            if (viewer == null || viewer.equals(sender)) {
                continue;
            }

            sendToPlayer(viewer, plugin.getChatFormatRenderer().parseFormat(
                    format.replace("%player%", sender.getName())
                            .replace("%displayname%", sender.getDisplayName())
                            .replace("%command%", command)
            ));
        }
    }

    private void sendToPlayer(Player player, net.kyori.adventure.text.Component message) {
        plugin.getPrimeScheduler().run(player, () -> player.sendMessage(message));
    }

    private String replace(String format, Player sender, Player recipient, String message) {
        return format.replace("%sender%", sender.getName())
                .replace("%sender_displayname%", sender.getDisplayName())
                .replace("%recipient%", recipient.getName())
                .replace("%recipient_displayname%", recipient.getDisplayName())
                .replace("%message%", message);
    }
}
