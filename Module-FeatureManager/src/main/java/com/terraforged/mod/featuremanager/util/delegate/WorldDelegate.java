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

package com.terraforged.mod.featuremanager.util.delegate;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ColorResolver;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WorldDelegate<T extends WorldAccess> implements WorldAccess {

    protected T delegate;

    public WorldDelegate(T delegate) {
        this.delegate = delegate;
    }

    public T getDelegate() {
        return delegate;
    }

    public void setDelegate(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public long getLunarTime() {
        return delegate.getLunarTime();
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
    public WorldProperties getLevelProperties() {
        return delegate.getLevelProperties();
    }

    @Override
    public LocalDifficulty getLocalDifficulty(BlockPos pos) {
        return delegate.getLocalDifficulty(pos);
    }

    @Override
    public Difficulty getDifficulty() {
        return delegate.getDifficulty();
    }

    @Override
    public ChunkManager getChunkManager() {
        return delegate.getChunkManager();
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return delegate.isChunkLoaded(chunkX, chunkZ);
    }

    @Override
    public Random getRandom() {
        return delegate.getRandom();
    }

    @Override
    public void updateNeighbors(BlockPos p_230547_1_, Block p_230547_2_) {
        delegate.updateNeighbors(p_230547_1_, p_230547_2_);
    }

    @Override
    public void playSound(PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        delegate.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void addParticle(ParticleEffect particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        delegate.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void syncWorldEvent(PlayerEntity player, int type, BlockPos pos, int data) {
        delegate.syncWorldEvent(player, type, pos, data);
    }

    @Override
    public int getDimensionHeight() {
        return delegate.getDimensionHeight();
    }

    @Override
    public void syncWorldEvent(int type, BlockPos pos, int data) {
        delegate.syncWorldEvent(type, pos, data);
    }

    @Override
    public Stream<VoxelShape> getEntityCollisions(Entity p_230318_1_, Box p_230318_2_, Predicate<Entity> p_230318_3_) {
        return delegate.getEntityCollisions(p_230318_1_, p_230318_2_, p_230318_3_);
    }

    @Override
    public boolean intersectsEntities(Entity entityIn, VoxelShape shape) {
        return delegate.intersectsEntities(entityIn, shape);
    }

    @Override
    public BlockPos getTopPosition(Heightmap.Type heightmapType, BlockPos pos) {
        return delegate.getTopPosition(heightmapType, pos);
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return delegate.getRegistryManager();
    }

    @Override
    public Optional<RegistryKey<Biome>> method_31081(BlockPos p_242406_1_) {
        return delegate.method_31081(p_242406_1_);
    }

    @Override
    public List<Entity> getOtherEntities(Entity entityIn, Box boundingBox, Predicate<? super Entity> predicate) {
        return delegate.getOtherEntities(entityIn, boundingBox, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesByClass(Class<? extends T> clazz, Box aabb, Predicate<? super T> filter) {
        return delegate.getEntitiesByClass(clazz, aabb, filter);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> p_225316_1_, Box p_225316_2_, Predicate<? super T> p_225316_3_) {
        return delegate.getEntitiesIncludingUngeneratedChunks(p_225316_1_, p_225316_2_, p_225316_3_);
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return delegate.getPlayers();
    }

    @Override
    public List<Entity> getOtherEntities(Entity entityIn, Box bb) {
        return delegate.getOtherEntities(entityIn, bb);
    }

    @Override
    public <T extends Entity> List<T> getNonSpectatingEntities(Class<? extends T> p_217357_1_, Box p_217357_2_) {
        return delegate.getNonSpectatingEntities(p_217357_1_, p_217357_2_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> p_225317_1_, Box p_225317_2_) {
        return delegate.getEntitiesIncludingUngeneratedChunks(p_225317_1_, p_225317_2_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        return delegate.getClosestPlayer(x, y, z, distance, predicate);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(Entity entityIn, double distance) {
        return delegate.getClosestPlayer(entityIn, distance);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, boolean creativePlayers) {
        return delegate.getClosestPlayer(x, y, z, distance, creativePlayers);
    }

    @Override
    public boolean isPlayerInRange(double x, double y, double z, double distance) {
        return delegate.isPlayerInRange(x, y, z, distance);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(TargetPredicate predicate, LivingEntity target) {
        return delegate.getClosestPlayer(predicate, target);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(TargetPredicate predicate, LivingEntity target, double p_217372_3_, double p_217372_5_, double p_217372_7_) {
        return delegate.getClosestPlayer(predicate, target, p_217372_3_, p_217372_5_, p_217372_7_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(TargetPredicate predicate, double x, double y, double z) {
        return delegate.getClosestPlayer(predicate, x, y, z);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getClosestEntity(Class<? extends T> entityClazz, TargetPredicate p_217360_2_, LivingEntity target, double x, double y, double z, Box boundingBox) {
        return delegate.getClosestEntity(entityClazz, p_217360_2_, target, x, y, z, boundingBox);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getClosestEntityIncludingUngeneratedChunks(Class<? extends T> p_225318_1_, TargetPredicate p_225318_2_, LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, Box p_225318_10_) {
        return delegate.getClosestEntityIncludingUngeneratedChunks(p_225318_1_, p_225318_2_, p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_, p_225318_10_);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getClosestEntity(List<? extends T> entities, TargetPredicate predicate, LivingEntity target, double x, double y, double z) {
        return delegate.getClosestEntity(entities, predicate, target, x, y, z);
    }

    @Override
    public List<PlayerEntity> getPlayers(TargetPredicate predicate, LivingEntity target, Box box) {
        return delegate.getPlayers(predicate, target, box);
    }

    @Override
    public <T extends LivingEntity> List<T> getTargets(Class<? extends T> p_217374_1_, TargetPredicate p_217374_2_, LivingEntity p_217374_3_, Box p_217374_4_) {
        return delegate.getTargets(p_217374_1_, p_217374_2_, p_217374_3_, p_217374_4_);
    }

    @Override
    @Nullable
    public PlayerEntity getPlayerByUuid(UUID uniqueIdIn) {
        return delegate.getPlayerByUuid(uniqueIdIn);
    }

    @Override
    @Nullable
    public Chunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return delegate.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public int getTopY(Heightmap.Type heightmapType, int x, int z) {
        return delegate.getTopY(heightmapType, x, z);
    }

    @Override
    public int getAmbientDarkness() {
        return delegate.getAmbientDarkness();
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return delegate.getBiomeAccess();
    }

    @Override
    public Biome getBiome(BlockPos p_226691_1_) {
        return delegate.getBiome(p_226691_1_);
    }

    @Override
    public Stream<BlockState> method_29556(Box area) {
        return delegate.method_29556(area);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getColor(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return delegate.getColor(blockPosIn, colorResolverIn);
    }

    @Override
    public Biome getBiomeForNoiseGen(int x, int y, int z) {
        return delegate.getBiomeForNoiseGen(x, y, z);
    }

    @Override
    public Biome getGeneratorStoredBiome(int x, int y, int z) {
        return delegate.getGeneratorStoredBiome(x, y, z);
    }

    @Override
    public boolean isClient() {
        return delegate.isClient();
    }

    @Override
    @Deprecated
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public DimensionType getDimension() {
        return delegate.getDimension();
    }

    @Override
    public boolean isAir(BlockPos pos) {
        return delegate.isAir(pos);
    }

    @Override
    public boolean isSkyVisibleAllowingSea(BlockPos pos) {
        return delegate.isSkyVisibleAllowingSea(pos);
    }

    @Override
    @Deprecated
    public float getBrightness(BlockPos pos) {
        return delegate.getBrightness(pos);
    }

    @Override
    public int getStrongRedstonePower(BlockPos pos, Direction direction) {
        return delegate.getStrongRedstonePower(pos, direction);
    }

    @Override
    public Chunk getChunk(BlockPos pos) {
        return delegate.getChunk(pos);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return delegate.getChunk(chunkX, chunkZ);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
        return delegate.getChunk(chunkX, chunkZ, requiredStatus);
    }

    @Override
    @Nullable
    public BlockView getExistingChunk(int chunkX, int chunkZ) {
        return delegate.getExistingChunk(chunkX, chunkZ);
    }

    @Override
    public boolean isWater(BlockPos pos) {
        return delegate.isWater(pos);
    }

    @Override
    public boolean containsFluid(Box bb) {
        return delegate.containsFluid(bb);
    }

    @Override
    public int getLightLevel(BlockPos pos) {
        return delegate.getLightLevel(pos);
    }

    @Override
    public int getLightLevel(BlockPos pos, int amount) {
        return delegate.getLightLevel(pos, amount);
    }

    @Override
    @Deprecated
    public boolean isChunkLoaded(BlockPos pos) {
        return delegate.isChunkLoaded(pos);
    }

//    @Override
//    public boolean isAreaLoaded(BlockPos center, int range) {
//        return delegate.isAreaLoaded(center, range);
//    }

    @Override
    @Deprecated
    public boolean isRegionLoaded(BlockPos from, BlockPos to) {
        return delegate.isRegionLoaded(from, to);
    }

    @Override
    @Deprecated
    public boolean isRegionLoaded(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return delegate.isRegionLoaded(fromX, fromY, fromZ, toX, toY, toZ);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float getBrightness(Direction p_230487_1_, boolean p_230487_2_) {
        return delegate.getBrightness(p_230487_1_, p_230487_2_);
    }

    @Override
    public LightingProvider getLightingProvider() {
        return delegate.getLightingProvider();
    }

    @Override
    public int getLightLevel(LightType lightTypeIn, BlockPos blockPosIn) {
        return delegate.getLightLevel(lightTypeIn, blockPosIn);
    }

    @Override
    public int getBaseLightLevel(BlockPos blockPosIn, int amount) {
        return delegate.getBaseLightLevel(blockPosIn, amount);
    }

    @Override
    public boolean isSkyVisible(BlockPos blockPosIn) {
        return delegate.isSkyVisible(blockPosIn);
    }

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
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public boolean canPlace(BlockState state, BlockPos pos, ShapeContext context) {
        return delegate.canPlace(state, pos, context);
    }

    @Override
    public boolean intersectsEntities(Entity p_226668_1_) {
        return delegate.intersectsEntities(p_226668_1_);
    }

    @Override
    public boolean isSpaceEmpty(Box p_226664_1_) {
        return delegate.isSpaceEmpty(p_226664_1_);
    }

    @Override
    public boolean isSpaceEmpty(Entity p_226669_1_) {
        return delegate.isSpaceEmpty(p_226669_1_);
    }

    @Override
    public boolean isSpaceEmpty(Entity p_226665_1_, Box p_226665_2_) {
        return delegate.isSpaceEmpty(p_226665_1_, p_226665_2_);
    }

    @Override
    public boolean isSpaceEmpty(Entity p_234865_1_, Box p_234865_2_, Predicate<Entity> p_234865_3_) {
        return delegate.isSpaceEmpty(p_234865_1_, p_234865_2_, p_234865_3_);
    }

    @Override
    public Stream<VoxelShape> getCollisions(Entity p_234867_1_, Box p_234867_2_, Predicate<Entity> p_234867_3_) {
        return delegate.getCollisions(p_234867_1_, p_234867_2_, p_234867_3_);
    }

    @Override
    public Stream<VoxelShape> getBlockCollisions(Entity p_226666_1_, Box p_226666_2_) {
        return delegate.getBlockCollisions(p_226666_1_, p_226666_2_);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isBlockSpaceEmpty(Entity p_242405_1_, Box p_242405_2_, BiPredicate<BlockState, BlockPos> p_242405_3_) {
        return delegate.isBlockSpaceEmpty(p_242405_1_, p_242405_2_, p_242405_3_);
    }

    @Override
    public Stream<VoxelShape> getBlockCollisions(Entity p_241457_1_, Box p_241457_2_, BiPredicate<BlockState, BlockPos> p_241457_3_) {
        return delegate.getBlockCollisions(p_241457_1_, p_241457_2_, p_241457_3_);
    }

    @Override
    public boolean testBlockState(BlockPos pos, Predicate<BlockState> state) {
        return delegate.testBlockState(pos, state);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        return delegate.setBlockState(pos, state, flags, recursionLeft);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        return delegate.setBlockState(pos, newState, flags);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        return delegate.removeBlock(pos, isMoving);
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean dropBlock) {
        return delegate.breakBlock(pos, dropBlock);
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean dropBlock, Entity entity) {
        return delegate.breakBlock(pos, dropBlock, entity);
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean dropBlock, Entity entity, int recursionLeft) {
        return delegate.breakBlock(pos, dropBlock, entity, recursionLeft);
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        return delegate.spawnEntity(entityIn);
    }

    @Override
    public float getMoonSize() {
        return delegate.getMoonSize();
    }

    @Override
    public float getSkyAngle(float p_242415_1_) {
        return delegate.getSkyAngle(p_242415_1_);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getMoonPhase() {
        return delegate.getMoonPhase();
    }
}
