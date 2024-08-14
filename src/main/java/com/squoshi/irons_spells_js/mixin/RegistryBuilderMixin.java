package com.squoshi.irons_spells_js.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.squoshi.irons_spells_js.IronsSpellsJSMod;
import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

@Mixin(value = RegistryBuilder.class, remap = false)
public class RegistryBuilderMixin<T> {
    @Unique
    private final static Set<String> kjs$ignoreModules = Set.of("java.base", "neoforge", "fml_loader", "kubejs");
    @Unique
    private final static Set<String> kjs$ignoreRegistries = Set.of("neoforge");
    @Unique
    private final static Set<String> kjs$visitedClasses = new HashSet<>();

    @Unique
    private static synchronized void kjs$discoverRegistry(String className) {
        kjs$visitedClasses.add(className);
        try {
            var clazz = Class.forName(className);
            for (var field : clazz.getDeclaredFields()) {
                if (field.getType() == ResourceKey.class
                        && Modifier.isPublic(field.getModifiers())
                        && Modifier.isStatic(field.getModifiers())
                        && field.getGenericType() instanceof ParameterizedType t1
                        && t1.getActualTypeArguments()[0] instanceof ParameterizedType t2
                ) {
                    var key = (ResourceKey) field.get(null);
                    var type = t2.getActualTypeArguments()[0];
                    var typeInfo = TypeInfo.of(type);
                    RegistryType.register(key, typeInfo);
                    IronsSpellsJSMod.LOGGER.info("Discovered Registry: {}, using class: {}", key, typeInfo);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "create", at = @At(value = "RETURN"))
    private void iron_spells_js$grabRegistries(CallbackInfoReturnable<Registry<T>> cir, @Local BaseMappedRegistry registry) {
        if (kjs$ignoreRegistries.contains(registry.key().location().getNamespace())) return;
        var stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stack) {
            if (kjs$ignoreModules.contains(stackTraceElement.getModuleName())) continue;
            var className = stackTraceElement.getClassName();
            if (kjs$visitedClasses.contains(className)) return;
            kjs$discoverRegistry(className);
        }
    }
}
