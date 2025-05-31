package com.better.keystrokes.command;

import com.better.keystrokes.gui.KeystrokesGui;
import com.better.keystrokes.module.ModuleManager;
import com.better.keystrokes.module.impl.KeystrokesModule;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class CommandKeystrokes extends CommandBase {
    
    private boolean shouldOpenGui = false;

    @Override
    public String getCommandName() {
        return "keystrokes";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/keystrokes [toggle]";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        KeystrokesModule module = ModuleManager.getModule(KeystrokesModule.class);
        if (module == null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Keystrokes module not found."));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
            module.toggle();
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Keystrokes " + (module.isEnabled() ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled") + "."));
        } else {
            this.shouldOpenGui = true;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (shouldOpenGui) {
            Minecraft.getMinecraft().displayGuiScreen(new KeystrokesGui());
            shouldOpenGui = false;
        }
    }
}