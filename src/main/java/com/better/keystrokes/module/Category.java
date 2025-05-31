package com.better.keystrokes.module;

public enum Category {
    COMBAT("Combat"),
    MISC("Misc"),
    CLIENT("Client"),
    GUI("GUI");

    public final String name;

    Category(String name) {
        this.name = name;
    }
}