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

import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.feature.TerraFeatures;
import com.terraforged.mod.feature.context.ContextSelectorFeature;
import com.terraforged.mod.feature.decorator.FilterDecorator;
import com.terraforged.mod.feature.decorator.poisson.FastPoissonAtSurface;
import com.terraforged.mod.feature.feature.BushFeature;
import com.terraforged.mod.feature.feature.DiskFeature;
import com.terraforged.mod.feature.feature.FreezeLayer;
import net.fabricmc.fabric.api.biome.v1.OverworldBiomes;
import net.fabricmc.fabric.api.biome.v1.OverworldClimate;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.BiomeKeys;

public class RegistrationEvents {

    static void registerCodecs() {
        Registry.register(Registry.BIOME_SOURCE, TerraForgedMod.MODID + ":climate", TFBiomeProvider.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, TerraForgedMod.MODID + ":generator", TFChunkGenerator.CODEC);
    }

    static void registerMissingBiomeTypes() {
        OverworldBiomes.addContinentalBiome(BiomeKeys.ICE_SPIKES, OverworldClimate.SNOWY, 0.2);
        OverworldBiomes.addContinentalBiome(BiomeKeys.MUSHROOM_FIELDS, OverworldClimate.TEMPERATE, 0.2);
        OverworldBiomes.addContinentalBiome(BiomeKeys.MUSHROOM_FIELD_SHORE, OverworldClimate.TEMPERATE, 0.2);
    }

    public static void registerFeatures() {
        Log.info("Registering features");
        TerraFeatures.INSTANCE.toString();
        DiskFeature.INSTANCE.toString();
        FreezeLayer.INSTANCE.toString();
        BushFeature.INSTANCE.toString();
        ContextSelectorFeature.INSTANCE.toString();
    }

    public static void registerDecorators() {
        Log.info("Registering decorators");
        FilterDecorator.INSTANCE.toString();
        FastPoissonAtSurface.INSTANCE.toString();
    }
}
