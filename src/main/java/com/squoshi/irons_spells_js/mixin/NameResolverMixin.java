package com.squoshi.irons_spells_js.mixin;

import com.probejs.compiler.formatter.NameResolver;
import com.probejs.compiler.formatter.SpecialTypes;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
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
    private static void kjs_irons_spells$injectTypes(CallbackInfo ci){
        NameResolver.putSpecialAssignments(ISSKJSUtils.AttributeHolder.class, () -> List.of("Special.Attribute"));
        NameResolver.putSpecialAssignments(ISSKJSUtils.SoundEventHolder.class, () -> List.of("Special.SoundEvent"));
        NameResolver.putSpecialAssignments(ISSKJSUtils.SchoolHolder.class, () -> List.of("Special.School"));
        NameResolver.putSpecialAssignments(ISSKJSUtils.SpellHolder.class, () -> List.of("Special.Spells"));
        SpecialTypes.assignRegistry(AbstractSpell.class, SpellRegistry.SPELL_REGISTRY_KEY);
        SpecialTypes.assignRegistry(SchoolType.class, SchoolRegistry.SCHOOL_REGISTRY_KEY);
    }
}
