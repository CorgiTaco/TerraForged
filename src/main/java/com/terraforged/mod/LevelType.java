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

package com.terraforged.mod;

import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.mixin.access.GeneratorTypeAccess;
import com.terraforged.mod.util.DimUtils;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

import java.util.function.Supplier;

//TODO: Add options
public class LevelType extends GeneratorType {

    public static final Identifier LEVEL_NAME = new Identifier(TerraForgedMod.MODID, "terraforged");

    public static final GeneratorType TERRAFORGED = new LevelType();


    public LevelType() {
        super(LEVEL_NAME.toString());
    }

    @Override
    protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
//        // server.properties >> generator-settings: <preset_name>
//        Optional<Preset> preset = PresetManager.getPreset(options);
//        if (preset.isPresent()) {
//            Log.info("Creating TerraForged chunk-generator from preset {}", preset.get().getName());
//            TerraSettings terraSettings = preset.get().getSettings(seed);
//            return createOverworld(terraSettings, biomeRegistry, settings);
//        } else {
//            Log.info("Creating default TerraForged chunk-generator");
            return createOverworld(TerraSettings.defaults(seed), biomeRegistry, chunkGeneratorSettingsRegistry);
//        }
    }

    @Override
    public GeneratorOptions createDefaultOptions(DynamicRegistryManager.Impl registries, long seed, boolean generateStructures, boolean bonusChest) {
        Log.info("Creating TerraForged level settings");
        Registry<Biome> biomes = registries.get(Registry.BIOME_KEY);
        Registry<ChunkGeneratorSettings> settings = registries.get(Registry.NOISE_SETTINGS_WORLDGEN);
        TFChunkGenerator chunkGenerator = (TFChunkGenerator) getChunkGenerator(biomes, settings, seed);
        GeneratorOptions level = new GeneratorOptions(
                seed,
                generateStructures,
                bonusChest,
                DimUtils.createDimensionRegistry(seed, registries, chunkGenerator)
        );
        return DimUtils.populateDimensions(level, registries, chunkGenerator.getContext().terraSettings);
    }

    public static GeneratorOptions updateOverworld(GeneratorOptions level, DynamicRegistryManager.Impl registries, TerraSettings settings) {
        Log.info("Updating TerraForged level settings");
        TFChunkGenerator updatedGenerator = createOverworld(settings, registries);
        GeneratorOptions updatedLevel = new GeneratorOptions(
                level.getSeed(),
                level.shouldGenerateStructures(),
                level.hasBonusChest(),
                DimUtils.updateDimensionRegistry(level.getDimensions(), registries, updatedGenerator)
        );
        return DimUtils.populateDimensions(updatedLevel, registries, settings);
    }

    private static TFChunkGenerator createOverworld(TerraSettings settings, DynamicRegistryManager registries) {
        return createOverworld(settings, registries.get(Registry.BIOME_KEY), registries.get(Registry.NOISE_SETTINGS_WORLDGEN));
    }

    private static TFChunkGenerator createOverworld(TerraSettings settings, Registry<Biome> biomes, Registry<ChunkGeneratorSettings> dimSettings) {
        TFBiomeContext game = new TFBiomeContext(biomes);
        TerraContext context = new TerraContext(settings, game);
        TFBiomeProvider biomeProvider = new TFBiomeProvider(context);
        return new TFChunkGenerator(biomeProvider, new RegistryGetter<>(dimSettings, ChunkGeneratorSettings.OVERWORLD));
    }

    private static class RegistryGetter<T> implements Supplier<T> {

        private final Registry<T> registry;
        private final RegistryKey<T> key;

        private RegistryGetter(Registry<T> registry, RegistryKey<T> key) {
            this.registry = registry;
            this.key = key;
        }

        @Override
        public T get() {
            return registry.getOrThrow(key);
        }
    }
}
