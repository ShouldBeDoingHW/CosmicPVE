package com.cosmicpve.command;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.content.definition.reward.EnchantmentLevelMode;
import com.cosmicpve.content.definition.reward.GeneratedEquipmentCategory;
import com.cosmicpve.content.definition.reward.GeneratedEquipmentDefinition;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.reward.GeneratedEquipmentService;
import com.cosmicpve.reward.RewardDeliveryService;
import com.cosmicpve.reward.RewardGenerationContext;
import com.cosmicpve.reward.RewardGeneratorService;
import com.cosmicpve.reward.RewardTableService;
import com.cosmicpve.reward.spawner.MobSpawners;
import com.cosmicpve.reward.spawner.MobSpawnerEligibility;
import com.cosmicpve.reward.spawner.MobSpawnerConfiguration;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class RewardCommands {
    private static final RewardDeliveryService DELIVERY = new RewardDeliveryService();
    private RewardCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        var tableId = Commands.argument("table", IdentifierArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                        CosmicContent.repository().snapshot().rewardTables().keySet(), builder));
        var spawner = Commands.literal("spawner")
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("entity_type", IdentifierArgument.id())
                                        .executes(context -> giveSpawner(context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                IdentifierArgument.getId(context, "entity_type"), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> giveSpawner(context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        IdentifierArgument.getId(context, "entity_type"),
                                                        IntegerArgumentType.getInteger(context, "count")))))))
                .then(Commands.literal("inspect")
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                                .executes(context -> inspectSpawner(context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "position")))))
                .then(Commands.literal("debug-delay")
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(1, Short.MAX_VALUE))
                                        .executes(context -> debugSpawnerDelay(context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "position"),
                                                IntegerArgumentType.getInteger(context, "ticks"))))));
        return Commands.literal("reward")
                .then(Commands.literal("list").executes(context -> list(context.getSource())))
                .then(Commands.literal("inspect").then(tableId.executes(context -> inspect(
                        context.getSource(), IdentifierArgument.getId(context, "table")))))
                .then(Commands.literal("reload").executes(context -> reload(context.getSource())))
                .then(Commands.literal("roll").then(Commands.argument("table", IdentifierArgument.id())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                CosmicContent.repository().snapshot().rewardTables().keySet(), builder))
                        .executes(context -> roll(context.getSource(), IdentifierArgument.getId(context, "table"), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                .executes(context -> roll(context.getSource(),
                                        IdentifierArgument.getId(context, "table"),
                                        IntegerArgumentType.getInteger(context, "count"))))))
                .then(spawner)
                .then(Commands.literal("book").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rarity", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                java.util.Arrays.stream(CosmicEnchantmentTier.values())
                                                        .map(CosmicEnchantmentTier::serializedName), builder))
                                        .executes(context -> giveBooks(context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "rarity"), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> giveBooks(context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        StringArgumentType.getString(context, "rarity"),
                                                        IntegerArgumentType.getInteger(context, "count"))))))))
                .then(Commands.literal("equipment").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("maximum_rarity", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                List.of("ultimate", "legendary"), builder))
                                        .then(Commands.argument("minimum", IntegerArgumentType.integer(1, 5))
                                                .then(Commands.argument("maximum", IntegerArgumentType.integer(1, 5))
                                                        .then(Commands.argument("levels", StringArgumentType.word())
                                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                                        List.of("maximum", "random_valid"), builder))
                                                                .executes(context -> giveEquipment(context.getSource(),
                                                                        EntityArgument.getPlayer(context, "player"),
                                                                        StringArgumentType.getString(context, "maximum_rarity"),
                                                                        IntegerArgumentType.getInteger(context, "minimum"),
                                                                        IntegerArgumentType.getInteger(context, "maximum"),
                                                                        StringArgumentType.getString(context, "levels"))))))))));
    }

    private static int list(net.minecraft.commands.CommandSourceStack source) {
        var tables = CosmicContent.repository().snapshot().rewardTables().keySet();
        source.sendSuccess(() -> Component.literal("Cosmic reward tables (" + tables.size() + "): " + tables), false);
        return tables.size();
    }

    private static int inspect(net.minecraft.commands.CommandSourceStack source, Identifier id) {
        var table = CosmicContent.repository().findRewardTable(id);
        if (table.isEmpty()) { source.sendFailure(Component.literal("Unknown reward table: " + id)); return 0; }
        source.sendSuccess(() -> Component.literal(id + ": total weight " + table.orElseThrow().totalWeight()
                + ", entries " + table.orElseThrow().entries()), false);
        return table.orElseThrow().entries().size();
    }

    private static int reload(net.minecraft.commands.CommandSourceStack source) {
        source.getServer().reloadResources(source.getServer().getPackRepository().getSelectedIds());
        source.sendSuccess(() -> Component.literal("Cosmic content reload requested."), true);
        return 1;
    }

    private static int roll(net.minecraft.commands.CommandSourceStack source, Identifier id, int count)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        if (CosmicContent.repository().findRewardTable(id).isEmpty()) {
            source.sendFailure(Component.literal("Unknown reward table: " + id)); return 0;
        }
        var context = new RewardGenerationContext(source.registryAccess(), player.getRandom(), player);
        var rewards = new RewardTableService(CosmicContent.repository(), new RewardGeneratorService()).roll(id, count, context);
        DELIVERY.deliver(player, rewards);
        source.sendSuccess(() -> Component.literal("Rolled " + id + " " + count + " time(s); delivered "
                + rewards.size() + " stack(s)."), true);
        return rewards.size();
    }

    private static int giveSpawner(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, Identifier entityId, int count) {
        if (!MobSpawnerEligibility.isEligible(entityId)) {
            source.sendFailure(Component.literal("Entity type is not a valid mob: " + entityId)); return 0;
        }
        DELIVERY.deliver(player, List.of(MobSpawners.create(entityId, count)));
        source.sendSuccess(() -> Component.literal("Gave " + count + " " + entityId + " spawner(s)."), true);
        return count;
    }

    private static int inspectSpawner(net.minecraft.commands.CommandSourceStack source, net.minecraft.core.BlockPos position) {
        if (!(source.getLevel().getBlockEntity(position)
                instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity spawner)) {
            source.sendFailure(Component.literal("No mob spawner exists at " + position.toShortString() + "."));
            return 0;
        }
        var tag = spawner.saveCustomOnly(source.registryAccess());
        var level = source.getLevel();
        int blockLight = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, position);
        int skyLight = level.getBrightness(net.minecraft.world.level.LightLayer.SKY, position);
        boolean playerInRange = level.hasNearbyAlivePlayer(position.getX() + 0.5D, position.getY() + 0.5D,
                position.getZ() + 0.5D, 16.0D);
        boolean chunkLoaded = level.hasChunkAt(position);
        boolean spawnersEnabled = level.isSpawnerBlockEnabled();
        source.sendSuccess(() -> Component.literal("Spawner at " + position.toShortString() + ": " + tag
                + ", difficulty=" + level.getDifficulty() + ", block_light=" + blockLight
                + ", sky_light=" + skyLight + ", player_within_16=" + playerInRange
                + ", chunk_loaded=" + chunkLoaded + ", spawners_enabled=" + spawnersEnabled), false);
        return 1;
    }

    private static int debugSpawnerDelay(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.core.BlockPos position, int ticks) {
        if (!(source.getLevel().getBlockEntity(position)
                instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity spawner)) {
            source.sendFailure(Component.literal("No mob spawner exists at " + position.toShortString() + "."));
            return 0;
        }
        boolean changed = MobSpawnerConfiguration.setDebugDelay(spawner, source.getLevel(), position, ticks);
        if (!changed) { source.sendFailure(Component.literal("Invalid debug delay.")); return 0; }
        source.sendSuccess(() -> Component.literal("Set spawner delay at " + position.toShortString()
                + " to " + ticks + " tick(s). Normal future delays remain vanilla."), true);
        return 1;
    }

    private static int giveEquipment(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, String rarityName, int minimum, int maximum, String modeName) {
        try {
            var rarity = CosmicEnchantmentTier.valueOf(rarityName.toUpperCase(Locale.ROOT));
            var mode = EnchantmentLevelMode.valueOf(modeName.toUpperCase(Locale.ROOT));
            if (maximum < minimum) throw new IllegalArgumentException("maximum must be at least minimum");
            var definition = new GeneratedEquipmentDefinition(GeneratedEquipmentCategory.RANDOM_IRON_ARMOR_PIECE,
                    minimum, maximum, rarity, mode);
            var stack = new GeneratedEquipmentService().generate(definition,
                    source.registryAccess().lookupOrThrow(Registries.ENCHANTMENT), player.getRandom());
            DELIVERY.deliver(player, List.of(stack));
            source.sendSuccess(() -> Component.literal("Generated " + stack.getHoverName().getString() + "."), true);
            return 1;
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Invalid generated-equipment request: " + exception.getMessage()));
            return 0;
        }
    }

    private static int giveBooks(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, String rarityName, int count) {
        try {
            var rarity = CosmicEnchantmentTier.valueOf(rarityName.toUpperCase(Locale.ROOT));
            var generator = new RewardGeneratorService();
            var generated = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
            var context = new RewardGenerationContext(source.registryAccess(), player.getRandom(), player);
            for (int index = 0; index < count; index++) {
                generator.generate(new com.cosmicpve.content.definition.reward.RewardDescriptor.CosmicBook(rarity), context)
                        .ifPresent(generated::add);
            }
            if (generated.size() != count) {
                source.sendFailure(Component.literal("No implemented enchantment exists for rarity " + rarityName));
                return 0;
            }
            DELIVERY.deliver(player, generated);
            source.sendSuccess(() -> Component.literal("Generated " + count + " random " + rarityName
                    + " Cosmic book(s)."), true);
            return count;
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Unknown Cosmic rarity: " + rarityName));
            return 0;
        }
    }
}
