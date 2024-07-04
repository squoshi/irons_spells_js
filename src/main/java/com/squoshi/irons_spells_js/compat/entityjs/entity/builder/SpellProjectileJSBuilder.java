package com.squoshi.irons_spells_js.compat.entityjs.entity.builder;

import com.squoshi.irons_spells_js.compat.entityjs.entity.SpellProjectileJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class SpellProjectileJSBuilder extends ProjectileEntityBuilder<SpellProjectileJS> {
    public transient Consumer<SpellProjectileJS.OnAntiMagicContext> onAntiMagic;
    public transient Consumer<SpellProjectileJS> trailParticles;
    public transient Consumer<SpellProjectileJS.ImpactParticleContext> impactParticles;
    public transient Object setImpactSound;
    public SpellProjectileJSBuilder(ResourceLocation i) {
        super(i);
    }

    public SpellProjectileJSBuilder onAntiMagic(Consumer<SpellProjectileJS.OnAntiMagicContext> onAntiMagic) {
        this.onAntiMagic = onAntiMagic;
        return this;
    }
    @Info(value = """
            A consumer determining the impact particles for the spell.
                        
            Example usage:
            ```javascript
            builder.impactParticles(context => {
                const {x, y, z, entity} = context
                // Logic for spawning impact particles
            });
            ```
            """)
    public SpellProjectileJSBuilder impactParticles(Consumer<SpellProjectileJS.ImpactParticleContext> impactParticles) {
        this.impactParticles = impactParticles;
        return this;
    }
    @Info(value = """
            A consumer determining the trailing particles behind the spell.
                        
            Example usage:
            ```javascript
            builder.trailParticles(entity => {
                // Logic for spawning trailing particles
            });
            ```
            """)
    public SpellProjectileJSBuilder trailParticles(Consumer<SpellProjectileJS> trailParticles) {
        this.trailParticles = trailParticles;
        return this;
    }
    @Info(value = """
            Sets the impact sound for the entity using a string representation.
                        
            Example usage:
            ```javascript
            builder.setImpactSound("minecraft:entity.generic.swim");
            ```
            """)
    public SpellProjectileJSBuilder setImpactSound(Object sound) {
        if (sound instanceof String) setImpactSound = new ResourceLocation((String) sound);
        else if (sound instanceof ResourceLocation) setImpactSound = (ResourceLocation) sound;
        else {
            EntityJSHelperClass.logErrorMessageOnce("[SpellJS]: Invalid value for setImpactSound. Value: " + sound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.generic.swim\"");

            setImpactSound = null;
        }
        return this;
    }
    @Override
    public EntityType.EntityFactory<SpellProjectileJS> factory() {
        return (type, level) -> new SpellProjectileJS(this, type, level);
    }
}