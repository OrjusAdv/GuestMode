package ru.orjus.guestmode.handler;

import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.orjus.guestmode.GuestMode;

import java.util.List;

public class ChatHandler implements Listener {

    private final GuestMode plugin;

    public ChatHandler(GuestMode plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChatMessageSend(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Audience audience = plugin.getAdventure().sender(player);

        if (plugin.getGuestManager().isGuest(player.getUniqueId())) {
            plugin.getLocaleManager().sendLocalizedMessage(audience, "chat_blocked");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChatCommandUsed(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Audience audience = plugin.getAdventure().sender(player);

        if (plugin.getGuestManager().isGuest(player.getUniqueId())) {
            String command = event.getMessage().split(" ")[0].toLowerCase();
            List<String> allowedCommands = plugin.getConfig().getStringList("allowedCommands");

            if (!allowedCommands.contains(command)) {
                plugin.getLocaleManager().sendLocalizedMessage(audience, "command_blocked");
                event.setCancelled(true);
            }
        }
    }
}
