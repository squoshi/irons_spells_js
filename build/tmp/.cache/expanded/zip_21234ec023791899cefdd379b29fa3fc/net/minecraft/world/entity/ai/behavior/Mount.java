package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class Mount<E extends LivingEntity> extends Behavior<E> {
   private static final int CLOSE_ENOUGH_TO_START_RIDING_DIST = 1;
   private final float speedModifier;

   public Mount(float pSpeedModifier) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.RIDE_TARGET, MemoryStatus.VALUE_PRESENT));
      this.speedModifier = pSpeedModifier;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
      return !pOwner.isPassenger();
   }

   protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
      if (this.isCloseEnoughToStartRiding(pEntity)) {
         pEntity.startRiding(this.getRidableEntity(pEntity));
      } else {
         BehaviorUtils.setWalkAndLookTargetMemories(pEntity, this.getRidableEntity(pEntity), this.speedModifier, 1);
      }

   }

   private boolean isCloseEnoughToStartRiding(E pEntity) {
      return this.getRidableEntity(pEntity).closerThan(pEntity, 1.0D);
   }

   private Entity getRidableEntity(E pEntity) {
      return pEntity.getBrain().getMemory(MemoryModuleType.RIDE_TARGET).get();
   }
}