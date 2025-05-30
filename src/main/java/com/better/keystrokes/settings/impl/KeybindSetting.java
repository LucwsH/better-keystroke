package com.better.keystrokes.settings.impl;

import com.better.keystrokes.settings.Setting;

public class KeybindSetting extends Setting {

    private int keyCode;
    private boolean listening;

    public KeybindSetting(String name, int defaultKeyCode) {
        this.name = name;
        this.keyCode = defaultKeyCode;
        this.listening = false;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }
}