package com.better.keystrokes.gui;

import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.module.impl.KeystrokesModule;
import com.better.keystrokes.settings.Setting;
import com.better.keystrokes.settings.impl.TickSetting;
import net.minecraft.client.gui.GuiScreen;

import java.awt.Color;
import java.io.IOException;

public class KeystrokesGui extends GuiScreen {

    private final Color COLOR_BACKGROUND = new Color(30, 32, 40, 200);
    private final Color COLOR_CARD = new Color(40, 42, 54);
    private final Color COLOR_ACCENT = new Color(80, 150, 255);
    private final Color COLOR_ACCENT_INACTIVE = new Color(68, 71, 90);
    private final Color COLOR_FONT = Color.WHITE;
    private final Color COLOR_FONT_SECONDARY = new Color(150, 150, 160);

    private final int settingHeight = 25;
    private final int padding = 10;
    private final int cardWidth = 220;

    private final KeystrokesModule keystrokesModule;
    private int cardX, cardY, cardHeight;
    private boolean isDraggingKeystrokesDisplay;
    private int dragKeystrokesX, dragKeystrokesY;

    public KeystrokesGui() {
        this.keystrokesModule = ModuleManager.getModule(KeystrokesModule.class);
    }

    @Override
    public void initGui() {
        super.initGui();
        if (this.keystrokesModule != null) {
            this.cardX = (this.width / 2) - (cardWidth / 2);
            this.cardY = (this.height / 2) - 100;
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.keystrokesModule == null) {
            String errorMsg = "Keystrokes Module not found!";
            this.fontRendererObj.drawStringWithShadow(errorMsg, (this.width - this.fontRendererObj.getStringWidth(errorMsg)) / 2, this.height / 2, Color.RED.getRGB());
            return;
        }

        drawRect(0, 0, this.width, this.height, COLOR_BACKGROUND.getRGB());

        if (isDraggingKeystrokesDisplay) {
            keystrokesModule.setX(mouseX - dragKeystrokesX);
            keystrokesModule.setY(mouseY - dragKeystrokesY);
        }

        keystrokesModule.renderForPreview();

        int settingCount = keystrokesModule.getSettings().size();
        int titleHeight = 20;
        cardHeight = padding * 2 + titleHeight + (settingCount * settingHeight);

        ClickGUI.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 6, COLOR_CARD.getRGB());

        int titleY = cardY + padding;
        this.fontRendererObj.drawStringWithShadow("Keystrokes Settings", cardX + padding, titleY + 1, COLOR_FONT.getRGB());


        int masterToggleX = cardX + cardWidth - padding - 22;
        int masterToggleY = titleY + (this.fontRendererObj.FONT_HEIGHT / 2) - 6;
        drawToggleSwitch(keystrokesModule.isEnabled(), masterToggleX, masterToggleY);

        int currentSettingY = cardY + padding + titleHeight;
        for (Setting s : keystrokesModule.getSettings()) {
            drawSetting(s, cardX + padding, currentSettingY, mouseX, mouseY);
            currentSettingY += settingHeight;
        }

        String tip = "You can drag the Keystrokes display to move it.";
        this.fontRendererObj.drawStringWithShadow(tip, (this.width - this.fontRendererObj.getStringWidth(tip)) / 2, this.cardY + cardHeight + padding, Color.LIGHT_GRAY.getRGB());

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawSetting(Setting s, int x, int y, int mouseX, int mouseY) {
        int toggleWidth = 22;
        int toggleHeight = 12;
        int toggleX = x + cardWidth - (padding * 2) - toggleWidth;
        int toggleY = y + (settingHeight - toggleHeight) / 2;

        if (s instanceof TickSetting) {
            TickSetting setting = (TickSetting) s;
            this.fontRendererObj.drawString(setting.name, x, y + (settingHeight - this.fontRendererObj.FONT_HEIGHT) / 2, COLOR_FONT_SECONDARY.getRGB());
            drawToggleSwitch(setting.isToggled(), toggleX, toggleY);
        }
    }

    private void drawToggleSwitch(boolean enabled, int x, int y) {
        Color switchColor = enabled ? COLOR_ACCENT : COLOR_ACCENT_INACTIVE;
        Color circleColor = Color.WHITE;
        int switchWidth = 22;
        int switchHeight = 12;

        ClickGUI.drawRoundedRect(x, y, switchWidth, switchHeight, 6, switchColor.getRGB());
        ClickGUI.drawCircle(enabled ? x + switchWidth - 6 : x + 6, y + switchHeight / 2, 4, circleColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (keystrokesModule == null) return;

        int titleY = cardY + padding;
        int masterToggleX = cardX + cardWidth - padding - 22;
        int masterToggleY = titleY + (this.fontRendererObj.FONT_HEIGHT / 2) - 6;
        if (isHovered(mouseX, mouseY, masterToggleX, masterToggleY, 22, 12)) {
            keystrokesModule.toggle();
            return;
        }

        int titleHeight = 20;
        int currentSettingY = cardY + padding + titleHeight;
        for (Setting s : keystrokesModule.getSettings()) {
            if (isHovered(mouseX, mouseY, cardX + padding, currentSettingY, cardWidth - (padding*2), settingHeight)) {
                if (s instanceof TickSetting) {
                    ((TickSetting) s).toggle();
                    return;
                }
            }
            currentSettingY += settingHeight;
        }

        int ksDisplayX = keystrokesModule.getX();
        int ksDisplayY = keystrokesModule.getY();
        int ksDisplayWidth = keystrokesModule.getDisplayWidth();
        int ksDisplayHeight = keystrokesModule.getDisplayHeight();

        if (mouseButton == 0 && isHovered(mouseX, mouseY, ksDisplayX, ksDisplayY, ksDisplayWidth, ksDisplayHeight)) {
            isDraggingKeystrokesDisplay = true;
            dragKeystrokesX = mouseX - ksDisplayX;
            dragKeystrokesY = mouseY - ksDisplayY;
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            isDraggingKeystrokesDisplay = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}