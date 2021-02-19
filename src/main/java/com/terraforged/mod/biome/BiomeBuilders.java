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

package com.terraforged.mod.biome;

import com.terraforged.mod.biome.utils.BiomeBuilder;
import com.terraforged.mod.biome.utils.BiomeUtils;
import com.terraforged.mod.featuremanager.matcher.dynamic.DynamicMatcher;
import net.fabricmc.fabric.api.biome.v1.OverworldClimate;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class BiomeBuilders {

    public static BiomeBuilder bryce() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.BADLANDS);
        builder.type(OverworldClimate.DRY);
        builder.filterFeatures(DynamicMatcher.config(TreeFeatureConfig.class));
        // dead bush
        DefaultBiomeFeatures.addDesertDeadBushes(builder.getSettings());
        return builder;
    }

    public static BiomeBuilder coldMarsh() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.SNOWY_TAIGA);
        builder.type(OverworldClimate.SNOWY);
        builder.downfall(0.5F);
        builder.temperature(0.225F);
        builder.category(Biome.Category.SWAMP);
        builder.precipitation(Biome.Precipitation.SNOW);
        builder.filterFeatures(DynamicMatcher.config(TreeFeatureConfig.class));
        deadBush(builder);
        denseGrass(builder);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder coldSteppe() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.GIANT_SPRUCE_TAIGA);
        builder.type(OverworldClimate.COOL);
        builder.precipitation(Biome.Precipitation.SNOW);
        builder.filterFeatures(
                DynamicMatcher.config(TreeFeatureConfig.class),
                DynamicMatcher.feature(Feature.FOREST_ROCK),
                // red/brown mushrooms
                DynamicMatcher.of(ConfiguredFeatures.RED_MUSHROOM_TAIGA),
                DynamicMatcher.of(ConfiguredFeatures.BROWN_MUSHROOM_TAIGA)
        );
        builder.temperature(0.25F);
        builder.downfall(0.025F);
        deadBush(builder);
        denseGrass(builder);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder firForest() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.TAIGA);
        builder.type(OverworldClimate.COOL);
        deadBush(builder);
        denseGrass(builder);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder flowerPlains() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.FLOWER_FOREST);
        builder.type(OverworldClimate.COOL);
        return builder;
    }

    public static BiomeBuilder frozenLake() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.FROZEN_RIVER);
        builder.type(OverworldClimate.SNOWY);
        builder.filterFeatures(DynamicMatcher.config(TreeFeatureConfig.class));
        return builder;
    }

    public static BiomeBuilder frozenMarsh() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.SNOWY_TAIGA);
        builder.type(OverworldClimate.SNOWY);
        builder.category(Biome.Category.SWAMP);
        builder.downfall(0.15F);
        builder.temperature(0.14999F);
        builder.precipitation(Biome.Precipitation.SNOW);
        builder.filterFeatures(DynamicMatcher.config(TreeFeatureConfig.class));
        return builder;
    }

    public static BiomeBuilder lake() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.RIVER);
        builder.type(OverworldClimate.COOL);
        builder.filterFeatures(DynamicMatcher.config(TreeFeatureConfig.class));
        return builder;
    }

    public static BiomeBuilder marshland() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.SWAMP);
        builder.type(OverworldClimate.COOL);
        builder.temperature(0.7F);
        builder.category(Biome.Category.SWAMP);
        builder.filterFeatures(DynamicMatcher.config(TreeFeatureConfig.class));
        deadBush(builder);
        ferns(builder);
        denseGrass(builder);
        DefaultBiomeFeatures.addSwampVegetation(builder.getSettings());
        return builder;
    }

    public static BiomeBuilder savannaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.SAVANNA);
        builder.type(OverworldClimate.TEMPERATE);
        builder.filterFeatures(DynamicMatcher.config(TreeFeatureConfig.class));
        deadBush(builder);
        denseGrass(builder);
        return builder;
    }

    public static BiomeBuilder shatteredSavannaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.SHATTERED_SAVANNA);
        builder.type(OverworldClimate.TEMPERATE);
        deadBush(builder);
        denseGrass(builder);
        return builder;
    }

    public static BiomeBuilder snowyFirForest() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.SNOWY_TAIGA);
        builder.type(OverworldClimate.SNOWY);
        builder.category(Biome.Category.ICY);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder snowyTaigaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.SNOWY_TAIGA);
        builder.type(OverworldClimate.SNOWY);
        builder.filterFeatures(DynamicMatcher.config(TreeFeatureConfig.class));
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder steppe() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.GIANT_SPRUCE_TAIGA);
        builder.filterFeatures(
                DynamicMatcher.config(TreeFeatureConfig.class),
                DynamicMatcher.feature(Feature.FOREST_ROCK),
                // red/brown mushrooms
                DynamicMatcher.of(ConfiguredFeatures.RED_MUSHROOM_TAIGA),
                DynamicMatcher.of(ConfiguredFeatures.BROWN_MUSHROOM_TAIGA)
        );
        builder.setParentKey(BiomeKeys.SHATTERED_SAVANNA_PLATEAU);
        builder.category(Biome.Category.SAVANNA);
        builder.copyAmbience(BiomeKeys.SAVANNA);
        builder.downfall(0.05F);
        builder.temperature(1.2F);
        deadBush(builder);
        denseGrass(builder);
        return builder;
    }

    public static BiomeBuilder stoneForest() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.JUNGLE);
        builder.type(OverworldClimate.TEMPERATE);
        builder.weight(2);
        return builder;
    }

    public static BiomeBuilder taigaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.TAIGA);
        builder.type(OverworldClimate.COOL);
        builder.filterFeatures(
                DynamicMatcher.config(TreeFeatureConfig.class),
                // red/brown mushrooms
                DynamicMatcher.of(ConfiguredFeatures.RED_MUSHROOM_TAIGA),
                DynamicMatcher.of(ConfiguredFeatures.BROWN_MUSHROOM_TAIGA)
        );
        deadBush(builder);
        denseGrass(builder);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder warmBeach() {
        BiomeBuilder builder = BiomeUtils.getBuilder(BiomeKeys.WARM_OCEAN);
        builder.type(OverworldClimate.TEMPERATE);
        builder.filterFeatures(DynamicMatcher.config(TreeFeatureConfig.class));
        builder.category(Biome.Category.BEACH);
        builder.temperature(1.0F);
        return builder;
    }

    private static void deadBush(BiomeBuilder builder) {
        // dead bush
        DefaultBiomeFeatures.addDesertDeadBushes(builder.getSettings());
    }

    private static void denseGrass(BiomeBuilder builder) {
        // plains grass
        builder.getSettings().feature(GenerationStep.Feature.VEGETAL_DECORATION, ConfiguredFeatures.PATCH_GRASS_PLAIN);
        // extra grass 1
        DefaultBiomeFeatures.addShatteredSavannaGrass(builder.getSettings());
        // extra grass 2
        DefaultBiomeFeatures.addSavannaGrass(builder.getSettings());
    }

    private static void ferns(BiomeBuilder builder) {
        DefaultBiomeFeatures.addLargeFerns(builder.getSettings());
    }
}
