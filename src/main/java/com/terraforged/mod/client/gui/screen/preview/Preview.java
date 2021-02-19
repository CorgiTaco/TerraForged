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

package com.terraforged.mod.client.gui.screen.preview;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.cache.CacheEntry;
import com.terraforged.engine.concurrent.cache.CacheManager;
import com.terraforged.engine.concurrent.thread.ThreadPool;
import com.terraforged.engine.concurrent.thread.ThreadPools;
import com.terraforged.engine.settings.Settings;
import com.terraforged.engine.tile.Size;
import com.terraforged.engine.tile.Tile;
import com.terraforged.engine.tile.gen.TileGenerator;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.continent.MutableVeci;
import com.terraforged.engine.world.continent.SpawnType;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.util.DataUtils;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

public class Preview extends ButtonWidget {

    private static final int FACTOR = 4;
    public static final int SIZE = Size.chunkToBlock(1 << FACTOR);
    private static final float[] LEGEND_SCALES = {1, 0.9F, 0.75F, 0.6F};

    private final int offsetX;
    private final int offsetZ;
    private final ThreadPool threadPool = ThreadPools.createDefault();
    private final Random random = new Random(System.currentTimeMillis());
    private final PreviewSettings previewSettings = new PreviewSettings();
    private final NativeImageBackedTexture texture = new NativeImageBackedTexture(new NativeImage(SIZE, SIZE, true));

    private int seed;
    private long lastUpdate = 0L;
    private Tile tile = null;
    private CacheEntry<Tile> task = null;
    private CompoundTag lastWorldSettings = null;
    private CompoundTag lastPreviewSettings = null;

    private Settings settings = new Settings();
    private MutableVeci center = new MutableVeci();

    private String hoveredCoords = "";
    private String[] values = {"", "", ""};
    private String[] labels = {GuiKeys.PREVIEW_AREA.get(), GuiKeys.PREVIEW_TERRAIN.get(), GuiKeys.PREVIEW_BIOME.get()};

    public Preview(int seed) {
        super(0, 0, 0, 0, new LiteralText(""), b -> {});
        this.seed = seed == -1 ? random.nextInt() : seed;
        this.offsetX = 0;
        this.offsetZ = 0;
    }

    public int getSeed() {
        return seed;
    }

    public void regenerate() {
        this.seed = random.nextInt();
        this.lastWorldSettings = null;
        this.lastPreviewSettings = null;
    }

    public void close() {
        texture.close();
        threadPool.shutdown();
        CacheManager.get().clear();
    }

    public boolean click(double mx, double my) {
        if (updateLegend((int) mx, (int) my) && !hoveredCoords.isEmpty()) {
            super.playDownSound(MinecraftClient.getInstance().getSoundManager());
            MinecraftClient.getInstance().keyboard.setClipboard(hoveredCoords);
            return true;
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mx, int my, float partialTicks) {
        this.height = getSize();

        preRender();

        texture.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableRescaleNormal();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        DrawableHelper.drawTexture(matrixStack, x, y, 0, 0, width, height, width, height);
        RenderSystem.disableRescaleNormal();

        updateLegend(mx, my);

        renderLegend(matrixStack, mx, my, labels, values, x, y + width, 10, 0xFFFFFF);
    }

    public void update(Settings settings, CompoundTag prevSettings) {
        long time = System.currentTimeMillis();
        if (time - lastUpdate < 20) {
            return;
        }


        // Dumb way of preventing the image repainting when nothing has changed
        CompoundTag previewSettings = prevSettings.copy();
        CompoundTag worldSettings = DataUtils.toCompactNBT(settings);
        if (Objects.equals(lastWorldSettings, worldSettings) && Objects.equals(lastPreviewSettings, previewSettings)) {
            return;
        }

        lastUpdate = time;
        lastWorldSettings = worldSettings;
        lastPreviewSettings = previewSettings;

        DataUtils.fromNBT(prevSettings, previewSettings);
        settings.world.seed = seed;

        task = generate(settings, prevSettings);
    }

    private int getSize() {
        return width;
    }

    private void preRender() {
        if (task != null && task.isDone()) {
            try {
                tile = task.get();
                render(tile);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                task = null;
            }
        }
    }

    private void render(Tile tile) {
        NativeImage image = texture.getImage();
        if (image == null) {
            return;
        }

        RenderMode renderer = previewSettings.display;
        Levels levels = new Levels(settings.world);

        int stroke = 2;
        int width = tile.getBlockSize().size;

        tile.iterate((cell, x, z) -> {
            if (x < stroke || z < stroke || x >= width - stroke || z >= width - stroke) {
                image.setPixelColor(x, z, Color.BLACK.getRGB());
            } else {
                image.setPixelColor(x, z, renderer.getColor(cell, levels));
            }
        });

        texture.upload();
    }

    private CacheEntry<Tile> generate(Settings settings, CompoundTag prevSettings) {
        DataUtils.fromNBT(prevSettings, previewSettings);
        settings.world.seed = seed;
        this.settings = settings;

        CacheManager.get().clear();
        GeneratorContext context = GeneratorContext.createNoCache(settings);
        if (settings.world.properties.spawnType == SpawnType.CONTINENT_CENTER) {
            long center = context.worldGenerator.get().getHeightmap().getContinent().getNearestCenter(offsetX, offsetZ);
            this.center.x = PosUtil.unpackLeft(center);
            this.center.z = PosUtil.unpackRight(center);
        } else {
            center.x = 0;
            center.z = 0;
        }

        TileGenerator renderer = TileGenerator.builder()
                .pool(threadPool)
                .size(FACTOR, 0)
                .factory(context.worldGenerator.get())
                .batch(6)
                .build();

        return renderer.getAsync(center.x, center.z, getZoom(), false);
    }

    private boolean updateLegend(int mx ,int my) {
        if (tile != null) {
            int left = this.x;
            int top = this.y;
            float size = this.width;

            int zoom = getZoom();
            int width = Math.max(1, tile.getBlockSize().size * zoom);
            int height = Math.max(1, tile.getBlockSize().size * zoom);
            values[0] = width + "x" + height;
            if (mx >= left && mx <= left + size && my >= top && my <= top + size) {
                float fx = (mx - left) / size;
                float fz = (my - top) / size;
                int ix = NoiseUtil.round(fx * tile.getBlockSize().size);
                int iz = NoiseUtil.round(fz * tile.getBlockSize().size);
                Cell cell = tile.getCell(ix, iz);
                values[1] = getTerrainName(cell);
                values[2] = getBiomeName(cell);

                int dx = (ix - (tile.getBlockSize().size / 2)) * zoom;
                int dz = (iz - (tile.getBlockSize().size / 2)) * zoom;

                hoveredCoords = (center.x + dx) + ":" + (center.z + dz);
                return true;
            } else {
                hoveredCoords = "";
            }
        }
        return false;
    }

    private float getLegendScale() {
        int index = MinecraftClient.getInstance().options.guiScale - 1;
        if (index < 0 || index >= LEGEND_SCALES.length) {
            // index=-1 == GuiScale(AUTO) which is the same as GuiScale(4)
            // values above 4 don't exist but who knows what mods might try set it to
            // in both cases use the smallest acceptable scale
            index = LEGEND_SCALES.length - 1;
        }
        return LEGEND_SCALES[index];
    }

    private void renderLegend(MatrixStack matrixStack, int mx, int my, String[] labels, String[] values, int left, int top, int lineHeight, int color) {
        float scale = getLegendScale();

        RenderSystem.pushMatrix();
        RenderSystem.translatef(left + 3.75F * scale, top - lineHeight * (3.2F * scale), 0);
        RenderSystem.scalef(scale, scale, 1);

        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        int spacing = 0;
        for (String s : labels) {
            spacing = Math.max(spacing, renderer.getWidth(s));
        }

        float maxWidth = (width - 4) / scale;
        for (int i = 0; i < labels.length && i < values.length; i++) {
            String label = labels[i];
            String value = values[i];

            while (value.length() > 0 && spacing + renderer.getWidth(value) > maxWidth) {
                value = value.substring(0, value.length() - 1);
            }

            drawStringWithShadow(matrixStack, renderer, label, 0, i * lineHeight, color);
            drawStringWithShadow(matrixStack, renderer, value, spacing, i * lineHeight, color);
        }

        RenderSystem.popMatrix();

        if (PreviewSettings.showCoords && !hoveredCoords.isEmpty()) {
            drawCenteredString(matrixStack, renderer, hoveredCoords, mx, my - 10, 0xFFFFFF);
        }
    }

    private int getZoom() {
        return NoiseUtil.round(1.5F * (101 - previewSettings.zoom));
    }

    private static String getTerrainName(Cell cell) {
        if (cell.terrain.isRiver()) {
            return "river";
        }
        return cell.terrain.getName().toLowerCase();
    }

    private static String getBiomeName(Cell cell) {
        String terrain = cell.terrain.getName().toLowerCase();
        if (terrain.contains("ocean")) {
            if (cell.temperature < 0.3) {
                return "cold_" + terrain;
            }
            if (cell.temperature > 0.6) {
                return "warm_" + terrain;
            }
            return terrain;
        }
        if (terrain.contains("river")) {
            return "river";
        }
        return cell.biome.name().toLowerCase();
    }
}
