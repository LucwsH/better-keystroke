package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.settings.impl.KeybindSetting;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import com.better.keystrokes.settings.impl.DoubleSliderSetting;
import com.better.keystrokes.settings.impl.SliderSetting;
import com.better.keystrokes.settings.impl.TickSetting;
import com.better.keystrokes.utils.ClientUtils;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.lwjgl.input.Mouse;

import java.util.Random;

public class RightClicker extends Module {

    public DoubleSliderSetting cps;
    public SliderSetting jitter;
    public TickSetting onlyBlocks;
    public TickSetting noBlockSword;
    public TickSetting ignoreRods;
    public TickSetting allowEatAndDrink;
    public TickSetting allowBow;

    private final Random random = new Random();
    private long rightDownTime, rightUpTime, rightk, rightl, rightFatigue, rightBurst;
    private double rightm;
    private boolean rightn, rightDown, fatigued, burstMode;
    private int clickCount, burstClicks;
    private long sessionStart;

    public RightClicker() {
        super("Right Clicker", "Automatically clicks the right mouse button.", Category.COMBAT);
        this.moduleToggleKeybind = new KeybindSetting("Toggle Key", Keyboard.KEY_NONE);
        addSetting(this.moduleToggleKeybind);
        addSetting(cps = new DoubleSliderSetting("CPS", 10, 14, 1, 30, 1));
        addSetting(jitter = new SliderSetting("Jitter", 0.0, 0.0, 3.0, 1));
        addSetting(onlyBlocks = new TickSetting("Only blocks", false));
        addSetting(noBlockSword = new TickSetting("Don't block with sword", true));
        addSetting(ignoreRods = new TickSetting("Ignore rods", true));
        addSetting(allowEatAndDrink = new TickSetting("Allow eat & drink", true));
        addSetting(allowBow = new TickSetting("Allow bow", true));
    }

    @Override
    public void onEnable() {
        resetClicking();
        sessionStart = System.currentTimeMillis();
        clickCount = 0;
        fatigued = false;
        burstMode = false;
        burstClicks = 0;
    }

    @Override
    public void onDisable() {
        resetClicking();
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            if (this.rightDown) {
                resetClicking();
            }
            return;
        }

        if (mc.currentScreen != null || !mc.inGameHasFocus) {
            resetClicking();
            return;
        }

        if (Mouse.isButtonDown(1)) {
            ravenClickLogic();
        } else {
            resetClicking();
        }
    }

    private void ravenClickLogic() {
        if (!isClickAllowed()) {
            return;
        }

        int useKey = mc.gameSettings.keyBindUseItem.getKeyCode();

        if (this.rightUpTime == 0L || this.rightDownTime == 0L) {
            generateTimings();
        }

        if (System.currentTimeMillis() > this.rightUpTime) {
            ClientUtils.setKeyBindState(useKey, true);
            ClientUtils.click(useKey);
            ClientUtils.setMouseButtonState(1, true);
            applyJitter();
            generateTimings();
            this.rightDown = true;
            clickCount++;
        } else if (System.currentTimeMillis() > this.rightDownTime && this.rightDown) {
            ClientUtils.setKeyBindState(useKey, false);
            ClientUtils.setMouseButtonState(1, false);
            this.rightDown = false;
        }
    }

    private void generateTimings() {
        double min = cps.getMinValue();
        double max = cps.getMaxValue();

        updateFatigueState();
        updateBurstState();

        if (fatigued) {
            min *= 0.75;
            max *= 0.85;
        }

        if (burstMode && burstClicks < 4) {
            min *= 1.15;
            max *= 1.25;
            burstClicks++;
        } else if (burstMode) {
            burstMode = false;
            burstClicks = 0;
        }

        double currentCps = min + (random.nextDouble() * (max - min));

        if (random.nextInt(100) < 4) {
            currentCps *= 0.5 + (random.nextDouble() * 0.2);
        }

        long delay = (long) Math.round(1000.0D / currentCps);

        if (System.currentTimeMillis() > this.rightk) {
            if (!this.rightn && this.random.nextInt(100) >= 80) {
                this.rightn = true;
                this.rightm = 1.08D + this.random.nextDouble() * 0.18D;
            } else {
                this.rightn = false;
            }
            this.rightk = System.currentTimeMillis() + 450L + (long)this.random.nextInt(1600);
        }
        if (this.rightn) {
            delay = (long)((double)delay * this.rightm);
        }
        if (System.currentTimeMillis() > this.rightl) {
            if (this.random.nextInt(100) >= 78) {
                delay += 40L + (long)this.random.nextInt(110);
            }
            this.rightl = System.currentTimeMillis() + 550L + (long)this.random.nextInt(1900);
        }

        this.rightUpTime = System.currentTimeMillis() + delay;
        long downTimeOffset = delay / 2L - (long)this.random.nextInt(12);
        this.rightDownTime = System.currentTimeMillis() + Math.max(1, downTimeOffset);
        if (this.rightDownTime >= this.rightUpTime) {
            this.rightDownTime = this.rightUpTime - Math.max(1, delay / 11);
        }
    }

    private void updateFatigueState() {
        long timePlaying = System.currentTimeMillis() - sessionStart;
        if (timePlaying > 25000 && System.currentTimeMillis() > rightFatigue) {
            if (clickCount > 120 && random.nextInt(100) < 12) {
                fatigued = true;
                rightFatigue = System.currentTimeMillis() + 1800L + random.nextInt(2500);
            } else if (fatigued) {
                fatigued = false;
            }
        }
    }

    private void updateBurstState() {
        if (System.currentTimeMillis() > rightBurst && !burstMode) {
            if (random.nextInt(100) < 6) {
                burstMode = true;
                burstClicks = 0;
            }
            rightBurst = System.currentTimeMillis() + 4000L + random.nextInt(8000);
        }
    }

    private void resetClicking() {
        if(this.rightDown) {
            ClientUtils.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            ClientUtils.setMouseButtonState(1, false);
        }
        this.rightDownTime = 0L;
        this.rightUpTime = 0L;
        this.rightDown = false;
    }

    private boolean isClickAllowed() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();

        if (heldItem != null) {
            Item item = heldItem.getItem();

            if (allowEatAndDrink.isToggled() && (item instanceof ItemFood || item instanceof ItemPotion || item instanceof ItemBucketMilk)) {
                if (mc.thePlayer.isUsingItem()) return false;
            }
            if (allowBow.isToggled() && item instanceof ItemBow) {
                if (mc.thePlayer.isUsingItem()) return false;
            }
            if (ignoreRods.isToggled() && item instanceof ItemFishingRod) {
                return false;
            }
            if (onlyBlocks.isToggled() && !(item instanceof ItemBlock)) {
                return false;
            }
            if (noBlockSword.isToggled() && item instanceof ItemSword) {
                return false;
            }
        } else {
            if(onlyBlocks.isToggled()) {
                return false;
            }
        }
        return true;
    }

    private void applyJitter() {
        if (jitter.getValue() > 0) {
            double jitterValue = jitter.getValue() * 0.4;
            float yawChange = (random.nextBoolean() ? 1 : -1) * random.nextFloat() * (float)jitterValue;
            float pitchChange = (random.nextBoolean() ? 1 : -1) * random.nextFloat() * (float)(jitterValue * 0.45f);
            mc.thePlayer.rotationYaw += yawChange;
            mc.thePlayer.rotationPitch += pitchChange;
        }
    }
}