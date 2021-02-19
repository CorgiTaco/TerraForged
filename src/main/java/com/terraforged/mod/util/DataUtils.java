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

package com.terraforged.mod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.terraforged.engine.serialization.serializer.Deserializer;
import com.terraforged.engine.serialization.serializer.Serializer;
import com.terraforged.mod.Log;
import com.terraforged.mod.util.nbt.DynamicReader;
import com.terraforged.mod.util.nbt.DynamicWriter;
import com.terraforged.mod.util.nbt.NBTReader;
import com.terraforged.mod.util.nbt.NBTWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class DataUtils {

    public static void initDirs(File... dirs) {
        for (File dir : dirs) {
            if (!dir.exists() && !dir.mkdirs()) {
                Log.err("Unable to make directory {}", dir);
            }
        }
    }

    public static JsonElement toJson(CompoundTag tag) {
        Dynamic<Tag> input = new Dynamic<>(NbtOps.INSTANCE, tag);
        Dynamic<JsonElement> output = input.convert(JsonOps.INSTANCE);
        return output.getValue();
    }

    public static JsonElement toJson(Object o) {
        try {
            DynamicWriter<JsonElement> writer = new DynamicWriter<>(JsonOps.INSTANCE);
            Serializer.serialize(o, writer, false);
            return writer.get();
        } catch (Throwable t) {
            return new JsonObject();
        }
    }

    public static CompoundTag fromJson(JsonElement json) {
        Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);
        Dynamic<Tag> output = input.convert(NbtOps.INSTANCE);
        return (CompoundTag) output.getValue();
    }

    public static boolean fromJson(JsonElement json, Object o) {
        try {
            DynamicReader<JsonElement> reader = new DynamicReader<>(json, JsonOps.INSTANCE);
            Deserializer.deserialize(reader, o);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static CompoundTag toNBT(Object object) {
        return toNBT("", object);
    }

    public static CompoundTag toNBT(String owner, Object object) {
        try {
            NBTWriter writer = new NBTWriter();
            Serializer.serialize(object, writer, owner, true);
            return writer.compound();
        } catch (IllegalAccessException e) {
            return new CompoundTag();
        }
    }

    public static CompoundTag toCompactNBT(Object object) {
        try {
            NBTWriter writer = new NBTWriter();
            writer.readFrom(object);
            return stripMetadata(writer.compound());
        } catch (IllegalAccessException e) {
            return new CompoundTag();
        }
    }

    public static Stream<String> streamKeys(CompoundTag compound) {
        return compound.getKeys().stream()
                .filter(name -> !name.startsWith("#"))
                .sorted(Comparator.comparing(name -> compound.getCompound("#" + name).getInt("order")));
    }

    public static <T extends Tag> T stripMetadata(T tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compound = (CompoundTag) tag;
            List<String> keys = new LinkedList<>(compound.getKeys());
            for (String key : keys) {
                if (key.charAt(0) == '#') {
                    compound.remove(key);
                } else {
                    stripMetadata(compound.get(key));
                }
            }
        } else if (tag instanceof ListTag) {
            ListTag list = (ListTag) tag;
            for (int i = 0; i < list.size(); i++) {
                stripMetadata(list.get(i));
            }
        }
        return tag;
    }

    public static boolean fromNBT(CompoundTag settings, Object object) {
        try {
            NBTReader reader = new NBTReader(settings);
            return reader.writeTo(object);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
