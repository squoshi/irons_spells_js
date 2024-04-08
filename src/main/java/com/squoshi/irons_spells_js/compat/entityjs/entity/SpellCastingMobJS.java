package com.squoshi.irons_spells_js.compat.entityjs.entity;

import com.google.common.collect.Maps;
import com.mojang.serialization.Dynamic;
import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellCastingMobJSBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.UtilsJS;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.fire.BurningDashSpell;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.PartEntityJS;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.events.BuildBrainEventJS;
import net.liopyu.entityjs.events.BuildBrainProviderEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.ModKeybinds;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public class SpellCastingMobJS extends PathfinderMob implements IAnimatableJS, IMagicEntity {
        //Manual implimentation of AbstractSpellCastingMob
         private static final EntityDataAccessor<Boolean> DATA_CANCEL_CAST;
        private static final EntityDataAccessor<Boolean> DATA_DRINKING_POTION;
        private final MagicData playerMagicData = new MagicData(true);
        private static final AttributeModifier SPEED_MODIFIER_DRINKING;
        @javax.annotation.Nullable
        private SpellData castingSpell;
        private final HashMap<String, AbstractSpell> spells = Maps.newHashMap();
        private int drinkTime;
        public boolean hasUsedSingleAttack;
        private AbstractSpell lastCastSpellType = SpellRegistry.none();
        private AbstractSpell instantCastSpellType = SpellRegistry.none();
        // EntityJS implementations
    private final SpellCastingMobJSBuilder builder;
    private final AnimatableInstanceCache animationFactory;
    protected PathNavigation navigation;
    public final PartEntityJS<?>[] partEntities;
    private final NonNullList<ItemStack> handItems;
    private final NonNullList<ItemStack> armorItems;
    protected boolean thisJumping;

    public String entityName() {
        return this.getType().toString();
    }
        public SpellCastingMobJS(SpellCastingMobJSBuilder builder, EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
            this.playerMagicData.setSyncedData(new SyncedSpellData(this));
            this.lookControl = this.createLookControl();

            this.handItems = NonNullList.withSize(2, ItemStack.EMPTY);
            this.armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
            this.thisJumping = false;
            this.builder = builder;
            this.animationFactory = GeckoLibUtil.createInstanceCache(this);
            List<PartEntityJS<?>> tempPartEntities = new ArrayList<>();
            for (ContextUtils.PartEntityParams<SpellCastingMobJS> params : builder.partEntityParamsList) {
                PartEntityJS<?> partEntity = new PartEntityJS<>(this, params.name, params.width, params.height, params.builder);
                tempPartEntities.add(partEntity);
            }

            partEntities = tempPartEntities.toArray(new PartEntityJS<?>[0]);
            this.navigation = this.createNavigation(pLevel);
        }

    protected LookControl createLookControl() {
            return new LookControl(this) {
                protected boolean resetXRotOnTick() {
                    return SpellCastingMobJS.this.getTarget() == null;
                }
            };
        }

        public MagicData getMagicData() {
            return this.playerMagicData;
        }

        protected void defineSynchedData() {
            super.defineSynchedData();
            this.entityData.define(DATA_CANCEL_CAST, false);
            this.entityData.define(DATA_DRINKING_POTION, false);
        }

        public boolean isDrinkingPotion() {
            return (Boolean)this.entityData.get(DATA_DRINKING_POTION);
        }

        protected void setDrinkingPotion(boolean drinkingPotion) {
            this.entityData.set(DATA_DRINKING_POTION, drinkingPotion);
        }
        public void startDrinkingPotion() {
            if (!this.level().isClientSide) {
                this.setDrinkingPotion(true);
                this.drinkTime = 35;
                AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
                attributeinstance.removeModifier(SPEED_MODIFIER_DRINKING);
                attributeinstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
            }

        }

        private void finishDrinkingPotion() {
            this.setDrinkingPotion(false);
            this.heal(Math.min(Math.max(10.0F, this.getMaxHealth() / 10.0F), this.getMaxHealth() / 4.0F));
            this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING);
            if (!this.isSilent()) {
                this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            }

        }

        public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
            super.onSyncedDataUpdated(pKey);
            if (this.level().isClientSide) {
                if (pKey.getId() == DATA_CANCEL_CAST.getId()) {
                    this.cancelCast();
                }

            }
        }

        public void addAdditionalSaveData(CompoundTag pCompound) {
            super.addAdditionalSaveData(pCompound);
            this.playerMagicData.getSyncedData().saveNBTData(pCompound);
            pCompound.putBoolean("usedSpecial", this.hasUsedSingleAttack);
        }

        public void readAdditionalSaveData(CompoundTag pCompound) {
            super.readAdditionalSaveData(pCompound);
            SyncedSpellData syncedSpellData = new SyncedSpellData(this);
            syncedSpellData.loadNBTData(pCompound);
            if (syncedSpellData.isCasting()) {
                AbstractSpell spell = SpellRegistry.getSpell(syncedSpellData.getCastingSpellId());
                this.initiateCastSpell(spell, syncedSpellData.getCastingSpellLevel());
            }

            this.playerMagicData.setSyncedData(syncedSpellData);
            this.hasUsedSingleAttack = pCompound.getBoolean("usedSpecial");
        }

        public void cancelCast() {
            if (builder.onCancelledCast != null) {
                builder.onCancelledCast.accept(this);
            }
            if (this.isCasting()) {
                if (this.level().isClientSide) {
                } else {
                    this.entityData.set(DATA_CANCEL_CAST, !(Boolean)this.entityData.get(DATA_CANCEL_CAST));
                }

                this.castComplete();
            }

        }

        private void castComplete() {
            if (!this.level().isClientSide) {
                if (this.castingSpell != null) {
                    this.castingSpell.getSpell().onServerCastComplete(this.level(), this.castingSpell.getLevel(), this, this.playerMagicData, false);
                }
            } else {
                this.playerMagicData.resetCastingState();
            }

            this.castingSpell = null;
        }

        public void startAutoSpinAttack(int pAttackTicks) {
            this.autoSpinAttackTicks = pAttackTicks;
            if (!this.level().isClientSide) {
                this.setLivingEntityFlag(4, true);
            }

            this.setYRot((float)(Math.atan2(this.getDeltaMovement().x, this.getDeltaMovement().z) * 57.2957763671875));
        }

        public void setSyncedSpellData(SyncedSpellData syncedSpellData) {
            if (this.level().isClientSide) {
                boolean isCasting = this.playerMagicData.isCasting();
                this.playerMagicData.setSyncedData(syncedSpellData);
                this.castingSpell = this.playerMagicData.getCastingSpell();
                if (this.castingSpell != null) {
                    if (!this.playerMagicData.isCasting() && isCasting) {
                        this.castComplete();
                    } else if (this.playerMagicData.isCasting() && !isCasting) {
                        AbstractSpell spell = this.playerMagicData.getCastingSpell().getSpell();
                        this.initiateCastSpell(spell, this.playerMagicData.getCastingSpellLevel());
                        if (this.castingSpell.getSpell().getCastType() == CastType.INSTANT) {
                            this.instantCastSpellType = this.castingSpell.getSpell();
                            this.castingSpell.getSpell().onClientPreCast(this.level(), this.castingSpell.getLevel(), this, InteractionHand.MAIN_HAND, this.playerMagicData);
                            this.castComplete();
                        }
                    }

                }
            }
        }

        protected void customServerAiStep() {
            super.customServerAiStep();
            if (this.isDrinkingPotion()) {
                if (this.drinkTime-- <= 0) {
                    this.finishDrinkingPotion();
                } else if (this.drinkTime % 4 == 0 && !this.isSilent()) {
                    this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_DRINK, this.getSoundSource(), 1.0F, Utils.random.nextFloat() * 0.1F + 0.9F);
                }
            }

            if (this.castingSpell != null) {
                this.playerMagicData.handleCastDuration();
                if (this.playerMagicData.isCasting()) {
                    this.castingSpell.getSpell().onServerCastTick(this.level(), this.castingSpell.getLevel(), this, this.playerMagicData);
                }

                this.forceLookAtTarget(this.getTarget());
                if (this.playerMagicData.getCastDurationRemaining() <= 0) {
                    if (this.castingSpell.getSpell().getCastType() == CastType.LONG || this.castingSpell.getSpell().getCastType() == CastType.INSTANT) {
                        this.castingSpell.getSpell().onCast(this.level(), this.castingSpell.getLevel(), this, CastSource.MOB, this.playerMagicData);
                    }

                    this.castComplete();
                } else if (this.castingSpell.getSpell().getCastType() == CastType.CONTINUOUS && (this.playerMagicData.getCastDurationRemaining() + 1) % 10 == 0) {
                    this.castingSpell.getSpell().onCast(this.level(), this.castingSpell.getLevel(), this, CastSource.MOB, this.playerMagicData);
                }

            }
        }

        public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
            if (spell == SpellRegistry.none()) {
                this.castingSpell = null;
            } else {

                this.castingSpell = new SpellData(spell, spellLevel);
                if (this.getTarget() != null) {
                    this.forceLookAtTarget(this.getTarget());
                }

                if (!this.level().isClientSide && !this.castingSpell.getSpell().checkPreCastConditions(this.level(), spellLevel, this, this.playerMagicData)) {
                    this.castingSpell = null;
                } else {
                    if (spell != SpellRegistry.TELEPORT_SPELL.get() && spell != SpellRegistry.FROST_STEP_SPELL.get()) {
                        if (spell == SpellRegistry.BLOOD_STEP_SPELL.get()) {
                            this.setTeleportLocationBehindTarget(3);
                        } else if (spell == SpellRegistry.BURNING_DASH_SPELL.get()) {
                            this.setBurningDashDirectionData();
                        }
                    } else {
                        this.setTeleportLocationBehindTarget(10);
                    }

                    this.playerMagicData.initiateCast(this.castingSpell.getSpell(), this.castingSpell.getLevel(), this.castingSpell.getSpell().getEffectiveCastTime(this.castingSpell.getLevel(), this), CastSource.MOB, SpellSelectionManager.MAINHAND);
                    if (!this.level().isClientSide) {
                        this.castingSpell.getSpell().onServerPreCast(this.level(), this.castingSpell.getLevel(), this, this.playerMagicData);
                    }

                }
            }
        }

        public void notifyDangerousProjectile(Projectile projectile) {
        }

        public boolean isCasting() {
            if (builder.isCasting != null){
                Object obj = builder.isCasting.apply(this);
                if (obj instanceof Boolean b) return b;
                EntityJSHelperClass.logErrorMessageOnce("[KubeJS Irons Spells]: Invalid return value for isCasting from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + this.playerMagicData.isCasting());
            }
            return this.playerMagicData.isCasting();
        }

        public boolean setTeleportLocationBehindTarget(int distance) {
            LivingEntity target = this.getTarget();
            boolean valid = false;
            if (target != null) {
                Vec3 rotation = target.getLookAngle().normalize().scale((double)(-distance));
                Vec3 pos = target.position();
                Vec3 teleportPos = rotation.add(pos);

                for(int i = 0; i < 24; ++i) {
                    Vec3 randomness = Utils.getRandomVec3((double)(0.15F * (float)i)).multiply(1.0, 0.0, 1.0);
                    teleportPos = Utils.moveToRelativeGroundLevel(this.level(), target.position().subtract((new Vec3(0.0, 0.0, (double)((float)distance / (float)(i / 7 + 1)))).yRot(-(target.getYRot() + (float)(i * 45)) * 0.017453292F)).add(randomness), 5);
                    teleportPos = new Vec3(teleportPos.x, teleportPos.y + 0.10000000149011612, teleportPos.z);
                    AABB reposBB = this.getBoundingBox().move(teleportPos.subtract(this.position()));
                    if (!this.level().collidesWithSuffocatingBlock(this, reposBB.inflate(-0.05000000074505806))) {
                        valid = true;
                        break;
                    }
                }

                if (valid) {
                    this.playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(teleportPos));
                } else {
                    this.playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(this.position()));
                }
            } else {
                this.playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(this.position()));
            }

            return valid;
        }

        public void setBurningDashDirectionData() {
            this.playerMagicData.setAdditionalCastData(new BurningDashSpell.BurningDashDirectionOverrideCastData());
        }

        private void forceLookAtTarget(LivingEntity target) {
            if (target != null) {
                double d0 = target.getX() - this.getX();
                double d2 = target.getZ() - this.getZ();
                double d1 = target.getEyeY() - this.getEyeY();
                double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                float f = (float)(Mth.atan2(d2, d0) * 57.2957763671875) - 90.0F;
                float f1 = (float)(-(Mth.atan2(d1, d3) * 57.2957763671875));
                this.setXRot(f1 % 360.0F);
                this.setYRot(f % 360.0F);
            }

        }

        private void addClientSideParticles() {
            double d0 = 0.4;
            double d1 = 0.3;
            double d2 = 0.35;
            float f = this.yBodyRot * 0.017453292F + Mth.cos((float)this.tickCount * 0.6662F) * 0.25F;
            float f1 = Mth.cos(f);
            float f2 = Mth.sin(f);
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + (double)f1 * 0.6, this.getY() + 1.8, this.getZ() + (double)f2 * 0.6, d0, d1, d2);
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() - (double)f1 * 0.6, this.getY() + 1.8, this.getZ() - (double)f2 * 0.6, d0, d1, d2);
        }


        static {
            DATA_CANCEL_CAST = SynchedEntityData.defineId(SpellCastingMobJS.class, EntityDataSerializers.BOOLEAN);
            DATA_DRINKING_POTION = SynchedEntityData.defineId(SpellCastingMobJS.class, EntityDataSerializers.BOOLEAN);
            SPEED_MODIFIER_DRINKING = new AttributeModifier(UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E"), "Drinking speed penalty", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }

    /**
     * EntityJS Builder Overrides Below
     */

    //Multi Hitbox logic
    public void setId(int entityId) {
        super.setId(entityId);

        for(int i = 0; i < this.partEntities.length; ++i) {
            PartEntityJS<?> partEntity = this.partEntities[i];
            if (partEntity != null) {
                partEntity.setId(entityId + i + 1);
            }
        }

    }

    public void tickPart(String partName, double offsetX, double offsetY, double offsetZ) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        PartEntityJS[] var14 = this.partEntities;
        int var15 = var14.length;

        for(int var16 = 0; var16 < var15; ++var16) {
            PartEntityJS<?> partEntity = var14[var16];
            if (partEntity.name.equals(partName)) {
                partEntity.movePart(x + offsetX, y + offsetY, z + offsetZ, partEntity.getYRot(), partEntity.getXRot());
                return;
            }
        }

        EntityJSHelperClass.logWarningMessageOnce("Part with name " + partName + " not found for entity: " + this.entityName());
    }

    public boolean isMultipartEntity() {
        return this.partEntities != null;
    }

    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
    }

    public PartEntity<?>[] getParts() {
        return (PartEntity[])Objects.requireNonNullElseGet(this.partEntities, () -> {
            return new PartEntity[0];
        });
    }
    //Builder/Animatable Logic
    public BaseLivingEntityBuilder<?> getBuilder() {
        return this.builder;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationFactory;
    }
    //Ai logic
    protected Brain.Provider<?> brainProvider() {
        if (EventHandlers.buildBrainProvider.hasListeners()) {
            BuildBrainProviderEventJS<SpellCastingMobJS> event = new BuildBrainProviderEventJS();
            EventHandlers.buildBrainProvider.post(event, this.getTypeId());
            return event.provide();
        } else {
            return super.brainProvider();
        }
    }

    protected Brain<SpellCastingMobJS> makeBrain(Dynamic<?> p_21069_) {
        if (EventHandlers.buildBrain.hasListeners()) {
            Brain<SpellCastingMobJS> brain = (Brain)UtilsJS.cast(this.brainProvider().makeBrain(p_21069_));
            EventHandlers.buildBrain.post(new BuildBrainEventJS(brain), this.getTypeId());
            return brain;
        } else {
            return (Brain)UtilsJS.cast(super.makeBrain(p_21069_));
        }
    }

    protected void registerGoals() {
        if (EventHandlers.addGoalTargets.hasListeners()) {
            EventHandlers.addGoalTargets.post(new AddGoalTargetsEventJS<>(this, this.targetSelector), this.getTypeId());
        }

        if (EventHandlers.addGoalSelectors.hasListeners()) {
            EventHandlers.addGoalSelectors.post(new AddGoalSelectorsEventJS<>(this, this.goalSelector), this.getTypeId());
        }

    }



    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.builder.onInteract != null) {
            ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(this, pPlayer, pHand);
            EntityJSHelperClass.consumerCallback(this.builder.onInteract, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onInteract.");
        }

        return super.mobInteract(pPlayer, pHand);
    }

    public boolean doHurtTarget(Entity pEntity) {
        if (this.builder != null && this.builder.onHurtTarget != null) {
            ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, this);
            EntityJSHelperClass.consumerCallback(this.builder.onHurtTarget, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onHurtTarget.");
        }

        return super.doHurtTarget(pEntity);
    }

    public void onJump() {
        if (this.builder.onLivingJump != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onLivingJump, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onLivingJump.");
        }

    }

    public void aiStep() {
        super.aiStep();
        if (this.canJump() && this.onGround() && this.getNavigation().isInProgress() && this.shouldJump()) {
            this.jump();
        }

        if (this.builder.aiStep != null) {
            EntityJSHelperClass.consumerCallback(this.builder.aiStep, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: aiStep.");
        }

    }

    protected void tickDeath() {
        if (this.builder.tickDeath != null) {
            EntityJSHelperClass.consumerCallback(this.builder.tickDeath, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: tickDeath.");
        } else {
            super.tickDeath();
        }

    }

    protected void tickLeash() {
        super.tickLeash();
        if (this.builder.tickLeash != null) {
            Player $$0 = (Player)this.getLeashHolder();
            ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext($$0, this);
            EntityJSHelperClass.consumerCallback(this.builder.tickLeash, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: tickLeash.");
        }

    }
    public boolean canBeLeashed(Player pPlayer) {
        if (this.builder.canBeLeashed != null) {
            ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(pPlayer, this);
            Object obj = this.builder.canBeLeashed.apply(context);
            if (obj instanceof Boolean) {
                Boolean b = (Boolean)obj;
                return b;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeLeashed from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to false.");
        }

        return false;
    }
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (this.builder.onTargetChanged != null) {
            ContextUtils.TargetChangeContext context = new ContextUtils.TargetChangeContext(target, this);
            EntityJSHelperClass.consumerCallback(this.builder.onTargetChanged, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onTargetChanged.");
        }

    }

    public void ate() {
        super.ate();
        if (this.builder.ate != null) {
            EntityJSHelperClass.consumerCallback(this.builder.ate, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: ate.");
        }

    }

    protected PathNavigation createNavigation(Level pLevel) {
        if (this.builder != null && this.builder.createNavigation != null) {
            ContextUtils.EntityLevelContext context = new ContextUtils.EntityLevelContext(pLevel, this);
            Object obj = this.builder.createNavigation.apply(context);
            if (obj instanceof PathNavigation) {
                PathNavigation p = (PathNavigation)obj;
                return p;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for createNavigation from entity: " + var10000 + ". Value: " + obj + ". Must be PathNavigation. Defaulting to super method.");
                return super.createNavigation(pLevel);
            }
        } else {
            return super.createNavigation(pLevel);
        }
    }



    public MobType getMobType() {
        return this.builder.mobType;
    }

    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
        ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, (item) -> {
            return item instanceof BowItem;
        })));
        AbstractArrow abstractarrow = this.getArrow(itemstack, pDistanceFactor);
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            abstractarrow = ((BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrow);
        }

        double d0 = pTarget.getX() - this.getX();
        double d1 = pTarget.getY(0.3333333333333333) - abstractarrow.getY();
        double d2 = pTarget.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        abstractarrow.shoot(d0, d1 + d3 * 0.20000000298023224, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(abstractarrow);
    }

    protected AbstractArrow getArrow(ItemStack pArrowStack, float pVelocity) {
        return ProjectileUtil.getMobArrow(this, pArrowStack, pVelocity);
    }

    public boolean canJump() {
        return (Boolean)Objects.requireNonNullElse(this.builder.canJump, true);
    }

    public void jump() {
        double jumpPower = (double)(this.getJumpPower() + this.getJumpBoostPower());
        Vec3 currentVelocity = this.getDeltaMovement();
        this.setDeltaMovement(currentVelocity.x, jumpPower, currentVelocity.z);
        this.hasImpulse = true;
        if (this.isSprinting()) {
            float yawRadians = this.getYRot() * 0.017453292F;
            this.setDeltaMovement(this.getDeltaMovement().add(-Math.sin((double)yawRadians) * 0.2, 0.0, Math.cos((double)yawRadians) * 0.2));
        }

        this.hasImpulse = true;
        this.onJump();
        ForgeHooks.onLivingJump(this);
    }

    public boolean shouldJump() {
        BlockPos forwardPos = this.blockPosition().relative(this.getDirection());
        return this.level().loadedAndEntityCanStandOn(forwardPos, this) && (double)this.getStepHeight() < this.level().getBlockState(forwardPos).getShape(this.level(), forwardPos).max(Direction.Axis.Y);
    }

    public HumanoidArm getMainArm() {
        return this.builder.mainArm != null ? (HumanoidArm)this.builder.mainArm : super.getMainArm();
    }

    public float getWalkTargetValue(BlockPos pos, LevelReader levelReader) {
        if (this.builder.walkTargetValue == null) {
            return super.getWalkTargetValue(pos, levelReader);
        } else {
            ContextUtils.EntityBlockPosLevelContext context = new ContextUtils.EntityBlockPosLevelContext(pos, levelReader, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.walkTargetValue.apply(context), "float");
            if (obj != null) {
                return (Float)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for walkTargetValue from entity: " + var10000 + ". Value: " + this.builder.walkTargetValue.apply(context) + ". Must be a float. Defaulting to " + super.getWalkTargetValue(pos, levelReader));
                return super.getWalkTargetValue(pos, levelReader);
            }
        }
    }

    protected boolean shouldStayCloseToLeashHolder() {
        if (this.builder.shouldStayCloseToLeashHolder == null) {
            return super.shouldStayCloseToLeashHolder();
        } else {
            Object value = this.builder.shouldStayCloseToLeashHolder.apply(this);
            if (value instanceof Boolean) {
                Boolean b = (Boolean)value;
                return b;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldStayCloseToLeashHolder from entity: " + this.entityName() + ". Value: " + value + ". Must be a boolean. Defaulting to " + super.shouldStayCloseToLeashHolder());
                return super.shouldStayCloseToLeashHolder();
            }
        }
    }

    public boolean canFireProjectileWeaponPredicate(ProjectileWeaponItem projectileWeapon) {
        if (this.builder.canFireProjectileWeaponPredicate != null) {
            ContextUtils.EntityProjectileWeaponContext context = new ContextUtils.EntityProjectileWeaponContext(projectileWeapon, this);
            Object obj = this.builder.canFireProjectileWeaponPredicate.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            String var10000 = this.entityName();
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFireProjectileWeaponPredicate from entity: " + var10000 + ". Value: " + obj + ". Must be a boolean. Defaulting to false.");
        }

        return false;
    }

    public boolean canFireProjectileWeapons(ProjectileWeaponItem projectileWeapon) {
        if (this.builder.canFireProjectileWeapon == null) {
            return super.canFireProjectileWeapon(projectileWeapon);
        } else {
            return this.builder.canFireProjectileWeapon.test(projectileWeapon.getDefaultInstance()) && projectileWeapon instanceof ProjectileWeaponItem;
        }
    }

    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
        if (!this.canFireProjectileWeapons(projectileWeapon) && !this.canFireProjectileWeaponPredicate(projectileWeapon)) {
            return super.canFireProjectileWeapon(projectileWeapon);
        } else {
            return this.canFireProjectileWeapons(projectileWeapon) && this.canFireProjectileWeaponPredicate(projectileWeapon);
        }
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return this.builder.setAmbientSound != null ? (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation)this.builder.setAmbientSound) : super.getAmbientSound();
    }

    public boolean canHoldItem(ItemStack stack) {
        if (this.builder.canHoldItem != null) {
            ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(stack, this);
            Object obj = this.builder.canHoldItem.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canHoldItem from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canHoldItem(stack));
        }

        return super.canHoldItem(stack);
    }

    protected boolean shouldDespawnInPeaceful() {
        return (Boolean)Objects.requireNonNullElseGet(this.builder.shouldDespawnInPeaceful, () -> {
            return super.shouldDespawnInPeaceful();
        });
    }

    public boolean isPersistenceRequired() {
        return (Boolean)Objects.requireNonNullElseGet(this.builder.isPersistenceRequired, () -> {
            return super.isPersistenceRequired();
        });
    }

    public double getMeleeAttackRangeSqr(LivingEntity entity) {
        if (this.builder.meleeAttackRangeSqr != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.meleeAttackRangeSqr.apply(this), "double");
            if (obj != null) {
                return (Double)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for meleeAttackRangeSqr from entity: " + var10000 + ". Value: " + this.builder.meleeAttackRangeSqr.apply(this) + ". Must be a double. Defaulting to " + super.getMeleeAttackRangeSqr(entity));
                return super.getMeleeAttackRangeSqr(entity);
            }
        } else {
            return super.getMeleeAttackRangeSqr(entity);
        }
    }

    public int getAmbientSoundInterval() {
        return this.builder.ambientSoundInterval != null ? (Integer)this.builder.ambientSoundInterval : super.getAmbientSoundInterval();
    }

    public double getMyRidingOffset() {
        if (this.builder.myRidingOffset == null) {
            return super.getMyRidingOffset();
        } else {
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.myRidingOffset.apply(this), "double");
            if (obj != null) {
                return (Double)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for myRidingOffset from entity: " + var10000 + ". Value: " + this.builder.myRidingOffset.apply(this) + ". Must be a double. Defaulting to " + super.getMyRidingOffset());
                return super.getMyRidingOffset();
            }
        }
    }

    protected double followLeashSpeed() {
        return (Double)Objects.requireNonNullElseGet(this.builder.followLeashSpeed, () -> {
            return super.followLeashSpeed();
        });
    }

    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        if (this.builder.removeWhenFarAway == null) {
            return super.removeWhenFarAway(pDistanceToClosestPlayer);
        } else {
            ContextUtils.EntityDistanceToPlayerContext context = new ContextUtils.EntityDistanceToPlayerContext(pDistanceToClosestPlayer, this);
            Object obj = this.builder.removeWhenFarAway.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for removeWhenFarAway from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.removeWhenFarAway(pDistanceToClosestPlayer));
                return super.removeWhenFarAway(pDistanceToClosestPlayer);
            }
        }
    }

    public boolean isAlliedTo(Entity pEntity) {
        if (this.builder.isAlliedTo != null) {
            ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, this);
            Object obj = this.builder.isAlliedTo.apply(context);
            if (obj instanceof Boolean) {
                Boolean b = (Boolean)obj;
                return b;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAlliedTo from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isAlliedTo(pEntity));
        }

        return super.isAlliedTo(pEntity);
    }

    public void travel(Vec3 pTravelVector) {
        LivingEntity livingentity = this.getControllingPassenger();
        if (this.isAlive() && this.isVehicle() && this.builder.canSteer && livingentity != null) {
            if (this.getControllingPassenger() instanceof Player && this.builder.mountJumpingEnabled) {
                if (this.ableToJump()) {
                    this.setThisJumping(true);
                }

                if (this.thisJumping) {
                    this.setThisJumping(false);
                    double jumpPower = (double)(this.getJumpPower() + this.getJumpBoostPower());
                    Vec3 currentVelocity = this.getDeltaMovement();
                    double newVelocityX = currentVelocity.x;
                    double newVelocityY = currentVelocity.y + jumpPower;
                    double newVelocityZ = currentVelocity.z;
                    this.setDeltaMovement(newVelocityX, newVelocityY, newVelocityZ);
                    this.onJump();
                    ForgeHooks.onLivingJump(this);
                }
            }

            LivingEntity passenger = this.getControllingPassenger();
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
            this.setYRot(passenger.getYRot());
            this.setXRot(passenger.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;
            float x = passenger.xxa * 0.5F;
            float z = passenger.zza;
            if (z <= 0.0F) {
                z *= 0.25F;
            }

            this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            super.travel(new Vec3((double)x, pTravelVector.y, (double)z));
        } else {
            super.travel(pTravelVector);
        }

        if (this.builder.travel != null) {
            ContextUtils.Vec3Context context = new ContextUtils.Vec3Context(pTravelVector, this);
            EntityJSHelperClass.consumerCallback(this.builder.travel, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: travel.");
        }

    }

    public void tick() {
        super.tick();
        if (this.builder.tick != null && !this.level().isClientSide()) {
            EntityJSHelperClass.consumerCallback(this.builder.tick, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: tick.");
        }

    }

    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (this.builder.onAddedToWorld != null && !this.level().isClientSide()) {
            EntityJSHelperClass.consumerCallback(this.builder.onAddedToWorld, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onAddedToWorld.");
        }

    }

    protected void doAutoAttackOnTouch(@NotNull LivingEntity target) {
        super.doAutoAttackOnTouch(target);
        if (this.builder.doAutoAttackOnTouch != null) {
            ContextUtils.AutoAttackContext context = new ContextUtils.AutoAttackContext(this, target);
            EntityJSHelperClass.consumerCallback(this.builder.doAutoAttackOnTouch, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: doAutoAttackOnTouch.");
        }

    }

    protected int decreaseAirSupply(int p_21303_) {
        if (this.builder.onDecreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onDecreaseAirSupply, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onDecreaseAirSupply.");
        }

        return super.decreaseAirSupply(p_21303_);
    }

    protected int increaseAirSupply(int p_21307_) {
        if (this.builder.onIncreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onIncreaseAirSupply, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onIncreaseAirSupply.");
        }

        return super.increaseAirSupply(p_21307_);
    }

    protected void blockedByShield(@NotNull LivingEntity p_21246_) {
        super.blockedByShield(p_21246_);
        if (this.builder.onBlockedByShield != null) {
            ContextUtils.LivingEntityContext context = new ContextUtils.LivingEntityContext(this, p_21246_);
            EntityJSHelperClass.consumerCallback(this.builder.onBlockedByShield, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onDecreaseAirSupply.");
        }

    }

    public void onEquipItem(EquipmentSlot slot, ItemStack previous, ItemStack current) {
        super.onEquipItem(slot, previous, current);
        if (this.builder.onEquipItem != null) {
            ContextUtils.EntityEquipmentContext context = new ContextUtils.EntityEquipmentContext(slot, previous, current, this);
            EntityJSHelperClass.consumerCallback(this.builder.onEquipItem, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onEquipItem.");
        }

    }

    public void onEffectAdded(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
        if (this.builder.onEffectAdded != null) {
            ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
            EntityJSHelperClass.consumerCallback(this.builder.onEffectAdded, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onEffectAdded.");
        } else {
            super.onEffectAdded(effectInstance, entity);
        }

    }

    protected void onEffectRemoved(@NotNull MobEffectInstance effectInstance) {
        if (this.builder.onEffectRemoved != null) {
            ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
            EntityJSHelperClass.consumerCallback(this.builder.onEffectRemoved, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onEffectRemoved.");
        } else {
            super.onEffectRemoved(effectInstance);
        }

    }

    public void heal(float amount) {
        super.heal(amount);
        if (this.builder.onLivingHeal != null) {
            ContextUtils.EntityHealContext context = new ContextUtils.EntityHealContext(this, amount);
            EntityJSHelperClass.consumerCallback(this.builder.onLivingHeal, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onLivingHeal.");
        }

    }

    public void die(@NotNull DamageSource damageSource) {
        super.die(damageSource);
        if (this.builder.onDeath != null) {
            ContextUtils.DeathContext context = new ContextUtils.DeathContext(this, damageSource);
            EntityJSHelperClass.consumerCallback(this.builder.onDeath, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onDeath.");
        }

    }

    protected void dropCustomDeathLoot(@NotNull DamageSource damageSource, int lootingMultiplier, boolean allowDrops) {
        if (this.builder.dropCustomDeathLoot != null) {
            ContextUtils.EntityLootContext context = new ContextUtils.EntityLootContext(damageSource, lootingMultiplier, allowDrops, this);
            EntityJSHelperClass.consumerCallback(this.builder.dropCustomDeathLoot, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: dropCustomDeathLoot.");
        } else {
            super.dropCustomDeathLoot(damageSource, lootingMultiplier, allowDrops);
        }

    }

    protected void onFlap() {
        if (this.builder.onFlap != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onFlap, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onFlap.");
        }

        super.onFlap();
    }

    public boolean ableToJump() {
        return ModKeybinds.mount_jump.isDown() && this.onGround();
    }

    public void setThisJumping(boolean value) {
        this.thisJumping = value;
    }

    public LivingEntity getControllingPassenger() {
        Entity var2 = this.getFirstPassenger();
        LivingEntity var10000;
        if (var2 instanceof LivingEntity entity) {
            var10000 = entity;
        } else {
            var10000 = null;
        }

        return var10000;
    }

    @Info("Calls a triggerable animation to be played anywhere.\n")
    public void triggerAnimation(String controllerName, String animName) {
        this.triggerAnim(controllerName, animName);
    }

    public boolean canCollideWith(Entity pEntity) {
        if (this.builder.canCollideWith != null) {
            ContextUtils.CollidingEntityContext context = new ContextUtils.CollidingEntityContext(this, pEntity);
            Object obj = this.builder.canCollideWith.apply(context);
            if (obj instanceof Boolean) {
                Boolean b = (Boolean)obj;
                return b;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canCollideWith from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canCollideWith(pEntity));
        }

        return super.canCollideWith(pEntity);
    }

    protected float getSoundVolume() {
        return (Float)Objects.requireNonNullElseGet(this.builder.setSoundVolume, () -> {
            return super.getSoundVolume();
        });
    }

    protected float getWaterSlowDown() {
        return (Float)Objects.requireNonNullElseGet(this.builder.setWaterSlowDown, () -> {
            return super.getWaterSlowDown();
        });
    }

    protected float getBlockJumpFactor() {
        if (this.builder.setBlockJumpFactor == null) {
            return super.getBlockJumpFactor();
        } else {
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.setBlockJumpFactor.apply(this), "float");
            if (obj != null) {
                return (Float)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setBlockJumpFactor from entity: " + var10000 + ". Value: " + this.builder.setBlockJumpFactor.apply(this) + ". Must be a float. Defaulting to " + super.getBlockJumpFactor());
                return super.getBlockJumpFactor();
            }
        }
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        if (this.builder != null && this.builder.setStandingEyeHeight != null) {
            ContextUtils.EntityPoseDimensionsContext context = new ContextUtils.EntityPoseDimensionsContext(pPose, pDimensions, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.setStandingEyeHeight.apply(context), "float");
            if (obj != null) {
                return (Float)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setStandingEyeHeight from entity: " + var10000 + ". Value: " + this.builder.setStandingEyeHeight.apply(context) + ". Must be a float. Defaulting to " + super.getStandingEyeHeight(pPose, pDimensions));
                return super.getStandingEyeHeight(pPose, pDimensions);
            }
        } else {
            return super.getStandingEyeHeight(pPose, pDimensions);
        }
    }

    public boolean isPushable() {
        return this.builder.isPushable;
    }

    protected float getBlockSpeedFactor() {
        if (this.builder.blockSpeedFactor == null) {
            return super.getBlockSpeedFactor();
        } else {
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.blockSpeedFactor.apply(this), "float");
            if (this.builder.blockSpeedFactor == null) {
                return super.getBlockSpeedFactor();
            } else if (obj != null) {
                return (Float)obj;
            } else {
                Object var10000 = this.builder.get();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + var10000 + ". Value: " + this.builder.blockSpeedFactor.apply(this) + ". Must be a float, defaulting to " + super.getBlockSpeedFactor());
                return super.getBlockSpeedFactor();
            }
        }
    }

    protected boolean canAddPassenger(@NotNull Entity entity) {
        if (this.builder.canAddPassenger == null) {
            return super.canAddPassenger(entity);
        } else {
            ContextUtils.PassengerEntityContext context = new ContextUtils.PassengerEntityContext(entity, this);
            Object obj = this.builder.canAddPassenger.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.canAddPassenger(entity));
                return super.canAddPassenger(entity);
            }
        }
    }

    protected boolean shouldDropLoot() {
        if (this.builder.shouldDropLoot != null) {
            Object obj = this.builder.shouldDropLoot.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropLoot from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.shouldDropLoot());
        }

        return super.shouldDropLoot();
    }

    protected boolean isAffectedByFluids() {
        if (this.builder.isAffectedByFluids != null) {
            Object obj = this.builder.isAffectedByFluids.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByFluids from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isAffectedByFluids());
        }

        return super.isAffectedByFluids();
    }

    protected boolean isAlwaysExperienceDropper() {
        return this.builder.isAlwaysExperienceDropper;
    }

    protected boolean isImmobile() {
        if (this.builder.isImmobile != null) {
            Object obj = this.builder.isImmobile.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isImmobile from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isImmobile());
        }

        return super.isImmobile();
    }

    protected boolean isFlapping() {
        if (this.builder.isFlapping != null) {
            Object obj = this.builder.isFlapping.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFlapping from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isFlapping());
        }

        return super.isFlapping();
    }

    public int calculateFallDamage(float fallDistance, float pDamageMultiplier) {
        if (this.builder.calculateFallDamage == null) {
            return super.calculateFallDamage(fallDistance, pDamageMultiplier);
        } else {
            ContextUtils.CalculateFallDamageContext context = new ContextUtils.CalculateFallDamageContext(fallDistance, pDamageMultiplier, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.calculateFallDamage.apply(context), "integer");
            if (obj != null) {
                return (Integer)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for calculateFallDamage from entity: " + var10000 + ". Value: " + this.builder.calculateFallDamage.apply(context) + ". Must be an int, defaulting to " + super.calculateFallDamage(fallDistance, pDamageMultiplier));
                return super.calculateFallDamage(fallDistance, pDamageMultiplier);
            }
        }
    }

    protected boolean repositionEntityAfterLoad() {
        return (Boolean)Objects.requireNonNullElseGet(this.builder.repositionEntityAfterLoad, () -> {
            return super.repositionEntityAfterLoad();
        });
    }

    protected float nextStep() {
        if (this.builder.nextStep != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.nextStep.apply(this), "float");
            if (obj != null) {
                return (Float)obj;
            }

            String var10000 = this.entityName();
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for nextStep from entity: " + var10000 + ". Value: " + this.builder.nextStep.apply(this) + ". Must be a float, defaulting to " + super.nextStep());
        }

        return super.nextStep();
    }

    protected @Nullable SoundEvent getHurtSound(@NotNull DamageSource p_21239_) {
        if (this.builder.setHurtSound == null) {
            return super.getHurtSound(p_21239_);
        } else {
            ContextUtils.HurtContext context = new ContextUtils.HurtContext(this, p_21239_);
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.setHurtSound.apply(context), "resourcelocation");
            if (obj != null) {
                return (SoundEvent)Objects.requireNonNull((SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation)obj));
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setHurtSound from entity: " + var10000 + ". Value: " + this.builder.setHurtSound.apply(context) + ". Must be a ResourceLocation or String. Defaulting to \"minecraft:entity.generic.hurt\"");
                return super.getHurtSound(p_21239_);
            }
        }
    }

    protected SoundEvent getSwimSplashSound() {
        return this.builder.setSwimSplashSound == null ? super.getSwimSplashSound() : (SoundEvent)Objects.requireNonNull((SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation)this.builder.setSwimSplashSound));
    }

    protected SoundEvent getSwimSound() {
        return this.builder.setSwimSound == null ? super.getSwimSound() : (SoundEvent)Objects.requireNonNull((SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation)this.builder.setSwimSound));
    }

    public boolean canAttackType(@NotNull EntityType<?> entityType) {
        if (this.builder.canAttackType != null) {
            ContextUtils.EntityTypeEntityContext context = new ContextUtils.EntityTypeEntityContext(this, entityType);
            Object obj = this.builder.canAttackType.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttackType from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canAttackType(entityType));
        }

        return super.canAttackType(entityType);
    }

    public float getScale() {
        if (this.builder.scale == null) {
            return super.getScale();
        } else {
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.scale.apply(this), "float");
            if (obj != null) {
                return (Float)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for scale from entity: " + var10000 + ". Value: " + this.builder.scale.apply(this) + ". Must be a float. Defaulting to " + super.getScale());
                return super.getScale();
            }
        }
    }

    public boolean shouldDropExperience() {
        if (this.builder.shouldDropExperience != null) {
            Object obj = this.builder.shouldDropExperience.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropExperience from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.shouldDropExperience());
        }

        return super.shouldDropExperience();
    }

    public double getVisibilityPercent(@Nullable Entity p_20969_) {
        if (this.builder.visibilityPercent != null) {
            ContextUtils.VisualContext context = new ContextUtils.VisualContext(p_20969_, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.visibilityPercent.apply(context), "double");
            if (obj != null) {
                return (Double)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for visibilityPercent from entity: " + var10000 + ". Value: " + this.builder.visibilityPercent.apply(context) + ". Must be a double. Defaulting to " + super.getVisibilityPercent(p_20969_));
                return super.getVisibilityPercent(p_20969_);
            }
        } else {
            return super.getVisibilityPercent(p_20969_);
        }
    }

    public boolean canAttack(@NotNull LivingEntity entity) {
        if (this.builder.canAttack != null) {
            ContextUtils.LivingEntityContext context = new ContextUtils.LivingEntityContext(this, entity);
            Object obj = this.builder.canAttack.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj && super.canAttack(entity);
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttack from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canAttack(entity));
        }

        return super.canAttack(entity);
    }

    public boolean canBeAffected(@NotNull MobEffectInstance effectInstance) {
        if (this.builder.canBeAffected == null) {
            return super.canBeAffected(effectInstance);
        } else {
            ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
            Object result = this.builder.canBeAffected.apply(context);
            if (result instanceof Boolean) {
                return (Boolean)result;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeAffected from entity: " + this.entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + super.canBeAffected(effectInstance));
                return super.canBeAffected(effectInstance);
            }
        }
    }

    public boolean isInvertedHealAndHarm() {
        if (this.builder.invertedHealAndHarm == null) {
            return super.isInvertedHealAndHarm();
        } else {
            Object obj = this.builder.invertedHealAndHarm.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for invertedHealAndHarm from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isInvertedHealAndHarm());
                return super.isInvertedHealAndHarm();
            }
        }
    }

    protected SoundEvent getDeathSound() {
        return this.builder.setDeathSound == null ? super.getDeathSound() : (SoundEvent)Objects.requireNonNull((SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation)this.builder.setDeathSound));
    }

    @NotNull
    public LivingEntity.@NotNull Fallsounds getFallSounds() {
        return this.builder.fallSounds != null ? new LivingEntity.Fallsounds((SoundEvent)Objects.requireNonNull((SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation)this.builder.smallFallSound)), (SoundEvent)Objects.requireNonNull((SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation)this.builder.largeFallSound))) : super.getFallSounds();
    }

    public @NotNull SoundEvent getEatingSound(@NotNull ItemStack itemStack) {
        return this.builder.eatingSound != null ? (SoundEvent)Objects.requireNonNull((SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation)this.builder.eatingSound)) : super.getEatingSound(itemStack);
    }

    public boolean onClimbable() {
        if (this.builder.onClimbable == null) {
            return super.onClimbable();
        } else {
            Object obj = this.builder.onClimbable.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for onClimbable from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to super.onClimbable(): " + super.onClimbable());
                return super.onClimbable();
            }
        }
    }

    public boolean canBreatheUnderwater() {
        return (Boolean)Objects.requireNonNullElseGet(this.builder.canBreatheUnderwater, () -> {
            return super.canBreatheUnderwater();
        });
    }

    public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource damageSource) {
        if (this.builder.onLivingFall != null) {
            ContextUtils.EntityFallDamageContext context = new ContextUtils.EntityFallDamageContext(this, damageMultiplier, distance, damageSource);
            EntityJSHelperClass.consumerCallback(this.builder.onLivingFall, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onLivingFall.");
        }

        return super.causeFallDamage(distance, damageMultiplier, damageSource);
    }

    public void setSprinting(boolean sprinting) {
        if (this.builder.onSprint != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onSprint, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onSprint.");
        }

        super.setSprinting(sprinting);
    }

    public float getJumpBoostPower() {
        if (this.builder.jumpBoostPower == null) {
            return super.getJumpBoostPower();
        } else {
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.jumpBoostPower.apply(this), "float");
            if (obj != null) {
                return (Float)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for jumpBoostPower from entity: " + var10000 + ". Value: " + this.builder.jumpBoostPower.apply(this) + ". Must be a float. Defaulting to " + super.getJumpBoostPower());
                return super.getJumpBoostPower();
            }
        }
    }

    public boolean canStandOnFluid(@NotNull FluidState fluidState) {
        if (this.builder.canStandOnFluid != null) {
            ContextUtils.EntityFluidStateContext context = new ContextUtils.EntityFluidStateContext(this, fluidState);
            Object obj = this.builder.canStandOnFluid.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canStandOnFluid from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canStandOnFluid(fluidState));
        }

        return super.canStandOnFluid(fluidState);
    }

    public boolean isSensitiveToWater() {
        if (this.builder.isSensitiveToWater != null) {
            Object obj = this.builder.isSensitiveToWater.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSensitiveToWater from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isSensitiveToWater());
        }

        return super.isSensitiveToWater();
    }

    public void stopRiding() {
        super.stopRiding();
        if (this.builder.onStopRiding != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onStopRiding, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onStopRiding.");
        }

    }

    public void rideTick() {
        super.rideTick();
        if (this.builder.rideTick != null) {
            EntityJSHelperClass.consumerCallback(this.builder.rideTick, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: rideTick.");
        }

    }

    public void onItemPickup(@NotNull ItemEntity p_21054_) {
        super.onItemPickup(p_21054_);
        if (this.builder.onItemPickup != null) {
            ContextUtils.EntityItemEntityContext context = new ContextUtils.EntityItemEntityContext(this, p_21054_);
            EntityJSHelperClass.consumerCallback(this.builder.onItemPickup, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onItemPickup.");
        }

    }

    public boolean hasLineOfSight(@NotNull Entity entity) {
        if (this.builder.hasLineOfSight != null) {
            ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(entity, this);
            Object obj = this.builder.hasLineOfSight.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for hasLineOfSight from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.hasLineOfSight(entity));
        }

        return super.hasLineOfSight(entity);
    }

    public void onEnterCombat() {
        if (this.builder.onEnterCombat != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onEnterCombat, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onEnterCombat.");
        } else {
            super.onEnterCombat();
        }

    }

    public void onLeaveCombat() {
        if (this.builder.onLeaveCombat != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onLeaveCombat, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onLeaveCombat.");
        }

        super.onLeaveCombat();
    }

    public boolean isAffectedByPotions() {
        if (this.builder.isAffectedByPotions != null) {
            Object obj = this.builder.isAffectedByPotions.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByPotions from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isAffectedByPotions());
        }

        return super.isAffectedByPotions();
    }

    public boolean attackable() {
        if (this.builder.isAttackable != null) {
            Object obj = this.builder.isAttackable.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAttackable from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.attackable());
        }

        return super.attackable();
    }

    public boolean canTakeItem(@NotNull ItemStack itemStack) {
        if (this.builder.canTakeItem != null) {
            ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, this.level());
            Object obj = this.builder.canTakeItem.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTakeItem from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canTakeItem(itemStack));
        }

        return super.canTakeItem(itemStack);
    }

    public boolean isSleeping() {
        if (this.builder.isSleeping != null) {
            Object obj = this.builder.isSleeping.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSleeping from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isSleeping());
        }

        return super.isSleeping();
    }

    public void startSleeping(@NotNull BlockPos blockPos) {
        if (this.builder.onStartSleeping != null) {
            ContextUtils.EntityBlockPosContext context = new ContextUtils.EntityBlockPosContext(this, blockPos);
            EntityJSHelperClass.consumerCallback(this.builder.onStartSleeping, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onStartSleeping.");
        }

        super.startSleeping(blockPos);
    }

    public void stopSleeping() {
        if (this.builder.onStopSleeping != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onStopSleeping, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onStopSleeping.");
        }

        super.stopSleeping();
    }

    public @NotNull ItemStack eat(@NotNull Level level, @NotNull ItemStack itemStack) {
        if (this.builder.eat != null) {
            ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, level);
            EntityJSHelperClass.consumerCallback(this.builder.eat, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: eat.");
            return itemStack;
        } else {
            return super.eat(level, itemStack);
        }
    }

    public boolean shouldRiderFaceForward(@NotNull Player player) {
        if (this.builder.shouldRiderFaceForward != null) {
            ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(player, this);
            Object obj = this.builder.shouldRiderFaceForward.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldRiderFaceForward from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.shouldRiderFaceForward(player));
        }

        return super.shouldRiderFaceForward(player);
    }

    public boolean canFreeze() {
        if (this.builder.canFreeze != null) {
            Object obj = this.builder.canFreeze.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canFreeze());
        }

        return super.canFreeze();
    }

    public boolean isFreezing() {
        if (this.builder.isFreezing != null) {
            Object obj = this.builder.isFreezing.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFreezing from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isFreezing());
        }

        return super.isFreezing();
    }

    public boolean isCurrentlyGlowing() {
        if (this.builder.isCurrentlyGlowing != null && !this.level().isClientSide()) {
            Object obj = this.builder.isCurrentlyGlowing.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isCurrentlyGlowing());
        }

        return super.isCurrentlyGlowing();
    }

    public boolean canDisableShield() {
        if (this.builder.canDisableShield != null) {
            Object obj = this.builder.canDisableShield.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canDisableShield from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canDisableShield());
        }

        return super.canDisableShield();
    }

    public void onClientRemoval() {
        if (this.builder.onClientRemoval != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onClientRemoval, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onClientRemoval.");
        }

        super.onClientRemoval();
    }

    public void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
        if (this.builder.onHurt != null) {
            ContextUtils.EntityDamageContext context = new ContextUtils.EntityDamageContext(pDamageSource, pDamageAmount, this);
            EntityJSHelperClass.consumerCallback(this.builder.onHurt, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: onHurt.");
        }

        super.actuallyHurt(pDamageSource, pDamageAmount);
    }

    public void lavaHurt() {
        if (this.builder.lavaHurt != null) {
            EntityJSHelperClass.consumerCallback(this.builder.lavaHurt, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: lavaHurt.");
        }

        super.lavaHurt();
    }

    public int getExperienceReward() {
        if (this.builder.experienceReward != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.experienceReward.apply(this), "integer");
            if (obj != null) {
                return (Integer)obj;
            }

            String var10000 = this.entityName();
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for experienceReward from entity: " + var10000 + ". Value: " + this.builder.experienceReward.apply(this) + ". Must be an integer. Defaulting to " + super.getExperienceReward());
        }

        return super.getExperienceReward();
    }

    public boolean dampensVibrations() {
        if (this.builder.dampensVibrations != null) {
            Object obj = this.builder.dampensVibrations.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for dampensVibrations from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.dampensVibrations());
        }

        return super.dampensVibrations();
    }

    public void playerTouch(Player p_20081_) {
        if (this.builder.playerTouch != null) {
            ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(p_20081_, this);
            EntityJSHelperClass.consumerCallback(this.builder.playerTouch, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: playerTouch.");
        }

    }

    public boolean showVehicleHealth() {
        if (this.builder.showVehicleHealth != null) {
            Object obj = this.builder.showVehicleHealth.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for showVehicleHealth from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.showVehicleHealth());
        }

        return super.showVehicleHealth();
    }

    public void thunderHit(ServerLevel p_19927_, LightningBolt p_19928_) {
        if (this.builder.thunderHit != null) {
            super.thunderHit(p_19927_, p_19928_);
            ContextUtils.ThunderHitContext context = new ContextUtils.ThunderHitContext(p_19927_, p_19928_, this);
            EntityJSHelperClass.consumerCallback(this.builder.thunderHit, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: thunderHit.");
        }

    }

    public boolean isInvulnerableTo(DamageSource p_20122_) {
        if (this.builder.isInvulnerableTo != null) {
            ContextUtils.DamageContext context = new ContextUtils.DamageContext(this, p_20122_);
            Object obj = this.builder.isInvulnerableTo.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isInvulnerableTo from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isInvulnerableTo(p_20122_));
        }

        return super.isInvulnerableTo(p_20122_);
    }

    public boolean canChangeDimensions() {
        if (this.builder.canChangeDimensions != null) {
            Object obj = this.builder.canChangeDimensions.apply(this);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canChangeDimensions());
        }

        return super.canChangeDimensions();
    }

    public boolean mayInteract(@NotNull Level p_146843_, @NotNull BlockPos p_146844_) {
        if (this.builder.mayInteract != null) {
            ContextUtils.MayInteractContext context = new ContextUtils.MayInteractContext(p_146843_, p_146844_, this);
            Object obj = this.builder.mayInteract.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for mayInteract from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.mayInteract(p_146843_, p_146844_));
        }

        return super.mayInteract(p_146843_, p_146844_);
    }

    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        if (this.builder.canTrample != null) {
            ContextUtils.CanTrampleContext context = new ContextUtils.CanTrampleContext(state, pos, fallDistance, this);
            Object obj = this.builder.canTrample.apply(context);
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + this.entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canTrample(state, pos, fallDistance));
        }

        return super.canTrample(state, pos, fallDistance);
    }

    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (this.builder.onRemovedFromWorld != null) {
            EntityJSHelperClass.consumerCallback(this.builder.onRemovedFromWorld, this, "[EntityJS]: Error in " + this.entityName() + "builder for field: onRemovedFromWorld.");
        }

    }

    public int getMaxFallDistance() {
        if (this.builder.setMaxFallDistance == null) {
            return super.getMaxFallDistance();
        } else {
            Object obj = EntityJSHelperClass.convertObjectToDesired(this.builder.setMaxFallDistance.apply(this), "integer");
            if (obj != null) {
                return (Integer)obj;
            } else {
                String var10000 = this.entityName();
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + var10000 + ". Value: " + this.builder.setMaxFallDistance.apply(this) + ". Must be an integer. Defaulting to " + super.getMaxFallDistance());
                return super.getMaxFallDistance();
            }
        }
    }

    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        if (this.builder.lerpTo != null) {
            ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, teleport, this);
            EntityJSHelperClass.consumerCallback(this.builder.lerpTo, context, "[EntityJS]: Error in " + this.entityName() + "builder for field: lerpTo.");
        }

    }

    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    public Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    public ItemStack getItemBySlot(EquipmentSlot slot) {
        ItemStack var10000;
        switch (slot.getType()) {
            case HAND:
                var10000 = (ItemStack)this.handItems.get(slot.getIndex());
                break;
            case ARMOR:
                var10000 = (ItemStack)this.armorItems.get(slot.getIndex());
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        return var10000;
    }

    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        this.verifyEquippedItem(stack);
        switch (slot.getType()) {
            case HAND:
                this.onEquipItem(slot, (ItemStack)this.handItems.set(slot.getIndex(), stack), stack);
                break;
            case ARMOR:
                this.onEquipItem(slot, (ItemStack)this.armorItems.set(slot.getIndex(), stack), stack);
        }

    }
}