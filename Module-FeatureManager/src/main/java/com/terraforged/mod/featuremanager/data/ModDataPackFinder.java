///*
// * MIT License
// *
// * Copyright (c) 2020 TerraForged
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//package com.terraforged.mod.featuremanager.data;
//
//import com.terraforged.mod.featuremanager.FeatureManager;
//import net.fabricmc.loader.api.FabricLoader;
//import net.minecraft.resource.ResourcePack;
//import net.minecraft.resource.ResourcePackProfile;
//import net.minecraft.resource.ResourcePackProvider;
//import net.minecraft.resource.ResourcePackSource;
//
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
//public class ModDataPackFinder implements ResourcePackProvider {
//
//    @Override
//    public void register(Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory) {
//        for (ModFileInfo info : FabricLoader.getInstance().get.get().getModFiles()) {
//            ResourcePackProfile packInfo = ResourcePackProfile.of(
//                    info.getFile().getFileName(),
//                    true,
//                    new ModSupplier(info.getFile()),
//                    factory,
//                    ResourcePackProfile.InsertionPosition.TOP,
//                    ResourcePackSource.PACK_SOURCE_BUILTIN
//            );
//
//            if (packInfo != null) {
//                FeatureManager.LOG.debug(" Adding Mod RP: {}", packInfo.getName());
//                consumer.accept(packInfo);
//            }
//        }
//    }
//
//    private static class ModSupplier implements Supplier<ResourcePack> {
//
//        private final ModFile modFile;
//
//        private ModSupplier(ModFile modFile) {
//            this.modFile = modFile;
//        }
//
//        @Override
//        public ResourcePack get() {
//            return new ModFileResourcePack(modFile);
//        }
//    }
//}
