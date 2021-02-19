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

package com.terraforged.mod;

import com.terraforged.engine.Engine;
import com.terraforged.mod.api.material.WGTags;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.mixin.access.GeneratorTypeAccess;
import com.terraforged.mod.server.ServerEvents;
import com.terraforged.mod.server.command.TerraCommand;
import com.terraforged.mod.util.DataUtils;
import com.terraforged.mod.util.Environment;
import net.fabricmc.api.ModInitializer;

import java.io.File;

public class TerraForgedMod implements ModInitializer {

    public static final String MODID = "terraforged";
    public static final File CONFIG_DIR = new File("config", MODID).getAbsoluteFile();
    public static final File PRESETS_DIR = new File(CONFIG_DIR, "presets");
    public static final File DATAPACK_DIR = new File(CONFIG_DIR, "datapacks");

    public static void setup() {
        Log.info("Common setup");
        DataUtils.initDirs(PRESETS_DIR, DATAPACK_DIR);
        TerraCommand.init();
        ConfigManager.init();
        RegistrationEvents.registerCodecs();
        RegistrationEvents.registerMissingBiomeTypes();
    }

    public static void complete() {
        // log version because people do dumb stuff like renaming jars
        Log.info("Loaded TerraForged version {}");
    }

    @Override
    public void onInitialize() {
        Environment.log();
        ServerEvents.serverStop();

        Engine.init();
        WGTags.init();

        setup();
        complete();
        RegistrationEvents.registerFeatures();
        RegistrationEvents.registerDecorators();
        TerraCommand.register();
        ModBiomes.register();
        GeneratorTypeAccess.getVALUES().add(LevelType.TERRAFORGED);
    }

//    public static class ForgeEvents {
//        @SubscribeEvent
//        public static void update(TagsUpdatedEvent event) {
//            Log.info("Tags Reloaded");
//            WGTags.printTags();
//        }
//    }
}
