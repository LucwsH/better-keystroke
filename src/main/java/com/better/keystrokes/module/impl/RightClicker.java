package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Module;
import com.better.keystrokes.settings.impl.DoubleSliderSetting;
import com.better.keystrokes.settings.impl.SliderSetting;
import com.better.keystrokes.settings.impl.TickSetting;
import com.better.keystrokes.utils.ClientUtils;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
    private long rightDownTime, rightUpTime, rightk, rightl;
    private double rightm;
    private boolean rightn;
    private boolean rightDown;

    public RightClicker() {
        super("Right Clicker", "Automatically clicks the right mouse button.");

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
    }

    @Override
    public void onDisable() {
        resetClicking();
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !this.isEnabled() || mc.currentScreen != null || !mc.inGameHasFocus) {
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
        } else if (System.currentTimeMillis() > this.rightDownTime && this.rightDown) {
            ClientUtils.setKeyBindState(useKey, false);
            ClientUtils.setMouseButtonState(1, false);
            this.rightDown = false;
        }
    }

    private void generateTimings() {
        double min = cps.getMinValue();
        double max = cps.getMaxValue();
        double currentCps = min + (random.nextDouble() * (max - min));
        long delay = (long) Math.round(1000.0D / currentCps);

        if (System.currentTimeMillis() > this.rightk) {
            if (!this.rightn && this.random.nextInt(100) >= 85) {
                this.rightn = true;
                this.rightm = 1.1D + this.random.nextDouble() * 0.15D;
            } else {
                this.rightn = false;
            }
            this.rightk = System.currentTimeMillis() + 500L + (long)this.random.nextInt(1500);
        }
        if (this.rightn) {
            delay = (long)((double)delay * this.rightm);
        }
        if (System.currentTimeMillis() > this.rightl) {
            if (this.random.nextInt(100) >= 80) {
                delay += 50L + (long)this.random.nextInt(100);
            }
            this.rightl = System.currentTimeMillis() + 500L + (long)this.random.nextInt(1500);
        }

        this.rightUpTime = System.currentTimeMillis() + delay;
        this.rightDownTime = System.currentTimeMillis() + delay / 2L - (long)this.random.nextInt(10);
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
                return false;
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
            double jitterValue = jitter.getValue() * 0.45;
            if (random.nextBoolean()) {
                mc.thePlayer.rotationYaw += (float)(random.nextFloat() * jitterValue);
            } else {
                mc.thePlayer.rotationYaw -= (float)(random.nextFloat() * jitterValue);
            }

            if (random.nextBoolean()) {
                mc.thePlayer.rotationPitch += (float)(random.nextFloat() * jitterValue * 0.5f);
            } else {
                mc.thePlayer.rotationPitch -= (float)(random.nextFloat() * jitterValue * 0.5f);
            }
        }
    }
}