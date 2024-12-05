package ru.orjus.guestmode.command;

import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import ru.orjus.guestmode.GuestMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuestModeCommand implements CommandExecutor, TabCompleter {

    private final GuestMode plugin;

    public GuestModeCommand(GuestMode plugin) {
        this.plugin = plugin;

        PluginCommand command = plugin.getCommand("guestmode");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            plugin.getLogger().warning("Command 'guestmode' is not defined in plugin.yml.");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Audience audience = plugin.getAdventure().sender(sender);

        if (args.length == 0) {
            plugin.getLocaleManager().sendLocalizedMessage(audience, "command.usage");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "info":
                handleInfo(audience);
                break;
            case "reload":
                handleReload(audience);
                break;
            case "remove":
                handleRemove(audience, args);
                break;
            case "whitelist":
                handleWhitelist(audience, args);
                break;
            default:
                plugin.getLocaleManager().sendLocalizedMessage(audience, "command.unknown");
                break;
        }
        return true;
    }

    private void handleInfo(Audience audience) {
        int guestCount = plugin.getGuestManager().getGuestCount();
        plugin.getLocaleManager().sendLocalizedMessage(audience, "info.header");
        plugin.getLocaleManager().sendLocalizedMessage(audience, "info.guest_count", guestCount);
    }

    private void handleReload(Audience audience) {
        if (!plugin.getConfigFile().exists()) {
            plugin.saveDefaultConfig();
            plugin.getLocaleManager().sendLocalizedMessage(audience, "reload.config_restored");
        }
        plugin.reloadConfig();

        String newLocale = plugin.getConfig().getString("locale", "ru_RU");
        if (!plugin.getLocaleManager().isLocaleAvailable(newLocale)) {
            plugin.getLogger().warning("Locale " + newLocale + " not found. Falling back to default (ru_RU).");
            newLocale = "ru_RU";
        }

        plugin.getLocaleManager().setLocale(newLocale);

        plugin.getLogger().info("Configuration and locale reloaded. Current locale: " + newLocale);
        plugin.getLocaleManager().sendLocalizedMessage(audience, "reload.success");
    }

    private void handleRemove(Audience audience, String[] args) {
        if (args.length < 2) {
            plugin.getLocaleManager().sendLocalizedMessage(audience, "remove.usage");
            return;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        UUID playerId = offlinePlayer.getUniqueId();

        boolean removed = plugin.getGuestManager().removeGuest(playerId);

        if (removed) {
            plugin.getLocaleManager().sendLocalizedMessage(audience, "remove.success", playerName);
        } else {
            plugin.getLocaleManager().sendLocalizedMessage(audience, "remove.not_in_guest", playerName);
        }
    }

    private void handleWhitelist(Audience audience, String[] args) {
        if (args.length < 3) {
            plugin.getLocaleManager().sendLocalizedMessage(audience, "whitelist.usage");
            return;
        }

        String action = args[1].toLowerCase();
        String playerName = args[2];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        UUID playerId = offlinePlayer.getUniqueId();

        switch (action) {
            case "add":
                boolean added = plugin.getGuestManager().addToWhitelist(playerId);
                if (added) {
                    plugin.getLocaleManager().sendLocalizedMessage(audience, "whitelist.add.success", playerName);
                } else {
                    plugin.getLocaleManager().sendLocalizedMessage(audience, "whitelist.add.already", playerName);
                }
                break;

            case "remove":
                boolean removed = plugin.getGuestManager().removeFromWhitelist(playerId);
                if (removed) {
                    plugin.getLocaleManager().sendLocalizedMessage(audience, "whitelist.remove.success", playerName);
                } else {
                    plugin.getLocaleManager().sendLocalizedMessage(audience, "whitelist.remove.not_found", playerName);
                }
                break;

            default:
                plugin.getLocaleManager().sendLocalizedMessage(audience, "whitelist.usage");
                break;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("info");
            suggestions.add("reload");
            suggestions.add("remove");
            suggestions.add("whitelist");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            for (UUID playerId : plugin.getGuestManager().getGuestPlayers()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
                if (player.getName() != null) {
                    suggestions.add(player.getName());
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("whitelist")) {
            suggestions.add("add");
            suggestions.add("remove");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("whitelist") && args[1].equalsIgnoreCase("remove")) {
            for (UUID playerId : plugin.getGuestManager().getWhitelist()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
                if (player.getName() != null) {
                    suggestions.add(player.getName());
                }
            }
        }

        return filterSuggestions(suggestions, args[args.length - 1]);
    }

    private List<String> filterSuggestions(List<String> suggestions, String input) {
        List<String> filtered = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(suggestion);
            }
        }
        return filtered;
    }
}
