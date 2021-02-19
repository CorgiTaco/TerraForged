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

package com.terraforged.mod.featuremanager.template;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;

public class StructureUtils {

    public static final List<StructureFeature<?>> SURFACE_STRUCTURES = StructureFeature.STRUCTURES.values().stream()
            .filter(structure -> structure.getGenerationStep() == GenerationStep.Feature.SURFACE_STRUCTURES)
            .collect(Collectors.toList());

    public static boolean hasOvergroundStructure(Chunk chunk) {
        Map<StructureFeature<?>, LongSet> references = chunk.getStructureReferences();
        for (StructureFeature<?> structure : SURFACE_STRUCTURES) {
            LongSet refs = references.get(structure);
            if (refs != null && refs.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStructure(Chunk chunk, StructureFeature<?> structure) {
        LongSet refs = chunk.getStructureReferences().get(structure.getName());
        return refs != null && refs.size() > 0;
    }
}
