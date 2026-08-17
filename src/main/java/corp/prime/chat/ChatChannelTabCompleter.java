package corp.prime.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatChannelTabCompleter implements Listener {
    private final PrimeChat plugin;

    public ChatChannelTabCompleter(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        String buffer = event.getBuffer();
        if (buffer == null || !buffer.startsWith("/")) {
            return;
        }

        String input = buffer.substring(1);
        int space = input.indexOf(' ');
        String command = space >= 0 ? input.substring(0, space) : input;

        ChatChannel channel = plugin.getChatChannelManager().getChannelByCommand(command);
        if (channel == null || !channel.isEnabled() || !channel.isCommand()) {
            return;
        }

        if (space < 0 || input.substring(space + 1).trim().isEmpty()) {
            event.setCompletions(new ArrayList<>());
        }
    }
}
