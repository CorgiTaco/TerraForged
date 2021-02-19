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

package com.terraforged.mod.featuremanager.data;

import com.terraforged.mod.Log;
import java.io.File;
import java.util.function.Consumer;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;

public class FolderDataPackFinder extends FileResourcePackProvider {

    public static final ResourcePackSource TF_FOLDER = ResourcePackSource.method_29486("pack.source.folder");

    public FolderDataPackFinder(File folderIn) {
        this(folderIn, TF_FOLDER);
    }

    public FolderDataPackFinder(File folderIn, ResourcePackSource decorator) {
        super(folderIn, decorator);
    }

    @Override
    public void register(Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory) {
        Log.debug("Searching for DataPacks...");
        super.register(packLogger(consumer), factory);
    }

    private static Consumer<ResourcePackProfile> packLogger(Consumer<ResourcePackProfile> consumer) {
        return packInfo -> {
            Log.debug("Adding datapack: {}", packInfo.getName());
            consumer.accept(packInfo);
        };
    }
}
