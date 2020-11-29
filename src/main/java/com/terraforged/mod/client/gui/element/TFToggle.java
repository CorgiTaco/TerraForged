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

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class TFToggle extends TFButton {

    private final boolean noname;
    private final String prefix;
    private final String name;
    private final CompoundNBT value;
    private final ListNBT options;
    private final List<String> tooltip;

    private int index;
    private Runnable callback = () -> {};

    public TFToggle(String name, CompoundNBT value) {
        super(value.getString(name));
        this.name = name;
        this.value = value;
        this.prefix = Element.getDisplayName(name, value) + ": ";
        this.tooltip = Element.getToolTip(name, value);
        CompoundNBT meta = value.getCompound("#" + name);
        this.noname = meta.contains("noname");
        this.options = meta.getList("options", Constants.NBT.TAG_STRING);
        for (int i = 0; i < options.size(); i++) {
            String s = options.getString(i);
            if (s.equals(value.getString(name))) {
                index = i;
                break;
            }
        }

        if (noname) {
            setMessage(new StringTextComponent(value.getString(name)));
        } else {
            setMessage(new StringTextComponent(prefix + value.getString(name)));
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
        if (super.isValidClickButton(button)) {
            int direction = button == 0 ? 1 : -1;
            this.playDownSound(Minecraft.getInstance().getSoundHandler());
            this.onClick(mx, my, direction);
            return true;
        }
        return false;
    }

    private void onClick(double mouseX, double mouseY, int direction) {
        super.onClick(mouseX, mouseY);
        index += direction;
        if (index >= options.size()) {
            index = 0;
        } else if (index < 0) {
            index = options.size() - 1;
        }
        String option = options.getString(index);
        value.putString(name, option);
        if (noname) {
            setMessage(new StringTextComponent(option));
        } else {
            setMessage(new StringTextComponent(prefix + option));
        }
        callback.run();
    }
}
