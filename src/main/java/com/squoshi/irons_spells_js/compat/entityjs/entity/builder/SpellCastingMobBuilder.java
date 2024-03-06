package com.squoshi.irons_spells_js.compat.entityjs.entity.builder;

import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.MobBuilder;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class SpellCastingMobBuilder<T extends AbstractSpellCastingMob & IAnimatableJS> extends MobBuilder<T> {
    public transient Consumer<LivingEntity> onCancelledCast;
    public transient Consumer<LivingEntity> onLivingJump;
    public transient Function<LivingEntity, Object> isCasting;
    public transient boolean setCanBeLeashed;
    public SpellCastingMobBuilder(ResourceLocation i) {
        super(i);
        setCanBeLeashed = false;
    }
    public SpellCastingMobBuilder<T> setCanBeLeashed(boolean setCanBeLeashed){
        this.setCanBeLeashed = setCanBeLeashed;
        return this;
    }
    public SpellCastingMobBuilder<T> isCasting(Function<LivingEntity, Object> isCasting){
        this.isCasting = isCasting;
        return this;
    }
    public SpellCastingMobBuilder<T> onCancelledCast(Consumer<LivingEntity> onCancelledCast){
        this.onCancelledCast = onCancelledCast;
        return this;
    }
    @Info(value = """
            Sets a callback function to be executed when the entity jumps.
                        
            Example usage:
            ```javascript
            entityBuilder.onLivingJump(entity => {
                // Custom logic to handle the entity's jump action
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onLivingJump(Consumer<LivingEntity> onJump) {
        this.onLivingJump = onJump;
        return this;
    }
}
