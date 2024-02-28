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
import net.minecraft.world.phys.shapes.CollisionContext;

public class TryFindLandNearWater extends Behavior<PathfinderMob> {
   private final int range;
   private final float speedModifier;
   private long nextOkStartTime;

   public TryFindLandNearWater(int pRange, float pSpeedModifier) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
      this.range = pRange;
      this.speedModifier = pSpeedModifier;
   }

   protected void stop(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      this.nextOkStartTime = pGameTime + 40L;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, PathfinderMob pOwner) {
      return !pOwner.level.getFluidState(pOwner.blockPosition()).is(FluidTags.WATER);
   }

   protected void start(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      if (pGameTime >= this.nextOkStartTime) {
         CollisionContext collisioncontext = CollisionContext.of(pEntity);
         BlockPos blockpos = pEntity.blockPosition();
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(BlockPos blockpos1 : BlockPos.withinManhattan(blockpos, this.range, this.range, this.range)) {
            if ((blockpos1.getX() != blockpos.getX() || blockpos1.getZ() != blockpos.getZ()) && pLevel.getBlockState(blockpos1).getCollisionShape(pLevel, blockpos1, collisioncontext).isEmpty() && !pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos1, Direction.DOWN)).getCollisionShape(pLevel, blockpos1, collisioncontext).isEmpty()) {
               for(Direction direction : Direction.Plane.HORIZONTAL) {
                  blockpos$mutableblockpos.setWithOffset(blockpos1, direction);
                  if (pLevel.getBlockState(blockpos$mutableblockpos).isAir() && pLevel.getBlockState(blockpos$mutableblockpos.move(Direction.DOWN)).is(Blocks.WATER)) {
                     this.nextOkStartTime = pGameTime + 40L;
                     BehaviorUtils.setWalkAndLookTargetMemories(pEntity, blockpos1, this.speedModifier, 0);
                     return;
                  }
               }
            }
         }

      }
   }
}