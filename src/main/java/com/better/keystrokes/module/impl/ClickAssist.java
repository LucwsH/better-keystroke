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
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClickAssist extends Module {

    public SliderSetting leftChance;
    public SliderSetting rightChance;
    public SliderSetting leftMinCPS;
    public SliderSetting rightMinCPS;
    public TickSetting leftClick;
    public TickSetting rightClick;
    public TickSetting weaponOnly;
    public TickSetting blocksOnly;
    public TickSetting onlyWhileTargeting;

    private Robot bot;
    private final Random random = new Random();
    private boolean engagedLeft = false;
    private boolean engagedRight = false;
    private final ConcurrentLinkedQueue<Long> clicks = new ConcurrentLinkedQueue<>();

    public ClickAssist() {
        super("Click Assist", "Assists your clicks to boost CPS.", Category.COMBAT);
        this.moduleToggleKeybind = new KeybindSetting("Toggle Key", Keyboard.KEY_NONE);
        addSetting(this.moduleToggleKeybind);
        addSetting(leftClick = new TickSetting("Left click", true));
        addSetting(leftChance = new SliderSetting("Chance [L]", 80.0, 0.0, 100.0, 1));
        addSetting(leftMinCPS = new SliderSetting("Min CPS [L]", 5, 1, 20, 1));
        addSetting(weaponOnly = new TickSetting("Weapon only [L]", true));
        addSetting(onlyWhileTargeting = new TickSetting("Only while targeting [L]", false));
        addSetting(rightClick = new TickSetting("Right click", false));
        addSetting(rightChance = new SliderSetting("Chance [R]", 80.0, 0.0, 100.0, 1));
        addSetting(rightMinCPS = new SliderSetting("Min CPS [R]", 5, 1, 20, 1));
        addSetting(blocksOnly = new TickSetting("Blocks only [R]", true));

        try {
            this.bot = new Robot();
        } catch (AWTException e) {
            this.setEnabled(false);
        }
    }

    @Override
    public void onEnable() {
        if (this.bot == null) {
            try {
                this.bot = new Robot();
            } catch (AWTException e) {
                this.setEnabled(false);
            }
        }
    }

    @Override
    public void onDisable() {
        if (bot != null) {
            fix(0);
            fix(1);
        }
        this.engagedLeft = false;
        this.engagedRight = false;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (mc.thePlayer == null || bot == null) {
            return;
        }
        fix(0);
        fix(1);
    }

    private int getCPS() {
        long currentTime = System.currentTimeMillis();
        clicks.removeIf(time -> time < currentTime - 1000);
        return clicks.size();
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!event.buttonstate) {
            return;
        }

        clicks.add(System.currentTimeMillis());

        if (!this.isEnabled() || bot == null || mc.currentScreen != null || mc.thePlayer == null) {
            return;
        }

        if (mc.thePlayer.isEating() || mc.thePlayer.isBlocking()) {
            return;
        }

        if (event.button == 0 && leftClick.isToggled()) {
            if (getCPS() < leftMinCPS.getValue()) return;
            if (engagedLeft) {
                engagedLeft = false;
            } else {
                if (weaponOnly.isToggled() && !PlayerUtils.isHoldingWeapon()) return;
                if (onlyWhileTargeting.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) return;
                if (random.nextDouble() * 100.0 >= leftChance.getValue()) return;

                bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                engagedLeft = true;
            }
        } else if (event.button == 1 && rightClick.isToggled()) {
            if (getCPS() < rightMinCPS.getValue()) return;
            if (engagedRight) {
                engagedRight = false;
            } else {
                if (blocksOnly.isToggled()) {
                    ItemStack item = mc.thePlayer.getHeldItem();
                    if (item == null || !(item.getItem() instanceof ItemBlock)) return;
                }
                if (random.nextDouble() * 100.0 >= rightChance.getValue()) return;

                bot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                bot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                engagedRight = true;
            }
        }
    }

    private void fix(int button) {
        if (button == 0) {
            if (engagedLeft && !Mouse.isButtonDown(0)) {
                bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                engagedLeft = false;
            }
        } else if (button == 1) {
            if (engagedRight && !Mouse.isButtonDown(1)) {
                bot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                engagedRight = false;
            }
        }
    }
}