package com.terraforged.mod.mixin.access;

import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MoreOptionsDialog.class)
public interface MoreOptionsDialogAccess {

    @Invoker void invokeSetGeneratorOptions(GeneratorOptions generatorOptions);
}
