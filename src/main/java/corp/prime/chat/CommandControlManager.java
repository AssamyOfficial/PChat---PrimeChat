package corp.prime.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CommandControlManager implements Listener {
    private final PrimeChat plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Set<UUID> delayedExecution = new HashSet<>();

    public CommandControlManager(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String raw = event.getMessage();
        String command = raw.substring(1).split(" ")[0].toLowerCase();

        if (delayedExecution.remove(player.getUniqueId())) {
            return;
        }

        if (isListed("commands.command-control.whitelist", command)) {
            if (!plugin.getCommandConfig().getBoolean("commands.command-control.whitelist-enabled", false)) {
                return;
            }
        }

        if (plugin.getCommandConfig().getBoolean("commands.command-control.whitelist-enabled", false)
                && !isListed("commands.command-control.whitelist", command)) {
            event.setCancelled(true);
            send(player, "commands.command-control.messages.not-whitelisted", "<red>Эта команда недоступна.</red>");
            return;
        }

        if (plugin.getCommandConfig().getBoolean("commands.command-control.blacklist-enabled", false)
                && isListed("commands.command-control.blacklist", command)) {
            event.setCancelled(true);
            send(player, "commands.command-control.messages.blacklisted", "<red>Эта команда запрещена.</red>");
            return;
        }

        String path = "commands.command-control.delays." + command;
        if (!plugin.getCommandConfig().getBoolean(path + ".enabled", false)) {
            return;
        }

        long seconds = plugin.getCommandConfig().getLong(path + ".seconds", 0L);
        if (seconds <= 0) {
            return;
        }

        String bypass = plugin.getCommandConfig().getString(path + ".bypass-permission", "");
        if (!bypass.isEmpty() && player.hasPermission(bypass)) {
            return;
        }

        long now = System.currentTimeMillis();
        long until = cooldowns
                .computeIfAbsent(player.getUniqueId(), key -> new HashMap<>())
                .getOrDefault(command, 0L);

        if (until > now) {
            long left = Math.max(1, (until - now + 999) / 1000);
            event.setCancelled(true);
            send(player, "commands.command-control.messages.cooldown", "<yellow>Подождите %seconds% сек.</yellow>".replace("%seconds%", String.valueOf(left)));
            return;
        }

        cooldowns.get(player.getUniqueId()).put(command, now + seconds * 1000L);

        if (seconds > 0 && plugin.getCommandConfig().getBoolean(path + ".delay-execution", false)) {
            event.setCancelled(true);
            delayedExecution.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.performCommand(raw.substring(1));
                }
                delayedExecution.remove(player.getUniqueId());
            }, seconds * 20L);
        }
    }

    private boolean isListed(String path, String command) {
        List<String> list = plugin.getCommandConfig().getStringList(path);
        for (String entry : list) {
            if (entry == null) continue;
            String value = entry.trim().toLowerCase();
            if (value.startsWith("/")) value = value.substring(1);
            if (value.equals(command)) return true;
        }
        return false;
    }

    private void send(Player player, String path, String fallback) {
        String message = plugin.getCommandConfig().getString(path, fallback);
        player.sendMessage(plugin.getChatFormatRenderer().parseFormat(message));
    }
}
