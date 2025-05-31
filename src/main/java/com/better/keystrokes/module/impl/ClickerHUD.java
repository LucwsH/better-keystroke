package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.settings.impl.ComboSetting;
import com.better.keystrokes.settings.impl.KeybindSetting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class ClickerHUD extends Module {

    public ComboSetting hudPosition;

    private static final int SCREEN_MARGIN = 2;
    private static final int PADDING = 3;
    private static final int LINE_SPACING = 2;
    private static final int COLOR_BACKGROUND = 0x90000000;
    private static final int COLOR_TEXT_PREFIX = 0xFFFFFF;
    private static final int COLOR_TEXT_STATUS_ON = 0x00AA00;


    public ClickerHUD() {
        super("Clicker HUD", "Displays active clickers on screen.", Category.MISC);
        this.moduleToggleKeybind = new KeybindSetting("Toggle HUD", Keyboard.KEY_H);
        addSetting(this.moduleToggleKeybind);
        addSetting(hudPosition = new ComboSetting("HUD Position", "Top-Left", "Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right", "Top-Center"));
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
        ClickAssist clickAssist = ModuleManager.getModule(ClickAssist.class);

        List<String> activeClickerLines = new ArrayList<>();
        if (leftClicker != null && leftClicker.isEnabled()) {
            activeClickerLines.add("Left Clicker: ON");
        }
        if (rightClicker != null && rightClicker.isEnabled()) {
            activeClickerLines.add("Right Clicker: ON");
        }
        if (clickAssist != null && clickAssist.isEnabled()) {
            activeClickerLines.add("Click Assist: ON");
        }

        if (activeClickerLines.isEmpty()) {
            return;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        int effectiveLineHeight = mc.fontRendererObj.FONT_HEIGHT + LINE_SPACING;
        int maxWidth = 0;

        for (String line : activeClickerLines) {
            int separatorIndex = line.lastIndexOf(": ");
            if (separatorIndex != -1 && separatorIndex + 2 <= line.length()) {
                String prefix = line.substring(0, separatorIndex + 2);
                String status = line.substring(separatorIndex + 2);
                maxWidth = Math.max(maxWidth, mc.fontRendererObj.getStringWidth(prefix) + mc.fontRendererObj.getStringWidth(status));
            } else {
                maxWidth = Math.max(maxWidth, mc.fontRendererObj.getStringWidth(line));
            }
        }

        int hudContentHeight = activeClickerLines.size() * effectiveLineHeight;
        if (!activeClickerLines.isEmpty()) {
            hudContentHeight -= LINE_SPACING;
        }
        if (hudContentHeight < 0) hudContentHeight = 0;


        int bgWidth = maxWidth + (PADDING * 2);
        int bgHeight = hudContentHeight + (PADDING * 2);

        int bgX = 0, bgY = 0;
        String positionMode = hudPosition.getMode();

        switch (positionMode) {
            case "Top-Left":
                bgX = SCREEN_MARGIN;
                bgY = SCREEN_MARGIN;
                break;
            case "Top-Right":
                bgX = screenWidth - bgWidth - SCREEN_MARGIN;
                bgY = SCREEN_MARGIN;
                break;
            case "Bottom-Left":
                bgX = SCREEN_MARGIN;
                bgY = screenHeight - bgHeight - SCREEN_MARGIN;
                break;
            case "Bottom-Right":
                bgX = screenWidth - bgWidth - SCREEN_MARGIN;
                bgY = screenHeight - bgHeight - SCREEN_MARGIN;
                break;
            case "Top-Center":
                bgX = (screenWidth - bgWidth) / 2;
                bgY = SCREEN_MARGIN;
                break;
            default:
                bgX = SCREEN_MARGIN;
                bgY = SCREEN_MARGIN;
                break;
        }

        Gui.drawRect(bgX, bgY, bgX + bgWidth, bgY + bgHeight, COLOR_BACKGROUND);

        int currentY = bgY + PADDING;
        for (String line : activeClickerLines) {
            String prefixText;
            String statusText = "";
            int prefixColor = COLOR_TEXT_PREFIX;
            int statusColor = COLOR_TEXT_STATUS_ON;

            int separatorIndex = line.lastIndexOf(": ");
            if (separatorIndex != -1 && separatorIndex + 2 <= line.length()) {
                prefixText = line.substring(0, separatorIndex + 2);
                statusText = line.substring(separatorIndex + 2);
            } else {
                prefixText = line;
            }

            int combinedWidth = mc.fontRendererObj.getStringWidth(prefixText) + mc.fontRendererObj.getStringWidth(statusText);
            int textX;

            if ("Top-Center".equals(positionMode)) {
                textX = bgX + (bgWidth - combinedWidth) / 2;
            } else {
                textX = bgX + PADDING;
            }

            mc.fontRendererObj.drawStringWithShadow(prefixText, textX, currentY, prefixColor);
            if (!statusText.isEmpty()) {
                mc.fontRendererObj.drawStringWithShadow(statusText, textX + mc.fontRendererObj.getStringWidth(prefixText), currentY, statusColor);
            }

            currentY += effectiveLineHeight;
        }
    }
}