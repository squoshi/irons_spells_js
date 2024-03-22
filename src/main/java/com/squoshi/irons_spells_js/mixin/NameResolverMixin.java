package com.squoshi.irons_spells_js.mixin;

import com.probejs.compiler.formatter.NameResolver;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(value = NameResolver.class, remap = false)
public class NameResolverMixin {
    @Inject(method = "init", at = @At(value = "TAIL"))
    private static void kjs_ironspells$injectTypes(CallbackInfo ci){
        NameResolver.putSpecialAssignments(ISSKJSUtils.AttributeHolder.class, () -> List.of("Special.Attribute"));
        NameResolver.putSpecialAssignments(ISSKJSUtils.SoundEventHolder.class, () -> List.of("Special.SoundEvent"));
    }
}
