package com.terraforged.mod.mixin.client;

import com.terraforged.mod.client.gui.IButtonHeight;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractButtonWidget.class)
public class MixinAbstractButtonWidget implements IButtonHeight {


    @Shadow protected int height;

    @Override
    public void setHeight(int height) {
        this.height = height;
    }
}
