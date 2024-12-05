package ru.orjus.guestmode.manager;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import ru.orjus.guestmode.GuestMode;

import java.util.*;
import java.util.stream.Collectors;

public class GuestManager {

    private final GuestMode plugin;
    private final LuckPerms luckPerms;

    private final Set<UUID> guestPlayers = new HashSet<>();
    private final Set<UUID> guestWhitelist = new HashSet<>();

    public GuestManager(GuestMode plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    /**
     * Добавляет игрока в список гостей.
     *
     * @param playerId UUID игрока.
     * @return true, если игрок был успешно добавлен в список гостей.
     *         false, если игрок находится в whitelist или уже является гостем.
     */
    public boolean addGuest(UUID playerId) {
        if (guestWhitelist.contains(playerId)) {
            return false;
        }
        return guestPlayers.add(playerId);
    }

    /**
     * Удаляет игрока из списка гостей.
     *
     * @param playerId UUID игрока.
     * @return true, если игрок был успешно удалён из списка гостей.
     */
    public boolean removeGuest(UUID playerId) {
        return guestPlayers.remove(playerId);
    }

    /**
     * Проверяет, является ли игрок гостем.
     *
     * Игрок считается гостем, если:
     * 1. Его UUID находится в списке гостей.
     * 2. У него есть LuckPerms-группа "guest".
     * 3. У него установлен активный пермишен "guestmode.active".
     *
     * @param playerId UUID игрока.
     * @return true, если игрок находится в гостевом режиме.
     */
    public boolean isGuest(UUID playerId) {
        if (guestPlayers.contains(playerId)) {
            return true;
        }
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user != null) {
            return user.getNodes().stream().anyMatch(node ->
                    node.getKey().equals("group.guest") ||
                            (node.getKey().equals("guestmode.active") && node.getValue())
            );
        }
        return false;
    }

    /**
     * Добавляет игрока в список исключений (whitelist).
     * Игроки в whitelist не могут быть добавлены в гостевой режим.
     *
     * @param playerId UUID игрока.
     * @return true, если игрок был успешно добавлен в whitelist.
     */
    public boolean addToWhitelist(UUID playerId) {
        return guestWhitelist.add(playerId);
    }

    /**
     * Удаляет игрока из списка исключений (whitelist).
     *
     * @param playerId UUID игрока.
     * @return true, если игрок был успешно удалён из whitelist.
     */
    public boolean removeFromWhitelist(UUID playerId) {
        return guestWhitelist.remove(playerId);
    }

    /**
     * Проверяет, находится ли игрок в списке исключений (whitelist).
     *
     * @param playerId UUID игрока.
     * @return true, если игрок находится в whitelist.
     */
    public boolean isWhitelisted(UUID playerId) {
        return guestWhitelist.contains(playerId);
    }

    /**
     * Возвращает текущее количество гостей.
     *
     * @return Количество игроков в гостевом режиме.
     */
    public int getGuestCount() {
        return guestPlayers.size();
    }

    /**
     * Возвращает копию списка UUID всех гостей.
     *
     * @return Список UUID игроков, находящихся в гостевом режиме.
     */
    public Set<UUID> getGuestPlayers() {
        return new HashSet<>(guestPlayers);
    }

    /**
     * Возвращает копию списка UUID всех игроков в whitelist.
     *
     * @return Список UUID игроков, находящихся в whitelist.
     */
    public Set<UUID> getWhitelist() {
        return new HashSet<>(guestWhitelist);
    }

    /**
     * Сохраняет текущий список гостей и whitelist в файл конфигурации.
     *
     * Списки сохраняются как строковые представления UUID в параметрах:
     * - "guestList": список гостей.
     * - "guestWhitelist": список игроков в whitelist.
     */
    public void saveGuests() {
        plugin.getConfig().set("guestList", guestPlayers.stream().map(UUID::toString).collect(Collectors.toList()));
        plugin.getConfig().set("guestWhitelist", guestWhitelist.stream().map(UUID::toString).collect(Collectors.toList()));
        plugin.saveConfig();
    }

    /**
     * Загружает список гостей и whitelist из файла конфигурации.
     *
     * Списки берутся из параметров:
     * - "guestList": список гостей.
     * - "guestWhitelist": список игроков в whitelist.
     */
    public void loadGuests() {
        guestPlayers.clear();
        guestWhitelist.clear();

        guestPlayers.addAll(plugin.getConfig().getStringList("guestList").stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet()));

        guestWhitelist.addAll(plugin.getConfig().getStringList("guestWhitelist").stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet()));
    }
}
