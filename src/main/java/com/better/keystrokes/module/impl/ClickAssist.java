package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.settings.impl.KeybindSetting;
import com.better.keystrokes.settings.impl.SliderSetting;
import com.better.keystrokes.settings.impl.TickSetting;
import com.better.keystrokes.utils.PlayerUtils;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Random;

public class ClickAssist extends Module {

    public SliderSetting leftChance;
    public SliderSetting rightChance;
    public SliderSetting leftMinDelay;
    public SliderSetting leftMaxDelay;
    public SliderSetting rightMinDelay;
    public SliderSetting rightMaxDelay;
    public TickSetting leftClick;
    public TickSetting rightClick;
    public TickSetting weaponOnly;
    public TickSetting blocksOnly;
    public TickSetting onlyWhileTargeting;

    private Robot bot;
    private final Random random = new Random();

    public ClickAssist() {
        super("Click Assist", "Assists your clicking.", Category.COMBAT);
        this.moduleToggleKeybind = new KeybindSetting("Toggle Key", Keyboard.KEY_NONE);
        addSetting(this.moduleToggleKeybind);
        addSetting(leftClick = new TickSetting("Left click", true));
        addSetting(leftChance = new SliderSetting("Chance [L]", 75.0, 0.0, 100.0, 1));
        addSetting(leftMinDelay = new SliderSetting("Min ms [L]", 15.0, 1.0, 200.0, 1));
        addSetting(leftMaxDelay = new SliderSetting("Max ms [L]", 35.0, 1.0, 200.0, 1));
        addSetting(weaponOnly = new TickSetting("Weapon only [L]", true));
        addSetting(onlyWhileTargeting = new TickSetting("Only while targeting [L]", false));
        addSetting(rightClick = new TickSetting("Right click", false));
        addSetting(rightChance = new SliderSetting("Chance [R]", 60.0, 0.0, 100.0, 1));
        addSetting(rightMinDelay = new SliderSetting("Min ms [R]", 20.0, 1.0, 200.0, 1));
        addSetting(rightMaxDelay = new SliderSetting("Max ms [R]", 50.0, 1.0, 200.0, 1));
        addSetting(blocksOnly = new TickSetting("Blocks only [R]", true));

        try {
            this.bot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            this.setEnabled(false);
        }
    }

    @Override
    public void onEnable() {
        if (this.bot == null) {
            try {
                this.bot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
                this.setEnabled(false);
            }
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!event.buttonstate) {
            return;
        }

        if (!this.isEnabled() || mc.thePlayer == null || mc.currentScreen != null || this.bot == null) {
            return;
        }

        if (mc.thePlayer.isEating() || mc.thePlayer.isBlocking()) {
            return;
        }

        if (event.button == 0 && leftClick.isToggled()) {
            handleLeftClick();
        }

        if (event.button == 1 && rightClick.isToggled()) {
            handleRightClick();
        }
    }

    private void handleLeftClick() {
        if (weaponOnly.isToggled() && !PlayerUtils.isHoldingWeapon()) {
            return;
        }

        if (onlyWhileTargeting.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
            return;
        }

        if (random.nextDouble() < (leftChance.getValue() / 100.0)) {
            new Thread(() -> {
                try {
                    long delay = calculateDelay(leftMinDelay.getValue(), leftMaxDelay.getValue());
                    Thread.sleep(delay);
                    bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    Thread.sleep(5);
                    bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void handleRightClick() {
        if (blocksOnly.isToggled()) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
                return;
            }
        }

        if (random.nextDouble() < (rightChance.getValue() / 100.0)) {
            new Thread(() -> {
                try {
                    long delay = calculateDelay(rightMinDelay.getValue(), rightMaxDelay.getValue());
                    Thread.sleep(delay);
                    bot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                    Thread.sleep(5);
                    bot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private long calculateDelay(double min, double max) {
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return (long) (min + (random.nextDouble() * (max - min)));
    }
}