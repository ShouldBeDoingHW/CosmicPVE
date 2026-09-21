package com.cosmicpve.command;

import com.cosmicpve.adventure.AdventureInfoMenu;
import net.minecraft.commands.Commands;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class AdventureInfoCommands {
    private AdventureInfoCommands() {}
    public static final java.util.List<String> ALIASES = java.util.List.of("adventures", "adventure", "adv");
    public static void registerPublic(RegisterCommandsEvent event) {
        for (String alias : ALIASES) event.getDispatcher().register(Commands.literal(alias).executes(context -> {
            var player = context.getSource().getPlayerOrException();
            player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new AdventureInfoMenu(id, inventory),
                    AdventureInfoMenu.TITLE));
            return 1;
        }));
    }
}
