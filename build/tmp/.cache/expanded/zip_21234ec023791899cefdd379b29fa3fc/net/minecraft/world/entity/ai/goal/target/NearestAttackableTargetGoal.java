package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
   private static final int DEFAULT_RANDOM_INTERVAL = 10;
   protected final Class<T> targetType;
   protected final int randomInterval;
   @Nullable
   protected LivingEntity target;
   /** This filter is applied to the Entity search. Only matching entities will be targeted. */
   protected TargetingConditions targetConditions;

   public NearestAttackableTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee) {
      this(pMob, pTargetType, 10, pMustSee, false, (Predicate<LivingEntity>)null);
   }

   public NearestAttackableTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, Predicate<LivingEntity> pTargetPredicate) {
      this(pMob, pTargetType, 10, pMustSee, false, pTargetPredicate);
   }

   public NearestAttackableTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, boolean pMustReach) {
      this(pMob, pTargetType, 10, pMustSee, pMustReach, (Predicate<LivingEntity>)null);
   }

   public NearestAttackableTargetGoal(Mob pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate) {
      super(pMob, pMustSee, pMustReach);
      this.targetType = pTargetType;
      this.randomInterval = reducedTickDelay(pRandomInterval);
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
      this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(pTargetPredicate);
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
         return false;
      } else {
         this.findTarget();
         return this.target != null;
      }
   }

   protected AABB getTargetSearchArea(double pTargetDistance) {
      return this.mob.getBoundingBox().inflate(pTargetDistance, 4.0D, pTargetDistance);
   }

   protected void findTarget() {
      if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
         this.target = this.mob.level.getNearestEntity(this.mob.level.getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), (p_148152_) -> {
            return true;
         }), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
      } else {
         this.target = this.mob.level.getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
      }

   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.setTarget(this.target);
      super.start();
   }

   public void setTarget(@Nullable LivingEntity pTarget) {
      this.target = pTarget;
   }
}