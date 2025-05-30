package com.better.keystrokes.config;

import com.better.keystrokes.module.Module;
import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.settings.Setting;
import com.better.keystrokes.settings.impl.ComboSetting;
import com.better.keystrokes.settings.impl.DoubleSliderSetting;
import com.better.keystrokes.settings.impl.SliderSetting;
import com.better.keystrokes.settings.impl.TickSetting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class ConfigManager {

    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        File configDir = new File(System.getProperty("user.home") + File.separator + ".minecraft" + File.separator + "BetterKeystrokes");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        this.configFile = new File(configDir, "config.json");
    }

    public void saveConfig() {
        JsonObject configJson = new JsonObject();
        JsonObject modulesJson = new JsonObject();

        for (Module module : ModuleManager.getModules()) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("x", module.getX());
            moduleJson.addProperty("y", module.getY());
            moduleJson.addProperty("expanded", module.isExpanded);

            JsonObject settingsJson = new JsonObject();
            for (Setting setting : module.getSettings()) {
                JsonObject settingJson = new JsonObject();
                if (setting instanceof TickSetting) {
                    settingJson.addProperty("value", ((TickSetting) setting).isToggled());
                } else if (setting instanceof SliderSetting) {
                    settingJson.addProperty("value", ((SliderSetting) setting).getValue());
                } else if (setting instanceof DoubleSliderSetting) {
                    settingJson.addProperty("minValue", ((DoubleSliderSetting) setting).getMinValue());
                    settingJson.addProperty("maxValue", ((DoubleSliderSetting) setting).getMaxValue());
                } else if (setting instanceof ComboSetting) {
                    settingJson.addProperty("mode", ((ComboSetting) setting).getMode());
                }
                settingsJson.add(setting.name, settingJson);
            }
            moduleJson.add("settings", settingsJson);
            modulesJson.add(module.getName(), moduleJson);
        }

        configJson.add("modules", modulesJson);

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(configJson, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonElement jsonElement = new JsonParser().parse(reader);
            if (!jsonElement.isJsonObject()) return;

            JsonObject configJson = jsonElement.getAsJsonObject();
            JsonObject modulesJson = configJson.getAsJsonObject("modules");

            if (modulesJson == null) return;

            for (Map.Entry<String, JsonElement> entry : modulesJson.entrySet()) {
                String moduleName = entry.getKey();
                Module module = findModuleByName(moduleName);
                if (module != null) {
                    JsonObject moduleJson = entry.getValue().getAsJsonObject();

                    if (moduleJson.has("enabled")) {
                        module.setEnabled(moduleJson.get("enabled").getAsBoolean());
                    }
                    if (moduleJson.has("x")) {
                        module.setX(moduleJson.get("x").getAsInt());
                    }
                    if (moduleJson.has("y")) {
                        module.setY(moduleJson.get("y").getAsInt());
                    }
                    if (moduleJson.has("expanded")) {
                        module.isExpanded = moduleJson.get("expanded").getAsBoolean();
                    }

                    JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                    if (settingsJson != null) {
                        for (Setting setting : module.getSettings()) {
                            JsonObject settingJson = settingsJson.getAsJsonObject(setting.name);
                            if (settingJson != null) {
                                if (setting instanceof TickSetting && settingJson.has("value")) {
                                    ((TickSetting) setting).setToggled(settingJson.get("value").getAsBoolean());
                                } else if (setting instanceof SliderSetting && settingJson.has("value")) {
                                    ((SliderSetting) setting).setValue(settingJson.get("value").getAsDouble());
                                } else if (setting instanceof DoubleSliderSetting) {
                                    if (settingJson.has("minValue")) {
                                        ((DoubleSliderSetting) setting).setMinValue(settingJson.get("minValue").getAsDouble());
                                    }
                                    if (settingJson.has("maxValue")) {
                                        ((DoubleSliderSetting) setting).setMaxValue(settingJson.get("maxValue").getAsDouble());
                                    }
                                } else if (setting instanceof ComboSetting && settingJson.has("mode")) {
                                    ((ComboSetting) setting).setMode(settingJson.get("mode").getAsString());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private Module findModuleByName(String name) {
        for (Module module : ModuleManager.getModules()) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}