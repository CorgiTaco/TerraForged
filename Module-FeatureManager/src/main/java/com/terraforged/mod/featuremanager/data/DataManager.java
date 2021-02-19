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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.terraforged.mod.featuremanager.FeatureManager;
import com.terraforged.mod.server.IProvidersAdder;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class DataManager implements AutoCloseable {

    public static final Predicate<String> ANY = s -> true;
    public static final Predicate<String> NBT = s -> s.endsWith(".nbt");
    public static final Predicate<String> JSON = s -> s.endsWith(".json");

    private final ResourcePackManager packList;
    private final ResourceManager resourceManager;

    public DataManager(ResourceManager resourceManager, ResourcePackManager packList) {
        this.resourceManager = resourceManager;
        this.packList = packList;
    }

    @Override
    public void close() {
        packList.close();
    }

    public Resource getResource(Identifier location) throws IOException {
        return resourceManager.getResource(location);
    }

    public void forEach(String path, Predicate<String> matcher, ResourceVisitor<InputStream> consumer) {
        FeatureManager.LOG.debug("Input path: {}", path);
        for (Identifier location : resourceManager.findResources(path, matcher)) {
            FeatureManager.LOG.debug(" Location: {}", location);
            try (Resource resource = getResource(location)) {
                if (resource == null) {
                    continue;
                }
                try (InputStream inputStream = resource.getInputStream()) {
                    consumer.accept(location, inputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    public <T extends Object> void forEachTag(String type, List<Tag.Identified<T>> tags, Object registry, BiConsumer<Tag<T>, Set<T>> setter) {
//        JsonParser parser = new JsonParser();
//        String tagPath = "tags/" + type + "/";
//
//        for (Tag.Identified<T> tag : tags) {
//            try {
//                Set<T> set = new HashSet<>();
//                Identifier name = tag.getId();
//                String namespace = name.getNamespace();
//                String filepath = tagPath + name.getPath() + ".json";
//                Identifier path = new Identifier(namespace, filepath);
//
//                for (Resource resource : resourceManager.getAllResources(path)) {
//                    try (InputStream inputStream = resource.getInputStream()) {
//                        Reader reader = new BufferedReader(new InputStreamReader(inputStream));
//                        JsonElement element = parser.parse(reader);
//                        if (element.isJsonObject()) {
//                            TagLoader.loadTag(element.getAsJsonObject(), tag, registry, set);
//                        }
//                    }
//                }
//
//                setter.accept(tag, set);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void forEachJson(String path, ResourceVisitor<JsonElement> consumer) {
        JsonParser parser = new JsonParser();
        forEach(path, DataManager.JSON, (location, data) -> {
            Reader reader = new BufferedReader(new InputStreamReader(data));
            JsonElement element = parser.parse(reader);
            consumer.accept(location, element);
        });
    }

    public static DataManager of(File dir) {
        ReloadableResourceManagerImpl manager = new ReloadableResourceManagerImpl(ResourceType.SERVER_DATA);
        ResourcePackManager packList = new ResourcePackManager(ResourcePackProfile::new);

//        ((IProvidersAdder) packList).addPack(new ModDataPackFinder());
        // add global packs after mods so that they override
        ((IProvidersAdder) packList).addPack(new FolderDataPackFinder(dir));

        packList.scanPacks();
        packList.getProfiles().stream().map(ResourcePackProfile::createResourcePack).forEach(manager::addPack);

        return new DataManager(manager, packList);
    }
}
