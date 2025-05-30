package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Module;
import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.settings.impl.ComboSetting;
import com.better.keystrokes.settings.impl.KeybindSetting;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class ClickerHUDModule extends Module {

    public KeybindSetting toggleKey;
    public ComboSetting hudPosition;

    public ClickerHUDModule() {
        super("Clicker HUD", "Displays active clickers on screen.");
        this.moduleToggleKeybind = new KeybindSetting("Toggle HUD", Keyboard.KEY_H);
        addSetting(this.moduleToggleKeybind);
        addSetting(hudPosition = new ComboSetting("HUD Position", "Top-Left", "Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right", "Top-Center"));
        this.x = 540;
        this.y = 20;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT && event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        if (!this.isEnabled() || mc.gameSettings.showDebugInfo) {
            return;
        }

        LeftClicker leftClicker = ModuleManager.getModule(LeftClicker.class);
        RightClicker rightClicker = ModuleManager.getModule(RightClicker.class);

        if (leftClicker == null || rightClicker == null) return;

        List<String> activeClickerLines = new ArrayList<>();
        if (leftClicker.isEnabled()) {
            activeClickerLines.add("Left Clicker: ON");
        }
        if (rightClicker.isEnabled()) {
            activeClickerLines.add("Right Clicker: ON");
        }

        if (activeClickerLines.isEmpty()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        int lineHeight = mc.fontRendererObj.FONT_HEIGHT + 1;
        int hudHeight = activeClickerLines.size() * lineHeight -1;
        if (hudHeight < 0) hudHeight = 0;


        String positionMode = hudPosition.getMode();
        int startY;

        switch(positionMode) {
            case "Top-Left":
            case "Top-Center":
            case "Top-Right":
                startY = 2;
                break;
            case "Bottom-Left":
            case "Bottom-Right":
                startY = screenHeight - hudHeight - 2;
                break;
            default:
                startY = 2;
        }

        for (int i = 0; i < activeClickerLines.size(); i++) {
            String line = activeClickerLines.get(i);
            int stringWidth = mc.fontRendererObj.getStringWidth(line);
            int xPos;

            switch(positionMode) {
                case "Top-Left":
                case "Bottom-Left":
                    xPos = 2;
                    break;
                case "Top-Right":
                case "Bottom-Right":
                    xPos = screenWidth - stringWidth - 2;
                    break;
                case "Top-Center":
                    xPos = (screenWidth - stringWidth) / 2;
                    break;
                default:
                    xPos = 2;
            }
            mc.fontRendererObj.drawStringWithShadow(line, xPos, startY + (i * lineHeight), 0xFFFFFF);
        }
    }
}