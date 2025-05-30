package com.better.keystrokes.utils;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.input.Mouse;

import java.nio.ByteBuffer;

public class ClientUtils {

    public static void click(int key) {
        KeyBinding.onTick(key);
    }

    public static void setKeyBindState(int key, boolean pressed) {
        KeyBinding.setKeyBindState(key, pressed);
    }

    public static void setMouseButtonState(int mouseButton, boolean held) {
        MouseEvent m = new MouseEvent();
        try {
            ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, mouseButton, "button");
            ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, held, "buttonstate");
            MinecraftForge.EVENT_BUS.post(m);

            ByteBuffer buttons = ObfuscationReflectionHelper.getPrivateValue(Mouse.class, null, "buttons");
            buttons.put(mouseButton, (byte)(held ? 1 : 0));
            ObfuscationReflectionHelper.setPrivateValue(Mouse.class, null, buttons, "buttons");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}