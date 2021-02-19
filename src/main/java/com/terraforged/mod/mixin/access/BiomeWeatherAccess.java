package com.terraforged.mod.mixin.access;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Biome.Weather.class)
public interface BiomeWeatherAccess {


    @Accessor float getDownfall();
    @Accessor float getTemperature();
    @Accessor Biome.Precipitation getPrecipitation();
    @Accessor Biome.TemperatureModifier getTemperatureModifier();
}
