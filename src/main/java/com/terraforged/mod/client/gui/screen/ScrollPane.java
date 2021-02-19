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

package com.terraforged.mod.client.gui.screen;

import com.terraforged.mod.client.gui.IButtonHeight;
import com.terraforged.mod.client.gui.element.Element;
import com.terraforged.mod.client.gui.screen.overlay.OverlayRenderer;
import com.terraforged.mod.client.gui.screen.preview.Preview;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ScrollPane extends ElementListWidget<ScrollPane.Entry> implements OverlayRenderer {

    private boolean hovered = false;
    private boolean renderSelection = true;

    public ScrollPane(int slotHeightIn) {
        super(MinecraftClient.getInstance(), 0, 0, 0, 0, slotHeightIn);
        setRenderSelection(false);
    }

    public void addButton(AbstractButtonWidget button) {
        super.addEntry(new com.terraforged.mod.client.gui.screen.ScrollPane.Entry(button));
    }

    @Override
    public void renderOverlays(MatrixStack matrixStack, Screen screen, int x, int y) {
        for (com.terraforged.mod.client.gui.screen.ScrollPane.Entry entry : this.children()) {
            if (entry.isMouseOver(x, y) && entry.option.isMouseOver(x, y)) {
                AbstractButtonWidget button = entry.option;
                if (button instanceof Element) {
                    Element element = (Element) button;
                    if (!element.getTooltip().isEmpty()) {
                        screen.renderTooltip(matrixStack, element.getToolTipText(), x, y);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public int getRowWidth() {
        return width - 20;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        super.render(matrixStack, x, y, partialTicks);
        hovered = isMouseOver(x, y);
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.right;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (com.terraforged.mod.client.gui.screen.ScrollPane.Entry entry : children()) {
            if (!entry.isMouseOver(mouseX, mouseY) && entry.option.isFocused()) {
                entry.option.changeFocus(true);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double direction) {
        return hovered && super.mouseScrolled(x, y, direction);
    }

    @Override
    protected boolean isSelectedItem(int index) {
        return renderSelection && Objects.equals(getSelected(), children().get(index));
    }

    public class Entry extends ElementListWidget.Entry<com.terraforged.mod.client.gui.screen.ScrollPane.Entry> {

        public final AbstractButtonWidget option;

        public Entry(AbstractButtonWidget option) {
            this.option = option;
        }

        @Nullable
        public net.minecraft.client.gui.Element getFocused() {
            return option;
        }

        @Override
        public List<? extends net.minecraft.client.gui.Element> children() {
            return Collections.singletonList(option);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (super.mouseClicked(x, y, button)) {
                setSelected(this);
                option.active = true;
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            super.mouseReleased(x, y, button);
            return option.mouseReleased(x, y, button);
        }

        @Override
        public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            int optionWidth = Math.min(396, width);
            int padding = (width - optionWidth) / 2;
            option.x = left + padding;
            option.y = top;
            option.visible = true;
            option.setWidth(optionWidth);
            ((IButtonHeight) option).setHeight(height - 1);
            if (option instanceof Preview) {
                ((IButtonHeight) option).setHeight(option.getWidth());
            }
            option.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
