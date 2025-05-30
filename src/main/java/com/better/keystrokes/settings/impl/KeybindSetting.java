package com.better.keystrokes.settings.impl;

import com.better.keystrokes.settings.Setting;

public class KeybindSetting extends Setting {

    private int keyCode;
    private boolean listening;
    private Runnable onKeyCodeChanged;

    public KeybindSetting(String name, int defaultKeyCode) {
        this(name, defaultKeyCode, null);
    }

    public KeybindSetting(String name, int defaultKeyCode, Runnable onKeyCodeChangedCallback) {
        this.name = name;
        this.keyCode = defaultKeyCode;
        this.listening = false;
        this.onKeyCodeChanged = onKeyCodeChangedCallback;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        if (this.keyCode != keyCode) {
            this.keyCode = keyCode;
            if (onKeyCodeChanged != null) {
                onKeyCodeChanged.run();
            }
        }
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }
}