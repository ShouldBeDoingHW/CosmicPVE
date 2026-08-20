package com.cosmicpve.command;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.cosmicpve.data.component.EnchantmentOrbData;
import com.cosmicpve.equipment.enchantment.OrbType;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class EnchantingCommands {
    private EnchantingCommands() {}
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        var give = Commands.literal("give").then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("enchantment-id", IdentifierArgument.id())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .then(Commands.argument("success", IntegerArgumentType.integer(1,100))
                                        .then(Commands.argument("destroy", IntegerArgumentType.integer(1,100))
                                                .executes(c -> giveBook(c.getSource(), EntityArgument.getPlayer(c,"player"),
                                                        IdentifierArgument.getId(c,"enchantment-id"), IntegerArgumentType.getInteger(c,"level"),
                                                        IntegerArgumentType.getInteger(c,"success"), IntegerArgumentType.getInteger(c,"destroy"))))))));
        var random = Commands.literal("give-random").then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("enchantment-id", IdentifierArgument.id())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1)).executes(c -> {
                            var id=IdentifierArgument.getId(c,"enchantment-id");
                            var spec=CosmicEnchantmentSpecs.find(id);
                            if(spec.isEmpty()){c.getSource().sendFailure(Component.literal("Unknown Cosmic enchantment."));return 0;}
                            var tier=spec.orElseThrow().tier(); var rng=c.getSource().getServer().overworld().getRandom();
                            return giveBook(c.getSource(),EntityArgument.getPlayer(c,"player"),id,IntegerArgumentType.getInteger(c,"level"),
                                    tier.randomSuccess(rng),tier.randomDestroy(rng));
                        }))));
        var scroll = Commands.literal("white-scroll").then(Commands.literal("give")
                .then(Commands.argument("player",EntityArgument.player())
                        .executes(c -> giveScroll(EntityArgument.getPlayer(c,"player"),1))
                        .then(Commands.argument("count",IntegerArgumentType.integer(1,64))
                                .executes(c -> giveScroll(EntityArgument.getPlayer(c,"player"),IntegerArgumentType.getInteger(c,"count"))))));
        var transmog = Commands.literal("transmog").then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(c -> giveSimple(EntityArgument.getPlayer(c, "player"), ModItems.TRANSMOG_SCROLL.get(), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                .executes(c -> giveSimple(EntityArgument.getPlayer(c, "player"), ModItems.TRANSMOG_SCROLL.get(),
                                        IntegerArgumentType.getInteger(c, "count"))))));
        var orbGive = Commands.literal("give").then(Commands.argument("player", EntityArgument.player())
                .then(orbType("armor", OrbType.ARMOR, false)).then(orbType("weapon", OrbType.WEAPON, false)));
        var orbRandom = Commands.literal("give-random").then(Commands.argument("player", EntityArgument.player())
                .then(orbType("armor", OrbType.ARMOR, true)).then(orbType("weapon", OrbType.WEAPON, true)));
        var orbs = Commands.literal("orb").then(orbGive).then(orbRandom);
        return Commands.literal("enchant").then(Commands.literal("book").then(give).then(random))
                .then(scroll).then(transmog).then(orbs);
    }
    private static int giveBook(net.minecraft.commands.CommandSourceStack source, net.minecraft.server.level.ServerPlayer player,
            net.minecraft.resources.Identifier id,int level,int success,int destroy){
        var spec=CosmicEnchantmentSpecs.find(id); var holder=source.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(id);
        if(spec.isEmpty()||holder.isEmpty()||level>holder.orElseThrow().value().getMaxLevel()
                ||!spec.orElseThrow().tier().allowsRates(success,destroy)){
            source.sendFailure(Component.literal("Invalid Cosmic enchantment book parameters.")); return 0;
        }
        var stack=new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        stack.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(),new CosmicEnchantmentBookData(1,id,level,success,destroy));
        player.getInventory().placeItemBackInInventory(stack); return 1;
    }
    private static int giveScroll(net.minecraft.server.level.ServerPlayer player,int count){
        player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.WHITE_SCROLL.get(),count)); return count;
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> orbType(
            String literal, OrbType type, boolean random) {
        var node = Commands.literal(literal);
        if (random) return node.executes(c -> {
            var rng = c.getSource().getServer().overworld().getRandom();
            return giveOrb(EntityArgument.getPlayer(c, "player"), type,
                    rng.nextInt(100) + 1, rng.nextInt(100) + 1);
        });
        return node.then(Commands.argument("success", IntegerArgumentType.integer(1, 100))
                .then(Commands.argument("destroy", IntegerArgumentType.integer(1, 100))
                        .executes(c -> giveOrb(EntityArgument.getPlayer(c, "player"), type,
                                IntegerArgumentType.getInteger(c, "success"),
                                IntegerArgumentType.getInteger(c, "destroy")))));
    }

    private static int giveOrb(net.minecraft.server.level.ServerPlayer player, OrbType type, int success, int destroy) {
        var item = type == OrbType.ARMOR ? ModItems.ARMOR_ENCHANTMENT_ORB.get() : ModItems.WEAPON_ENCHANTMENT_ORB.get();
        var stack = new ItemStack(item);
        stack.set(ModDataComponents.ENCHANTMENT_ORB.get(),
                new EnchantmentOrbData(EnchantmentOrbData.CURRENT_DATA_VERSION, success, destroy));
        player.getInventory().placeItemBackInInventory(stack);
        return 1;
    }

    private static int giveSimple(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        player.getInventory().placeItemBackInInventory(new ItemStack(item, count));
        return count;
    }
}
