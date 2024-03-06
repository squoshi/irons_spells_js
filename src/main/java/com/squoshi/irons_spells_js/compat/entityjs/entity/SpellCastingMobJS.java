package com.squoshi.irons_spells_js.compat.entityjs.entity;

import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellCastingMobJSBuilder;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.liolib.core.animatable.instance.AnimatableInstanceCache;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class SpellCastingMobJS extends AbstractSpellCastingMob implements IAnimatableJS {
    private final SpellCastingMobJSBuilder builder;
    private final AnimatableInstanceCache animationFactory;

    public SpellCastingMobJS(SpellCastingMobJSBuilder builder, EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        this.animationFactory = net.liopyu.liolib.util.GeckoLibUtil.createInstanceCache(this);
    }

    @Override
    public BaseLivingEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationFactory;
    }
}
