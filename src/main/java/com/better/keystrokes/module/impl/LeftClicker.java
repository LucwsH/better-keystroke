package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.settings.impl.KeybindSetting;
import org.lwjgl.input.Keyboard;
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
    private long leftDownTime, leftUpTime, leftk, leftl, leftFatigue, leftBurst;
    private boolean leftDown, breakHeld, leftn, fatigued, burstMode;
    private double leftm;
    private int clickCount, burstClicks;
    private long sessionStart;

    private Method guiMouseClickedMethod = null;
    private long nextInventoryClickTime = 0;
    private Field theSlotField;

    public LeftClicker() {
        super("Left Clicker", "Automatically clicks the left mouse button.", Category.COMBAT);
        this.moduleToggleKeybind = new KeybindSetting("Toggle Key", Keyboard.KEY_NONE);
        addSetting(this.moduleToggleKeybind);
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
        ClickAssist clickAssistModule = (ClickAssist) ModuleManager.getModule(ClickAssist.class);
        if (clickAssistModule != null && clickAssistModule.isEnabled() && clickAssistModule.leftClick.isToggled()) {
            this.setEnabled(false);
            return;
        }

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

    private void handleEvent() {
        if (!this.isEnabled()) {
            if (this.leftDown || this.breakHeld) {
                resetClicking();
            }
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
        } else if (mc.currentScreen instanceof GuiContainer) {
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
            } else {
                resetClicking();
            }
        }
        else {
            resetClicking();
        }
    }


    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START && "On Render".equals(clickTimings.getMode())) {
            handleEvent();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && "On Tick".equals(clickTimings.getMode())) {
            handleEvent();
        }
    }

    private void ravenClickLogic() {
        int attackKey = mc.gameSettings.keyBindAttack.getKeyCode();

        if (this.leftUpTime == 0L || this.leftDownTime == 0L) {
            generateTimings();
        }

        if (System.currentTimeMillis() > this.leftDownTime && this.leftDown) {
            ClientUtils.setKeyBindState(attackKey, false);
            ClientUtils.setMouseButtonState(0, false);
            this.leftDown = false;
        }

        if (System.currentTimeMillis() > this.leftUpTime) {
            ClientUtils.setKeyBindState(attackKey, true);
            ClientUtils.click(attackKey);
            ClientUtils.setMouseButtonState(0, true);
            applyJitter();
            generateTimings();
            this.leftDown = true;
            clickCount++;
        }
    }

    private void generateTimings() {
        double min = cps.getMinValue();
        double max = cps.getMaxValue();

        updateFatigueState();
        updateBurstState();

        if (fatigued) {
            min *= 0.7;
            max *= 0.8;
        }

        if (burstMode && burstClicks < 5) {
            min *= 1.2;
            max *= 1.3;
            burstClicks++;
        } else if (burstMode) {
            burstMode = false;
            burstClicks = 0;
        }

        double currentCps = min + (random.nextDouble() * (max - min));

        if (random.nextInt(100) < 3) {
            currentCps *= 0.4 + (random.nextDouble() * 0.3);
        }

        long delay = (long) Math.round(1000.0D / currentCps);

        if (System.currentTimeMillis() > this.leftk) {
            if (!this.leftn && this.random.nextInt(100) >= 82) {
                this.leftn = true;
                this.leftm = 1.05D + this.random.nextDouble() * 0.2D;
            } else {
                this.leftn = false;
            }
            this.leftk = System.currentTimeMillis() + 400L + (long) this.random.nextInt(1800);
        }
        if (this.leftn) {
            delay = (long) ((double) delay * this.leftm);
        }
        if (System.currentTimeMillis() > this.leftl) {
            if (this.random.nextInt(100) >= 75) {
                delay += 30L + (long) this.random.nextInt(120);
            }
            this.leftl = System.currentTimeMillis() + 600L + (long) this.random.nextInt(2000);
        }

        this.leftUpTime = System.currentTimeMillis() + delay;
        long downTimeOffset = (long) (delay * (0.35 + random.nextDouble() * 0.2));
        this.leftDownTime = System.currentTimeMillis() + Math.max(1, downTimeOffset);
        if (this.leftDownTime >= this.leftUpTime) {
            this.leftDownTime = this.leftUpTime - Math.max(1, delay / 12);
        }
    }

    private void updateFatigueState() {
        long timePlaying = System.currentTimeMillis() - sessionStart;
        if (timePlaying > 30000 && System.currentTimeMillis() > leftFatigue) {
            if (clickCount > 150 && random.nextInt(100) < 15) {
                fatigued = true;
                leftFatigue = System.currentTimeMillis() + 2000L + random.nextInt(3000);
            } else if (fatigued) {
                fatigued = false;
            }
            clickCount = 0;
        }
    }

    private void updateBurstState() {
        if (System.currentTimeMillis() > leftBurst && !burstMode) {
            if (random.nextInt(100) < 8) {
                burstMode = true;
                burstClicks = 0;
            }
            leftBurst = System.currentTimeMillis() + 3000L + random.nextInt(7000);
        }
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
        this.nextInventoryClickTime = 0L;
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
            double jitterValue = jitter.getValue() * 0.45;
            float yawChange = (random.nextBoolean() ? -1 : 1) * (float)(random.nextDouble() * jitterValue);
            float pitchChange = (random.nextBoolean() ? -1 : 1) * (float)(random.nextDouble() * (jitterValue * 0.4f));
            mc.thePlayer.rotationYaw += yawChange;
            mc.thePlayer.rotationPitch += pitchChange;
        }
    }
}