package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;

public class TryLaySpawnOnWaterNearLand extends Behavior<Frog> {
   private final Block spawnBlock;
   private final MemoryModuleType<?> memoryModule;

   public TryLaySpawnOnWaterNearLand(Block pSpawnBlock, MemoryModuleType<?> pMemoryModule) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.IS_PREGNANT, MemoryStatus.VALUE_PRESENT));
      this.spawnBlock = pSpawnBlock;
      this.memoryModule = pMemoryModule;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Frog pOwner) {
      return !pOwner.isInWater() && pOwner.isOnGround();
   }

   protected void start(ServerLevel pLevel, Frog pEntity, long pGameTime) {
      BlockPos blockpos = pEntity.blockPosition().below();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos1 = blockpos.relative(direction);
         if (pLevel.getBlockState(blockpos1).getCollisionShape(pLevel, blockpos1).getFaceShape(Direction.UP).isEmpty() && pLevel.getFluidState(blockpos1).is(Fluids.WATER)) {
            BlockPos blockpos2 = blockpos1.above();
            if (pLevel.getBlockState(blockpos2).isAir()) {
               pLevel.setBlock(blockpos2, this.spawnBlock.defaultBlockState(), 3);
               pLevel.playSound((Player)null, pEntity, SoundEvents.FROG_LAY_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
               pEntity.getBrain().eraseMemory(this.memoryModule);
               return;
            }
         }
      }

   }
}