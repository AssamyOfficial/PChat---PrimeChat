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

        /*
         * ========================================================
         * CHAT CHANNELS
         * ========================================================
         */

        getLogger().info(
                "=== PrimeChat: начинаем загрузку каналов ==="
        );

        chatChannelManager =
                new ChatChannelManager(this);

        chatChannelRouter =
                new ChatChannelRouter(this);

        chatFormatRenderer =
                new ChatFormatRenderer(this);

        chatChannelFormatter =
                new ChatChannelFormatter(this);

        chatChannelMessageSender =
                new ChatChannelMessageSender(this);

        getLogger().info(
                "=== PrimeChat: менеджер каналов создан ==="
        );

        /*
         * ========================================================
         * LISTENER
         * ========================================================
         */

        getServer().getPluginManager().registerEvents(
                new ChatListener(this),
                this
        );

        /*
         * ========================================================
         * COMMAND
         * ========================================================
         */

        getCommand("primechat").setExecutor(
                new PrimeChatCommand(this)
        );

        getCommand("primechat").setTabCompleter(
                new PrimeChatTabCompleter()
        );

        /*
         * ========================================================
         * PLACEHOLDER API
         * ========================================================
         */

        if (Bukkit.getPluginManager()
                .isPluginEnabled("PlaceholderAPI")) {

            getLogger().info(
                    "PlaceholderAPI найден. "
                            + "Интеграция активирована."
            );

        } else {

            getLogger().info(
                    "PlaceholderAPI не найден. "
                            + "Работаем в автономном режиме."
            );
        }

        getLogger().info(
                "PrimeChat 1.3.0 успешно запущен!"
        );
    }

    @Override
    public void onDisable() {

        getLogger().info(
                "PrimeChat выключен."
        );
    }

    /*
     * ============================================================
     * GETTERS
     * ============================================================
     */

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

