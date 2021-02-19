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

package com.terraforged.mod.api.material;

import com.terraforged.mod.Log;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class WGTags {

    public static final Tag<Block> STONE = tag("wg_stone");
    public static final Tag<Block> DIRT = tag("wg_dirt");
    public static final Tag<Block> CLAY = tag("wg_clay");
    public static final Tag<Block> SEDIMENT = tag("wg_sediment");
    public static final Tag<Block> ERODIBLE = tag("wg_erodible");
    public static final List<Tag<Block>> WG_TAGS = Collections.unmodifiableList(Arrays.asList(STONE, DIRT, CLAY, SEDIMENT, ERODIBLE));

    public static void init() {
        Log.info("Initializing tags");
    }

    private static Tag<Block> tag(String name) {
        return TagRegistry.block(new Identifier("c", name));
    }

    public static Predicate<BlockState> stone() {
        return toStatePredicate(STONE);
    }

    private static Predicate<BlockState> toStatePredicate(Tag<Block> tag) {
        return state -> tag.contains(state.getBlock());
    }

    public static void printTags() {
        for (Tag<Block> tag : WG_TAGS) {
//            Log.debug("World-Gen Tag: {}", tag.());
            for (Block block : tag.values()) {
                Log.debug(" - {}", Registry.BLOCK.getId(block));
            }
        }
    }
}
