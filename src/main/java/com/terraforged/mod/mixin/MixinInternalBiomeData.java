package com.terraforged.mod.mixin;

import com.terraforged.mod.biome.provider.BiomeHelper;
import net.fabricmc.fabric.api.biome.v1.OverworldClimate;
import net.fabricmc.fabric.impl.biome.InternalBiomeData;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InternalBiomeData.class)
public class MixinInternalBiomeData {


    @Inject(method = "addOverworldContinentalBiome", at = @At("RETURN"))
    private static void trackAllOverworldBiomes(OverworldClimate climate, RegistryKey<Biome> biome, double weight, CallbackInfo ci) {
        BiomeHelper.OVERWORLD_BIOMES.put(biome, weight);
    }

}
