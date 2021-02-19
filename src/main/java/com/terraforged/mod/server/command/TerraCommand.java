/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.world.WorldGenerator;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.settings.preset.Preset;
import com.terraforged.mod.chunk.settings.preset.PresetManager;
import com.terraforged.mod.data.DataGen;
import com.terraforged.mod.mixin.access.ThreadExecutorAccess;
import com.terraforged.mod.profiler.Profiler;
import com.terraforged.mod.server.command.arg.TerrainArgType;
import com.terraforged.mod.server.command.search.BiomeSearchTask;
import com.terraforged.mod.server.command.search.BothSearchTask;
import com.terraforged.mod.server.command.search.Search;
import com.terraforged.mod.server.command.search.TerrainSearchTask;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.HorizontalVoronoiBiomeAccessType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class TerraCommand {
    private static final Formatting TITLE_FORMAT = Formatting.ITALIC;
    private static final Formatting SECONDARY_FORMAT = Formatting.YELLOW;
    private static final Formatting PREFIX_FORMAT = Formatting.GOLD;
    private static final Map<UUID, Integer> SEARCH_IDS = Collections.synchronizedMap(new HashMap<>());
    private static final BiFunction<UUID, Integer, Integer> INCREMENTER = (k, v) -> v == null ? 0 : v + 1;

    public static void init() {
        ArgumentTypes.register("terraforged:terrain", TerrainArgType.class, new ConstantArgumentSerializer<>(TerrainArgType::new));
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((source, dedicated) -> {
            register(source);

        });

        Log.info("Registering /terra command");
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(command());
    }

    private static LiteralArgumentBuilder<ServerCommandSource> command() {
        return CommandManager.literal("terra")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("benchmark")
                        .then(CommandManager.literal("reset")
                                .executes(TerraCommand::benchmarkStart))
                        .then(CommandManager.literal("stats")
                                .executes(TerraCommand::benchmarkStats)))
                .then(CommandManager.literal("query")
                        .executes(TerraCommand::query))
                .then(CommandManager.literal("data")
                        .then(CommandManager.literal("dump")
                                .executes(TerraCommand::dump)))
                .then(CommandManager.literal("preset")
                        .then(CommandManager.literal("save")
                                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(TerraCommand::savePreset))))
                .then(CommandManager.literal("debug")
                        .executes(TerraCommand::debugBiome))
                .then(CommandManager.literal("locate")
                        .then(CommandManager.literal("biome")
                                .then(CommandManager.argument("biome", IdentifierArgumentType.identifier())
                                        .suggests(SuggestionProviders.ALL_BIOMES)
                                        .executes(TerraCommand::findBiome)))
                        .then(CommandManager.literal("terrain")
                                .then(CommandManager.argument("terrain", TerrainArgType.terrain())
                                        .executes(TerraCommand::findTerrain)))
                        .then(CommandManager.literal("both")
                                .then(CommandManager.argument("biome", IdentifierArgumentType.identifier())
                                        .suggests(SuggestionProviders.ALL_BIOMES)
                                        .then(CommandManager.argument("terrain", TerrainArgType.terrain())
                                                .executes(TerraCommand::findTerrainAndBiome)))));
    }

    private static int query(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getContext(context);
        BlockPos pos = context.getSource().getPlayer().getBlockPos();
        TFBiomeProvider biomeProvider = getBiomeProvider(context);
        try (Resource<Cell> cell = biomeProvider.lookupPos(pos.getX(), pos.getZ())) {
            Biome biome = biomeProvider.getBiome(cell.get(), pos.getX(), pos.getZ());
            context.getSource().sendFeedback(new LiteralText("At ")
                    .append(createTeleportMessage(pos))
                    .append(new LiteralText(": TerrainType = "))
                    .append(createPrimary(cell.get().terrain.getName()))
                    .append(new LiteralText(", Biome = "))
                    .append(createPrimary(context.getSource().getWorld().getRegistryManager().get(Registry.BIOME_KEY).getId(biome)))
                    .append(new LiteralText(", BiomeType = "))
                    .append(createPrimary(cell.get().biome.name())), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int benchmarkStart(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Profiler.reset();
        context.getSource().sendFeedback(createText("Reset profiler"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int benchmarkStats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        long fastest = 0L;
        long slowest = 0L;
        double average = 0.0;

        for (Profiler profiler : Profiler.values()) {
            average += profiler.averageMS();
            slowest += profiler.maxMS();
            fastest += profiler.minMS();
            context.getSource().sendFeedback(profiler.toText(), false);
        }

        long min = fastest;
        long max = slowest;
        context.getSource().sendFeedback(createText("Chunk Average", PREFIX_FORMAT)
                .append(String.format(": %.3fms", average))
                .styled(style -> style.withHoverEvent(Profiler.createHoverStats(min, max))), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int dump(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getContext(context);

        try {
            context.getSource().sendFeedback(new LiteralText("Exporting data"), true);
            DataGen.dumpData();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int savePreset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context);
        String name = StringArgumentType.getString(context, "name");
        Preset preset = new Preset(name, terraContext.terraSettings);
        context.getSource().sendFeedback(new LiteralText("Saving preset: " + preset.getName()), true);

        PresetManager presets = PresetManager.load();
        presets.add(preset);
        presets.saveAll();

        return Command.SINGLE_SUCCESS;
    }

    private static int debugBiome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        BlockPos position = player.getBlockPos();
        int x = position.getX();
        int z = position.getZ();

        long seed = player.getServerWorld().getSeed();
        BiomeSource biomeProvider = player.getServerWorld().getChunkManager().getChunkGenerator().getBiomeSource();
        Biome actual = player.getServerWorld().getBiome(position);
        Biome biome = HorizontalVoronoiBiomeAccessType.INSTANCE.getBiome(seed, x, 0, z, biomeProvider);

        context.getSource().sendFeedback(new LiteralText("At ")
                .append(createTeleportMessage(position))
                .append(new LiteralText(": Actual Biome = "))
                .append(createPrimary(context.getSource().getWorld().getRegistryManager().get(Registry.BIOME_KEY).getId(actual)))
                .append(new LiteralText(", Lookup Biome = "))
                .append(createPrimary(context.getSource().getWorld().getRegistryManager().get(Registry.BIOME_KEY).getId(biome))), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int findTerrain(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context);
        Terrain type = TerrainArgType.getTerrain(context, "terrain");
        BlockPos pos = context.getSource().getPlayer().getBlockPos();
        UUID playerID = context.getSource().getPlayer().getUuid();
        MinecraftServer server = context.getSource().getMinecraftServer();
        WorldGenerator generator = terraContext.worldGenerator.get().get();
        Search search = new TerrainSearchTask(pos, type, getChunkGenerator(context), generator);
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendFeedback(createPrefix(identifier)
                .append(new LiteralText(" Searching for "))
                .append(createPrimary(type.getName()))
                .append(new LiteralText("...")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int findBiome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getContext(context);
        Biome biome = getBiome(context, "biome");
        BlockPos pos = context.getSource().getPlayer().getBlockPos();
        UUID playerID = context.getSource().getPlayer().getUuid();
        MinecraftServer server = context.getSource().getMinecraftServer();
        ServerWorld world = context.getSource().getPlayer().getServerWorld();
        Search search = new BiomeSearchTask(pos, biome, world.getChunkManager().getChunkGenerator(), getBiomeProvider(context));
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendFeedback(createPrefix(identifier)
                .append(new LiteralText(" Searching for "))
                .append(createPrimary(context.getSource().getWorld().getRegistryManager().get(Registry.BIOME_KEY).getId(biome)))
                .append(new LiteralText("...")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int findTerrainAndBiome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context);
        Terrain target = TerrainArgType.getTerrain(context, "terrain");
        Biome biome = getBiome(context, "biome");
        BlockPos pos = context.getSource().getPlayer().getBlockPos();
        UUID playerID = context.getSource().getPlayer().getUuid();
        MinecraftServer server = context.getSource().getMinecraftServer();
        WorldGenerator generator = terraContext.worldGenerator.get().get();
        Search biomeSearch = new BiomeSearchTask(pos, biome, getChunkGenerator(context), getBiomeProvider(context));
        Search terrainSearch = new TerrainSearchTask(pos, target, getChunkGenerator(context), generator);
        Search search = new BothSearchTask(pos, biomeSearch, terrainSearch);
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendFeedback(createPrefix(identifier)
                .append(new LiteralText(" Searching for "))
                .append(createPrimary(context.getSource().getWorld().getRegistryManager().get(Registry.BIOME_KEY).getId(biome)))
                .append(new LiteralText(" and "))
                .append(createPrimary(target.getName()))
                .append(new LiteralText("...")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int doSearch(MinecraftServer server, UUID userId, Supplier<BlockPos> supplier) {
        int identifier = SEARCH_IDS.compute(userId, INCREMENTER);
        CompletableFuture.supplyAsync(supplier).thenAccept(pos -> ((ThreadExecutorAccess) server).invokeSubmitAsync(() -> {
            PlayerEntity player = server.getPlayerManager().getPlayer(userId);
            if (player == null) {
                SEARCH_IDS.remove(userId);
                return;
            }

            if (pos == BlockPos.ORIGIN) {
                Text message = createPrefix(identifier).append(new LiteralText(" Location not found :["));
                player.sendSystemMessage(message, Util.NIL_UUID);
                return;
            }

            double distance = Math.sqrt(player.getBlockPos().getSquaredDistance(pos));
            Text result = createPrefix(identifier)
                    .append(new LiteralText(" Nearest match: "))
                    .append(createTeleportMessage(pos))
                    .append(new LiteralText(String.format(" Distance: %.2f", distance)));

            player.sendSystemMessage(result, Util.NIL_UUID);
        }));
        return identifier;
    }

    private static TerraContext getContext(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ChunkGenerator generator = context.getSource().getWorld().getChunkManager().getChunkGenerator();
        if (generator instanceof TFChunkGenerator) {
            TFChunkGenerator gen = (TFChunkGenerator) generator;
            return gen.getContext();
        }
        throw createException("Invalid world type", "This command can only be run in a TerraForged world!");
    }

    private static Biome getBiome(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier location = IdentifierArgumentType.getIdentifier(context, name);
        return context.getSource().getMinecraftServer().getRegistryManager().getOptional(Registry.BIOME_KEY)
                .flatMap(registry -> registry.getOrEmpty(location))
                .orElseThrow(() -> createException("biome", "Unrecognized biome %s", location));
    }

    private static String getBiomeName(CommandContext<ServerCommandSource> context, Biome biome) {
        return context.getSource().getMinecraftServer().getRegistryManager()
                .getOptional(Registry.BIOME_KEY)
                .map(r -> r.getId(biome))
                .map(Objects::toString)
                .orElse("unknown");
    }

    private static ChunkGenerator getChunkGenerator(CommandContext<ServerCommandSource> context) {
        return context.getSource().getWorld().getChunkManager().getChunkGenerator();
    }

    private static TFBiomeProvider getBiomeProvider(CommandContext<ServerCommandSource> context) {
        return (TFBiomeProvider) context.getSource().getWorld().getChunkManager().getChunkGenerator().getBiomeSource();
    }

    private static CommandSyntaxException createException(String type, String message, Object... args) {
        return new CommandSyntaxException(
                new SimpleCommandExceptionType(new LiteralText(type)),
                new LiteralText(String.format(message, args))
        );
    }

    private static MutableText createTeleportMessage(BlockPos pos) {
        return Texts.bracketed(new TranslatableText(
                "chat.coordinates", pos.getX(), pos.getY(), pos.getZ()
        )).styled(s -> s.withFormatting(Formatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip")))
        );
    }

    private static MutableText createPrefix(int identifier) {
        return new LiteralText("")
                .append(Texts.bracketed(new LiteralText(String.format("%03d", identifier)))
                        .styled(style -> style.withFormatting(PREFIX_FORMAT)));
    }

    private static MutableText createPrimary(@Nullable Object name) {
        return createText(name, TITLE_FORMAT);
    }

    private static MutableText createText(@Nullable Object name, Formatting... formatting) {
        String title = name == null ? "null" : name.toString();
        return new LiteralText("").append(new LiteralText(title).styled(style -> {
            for (Formatting f : formatting) {
                style = style.withFormatting(f);
            }
            return style;
        }));
    }
}
