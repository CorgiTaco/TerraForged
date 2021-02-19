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

import com.terraforged.mod.Log;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import com.terraforged.mod.chunk.settings.StructureSettings;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StructureValidator {

    private static final String REMOVED = "A third-party mod has removed structure [{}] from all overworld biomes so it cannot be generated!";
    private static final String UNREGISTERED = "Structure [{}] does not have any generation settings registered for it so it cannot be generated!";

    public static void validateConfigs(ChunkGeneratorSettings dimension, TFBiomeContext context, StructureSettings settings) {
        Log.info("Validating user structure preferences...");

        final StructuresConfig structuresSettings = dimension.getStructuresConfig();
        final List<StructureFeature<?>> activeStructures = getActiveStructures(context);
        final Map<String, StructureSettings.StructureSeparation> userSettings = settings.getOrDefaultStructures();

        // Check for structures that have been removed from biomes when the user has it enabled
        for (Map.Entry<String, StructureSettings.StructureSeparation> entry : userSettings.entrySet()) {
            if (entry.getValue().disabled) {
                continue;
            }

            Identifier name = Identifier.tryParse(entry.getKey());
            if (name == null) {
                continue;
            }

            StructureFeature<?> structure = Registry.STRUCTURE_FEATURE.get(name);
            if (structure != null && structuresSettings.getForType(structure) == null) {
                Log.info(REMOVED, name);
            }
        }

        // Check for mods removing strongholds from all biomes when the user has it enabled
        if (!settings.stronghold.disabled && !activeStructures.contains(StructureFeature.STRONGHOLD)) {
            Log.info(REMOVED, Registry.STRUCTURE_FEATURE.getId(StructureFeature.STRONGHOLD));
        }

        // Check for structures that have been added to biomes without having registered generation settings for it
        for (StructureFeature<?> structure : activeStructures) {
            if (structuresSettings.getForType(structure) == null) {
                String name = Objects.toString(Registry.STRUCTURE_FEATURE.getId(structure));
                StructureSettings.StructureSeparation userSetting = userSettings.get(name);

                // Ignore if user has disabled it anyway
                if (userSetting != null && userSetting.disabled) {
                    continue;
                }

                Log.warn(UNREGISTERED, name);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static List<StructureFeature<?>> getActiveStructures(TFBiomeContext context) {
        final Set<StructureFeature<?>> structures = new HashSet<>();
        final Biome[] overworldBiomes = BiomeAnalyser.getOverworldBiomes(context);

        for (Biome biome : overworldBiomes) {
            for (Supplier<ConfiguredStructureFeature<?, ?>> structureFeature : biome.getGenerationSettings().getStructureFeatures()) {
                StructureFeature<?> structure = structureFeature.get().feature;
                structures.add(structure);
            }
        }

        return structures.stream()
                .filter(structure -> Registry.STRUCTURE_FEATURE.getId(structure) != null)
                .sorted(Comparator.comparing(Registry.STRUCTURE_FEATURE::getId))
                .collect(Collectors.toList());
    }
}
