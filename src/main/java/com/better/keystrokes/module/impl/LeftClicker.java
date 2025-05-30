package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Module;
import com.better.keystrokes.settings.impl.ComboSetting;
import com.better.keystrokes.settings.impl.DoubleSliderSetting;
import com.better.keystrokes.settings.impl.SliderSetting;
import com.better.keystrokes.settings.impl.TickSetting;
import com.better.keystrokes.utils.ClientUtils;
import com.better.keystrokes.utils.PlayerUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

public class LeftClicker extends Module {

    public DoubleSliderSetting cps;
    public SliderSetting jitter;
    public TickSetting weaponOnly;
    public TickSetting breakBlocks;
    public TickSetting inventoryFill;
    public ComboSetting clickTimings;

    private final Random random = new Random();
    private long leftDownTime, leftUpTime, leftk, leftl;
    private boolean leftDown, breakHeld, leftn;
    private double leftm;

    private Method guiMouseClickedMethod = null;
    private long nextInventoryClickTime = 0;
    private Field theSlotField;

    public LeftClicker() {
        super("Left Clicker", "Automatically clicks the left mouse button.");

        addSetting(cps = new DoubleSliderSetting("CPS", 9, 13, 1, 30, 1));
        addSetting(jitter = new SliderSetting("Jitter", 0.0, 0.0, 3.0, 1));
        addSetting(weaponOnly = new TickSetting("Weapon only", false));
        addSetting(breakBlocks = new TickSetting("Break blocks", false));
        addSetting(inventoryFill = new TickSetting("Inventory fill", true));
        addSetting(clickTimings = new ComboSetting("Click Event", "On Render", "On Tick", "On Render"));

        try {
            this.guiMouseClickedMethod = ReflectionHelper.findMethod(
                    GuiScreen.class, null, new String[]{"func_73864_a", "mouseClicked"},
                    int.class, int.class, int.class
            );
            if (this.guiMouseClickedMethod != null) {
                this.guiMouseClickedMethod.setAccessible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (event.phase != TickEvent.Phase.START || !this.isEnabled() || !"On Render".equals(clickTimings.getMode())) {
            return;
        }

        if (mc.currentScreen == null && mc.inGameHasFocus) {
            if (Mouse.isButtonDown(0)) {
                if (weaponOnly.isToggled() && !PlayerUtils.isHoldingWeapon()) {
                    return;
                }
                if (handleBreakBlocks()) {
                    return;
                }
                ravenClickLogic();
            } else {
                resetClicking();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !this.isEnabled()) {
            return;
        }

        if (mc.currentScreen instanceof GuiContainer) {
            if (inventoryFill.isToggled() && Mouse.isButtonDown(0)) {
                if (mc.thePlayer.inventory.getItemStack() != null) return;
                Slot hoveredSlot = getSlotUnderMouse((GuiContainer) mc.currentScreen);
                if (hoveredSlot != null && (hoveredSlot.inventory instanceof InventoryCrafting || hoveredSlot instanceof SlotCrafting)) {
                    return;
                }
                if (System.currentTimeMillis() > this.nextInventoryClickTime) {
                    double min = cps.getMinValue();
                    double max = cps.getMaxValue();
                    double currentCps = min + (random.nextDouble() * (max - min));
                    this.nextInventoryClickTime = System.currentTimeMillis() + (long) (1000 / currentCps);
                    performInventoryClick();
                }
            }
        } else if ("On Tick".equals(clickTimings.getMode())) {
            if (Mouse.isButtonDown(0)) {
                if (weaponOnly.isToggled() && !PlayerUtils.isHoldingWeapon()) return;
                if (handleBreakBlocks()) return;
                ravenClickLogic();
            } else {
                resetClicking();
            }
        }
    }

    private void ravenClickLogic() {
        int attackKey = mc.gameSettings.keyBindAttack.getKeyCode();

        if (this.leftUpTime == 0L || this.leftDownTime == 0L) {
            generateTimings();
        }

        if (System.currentTimeMillis() > this.leftUpTime) {
            ClientUtils.setKeyBindState(attackKey, true);
            ClientUtils.click(attackKey);
            ClientUtils.setMouseButtonState(0, true);
            applyJitter();
            generateTimings();
            this.leftDown = true;
        } else if (System.currentTimeMillis() > this.leftDownTime && this.leftDown) {
            ClientUtils.setKeyBindState(attackKey, false);
            ClientUtils.setMouseButtonState(0, false);
            this.leftDown = false;
        }
    }

    private void generateTimings() {
        double min = cps.getMinValue();
        double max = cps.getMaxValue();
        double currentCps = min + (random.nextDouble() * (max - min));
        long delay = (long) Math.round(1000.0D / currentCps);

        if (System.currentTimeMillis() > this.leftk) {
            if (!this.leftn && this.random.nextInt(100) >= 85) {
                this.leftn = true;
                this.leftm = 1.1D + this.random.nextDouble() * 0.15D;
            } else {
                this.leftn = false;
            }
            this.leftk = System.currentTimeMillis() + 500L + (long) this.random.nextInt(1500);
        }
        if (this.leftn) {
            delay = (long) ((double) delay * this.leftm);
        }
        if (System.currentTimeMillis() > this.leftl) {
            if (this.random.nextInt(100) >= 80) {
                delay += 50L + (long) this.random.nextInt(100);
            }
            this.leftl = System.currentTimeMillis() + 500L + (long) this.random.nextInt(1500);
        }

        this.leftUpTime = System.currentTimeMillis() + delay;
        this.leftDownTime = System.currentTimeMillis() + (delay / 2L) - (long) (this.random.nextInt(10));
    }

    private void resetClicking() {
        if (this.leftDown || this.breakHeld) {
            ClientUtils.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
            ClientUtils.setMouseButtonState(0, false);
        }
        this.leftDownTime = 0L;
        this.leftUpTime = 0L;
        this.leftDown = false;
        this.breakHeld = false;
    }

    private Slot getSlotUnderMouse(GuiContainer gui) {
        if (gui == null) return null;
        try {
            if (theSlotField == null) {
                theSlotField = ReflectionHelper.findField(GuiContainer.class, "theSlot", "field_147006_u");
                theSlotField.setAccessible(true);
            }
            return (Slot) theSlotField.get(gui);
        } catch (Exception e) {
            return null;
        }
    }

    private void performInventoryClick() {
        if (this.guiMouseClickedMethod == null || mc.currentScreen == null) return;
        try {
            int mouseX = Mouse.getX() * mc.currentScreen.width / mc.displayWidth;
            int mouseY = mc.currentScreen.height - Mouse.getY() * mc.currentScreen.height / mc.displayHeight - 1;
            this.guiMouseClickedMethod.invoke(mc.currentScreen, mouseX, mouseY, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean handleBreakBlocks() {
        if (breakBlocks.isToggled() && mc.objectMouseOver != null) {
            BlockPos pos = mc.objectMouseOver.getBlockPos();
            if (pos != null) {
                if (mc.theWorld.getBlockState(pos).getBlock() != Blocks.air && !(mc.theWorld.getBlockState(pos).getBlock() instanceof net.minecraft.block.BlockLiquid)) {
                    if (!this.breakHeld) {
                        ClientUtils.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                        ClientUtils.setMouseButtonState(0, true);
                        this.breakHeld = true;
                    }
                    return true;
                }
            }
        }
        if (this.breakHeld) {
            ClientUtils.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
            ClientUtils.setMouseButtonState(0, false);
            this.breakHeld = false;
        }
        return false;
    }

    private void applyJitter() {
        if (jitter.getValue() > 0) {
            double jitterValue = jitter.getValue() * 0.5;
            mc.thePlayer.rotationYaw += (random.nextBoolean() ? -1 : 1) * random.nextFloat() * jitterValue;
            mc.thePlayer.rotationPitch += (random.nextBoolean() ? -1 : 1) * random.nextFloat() * jitterValue * 0.5f;
        }
    }
}