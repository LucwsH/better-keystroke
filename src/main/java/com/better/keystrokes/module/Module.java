package com.better.keystrokes.module;

import com.better.keystrokes.settings.Setting;
import com.better.keystrokes.settings.impl.KeybindSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public String name;
    public String description;
    public boolean enabled;
    public boolean visibleInGui = true;
    public List<Setting> settings = new ArrayList<>();
    public boolean isExpanded = false;
    protected Minecraft mc = Minecraft.getMinecraft();

    public int x, y, width, headerHeight;
    private boolean isDragging;
    private int dragX, dragY;

    public KeybindSetting moduleToggleKeybind = null;

    public Module(String name, String description) {
        this.name = name;
        this.description = description;
        this.enabled = false;
        this.width = 130;
        this.headerHeight = 22;
        this.isDragging = false;
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

    public int getToggleKeyCode() {
        return moduleToggleKeybind != null ? moduleToggleKeybind.getKeyCode() : Keyboard.KEY_NONE;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onUpdate() {}


    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }

    public int getHeight() {
        int height = this.headerHeight;
        if (isExpanded) {
            height += this.getSettings().size() * 20 + 5;
        }
        return height;
    }

    public boolean isDragging() { return isDragging; }

    public void startDragging(int mouseX, int mouseY) {
        this.isDragging = true;
        this.dragX = mouseX - this.x;
        this.dragY = mouseY - this.y;
    }

    public void stopDragging() {
        this.isDragging = false;
    }

    public void updatePosition(int mouseX, int mouseY) {
        if (this.isDragging) {
            setX(mouseX - this.dragX);
            setY(mouseY - this.dragY);
        }
    }
}