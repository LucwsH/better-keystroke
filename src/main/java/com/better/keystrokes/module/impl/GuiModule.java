package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.settings.impl.KeybindSetting;
import org.lwjgl.input.Keyboard;

public class GuiModule extends Module {

    private final KeybindSetting keybindSetting;

    public GuiModule() {
        super("GUI Settings", "Configure the client GUI.", Category.CLIENT);
        this.keybindSetting = new KeybindSetting("Open GUI", Keyboard.KEY_RSHIFT);
        this.addSetting(keybindSetting);
    }

    public int getKey() {
        return this.keybindSetting.getKeyCode();
    }

}