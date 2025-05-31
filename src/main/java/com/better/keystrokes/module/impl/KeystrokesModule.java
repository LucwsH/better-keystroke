
package com.better.keystrokes.module.impl;

import com.better.keystrokes.gui.ClickGUI;
import com.better.keystrokes.gui.KeystrokesGui;
import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.settings.impl.TickSetting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.awt.Color;

public class KeystrokesModule extends Module {

    private final Color KEY_BACKGROUND_COLOR = new Color(0, 0, 0, 100);
    private final Color KEY_PRESSED_COLOR = new Color(200, 200, 200, 120);
    private final Color KEY_TEXT_COLOR = Color.WHITE;
    private final int KEY_ROUNDING_RADIUS = 3;

    public int keyWidth = 22;
    public int keyHeight = 22;
    public int gap = 3;
    public int spacebarHeight = 16;
    public int mouseButtonWidth;

    public TickSetting showMouseButtons;
    public TickSetting showSpacebar;
    public TickSetting textShadow;

    public KeystrokesModule() {
        super("Keystrokes", "Displays your keystrokes on screen.", Category.MISC);
        this.visibleInGui = false;
        this.enabled = true;

        this.setX(20);
        this.setY(50);

        mouseButtonWidth = (keyWidth * 3 + gap * 2 - gap) / 2;

        addSetting(showMouseButtons = new TickSetting("Show Mouse Buttons", true));
        addSetting(showSpacebar = new TickSetting("Show Spacebar", true));
        addSetting(textShadow = new TickSetting("Text Shadow", true));
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!this.isEnabled() || mc.gameSettings.showDebugInfo || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (mc.currentScreen == null || mc.currentScreen instanceof KeystrokesGui) {
            if (event.phase == TickEvent.Phase.END) {
                renderKeystrokesLayout();
            }
        }
    }

    public void renderForPreview() {
        renderKeystrokesLayout();
    }

    public int getDisplayWidth() {
        return (keyWidth * 3) + (gap * 2);
    }

    public int getDisplayHeight() {
        int currentHeight = 0;
        currentHeight += keyHeight;
        currentHeight += gap + keyHeight;

        if (showMouseButtons.isToggled()) {
            currentHeight += gap + keyHeight;
        }
        if (showSpacebar.isToggled()) {
            currentHeight += gap + spacebarHeight;
        }
        return currentHeight;
    }

    private void renderKeystrokesLayout() {
        int currentX = this.getX();
        int currentY = this.getY();

        KeyBinding keyW = mc.gameSettings.keyBindForward;
        drawKey(keyW.isKeyDown(), currentX + keyWidth + gap, currentY, keyWidth, keyHeight, "W");
        int nextY = currentY + keyHeight + gap;

        KeyBinding keyA = mc.gameSettings.keyBindLeft;
        KeyBinding keyS = mc.gameSettings.keyBindBack;
        KeyBinding keyD = mc.gameSettings.keyBindRight;
        drawKey(keyA.isKeyDown(), currentX, nextY, keyWidth, keyHeight, "A");
        drawKey(keyS.isKeyDown(), currentX + keyWidth + gap, nextY, keyWidth, keyHeight, "S");
        drawKey(keyD.isKeyDown(), currentX + (keyWidth + gap) * 2, nextY, keyWidth, keyHeight, "D");
        nextY += keyHeight + gap;

        if (showMouseButtons.isToggled()) {
            boolean lmbPressed = Mouse.isButtonDown(0);
            boolean rmbPressed = Mouse.isButtonDown(1);

            drawKey(lmbPressed, currentX, nextY, mouseButtonWidth, keyHeight, "LMB");
            drawKey(rmbPressed, currentX + mouseButtonWidth + gap, nextY, mouseButtonWidth, keyHeight, "RMB");
            nextY += keyHeight + gap;
        }

        if (showSpacebar.isToggled()) {
            KeyBinding keySpace = mc.gameSettings.keyBindJump;
            drawKey(keySpace.isKeyDown(), currentX, nextY, (keyWidth * 3) + (gap * 2), spacebarHeight, "");
        }
    }

    private void drawKey(boolean pressed, int x, int y, int width, int height, String text) {
        ClickGUI.drawRoundedRect(x, y, width, height, KEY_ROUNDING_RADIUS, pressed ? KEY_PRESSED_COLOR.getRGB() : KEY_BACKGROUND_COLOR.getRGB());

        if (!text.isEmpty()) {
            int textWidth = mc.fontRendererObj.getStringWidth(text);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - mc.fontRendererObj.FONT_HEIGHT) / 2 + 1;

            if (textShadow.isToggled()) {
                mc.fontRendererObj.drawStringWithShadow(text, textX, textY, KEY_TEXT_COLOR.getRGB());
            } else {
                mc.fontRendererObj.drawString(text, textX, textY, KEY_TEXT_COLOR.getRGB());
            }
        }
    }
}