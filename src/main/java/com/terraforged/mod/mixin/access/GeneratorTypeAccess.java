package com.terraforged.mod.mixin.access;

import net.minecraft.client.world.GeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GeneratorType.class)
public interface GeneratorTypeAccess {
    
    
    @Accessor static List<GeneratorType> getVALUES() {
        throw new Error("Mixin did not apply");
    }
}
