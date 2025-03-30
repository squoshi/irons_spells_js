package com.squoshi.irons_spells_js.compat.entityjs.entity.builder;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public abstract class SpellCastingMobBuilder<T extends PathfinderMob & IAnimatableJS> extends PathfinderMobBuilder<T> {
    public transient Consumer<LivingEntity> onCancelledCast;
    public transient Function<LivingEntity, Object> isCasting;

    public SpellCastingMobBuilder(ResourceLocation i) {
        super(i);
    }
	@Info(value = """
            Sets a callback function to determine whether the entity is currently casting.
                        
            Example usage:
            ```javascript
            spellEntityBuilder.isCasting(entity => {
                return true;
            });
            ```
            """)
    public SpellCastingMobBuilder<T> isCasting(Function<LivingEntity, Object> isCasting){
        this.isCasting = isCasting;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the entity stops casting a spell.
                        
            Example usage:
            ```javascript
            spellEntityBuilder.onCancelledCast(entity => {
                // Custom logic to handle the entity cancelling their spell casts
            });
            ```
            """)
    public SpellCastingMobBuilder<T> onCancelledCast(Consumer<LivingEntity> onCancelledCast){
        this.onCancelledCast = onCancelledCast;
        return this;
    }
}