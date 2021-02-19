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

package com.terraforged.mod.featuremanager.biome;

import com.google.common.base.Suppliers;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BiomeFeatures {

    public static final BiomeFeatures NONE = new BiomeFeatures();

    private static final List<List<BiomeFeature>> NO_FEATURES = Stream.of(GenerationStep.Feature.values())
            .map(stage -> Collections.<BiomeFeature>emptyList())
            .collect(Collectors.toList());

    private static final List<List<StructureFeature<?>>> NO_STRUCTURES = Stream.of(GenerationStep.Feature.values())
            .map(stage -> Collections.<StructureFeature<?>>emptyList())
            .collect(Collectors.toList());

    private static final Supplier<List<List<StructureFeature<?>>>> STRUCTURES = Suppliers.memoize(() -> {
        Map<GenerationStep.Feature, List<StructureFeature<?>>> map = Registry.STRUCTURE_FEATURE.stream()
                .collect(Collectors.groupingBy(StructureFeature::getGenerationStep));

        List<List<StructureFeature<?>>> list = new ArrayList<>();
        for (GenerationStep.Feature stage : GenerationStep.Feature.values()) {
            list.add(map.getOrDefault(stage, Collections.emptyList()));
        }

        return Collections.unmodifiableList(list);
    });

    private final List<List<BiomeFeature>> features;
    private final List<List<StructureFeature<?>>> structures;

    private BiomeFeatures() {
        features = NO_FEATURES;
        structures = NO_STRUCTURES;
    }

    public BiomeFeatures(Builder builder) {
        this.features = builder.compileFeatures();
        this.structures = STRUCTURES.get();
    }

    public List<List<BiomeFeature>> getFeatures() {
        return features;
    }

    public List<List<StructureFeature<?>>> getStructures() {
        return structures;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int size;
        private Map<GenerationStep.Feature, List<BiomeFeature>> features = Collections.emptyMap();

        public Builder add(GenerationStep.Feature stage, Collection<BiomeFeature> features) {
            for (BiomeFeature feature : features) {
                add(stage, feature);
            }
            return this;
        }

        public Builder add(GenerationStep.Feature stage, BiomeFeature feature) {
            if (features.isEmpty()) {
                features = new EnumMap<>(GenerationStep.Feature.class);
            }
            features.computeIfAbsent(stage, s -> new ArrayList<>()).add(feature);
            size++;
            return this;
        }

        private List<List<BiomeFeature>> compileFeatures() {
            List<List<BiomeFeature>> list = new ArrayList<>(size);
            for (GenerationStep.Feature stage : GenerationStep.Feature.values()) {
                list.add(features.getOrDefault(stage, Collections.emptyList()));
            }
            return list;
        }

        public BiomeFeatures build() {
            return new BiomeFeatures(this);
        }
    }
}
