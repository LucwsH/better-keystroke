package com.better.keystrokes.gui;

import com.better.keystrokes.Main;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.module.impl.GuiModule;
import com.better.keystrokes.settings.Setting;
import com.better.keystrokes.settings.impl.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;

public class ClickGUI extends GuiScreen {

    private final Color COLOR_BACKGROUND = new Color(30, 32, 40, 150);
    private final Color COLOR_CARD = new Color(40, 42, 54);
    private final Color COLOR_SHADOW = new Color(20, 20, 30, 100);
    private final Color COLOR_ACCENT = new Color(80, 150, 255);
    private final Color COLOR_ACCENT_INACTIVE = new Color(68, 71, 90);
    private final Color COLOR_FONT = Color.WHITE;
    private final Color COLOR_FONT_SECONDARY = new Color(150, 150, 160);

    private final int settingHeight = 20;
    private final int padding = 10;
    private final int cardWidth = 130;

    private SliderSetting draggingSlider = null;
    private DoubleSliderSetting draggingDoubleMin = null;
    private DoubleSliderSetting draggingDoubleMax = null;
    private Module currentModuleForSlider = null;
    private KeybindSetting listeningKeybind = null;
    private Module draggingModule = null;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, COLOR_BACKGROUND.getRGB());

        for (Module m : ModuleManager.getModules()) {
            if (!m.visibleInGui) continue;

            if (draggingModule != null && draggingModule == m) {
                m.updatePosition(mouseX, mouseY);
            }
            drawModuleCard(m, mouseX, mouseY);
        }

        handleSliderDragging(mouseX);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawModuleCard(Module m, int mouseX, int mouseY) {
        int x = m.getX();
        int y = m.getY();
        int width = m.getWidth();
        int totalHeight = m.getHeight();
        int headerHeight = m.headerHeight;

        drawRoundedRect(x + 2, y + 2, width, totalHeight, 6, COLOR_SHADOW);
        drawRoundedRect(x, y, width, totalHeight, 6, COLOR_CARD);

        Color headerColor = (m.isEnabled() || m instanceof GuiModule) ? COLOR_ACCENT : new Color(45, 47, 54);
        drawRoundedRect(x, y, width, headerHeight, 6, headerColor);

        this.fontRendererObj.drawStringWithShadow(m.getName(), x + 7, y + 7, COLOR_FONT.getRGB());

        if (m.isExpanded) {
            int settingY = y + headerHeight + (padding / 2);
            for (Setting s : m.getSettings()) {
                drawSetting(s, x, settingY, mouseX, mouseY);
                settingY += settingHeight;
            }
        }
    }

    private void drawSetting(Setting s, int cardX, int y, int mouseX, int mouseY) {
        if (s instanceof TickSetting) {
            TickSetting setting = (TickSetting) s;
            this.fontRendererObj.drawString(setting.name, cardX + 8, y + 6, COLOR_FONT_SECONDARY.getRGB());
            drawToggleSwitch(setting.isToggled(), cardX + cardWidth - 28, y + 4);
        } else if (s instanceof SliderSetting) {
            SliderSetting setting = (SliderSetting) s;
            double percent = (setting.getValue() - setting.getMinValue()) / (setting.getMaxValue() - setting.getMinValue());
            int sliderWidth = cardWidth - 16;
            int fillWidth = (int) (sliderWidth * percent);
            String valueStr = String.format("%.1f", setting.getValue());
            this.fontRendererObj.drawString(setting.name, cardX + 8, y + 2, COLOR_FONT_SECONDARY.getRGB());
            this.fontRendererObj.drawString(valueStr, cardX + cardWidth - 8 - fontRendererObj.getStringWidth(valueStr), y + 2, COLOR_FONT.getRGB());
            int sliderX = cardX + 8;
            int sliderY = y + 13;
            drawRoundedRect(sliderX, sliderY, sliderWidth, 4, 2, COLOR_ACCENT_INACTIVE);
            if (fillWidth > 0) {
                drawRoundedRect(sliderX, sliderY, fillWidth, 4, 2, COLOR_ACCENT);
            }
            drawCircle(sliderX + fillWidth, sliderY + 2, 4, COLOR_ACCENT);
            drawCircle(sliderX + fillWidth, sliderY + 2, 3, COLOR_FONT);
        } else if (s instanceof ComboSetting) {
            ComboSetting setting = (ComboSetting) s;
            this.fontRendererObj.drawString(setting.name, cardX + 8, y + 6, COLOR_FONT_SECONDARY.getRGB());
            this.fontRendererObj.drawString(setting.getMode(), cardX + cardWidth - 8 - fontRendererObj.getStringWidth(setting.getMode()), y + 6, COLOR_FONT.getRGB());
        } else if (s instanceof DoubleSliderSetting) {
            DoubleSliderSetting setting = (DoubleSliderSetting) s;
            double range = setting.getSliderMaxValue() - setting.getSliderMinValue();
            double minPercent = (setting.getMinValue() - setting.getSliderMinValue()) / range;
            double maxPercent = (setting.getMaxValue() - setting.getSliderMinValue()) / range;
            int sliderWidth = cardWidth - 16;
            int fillStart = (int) (sliderWidth * minPercent);
            int fillEnd = (int) (sliderWidth * maxPercent);
            String valMinStr = String.format("%.1f", setting.getMinValue());
            String valMaxStr = String.format("%.1f", setting.getMaxValue());
            this.fontRendererObj.drawString(setting.name, cardX + 8, y + 2, COLOR_FONT_SECONDARY.getRGB());
            this.fontRendererObj.drawString(valMinStr + " - " + valMaxStr, cardX + cardWidth - 8 - fontRendererObj.getStringWidth(valMinStr + " - " + valMaxStr), y + 2, COLOR_FONT.getRGB());
            int sliderX = cardX + 8;
            int sliderY = y + 13;
            drawRoundedRect(sliderX, sliderY, sliderWidth, 4, 2, COLOR_ACCENT_INACTIVE);
            if (fillEnd > fillStart) {
                drawRoundedRect(sliderX + fillStart, sliderY, fillEnd - fillStart, 4, 2, COLOR_ACCENT);
            }
            drawCircle(sliderX + fillStart, sliderY + 2, 4, COLOR_ACCENT);
            drawCircle(sliderX + fillStart, sliderY + 2, 3, COLOR_FONT);
            drawCircle(sliderX + fillEnd, sliderY + 2, 4, COLOR_ACCENT);
            drawCircle(sliderX + fillEnd, sliderY + 2, 3, COLOR_FONT);
        } else if (s instanceof KeybindSetting) {
            KeybindSetting setting = (KeybindSetting) s;
            String keyName = setting.isListening() ? "..." : Keyboard.getKeyName(setting.getKeyCode());
            this.fontRendererObj.drawString(setting.name, cardX + 8, y + 6, COLOR_FONT_SECONDARY.getRGB());
            this.fontRendererObj.drawString(keyName, cardX + cardWidth - 8 - fontRendererObj.getStringWidth(keyName), y + 6, COLOR_FONT.getRGB());
        }
    }

    private void drawToggleSwitch(boolean enabled, int x, int y) {
        Color switchColor = enabled ? COLOR_ACCENT : COLOR_ACCENT_INACTIVE;
        Color circleColor = Color.WHITE;
        drawRoundedRect(x, y, 22, 12, 6, switchColor);
        drawCircle(enabled ? x + 16 : x + 6, y + 6, 4, circleColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.listeningKeybind != null) {
            this.listeningKeybind.setListening(false);
            this.listeningKeybind = null;
            return;
        }

        for (int i = ModuleManager.getModules().size() - 1; i >= 0; i--) {
            Module m = ModuleManager.getModules().get(i);

            if (!m.visibleInGui) continue;

            if (isHovered(mouseX, mouseY, m.getX(), m.getY(), m.getWidth(), m.getHeight())) {
                if (isHovered(mouseX, mouseY, m.getX(), m.getY(), m.getWidth(), m.headerHeight)) {
                    if (mouseButton == 0) {
                        if (!(m instanceof GuiModule)) {
                            m.toggle();
                        }
                        draggingModule = m;
                        m.startDragging(mouseX, mouseY);

                    } else if (mouseButton == 1) {
                        m.isExpanded = !m.isExpanded;
                    }
                } else if (m.isExpanded) {
                    int settingY = m.getY() + m.headerHeight + (padding / 2);
                    for (Setting s : m.getSettings()) {
                        if (isHovered(mouseX, mouseY, m.getX(), settingY, m.getWidth(), settingHeight)) {
                            this.currentModuleForSlider = m;
                            if (s instanceof TickSetting) {
                                ((TickSetting) s).toggle();
                            } else if (s instanceof ComboSetting) {
                                ((ComboSetting) s).cycle();
                            } else if (s instanceof SliderSetting) {
                                draggingSlider = (SliderSetting) s;
                            } else if (s instanceof DoubleSliderSetting) {
                                DoubleSliderSetting ds = (DoubleSliderSetting) s;
                                double range = ds.getSliderMaxValue() - ds.getSliderMinValue();
                                double sliderWidth = cardWidth - 16;
                                double minHandleX = m.getX() + 8 + (((ds.getMinValue() - ds.getSliderMinValue()) / range) * sliderWidth);
                                double maxHandleX = m.getX() + 8 + (((ds.getMaxValue() - ds.getSliderMinValue()) / range) * sliderWidth);
                                if (Math.abs(mouseX - minHandleX) < Math.abs(mouseX - maxHandleX)) {
                                    draggingDoubleMin = ds;
                                } else {
                                    draggingDoubleMax = ds;
                                }
                            } else if (s instanceof KeybindSetting) {
                                KeybindSetting ks = (KeybindSetting) s;
                                ks.setListening(true);
                                this.listeningKeybind = ks;
                            }
                            return;
                        }
                        settingY += settingHeight;
                    }
                }
                ModuleManager.getModules().remove(m);
                ModuleManager.getModules().add(m);
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.listeningKeybind != null) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
                this.listeningKeybind.setKeyCode(Keyboard.KEY_NONE);
            } else {
                this.listeningKeybind.setKeyCode(keyCode);
            }
            // Atualiza a keybind principal do mod se ela for alterada
            if(Main.INSTANCE.getOpenGuiKey() != null) {
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
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if(draggingModule != null && state == 0) {
            draggingModule.stopDragging();
            draggingModule = null;
        }
        draggingSlider = null;
        draggingDoubleMin = null;
        draggingDoubleMax = null;
        currentModuleForSlider = null;
        super.mouseReleased(mouseX, mouseY, state);
    }

    // Este método é necessário para arrastar sliders mesmo quando o mouse sai do componente
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(draggingSlider != null || draggingDoubleMin != null || draggingDoubleMax != null) {
            handleSliderDragging(mouseX);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    private void handleSliderDragging(int mouseX) {
        if (draggingSlider == null && draggingDoubleMin == null && draggingDoubleMax == null) return;
        if (currentModuleForSlider == null) return;

        int sliderStartX = currentModuleForSlider.getX() + 8;
        int sliderWidth = currentModuleForSlider.getWidth() - 16;
        double relativeMouseX = mouseX - sliderStartX;
        double percent = Math.max(0.0, Math.min(1.0, relativeMouseX / sliderWidth));

        if (draggingSlider != null) {
            double range = draggingSlider.getMaxValue() - draggingSlider.getMinValue();
            double newValue = draggingSlider.getMinValue() + (percent * range);
            draggingSlider.setValue(newValue);
        } else if (draggingDoubleMin != null) {
            double range = draggingDoubleMin.getSliderMaxValue() - draggingDoubleMin.getSliderMinValue();
            double newValue = draggingDoubleMin.getSliderMinValue() + (percent * range);
            draggingDoubleMin.setMinValue(newValue);
        } else if (draggingDoubleMax != null) {
            double range = draggingDoubleMax.getSliderMaxValue() - draggingDoubleMax.getSliderMinValue();
            double newValue = draggingDoubleMax.getSliderMinValue() + (percent * range);
            draggingDoubleMax.setMaxValue(newValue);
        }
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void glColor(Color color) {
        GlStateManager.color(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }

    public static void drawCircle(float x, float y, float radius, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        glColor(color);
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

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        glColor(color);
        GL11.glBegin(GL11.GL_POLYGON);
        float x2 = x + width;
        float y2 = y + height;
        float r = Math.min(Math.min(width, height) / 2, radius);
        for (int i = 0; i <= 90; i += 3) { GL11.glVertex2d(x + r - Math.sin(Math.toRadians(i)) * r, y + r - Math.cos(Math.toRadians(i)) * r); }
        for (int i = 90; i <= 180; i += 3) { GL11.glVertex2d(x + r - Math.sin(Math.toRadians(i)) * r, y2 - r - Math.cos(Math.toRadians(i)) * r); }
        for (int i = 180; i <= 270; i += 3) { GL11.glVertex2d(x2 - r - Math.sin(Math.toRadians(i)) * r, y2 - r - Math.cos(Math.toRadians(i)) * r); }
        for (int i = 270; i <= 360; i += 3) { GL11.glVertex2d(x2 - r - Math.sin(Math.toRadians(i)) * r, y + r - Math.cos(Math.toRadians(i)) * r); }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}