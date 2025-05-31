package com.better.keystrokes;

import com.better.keystrokes.command.CommandKeystrokes;
import com.better.keystrokes.config.ConfigManager;
import com.better.keystrokes.gui.ClickGUI;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.module.impl.GuiModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = "betterkeystrokes", useMetadata=true, version = "1.0")
public class Main {

    public static Main INSTANCE;
    public ModuleManager moduleManager;
    public ConfigManager configManager;

    private KeyBinding openGuiKey;
    private final Map<Module, Boolean> lastToggleKeyState = new HashMap<>();

    private CommandKeystrokes keystrokesCommand;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        INSTANCE = this;

        moduleManager = new ModuleManager();
        configManager = new ConfigManager();

        MinecraftForge.EVENT_BUS.register(this);

        keystrokesCommand = new CommandKeystrokes();
        ClientCommandHandler.instance.registerCommand(keystrokesCommand);
        MinecraftForge.EVENT_BUS.register(keystrokesCommand);

        for (Module module : ModuleManager.getModules()) {
            MinecraftForge.EVENT_BUS.register(module);
            if (module.moduleToggleKeybind != null) {
                lastToggleKeyState.put(module, false);
            }
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
            if (!(Minecraft.getMinecraft().currentScreen instanceof ClickGUI)) {
                Minecraft.getMinecraft().displayGuiScreen(new ClickGUI());
            }
        }
    }
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().thePlayer != null) {
            Minecraft mc = Minecraft.getMinecraft();

            if (mc.currentScreen != null && !(mc.currentScreen instanceof ClickGUI)) {
                return;
            }

            if (mc.currentScreen instanceof ClickGUI) {
                ClickGUI clickGUI = (ClickGUI) mc.currentScreen;
                if (clickGUI.isListeningToKeybind()) {
                    return;
                }
            }

            for (Module module : ModuleManager.getModules()) {
                if (module.moduleToggleKeybind != null && module.moduleToggleKeybind.getKeyCode() != Keyboard.KEY_NONE) {
                    boolean keyPressed = Keyboard.isKeyDown(module.moduleToggleKeybind.getKeyCode());
                    boolean previousState = lastToggleKeyState.getOrDefault(module, false);

                    if (keyPressed && !previousState) {
                        if(mc.currentScreen == null || mc.currentScreen instanceof ClickGUI) {
                            module.onToggleKeyPressed();
                        }
                    }
                    lastToggleKeyState.put(module, keyPressed);
                }
            }
        }
    }


    public KeyBinding getOpenGuiKey() {
        return openGuiKey;
    }
}