package com.squoshi.irons_spells_js.util;

import dev.latvian.mods.kubejs.script.ConsoleJS;

import java.util.function.Consumer;

public class ISSKJSUtils {
	@SuppressWarnings("UnusedReturnValue")
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