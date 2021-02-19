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

package com.terraforged.mod.featuremanager.util;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.decorator.ConfiguredDecorator;
import net.minecraft.world.gen.feature.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FeatureDebugger {

    public static List<String> getErrors(ConfiguredFeature<?, ?> feature) {
        List<String> errors = new ArrayList<>();
        checkConfiguredFeature(feature, errors);
        return errors;
    }

    private static void checkConfiguredFeature(ConfiguredFeature<?, ?> feature, List<String> errors) {
        if (!validateConfiguredFeature(feature, errors)) {
            return;
        }

        if (feature.config instanceof DecoratedFeatureConfig) {
            decorated((DecoratedFeatureConfig) feature.config, errors);
            return;
        }

        // note SingleRandomFeature & SingleRandomFeatureConfig names a mixed up
        if (feature.config instanceof SimpleRandomFeatureConfig) {
            single((SimpleRandomFeatureConfig) feature.config, errors);
            return;
        }

        if (feature.config instanceof RandomBooleanFeatureConfig) {
            twoChoice((RandomBooleanFeatureConfig) feature.config, errors);
            return;
        }

        if (feature.config instanceof RandomFeatureConfig) {
            multi((RandomFeatureConfig) feature.config, errors);
            return;
        }
    }

    private static void decorated(DecoratedFeatureConfig config, List<String> errors) {
        checkConfiguredFeature(config.feature.get(), errors);
        checkDecorator(config.decorator, errors);
    }

    private static void single(SimpleRandomFeatureConfig config, List<String> errors) {
        for (Supplier<ConfiguredFeature<?, ?>> feature : config.features) {
            checkConfiguredFeature(feature.get(), errors);
        }
    }

    private static void twoChoice(RandomBooleanFeatureConfig config, List<String> errors) {
        checkConfiguredFeature(config.featureTrue.get(), errors);
        checkConfiguredFeature(config.featureFalse.get(), errors);
    }

    private static void multi(RandomFeatureConfig config, List<String> errors) {
        for (RandomFeatureEntry feature : config.features) {
            checkConfiguredFeature(feature.feature.get(), errors);
        }
    }

    private static boolean validateConfiguredFeature(ConfiguredFeature<?, ?> feature, List<String> list) {
        if (feature == null) {
            list.add("null  configured feature - this is bad D:");
            return false;
        }
        return checkFeature(feature.feature, list) && checkConfig(feature.config, list);
    }

    private static boolean checkFeature(Feature<?> feature, List<String> list) {
        if (feature == null) {
            list.add("null feature");
            return false;
        } else if (Registry.FEATURE.getId(feature) != null) {
            list.add("unregistered feature: " + feature.getClass().getName());
            return false;
        }
        return true;
    }

    private static boolean checkConfig(FeatureConfig config, List<String> list) {
        if (config == null) {
            list.add("null config");
            return false;
        }

        try {
//            config.serialize(JsonOps.INSTANCE);
            return true;
        } catch (Throwable t) {
            list.add("config: " + config.getClass().getName() + ", error: " + t.getMessage());
            return false;
        }
    }

    private static boolean checkDecorator(ConfiguredDecorator<?> decorator, List<String> list) {
        if (decorator == null) {
            list.add("null configured placement");
            return false;
        }

        boolean valid = true;
        // no longer exposed
//        if (decorator.decorator == null) {
//            valid = false;
//            list.add("null placement");
//        } else if (!ForgeRegistries.DECORATORS.containsValue(decorator.decorator)) {
//            list.add("unregistered placement: " + decorator.decorator.getClass().getName());
//        }

        if (decorator.getConfig() == null) {
            valid = false;
            list.add("null decorator config");
        } else {
            try {
//                decorator.config.serialize(JsonOps.INSTANCE);
            } catch (Throwable t) {
                valid = false;
                list.add("placement config: " + decorator.getConfig().getClass().getName() + ", error: " + t.getMessage());
            }
        }
        return valid;
    }
}
