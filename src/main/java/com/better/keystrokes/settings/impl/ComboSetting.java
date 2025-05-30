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
    }

    public String getMode() {
        return options.get(index);
    }

    public void cycle() {
        if (index < options.size() - 1) {
            index++;
        } else {
            index = 0;
        }
    }

    public void setMode(String name) {
        int newIndex = options.indexOf(name);
        if (newIndex != -1) {
            this.index = newIndex;
        }
    }
}