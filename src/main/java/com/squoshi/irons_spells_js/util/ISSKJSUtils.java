package com.squoshi.irons_spells_js.util;

import dev.latvian.mods.kubejs.util.ConsoleJS;

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
}
