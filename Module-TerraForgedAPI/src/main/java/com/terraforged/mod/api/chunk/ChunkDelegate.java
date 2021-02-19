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

package com.terraforged.mod.api.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.feature.StructureFeature;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ChunkDelegate implements Chunk {

    protected final Chunk delegate;

    public ChunkDelegate(Chunk delegate) {
        this.delegate = delegate;
    }

    public Chunk getDelegate() {
        return delegate;
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        return delegate.setBlockState(pos, state, isMoving);
    }

    @Override
    public void setBlockEntity(BlockPos pos, BlockEntity tileEntityIn) {
        delegate.setBlockEntity(pos, tileEntityIn);
    }

    @Override
    public void addEntity(Entity entityIn) {
        delegate.addEntity(entityIn);
    }

    @Override
    @Nullable
    public ChunkSection getHighestNonEmptySection() {
        return delegate.getHighestNonEmptySection();
    }

    @Override
    public int getHighestNonEmptySectionYOffset() {
        return delegate.getHighestNonEmptySectionYOffset();
    }

    @Override
    public Set<BlockPos> getBlockEntityPositions() {
        return delegate.getBlockEntityPositions();
    }

    @Override
    public ChunkSection[] getSectionArray() {
        return delegate.getSectionArray();
    }

    @Override
    public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
        return delegate.getHeightmaps();
    }

    @Override
    public void setHeightmap(Heightmap.Type type, long[] data) {
        delegate.setHeightmap(type, data);
    }

    @Override
    public Heightmap getHeightmap(Heightmap.Type typeIn) {
        return delegate.getHeightmap(typeIn);
    }

    @Override
    public int sampleHeightmap(Heightmap.Type heightmapType, int x, int z) {
        return delegate.sampleHeightmap(heightmapType, x, z);
    }

    @Override
    public ChunkPos getPos() {
        return delegate.getPos();
    }

    @Override
    public void setLastSaveTime(long saveTime) {
        delegate.setLastSaveTime(saveTime);
    }

    @Override
    public Map<StructureFeature<?>, StructureStart<?>> getStructureStarts() {
        return delegate.getStructureStarts();
    }

    @Override
    public void setStructureStarts(Map<StructureFeature<?>, StructureStart<?>> structureStartsIn) {
        delegate.setStructureStarts(structureStartsIn);
    }

    @Override
    public boolean areSectionsEmptyBetween(int startY, int endY) {
        return delegate.areSectionsEmptyBetween(startY, endY);
    }

    @Override
    @Nullable
    public BiomeArray getBiomeArray() {
        return delegate.getBiomeArray();
    }

    @Override
    public void setShouldSave(boolean modified) {
        delegate.setShouldSave(modified);
    }

    @Override
    public boolean needsSaving() {
        return delegate.needsSaving();
    }

    @Override
    public ChunkStatus getStatus() {
        return delegate.getStatus();
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
        delegate.removeBlockEntity(pos);
    }

    @Override
    public void markBlockForPostProcessing(BlockPos pos) {
        delegate.markBlockForPostProcessing(pos);
    }

    @Override
    public ShortList[] getPostProcessingLists() {
        return delegate.getPostProcessingLists();
    }

    @Override
    public void markBlockForPostProcessing(short packedPosition, int index) {
        delegate.markBlockForPostProcessing(packedPosition, index);
    }

    @Override
    public void addPendingBlockEntityTag(CompoundTag nbt) {
        delegate.addPendingBlockEntityTag(nbt);
    }

    @Override
    @Nullable
    public CompoundTag getBlockEntityTag(BlockPos pos) {
        return delegate.getBlockEntityTag(pos);
    }

    @Override
    @Nullable
    public CompoundTag getPackedBlockEntityTag(BlockPos pos) {
        return delegate.getPackedBlockEntityTag(pos);
    }

    @Override
    public Stream<BlockPos> getLightSourcesStream() {
        return delegate.getLightSourcesStream();
    }

    @Override
    public TickScheduler<Block> getBlockTickScheduler() {
        return delegate.getBlockTickScheduler();
    }

    @Override
    public TickScheduler<Fluid> getFluidTickScheduler() {
        return delegate.getFluidTickScheduler();
    }

    @Override
    public UpgradeData getUpgradeData() {
        return delegate.getUpgradeData();
    }

    @Override
    public void setInhabitedTime(long newInhabitedTime) {
        delegate.setInhabitedTime(newInhabitedTime);
    }

    @Override
    public long getInhabitedTime() {
        return delegate.getInhabitedTime();
    }

    @Override
    public boolean isLightOn() {
        return delegate.isLightOn();
    }

    @Override
    public void setLightOn(boolean lightCorrectIn) {
        delegate.setLightOn(lightCorrectIn);
    }

//    @Override
//    @Nullable
//    public WorldAccess getWorldForge() {
//        return delegate.getWorldForge();
//    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return delegate.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return delegate.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return delegate.getFluidState(pos);
    }

    @Override
    public int getLuminance(BlockPos pos) {
        return delegate.getLuminance(pos);
    }

    @Override
    public int getMaxLightLevel() {
        return delegate.getMaxLightLevel();
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public Stream<BlockState> method_29546(Box p_234853_1_) {
        return delegate.method_29546(p_234853_1_);
    }

    @Override
    public BlockHitResult raycast(RaycastContext context) {
        return delegate.raycast(context);
    }

    @Override
    @Nullable
    public BlockHitResult raycastBlock(Vec3d startVec, Vec3d endVec, BlockPos pos, VoxelShape shape, BlockState state) {
        return delegate.raycastBlock(startVec, endVec, pos, shape, state);
    }

    @Override
    public double getDismountHeight(VoxelShape p_242402_1_, Supplier<VoxelShape> p_242402_2_) {
        return delegate.getDismountHeight(p_242402_1_, p_242402_2_);
    }

    @Override
    public double getDismountHeight(BlockPos p_242403_1_) {
        return delegate.getDismountHeight(p_242403_1_);
    }

    @Override
    @Nullable
    public StructureStart<?> getStructureStart(StructureFeature<?> p_230342_1_) {
        return delegate.getStructureStart(p_230342_1_);
    }

    @Override
    public void setStructureStart(StructureFeature<?> p_230344_1_, StructureStart<?> p_230344_2_) {
        delegate.setStructureStart(p_230344_1_, p_230344_2_);
    }

    @Override
    public LongSet getStructureReferences(StructureFeature<?> p_230346_1_) {
        return delegate.getStructureReferences(p_230346_1_);
    }

    @Override
    public void addStructureReference(StructureFeature<?> p_230343_1_, long p_230343_2_) {
        delegate.addStructureReference(p_230343_1_, p_230343_2_);
    }

    @Override
    public Map<StructureFeature<?>, LongSet> getStructureReferences() {
        return delegate.getStructureReferences();
    }

    @Override
    public void setStructureReferences(Map<StructureFeature<?>, LongSet> structureReferences) {
        delegate.setStructureReferences(structureReferences);
    }

    public static Chunk unwrap(Chunk chunk) {
        if (chunk instanceof ChunkDelegate) {
            return unwrap(((ChunkDelegate) chunk).getDelegate());
        }
        return chunk;
    }
}