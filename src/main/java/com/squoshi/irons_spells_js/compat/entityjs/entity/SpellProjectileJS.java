package com.squoshi.irons_spells_js.compat.entityjs.entity;

import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellProjectileJSBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import io.redspace.ironsspellbooks.entity.spells.ShieldPart;
import io.redspace.ironsspellbooks.entity.spells.blood_slash.BloodSlashProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpellProjectileJS extends ThrowableItemProjectile implements IProjectileEntityJS, AntiMagicSusceptible {
    public SpellProjectileJSBuilder builder;
    private static final double SPEED = 1d;
    private static final int EXPIRE_TIME = 4 * 20;
    public AABB oldBB;
    private int age;
    private float damage;
    public int animationTime;
    private List<Entity> victims;
    public SpellProjectileJS(SpellProjectileJSBuilder builder, EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        oldBB = getBoundingBox();
        victims = new ArrayList<>();
        this.setNoGravity(true);
    }
    public SpellProjectileJS(EntityType<? extends ThrowableItemProjectile> entityType, Level levelIn, LivingEntity shooter) {
        super(entityType,levelIn);
        setOwner(shooter);
        setYRot(shooter.getYRot());
        setXRot(shooter.getXRot());
    }
    // Builder Overrides
    @Override
    public ProjectileEntityBuilder<?> getProjectileBuilder() {
        return builder;
    }

    @Override
    protected Item getDefaultItem() {
        return null;
    }
    // Spell Overrides
    public void shoot(Vec3 rotation) {
        setDeltaMovement(rotation.scale(SPEED));
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
    // Basic Logic Overrides


    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    @Override
    public void tick() {
        super.tick();
        if (++age > EXPIRE_TIME) {
            discard();
            return;
        }
        oldBB = getBoundingBox();

        if (!level.isClientSide) {
            HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (hitresult.getType() == HitResult.Type.BLOCK) {
                onHitBlock((BlockHitResult) hitresult);
            }
            for (Entity entity : level.getEntities(this, this.getBoundingBox()).stream().filter(target -> canHitEntity(target) && !victims.contains(target)).collect(Collectors.toSet())) {
                damageEntity(entity);
                //IronsSpellbooks.LOGGER.info(entity.getName().getString());
                MagicManager.spawnParticles(level, ParticleHelper.BLOOD, entity.getX(), entity.getY(), entity.getZ(), 50, 0, 0, 0, .5, true);
                if (entity instanceof ShieldPart || entity instanceof AbstractShieldEntity) {
                    discard();
                    return;
                }
            }
        }

        setPos(position().add(getDeltaMovement()));
        spawnParticles();
    }



    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        discard();
    }
    public static class EntityProjectileWeaponContext {
        @Info("The projectile weapon being used")
        public final Projectile projectileWeapon;
        @Info("The entity using the spell")
        public final Entity entity;
        public EntityProjectileWeaponContext(Projectile projectile, Entity entity) {
            this.projectileWeapon = projectile;
            this.entity = entity;
        }
    }
    private void damageEntity(Entity entity) {
        if (builder.damageEntity != null) {
            if (!victims.contains(entity)) {
                final EntityProjectileWeaponContext context = new EntityProjectileWeaponContext(this, entity);
                builder.damageEntity.accept(context);
                victims.add(entity);
            }
        }
    }

    //https://forge.gemwire.uk/wiki/Particles
    public void spawnParticles() {
        if (level.isClientSide) {

            float width = (float) getBoundingBox().getXsize();
            float step = .25f;
            float radians = Mth.DEG_TO_RAD * getYRot();
            float speed = .1f;
            for (int i = 0; i < width / step; i++) {
                double x = getX();
                double y = getY();
                double z = getZ();
                double offset = step * (i - width / step / 2);
                double rotX = offset * Math.cos(radians);
                double rotZ = -offset * Math.sin(radians);

                double dx = Math.random() * speed * 2 - speed;
                double dy = Math.random() * speed * 2 - speed;
                double dz = Math.random() * speed * 2 - speed;

                level.addParticle(ParticleHelper.BLOOD, false, x + rotX + dx, y + dy, z + rotZ + dz, dx, dy, dz);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity != getOwner() && super.canHitEntity(entity);
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
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
