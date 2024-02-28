package com.squoshi.irons_spells_js.util;

import io.redspace.ironsspellbooks.api.spells.ICastData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ClientCastContext {
    public final Level level;
    public final int spellLevel;
    public final LivingEntity entity;
    public final ICastData castData;

    public ClientCastContext(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        this.level = level;
        this.spellLevel = spellLevel;
        this.entity = entity;
        this.castData = castData;
    }
}