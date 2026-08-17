package corp.prime.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandControlManager implements Listener {
    private final PrimeChat plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Map<String, Long>> delayedExecutions = new HashMap<>();

    public CommandControlManager(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String raw = event.getMessage();

        if (raw.length() <= 1 || !raw.startsWith("/")) {
            return;
        }

        String command = raw.substring(1).split(" ")[0].toLowerCase();
        String bypass = plugin.getCommandConfig().getString(
                "commands.command-control.bypass-permission",
                "primechat.commandcontrol.bypass"
        );

        if (!bypass.isEmpty() && player.hasPermission(bypass)) {
            return;
        }

        if (isListed("commands.command-control.whitelist", command)
                && plugin.getCommandConfig().getBoolean(
                "commands.command-control.whitelist-enabled", false)) {
            return;
        }

        if (plugin.getCommandConfig().getBoolean(
                "commands.command-control.whitelist-enabled", false)) {
            event.setCancelled(true);
            send(player,
                    "commands.command-control.messages.not-whitelisted",
                    "<red>Эта команда не разрешена.</red>",
                    null
            );
            return;
        }

        if (plugin.getCommandConfig().getBoolean(
                "commands.command-control.blacklist-enabled", false)
                && isListed("commands.command-control.blacklist", command)) {
            event.setCancelled(true);
            send(player,
                    "commands.command-control.messages.blacklisted",
                    "<red>Эта команда запрещена.</red>",
                    null
            );
            return;
        }

        long seconds = plugin.getCommandConfig().getLong(
                "commands.command-control.delays." + command,
                0L
        );

        if (seconds <= 0) {
            return;
        }

        String delayBypass = plugin.getCommandConfig().getString(
                "commands.command-control.delay-permissions.bypass",
                "primechat.commanddelay.bypass"
        );

        if (!delayBypass.isEmpty() && player.hasPermission(delayBypass)) {
            return;
        }

        String reducePermission = plugin.getCommandConfig().getString(
                "commands.command-control.delay-permissions.reduce",
                "primechat.commanddelay.reduce"
        );

        if (!reducePermission.isEmpty() && player.hasPermission(reducePermission)) {
            seconds = plugin.getCommandConfig().getLong(
                    "commands.command-control.delay-permissions.reduced-seconds",
                    seconds
            );
        }

        if (seconds <= 0) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(uuid, key -> new HashMap<>());
        long until = playerCooldowns.getOrDefault(command, 0L);

        if (until > now) {
            long left = Math.max(1, (until - now + 999) / 1000);
            event.setCancelled(true);
            send(player,
                    "commands.command-control.messages.cooldown",
                    "<yellow>Подождите <white>%seconds%</white> сек.</yellow>",
                    String.valueOf(left)
            );
            return;
        }

        playerCooldowns.put(command, now + seconds * 1000L);
    }

    private boolean isListed(String path, String command) {
        List<String> list = plugin.getCommandConfig().getStringList(path);

        for (String entry : list) {
            if (entry == null) {
                continue;
            }

            String value = entry.trim().toLowerCase();

            if (value.startsWith("/")) {
                value = value.substring(1);
            }

            if (value.equals(command)) {
                return true;
            }
        }

        return false;
    }

    private void send(Player player, String path, String fallback, String seconds) {
        String message = plugin.getCommandConfig().getString(path, fallback);

        if (seconds != null) {
            message = message.replace("%seconds%", seconds);
        }

        player.sendMessage(
                plugin.getChatFormatRenderer().parseFormat(message)
        );
    }
}
