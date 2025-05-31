package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class FastPlace extends Module {

    private Field rightClickDelayTimerField;

    public FastPlace() {
        super("Fast Place", "Removes the right click delay.", Category.COMBAT);
        this.visibleInGui = false;

        try {
            this.rightClickDelayTimerField = ReflectionHelper.findField(Minecraft.class, "field_71467_ac", "rightClickDelayTimer");
            this.rightClickDelayTimerField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
            this.setEnabled(false);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || !this.isEnabled() || this.rightClickDelayTimerField == null) {
            return;
        }

        if (event.phase == TickEvent.Phase.START) {
            try {
                if (rightClickDelayTimerField.getInt(mc) > 0) {
                    this.rightClickDelayTimerField.set(mc, 0);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                this.setEnabled(false);
            }
        }
    }
}