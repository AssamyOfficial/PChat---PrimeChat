package corp.prime.chat;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class CommandConfig {
    private final PrimeChat plugin;
    private YamlConfiguration config;

    public CommandConfig(PrimeChat plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "command.yml");

        if (!file.exists()) {
            plugin.saveResource("command.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }
}
