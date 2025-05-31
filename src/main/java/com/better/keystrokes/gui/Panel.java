package com.better.keystrokes.gui;

import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.module.impl.SelfDestruct;
import com.better.keystrokes.settings.Setting;
import com.better.keystrokes.settings.impl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.util.List;
import java.util.Locale;

public class Panel {
    public int x, y, width, height;
    public Category category;
    private final ClickGUI parent;
    private boolean dragging;
    private int dragX, dragY;
    public float scrollY;
    private float totalContentHeight;

    private final List<Module> modules;
    private final Minecraft mc = Minecraft.getMinecraft();
    private final FontRenderer fr = mc.fontRendererObj;

    private SliderSetting draggingSlider = null;
    private DoubleSliderSetting draggingDoubleMin = null;
    private DoubleSliderSetting draggingDoubleMax = null;

    private static final int HEADER_HEIGHT = 18;
    private static final int MODULE_HEIGHT = 16;
    private static final int SETTING_HEIGHT = 16;
    private static final int BORDER_RADIUS = 4;
    private static final Color PANEL_BG_COLOR = new Color(21, 22, 25);
    private static final Color HEADER_COLOR = new Color(28, 29, 33);
    private static final Color MODULE_BG_COLOR = new Color(44, 47, 51);
    private static final Color FONT_COLOR_TITLE = new Color(220, 221, 222);
    private static final Color FONT_COLOR_BODY = new Color(180, 181, 182);
    private static final Color ACCENT_COLOR = new Color(79, 93, 223);
    private static final Color ACCENT_INACTIVE = new Color(68, 71, 90);

    private static final int SLIDER_WIDTH = 70;

    public Panel(Category category, int x, int y, int width, int height, ClickGUI parent) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.parent = parent;
        this.modules = ModuleManager.getModulesInCategory(category);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        ClickGUI.drawRoundedRect(x, y, width, height, BORDER_RADIUS, PANEL_BG_COLOR.getRGB());
        ClickGUI.drawRoundedRect(x, y, width, HEADER_HEIGHT, BORDER_RADIUS, HEADER_COLOR.getRGB());
        fr.drawString(category.name, x + (width - fr.getStringWidth(category.name)) / 2, y + (HEADER_HEIGHT - fr.FONT_HEIGHT) / 2, FONT_COLOR_TITLE.getRGB());

        ClickGUI.startScissor(x, y + HEADER_HEIGHT, width, height - HEADER_HEIGHT);

        int currentY = (int) (y + HEADER_HEIGHT + 5 - scrollY);
        totalContentHeight = 5;

        for (Module module : modules) {
            if (!module.visibleInGui) continue;

            boolean isSelfDestruct = module instanceof SelfDestruct;

            ClickGUI.drawRect(x + 2, currentY, x + width - 2, currentY + MODULE_HEIGHT, module.isEnabled() ? ACCENT_COLOR.darker().getRGB() : MODULE_BG_COLOR.getRGB());
            if (isHovered(mouseX, mouseY, x + 2, currentY, width - 4, MODULE_HEIGHT)) {
                ClickGUI.drawRect(x + 2, currentY, x + width - 2, currentY + MODULE_HEIGHT, new Color(255, 255, 255, 20).getRGB());
            }

            fr.drawString(module.getName(), x + 5, currentY + (MODULE_HEIGHT - fr.FONT_HEIGHT) / 2, FONT_COLOR_TITLE.getRGB());
            if (!module.getSettings().isEmpty() && !isSelfDestruct) {
                fr.drawString(module.isExpanded ? "-" : "+", x + width - 10, currentY + (MODULE_HEIGHT - fr.FONT_HEIGHT) / 2, FONT_COLOR_TITLE.getRGB());
            }

            currentY += MODULE_HEIGHT + 2;
            totalContentHeight += MODULE_HEIGHT + 2;

            if (module.isExpanded && !isSelfDestruct) {
                for(Setting setting : module.getSettings()){
                    drawSetting(setting, x + 4, currentY, width - 8, mouseX, mouseY);
                    currentY += SETTING_HEIGHT + 2;
                    totalContentHeight += SETTING_HEIGHT + 2;
                }
            }
        }
        ClickGUI.stopScissor();
        updateSlider(mouseX);
        updateDoubleSlider(mouseX);
    }

    private void drawSetting(Setting s, int x, int y, int width, int mouseX, int mouseY){
        fr.drawString(s.name, x + 2, y + (SETTING_HEIGHT - fr.FONT_HEIGHT) / 2, FONT_COLOR_BODY.getRGB());

        if (s instanceof TickSetting) {
            TickSetting set = (TickSetting) s;
            int toggleX = x + width - 22;
            int toggleY = y + (SETTING_HEIGHT - 12) / 2;
            ClickGUI.drawRoundedRect(toggleX, toggleY, 22, 12, 6, set.isToggled() ? ACCENT_COLOR.getRGB() : ACCENT_INACTIVE.getRGB());
            ClickGUI.drawCircle(set.isToggled() ? toggleX + 16 : toggleX + 6, toggleY + 6, 4, Color.WHITE);
        }
        else if (s instanceof SliderSetting) {
            SliderSetting set = (SliderSetting) s;
            int sliderX = x + width - SLIDER_WIDTH - 2;
            int sliderY = y + (SETTING_HEIGHT - 4) / 2;

            String valueStr = String.format(Locale.US, "%." + set.decimalPlaces + "f", set.getValue());
            fr.drawString(valueStr, sliderX - fr.getStringWidth(valueStr) - 4, y + (SETTING_HEIGHT - fr.FONT_HEIGHT) / 2, FONT_COLOR_BODY.getRGB());

            double percent = (set.getValue() - set.getMinValue()) / (set.getMaxValue() - set.getMinValue());
            int fillWidth = (int) (SLIDER_WIDTH * percent);

            ClickGUI.drawRoundedRect(sliderX, sliderY, SLIDER_WIDTH, 4, 2, ACCENT_INACTIVE.getRGB());
            if (fillWidth > 0) ClickGUI.drawRoundedRect(sliderX, sliderY, fillWidth, 4, 2, ACCENT_COLOR.getRGB());
            ClickGUI.drawCircle(sliderX + fillWidth, sliderY + 2, 4, Color.WHITE);
        }
        else if (s instanceof DoubleSliderSetting) {
            DoubleSliderSetting set = (DoubleSliderSetting) s;
            int sliderX = x + width - SLIDER_WIDTH - 2;
            int sliderY = y + (SETTING_HEIGHT - 4) / 2;

            String minValStr = String.format(Locale.US, "%." + set.decimalPlaces + "f", set.getMinValue());
            String maxValStr = String.format(Locale.US, "%." + set.decimalPlaces + "f", set.getMaxValue());
            String text = minValStr + "-" + maxValStr;

            fr.drawString(text, sliderX - fr.getStringWidth(text) - 4, y + (SETTING_HEIGHT - fr.FONT_HEIGHT) / 2, FONT_COLOR_BODY.getRGB());

            double minPercent = (set.getMinValue() - set.getSliderMinValue()) / (set.getSliderMaxValue() - set.getSliderMinValue());
            double maxPercent = (set.getMaxValue() - set.getSliderMinValue()) / (set.getSliderMaxValue() - set.getSliderMinValue());

            int minFill = (int) (SLIDER_WIDTH * minPercent);
            int maxFill = (int) (SLIDER_WIDTH * maxPercent);

            ClickGUI.drawRoundedRect(sliderX, sliderY, SLIDER_WIDTH, 4, 2, ACCENT_INACTIVE.getRGB());
            if (maxFill - minFill > 0) {
                ClickGUI.drawRoundedRect(sliderX + minFill, sliderY, maxFill - minFill, 4, 2, ACCENT_COLOR.getRGB());
            }

            ClickGUI.drawCircle(sliderX + minFill, sliderY + 2, 4, Color.WHITE);
            ClickGUI.drawCircle(sliderX + maxFill, sliderY + 2, 4, Color.WHITE);
        }
        else if (s instanceof ComboSetting) {
            ComboSetting set = (ComboSetting) s;
            String mode = set.getMode();
            int modeWidth = fr.getStringWidth(mode) + 8;
            int modeX = x + width - modeWidth - 2;
            ClickGUI.drawRoundedRect(modeX, y + 2, modeWidth, SETTING_HEIGHT - 4, 3, ACCENT_INACTIVE.getRGB());
            fr.drawString(mode, modeX + 4, y + (SETTING_HEIGHT - fr.FONT_HEIGHT) / 2, FONT_COLOR_TITLE.getRGB());
        }
        else if (s instanceof KeybindSetting) {
            KeybindSetting set = (KeybindSetting) s;
            String keyName = set.isListening() ? "..." : Keyboard.getKeyName(set.getKeyCode());
            String text = "[" + keyName + "]";
            fr.drawString(text, x + width - fr.getStringWidth(text) - 2, y + (SETTING_HEIGHT - fr.FONT_HEIGHT) / 2, FONT_COLOR_TITLE.getRGB());
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
            if (mouseButton == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            }
            return;
        }

        if(isHovered(mouseX, mouseY, x, y + HEADER_HEIGHT, width, height - HEADER_HEIGHT)) {
            int currentY = (int) (y + HEADER_HEIGHT + 5 - scrollY);
            for (Module module : modules) {
                if (!module.visibleInGui) continue;

                if (isHovered(mouseX, mouseY, x, currentY, width, MODULE_HEIGHT)) {
                    if (module instanceof SelfDestruct) {
                        if (mouseButton == 0) {
                            module.onEnable();
                        }
                    } else {
                        if (mouseButton == 0) module.toggle();
                        else if (mouseButton == 1) {
                            if (!module.getSettings().isEmpty()) {
                                module.isExpanded = !module.isExpanded;
                            }
                        }
                    }
                    return;
                }
                currentY += MODULE_HEIGHT + 2;

                if (module.isExpanded) {
                    for(Setting setting : module.getSettings()){
                        if(isHovered(mouseX, mouseY, x + 4, currentY, width - 8, SETTING_HEIGHT)){
                            handleSettingClick(setting, mouseX, mouseY, mouseButton, x + 4, currentY, width-8);
                            return;
                        }
                        currentY += SETTING_HEIGHT + 2;
                    }
                }
            }
        }
    }

    private void handleSettingClick(Setting s, int mouseX, int mouseY, int mouseButton, int x, int y, int width){
        if(s instanceof TickSetting){
            ((TickSetting) s).toggle();
        }
        else if(s instanceof SliderSetting){
            if (mouseButton == 0) {
                draggingSlider = (SliderSetting) s;
                updateSlider(mouseX);
            }
        }
        else if (s instanceof DoubleSliderSetting) {
            if (mouseButton == 0) {
                DoubleSliderSetting ds = (DoubleSliderSetting) s;
                int sliderX = x + width - SLIDER_WIDTH - 2;
                double minHandleX = sliderX + (SLIDER_WIDTH * ((ds.getMinValue() - ds.getSliderMinValue()) / (ds.getSliderMaxValue() - ds.getSliderMinValue())));
                double maxHandleX = sliderX + (SLIDER_WIDTH * ((ds.getMaxValue() - ds.getSliderMinValue()) / (ds.getSliderMaxValue() - ds.getSliderMinValue())));

                if (Math.abs(mouseX - minHandleX) < Math.abs(mouseX - maxHandleX)) {
                    draggingDoubleMin = ds;
                } else {
                    draggingDoubleMax = ds;
                }
                updateDoubleSlider(mouseX);
            }
        }
        else if(s instanceof ComboSetting){
            if (mouseButton == 0) {
                ((ComboSetting) s).cycle();
            } else if (mouseButton == 1) {
                ((ComboSetting) s).cycleReverse();
            }
        }
        else if(s instanceof KeybindSetting){
            KeybindSetting set = (KeybindSetting) s;
            set.setListening(true);
            parent.setListeningKeybind(set);
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            dragging = false;
            draggingSlider = null;
            draggingDoubleMin = null;
            draggingDoubleMax = null;
        }
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick){
        updateSlider(mouseX);
        updateDoubleSlider(mouseX);
    }

    private void updateSlider(int mouseX) {
        if (draggingSlider != null) {
            int settingX = this.x + 4;
            int settingWidth = this.width - 8;
            int sliderX = settingX + settingWidth - SLIDER_WIDTH - 2;

            double percent = Math.max(0, Math.min(1, (double)(mouseX - sliderX) / SLIDER_WIDTH));
            double range = draggingSlider.getMaxValue() - draggingSlider.getMinValue();
            double newValue = draggingSlider.getMinValue() + (percent * range);
            draggingSlider.setValue(newValue);
        }
    }

    private void updateDoubleSlider(int mouseX) {
        int settingX = this.x + 4;
        int settingWidth = this.width - 8;
        int sliderX = settingX + settingWidth - SLIDER_WIDTH - 2;

        if (draggingDoubleMin != null) {
            double percent = Math.max(0, Math.min(1, (double)(mouseX - sliderX) / SLIDER_WIDTH));
            double range = draggingDoubleMin.getSliderMaxValue() - draggingDoubleMin.getSliderMinValue();
            double newValue = draggingDoubleMin.getSliderMinValue() + (percent * range);
            draggingDoubleMin.setMinValue(newValue);
        }
        if (draggingDoubleMax != null) {
            double percent = Math.max(0, Math.min(1, (double)(mouseX - sliderX) / SLIDER_WIDTH));
            double range = draggingDoubleMax.getSliderMaxValue() - draggingDoubleMax.getSliderMinValue();
            double newValue = draggingDoubleMax.getSliderMinValue() + (percent * range);
            draggingDoubleMax.setMaxValue(newValue);
        }
    }

    public void handleMouseScrolling(int mouseX, int mouseY, int dWheel) {
        if (isHovered(mouseX, mouseY, x, y, width, height)) {
            if (dWheel > 0) {
                scrollY = Math.max(0, scrollY - 15);
            } else if (dWheel < 0) {
                float maxScroll = Math.max(0, totalContentHeight - (height - HEADER_HEIGHT));
                scrollY = Math.min(maxScroll, scrollY + 15);
            }
        }
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}