package com.squoshi.irons_spells_js.util;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class CastContext {
    public final Level level;
    public final int spellLevel;
    public final LivingEntity entity;
    public final MagicData playerMagicData;

    public CastContext(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        this.level = level;
        this.spellLevel = spellLevel;
        this.entity = entity;
        this.playerMagicData = playerMagicData;
    }
}