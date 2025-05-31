package com.better.keystrokes.module;

import com.better.keystrokes.settings.Setting;
import com.better.keystrokes.settings.impl.KeybindSetting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public String name;
    public String description;
    public Category category;
    public boolean enabled;
    public boolean visibleInGui = true;
    public List<Setting> settings = new ArrayList<>();
    public boolean isExpanded = false;
    protected Minecraft mc = Minecraft.getMinecraft();
    public KeybindSetting moduleToggleKeybind = null;

    protected int x, y;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
    }

    public void addSetting(Setting setting) {
        this.settings.add(setting);
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void setEnabledInstantly(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void onToggleKeyPressed() {
        this.toggle();
    }

    public void onEnable() {}
    public void onDisable() {}

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
}