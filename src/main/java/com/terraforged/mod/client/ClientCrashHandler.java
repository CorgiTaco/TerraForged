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

package com.terraforged.mod.client;

import com.terraforged.engine.concurrent.thread.ThreadPools;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.profiler.crash.CrashHandler;
import com.terraforged.mod.profiler.crash.CrashReportBuilder;
import net.minecraft.Bootstrap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.chunk.Chunk;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.StampedLock;

public class ClientCrashHandler implements CrashHandler {

    private final StampedLock lock = new StampedLock();

    @Override
    public void crash(Chunk chunk, TFChunkGenerator generator, Throwable t) {
        final long stamp = lock.tryWriteLock();
        if (!lock.validate(stamp)) {
            return;
        }

        try {
            ThreadPools.shutdownAll();
            CrashReport report = CrashReportBuilder.buildCrashReport(chunk, generator, t);
            ClientCrashHandler.displayCrashReport(report);
        } finally {
            lock.unlockWrite(stamp);
        }
    }
    private static void displayCrashReport(CrashReport report) {
        File file1 = new File(MinecraftClient.getInstance().runDirectory, "crash-reports");
        File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
        Bootstrap.println(report.asString());
        if (report.getFile() != null) {
            Bootstrap.println("#@!@# Game crashed! Crash report saved to: #@!@# " + report.getFile());
            Runtime.getRuntime().halt(-1);
        } else if (report.writeToFile(file2)) {
            Bootstrap.println("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
            Runtime.getRuntime().halt(-1);
        } else {
            Bootstrap.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            Runtime.getRuntime().halt(-2);
        }
    }
}
