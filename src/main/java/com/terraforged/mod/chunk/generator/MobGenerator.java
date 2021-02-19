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

import com.terraforged.mod.chunk.TFChunkGenerator;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.GameRules;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.StructureFeature;
//import net.minecraftforge.common.world.StructureSpawnManager;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;

import java.util.List;

public class MobGenerator implements Generator.Mobs {

    // may be accessed cross-thread
    private static volatile boolean mobSpawning = true;

    private final CatSpawner catSpawner = new CatSpawner();
    private final PillagerSpawner patrolSpawner = new PillagerSpawner();
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final TFChunkGenerator generator;

    public MobGenerator(TFChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public final void generateMobs(ChunkRegion region) {
        // vanilla does NOT check the mobSpawning gamerule before calling this
        if (MobGenerator.mobSpawning) {
            int chunkX = region.getCenterChunkX();
            int chunkZ = region.getCenterChunkZ();
            Biome biome = region.getChunk(chunkX, chunkZ).getBiomeArray().getBiomeForNoiseGen(0, 0, 0);
            ChunkRandom sharedseedrandom = new ChunkRandom();
            sharedseedrandom.setPopulationSeed(region.getSeed(), chunkX << 4, chunkZ << 4);
            SpawnHelper.populateEntities(region, biome, chunkX, chunkZ, sharedseedrandom);
        }
    }

    @Override
    public final void tickSpawners(ServerWorld world, boolean hostile, boolean peaceful) {
        phantomSpawner.spawn(world, hostile, peaceful);
        patrolSpawner.spawn(world, hostile, peaceful);
        catSpawner.spawn(world, hostile, peaceful);
    }

    @Override
    public List<SpawnSettings.SpawnEntry> getSpawns(Biome biome, StructureAccessor structures, SpawnGroup type, BlockPos pos) {
        List<SpawnSettings.SpawnEntry> spawns = null;
        if (spawns != null) {
            return spawns;
        }

        if (structures.getStructureAt(pos, true, StructureFeature.SWAMP_HUT).hasChildren()) {
            if (type == SpawnGroup.MONSTER) {
                return StructureFeature.SWAMP_HUT.getMonsterSpawns();
            }

            if (type == SpawnGroup.CREATURE) {
                return StructureFeature.SWAMP_HUT.getCreatureSpawns();
            }
        }

        if (type == SpawnGroup.MONSTER) {
            if (structures.getStructureAt(pos, false, StructureFeature.PILLAGER_OUTPOST).hasChildren()) {
                return StructureFeature.PILLAGER_OUTPOST.getMonsterSpawns();
            }

            if (structures.getStructureAt(pos, false, StructureFeature.MONUMENT).hasChildren()) {
                return StructureFeature.MONUMENT.getMonsterSpawns();
            }

            if (structures.getStructureAt(pos, true, StructureFeature.FORTRESS).hasChildren()) {
                return StructureFeature.FORTRESS.getMonsterSpawns();
            }
        }

        return biome.getSpawnSettings().getSpawnEntry(type);
    }
}
