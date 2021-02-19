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

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;

import java.util.List;
import java.util.Objects;

public class TFToggle extends TFButton {

    private final boolean noname;
    private final String prefix;
    private final String name;
    private final CompoundTag value;
    private final ListTag options;
    private final List<String> tooltip;
    public static final int TAG_BYTE = 1;

    private int index;
    private Runnable callback = () -> {};

    public TFToggle(String name, CompoundTag value) {
        super(value.getString(name));
        this.name = name;
        this.value = value;
        this.prefix = Element.getDisplayName(name, value) + ": ";
        this.tooltip = Element.getToolTip(name, value);

        Tag selected = value.get(name);
        CompoundTag meta = value.getCompound("#" + name);
        this.noname = meta.contains("noname");
        this.options = (ListTag) meta.get("options");
        Objects.requireNonNull(options, "Missing options list");

        for (int i = 0; i < options.size(); i++) {
            Tag s = options.get(i);
            if (s.equals(selected)) {
                index = i;
                break;
            }
        }

        if (noname) {
            setMessage(new LiteralText(toString(value.get(name))));
        } else {
            setMessage(new LiteralText(prefix + toString(value.get(name))));
        }
    }

    public TFToggle callback(Runnable runnable) {
        this.callback = runnable;
        return this;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (isValidClickButton(button)) {
            int direction = button == 0 ? 1 : -1;
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(mx, my, direction);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 0 || button == 1;
    }

    private void onClick(double mouseX, double mouseY, int direction) {
        super.onClick(mouseX, mouseY);

        Tag option = options.get(increment(direction));
        value.put(name, option);

        if (noname) {
            setMessage(new LiteralText(toString(option)));
        } else {
            setMessage(new LiteralText(prefix + toString(option)));
        }

        callback.run();
    }

    private int increment(int direction) {
        index += direction;

        if (index >= options.size()) {
            index = 0;
        } else if (index < 0) {
            index = options.size() - 1;
        }

        return index;
    }

    private static String toString(Tag nbt) {
        if (nbt == null) {
            return "null";
        }
        if (nbt.getType() == TAG_BYTE) {
            return ((ByteTag) nbt).getByte() == 0b01 ? "true" : "false";
        }
        return nbt.asString();
    }
}
