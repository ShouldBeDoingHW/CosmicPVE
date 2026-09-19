package com.cosmicpve.adventure;

import com.cosmicpve.CosmicPVE;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class AdventureCommands {
    private AdventureCommands() {}
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("adventure")
                .then(Commands.literal("inspect").executes(c -> {
                    var p=c.getSource().getPlayerOrException();var s=DenseWoodlandsBootstrap.SESSIONS.session(p);
                    c.getSource().sendSuccess(()->Component.literal("session="+s+" localZombies="+AdventureZombies.count(p)),false);
                    var level = c.getSource().getServer().getLevel(DenseWoodlandsSessionService.DIMENSION);
                    if (level != null) {
                        var rules = level.getGameRules();
                        var state = level.getChunkSource().getLastSpawnState();
                        String diagnostics = "Woodlands spawning: difficulty=" + level.getDifficulty()
                                + " spawn_mobs=" + rules.get(net.minecraft.world.level.gamerules.GameRules.SPAWN_MOBS)
                                + " spawn_monsters=" + rules.get(net.minecraft.world.level.gamerules.GameRules.SPAWN_MONSTERS)
                                + " spawnableChunks=" + (state == null ? 0 : state.getSpawnableChunkCount())
                                + " counts=" + (state == null ? "not ticking" : state.getMobCategoryCounts())
                                + " playerDimension=" + p.level().dimension().identifier();
                        c.getSource().sendSuccess(() -> Component.literal(diagnostics), false);
                    }
                    return 1;
                }))
                .then(Commands.literal("features").executes(c -> {
                    c.getSource().sendSuccess(()->Component.literal(WoodlandTemplateFeature.counts().toString()),false);return 1;
                }))
                .then(Commands.literal("campsites").executes(c -> {
                    c.getSource().sendSuccess(()->Component.literal(WoodlandTemplateFeature.campsiteDiagnostics()+" "+WoodlandTemplateFeature.recentCamps().stream()
                            .filter(p->p.variant().contains("campsite")).toList().toString()),false);return 1;
                }))
                .then(Commands.literal("sample").then(Commands.argument("chunkX",IntegerArgumentType.integer(-6250,6250))
                        .then(Commands.argument("chunkZ",IntegerArgumentType.integer(-6250,6250))
                        .then(Commands.argument("width",IntegerArgumentType.integer(1,24)).executes(c -> {
                            var level=c.getSource().getServer().getLevel(DenseWoodlandsSessionService.DIMENSION);
                            var result=WoodlandProbe.sample(level,IntegerArgumentType.getInteger(c,"chunkX"),IntegerArgumentType.getInteger(c,"chunkZ"),IntegerArgumentType.getInteger(c,"width"));
                            CosmicPVE.LOGGER.info("WOODLANDS_PROBE {}",result);c.getSource().sendSuccess(()->Component.literal(result.toString()),false);return 1;
                        })))));
    }
}
