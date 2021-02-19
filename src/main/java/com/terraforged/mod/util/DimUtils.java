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

package com.terraforged.mod.util;

import com.mojang.serialization.Lifecycle;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.ThreadLocalResource;
import com.terraforged.mod.LevelType;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.mixin.access.GeneratorTypeAccess;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DimUtils {

    private static final ThreadLocalResource<Map<GeneratorType, SimpleRegistry<DimensionOptions>>> CACHE = ThreadLocalResource.withInitial(HashMap::new, Map::clear);

    public static RegistryKey<DimensionType> getOverworldType() {
        return DimensionType.OVERWORLD_REGISTRY_KEY;
    }

    public static SimpleRegistry<DimensionOptions> createDimensionRegistry(long seed, DynamicRegistryManager registries, ChunkGenerator generator) {
        SimpleRegistry<DimensionOptions> registry = new SimpleRegistry<>(Registry.DIMENSION_OPTIONS, Lifecycle.stable());
        registry.add(DimensionOptions.OVERWORLD, createDimension(getOverworldType(), registries, generator), Lifecycle.stable());
        registry.add(DimensionOptions.NETHER, createDefaultNether(seed, registries), Lifecycle.stable());
        registry.add(DimensionOptions.END, createDefaultEnd(seed, registries), Lifecycle.stable());
        return registry;
    }

    public static SimpleRegistry<DimensionOptions> updateDimensionRegistry(SimpleRegistry<DimensionOptions> registry, DynamicRegistryManager registries, ChunkGenerator generator) {
        DimensionOptions dimension = createDimension(getOverworldType(), registries, generator);
        registry.replace(OptionalInt.empty(), DimensionOptions.OVERWORLD, dimension, Lifecycle.stable());
        return registry;
    }

    public static GeneratorOptions populateDimensions(GeneratorOptions level, DynamicRegistryManager.Impl registries, TerraSettings settings) {
        try (Resource<?> ignored = CACHE.get()) {
            // replace nether with custom dimension if present
            overrideDimension(DimensionOptions.NETHER, settings.dimensions.dimensions.nether, level, registries);
            // replace end with custom dimension if present
            overrideDimension(DimensionOptions.END, settings.dimensions.dimensions.end, level, registries);
            // scan forge registry for other level-types and add their extra (ie not overworld,nether,end) dimensions
            if (settings.dimensions.dimensions.includeExtraDimensions) {
                addExtraDimensions(level, registries);
            }
        }
        return level;
    }

    private static void overrideDimension(RegistryKey<DimensionOptions> key, String levelType, GeneratorOptions level, DynamicRegistryManager.Impl registries) {
        SimpleRegistry<DimensionOptions> dimensions = getDimensions(levelType, level, registries);
        if (dimensions == null) {
            return;
        }

        DimensionOptions dimension = dimensions.get(key);
        if (dimension == null) {
            return;
        }

        // replace the current Dimension assigned to the key without changing the id
        Log.info("Overriding dimension {} with {}'s", key.getValue(), levelType);
        level.getDimensions().replace(OptionalInt.empty(), key, dimension, Lifecycle.stable());
    }

    private static void addExtraDimensions(GeneratorOptions level, DynamicRegistryManager.Impl registries) {
        for (GeneratorType type : GeneratorTypeAccess.getVALUES()) {
            if (type == LevelType.TERRAFORGED) {
                continue;
            }

            SimpleRegistry<DimensionOptions> dimensions = getDimensions(type, level, registries);
            if (dimensions == null) {
                continue;
            }

            for (Map.Entry<RegistryKey<DimensionOptions>, DimensionOptions> entry : dimensions.getEntries()) {
                // skip existing dims
                if (level.getDimensions().get(entry.getKey()) != null) {
                    continue;
                }
//                Log.info("Adding extra dimension {} dimension from {}", entry.getKey().getValue(), registries.get(Registry.DIMENSION_TYPE_KEY).getId(type));
                level.getDimensions().add(entry.getKey(), entry.getValue(), Lifecycle.stable());
            }
        }
    }

    @Nullable
    private static SimpleRegistry<DimensionOptions> getDimensions(String levelType, GeneratorOptions level, DynamicRegistryManager.Impl registries) {
        GeneratorType type = getLevelType(levelType);
        return getDimensions(type, level, registries);
    }

    @Nullable
    private static SimpleRegistry<DimensionOptions> getDimensions(GeneratorType type, GeneratorOptions level, DynamicRegistryManager.Impl registries) {
        // ignore our own level-type
        if (type == null || type == LevelType.TERRAFORGED) {
            return null;
        }

        return CACHE.open().computeIfAbsent(type, t -> {
            long seed = level.getSeed();
            boolean chest = level.hasBonusChest();
            boolean structures = level.shouldGenerateStructures();
            return t.createDefaultOptions(registries, seed, structures, chest).getDimensions();
        });
    }

    private static DimensionOptions createDefaultNether(long seed, DynamicRegistryManager registries) {
        return createDefaultDimension(
                seed,
                DimensionType.THE_NETHER_REGISTRY_KEY,
                ChunkGeneratorSettings.NETHER,
                registries,
                MultiNoiseBiomeSource.Preset.NETHER::getBiomeSource
        );
    }

    private static DimensionOptions createDefaultEnd(long seed, DynamicRegistryManager registries) {
        return createDefaultDimension(
                seed,
                DimensionType.THE_END_REGISTRY_KEY,
                ChunkGeneratorSettings.END,
                registries,
                TheEndBiomeSource::new
        );
    }

    private static DimensionOptions createDefaultDimension(long seed,
                                                           RegistryKey<DimensionType> type,
                                                           RegistryKey<ChunkGeneratorSettings> setting,
                                                           DynamicRegistryManager registries,
                                                           BiFunction<Registry<Biome>, Long, BiomeSource> factory) {
        Registry<Biome> biomes = registries.get(Registry.BIOME_KEY);
        Registry<ChunkGeneratorSettings> settings = registries.get(Registry.NOISE_SETTINGS_WORLDGEN);
        Supplier<ChunkGeneratorSettings> settingSupplier = () -> settings.getOrThrow(setting);
        BiomeSource biomeProvider = factory.apply(biomes, seed);
        ChunkGenerator generator = new NoiseChunkGenerator(biomeProvider, seed, settingSupplier);
        return createDimension(type, registries, generator);
    }

    private static DimensionOptions createDimension(RegistryKey<DimensionType> type, DynamicRegistryManager registries, ChunkGenerator generator) {
        Log.info("Creating dimension: {}", type.getValue());
        Registry<DimensionType> types = registries.get(Registry.DIMENSION_TYPE_KEY);
        Supplier<DimensionType> typeSupplier = () -> types.getOrThrow(type);
        return new DimensionOptions(typeSupplier, generator);
    }

    public static String getDisplayString(GeneratorType type) {
        if (type == LevelType.TERRAFORGED) {
            return "default";
        }
        return String.valueOf(type.getTranslationKey());
    }

    public static GeneratorType getLevelType(String name) {
        if (name.equalsIgnoreCase("default")) {
            return LevelType.TERRAFORGED;
        }

        Identifier location = Identifier.tryParse(name);
        if (location == null || location.equals(LevelType.TERRAFORGED.getTranslationKey().asString())) {
            return LevelType.TERRAFORGED;
        }

//        GeneratorType type = ForgeRegistries.WORLD_TYPES.getValue(location);
//        if (type == null) {
//            return LevelType.TERRAFORGED;
//        }

        return null;
    }
}
