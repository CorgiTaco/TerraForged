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

package com.terraforged.mod.client.gui.element;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface Element {

    AtomicInteger ID_COUNTER = new AtomicInteger(0);

    default Text getToolTipText() {
        BaseText base = new LiteralText("");
        for (String s : getTooltip()) {
            base.append(new LiteralText(s));
        }
        return base;
    }

    default List<String> getTooltip() {
        return Collections.emptyList();
    }

    static int nextID() {
        return ID_COUNTER.getAndAdd(1);
    }

    static String getDisplayName(String name, CompoundTag value) {
        if (name.contains(":")) {
            return name;
        }
        String key = getDisplayKey(name, value);
        if (key.endsWith(".")) {
            return "";
        }
        return I18n.translate(key);
    }

    static List<String> getToolTip(String name, CompoundTag value) {
        String key = getCommentKey(name, value);
        if (key.endsWith(".")) {
            return Collections.emptyList();
        }
        return Collections.singletonList(I18n.translate(key));
    }

    static String getDisplayKey(String name, CompoundTag value) {
        return "display.terraforged." + getKey(name, value);
    }

    static String getCommentKey(String name, CompoundTag value) {
        return "tooltip.terraforged." + getKey(name, value);
    }

    static String getKey(String name, CompoundTag value) {
        return value.getCompound("#" + name).getString("key");
    }
}
