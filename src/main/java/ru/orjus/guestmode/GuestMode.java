package ru.orjus.guestmode;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import org.bukkit.plugin.java.JavaPlugin;
import ru.orjus.guestmode.command.GuestModeCommand;
import ru.orjus.guestmode.handler.ChatHandler;
import ru.orjus.guestmode.handler.PlayerHandler;
import ru.orjus.guestmode.manager.GuestManager;
import ru.orjus.guestmode.manager.LocaleManager;

import java.io.File;

public final class GuestMode extends JavaPlugin {

    private LuckPerms luckPerms;
    private BukkitAudiences adventure;
    private GuestManager guestManager;
    private LocaleManager localeManager;

    @Override
    public void onEnable() {
        getLogger().info("Enabling GuestMode plugin...");

        luckPerms = LuckPermsProvider.get();
        if (luckPerms == null) {
            getLogger().severe("LuckPerms not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        adventure = BukkitAudiences.create(this);
        guestManager = new GuestManager(this, luckPerms);
        localeManager = new LocaleManager(this);

        saveDefaultConfig();
        String configuredLocale = getConfig().getString("locale", "ru_RU");
        localeManager.setLocale(configuredLocale);
        getLogger().info("Locale set to: " + configuredLocale);

        createGuestGroupIfNotExists();
        registerCommands();
        registerListeners();
        guestManager.loadGuests();

        getLogger().info("GuestMode plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
        }

        guestManager.saveGuests();
        getLogger().info("GuestMode plugin disabled!");
    }

    /**
     * Регистрация всех команд плагина.
     * Создаёт обработчики для каждой команды, которые затем регистрируются в Minecraft.
     */
    private void registerCommands() {
        new GuestModeCommand(this);
    }

    /**
     * Регистрация всех слушателей событий.
     * Включает обработчики событий игроков, чата и других действий.
     */
    private void registerListeners() {
        new ChatHandler(this);
        new PlayerHandler(this);
    }

    /**
     * Получение экземпляра менеджера локализации.
     * Используется для доступа к локализованным сообщениям.
     *
     * @return экземпляр LocaleManager.
     */
    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    /**
     * Получение экземпляра BukkitAudiences.
     * Используется для отправки сообщений игрокам через Adventure API.
     *
     * @return экземпляр BukkitAudiences.
     */
    public BukkitAudiences getAdventure() {
        return adventure;
    }

    /**
     * Получение экземпляра GuestManager.
     * Используется для управления гостевыми игроками и их данными.
     *
     * @return экземпляр GuestManager.
     */
    public GuestManager getGuestManager() {
        return guestManager;
    }

    /**
     * Получение экземпляра LuckPerms.
     * Используется для работы с API LuckPerms, включая управление группами и пермишенами.
     *
     * @return экземпляр LuckPerms.
     */
    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    /**
     * Возвращает файл конфигурации плагина.
     * Этот метод может быть полезен для прямого доступа к конфигурации
     * или её обработки в других компонентах.
     *
     * @return файл конфигурации config.yml.
     */
    public File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }

    /**
     * Проверяет наличие группы "guest" в LuckPerms.
     * Если группа отсутствует, логирует предупреждение и предлагает администратору
     * создать её вручную с помощью команды.
     */
    private void createGuestGroupIfNotExists() {
        Group guestGroup = luckPerms.getGroupManager().getGroup("guest");
        if (guestGroup == null) {
            getLogger().warning("LuckPerms group 'guest' does not exist. Please create it manually using the command:");
            getLogger().warning("/lp creategroup guest");
            return;
        }

        addGuestPermissionIfMissing(guestGroup);
    }

    /**
     * Добавляет пермишен "guestmode.active" в группу "guest" в LuckPerms,
     * если он отсутствует. Этот пермишен необходим для работы плагина,
     * чтобы идентифицировать гостевых игроков.
     *
     * @param guestGroup группа LuckPerms "guest".
     */
    private void addGuestPermissionIfMissing(Group guestGroup) {
        Node guestPermissionNode = Node.builder("guestmode.active").value(true).build();

        boolean hasPermission = guestGroup.data().toCollection().stream()
                .anyMatch(node -> node.getKey().equals("guestmode.active"));

        if (!hasPermission) {
            guestGroup.data().add(guestPermissionNode);
            luckPerms.getGroupManager().saveGroup(guestGroup);
            getLogger().info("Added permission 'guestmode.active' to group 'guest'.");
        } else {
            getLogger().info("Group 'guest' already has permission 'guestmode.active'.");
        }
    }
}
