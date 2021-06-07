package de.rexlmanu.smash.language;

import de.rexlmanu.smash.utility.HexChatColor;
import de.rexlmanu.smash.utility.LanguageUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class Language {

    private String locale;
    private Map<String, String> translations;

    private String getMessage(String key) {
        return HexChatColor.format(this.translations.get(key)
                .replace("%prefix", this.translations.getOrDefault("prefix", "prefix")));
    }

    public String translate(String key, Object... parameters) {
        if (!this.translations.containsKey(key)) return key;
        if (parameters.length == 0) return ChatColor.translateAlternateColorCodes('&', this.getMessage(key));
        String translation = String.format(this.getMessage(key), parameters);
        return ChatColor.translateAlternateColorCodes('&', translation);
    }

    public List<String> translateList(String key) {
        return LanguageUtils.toList(this.translate(key));
    }
}
