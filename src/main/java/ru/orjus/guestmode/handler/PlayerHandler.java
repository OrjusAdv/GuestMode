package ru.orjus.guestmode.handler;

import net.kyori.adventure.audience.Audience;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import ru.orjus.guestmode.GuestMode;

import java.util.*;

public class PlayerHandler implements Listener {

    private final GuestMode plugin;
    private final Set<Material> physicalInteractionBlocks = new HashSet<>();
    private final Set<Material> interactiveBlocks = new HashSet<>();

    public PlayerHandler(GuestMode plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadBlocksFromConfig();
    }

    private void loadBlocksFromConfig() {
        FileConfiguration config = plugin.getConfig();

        List<String> physicalBlocks = config.getStringList("physicalInteractionBlocks");
        for (String blockName : physicalBlocks) {
            try {
                Material material = Material.valueOf(blockName);
                physicalInteractionBlocks.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in physicalInteractionBlocks: " + blockName);
            }
        }

        List<String> interactiveBlockNames = config.getStringList("interactiveBlocks");
        for (String blockName : interactiveBlockNames) {
            try {
                Material material = Material.valueOf(blockName);
                interactiveBlocks.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in interactiveBlocks: " + blockName);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Audience audience = plugin.getAdventure().sender(player);

        if (plugin.getGuestManager().isWhitelisted(playerId)) {
            plugin.getGuestManager().removeFromWhitelist(playerId);
            plugin.getLocaleManager().sendLocalizedMessage(audience, "whitelist.skip_guestmode");
            return;
        }

        if (player.hasPlayedBefore()) {
            plugin.getLocaleManager().sendLocalizedMessage(audience, "welcome_back");
            return;
        }

        if (plugin.getGuestManager().addGuest(playerId)) {
            plugin.getLocaleManager().sendLocalizedMessage(audience, "guest_mode_added");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Audience audience = plugin.getAdventure().sender(player);

        if (plugin.getGuestManager().isGuest(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getLocaleManager().sendLocalizedMessage(audience, "block_break_denied");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Audience audience = plugin.getAdventure().sender(player);

        if (plugin.getGuestManager().isGuest(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getLocaleManager().sendLocalizedMessage(audience, "block_place_denied");
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            Audience audience = plugin.getAdventure().sender(player);

            if (plugin.getGuestManager().isGuest(player.getUniqueId())) {
                event.setCancelled(true);
                plugin.getLocaleManager().sendLocalizedMessage(audience, "item_pickup_denied");
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Audience audience = plugin.getAdventure().sender(player);

        if (plugin.getGuestManager().isGuest(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getLocaleManager().sendLocalizedMessage(audience, "item_drop_denied");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            Audience audience = plugin.getAdventure().sender(player);

            if (plugin.getGuestManager().isGuest(player.getUniqueId())) {
                event.setCancelled(true);
                plugin.getLocaleManager().sendLocalizedMessage(audience, "attack_denied");
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Audience audience = plugin.getAdventure().sender(player);

        if (plugin.getGuestManager().isGuest(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getLocaleManager().sendLocalizedMessage(audience, "entity_interact_denied");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Audience audience = plugin.getAdventure().sender(player);
        UUID playerId = player.getUniqueId();

        Block block = event.getClickedBlock();

        if (plugin.getGuestManager().isGuest(playerId)) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                assert block != null;
                if (interactiveBlocks.contains(block.getType())) {
                    event.setCancelled(true);
                    plugin.getLocaleManager().sendLocalizedMessage(audience, "block_interact_denied");
                }
            }
            if (event.getAction().equals(Action.PHYSICAL)) {
                assert block != null;
                if (physicalInteractionBlocks.contains(block.getType())) {
                    event.setCancelled(true);
                    plugin.getLocaleManager().sendLocalizedMessage(audience, "block_interact_denied");
                }
            }
        }
    }
}
