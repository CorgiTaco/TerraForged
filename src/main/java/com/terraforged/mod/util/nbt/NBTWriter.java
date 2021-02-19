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

package com.terraforged.mod.util.nbt;

import com.terraforged.engine.serialization.serializer.AbstractWriter;
import com.terraforged.mod.client.gui.page.Page;
import net.minecraft.nbt.*;

public class NBTWriter extends AbstractWriter<Tag, CompoundTag, ListTag, NBTWriter> {

    public static final int TAG_LIST = 9;


    public CompoundTag compound() {
        return (CompoundTag) get();
    }

    @Override
    protected NBTWriter self() {
        return this;
    }

    @Override
    protected boolean isObject(Tag value) {
        return value.getType() == Page.TAG_COMPOUND;
    }

    @Override
    protected boolean isArray(Tag value) {
        return value.getType() == TAG_LIST;
    }

    @Override
    protected void add(CompoundTag parent, String key, Tag value) {
        parent.put(key, value);
    }

    @Override
    protected void add(ListTag parent, Tag value) {
        parent.add(value);
    }

    @Override
    protected CompoundTag createObject() {
        return new CompoundTag();
    }

    @Override
    protected ListTag createArray() {
        return new ListTag();
    }

    @Override
    protected Tag closeObject(CompoundTag o) {
        return o;
    }

    @Override
    protected Tag closeArray(ListTag a) {
        return a;
    }

    @Override
    protected Tag create(String value) {
        return StringTag.of(value);
    }

    @Override
    protected Tag create(int value) {
        return IntTag.of(value);
    }

    @Override
    protected Tag create(float value) {
        return FloatTag.of(value);
    }

    @Override
    protected Tag create(boolean value) {
        return ByteTag.of(value);
    }
}
