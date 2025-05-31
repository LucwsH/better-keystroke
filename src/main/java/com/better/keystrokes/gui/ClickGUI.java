package com.better.keystrokes.gui;

import com.better.keystrokes.Main;
import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.settings.impl.KeybindSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends GuiScreen {

    private final List<Panel> panels = new ArrayList<>();
    private static final Color BG_COLOR = new Color(10, 10, 10, 100);
    private KeybindSetting listeningKeybind = null;

    public ClickGUI() {}

    @Override
    public void initGui() {
        super.initGui();
        this.panels.clear();

        int panelWidth = 168;  // 160 * 1.05
        int panelHeight = 189; // 180 * 1.05
        int gap = 10;
        int startY = 20;

        int panelCount = 0;
        for (Category category : Category.values()) {
            if (category != Category.GUI) {
                panelCount++;
            }
        }

        int totalContentWidth = (panelCount * panelWidth) + ((panelCount - 1) * gap);
        int startX = (this.width - totalContentWidth) / 2;

        for (Category category : Category.values()) {
            if (category == Category.GUI) continue;
            panels.add(new Panel(category, startX, startY, panelWidth, panelHeight, this));
            startX += panelWidth + gap;
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, BG_COLOR.getRGB());
        for (Panel panel : panels) {
            panel.drawScreen(mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.listeningKeybind != null) {
            this.listeningKeybind.setListening(false);
            this.listeningKeybind = null;
            return;
        }
        for (Panel panel : panels) {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Panel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (Panel panel : panels) {
            panel.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.listeningKeybind != null) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
                this.listeningKeybind.setKeyCode(Keyboard.KEY_NONE);
            } else {
                this.listeningKeybind.setKeyCode(keyCode);
            }

            if (Main.INSTANCE != null && Main.INSTANCE.getOpenGuiKey() != null && this.listeningKeybind.name.equals("Open GUI")) {
                Main.INSTANCE.getOpenGuiKey().setKeyCode(this.listeningKeybind.getKeyCode());
                KeyBinding.resetKeyBindingArrayAndHash();
            }

            this.listeningKeybind.setListening(false);
            this.listeningKeybind = null;
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            for (Panel panel : panels) {
                panel.handleMouseScrolling(mouseX, mouseY, dWheel);
            }
        }
    }

    public boolean isListeningToKeybind() {
        return this.listeningKeybind != null;
    }

    public void setListeningKeybind(KeybindSetting keybind) {
        this.listeningKeybind = keybind;
    }

    public static void glColor(int color) {
        float f = (color >> 24 & 0xFF) / 255.0F;
        float f1 = (color >> 16 & 0xFF) / 255.0F;
        float f2 = (color >> 8 & 0xFF) / 255.0F;
        float f3 = (color & 0xFF) / 255.0F;
        GlStateManager.color(f1, f2, f3, f);
    }

    public static void drawCircle(float x, float y, float radius, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        glColor(color.getRGB());
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        GL11.glBegin(GL11.GL_POLYGON);
        for (int i = 0; i <= 360; i++) {
            GL11.glVertex2d(x + Math.sin(Math.toRadians(i)) * radius, y + Math.cos(Math.toRadians(i)) * radius);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRoundedRect(int x, int y, int width, int height, int radius, int color) {
        glColor(color);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        GL11.glBegin(GL11.GL_POLYGON);
        float r = (float)radius;
        for (int i = 0; i <= 90; i += 3) { GL11.glVertex2d(x + r - Math.sin(Math.toRadians(i)) * r, y + r - Math.cos(Math.toRadians(i)) * r); }
        for (int i = 90; i <= 180; i += 3) { GL11.glVertex2d(x + r - Math.sin(Math.toRadians(i)) * r, y + height - r - Math.cos(Math.toRadians(i)) * r); }
        for (int i = 180; i <= 270; i += 3) { GL11.glVertex2d(x + width - r - Math.sin(Math.toRadians(i)) * r, y + height - r - Math.cos(Math.toRadians(i)) * r); }
        for (int i = 270; i <= 360; i += 3) { GL11.glVertex2d(x + width - r - Math.sin(Math.toRadians(i)) * r, y + r - Math.cos(Math.toRadians(i)) * r); }
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void startScissor(int x, int y, int width, int height) {
        int scaleFactor = new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scaleFactor, (Minecraft.getMinecraft().displayHeight) - ((y + height) * scaleFactor), width * scaleFactor, height * scaleFactor);
    }

    public static void stopScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}