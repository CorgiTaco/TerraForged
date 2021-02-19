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

package com.terraforged.mod.feature.feature;

import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.biome.TFBiomeContainer;
import com.terraforged.mod.featuremanager.template.BlockUtils;
import com.terraforged.mod.util.Flags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import javax.annotation.Nullable;
import java.util.Random;

public class FreezeLayer extends Feature<DefaultFeatureConfig> {

    public static final FreezeLayer INSTANCE = new FreezeLayer();

    private static final int AIR_FLAG = Flags.OPTION_1;
    private static final int LOG_OR_LEAVES_FLAG = Flags.OPTION_2;

    private static final BiomeAccessor WORLD_BIOME_ACCESSOR = (world, container, pos) -> world.getBiome(pos);
    private static final BiomeAccessor CONTAINER_BIOME_ACCESSOR = (world, container, pos) -> container.getBiome(pos.getX(), pos.getZ());

    public FreezeLayer() {
        super(DefaultFeatureConfig.CODEC);
        Registry.register(Registry.FEATURE, new Identifier(TerraForgedMod.MODID, "freeze_top_layer"), this);
    }

    @Override
    public boolean generate(StructureWorldAccess world, ChunkGenerator generator, Random rand, BlockPos pos, DefaultFeatureConfig config) {
        BlockPos.Mutable pos1 = new BlockPos.Mutable();
        BlockPos.Mutable pos2 = new BlockPos.Mutable();

        Chunk main = world.getChunk(pos);
        int mainChunkX = main.getPos().x;
        int mainChunkZ = main.getPos().z;

        int minX = (mainChunkX << 4) - 8;
        int minZ = (mainChunkZ << 4) - 8;
        int maxX = minX + 32;
        int maxZ = minZ + 32;

        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                int cx = mainChunkX + dx;
                int cz = mainChunkZ + dz;
                visitChunk(world, cx, cz, minX, minZ, maxX, maxZ, pos1, pos2, dx == 0 && dz == 0);
            }
        }

        return true;
    }

    private void visitChunk(WorldAccess world, int chunkX, int chunkZ, int areaMinX, int areaMinZ, int areaMaxX, int areaMaxZ, BlockPos.Mutable pos1, BlockPos.Mutable pos2, boolean main) {
        final Chunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.BIOMES, false);
        if (chunk == null) {
            return;
        }

        // skip if features haven't generated features yet and we're not visiting the main chunk
        if (!main && !chunk.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
            return;
        }

        final int chunkBlockX = chunkX << 4;
        final int chunkBlockZ = chunkZ << 4;
        final int minX = Math.max(areaMinX, chunkBlockX);
        final int minZ = Math.max(areaMinZ, chunkBlockZ);
        final int maxX = Math.min(areaMaxX, chunkBlockX + 15);
        final int maxZ = Math.min(areaMaxZ, chunkBlockZ + 15);
        final TFBiomeContainer biomes = getBiomeContainer(chunk);
        final BiomeAccessor biomeAccessor = getBiomeAccesor(biomes);
        final Heightmap leavesHeightmap = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING);
        final Heightmap groundHeightmap = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);

        for (int z = minZ; z <= maxZ; z++) {
            int dz = z & 15;
            for (int x = minX; x <= maxX; x++) {
                int dx = x & 15;
                int leavesY = leavesHeightmap.get(dx, dz);
                int groundY = groundHeightmap.get(dx, dz);

                pos1.set(x, leavesY, z);
                pos2.set(pos1).move(Direction.DOWN, 1);
                Biome biome = biomeAccessor.getBiome(world, biomes, pos1);

                if (leavesY > groundY) {
                    freezeLeaves(world, chunk, biome, pos1, pos2);
                }

                if (main) {
                    pos1.set(x, groundY, z);
                    pos2.set(pos1).move(Direction.DOWN, 1);
                    freezeGround(world, chunk, biome, pos1, pos2);
                }
            }
        }
    }

    // Chunk::getBlockState masks coords so need to relativize
    private void freezeLeaves(WorldAccess world, Chunk chunk, Biome biome, BlockPos.Mutable pos, BlockPos.Mutable below) {
        if (biome.canSetSnow(world, pos)) {
            BlockState stateUnder = chunk.getBlockState(below);
            if (stateUnder.getBlock() == Blocks.AIR) {
                return;
            }
            setSnow(chunk, pos, below, stateUnder);
        }
    }

    // Chunk::getBlockState & Chunk::setBlockState masks coords so need to relativize
    private void freezeGround(WorldAccess world, Chunk chunk, Biome biome, BlockPos.Mutable snowPos, BlockPos.Mutable underPos) {
        if (biome.canSetIce(world, underPos, false)) {
            chunk.setBlockState(underPos, Blocks.ICE.getDefaultState(), false);
        }

        if (biome.canSetSnow(world, snowPos)) {
            BlockState stateUnder = chunk.getBlockState(underPos);
            if (BlockUtils.isAir(stateUnder, chunk, underPos)) {
                return;
            }

            // don't place on logs
            if (BlockTags.LOGS.contains(stateUnder.getBlock())) {
                return;
            }

            // don't place if log/leaves directly above
            int stateAbove = getAboveStateFlags(chunk, snowPos);
            if (Flags.has(stateAbove, LOG_OR_LEAVES_FLAG)) {
                return;
            }

            if (setSnow(chunk, snowPos, underPos, stateUnder)) {
                if (!Flags.has(stateAbove, AIR_FLAG)) {
                    chunk.setBlockState(snowPos.move(Direction.UP, 1), Blocks.AIR.getDefaultState(), false);
                }
            }
        }
    }

    private int getAboveStateFlags(Chunk chunk, BlockPos.Mutable pos) {
        pos.move(Direction.UP, 1);
        BlockState state = chunk.getBlockState(pos);
        boolean air = BlockUtils.isAir(state, chunk, pos);
        boolean tree = !air && BlockUtils.isLeavesOrLogs(state);
        pos.move(Direction.DOWN, 1);
        return Flags.get(air, tree);
    }

    // Chunk::setBlockState masks coords so need to relativize
    private boolean setSnow(Chunk chunk, BlockPos pos1, BlockPos pos2, BlockState below) {
        if (BlockUtils.isSolid(chunk, pos1)) {
            return false;
        }

        chunk.setBlockState(pos1, Blocks.SNOW.getDefaultState(), false);
        if (below.contains(SnowyBlock.SNOWY)) {
            chunk.setBlockState(pos2, below.with(SnowyBlock.SNOWY, true), false);
        }
        return true;
    }

    @Nullable
    private static TFBiomeContainer getBiomeContainer(Chunk chunk) {
        BiomeArray biomes = chunk.getBiomeArray();
        if (biomes instanceof TFBiomeContainer) {
            return (TFBiomeContainer) biomes;
        }
        return null;
    }

    private static BiomeAccessor getBiomeAccesor(@Nullable TFBiomeContainer biomes) {
        return biomes == null ? WORLD_BIOME_ACCESSOR : CONTAINER_BIOME_ACCESSOR;
    }

    private interface BiomeAccessor {

        Biome getBiome(WorldAccess world, TFBiomeContainer container, BlockPos pos);
    }
}
