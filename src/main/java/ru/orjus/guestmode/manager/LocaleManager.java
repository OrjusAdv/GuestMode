package ru.orjus.guestmode.manager;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.orjus.guestmode.GuestMode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocaleManager {

    private final GuestMode plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, FileConfiguration> locales = new HashMap<>();
    private FileConfiguration messages;
    private String currentLocale;

    public LocaleManager(GuestMode plugin) {
        this.plugin = plugin;

        File localesDir = new File(plugin.getDataFolder(), "locales");
        if (!localesDir.exists()) {
            localesDir.mkdirs();
        }

        loadLocale("ru_RU"); // Стандартная локаль
        this.currentLocale = "ru_RU"; // Устанавливаем стандартную локаль
    }

    /**
     * Загружает или создаёт файл локали.
     *
     * @param locale Название локали (например, ru_RU).
     */
    private void loadLocale(String locale) {
        File file = new File(plugin.getDataFolder() + "/locales", locale + ".yml");
        if (!file.exists()) {
            plugin.saveResource("locales/" + locale + ".yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        locales.put(locale, config);
    }

    /**
     * Устанавливает текущую локаль.
     *
     * @param locale Название локали (например, ru_RU).
     */
    public void setLocale(String locale) {
        if (isLocaleAvailable(locale)) {
            this.currentLocale = locale;
            this.messages = locales.get(locale);
        } else {
            plugin.getLogger().warning("Locale " + locale + " not found. Falling back to default (ru_RU).");
            this.currentLocale = "ru_RU";
            this.messages = locales.get("ru_RU");
        }
    }

    /**
     * Проверяет, доступна ли локаль.
     *
     * @param locale Название локали.
     * @return true, если локаль доступна.
     */
    public boolean isLocaleAvailable(String locale) {
        return locales.containsKey(locale);
    }

    /**
     * Получает сообщение по ключу из текущей локали.
     *
     * @param key Ключ сообщения.
     * @return Локализованное сообщение.
     */
    public String getMessage(String key) {
        if (messages == null) {
            messages = locales.getOrDefault(currentLocale, locales.get("ru_RU"));
        }
        return messages.getString("messages." + key, "<red>Сообщение не найдено: " + key + "</red>");
    }

    /**
     * Отправляет локализованное сообщение указанному Audience с поддержкой плейсхолдеров.
     *
     * @param audience    объект Audience, которому будет отправлено сообщение.
     * @param key         ключ сообщения в локали.
     * @param placeholders дополнительные плейсхолдеры для замены.
     */
    public void sendLocalizedMessage(Audience audience, String key, Object... placeholders) {
        String message = getMessage(key);

        // Замена позиционных плейсхолдеров {0}, {1}, {2}, ...
        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i++) {
                message = message.replace("{" + i + "}", placeholders[i].toString());
            }
        }

        for (Object placeholder : placeholders) {
            if (placeholder instanceof Player player) {
                message = message.replace("{player}", player.getName());
                message = message.replace("{uuid}", player.getUniqueId().toString());
            } else if (placeholder instanceof String) {
                message = message.replace("{player}", placeholder.toString());
            }
        }

        audience.sendMessage(miniMessage.deserialize(message));
    }

    /**
     * Загрузка стандартных локалей.
     * Автоматически загружает ru_RU локаль.
     */
    public void loadDefaultLocales() {
        loadLocale("ru_RU");
    }
}
