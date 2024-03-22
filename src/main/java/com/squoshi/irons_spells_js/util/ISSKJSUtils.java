package com.squoshi.irons_spells_js.util;

import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

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

    public record AttributeHolder(ResourceLocation getLocation) {
        public static AttributeHolder of(Object o){
            if (o instanceof ResourceLocation rl){
                return new AttributeHolder(rl);
            }
            if (o instanceof String str){
                return new AttributeHolder(new ResourceLocation(str));
            }
            else throw new IllegalArgumentException("Attribute " + o + " is not valid, should be a String or ResourceLocation.");
        }
    }

    public record SoundEventHolder(ResourceLocation getLocation) {
        public static SoundEventHolder of(Object o){
            if (o instanceof ResourceLocation rl){
                return new SoundEventHolder(rl);
            }
            if (o instanceof String str){
                return new SoundEventHolder(new ResourceLocation(str));
            }
            else throw new IllegalArgumentException("SoundEvent " + o + " is not valid, should be a String or ResourceLocation.");
        }
    }
}
