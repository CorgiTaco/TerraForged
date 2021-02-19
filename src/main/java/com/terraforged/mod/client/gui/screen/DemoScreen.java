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

import com.terraforged.mod.LevelType;
import com.terraforged.mod.Log;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.screen.overlay.OverlayScreen;
import com.terraforged.mod.client.gui.screen.preview.PreviewPage;
import com.terraforged.mod.mixin.access.MoreOptionsDialogAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;

public class DemoScreen extends OverlayScreen {

    private static final int MESSAGE_COLOR = 0x00DDAA;
    public static final String LOGS = "";

    private final CreateWorldScreen parent;
    private final GeneratorOptions inputSettings;

    private final Instance instance;
    private final PreviewPage preview;
    private final String message = "TF-" + 0 + " | Settings not available in this version!";

    private GeneratorOptions outputSettings;

    public DemoScreen(CreateWorldScreen parent, GeneratorOptions settings) {
        this.parent = parent;
        this.inputSettings = settings;
        this.outputSettings = settings;
        this.instance = new Instance(ConfigScreen.getInitialSettings(settings));
        this.preview = new PreviewPage(instance.settings, ConfigScreen.getSeed(parent), true);
    }

    @Override
    public void init() {
        preview.initPage(0, 30, this);

        int buttonsCenter = width / 2;
        int buttonWidth = 50;
        int buttonHeight = 20;
        int buttonPad = 2;
        int buttonsRow = height - 25;

        // -52
        addButton(new ButtonWidget(buttonsCenter - buttonWidth - buttonPad, buttonsRow, buttonWidth, buttonHeight, GuiKeys.CANCEL.getText(), b -> onClose()));

        // +2
        addButton(new ButtonWidget(buttonsCenter + buttonPad, buttonsRow, buttonWidth, buttonHeight, GuiKeys.DONE.getText(), b -> {
            Log.debug("Updating generator settings...");
            DynamicRegistryManager.Impl registries = parent.moreOptionsDialog.method_29700();
            outputSettings = LevelType.updateOverworld(inputSettings, registries, instance.settings);
            Log.debug("Updating seed...");
            ConfigScreen.setSeed(parent, preview.getSeed());
            onClose();
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        preview.visit(pane -> pane.render(matrixStack, mouseX, mouseY, partialTicks));
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (client == null) {
            return;
        }
        client.textRenderer.draw(matrixStack, message, 5, 10, MESSAGE_COLOR);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return preview.action(pane -> pane.mouseClicked(mouseX, mouseY, button)) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return preview.action(pane -> pane.mouseReleased(mouseX, mouseY, button)) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return preview.action(pane -> pane.mouseDragged(x, y, button, dx, dy)) || super.mouseDragged(x, y, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double direction) {
        return preview.action(pane -> pane.mouseScrolled(x, y, direction)) || super.mouseScrolled(x, y, direction);
    }

    @Override
    public void onClose() {
        Log.debug("Returning to parent screen");
        preview.close();
        MinecraftClient.getInstance().openScreen(parent);
        ((MoreOptionsDialogAccess) parent.moreOptionsDialog).invokeSetGeneratorOptions(outputSettings);
    }
}
