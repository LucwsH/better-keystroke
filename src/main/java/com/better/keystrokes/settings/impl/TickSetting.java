package com.better.keystrokes.settings.impl;

import com.better.keystrokes.settings.Setting;

public class TickSetting extends Setting {
    private boolean toggled;

    public TickSetting(String name, boolean toggled) {
        this.name = name;
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public void toggle() {
        this.toggled = !this.toggled;
    }
}