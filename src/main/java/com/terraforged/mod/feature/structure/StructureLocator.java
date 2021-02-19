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

package com.terraforged.mod.feature.structure;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class StructureLocator {

    private static final int SEARCH_BATCH_SIZE = 100;

    public static BlockPos findStructure(TFChunkGenerator generator, WorldAccess world, StructureAccessor manager, StructureFeature<?> structure, BlockPos center, int attempts, boolean first, StructureConfig settings) {
        return findStructure(generator, world, manager, structure, center, attempts, first, settings, 5_000L);
    }

    // TODO: Consider splitting search area into concurrent search regions?
    public static BlockPos findStructure(TFChunkGenerator generator, WorldAccess world, StructureAccessor manager, StructureFeature<?> structure, BlockPos center, int radius, boolean first, StructureConfig settings, long timeout) {
        long seed = generator.getSeed();
        int separation = settings.getSpacing();
        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;

        ChunkRandom sharedseedrandom = new ChunkRandom();
        TFBiomeProvider biomeProvider = generator.getBiomeSource();

        int searchCount = 0;
        long searchTimeout = System.currentTimeMillis() + timeout;

        try (Resource<Cell> resource = Cell.pooled()) {
            Cell cell = resource.get();

            for (int dr = 0; dr <= radius; ++dr) {
                for (int dx = -dr; dx <= dr; ++dx) {
                    boolean flag = dx == -dr || dx == dr;

                    for (int dz = -dr; dz <= dr; ++dz) {
                        boolean flag1 = dz == -dr || dz == dr;
                        if (flag || flag1) {
                            int cx = chunkX + separation * dx;
                            int cz = chunkZ + separation * dz;

                            if (searchCount++ > SEARCH_BATCH_SIZE) {
                                searchCount = 0;
                                long now = System.currentTimeMillis();
                                if (now > searchTimeout) {
                                    Log.warn("Structure search took too long! {}", Registry.STRUCTURE_FEATURE.getId(structure));
                                    return null;
                                }
                            }

                            int x = cx << 4;
                            int z = cz << 4;
                            Biome biome = biomeProvider.fastLookupBiome(cell, x, z);
                            if (!biome.getGenerationSettings().hasStructureFeature(structure)) {
                                continue;
                            }

                            ChunkPos chunkpos = structure.getStartChunk(settings, seed, sharedseedrandom, cx, cz);
                            Chunk ichunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart<?> start = manager.getStructureStart(ChunkSectionPos.from(ichunk.getPos(), 0), structure, ichunk);
                            if (start != null && start.hasChildren()) {
                                if (first && start.isInExistingChunk()) {
                                    start.incrementReferences();
                                    return start.getPos();
                                }

                                if (!first) {
                                    return start.getPos();
                                }
                            }

                            if (dr == 0) {
                                break;
                            }
                        }
                    }

                    if (dr == 0) {
                        break;
                    }
                }
            }
        }
        return null;
    }
}
