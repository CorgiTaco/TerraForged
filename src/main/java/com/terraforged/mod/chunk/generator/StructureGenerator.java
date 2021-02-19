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
import com.terraforged.mod.chunk.settings.TerraSettings;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.function.Supplier;

public class StructureGenerator implements Generator.Structures {

    private final TFChunkGenerator generator;
    private final StructuresConfig structuresSettings;

    public StructureGenerator(TFChunkGenerator generator) {
        TerraSettings settings = generator.getBiomeSource().getSettings();
        this.generator = generator;
        this.structuresSettings = settings.structures.apply(generator.getSettings().get().getStructuresConfig()); // defensive copy
    }

    @Override
    public StructureConfig getSeparationSettings(StructureFeature<?> structure) {
        return structuresSettings.getForType(structure);
    }

    @Override
    public void generateStructureStarts(Chunk chunk, DynamicRegistryManager registries, StructureAccessor structures, StructureManager templates) {
        long seed = generator.getSeed();
        ChunkPos pos = chunk.getPos();
        generator.queueChunk(pos);
        Biome biome = generator.getBiomeSource().getBiomeForNoiseGen((pos.x << 2) + 2, 0, (pos.z << 2) + 2);
        generate(chunk, pos, biome, ConfiguredStructureFeatures.STRONGHOLD, registries, structures, templates, seed);
        for (Supplier<ConfiguredStructureFeature<?, ?>> supplier : biome.getGenerationSettings().getStructureFeatures()) {
            this.generate(chunk, pos, biome, supplier.get(), registries, structures, templates, seed);
        }
    }

    private void generate(Chunk chunk, ChunkPos pos, Biome biome, ConfiguredStructureFeature<?, ?> structure, DynamicRegistryManager registries, StructureAccessor structures, StructureManager templates, long seed) {
        StructureStart<?> start = structures.getStructureStart(ChunkSectionPos.from(chunk.getPos(), 0), structure.feature, chunk);
        int i = start != null ? start.getReferences() : 0;
        StructureConfig settings = getSeparationSettings(structure.feature);
        if (settings != null) {
            StructureStart<?> start1 = structure.tryPlaceStart(registries, generator, generator.getBiomeSource(), templates, seed, pos, biome, i, settings);
            structures.setStructureStart(ChunkSectionPos.from(chunk.getPos(), 0), structure.feature, start1, chunk);
        }
    }

    @Override
    public void generateStructureReferences(StructureWorldAccess world, Chunk chunk, StructureAccessor structures) {
        int radius = 8;
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        int endX = startX + 15;
        int endZ = startZ + 15;
        ChunkSectionPos sectionpos = ChunkSectionPos.from(chunk.getPos(), 0);

        for (int x = chunkX - radius; x <= chunkX + radius; ++x) {
            for (int z = chunkZ - radius; z <= chunkZ + radius; ++z) {
                long posId = ChunkPos.toLong(x, z);

                for (StructureStart<?> start : world.getChunk(x, z).getStructureStarts().values()) {
                    try {
                        if (start != StructureStart.DEFAULT && start.getBoundingBox().intersectsXZ(startX, startZ, endX, endZ)) {
                            structures.addStructureReference(sectionpos, start.getFeature(), posId, chunk);
                            DebugInfoSender.sendStructureStart(world, start);
                        }
                    } catch (Exception exception) {
                        CrashReport crashreport = CrashReport.create(exception, "Generating structure reference");
                        CrashReportSection crashreportcategory = crashreport.addElement("Structure");
                        crashreportcategory.add("Id", () -> Registry.STRUCTURE_FEATURE.getId(start.getFeature()).toString());
                        crashreportcategory.add("Name", () -> start.getFeature().getName());
                        crashreportcategory.add("Class", () -> start.getFeature().getClass().getCanonicalName());
                        throw new CrashException(crashreport);
                    }
                }
            }
        }
    }
}
