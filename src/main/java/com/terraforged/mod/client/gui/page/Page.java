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

package com.terraforged.mod.client.gui.page;

import com.terraforged.mod.client.gui.IButtonHeight;
import com.terraforged.mod.client.gui.element.*;
import com.terraforged.mod.client.gui.screen.ScrollPane;
import com.terraforged.mod.client.gui.screen.overlay.OverlayRenderer;
import com.terraforged.mod.client.gui.screen.overlay.OverlayScreen;
import com.terraforged.mod.util.DataUtils;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Page implements Element, OverlayRenderer {

    public static final int TAG_COMPOUND = 10;
    public static final int TAG_INT         = 3;
    public static final int TAG_FLOAT       = 5;
    public static final int TAG_STRING      = 8;

    protected static final Runnable NO_CALLBACK = () -> {};

    private static final int SLIDER_HEIGHT = 20;
    private static final int SLIDER_PAD = 2;

    private final Column[] columns;
    private final float[] sizes;
    private final int hpad;
    private final int vpad;
    protected OverlayScreen parent;

    public Page(int hpad, int vpad, float... columnSizes) {
        this.hpad = hpad;
        this.vpad = vpad;
        this.sizes = columnSizes;
        this.columns = new Column[columnSizes.length];
    }

    public abstract void callback(Runnable runnable);

    public abstract void save();

    public abstract void init(OverlayScreen parent);

    @Override
    public void renderOverlays(MatrixStack matrixStack, Screen screen, int mouseX, int mouseY) {
        for (Column column : columns) {
            if (column.scrollPane.children().isEmpty()) {
                continue;
            }
            column.scrollPane.renderOverlays(matrixStack, screen, mouseX, mouseY);
        }
    }

    public void visit(Consumer<ScrollPane> consumer) {
        for (Column column : columns) {
            if (column.scrollPane.children().isEmpty()) {
                continue;
            }
            consumer.accept(column.scrollPane);
        }
    }

    public boolean action(Function<ScrollPane, Boolean> action) {
        boolean result = false;
        for (Column column : columns) {
            if (column.scrollPane.children().isEmpty()) {
                continue;
            }
            boolean b = action.apply(column.scrollPane);
            result = b || result;
        }
        return result;
    }

    public void close() {

    }

    public String getTitle() {
        return "";
    }

    public Column getColumn(int index) {
        return columns[index];
    }

    public final void initPage(int marginH, int marginV, OverlayScreen parent) {
        this.parent = parent;
        int top = marginV;
        int left = marginH;
        int pageWidth = parent.width - (marginH * 2);
        int pageHeight = parent.height;
        for (int i = 0; i < columns.length; i++) {
            int columnWidth = Math.max(0, Math.round(sizes[i] * pageWidth) - (2 * hpad));
            Column column = new Column(left, top, columnWidth, pageHeight, hpad, vpad);
            columns[i] = column;
            left += columnWidth > 0 ? columnWidth + (2 * hpad) : 0;
        }
        init(parent);
    }

    public void addElements(int x, int y, Column column, CompoundTag settings, Consumer<AbstractButtonWidget> consumer, Runnable callback) {
        addElements(x, y, column, settings, false, consumer, callback);
    }

    public void addElements(int x, int y, Column column, CompoundTag settings, boolean deep, Consumer<AbstractButtonWidget> consumer, Runnable callback) {
        AtomicInteger top = new AtomicInteger(y);

        DataUtils.streamKeys(settings).forEach(name -> {
            AbstractButtonWidget button = createButton(name, settings, callback);
            if (button != null) {
                button.setWidth(column.width);
                ((IButtonHeight) button).setHeight(SLIDER_HEIGHT);
                button.x = x;
                button.y = top.getAndAdd(SLIDER_HEIGHT + SLIDER_PAD);
                consumer.accept(button);
                onAddWidget(button);
            } else if (deep) {
                Tag child = settings.get(name);
                if (child == null || child.getType() != TAG_COMPOUND) {
                    return;
                }
                AbstractButtonWidget label = createLabel(name, settings);
                if (label != null) {
                    label.x = x;
                    label.y = top.getAndAdd(SLIDER_HEIGHT + SLIDER_PAD);
                    consumer.accept(label);
                }
                addElements(x, top.get(), column, (CompoundTag) child, true, consumer, callback);
            }
        });
    }

    public AbstractButtonWidget createButton(String name, CompoundTag value, Runnable callback) {
        Tag tag = value.get(name);
        if (tag == null) {
            return null;
        }

        byte type = tag.getType();
        if (type == TAG_INT) {
            if (isRand(name, value)) {
                return new TFRandButton(name, value).callback(callback);
            }
            if (hasLimit(name, value)) {
                return new TFSlider.BoundInt(name, value).callback(callback);
            }
            return new TFSlider.Int(name, value).callback(callback);
        } else if (type == TAG_FLOAT) {
            if (hasLimit(name, value)) {
                return new TFSlider.BoundFloat(name, value).callback(callback);
            }
            return new TFSlider.Float(name, value).callback(callback);
        } else if (hasOptions(name, value)) {
            return new TFToggle(name, value).callback(callback);
        } else if (type == TAG_STRING) {
            return new TFTextBox(name, value);
        } else {
            return null;
        }
    }

    public AbstractButtonWidget createLabel(String name, CompoundTag settings) {
        if (settings.getCompound("#" + name).contains("noname")) {
            return null;
        }
        return new TFLabel(com.terraforged.mod.client.gui.element.Element.getDisplayName(name, settings));
    }

    public void onAddWidget(AbstractButtonWidget widget) {

    }

    public static class Column {

        public final int left;
        public final int right;
        public final int top;
        public final int bottom;
        public final int width;
        public final int height;
        public final ScrollPane scrollPane;

        private Column(int left, int top, int width, int height, int vpad, int hpad) {
            this.left = left + vpad;
            this.right = left + width - vpad;
            this.top = top + hpad;
            this.bottom = height - hpad;
            this.width = width;
            this.height = height;
            this.scrollPane = new ScrollPane(25);
            this.scrollPane.updateSize(width, height, 30, height - 30);
            this.scrollPane.setLeftPos(this.left);
        }
    }

    private static boolean hasOptions(String name, CompoundTag value) {
        return value.getCompound("#" + name).contains("options");
    }

    private static boolean hasLimit(String name, CompoundTag value) {
        return value.getCompound("#" + name).contains("limit_lower") || value.getCompound("#" + name).contains("limit_upper");
    }

    private static boolean isRand(String name, CompoundTag value) {
        return value.getCompound("#" + name).contains("random");
    }
}
