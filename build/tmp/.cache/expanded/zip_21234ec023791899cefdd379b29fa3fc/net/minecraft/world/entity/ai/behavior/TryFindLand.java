package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class TryFindLand extends Behavior<PathfinderMob> {
   private static final int COOLDOWN_TICKS = 60;
   private final int range;
   private final float speedModifier;
   private long nextOkStartTime;

   public TryFindLand(int pRange, float pSpeedModifier) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
      this.range = pRange;
      this.speedModifier = pSpeedModifier;
   }

   protected void stop(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      this.nextOkStartTime = pGameTime + 60L;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, PathfinderMob pOwner) {
      return pOwner.level.getFluidState(pOwner.blockPosition()).is(FluidTags.WATER);
   }

   protected void start(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      if (pGameTime >= this.nextOkStartTime) {
         BlockPos blockpos = pEntity.blockPosition();
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         CollisionContext collisioncontext = CollisionContext.of(pEntity);

         for(BlockPos blockpos1 : BlockPos.withinManhattan(blockpos, this.range, this.range, this.range)) {
            if (blockpos1.getX() != blockpos.getX() || blockpos1.getZ() != blockpos.getZ()) {
               BlockState blockstate = pLevel.getBlockState(blockpos1);
               BlockState blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos1, Direction.DOWN));
               if (!blockstate.is(Blocks.WATER) && pLevel.getFluidState(blockpos1).isEmpty() && blockstate.getCollisionShape(pLevel, blockpos1, collisioncontext).isEmpty() && blockstate1.isFaceSturdy(pLevel, blockpos$mutableblockpos, Direction.UP)) {
                  this.nextOkStartTime = pGameTime + 60L;
                  BehaviorUtils.setWalkAndLookTargetMemories(pEntity, blockpos1.immutable(), this.speedModifier, 1);
                  return;
               }
            }
         }

      }
   }
}