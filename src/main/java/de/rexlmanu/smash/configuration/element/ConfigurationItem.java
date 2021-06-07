package de.rexlmanu.smash.configuration.element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Accessors(fluent = true)
@Data
@SerializableAs("ConfigurationItem")
public class ConfigurationItem implements ConfigurationSerializable {
    public static ConfigurationItem deserialize(Map<String, Object> args) {
        return new ConfigurationItem(
                (Integer) args.get("slot"),
                args.get("translation").toString(),
                Material.valueOf(args.get("name").toString().toUpperCase())
        );
    }

    private int slot;
    private String translation;
    private Material material;

    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap();
        result.put("slot", slot);
        result.put("translation", translation);
        result.put("material", material);
        return result;
    }

    public String loreTranslation() {
        return this.translation + ".lore";
    }

    public String nameTranslation() {
        return this.translation + ".name";
    }

}
