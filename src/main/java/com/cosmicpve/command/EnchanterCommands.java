package com.cosmicpve.command;

import com.cosmicpve.enchanter.EnchanterMenu;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class EnchanterCommands {
    private EnchanterCommands() {}

    public static void registerPublic(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("enchanter").executes(context -> {
            var player = context.getSource().getPlayerOrException();
            player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new EnchanterMenu(id, inventory),
                    Component.literal("ENCHANTER").withStyle(style -> style.withColor(0xFFAA00).withBold(true))),
                    buffer -> buffer.writeInt(EnchanterMenu.SLOT_COUNT));
            return 1;
        }));
    }
}
