package com.better.keystrokes.module.impl;

import com.better.keystrokes.Main;
import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.module.ModuleManager;
import net.minecraftforge.common.MinecraftForge;

public class SelfDestruct extends Module {

    public SelfDestruct() {
        super("Self Destruct", "Disables other modules until restart. Keystrokes remains active.", Category.CLIENT);
        this.visibleInGui = true;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        MinecraftForge.EVENT_BUS.unregister(Main.INSTANCE);

        for (Module module : ModuleManager.getModules()) {
            if (module instanceof KeystrokesModule) {
                continue;
            }

            if (module == this) {
                continue;
            }

            MinecraftForge.EVENT_BUS.unregister(module);

            module.setEnabledInstantly(false);
        }

        if (mc.currentScreen != null) {
            mc.displayGuiScreen(null);
        }

        this.setEnabled(false);
    }
}