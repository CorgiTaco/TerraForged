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

package com.terraforged.mod.profiler.crash;

import com.google.gson.JsonElement;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.config.ConfigRef;
import com.terraforged.mod.profiler.ProfilerPrinter;
import com.terraforged.mod.util.DataUtils;
import com.terraforged.mod.util.reflect.Accessor;
import com.terraforged.mod.util.reflect.FieldAccessor;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.StructureFeature;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CrashReportBuilder {

    private static final FieldAccessor<CrashReport, ?> ACCESSOR = Accessor.field(CrashReport.class, boolean.class);

    public static CrashReport buildCrashReport(Chunk chunk, TFChunkGenerator generator, Throwable t) {
        CrashReport report = new CrashReport(t.getMessage(), t.getCause());
        // prevents pollution of the report with duplicate stack-traces
        ACCESSOR.setUnchecked(report, false);
        try {
            addContext(chunk, generator, report);
        } catch (Throwable ctx) {
            report.addElement("Reporting Exception").add("Uh Oh", ctx);
        }
        return report;
    }

    private static void addContext(Chunk chunk, TFChunkGenerator generator, CrashReport report) {
        addContext(chunk, report.addElement("Current Chunk"));
        addContext(generator, report.addElement("TerraForged ChunkGenerator"));
        addContext(generator.getBiomeSource(), report.addElement("TerraForged BiomeProvider"));
        addContext(ConfigManager.PERFORMANCE, report.addElement("TerraForged Performance Config"));
        addContext(ConfigManager.BIOME_WEIGHTS, report.addElement("TerraForged Biome Weights Config"));
        addProfilerContext(report.addElement("TerraForged Profiler"));
    }

    private static void addContext(Chunk chunk, CrashReportSection report) {
        report.add("Pos", chunk.getPos());
        report.add("Status", chunk.getStatus().getId());
        report.add("Structure Starts", getStructures(chunk.getStructureStarts()));
        report.add("Structure Refs", getStructures(chunk.getStructureReferences()));
    }

    private static void addContext(TFChunkGenerator generator, CrashReportSection report) {
        report.add("Seed", generator.getSeed());
        report.add("Settings", DataUtils.toJson(generator.getContext().terraSettings));
    }

    //TODO: Pull Dynamic Registries
    private static void addContext(TFBiomeProvider biomes, CrashReportSection report) {
//        report.add("Overworld Biomes", biomes.getBiomes().stream().map(Biome::getRegistryName).collect(Collectors.toList()));
//        report.addDetail("Biome Map", biomes.getBiomeMap().toJson(biomes.getContext().gameContext));
    }

    private static void addContext(ConfigRef ref, CrashReportSection report) {
        ref.forEach(report::add);
    }

    private static void addProfilerContext(CrashReportSection report) {
        StringWriter writer = new StringWriter();
        try {
            ProfilerPrinter.print(writer, "\t\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        report.add("Timings", "\n" + writer.toString());
    }

    private static List<String> getStructures(Map<StructureFeature<?>, ?> map) {
        return map.keySet().stream().map(CrashReportBuilder::getStructureName).sorted().collect(Collectors.toList());
    }

    private static String getStructureName(StructureFeature<?> structure) {
        Identifier name = Registry.STRUCTURE_FEATURE.getId(structure);
        if (name == null) {
            return structure.getName();
        }
        return name.toString();
    }

    private static String print(JsonElement jsonElement) {
        return "\n" + jsonElement.toString();
    }
}
