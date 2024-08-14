package com.squoshi.irons_spells_js.mixin;

import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.util.ID;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = KubeJSContext.class, remap = false)
public abstract class KubeJSContextMixin {
    @Shadow
    public abstract RegistryType<?> lookupRegistryType(TypeInfo type, Object from);

    @Inject(method = "internalJsToJavaLast", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/KubeJSContext;lookupRegistry(Ldev/latvian/mods/rhino/type/TypeInfo;Ljava/lang/Object;)Lnet/minecraft/core/Registry;"), cancellable = true)
    private void irons_spells_js$fixResourceKey(Object from, TypeInfo target, CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(ResourceKey.create(this.lookupRegistryType(target.param(0), from).key(), ID.mc(from)));
    }

}
