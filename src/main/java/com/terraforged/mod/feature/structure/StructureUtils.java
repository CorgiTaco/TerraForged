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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import com.terraforged.mod.chunk.settings.StructureSettings;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class StructureUtils {

    public static JsonObject addMissingStructures(JsonObject dest) {
        ChunkGeneratorSettings settings = BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrThrow(ChunkGeneratorSettings.OVERWORLD);
        JsonElement element = Codecs.encode(StructuresConfig.CODEC, settings.getStructuresConfig());
        if (element.isJsonObject()) {
            JsonObject defaults = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
                if (!dest.has(entry.getKey())) {
                    dest.add(entry.getKey(), entry.getValue());
                }
            }
        }
        return dest;
    }

    public static Map<String, StructureSettings.StructureSeparation> getOverworldStructureDefaults() {
        Map<String, StructureSettings.StructureSeparation> map = new LinkedHashMap<>();

        ChunkGeneratorSettings dimensionSettings = BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrThrow(ChunkGeneratorSettings.OVERWORLD);
        StructuresConfig structuresSettings = dimensionSettings.getStructuresConfig();

        for (Map.Entry<StructureFeature<?>, StructureConfig> entry : structuresSettings.getStructures().entrySet()) {
            if (Registry.STRUCTURE_FEATURE.getId(entry.getKey()) == null) {
                continue;
            }

            StructureSettings.StructureSeparation separation = new StructureSettings.StructureSeparation();
            separation.salt = entry.getValue().getSalt();
            separation.spacing = entry.getValue().getSpacing();
            separation.separation = entry.getValue().getSeparation();

            map.put(Objects.toString(Registry.STRUCTURE_FEATURE.getId(entry.getKey())), separation);
        }

        return map;
    }

    public static void retainOverworldStructures(Map<String, ?> map, StructuresConfig settings, TFBiomeContext context) {
        Biome[] overworldBiomes = BiomeAnalyser.getOverworldBiomes(context);
        for (StructureFeature<?> structure : settings.getStructures().keySet()) {
            if (Registry.STRUCTURE_FEATURE.getId(structure) == null) {
                continue;
            }

            // Remove if stronghold (has its own settings) or isn't an overworld structure
            if (structure == StructureFeature.STRONGHOLD || !hasStructure(structure, overworldBiomes)) {
                map.remove(Registry.STRUCTURE_FEATURE.getId(structure).toString());
            }
        }
    }

    public static boolean hasStructure(StructureFeature<?> structure, Biome[] biomes) {
        for (Biome biome : biomes) {
            if (biome.getGenerationSettings().hasStructureFeature(structure)) {
                return true;
            }
        }
        return false;
    }
}
