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

package com.terraforged.mod.api.biome.surface;

import com.terraforged.mod.api.chunk.column.DecoratorContext;
import com.terraforged.mod.biome.TFBiomeContainer;
import com.terraforged.engine.world.climate.Climate;
import com.terraforged.engine.world.heightmap.Levels;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.DimensionSettings;

public class SurfaceContext extends DecoratorContext implements AutoCloseable {

    public final long seed;
    public final BlockState solid;
    public final BlockState fluid;
    public final TFBiomeContainer biomes;
    public final SurfaceChunk buffer;
    public final CachedSurface cached = new CachedSurface();

    public double noise;

    public SurfaceContext(SurfaceChunk buffer, TFBiomeContainer biomes, Levels levels, Climate climate, DimensionSettings settings, long seed) {
        super(buffer, levels, climate);
        this.solid = settings.getDefaultBlock();
        this.fluid = settings.getDefaultFluid();
        this.buffer = buffer;
        this.biomes = biomes;
        this.seed = seed;
    }
}
