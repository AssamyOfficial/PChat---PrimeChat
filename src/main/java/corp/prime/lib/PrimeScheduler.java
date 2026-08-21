package corp.prime.lib;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class PrimeScheduler {
    private final JavaPlugin plugin;
    private final boolean folia;

    public PrimeScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folia = detectFolia();
    }

    public void run(Runnable task) {
        if (folia && runGlobal(task, 0L)) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void runAsync(Runnable task) {
        if (folia && runAsyncFolia(task, 0L)) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public void runLater(Runnable task, long delay) {
        if (folia && runGlobal(task, delay)) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    public void runAsyncLater(Runnable task, long delay) {
        if (folia && runAsyncFolia(task, delay)) {
            return;
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }

    public void run(Player player, Runnable task) {
        if (player != null && folia && runEntity(player, task, 0L)) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void runLater(Player player, Runnable task, long delay) {
        if (player != null && folia && runEntity(player, task, delay)) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    private boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private boolean runGlobal(Runnable task, long delay) {
        try {
            Method getter = Bukkit.class.getMethod("getGlobalRegionScheduler");
            Object scheduler = getter.invoke(null);
            Method runDelayed = scheduler.getClass().getMethod(
                    "runDelayed",
                    JavaPlugin.class,
                    Consumer.class,
                    long.class
            );
            Consumer<Object> consumer = ignored -> task.run();
            runDelayed.invoke(scheduler, plugin, consumer, delay);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private boolean runAsyncFolia(Runnable task, long delay) {
        try {
            Method getter = Bukkit.class.getMethod("getAsyncScheduler");
            Object scheduler = getter.invoke(null);
            Method runDelayed = scheduler.getClass().getMethod(
                    "runDelayed",
                    JavaPlugin.class,
                    Consumer.class,
                    long.class,
                    java.util.concurrent.TimeUnit.class
            );
            Consumer<Object> consumer = ignored -> task.run();
            runDelayed.invoke(
                    scheduler,
                    plugin,
                    consumer,
                    Math.max(1L, delay),
                    java.util.concurrent.TimeUnit.MILLISECONDS
            );
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private boolean runEntity(Player player, Runnable task, long delay) {
        try {
            Method getter = player.getClass().getMethod("getScheduler");
            Object scheduler = getter.invoke(player);
            Method runDelayed = scheduler.getClass().getMethod(
                    "runDelayed",
                    JavaPlugin.class,
                    Consumer.class,
                    Runnable.class,
                    long.class
            );
            Consumer<Object> consumer = ignored -> task.run();
            runDelayed.invoke(scheduler, plugin, consumer, null, delay);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
