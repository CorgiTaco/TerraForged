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

package com.terraforged.mod.client.gui.screen.page;

import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.element.TFTextBox;
import com.terraforged.mod.client.gui.page.BasePage;
import com.terraforged.mod.client.gui.screen.Instance;
import com.terraforged.mod.client.gui.screen.overlay.OverlayScreen;
import com.terraforged.mod.mixin.access.GeneratorTypeAccess;
import com.terraforged.mod.util.DataUtils;
import com.terraforged.mod.util.DimUtils;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class WorldPage extends BasePage {

    private final UpdatablePage preview;
    private final Instance instance;

    private CompoundTag worldSettings = null;
    private CompoundTag dimSettings = null;

    public WorldPage(Instance instance, UpdatablePage preview) {
        this.instance = instance;
        this.preview = preview;
    }

    @Override
    public String getTitle() {
        return GuiKeys.WORLD_SETTINGS.get();
    }

    @Override
    public void save() {

    }

    @Override
    public void init(OverlayScreen parent) {
        // re-sync settings from the settings object to the data structure
        worldSettings = getWorldSettings();
        dimSettings = getDimSettings();

        Column left = getColumn(0);
        addElements(left.left, left.top, left, worldSettings, true, left.scrollPane::addButton, this::update);

        addElements(left.left, left.top, left, dimSettings, true, left.scrollPane::addButton, this::update);
    }

    @Override
    public void onAddWidget(AbstractButtonWidget widget) {
        if (widget instanceof TFTextBox) {
            TFTextBox input = (TFTextBox) widget;
            input.setColorValidator(string -> Registry.BLOCK.containsId(new Identifier(string)));
        }
    }

    protected void update() {
        super.update();
        preview.apply(settings -> {
            DataUtils.fromNBT(worldSettings, settings.world);
            DataUtils.fromNBT(dimSettings, settings.dimensions);
        });
    }

    private CompoundTag getWorldSettings() {
        return instance.settingsData.getCompound("world");
    }

    private CompoundTag getDimSettings() {
        CompoundTag dimSettings = instance.settingsData.getCompound("dimensions");
        CompoundTag generators = dimSettings.getCompound("dimensions");
        for (String name : generators.getKeys()) {
            if (name.startsWith("#")) {
                Tag value = generators.get(name.substring(1));
                if (value instanceof StringTag) {
                    CompoundTag metadata = generators.getCompound(name);
                    metadata.put("options", getWorldTypes());
                }
            }
        }
        return dimSettings;
    }

    private static ListTag getWorldTypes() {
        ListTag options = new ListTag();
        for (GeneratorType type : GeneratorTypeAccess.getVALUES()) {
            String name = DimUtils.getDisplayString(type);
            Tag value = StringTag.of(name);
            options.add(value);
        }
        return options;
    }
}
