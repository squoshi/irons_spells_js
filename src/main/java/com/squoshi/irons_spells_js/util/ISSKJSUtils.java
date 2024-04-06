package com.squoshi.irons_spells_js.util;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.squoshi.irons_spells_js.IronsSpellsJSMod.LOGGER;

public class ISSKJSUtils {
    public static <T> boolean safeCallback(Consumer<T> consumer, T value, String errorMessage) {
        try {
            consumer.accept(value);
        } catch (Throwable e) {
            ConsoleJS.STARTUP.error(errorMessage, e);
            return false;
        }
        return true;
    }

    public record AttributeHolder(ResourceLocation getLocation) implements ResourceHolder<AttributeHolder>{
        public static AttributeHolder of(Object o) {
            return ResourceHolder.of(o, AttributeHolder::new);
        }
    }

    public record SoundEventHolder(ResourceLocation getLocation) implements ResourceHolder<SoundEventHolder> {
        public static SoundEventHolder of(Object o) {
            return ResourceHolder.of(o, SoundEventHolder::new);
        }
    }

    public record SpellHolder(ResourceLocation getLocation) implements ResourceHolder<SpellHolder> {
        public static SpellHolder of(Object o) {
            return ResourceHolder.of(o, SpellHolder::new);
        }
    }

    public record SchoolHolder(ResourceLocation getLocation) implements ResourceHolder<SchoolHolder> {
        public static SchoolHolder of(Object o){
            return ResourceHolder.of(o, SchoolHolder::new);
        }
    }

    @SuppressWarnings("rawtypes")
    public interface ResourceHolder<T extends ResourceHolder<T>> {
        static <T extends ResourceHolder<T>> T of(Object o, Function<ResourceLocation, T> constructor){
            if (o instanceof String str) {
                return constructor.apply(new ResourceLocation(str));
            }
            if (o instanceof ResourceLocation rl) {
                return constructor.apply(rl);
            }
            if (o instanceof RegistryObject reg) {
                try {
                    var fieldName = reg.getClass().getDeclaredField("name");
                    fieldName.setAccessible(true);
                    var rl = fieldName.get(reg);
                    return constructor.apply((ResourceLocation) rl);
                } catch (Exception ignored){
                    // impossible
                }
            }
            if (o instanceof BuilderBase builder){
                return constructor.apply(builder.id);
            }
            throw new IllegalArgumentException("Object " + o + " of class " + o.getClass().getName() + " is not valid, should be a String or ResourceLocation.");
        }
    }
}
