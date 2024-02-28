package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GoToTargetLocation<E extends Mob> extends Behavior<E> {
   private final MemoryModuleType<BlockPos> locationMemory;
   private final int closeEnoughDist;
   private final float speedModifier;

   public GoToTargetLocation(MemoryModuleType<BlockPos> pLocationMemory, int pCloseEnoughDist, float pSpeedModifier) {
      super(ImmutableMap.of(pLocationMemory, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
      this.locationMemory = pLocationMemory;
      this.closeEnoughDist = pCloseEnoughDist;
      this.speedModifier = pSpeedModifier;
   }

   protected void start(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      BlockPos blockpos = this.getTargetLocation(pEntity);
      boolean flag = blockpos.closerThan(pEntity.blockPosition(), (double)this.closeEnoughDist);
      if (!flag) {
         BehaviorUtils.setWalkAndLookTargetMemories(pEntity, getNearbyPos(pEntity, blockpos), this.speedModifier, this.closeEnoughDist);
      }

   }

   private static BlockPos getNearbyPos(Mob pMob, BlockPos pPos) {
      RandomSource randomsource = pMob.level.random;
      return pPos.offset(getRandomOffset(randomsource), 0, getRandomOffset(randomsource));
   }

   private static int getRandomOffset(RandomSource pRandom) {
      return pRandom.nextInt(3) - 1;
   }

   private BlockPos getTargetLocation(Mob pMob) {
      return pMob.getBrain().getMemory(this.locationMemory).get();
   }
}