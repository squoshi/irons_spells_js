package com.squoshi.irons_spells_js.compat.entityjs.entity;

import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellProjectileJSBuilder;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IProjectileEntityJS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SpellProjectileJS extends AbstractMagicProjectile implements IProjectileEntityJS, AntiMagicSusceptible {
    public static record OnAntiMagicContext(MagicData getMagicData, Entity getEntity){}

    public SpellProjectileJSBuilder builder;

    private float damage;

    public SpellProjectileJS(SpellProjectileJSBuilder builder, EntityType<? extends AbstractMagicProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
    }

    @SuppressWarnings("unused")
    public SpellProjectileJS(EntityType<? extends AbstractMagicProjectile> entityType, Level levelIn, LivingEntity shooter) {
        super(entityType,levelIn);
        setOwner(shooter);
    }

    @Override
    public ProjectileEntityBuilder<?> getProjectileBuilder() {
        return builder;
    }


    // New Overrides from AbstractMagicProjectile since the new ProjectileEntityBuilder allows for extending Projectile instead of only THrowableItemProjectile
    @Override
    public void trailParticles() {

    }

    @Override
    public void impactParticles(double v, double v1, double v2) {

    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
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