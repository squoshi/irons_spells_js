package com.squoshi.irons_spells_js.compat.entityjs.entity.builder;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.liopyu.entityjs.builders.MobBuilder;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.resources.ResourceLocation;

public abstract class SpellCastingMobBuilder<T extends AbstractSpellCastingMob & IAnimatableJS> extends MobBuilder<T> {
    public SpellCastingMobBuilder(ResourceLocation i) {
        super(i);
    }
}
