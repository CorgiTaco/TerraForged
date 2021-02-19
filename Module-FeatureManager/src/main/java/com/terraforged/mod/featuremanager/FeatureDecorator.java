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

package com.terraforged.mod.featuremanager;

import com.terraforged.mod.featuremanager.biome.BiomeFeature;
import com.terraforged.mod.featuremanager.biome.BiomeFeatures;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import java.util.List;

public interface FeatureDecorator {

    int FEATURE_STAGES = GenerationStep.Feature.values().length;

    FeatureManager getFeatureManager();

    default void decorate(ChunkGenerator generator, StructureAccessor manager, StructureWorldAccess region, Chunk chunk, Biome biome, BlockPos pos) {
        ChunkRandom random = new ChunkRandom();
        long decorationSeed = random.setPopulationSeed(region.getSeed(), pos.getX(), pos.getZ());

        BiomeFeatures biomeFeatures = getFeatureManager().getFeatures(biome);
        List<List<BiomeFeature>> stagedFeatures = biomeFeatures.getFeatures();
        List<List<StructureFeature<?>>> stagedStructures = biomeFeatures.getStructures();

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        BlockBox chunkBounds = new BlockBox(startX, startZ, startX + 15, startZ + 15);

        for (int stageIndex = 0; stageIndex < FEATURE_STAGES; stageIndex++) {
            int featureSeed = 0;

            if (stageIndex < stagedStructures.size()) {
                List<StructureFeature<?>> structures = stagedStructures.get(stageIndex);
                for (int structureIndex = 0; structureIndex < structures.size(); structureIndex++) {
                    StructureFeature<?> structure = structures.get(structureIndex);
                    random.setDecoratorSeed(decorationSeed, featureSeed++, stageIndex);
                    try {
                        manager.getStructuresWithChildren(ChunkSectionPos.from(pos), structure).forEach(start -> start.generateStructure(
                                region,
                                manager,
                                generator,
                                random,
                                chunkBounds,
                                chunkPos
                        ));
                    } catch (Throwable t) {
                        handle("structure", structure.getName(), t);
                    }
                }
            }

            if (stageIndex < stagedFeatures.size()) {
                List<BiomeFeature> features = stagedFeatures.get(stageIndex);
                for (int featureIndex = 0; featureIndex < features.size(); featureIndex++) {
                    BiomeFeature feature = features.get(featureIndex);
                    random.setDecoratorSeed(decorationSeed, featureSeed++, stageIndex);
                    if (feature.getPredicate().test(chunk, biome)) {
                        try {
                            feature.getFeature().generate(region, generator, random, pos);
                        } catch (Throwable t) {
                            handle("feature", feature.getIdentity().getComponents(), t);
                        }
                    }
                }
            }
        }
    }

    static void handle(String type, String identity, Throwable t) {
        FeatureManager.LOG.fatal("Fatal error placing {} '{}'", type, identity);
        t.printStackTrace();
    }
}
