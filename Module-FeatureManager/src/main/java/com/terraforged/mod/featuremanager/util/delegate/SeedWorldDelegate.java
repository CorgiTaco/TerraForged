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

package com.terraforged.mod.featuremanager.util.delegate;

import java.util.stream.Stream;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.StructureFeature;

public class SeedWorldDelegate extends WorldDelegate<StructureWorldAccess> implements StructureWorldAccess {

    private long seed;

    public SeedWorldDelegate(StructureWorldAccess world) {
        super(world);
        seed = world.getSeed();
    }

    @Override
    public ServerWorld toServerWorld() {
        return delegate.toServerWorld();
    }

    @Override
    public void setDelegate(StructureWorldAccess world) {
        super.setDelegate(world);
        seed = world.getSeed();
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public Stream<? extends StructureStart<?>> getStructures(ChunkSectionPos pos, StructureFeature<?> structure) {
        return delegate.getStructures(pos, structure);
    }
}
