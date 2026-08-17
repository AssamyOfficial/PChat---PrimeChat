package corp.prime.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AfkManager implements Listener {
    private final PrimeChat plugin;
    private final Set<UUID> afk = new HashSet<>();

    public AfkManager(PrimeChat plugin) {
        this.plugin = plugin;
    }

    public boolean toggle(Player player) {
        if (afk.remove(player.getUniqueId())) return false;
        afk.add(player.getUniqueId());
        return true;
    }

    public boolean isAfk(Player player) {
        return afk.contains(player.getUniqueId());
    }

    public void remove(Player player) {
        afk.remove(player.getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
            remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    public String getStatus(Player player) {
        if (!isAfk(player)) return "";
        return plugin.getCommandConfig().getString("commands.afk.status-suffix", " <gray>[AFK]</gray>");
    }
}
