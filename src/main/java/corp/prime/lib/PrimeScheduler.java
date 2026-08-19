package corp.prime.lib;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrimeScheduler {
    private final JavaPlugin plugin;

    public PrimeScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void run(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public void runLater(Runnable task, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    public void runAsyncLater(Runnable task, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }

    public void run(Player player, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
