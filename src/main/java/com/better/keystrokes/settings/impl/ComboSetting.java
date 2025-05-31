package com.better.keystrokes.settings.impl;

import com.better.keystrokes.settings.Setting;
import java.util.Arrays;
import java.util.List;

public class ComboSetting extends Setting {
    private List<String> options;
    private int index;

    public ComboSetting(String name, String defaultOption, String... options) {
        this.name = name;
        this.options = Arrays.asList(options);
        this.index = this.options.indexOf(defaultOption);
        if (this.index == -1 && !this.options.isEmpty()) {
            this.index = 0;
        }
    }

    public String getMode() {
        if (options.isEmpty() || index < 0 || index >= options.size()) {
            return "";
        }
        return options.get(index);
    }

    public void cycle() {
        if (options.isEmpty()) return;
        if (index < options.size() - 1) {
            index++;
        } else {
            index = 0;
        }
    }

    public void cycleReverse() {
        if (options.isEmpty()) return;
        if (index > 0) {
            index--;
        } else {
            index = options.size() - 1;
        }
    }

    public void setMode(String name) {
        if (options.isEmpty()) return;
        int newIndex = options.indexOf(name);
        if (newIndex != -1) {
            this.index = newIndex;
        }
    }
}