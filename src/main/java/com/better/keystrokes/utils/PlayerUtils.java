package com.better.keystrokes.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;

public class PlayerUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isPlayerInGame() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public static boolean isHoldingWeapon() {
        if (!isPlayerInGame() || mc.thePlayer.getCurrentEquippedItem() == null) {
            return false;
        }
        return mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword ||
                mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemAxe;
    }
}