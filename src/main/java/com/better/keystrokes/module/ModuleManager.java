package com.better.keystrokes.module;

import com.better.keystrokes.module.impl.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        modules.clear();

        Module guiModule = new GuiModule();
        Module leftClicker = new LeftClicker();
        Module rightClicker = new RightClicker();

        guiModule.setX(20);
        guiModule.setY(20);

        leftClicker.setX(160);
        leftClicker.setY(20);

        rightClicker.setX(300);
        rightClicker.setY(20);


        modules.add(guiModule);
        modules.add(leftClicker);
        modules.add(rightClicker);
        modules.add(new DelayRemover());
        modules.add(new FastPlace());
    }

    public static List<Module> getModules() {
        return modules;
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