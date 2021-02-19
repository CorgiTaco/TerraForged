package com.terraforged.mod.mixin.access;

import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadExecutor.class)
public interface ThreadExecutorAccess {


    @Invoker
    CompletableFuture<Void> invokeSubmitAsync(Runnable runnable);
}
