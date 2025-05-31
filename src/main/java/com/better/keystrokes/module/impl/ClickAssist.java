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
    private boolean engagedLeft = false;
    private boolean engagedRight = false;
    private long nextLeftClick = 0;
    private long nextRightClick = 0;

    public ClickAssist() {
        super("Click Assist", "Assists your clicking.", Category.COMBAT);
        this.moduleToggleKeybind = new KeybindSetting("Toggle Key", Keyboard.KEY_NONE);
        addSetting(this.moduleToggleKeybind);
        addSetting(leftClick = new TickSetting("Left click", true));
        addSetting(leftChance = new SliderSetting("Chance [L]", 75.0, 0.0, 100.0, 1));
        addSetting(leftMinDelay = new SliderSetting("Min ms [L]", 15.0, 1.0, 100.0, 1));
        addSetting(leftMaxDelay = new SliderSetting("Max ms [L]", 35.0, 1.0, 100.0, 1));
        addSetting(weaponOnly = new TickSetting("Weapon only [L]", true));
        addSetting(onlyWhileTargeting = new TickSetting("Only while targeting [L]", false));
        addSetting(rightClick = new TickSetting("Right click", false));
        addSetting(rightChance = new SliderSetting("Chance [R]", 60.0, 0.0, 100.0, 1));
        addSetting(rightMinDelay = new SliderSetting("Min ms [R]", 20.0, 1.0, 100.0, 1));
        addSetting(rightMaxDelay = new SliderSetting("Max ms [R]", 50.0, 1.0, 100.0, 1));
        addSetting(blocksOnly = new TickSetting("Blocks only [R]", true));

        try {
            this.bot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        try {
            if (this.bot == null) {
                this.bot = new Robot();
            }
        } catch (AWTException e) {
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        this.engagedLeft = false;
        this.engagedRight = false;
        this.nextLeftClick = 0;
        this.nextRightClick = 0;
    }

    @SubscribeEvent
    public void onMouseUpdate(MouseEvent event) {
        if (!this.isEnabled() || event.button < 0 || !event.buttonstate || mc.thePlayer == null || mc.currentScreen != null) {
            return;
        }

        if (mc.thePlayer.isEating() || mc.thePlayer.isBlocking()) {
            return;
        }

        if (event.button == 0 && leftClick.isToggled()) {
            handleLeftClick();
        } else if (event.button == 1 && rightClick.isToggled()) {
            handleRightClick();
        }
    }

    private void handleLeftClick() {
        if (this.engagedLeft) {
            this.engagedLeft = false;
            return;
        }

        if (weaponOnly.isToggled() && !PlayerUtils.isHoldingWeapon()) {
            return;
        }

        if (onlyWhileTargeting.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
            return;
        }

        if (System.currentTimeMillis() < this.nextLeftClick) {
            return;
        }

        if (random.nextDouble() * 100 > leftChance.getValue()) {
            updateLeftClickTiming();
            return;
        }

        if (this.bot != null) {
            this.bot.mouseRelease(16);
            this.bot.mousePress(16);
            this.engagedLeft = true;
            updateLeftClickTiming();
        }
    }

    private void handleRightClick() {
        if (this.engagedRight) {
            this.engagedRight = false;
            return;
        }

        if (blocksOnly.isToggled()) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
                return;
            }
        }

        if (System.currentTimeMillis() < this.nextRightClick) {
            return;
        }

        if (random.nextDouble() * 100 > rightChance.getValue()) {
            updateRightClickTiming();
            return;
        }

        if (this.bot != null) {
            this.bot.mouseRelease(4);
            this.bot.mousePress(4);
            this.engagedRight = true;
            updateRightClickTiming();
        }
    }

    private void updateLeftClickTiming() {
        double min = leftMinDelay.getValue();
        double max = leftMaxDelay.getValue();
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        long delay = (long) (min + (random.nextDouble() * (max - min)));
        this.nextLeftClick = System.currentTimeMillis() + delay;
    }

    private void updateRightClickTiming() {
        double min = rightMinDelay.getValue();
        double max = rightMaxDelay.getValue();
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        long delay = (long) (min + (random.nextDouble() * (max - min)));
        this.nextRightClick = System.currentTimeMillis() + delay;
    }
}