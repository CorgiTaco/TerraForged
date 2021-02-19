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

package com.terraforged.mod.chunk.generator;

import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.mod.api.chunk.column.ColumnDecorator;
import com.terraforged.mod.api.chunk.column.DecoratorContext;
import com.terraforged.mod.biome.TFBiomeContainer;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.fix.RegionFix;
import com.terraforged.mod.chunk.util.ChunkRegionBoundingBox;
import com.terraforged.mod.featuremanager.biome.BiomeFeature;
import com.terraforged.mod.featuremanager.biome.BiomeFeatures;
import com.terraforged.mod.profiler.watchdog.UncheckedException;
import com.terraforged.mod.profiler.watchdog.WarnTimer;
import com.terraforged.mod.profiler.watchdog.Watchdog;
import com.terraforged.mod.profiler.watchdog.WatchdogContext;
import com.terraforged.mod.util.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.List;

public class FeatureGenerator implements Generator.Features {

    private static final int FEATURE_STAGES = GenerationStep.Feature.values().length;
    private static final String STRUCTURE = "Structure";
    private static final String FEATURE = "Feature";

    private final long hangTime;
    private final TFChunkGenerator generator;

    public FeatureGenerator(TFChunkGenerator generator) {
        this.generator = generator;
        this.hangTime = Watchdog.getWatchdogHangTime();
    }

    @Override
    public final void generateFeatures(ChunkRegion region, StructureAccessor manager) {
        int chunkX = region.getCenterChunkX();
        int chunkZ = region.getCenterChunkZ();
        Chunk chunk = region.getChunk(chunkX, chunkZ);

        ChunkReader reader = generator.getChunkReader(chunkX, chunkZ);
        TFBiomeContainer container = TFBiomeContainer.getOrCreate(chunk, reader, generator.getBiomeSource());

        // de-hardcode sea-level
        RegionFix regionFix = new RegionFix(region, generator);

        Biome biome = container.getFeatureBiome();
        try (DecoratorContext context = generator.getContext().decorator(chunk)) {
            BlockPos pos = new BlockPos(context.blockX, 0, context.blockZ);

            // place biome features
            decorate(manager, regionFix, chunk, biome, pos);

            // run post processes on chunk
            postProcess(reader, container, context);

            // bake biome array
            ((ProtoChunk) chunk).setBiomes(container.bakeBiomes(Environment.isVanillaBiomes(), generator.getContext().biomeContext));

            // close the current chunk reader
            reader.close();

            // mark chunk disposed as this is the last usage of the reader
            reader.dispose();
        }
    }

    private void decorate(StructureAccessor manager, StructureWorldAccess region, Chunk chunk, Biome biome, BlockPos pos) {
        try (WatchdogContext context = Watchdog.punchIn(chunk, generator, hangTime)) {
            decorate(manager, region, chunk, biome, pos, context);
        }
    }

    private void decorate(StructureAccessor manager, StructureWorldAccess region, Chunk chunk, Biome biome, BlockPos pos, WatchdogContext context) {
        final ChunkRandom random = new ChunkRandom();
        final long decorationSeed = random.setPopulationSeed(region.getSeed(), pos.getX(), pos.getZ());

        final BiomeFeatures biomeFeatures = generator.getFeatureManager().getFeatures(biome);
        final List<List<BiomeFeature>> stagedFeatures = biomeFeatures.getFeatures();
        final List<List<StructureFeature<?>>> stagedStructures = biomeFeatures.getStructures();

        final WarnTimer timer = Watchdog.getWarnTimer();

        final int chunkX = pos.getX() >> 4;
        final int chunkZ = pos.getZ() >> 4;
        final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        final ChunkRegionBoundingBox chunkBounds = new ChunkRegionBoundingBox(chunkX, chunkZ, 1);

        for (int stageIndex = 0; stageIndex < FEATURE_STAGES; stageIndex++) {
            int featureSeed = 0;

            if (stageIndex < stagedStructures.size()) {
                context.pushPhase(STRUCTURE);
                List<StructureFeature<?>> structures = stagedStructures.get(stageIndex);
                for (int structureIndex = 0; structureIndex < structures.size(); structureIndex++) {
                    StructureFeature<?> structure = structures.get(structureIndex);
                    random.setDecoratorSeed(decorationSeed, featureSeed++, stageIndex);
                    try {
                        long timeStamp = timer.now();
                        context.pushIdentifier(structure.getName(), timeStamp);
                        manager.getStructuresWithChildren(ChunkSectionPos.from(pos), structure).forEach(start -> start.generateStructure(
                                region,
                                manager,
                                generator,
                                random,
                                chunkBounds.init(start),
                                chunkPos
                        ));
                        Generator.checkTime(STRUCTURE, structure.getName(), timer, timeStamp, context);
                    } catch (Throwable t) {
                        throw new UncheckedException(STRUCTURE, structure.getName(), t);
                    }
                }
            }

            if (stageIndex < stagedFeatures.size()) {
                context.pushPhase(FEATURE);
                List<BiomeFeature> features = stagedFeatures.get(stageIndex);
                for (int featureIndex = 0; featureIndex < features.size(); featureIndex++) {
                    BiomeFeature feature = features.get(featureIndex);
                    random.setDecoratorSeed(decorationSeed, featureSeed++, stageIndex);

                    if (!feature.getPredicate().test(chunk, biome)) {
                        continue;
                    }

                    try {
                        long timeStamp = timer.now();
                        context.pushIdentifier(feature.getIdentity(), timeStamp);
                        feature.getFeature().generate(region, generator, random, pos);
                        Generator.checkTime(FEATURE, feature.getIdentity(), timer, timeStamp, context);
                    } catch (Throwable t) {
                        throw new UncheckedException(FEATURE, feature.getIdentity(), t);
                    }
                }
            }
        }
    }

    private void postProcess(ChunkReader reader, TFBiomeContainer container, DecoratorContext context) {
        List<ColumnDecorator> decorators = generator.getPostProcessors();
        reader.iterate(context, (cell, dx, dz, ctx) -> {
            int px = ctx.blockX + dx;
            int pz = ctx.blockZ + dz;
            int py = ctx.chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, dx, dz);
            ctx.cell = cell;
            ctx.biome = container.getBiome(dx, dz);
            for (ColumnDecorator decorator : decorators) {
                decorator.decorate(ctx.chunk, ctx, px, py, pz);
            }
        });
    }
}
