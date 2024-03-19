package com.squoshi.irons_spells_js.compat.entityjs.entity.builder;

import com.squoshi.irons_spells_js.compat.entityjs.entity.SpellProjectileJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.item.ProjectileItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class SpellProjectileJSBuilder extends ProjectileEntityBuilder<SpellProjectileJS> {
    public transient Consumer<SpellProjectileJS.EntityProjectileWeaponContext> damageEntity;
    public SpellProjectileJSBuilder(ResourceLocation i) {
        super(i);
    }
    @Info(value = """
            Sets the logic for damaging the entities this spell hits.
            
            Example usage:
            ```javascript
            builder.damageEntity(context => {
            //Damage the entity with the provided context.
            });
            ```
            """)
    public SpellProjectileJSBuilder damageEntity(Consumer<SpellProjectileJS.EntityProjectileWeaponContext> f) {
        this.damageEntity = f;
        return this;
    }
    @Override
    public EntityType.EntityFactory<SpellProjectileJS> factory() {
        return (type, level) -> new SpellProjectileJS(this, type, level);
    }
}
