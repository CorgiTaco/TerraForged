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

package com.terraforged.mod.feature.sapling;

import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.featuremanager.template.template.Template;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;

public class SaplingListener {

    private static final BlockPos[] CENTER = {BlockPos.ORIGIN};

    private static final Vec3i[][] DIRECTIONS = {
            {new Vec3i(0, 0, 1), new Vec3i(1, 0, 1), new Vec3i(1, 0, 0)},
            {new Vec3i(1, 0, 0), new Vec3i(1, 0, -1), new Vec3i(0, 0, -1)},
            {new Vec3i(0, 0, -1), new Vec3i(-1, 0, -1), new Vec3i(-1, 0, 0)},
            {new Vec3i(-1, 0, 0), new Vec3i(-1, 0, 1), new Vec3i(0, 0, 1)},
    };

//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void onTreeGrow(SaplingGrowTreeEvent event) {
//        Optional<BlockDataManager> dataManager = getDataManger(event);
//        if (!dataManager.isPresent()) {
//            return;
//        }
//
//        StructureWorldAccess world = (StructureWorldAccess) event.getWorld();
//        Block block = world.getBlockState(event.getPos()).getBlock();
//        Optional<SaplingConfig> saplingConfig = dataManager.get().getConfig(block, SaplingConfig.class);
//        if (!saplingConfig.isPresent()) {
//            return;
//        }
//
//        BlockMirror mirror = TemplateFeature.nextMirror(event.getRand());
//        BlockRotation rotation = TemplateFeature.nextRotation(event.getRand());
//
//        // if part of a 2x2 grid get the min corner of it
//        BlockPos pos = getMinPos(world, block, event.getPos(), saplingConfig.get().hasGiant());
//        Vec3i[] directions = getNeighbours(world, block, pos, saplingConfig.get().hasGiant());
//        TemplateFeatureConfig feature = saplingConfig.get().next(event.getRand(), directions.length == 3);
//        if (feature == null) {
//            return;
//        }
//
//
//        // translate the pos so that the mirrored/rotated 2x2 grid aligns correctly
//        Vec3i translation = getTranslation(directions, mirror, rotation);
//        BlockPos origin = pos.subtract(translation);
//
//        // prevent vanilla tree growing
//        event.setResult(Event.Result.DENY);
//
//        // attempt to paste the tree & then clear up any remaining saplings
//        if (TemplateFeature.paste(world, event.getRand(), origin, mirror, rotation, feature, feature.decorator, Template.CHECKED)) {
//            for (Vec3i dir : directions) {
//                BlockPos neighbour = origin.add(dir);
//                BlockState state = world.getBlockState(neighbour);
//                if (state.getBlock() == block) {
//                    world.breakBlock(neighbour, false);
//                }
//            }
//        }
//    }
//
//    private static Optional<BlockDataManager> getDataManger(SaplingGrowTreeEvent event) {
//        // ignore if client
//        if (event.getWorld().isClient()) {
//            return Optional.empty();
//        }
//
//        // ignore other world types
//        if (!SaplingListener.isTerraGen(event.getWorld())) {
//            return Optional.empty();
//        }
//
//        // get the data manager from the world's chunk generator
//        if (event.getWorld() instanceof ServerWorld) {
//            ServerWorld serverWorld = (ServerWorld) event.getWorld();
//            ChunkGenerator generator = serverWorld.getChunkManager().chunkGenerator;
//            if (generator instanceof TFChunkGenerator) {
//                TerraContext context = ((TFChunkGenerator) generator).getContext();
//                if (context.terraSettings.miscellaneous.customBiomeFeatures) {
//                    return Optional.of(((TFChunkGenerator) generator).getBlockDataManager());
//                }
//            }
//        }
//        return Optional.empty();
//    }

    private static boolean isTerraGen(WorldAccess world) {
        if (world.getChunkManager() instanceof ServerChunkManager) {
            ServerChunkManager chunkProvider = (ServerChunkManager) world.getChunkManager();
            return chunkProvider.getChunkGenerator() instanceof TFChunkGenerator;
        }
        return false;
    }

    private static boolean isClearOverhead(WorldAccess world, BlockPos pos, Vec3i[] directions) {
        for (Vec3i dir : directions) {
            int x = pos.getX() + dir.getX();
            int z = pos.getZ() + dir.getZ();
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y > pos.getY()) {
                return false;
            }
        }
        return true;
    }

    private static BlockPos getMinPos(WorldAccess world, Block block, BlockPos pos, boolean checkNeighbours) {
        if (checkNeighbours) {
            for (Vec3i[] dirs : DIRECTIONS) {
                boolean match = true;
                for (Vec3i dir : dirs) {
                    BlockState state = world.getBlockState(pos.add(dir));
                    if (state.getBlock() != block) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    Vec3i min = getMin(dirs, BlockMirror.NONE, BlockRotation.NONE);
                    return pos.add(min);
                }
            }
        }
        return pos;
    }

    private static Vec3i getTranslation(Vec3i[] directions, BlockMirror mirror, BlockRotation rotation) {
        if (directions.length == 1 || mirror == BlockMirror.NONE && rotation == BlockRotation.NONE) {
            return Vec3i.ZERO;
        }
        return getMin(directions, mirror, rotation);
    }

    private static Vec3i getMin(Vec3i[] directions, BlockMirror mirror, BlockRotation rotation) {
        int minX = 0;
        int minZ = 0;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (Vec3i vec : directions) {
            BlockPos dir = Template.transform(pos.set(vec), mirror, rotation);
            minX = Math.min(dir.getX(), minX);
            minZ = Math.min(dir.getZ(), minZ);
        }
        return new Vec3i(minX, 0, minZ);
    }

    private static Vec3i[] getNeighbours(WorldAccess world, Block block, BlockPos pos, boolean checkNeighbours) {
        if (checkNeighbours) {
            for (Vec3i[] dirs : DIRECTIONS) {
                boolean match = true;
                for (Vec3i dir : dirs) {
                    BlockState state = world.getBlockState(pos.add(dir));
                    if (state.getBlock() != block) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return dirs;
                }
            }
        }
        return CENTER;
    }
}