package com.squoshi.irons_spells_js.compat.entityjs.entity;

import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellCastingMobJSBuilder;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.liolib.core.animatable.instance.AnimatableInstanceCache;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;

public class SpellCastingMobJS extends AbstractSpellCastingMob implements IAnimatableJS {
    private final SpellCastingMobJSBuilder builder;
    private final AnimatableInstanceCache animationFactory;
    public String entityName() {
        return this.getType().toString();
    }
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

    @Override
    public void cancelCast() {
        super.cancelCast();
        if (builder.onCancelledCast != null) {
            builder.onCancelledCast.accept(this);
        }
    }

    @Override
    public boolean isCasting() {
        if (builder.isCasting != null){
            Object obj = builder.isCasting.apply(this);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[KubeJS Irons Spells]: Invalid return value for isCasting from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isCasting());
        }
        return super.isCasting();
    }

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return builder.setCanBeLeashed;
    }

    public void jump() {
        double jumpPower = this.getJumpPower() + this.getJumpBoostPower();
        Vec3 currentVelocity = this.getDeltaMovement();

        // Adjust the Y component of the velocity to the calculated jump power
        this.setDeltaMovement(currentVelocity.x, jumpPower, currentVelocity.z);
        this.hasImpulse = true;
        if (this.isSprinting()) {
            // If sprinting, add a horizontal impulse for forward boost
            float yawRadians = this.getYRot() * 0.017453292F;
            this.setDeltaMovement(
                    this.getDeltaMovement().add(
                            -Math.sin(yawRadians) * 0.2,
                            0.0,
                            Math.cos(yawRadians) * 0.2
                    )
            );
        }
        this.hasImpulse = true;
        onJump();
        ForgeHooks.onLivingJump(this);
    }
    public void onJump() {
        if (builder.onLivingJump != null) {
            builder.onLivingJump.accept(this);
        }
    }
}
