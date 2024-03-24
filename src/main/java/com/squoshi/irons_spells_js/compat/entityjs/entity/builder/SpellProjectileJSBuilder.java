package com.squoshi.irons_spells_js.compat.entityjs.entity.builder;

import com.squoshi.irons_spells_js.compat.entityjs.entity.SpellProjectileJS;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.function.Consumer;

public class SpellProjectileJSBuilder extends ProjectileEntityBuilder<SpellProjectileJS> {
    public transient Consumer<SpellProjectileJS.OnAntiMagicContext> onAntiMagic;

    public SpellProjectileJSBuilder(ResourceLocation i) {
        super(i);
    }

    public SpellProjectileJSBuilder onAntiMagic(Consumer<SpellProjectileJS.OnAntiMagicContext> onAntiMagic) {
        this.onAntiMagic = onAntiMagic;
        return this;
    }

    @Override
    public EntityType.EntityFactory<SpellProjectileJS> factory() {
        return (type, level) -> new SpellProjectileJS(this, type, level);
    }
}