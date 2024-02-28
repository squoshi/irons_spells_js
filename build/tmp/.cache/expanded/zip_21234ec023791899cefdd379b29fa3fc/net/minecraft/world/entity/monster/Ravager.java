package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Ravager extends Raider {
   private static final Predicate<Entity> NO_RAVAGER_AND_ALIVE = (p_33346_) -> {
      return p_33346_.isAlive() && !(p_33346_ instanceof Ravager);
   };
   private static final double BASE_MOVEMENT_SPEED = 0.3D;
   private static final double ATTACK_MOVEMENT_SPEED = 0.35D;
   private static final int STUNNED_COLOR = 8356754;
   private static final double STUNNED_COLOR_BLUE = 0.5725490196078431D;
   private static final double STUNNED_COLOR_GREEN = 0.5137254901960784D;
   private static final double STUNNED_COLOR_RED = 0.4980392156862745D;
   private static final int ATTACK_DURATION = 10;
   public static final int STUN_DURATION = 40;
   private int attackTick;
   private int stunnedTick;
   private int roarTick;

   public Ravager(EntityType<? extends Ravager> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.maxUpStep = 1.0F;
      this.xpReward = 20;
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(4, new Ravager.RavagerMeleeAttackGoal());
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4D));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
      this.targetSelector.addGoal(2, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers());
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true, (p_199899_) -> {
         return !p_199899_.isBaby();
      }));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
   }

   /**
    * Sets MOVE, JUMP, LOOK Goal.Flags depending if entity is riding or been controlled
    */
   protected void updateControlFlags() {
      boolean flag = !(this.getControllingPassenger() instanceof Mob) || this.getControllingPassenger().getType().is(EntityTypeTags.RAIDERS);
      boolean flag1 = !(this.getVehicle() instanceof Boat);
      this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
      this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag && flag1);
      this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
      this.goalSelector.setControlFlag(Goal.Flag.TARGET, flag);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0D).add(Attributes.MOVEMENT_SPEED, 0.3D).add(Attributes.KNOCKBACK_RESISTANCE, 0.75D).add(Attributes.ATTACK_DAMAGE, 12.0D).add(Attributes.ATTACK_KNOCKBACK, 1.5D).add(Attributes.FOLLOW_RANGE, 32.0D);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("AttackTick", this.attackTick);
      pCompound.putInt("StunTick", this.stunnedTick);
      pCompound.putInt("RoarTick", this.roarTick);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.attackTick = pCompound.getInt("AttackTick");
      this.stunnedTick = pCompound.getInt("StunTick");
      this.roarTick = pCompound.getInt("RoarTick");
   }

   public SoundEvent getCelebrateSound() {
      return SoundEvents.RAVAGER_CELEBRATE;
   }

   protected PathNavigation createNavigation(Level pLevel) {
      return new Ravager.RavagerNavigation(this, pLevel);
   }

   public int getMaxHeadYRot() {
      return 45;
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return 2.1D;
   }

   /**
    * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
    * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
    */
   @Nullable
   public Entity getControllingPassenger() {
      Entity entity = this.getFirstPassenger();
      return entity != null && this.canBeControlledBy(entity) ? entity : null;
   }

   private boolean canBeControlledBy(Entity p_219063_) {
      return !this.isNoAi() && p_219063_ instanceof LivingEntity;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.isAlive()) {
         if (this.isImmobile()) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
         } else {
            double d0 = this.getTarget() != null ? 0.35D : 0.3D;
            double d1 = this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Mth.lerp(0.1D, d1, d0));
         }

         if (this.horizontalCollision && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
            boolean flag = false;
            AABB aabb = this.getBoundingBox().inflate(0.2D);

            for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
               BlockState blockstate = this.level.getBlockState(blockpos);
               Block block = blockstate.getBlock();
               if (block instanceof LeavesBlock) {
                  flag = this.level.destroyBlock(blockpos, true, this) || flag;
               }
            }

            if (!flag && this.onGround) {
               this.jumpFromGround();
            }
         }

         if (this.roarTick > 0) {
            --this.roarTick;
            if (this.roarTick == 10) {
               this.roar();
            }
         }

         if (this.attackTick > 0) {
            --this.attackTick;
         }

         if (this.stunnedTick > 0) {
            --this.stunnedTick;
            this.stunEffect();
            if (this.stunnedTick == 0) {
               this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.0F);
               this.roarTick = 20;
            }
         }

      }
   }

   private void stunEffect() {
      if (this.random.nextInt(6) == 0) {
         double d0 = this.getX() - (double)this.getBbWidth() * Math.sin((double)(this.yBodyRot * ((float)Math.PI / 180F))) + (this.random.nextDouble() * 0.6D - 0.3D);
         double d1 = this.getY() + (double)this.getBbHeight() - 0.3D;
         double d2 = this.getZ() + (double)this.getBbWidth() * Math.cos((double)(this.yBodyRot * ((float)Math.PI / 180F))) + (this.random.nextDouble() * 0.6D - 0.3D);
         this.level.addParticle(ParticleTypes.ENTITY_EFFECT, d0, d1, d2, 0.4980392156862745D, 0.5137254901960784D, 0.5725490196078431D);
      }

   }

   /**
    * Dead and sleeping entities cannot move
    */
   protected boolean isImmobile() {
      return super.isImmobile() || this.attackTick > 0 || this.stunnedTick > 0 || this.roarTick > 0;
   }

   public boolean hasLineOfSight(Entity pEntity) {
      return this.stunnedTick <= 0 && this.roarTick <= 0 ? super.hasLineOfSight(pEntity) : false;
   }

   protected void blockedByShield(LivingEntity pEntity) {
      if (this.roarTick == 0) {
         if (this.random.nextDouble() < 0.5D) {
            this.stunnedTick = 40;
            this.playSound(SoundEvents.RAVAGER_STUNNED, 1.0F, 1.0F);
            this.level.broadcastEntityEvent(this, (byte)39);
            pEntity.push(this);
         } else {
            this.strongKnockback(pEntity);
         }

         pEntity.hurtMarked = true;
      }

   }

   private void roar() {
      if (this.isAlive()) {
         for(LivingEntity livingentity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0D), NO_RAVAGER_AND_ALIVE)) {
            if (!(livingentity instanceof AbstractIllager)) {
               livingentity.hurt(DamageSource.mobAttack(this), 6.0F);
            }

            this.strongKnockback(livingentity);
         }

         Vec3 vec3 = this.getBoundingBox().getCenter();

         for(int i = 0; i < 40; ++i) {
            double d0 = this.random.nextGaussian() * 0.2D;
            double d1 = this.random.nextGaussian() * 0.2D;
            double d2 = this.random.nextGaussian() * 0.2D;
            this.level.addParticle(ParticleTypes.POOF, vec3.x, vec3.y, vec3.z, d0, d1, d2);
         }

         this.gameEvent(GameEvent.ENTITY_ROAR);
      }

   }

   private void strongKnockback(Entity pEntity) {
      double d0 = pEntity.getX() - this.getX();
      double d1 = pEntity.getZ() - this.getZ();
      double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
      pEntity.push(d0 / d2 * 4.0D, 0.2D, d1 / d2 * 4.0D);
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 4) {
         this.attackTick = 10;
         this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
      } else if (pId == 39) {
         this.stunnedTick = 40;
      }

      super.handleEntityEvent(pId);
   }

   public int getAttackTick() {
      return this.attackTick;
   }

   public int getStunnedTick() {
      return this.stunnedTick;
   }

   public int getRoarTick() {
      return this.roarTick;
   }

   public boolean doHurtTarget(Entity pEntity) {
      this.attackTick = 10;
      this.level.broadcastEntityEvent(this, (byte)4);
      this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
      return super.doHurtTarget(pEntity);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.RAVAGER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.RAVAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.RAVAGER_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.RAVAGER_STEP, 0.15F, 1.0F);
   }

   public boolean checkSpawnObstruction(LevelReader pLevel) {
      return !pLevel.containsAnyLiquid(this.getBoundingBox());
   }

   public void applyRaidBuffs(int pWave, boolean pUnusedFalse) {
   }

   public boolean canBeLeader() {
      return false;
   }

   class RavagerMeleeAttackGoal extends MeleeAttackGoal {
      public RavagerMeleeAttackGoal() {
         super(Ravager.this, 1.0D, true);
      }

      protected double getAttackReachSqr(LivingEntity pAttackTarget) {
         float f = Ravager.this.getBbWidth() - 0.1F;
         return (double)(f * 2.0F * f * 2.0F + pAttackTarget.getBbWidth());
      }
   }

   static class RavagerNavigation extends GroundPathNavigation {
      public RavagerNavigation(Mob p_33379_, Level p_33380_) {
         super(p_33379_, p_33380_);
      }

      protected PathFinder createPathFinder(int p_33382_) {
         this.nodeEvaluator = new Ravager.RavagerNodeEvaluator();
         return new PathFinder(this.nodeEvaluator, p_33382_);
      }
   }

   static class RavagerNodeEvaluator extends WalkNodeEvaluator {
      /**
       * Returns the exact path node type according to abilities and settings of the entity
       */
      protected BlockPathTypes evaluateBlockPathType(BlockGetter pLevel, boolean pCanOpenDoors, boolean pCanEnterDoors, BlockPos pPos, BlockPathTypes pNodeType) {
         return pNodeType == BlockPathTypes.LEAVES ? BlockPathTypes.OPEN : super.evaluateBlockPathType(pLevel, pCanOpenDoors, pCanEnterDoors, pPos, pNodeType);
      }
   }
}
