package corp.prime.chat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrimeChat extends JavaPlugin {
    private ChatChannelManager chatChannelManager;
    private ChatChannelRouter chatChannelRouter;
    private ChatFormatRenderer chatFormatRenderer;
    private ChatChannelFormatter chatChannelFormatter;
    private ChatChannelMessageSender chatChannelMessageSender;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        chatChannelManager = new ChatChannelManager(this);
        chatChannelRouter = new ChatChannelRouter(this);
        chatFormatRenderer = new ChatFormatRenderer(this);
        chatChannelFormatter = new ChatChannelFormatter(this);
        chatChannelMessageSender = new ChatChannelMessageSender(this);

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatChannelCommandListener(this), this);

        getCommand("primechat").setExecutor(new PrimeChatCommand(this));
        getCommand("primechat").setTabCompleter(new PrimeChatTabCompleter());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI найден. Интеграция активирована.");
        } else {
            getLogger().info("PlaceholderAPI не найден. Работаем в автономном режиме.");
        }

        getLogger().info("PrimeChat 1.2.0 успешно запущен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PrimeChat выключен.");
    }

    public ChatChannelManager getChatChannelManager() {
        return chatChannelManager;
    }

    public ChatChannelRouter getChatChannelRouter() {
        return chatChannelRouter;
    }

    public ChatFormatRenderer getChatFormatRenderer() {
        return chatFormatRenderer;
    }

    public ChatChannelFormatter getChatChannelFormatter() {
        return chatChannelFormatter;
    }

    public ChatChannelMessageSender getChatChannelMessageSender() {
        return chatChannelMessageSender;
    }
}
