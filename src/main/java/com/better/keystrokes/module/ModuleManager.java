package com.better.keystrokes.module;

import com.better.keystrokes.module.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    public static List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        modules.clear();
        modules.add(new GuiModule());
        modules.add(new LeftClicker());
        modules.add(new RightClicker());
        modules.add(new ClickAssist());
        modules.add(new ClickerHUD());
        modules.add(new SelfDestruct());
        modules.add(new DelayRemover());
        modules.add(new FastPlace());
        modules.add(new KeystrokesModule());
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static List<Module> getModulesInCategory(Category category) {
        return modules.stream().filter(m -> m.category == category).collect(Collectors.toList());
    }

    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (module.getClass() == clazz) {
                return (T) module;
            }
        }
        return null;
    }
}