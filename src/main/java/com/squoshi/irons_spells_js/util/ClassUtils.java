package com.squoshi.irons_spells_js.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentType;

import java.lang.reflect.Type;
import java.util.*;

public class ClassUtils {
    final private static Set<Class<?>> blacklistedClass = Set.of(MapCodec.class, DataComponentType.class);

    public static List<Type> getCommonTypes(Collection<Object> collection) {
        Iterator<Object> iterator = collection.iterator();
        List<Type> types = new ArrayList<>();

        if (iterator.hasNext()) {
            Object firstElement = iterator.next();

            types = getAllTypes(firstElement.getClass());

            if (!Collections.disjoint(types, blacklistedClass)) types.clear();
            if (types.size() <= 1) return types;

            while (iterator.hasNext()) {
                Object element = iterator.next();
                Iterator<Type> typeIterator = types.iterator();

                while (typeIterator.hasNext()) {
                    Type type = typeIterator.next();
                    if (type instanceof Class && !((Class<?>) type).isAssignableFrom(element.getClass())) {
                        typeIterator.remove();
                    }
                    if (types.size() == 1) return types;
                }
            }
        }
        return types;
    }

    private static List<Type> getAllTypes(Class<?> clazz) {
        List<Type> types = new ArrayList<>();
        while (clazz != null && clazz != Object.class && clazz != Record.class) {
            if (!clazz.isAnonymousClass() && !clazz.isSynthetic()) types.add(clazz);
            Collections.addAll(types, clazz.getGenericInterfaces());
            clazz = clazz.getSuperclass();
        }
        return types;
    }
}