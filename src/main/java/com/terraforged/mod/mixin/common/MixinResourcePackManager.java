package com.terraforged.mod.mixin.common;

import com.terraforged.mod.server.IProvidersAdder;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;


@Mixin(ResourcePackManager.class)
public class MixinResourcePackManager implements IProvidersAdder {
    @Shadow @Final private Set<ResourcePackProvider> providers;

    @Override
    public void addPack(ResourcePackProvider provider) {
        this.providers.add(provider);
    }
}
