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

package com.terraforged.mod.server;

import com.terraforged.mod.Log;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.featuremanager.data.FolderDataPackFinder;
import com.terraforged.mod.profiler.Profiler;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.minecraft.resource.ResourcePackManager;

import java.io.File;

public class ServerEvents {
    public static void serverStop() {
        ServerStopCallback.EVENT.register(minecraftServer -> {
            File dir = minecraftServer.getFile("dumps");
            Profiler.dump(dir);
        });


    }

    public static void addPackFinder(ResourcePackManager packList) {
        Log.info("Adding DataPackFinder");
        ((IProvidersAdder) packList).addPack(new FolderDataPackFinder(TerraForgedMod.DATAPACK_DIR));
    }
}
