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

package com.terraforged.mod.data.gen.feature;

import com.google.gson.JsonPrimitive;
import com.terraforged.mod.data.gen.FeatureInjectorProvider;
import com.terraforged.mod.feature.feature.BushFeature;
import com.terraforged.mod.featuremanager.FeatureSerializer;
import com.terraforged.mod.featuremanager.matcher.biome.BiomeMatcher;
import com.terraforged.mod.featuremanager.matcher.feature.FeatureMatcher;
import com.terraforged.mod.featuremanager.transformer.FeatureAppender;
import com.terraforged.mod.featuremanager.transformer.FeatureInjector;
import com.terraforged.mod.featuremanager.transformer.FeatureTransformer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.CountConfig;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.ConfiguredDecorator;
import net.minecraft.world.gen.decorator.CountExtraDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.NopeDecoratorConfig;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placer.DoublePlantPlacer;
import net.minecraft.world.gen.placer.SimpleBlockPlacer;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

import java.util.Arrays;

public class Shrubs {

    private static final String[] BIRCH = {"minecraft:birch*", "minecraft:tall_birch*"};
    private static final String[] FOREST = {"minecraft:forest", "minecraft:forest_hills", "minecraft:dark_forest", "minecraft:dark_forest_hills"};
    private static final String[] MARSH = {"terraforged:marshland", "terraforged:cold_marshland"};
    private static final String[] PLAINS = {"minecraft:birch*", "minecraft:plains", "minecraft:sunflower_plains", "minecraft:mountains"};
    private static final String[] STEPPE = {"minecraft:savanna", "minecraft:shattered_savanna", "terraforged:steppe"};
    private static final String[] COLD_STEPPE = {"terraforged:cold_steppe", "terraforged:cold_marshland"};
    private static final String[] TAIGA = {
            "minecraft:snowy_tundra",
            "minecraft:taiga",
            "minecraft:taiga_hills",
            "minecraft:wooded_mountains",
            "minecraft:taiga_mountains",
            "minecraft:snowy_taiga_mountains",
            "minecraft:gravelly_mountains",
            "minecraft:modified_gravelly_mountains",
            "terraforged:taiga_scrub",
            "terraforged:snowy_taiga_scrub",
    };

    public static void addInjectors(FeatureInjectorProvider provider) {
        // large bush
        addLargeBush(provider, "shrubs/birch_forest_bush", BIRCH, Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 0, 0.05F, 1);
        addLargeBush(provider, "shrubs/forest_bush", FOREST, Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 1, 0.05F, 1);

        // small bush
        addSmallBush(provider, "shrubs/marsh_bush", MARSH, Blocks.OAK_LOG, Blocks.BIRCH_LEAVES, 0.05F, 0.09F, 0.65F, 0, 0.3F, 1);
        addSmallBush(provider, "shrubs/plains_bush", PLAINS, Blocks.OAK_LOG, Blocks.BIRCH_LEAVES, 0.05F, 0.09F, 0.65F, 0, 0.05F, 1);
        addSmallBush(provider, "shrubs/steppe_bush", STEPPE, Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES, 0.06F, 0.08F, 0.7F, 0, 0.125F, 1);
        addSmallBush(provider, "shrubs/cold_steppe_bush", COLD_STEPPE, Blocks.SPRUCE_LOG, Blocks.OAK_LEAVES, 0.05F, 0.075F, 0.6F, 0, 0.125F, 1);
        addSmallBush(provider, "shrubs/taiga_scrub_bush", TAIGA, Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES, 0.05F, 0.075F, 0.6F, 0, 0.1F, 1);

        // forestFern
        // forestGrass
        // mountainGrass
        addBirchGrass(provider, "shrubs/birch_forest_grass");
        addForestGrass(provider, "shrubs/forest_grass");
    }

    private static void addLargeBush(FeatureInjectorProvider provider, String path, String[] biomes, Block log, Block leaves, int count, float chance, int extra) {
        provider.add(
                path,
                BiomeMatcher.of(provider.getContext(), biomes),
                FeatureMatcher.and(log, leaves),
                FeatureInjector.after(transform(
                        bush(count, chance, extra),
                        FeatureTransformer.builder()
                                .value(wrap(Blocks.JUNGLE_LEAVES), wrap(leaves))
                                .value(wrap(Blocks.JUNGLE_LOG), wrap(log))
                                .build()
                ))
        );
    }

    private static void addSmallBush(FeatureInjectorProvider provider, String path, String[] biomes, Block log, Block leaves, float air, float leaf, float size, int count, float chance, int extra) {
        provider.add(
                path,
                BiomeMatcher.of(provider.getContext(), biomes),
                FeatureMatcher.ANY,
                FeatureAppender.head(
                        GenerationStep.Feature.VEGETAL_DECORATION,
                        BushFeature.INSTANCE.configure(new BushFeature.Config(
                                log.getDefaultState(),
                                leaves.getDefaultState(),
                                air,
                                leaf,
                                size
                        )).decorate(Decorator.COUNT_EXTRA.configure(new CountExtraDecoratorConfig(
                                count,
                                chance,
                                extra
                        )))
                )
        );
    }

    private static void addBirchGrass(FeatureInjectorProvider provider, String path) {
        provider.add(
                path,
                BiomeMatcher.of(provider.getContext(), BIRCH),
                FeatureMatcher.ANY,
                FeatureAppender.head(
                        GenerationStep.Feature.VEGETAL_DECORATION,
                        Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(
                                Arrays.asList(
                                        patch(doubleBuilder(Blocks.TALL_GRASS).tries(56).cannotProject(), 0.8F),
                                        patch(doubleBuilder(Blocks.LILAC).tries(64).cannotProject(), 0.5F),
                                        patch(doubleBuilder(Blocks.LARGE_FERN).tries(48).cannotProject(), 0.3F),
                                        patch(singleBuilder(Blocks.FERN).tries(24).cannotProject(), 0.2F),
                                        patch(doubleBuilder(Blocks.PEONY).tries(32).cannotProject(), 0.1F)
                                ),
                                Feature.RANDOM_PATCH.configure(doubleBuilder(Blocks.TALL_GRASS).tries(48).cannotProject().build())
                        )).decorate(vegetationPlacement(7))
                )
        );
    }

    private static void addForestGrass(FeatureInjectorProvider provider, String path) {
        provider.add(
                path,
                BiomeMatcher.of(provider.getContext(), FOREST),
                FeatureMatcher.ANY,
                FeatureAppender.head(
                        GenerationStep.Feature.VEGETAL_DECORATION,
                        Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(
                                Arrays.asList(
                                        patch(singleBuilder(Blocks.GRASS).tries(56).cannotProject(), 0.5F),
                                        patch(doubleBuilder(Blocks.TALL_GRASS).tries(56).cannotProject(), 0.4F),
                                        patch(doubleBuilder(Blocks.LARGE_FERN).tries(48).cannotProject(), 0.2F),
                                        patch(singleBuilder(Blocks.FERN).tries(24).cannotProject(), 0.2F)
                                ),
                                Feature.RANDOM_PATCH.configure(singleBuilder(Blocks.GRASS).tries(48).cannotProject().build())
                        )).decorate(vegetationPlacement(7))
                )
        );
    }

    private static ConfiguredDecorator<?> vegetationPlacement(int count) {
        return Decorator.HEIGHTMAP_WORLD_SURFACE.configure(NopeDecoratorConfig.INSTANCE)
                .decorate(Decorator.COUNT.configure(new CountConfig(count)));
    }

    private static RandomFeatureEntry patch(RandomPatchFeatureConfig.Builder cluster, float chance) {
        return Feature.RANDOM_PATCH.configure(cluster.build()).withChance(chance);
    }

    private static RandomPatchFeatureConfig.Builder singleBuilder(Block block) {
        return new RandomPatchFeatureConfig.Builder(new SimpleBlockStateProvider(block.getDefaultState()), new SimpleBlockPlacer());
    }

    private static RandomPatchFeatureConfig.Builder doubleBuilder(Block block) {
        return new RandomPatchFeatureConfig.Builder(new SimpleBlockStateProvider(block.getDefaultState()), new DoublePlantPlacer());
    }

    private static JsonPrimitive wrap(Block block) {
        return new JsonPrimitive(Registry.BLOCK.getId(block).toString());
    }

    private static ConfiguredFeature<?, ?> bush(int count, float chance, int extra) {
        return bush(Decorator.COUNT_EXTRA.configure(new CountExtraDecoratorConfig(count, chance, extra)));
    }

    private static ConfiguredFeature<?, ?> bush(ConfiguredDecorator<?> placement) {
        return ConfiguredFeatures.JUNGLE_BUSH.decorate(placement);
    }

    private static ConfiguredFeature<?, ?> transform(ConfiguredFeature<?, ?> feature, FeatureTransformer transformer) {
        return FeatureSerializer.deserializeUnchecked(transformer.apply(FeatureSerializer.serialize(feature)));
    }
}
