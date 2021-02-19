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

package com.terraforged.mod.featuremanager.template.feature;

import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.featuremanager.FeatureManager;
import com.terraforged.mod.featuremanager.template.decorator.Decorator;
import com.terraforged.mod.featuremanager.template.decorator.DecoratorConfig;
import com.terraforged.mod.featuremanager.template.paste.Paste;
import com.terraforged.mod.featuremanager.template.paste.PasteType;
import com.terraforged.mod.featuremanager.template.template.Dimensions;
import com.terraforged.mod.featuremanager.template.template.Template;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;

import java.util.List;
import java.util.Random;

public class TemplateFeature extends Feature<TemplateFeatureConfig> {

    public TemplateFeature(String namespace) {
        super(TemplateFeatureConfig.CODEC);
        Registry.register(Registry.FEATURE, new Identifier(TerraForgedMod.MODID, namespace), this);
    }

    @Override
    public boolean generate(StructureWorldAccess world, ChunkGenerator generator, Random rand, BlockPos pos, TemplateFeatureConfig config) {
        BlockMirror mirror = nextMirror(rand);
        BlockRotation rotation = nextRotation(rand);
        return paste(world, rand, pos, mirror, rotation, config, config.decorator, Template.WORLD_GEN);
    }

    public static Template nextTemplate(List<Template> templates, Random random) {
        return templates.get(random.nextInt(templates.size()));
    }

    public static BlockMirror nextMirror(Random random) {
        return BlockMirror.values()[random.nextInt(BlockMirror.values().length)];
    }

    public static BlockRotation nextRotation(Random random) {
        return BlockRotation.values()[random.nextInt(BlockRotation.values().length)];
    }

    public static <T extends WorldAccess> boolean paste(StructureWorldAccess world, Random rand, BlockPos pos, BlockMirror mirror, BlockRotation rotation, TemplateFeatureConfig config, DecoratorConfig<T> decorator, PasteType pasteType) {
        if (config.templates.isEmpty()) {
            FeatureManager.LOG.warn("Empty template list for config: {}", config.name);
            return false;
        }

        Template template = nextTemplate(config.templates, rand);
        Dimensions dimensions = template.getDimensions(mirror, rotation);
        if (!config.type.getPlacement().canPlaceAt(world, pos, dimensions)) {
            return false;
        }

        Paste paste = pasteType.get(template);
        Placement placement = config.type.getPlacement();
        T buffer = decorator.createBuffer(world);
        if (paste.apply(buffer, pos, mirror, rotation, placement, config.paste)) {
            Identifier biome = world.method_31081(pos).map(RegistryKey::getValue).orElse(null);
            for (Decorator<T> d : decorator.getDecorators(biome)) {
                d.apply(buffer, rand);
            }
            return true;
        }

        return false;
    }
}
