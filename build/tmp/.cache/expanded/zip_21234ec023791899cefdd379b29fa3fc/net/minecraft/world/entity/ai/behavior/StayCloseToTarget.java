package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StayCloseToTarget<E extends LivingEntity> extends Behavior<E> {
   private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
   private final int closeEnough;
   private final int tooFar;
   private final float speedModifier;

   public StayCloseToTarget(Function<LivingEntity, Optional<PositionTracker>> pTargetPositionGetter, int pCloseEnough, int pTooFar, float pSpeedModifier) {
      super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
      this.targetPositionGetter = pTargetPositionGetter;
      this.closeEnough = pCloseEnough;
      this.tooFar = pTooFar;
      this.speedModifier = pSpeedModifier;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
      Optional<PositionTracker> optional = this.targetPositionGetter.apply(pOwner);
      if (optional.isEmpty()) {
         return false;
      } else {
         PositionTracker positiontracker = optional.get();
         return !pOwner.position().closerThan(positiontracker.currentPosition(), (double)this.tooFar);
      }
   }

   protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
      BehaviorUtils.setWalkAndLookTargetMemories(pEntity, this.targetPositionGetter.apply(pEntity).get(), this.speedModifier, this.closeEnough);
   }
}