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

package com.terraforged.mod.mixin.benchmark;

import com.terraforged.mod.profiler.Profiler;
import com.terraforged.mod.profiler.Section;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunkGenerator.class)
public abstract class MixinNoiseChunkGenerator extends ChunkGenerator {

    public MixinNoiseChunkGenerator(BiomeSource biomeProvider, StructuresConfig settings) {
        super(biomeProvider, settings);
    }

    @Inject(method = "populateNoise", at = @At("HEAD"))
    public void terrainIn(WorldAccess world, StructureAccessor structures, Chunk chunk, CallbackInfo ci) {
        Profiler.TERRAIN.punchIn();
    }

    @Inject(method = "populateNoise", at = @At("TAIL"))
    public void terrainOut(WorldAccess world, StructureAccessor structures, Chunk chunk, CallbackInfo ci) {
        Profiler.TERRAIN.punchOut();
    }

    @Inject(method = "buildSurface", at = @At("HEAD"))
    public void surfaceIn(ChunkRegion region, Chunk chunk, CallbackInfo ci) {
        Profiler.SURFACE.punchIn();
    }

    @Inject(method = "buildSurface", at = @At("TAIL"))
    public void surfaceOut(ChunkRegion region, Chunk chunk, CallbackInfo ci) {
        Profiler.SURFACE.punchOut();
    }

    @Inject(method = "populateEntities", at = @At("HEAD"))
    public void mobsIn(ChunkRegion region, CallbackInfo ci) {
        Profiler.MOB_SPAWNS.punchIn();
    }

    @Inject(method = "populateEntities", at = @At("TAIL"))
    public void mobsOut(ChunkRegion region, CallbackInfo ci) {
        Profiler.MOB_SPAWNS.punchOut();
    }

    @Override
    public void setStructureStarts(DynamicRegistryManager registries, StructureAccessor structures, Chunk chunk, StructureManager templates, long seed) {
        try (Section section = Profiler.STRUCTURE_STARTS.punchIn()) {
            super.setStructureStarts(registries, structures, chunk, templates, seed);
        }
    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor structures, Chunk chunk) {
        try (Section section = Profiler.STRUCTURE_REFS.punchIn()) {
            super.addStructureReferences(world, structures, chunk);
        }
    }

    @Override
    public void populateBiomes(Registry<Biome> registry, Chunk chunk) {
        try (Section section = Profiler.BIOMES.punchIn()) {
            super.populateBiomes(registry, chunk);
        }
    }

    @Override
    public void carve(long seed, BiomeAccess biomes, Chunk chunk, GenerationStep.Carver carver) {
        try (Section section = Profiler.get(carver).punchIn()) {
            super.carve(seed, biomes, chunk, carver);
        }
    }

    @Override
    public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
        try (Section section = Profiler.DECORATION.punchIn()) {
            super.generateFeatures(region, structures);
        }
    }
}
