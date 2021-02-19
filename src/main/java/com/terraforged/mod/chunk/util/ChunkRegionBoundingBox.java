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

package com.terraforged.mod.chunk.util;

import com.terraforged.mod.Log;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Vec3i;

public class ChunkRegionBoundingBox extends BlockBox {

    private static final int INCLUSIVE_SIZE = 15;
    private static final String ERROR_MESSAGE = "Structure {} attempted to change the world-gen region bounds to an unsafe location/size. Original: {}, Altered: {}";

    private final int x1, z1, x2, z2;
    private final int rx1, rz1, rx2, rz2;

    private String structure = "unknown";

    public ChunkRegionBoundingBox(int chunkX, int chunkZ, int chunkRadius) {
        super(chunkX << 4, chunkZ << 4, (chunkX << 4) + INCLUSIVE_SIZE, (chunkZ << 4) + INCLUSIVE_SIZE);
        this.x1 = minX;
        this.z1 = minZ;
        this.x2 = maxX;
        this.z2 = maxZ;
        this.rx1 = (chunkX - chunkRadius) << 4;
        this.rz1 = (chunkZ - chunkRadius) << 4;
        this.rx2 = ((chunkX + chunkRadius) << 4) + INCLUSIVE_SIZE;
        this.rz2 = ((chunkZ + chunkRadius) << 4) + INCLUSIVE_SIZE;
    }

    public ChunkRegionBoundingBox init(StructureStart<?> start) {
        return init(start.getFeature().getName());
    }

    public ChunkRegionBoundingBox init(String name) {
        minX = x1;
        minZ = z1;
        maxX = x2;
        maxZ = z2;
        minY = 1;
        maxY = 512;
        structure = name;
        return this;
    }

    @Override
    public void encompass(BlockBox other) {
        super.encompass(other);
        validate();
    }

    @Override
    public void move(int x, int y, int z) {
        super.move(x, y, z);
        validate();
    }

    @Override
    public void move(Vec3i vec) {
        super.move(vec);
        validate();
    }

    @Override
    public String toString() {
        return "BoundingBox{"
                + "minX=" + minX + ", minZ=" + minZ + ", maxX=" + maxX + ", maxZ=" + maxZ
                + ", sizeChunks=" + getSizeChunks()
                + "}";
    }

    private String getSizeChunks() {
        int chunkSizeX = (maxX >> 4) - (minX >> 4);
        int chunkSizeZ = (maxZ >> 4) - (minZ >> 4);
        return chunkSizeX + "x" + chunkSizeZ;
    }

    private void validate() {
        if (!contains(minX, minZ, rx1, rz1, rx2, rz2) || !contains(maxX, maxZ, rx1, rz1, rx2, rz2)) {
            String from = toString(rx1, rz1, rx2, rz2);
            String to = toString(minX, minZ, maxX, maxZ);
            Log.warn(ERROR_MESSAGE, structure, from, to);
        }
    }

    public static boolean contains(int x, int z, BlockBox bounds) {
        return contains(x, z, bounds.minX, bounds.minZ, bounds.maxX, bounds.maxZ);
    }

    public static boolean contains(int x, int z, int minX, int minZ, int maxX, int maxZ) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public static String toString(int minX, int minZ, int maxX, int maxZ) {
        return "Bounds{minX=" + minX + ",minZ=" + minZ + ",maxX=" + maxX + ",maxZ=" + maxZ + "}";
    }
}
