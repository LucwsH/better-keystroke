package com.better.keystrokes.module.impl;

import com.better.keystrokes.module.Category;
import com.better.keystrokes.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class DelayRemover extends Module {

    private Field leftClickCounterField;

    public DelayRemover() {
        super("Delay Remover", "Removes the left click delay.", Category.COMBAT);
        this.visibleInGui = false;

        try {
            this.leftClickCounterField = ReflectionHelper.findField(Minecraft.class, "field_71429_W", "leftClickCounter");
            this.leftClickCounterField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
            this.setEnabled(false);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || !this.isEnabled() || this.leftClickCounterField == null) {
            return;
        }

        if (event.phase == TickEvent.Phase.START) {
            try {
                if (leftClickCounterField.getInt(mc) > 0) {
                    this.leftClickCounterField.set(mc, 0);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                this.setEnabled(false);
            }
        }
    }
}