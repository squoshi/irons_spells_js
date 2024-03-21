package com.squoshi.irons_spells_js.compat.entityjs.entity;

import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellProjectileJSBuilder;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

public class SpellProjectileJS extends ThrowableItemProjectile implements IProjectileEntityJS, AntiMagicSusceptible {
    public static record OnAntiMagicContext(MagicData getMagicData, Entity getEntity){}

    public SpellProjectileJSBuilder builder;

    private float damage;

    public SpellProjectileJS(SpellProjectileJSBuilder builder, EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
    }

    @SuppressWarnings("unused")
    public SpellProjectileJS(EntityType<? extends ThrowableItemProjectile> entityType, Level levelIn, LivingEntity shooter) {
        super(entityType,levelIn);
        setOwner(shooter);
    }

    @Override
    public ProjectileEntityBuilder<?> getProjectileBuilder() {
        return builder;
    }

    @Override
    protected Item getDefaultItem() {
        return null;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity != getOwner() && super.canHitEntity(entity);
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        if (builder.onAntiMagic != null) {
            ISSKJSUtils.safeCallback(builder.onAntiMagic, new OnAntiMagicContext(playerMagicData, this), "Error while calling onAntiMagic");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putFloat("Damage", this.damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.damage = pCompound.getFloat("Damage");
    }
}
