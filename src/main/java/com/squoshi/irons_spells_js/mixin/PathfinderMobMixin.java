package com.squoshi.irons_spells_js.mixin;

import com.google.common.collect.Maps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
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
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.fire.BurningDashSpell;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.UUID;

// pain.
@Mixin(PathfinderMob.class)
public class PathfinderMobMixin extends Mob implements IMagicEntity {
	protected PathfinderMobMixin(EntityType<? extends Mob> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}
	private boolean recreateSpell;
	@Unique
	private static final EntityDataAccessor<Boolean> DATA_CANCEL_CAST;
	@Unique
	private static final EntityDataAccessor<Boolean> DATA_DRINKING_POTION;
	@Unique
	private final MagicData playerMagicData = new MagicData(true);
	@Unique
	private static final AttributeModifier SPEED_MODIFIER_DRINKING;

	@Unique
	public @Nullable SpellData castingSpell;
	@Unique
	public final HashMap<String, AbstractSpell> spells = Maps.newHashMap();
	@Unique
	public int drinkTime;
	@Unique
	public boolean hasUsedSingleAttack;

	public PathfinderMob self() {
		return (PathfinderMob) (Object) this;
	}

	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	public void init(EntityType<? extends PathfinderMob> entityType, Level level, CallbackInfo ci) {
		if (!(self() instanceof AbstractSpellCastingMob)){
			playerMagicData.setSyncedData(new SyncedSpellData(self()));
			this.lookControl = createLookControl();
		}
	}

	@Unique
	private LookControl createLookControl() {
		return new LookControl(this) {
			@Override
			protected boolean resetXRotOnTick() {
				return getTarget() == null;
			}
		};
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
		super.defineSynchedData(pBuilder);
		//pBuilder.define(DATA_SPELL, new SyncedSpellData(-1));
		pBuilder.define(DATA_CANCEL_CAST, false);
		pBuilder.define(DATA_DRINKING_POTION, false);
	}
	@Override
	public MagicData getMagicData() {
		return playerMagicData;
	}

	@Override
	public boolean isDrinkingPotion() {
		return (Boolean) this.entityData.get(DATA_DRINKING_POTION);
	}

	public void setDrinkingPotion(boolean drinkingPotion) {
		this.entityData.set(DATA_DRINKING_POTION, drinkingPotion);
	}

	@Override
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
		if (this.isSilent()) {
			this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
		super.onSyncedDataUpdated(pKey);

		if (!this.level().isClientSide) {
			return;
		}

		if (pKey.id() == DATA_CANCEL_CAST.id()) {
			if (Log.SPELL_DEBUG) {
				IronsSpellbooks.LOGGER.debug("ASCM.onSyncedDataUpdated.1 this.isCasting:{}, playerMagicData.isCasting:{} isClient:{}", isCasting(), playerMagicData == null ? "null" : playerMagicData.isCasting(), this.level().isClientSide());
			}
			cancelCast();
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		if (!(self() instanceof AbstractSpellCastingMob)){
			playerMagicData.getSyncedData().saveNBTData(pCompound, level().registryAccess());
			pCompound.putBoolean("usedSpecial", hasUsedSingleAttack);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		if (!(self() instanceof AbstractSpellCastingMob)){
			var syncedSpellData = new SyncedSpellData(this);
			syncedSpellData.loadNBTData(pCompound, this.level().registryAccess());
			if (syncedSpellData.isCasting()) {
				this.recreateSpell = true;
			}
			playerMagicData.setSyncedData(syncedSpellData);
			hasUsedSingleAttack = pCompound.getBoolean("usedSpecial");
		}
	}

	@Override
	public void cancelCast() {
		if (this.isCasting()) {
			if (!this.level().isClientSide) {
				this.entityData.set(DATA_CANCEL_CAST, !(Boolean)this.entityData.get(DATA_CANCEL_CAST));
			}
			this.castComplete();
		}
	}

	@Override
	public void castComplete() {
		if (!this.level().isClientSide) {
			if (this.castingSpell != null) {
				this.castingSpell.getSpell().onServerCastComplete(this.level(), this.castingSpell.getLevel(), self(), playerMagicData, false);
			}
		} else {
			playerMagicData.resetCastingState();
		}
		this.castingSpell = null;
	}

	@Override
	public void setSyncedSpellData(SyncedSpellData syncedSpellData) {
		if (this.level().isClientSide) {
			boolean isCasting = playerMagicData.isCasting();
			playerMagicData.setSyncedData(syncedSpellData);
			this.castingSpell = playerMagicData.getCastingSpell();
			if (this.castingSpell != null) {
				if (!playerMagicData.isCasting() && isCasting) {
					this.castComplete();
				} else if (playerMagicData.isCasting() && !isCasting) {
					AbstractSpell spell = playerMagicData.getCastingSpell().getSpell();
					this.initiateCastSpell(spell, playerMagicData.getCastingSpellLevel());
					if (this.castingSpell.getSpell().getCastType() == CastType.INSTANT) {
						this.castingSpell.getSpell().onClientPreCast(this.level(), this.castingSpell.getLevel(), self(), InteractionHand.MAIN_HAND, playerMagicData);
						this.castComplete();
					}
				}

			}
		}
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (recreateSpell) {
			recreateSpell = false;
			var syncedSpellData = playerMagicData.getSyncedData();
			var spell = SpellRegistry.getSpell(syncedSpellData.getCastingSpellId());
			this.initiateCastSpell(spell, syncedSpellData.getCastingSpellLevel());
			//setSyncedSpellData(syncedSpellData);
		}

		if (isDrinkingPotion()) {
			if (drinkTime-- <= 0) {
				finishDrinkingPotion();
			} else if (drinkTime % 4 == 0) {
				if (!this.isSilent()) {
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_DRINK, this.getSoundSource(), 1.0F, Utils.random.nextFloat() * 0.1F + 0.9F);
				}
			}
		}

		if (castingSpell == null) {
			return;
		}

		playerMagicData.handleCastDuration();

		if (playerMagicData.isCasting()) {
			castingSpell.getSpell().onServerCastTick(level(), castingSpell.getLevel(), this, playerMagicData);
		}

		if (Log.SPELL_DEBUG) {
			IronsSpellbooks.LOGGER.debug("ASCM.customServerAiStep.1");
		}

		this.forceLookAtTarget(getTarget());

		if (playerMagicData.getCastDurationRemaining() <= 0) {
			if (Log.SPELL_DEBUG) {
				IronsSpellbooks.LOGGER.debug("ASCM.customServerAiStep.2");
			}

			if (castingSpell.getSpell().getCastType() == CastType.LONG || castingSpell.getSpell().getCastType() == CastType.INSTANT) {
				if (Log.SPELL_DEBUG) {
					IronsSpellbooks.LOGGER.debug("ASCM.customServerAiStep.3");
				}
				castingSpell.getSpell().onCast(level(), castingSpell.getLevel(), this, CastSource.MOB, playerMagicData);
			}
			castComplete();
		} else if (castingSpell.getSpell().getCastType() == CastType.CONTINUOUS) {
			if ((playerMagicData.getCastDurationRemaining() + 1) % 10 == 0) {
				castingSpell.getSpell().onCast(this.level(), castingSpell.getLevel(), this, CastSource.MOB, playerMagicData);
			}
		}
	}

	@Override
	public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
		if (spell == SpellRegistry.none()) {
			this.castingSpell = null;
		} else {

			this.castingSpell = new SpellData(spell, spellLevel);
			if (this.getTarget() != null) {
				this.forceLookAtTarget(this.getTarget());
			}

			if (!this.level().isClientSide && !this.castingSpell.getSpell().checkPreCastConditions(this.level(), spellLevel, self(), playerMagicData)) {
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

				playerMagicData.initiateCast(this.castingSpell.getSpell(), this.castingSpell.getLevel(), this.castingSpell.getSpell().getEffectiveCastTime(this.castingSpell.getLevel(), self()), CastSource.MOB, SpellSelectionManager.MAINHAND);
				if (!this.level().isClientSide) {
					this.castingSpell.getSpell().onServerPreCast(this.level(), this.castingSpell.getLevel(), self(), playerMagicData);
				}

			}
		}
	}

	@Override
	public void notifyDangerousProjectile(Projectile projectile) {
	}

	@Override
	public boolean isCasting() {
		return playerMagicData.isCasting();
	}

	@Override
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
				if (!this.level().collidesWithSuffocatingBlock(self(), reposBB.inflate(-0.05000000074505806))) {
					valid = true;
					break;
				}
			}

			if (valid) {
				playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(teleportPos));
			} else {
				playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(this.position()));
			}
		} else {
			playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(this.position()));
		}

		return valid;
	}

	@Override
	public void setBurningDashDirectionData() {
		playerMagicData.setAdditionalCastData(new BurningDashSpell.BurningDashDirectionOverrideCastData());
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

	@Override
	public boolean getHasUsedSingleAttack() {
		return this.hasUsedSingleAttack;
	}

	@Override
	public void setHasUsedSingleAttack(boolean hasUsedSingleAttack) {
		this.hasUsedSingleAttack = hasUsedSingleAttack;
	}

	static {
		DATA_CANCEL_CAST = SynchedEntityData.defineId(PathfinderMob.class, EntityDataSerializers.BOOLEAN);
		DATA_DRINKING_POTION = SynchedEntityData.defineId(PathfinderMob.class, EntityDataSerializers.BOOLEAN);
		SPEED_MODIFIER_DRINKING =  new AttributeModifier(IronsSpellbooks.id("potion_slowdown"), -0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}