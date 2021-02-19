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

package com.terraforged.mod.featuremanager.template;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.TreeFeature;

import java.util.function.BiPredicate;

public class BlockUtils {

    public static boolean isAir(BlockView world, BlockPos pos) {
        return isAir(world.getBlockState(pos), world, pos);
    }

    public static boolean isAir(BlockState state, BlockView world, BlockPos pos) {
        return state.getBlock().getDefaultState().getMaterial() == Material.AIR;
    }

    public static boolean isSoil(WorldAccess world, BlockPos pos) {
        return TreeFeature.isSoil(world.getBlockState(pos).getBlock());
    }

    public static boolean isLeavesOrLogs(BlockState state) {
        return BlockTags.LOGS.contains(state.getBlock()) || BlockTags.LEAVES.contains(state.getBlock());
    }

    public static boolean isVegetation(WorldAccess world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isIn(BlockTags.SAPLINGS) || state.isIn(BlockTags.FLOWERS) || state.isOf(Blocks.VINE);
    }

    public static boolean canTreeReplace(WorldAccess world, BlockPos pos) {
        return TreeFeature.canReplace(world, pos) || isVegetation(world, pos);
    }

    public static boolean isSolid(BlockView reader, BlockPos pos) {
        BlockState state = reader.getBlockState(pos);
        return isSolid(state, reader, pos);
    }

    public static boolean isSolid(BlockState state, BlockView reader, BlockPos pos) {
        return state.isOpaque() || !state.canPathfindThrough(reader, pos, NavigationType.LAND);
    }

    public static boolean isSolidNoIce(BlockView reader, BlockPos pos) {
        BlockState state = reader.getBlockState(pos);
        return isSolid(state, reader, pos) && !BlockTags.ICE.contains(state.getBlock());
    }

    public static boolean isClearOverhead(WorldAccess world, BlockPos pos, int height, BiPredicate<WorldAccess, BlockPos> predicate) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        // world.getMaxHeight ?
        int max = Math.min(world.getDimensionHeight() - 1, pos.getY() + height);
        for (int y = pos.getY(); y < max; y++) {
            mutable.set(pos.getX(), y, pos.getZ());
            if (!predicate.test(world, mutable)) {
                return false;
            }
        }
        return true;
    }
}
