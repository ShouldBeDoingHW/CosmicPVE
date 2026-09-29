package com.cosmicpve.command;

import com.cosmicpve.CosmicPVE;
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
import com.cosmicpve.data.component.MysterySpawnerTier;
import com.cosmicpve.reward.spawner.MysterySpawners;

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
        var secretCache = Commands.literal("secret-weapon-cache")
                .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> giveLootbox(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                com.cosmicpve.registry.ModItems.SECRET_WEAPON_CACHE.get(), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                .executes(context -> giveLootbox(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                        com.cosmicpve.registry.ModItems.SECRET_WEAPON_CACHE.get(),
                                        IntegerArgumentType.getInteger(context, "count"))))))
                .then(Commands.literal("force").then(Commands.argument("weapon", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                com.cosmicpve.reward.lootbox.SignatureWeaponDefinition.ALL.stream()
                                        .map(value -> value.id().getPath()), builder))
                        .executes(context -> forceSignature(context.getSource(),
                                StringArgumentType.getString(context, "weapon")))));
        var cosmicTable = Commands.literal("cosmic-enchantment-table")
                .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> giveLootbox(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                com.cosmicpve.registry.ModItems.COSMIC_ENCHANTMENT_TABLE.get(), 1))))
                .then(Commands.literal("force").then(Commands.argument("enchantment", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                com.cosmicpve.reward.lootbox.CosmicEnchantmentTableRewards.POOL.stream()
                                        .map(value -> value.identifier().getPath()), builder))
                        .then(Commands.argument("success", IntegerArgumentType.integer(1, 100))
                                .executes(context -> forceTable(context.getSource(),
                                        StringArgumentType.getString(context, "enchantment"),
                                        IntegerArgumentType.getInteger(context, "success"))))));
        var adminAbuse = Commands.literal("admin-abuse")
                .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> giveLootbox(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                com.cosmicpve.registry.ModItems.ADMIN_ABUSE.get(), 1))))
                .then(Commands.literal("force").then(Commands.argument("outcome", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                com.cosmicpve.reward.lootbox.AdminAbuseRewards.ALL.stream()
                                        .map(com.cosmicpve.reward.lootbox.AdminAbuseRewards.Outcome::serializedName), builder))
                        .executes(context -> forceAdmin(context.getSource(),
                                StringArgumentType.getString(context, "outcome")))));
        var heroicTable = Commands.literal("heroic-cosmic-enchantment-table")
                .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> giveLootbox(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                com.cosmicpve.registry.ModItems.HEROIC_COSMIC_ENCHANTMENT_TABLE.get(), 1))))
                .then(Commands.literal("force").then(Commands.argument("enchantment", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                com.cosmicpve.reward.lootbox.HeroicCosmicEnchantmentTableRewards.POOL.stream()
                                        .map(Identifier::getPath), builder))
                        .then(Commands.argument("success", IntegerArgumentType.integer(1, 100))
                                .executes(context -> forceHeroicTable(context.getSource(),
                                        StringArgumentType.getString(context, "enchantment"),
                                        IntegerArgumentType.getInteger(context, "success"))))));
        var equipmentSample = Commands.literal("sample")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        List.of("ultimate", "legendary", "mastery"), builder))
                                .then(Commands.argument("category", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                List.of("armor", "weapon"), builder))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 8))
                                                .executes(context -> sampleEquipment(context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        StringArgumentType.getString(context, "tier"),
                                                        StringArgumentType.getString(context, "category"),
                                                        IntegerArgumentType.getInteger(context, "count")))))));
        return Commands.literal("reward")
                .then(Commands.literal("list").executes(context -> list(context.getSource())))
                .then(Commands.literal("inspect").then(tableId.executes(context -> inspect(
                        context.getSource(), IdentifierArgument.getId(context, "table")))))
                .then(Commands.literal("reload").executes(context -> reload(context.getSource())))
                .then(Commands.literal("animation-demo").executes(context -> animationDemo(context.getSource())))
                .then(Commands.literal("space-dust-bundle").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> giveLootbox(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                        com.cosmicpve.registry.ModItems.SPACE_DUST_BUNDLE.get(), 1)))))
                .then(Commands.literal("holy-white-scroll").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> giveLootbox(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                        com.cosmicpve.registry.ModItems.HOLY_WHITE_SCROLL.get(), 1)))))
                .then(secretCache)
                .then(cosmicTable)
                .then(heroicTable)
                .then(adminAbuse)
                .then(Commands.literal("roll").then(Commands.argument("table", IdentifierArgument.id())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                CosmicContent.repository().snapshot().rewardTables().keySet(), builder))
                        .executes(context -> roll(context.getSource(), IdentifierArgument.getId(context, "table"), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                .executes(context -> roll(context.getSource(),
                                        IdentifierArgument.getId(context, "table"),
                                        IntegerArgumentType.getInteger(context, "count"))))))
                .then(spawner)
                .then(Commands.literal("mystery-spawner").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("tier", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                List.of("simple", "elite", "mastery"), builder))
                                        .executes(context -> giveMysterySpawner(context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "tier"), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> giveMysterySpawner(context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        StringArgumentType.getString(context, "tier"),
                                                        IntegerArgumentType.getInteger(context, "count"))))))))
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
                .then(Commands.literal("equipment").then(equipmentSample).then(Commands.literal("give")
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

    private static int giveMysterySpawner(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, String tierName, int count) {
        MysterySpawnerTier tier;
        try { tier = MysterySpawnerTier.valueOf(tierName.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { source.sendFailure(Component.literal("Unknown Mystery Spawner tier.")); return 0; }
        DELIVERY.deliver(player, List.of(MysterySpawners.create(tier, count)));
        source.sendSuccess(() -> Component.literal("Gave " + count + " Mystery " + tier + " Spawner(s)."), true);
        return count;
    }

    private static int list(net.minecraft.commands.CommandSourceStack source) {
        var tables = CosmicContent.repository().snapshot().rewardTables().keySet();
        source.sendSuccess(() -> Component.literal("Cosmic reward tables (" + tables.size() + "): " + tables), false);
        return tables.size();
    }

    private static int animationDemo(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var candidates = List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BRICK),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FEATHER),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.AMETHYST_SHARD),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COPPER_INGOT));
        var finalReward = candidates.get(player.getRandom().nextInt(candidates.size())).copy();
        boolean opened = com.cosmicpve.reward.animation.SingleRewardAnimationService.INSTANCE.open(player, finalReward,
                com.cosmicpve.reward.animation.LootAnimationPreviewProvider.uniform(candidates), () -> {});
        if (!opened) { source.sendFailure(Component.literal("A loot animation is already active.")); return 0; }
        return 1;
    }

    private static int giveLootbox(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        var stacks = new java.util.ArrayList<net.minecraft.world.item.ItemStack>(count);
        for (int index = 0; index < count; index++) stacks.add(new net.minecraft.world.item.ItemStack(item));
        DELIVERY.deliver(player, stacks);
        source.sendSuccess(() -> Component.literal("Gave " + count + " " + item.getName(new net.minecraft.world.item.ItemStack(item)).getString() + "."), true);
        return count;
    }

    private static int forceSignature(net.minecraft.commands.CommandSourceStack source, String name)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var definition = com.cosmicpve.reward.lootbox.SignatureWeaponDefinition.ALL.stream()
                .filter(value -> value.id().getPath().equals(name)).findFirst();
        if (definition.isEmpty()) { source.sendFailure(Component.literal("Unknown signature weapon.")); return 0; }
        return com.cosmicpve.reward.lootbox.Step8ELootboxService.INSTANCE.forceSignature(
                source.getPlayerOrException(), definition.orElseThrow()) ? 1 : 0;
    }

    private static int forceTable(net.minecraft.commands.CommandSourceStack source, String name, int success)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var key = com.cosmicpve.reward.lootbox.CosmicEnchantmentTableRewards.POOL.stream()
                .filter(value -> value.identifier().getPath().equals(name)).findFirst();
        if (key.isEmpty()) { source.sendFailure(Component.literal("Unknown Cosmic Enchantment Table entry.")); return 0; }
        try {
            return com.cosmicpve.reward.lootbox.Step8ELootboxService.INSTANCE.forceTable(
                    source.getPlayerOrException(), key.orElseThrow(), success) ? 1 : 0;
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal(exception.getMessage())); return 0;
        }
    }

    private static int forceAdmin(net.minecraft.commands.CommandSourceStack source, String name)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var outcome = com.cosmicpve.reward.lootbox.AdminAbuseRewards.find(name);
        if (outcome.isEmpty()) { source.sendFailure(Component.literal("Unknown Admin Abuse outcome.")); return 0; }
        return com.cosmicpve.reward.lootbox.Step8ELootboxService.INSTANCE.forceAdmin(
                source.getPlayerOrException(), outcome.orElseThrow()) ? 1 : 0;
    }

    private static int forceHeroicTable(net.minecraft.commands.CommandSourceStack source, String name, int success)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var id = com.cosmicpve.reward.lootbox.HeroicCosmicEnchantmentTableRewards.POOL.stream()
                .filter(value -> value.getPath().equals(name)).findFirst();
        if (id.isEmpty()) { source.sendFailure(Component.literal("Unknown Heroic Table entry.")); return 0; }
        try {
            return com.cosmicpve.reward.lootbox.Step8ELootboxService.INSTANCE.forceHeroicTable(
                    source.getPlayerOrException(), id.orElseThrow(), success) ? 1 : 0;
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal(exception.getMessage())); return 0;
        }
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

    private static int sampleEquipment(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, String tierName, String categoryName, int count) {
        if (!List.of("ultimate", "legendary", "mastery").contains(tierName)
                || !List.of("armor", "weapon").contains(categoryName)) {
            source.sendFailure(Component.literal("Use ultimate/legendary/mastery and armor/weapon."));
            return 0;
        }
        var table = CosmicContent.repository().requireRewardTable(CosmicPVE.id("space_chest/" + tierName));
        var category = categoryName.equals("armor") ? GeneratedEquipmentCategory.RANDOM_IRON_ARMOR_PIECE
                : GeneratedEquipmentCategory.RANDOM_WEAPON;
        var matches = table.entries().stream().map(entry -> entry.reward())
                .filter(com.cosmicpve.content.definition.reward.RewardDescriptor.GeneratedEquipment.class::isInstance)
                .map(com.cosmicpve.content.definition.reward.RewardDescriptor.GeneratedEquipment.class::cast)
                .filter(reward -> reward.definition().category() == category).toList();
        if (matches.size() != 1) {
            source.sendFailure(Component.literal("Expected exactly one generated equipment row for this category."));
            return 0;
        }
        var definition = matches.getFirst().definition();
        var service = new GeneratedEquipmentService();
        var results = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
        try {
            for (int index = 0; index < count; index++) results.add(service.generate(definition,
                    source.registryAccess().lookupOrThrow(Registries.ENCHANTMENT), player.getRandom()));
        } catch (RuntimeException failure) {
            source.sendFailure(Component.literal("Sample generation failed: " + failure.getMessage()));
            return 0;
        }
        DELIVERY.deliver(player, results);
        source.sendSuccess(() -> Component.literal("Delivered " + count + " " + tierName + " " + categoryName
                + " samples from the production generator."), false);
        return count;
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
