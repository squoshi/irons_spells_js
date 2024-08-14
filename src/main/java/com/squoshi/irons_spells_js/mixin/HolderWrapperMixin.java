package com.squoshi.irons_spells_js.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = HolderWrapper.class, remap = false)
public interface HolderWrapperMixin {
    @Inject(method = "wrap", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isEmpty()Z"), cancellable = true)
    private static void iron_spells_js$returnDeferred(KubeJSContext cx, Object from, TypeInfo param, CallbackInfoReturnable<Holder<?>> cir, @Local Optional holder, @Local Registry registry, @Local ResourceLocation id) {
        if (holder.isEmpty()) cir.setReturnValue(DeferredHolder.create(registry.key(), id));
    }
}
