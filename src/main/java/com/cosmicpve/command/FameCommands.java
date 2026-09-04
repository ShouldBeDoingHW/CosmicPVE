package com.cosmicpve.command;

import com.cosmicpve.economy.FameService;
import com.cosmicpve.economy.fame.FameShopMenu;
import com.cosmicpve.economy.fame.FameShopService;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class FameCommands {
    private static final FameService FAME = new FameService();
    private static final FameShopService SHOP = new FameShopService();
    private FameCommands() {}
    public static void registerPublic(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("fame").executes(c -> open(c.getSource())));
        event.getDispatcher().register(Commands.literal("fameshop").executes(c -> open(c.getSource())));
    }
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("fame")
                .then(Commands.literal("inspect").then(Commands.argument("player", EntityArgument.player())
                        .executes(c -> inspect(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                .then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                .executes(c -> mutate(c.getSource(), EntityArgument.getPlayer(c, "player"),
                                        LongArgumentType.getLong(c, "amount"), 0)))))
                .then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(c -> mutate(c.getSource(), EntityArgument.getPlayer(c, "player"),
                                        LongArgumentType.getLong(c, "amount"), 1)))))
                .then(Commands.literal("shop")
                        .then(Commands.literal("refresh").then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> refresh(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                        .then(Commands.literal("inspect").then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> inspectShop(c.getSource(), EntityArgument.getPlayer(c, "player"))))));
    }
    private static int open(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException(); SHOP.catalog(player);
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new FameShopMenu(id, inventory),
                FameShopMenu.TITLE), buffer -> buffer.writeInt(0)); return 1;
    }
    private static int inspect(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal(player.getScoreboardName() + " Fame: " + FAME.balance(player))
                .withColor(FameService.COLOR), false); return 1;
    }
    private static int mutate(CommandSourceStack source, ServerPlayer player, long amount, int operation) {
        boolean ok = operation == 0 ? FAME.set(player, amount) : FAME.add(player, amount);
        if (!ok) { source.sendFailure(Component.literal("Fame mutation rejected.")); return 0; }
        return inspect(source, player);
    }
    private static int refresh(CommandSourceStack source, ServerPlayer player) {
        var catalog = SHOP.forceRefresh(player);
        source.sendSuccess(() -> Component.literal("Generated 9 Fame offers; refresh day " + catalog.nextRefreshDay()), true); return 1;
    }
    private static int inspectShop(CommandSourceStack source, ServerPlayer player) {
        var catalog = SHOP.catalog(player);
        source.sendSuccess(() -> Component.literal("Fame Shop generated day=" + catalog.generatedDay()
                + " refresh=" + catalog.nextRefreshDay()), false);
        for (int i = 0; i < catalog.offers().size(); i++) {
            int slot = i; var offer = catalog.offers().get(i);
            source.sendSuccess(() -> Component.literal(slot + ": " + offer.payload().getFirst().getHoverName().getString()
                    + " source=" + offer.sourceTier() + " row=" + offer.sourceRow()
                    + " price=" + offer.price() + " purchased=" + offer.purchased()), false);
        }
        return catalog.offers().size();
    }
}
