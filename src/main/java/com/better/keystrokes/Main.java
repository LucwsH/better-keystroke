package com.better.keystrokes;

import com.better.keystrokes.config.ConfigManager;
import com.better.keystrokes.gui.ClickGUI;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.module.impl.GuiModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = "betterkeystrokes", useMetadata=true, version = "1.0")
public class Main {

    public static Main INSTANCE;
    public ModuleManager moduleManager;
    public ConfigManager configManager;

    private KeyBinding openGuiKey;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        INSTANCE = this;

        moduleManager = new ModuleManager();
        configManager = new ConfigManager();

        MinecraftForge.EVENT_BUS.register(this);

        for (Module module : ModuleManager.getModules()) {
            MinecraftForge.EVENT_BUS.register(module);
        }

        configManager.loadConfig();

        GuiModule guiModule = ModuleManager.getModule(GuiModule.class);
        if (guiModule != null) {
            openGuiKey = new KeyBinding("Open ClickGUI", guiModule.getKey(), "BetterKeystrokes");
            ClientRegistry.registerKeyBinding(openGuiKey);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            configManager.saveConfig();
        }));
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openGuiKey != null && openGuiKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new ClickGUI());
        }
    }

    public KeyBinding getOpenGuiKey() {
        return openGuiKey;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}