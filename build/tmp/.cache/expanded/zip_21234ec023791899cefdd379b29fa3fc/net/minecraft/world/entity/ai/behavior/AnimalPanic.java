package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class AnimalPanic extends Behavior<PathfinderMob> {
   private static final int PANIC_MIN_DURATION = 100;
   private static final int PANIC_MAX_DURATION = 120;
   private static final int PANIC_DISTANCE_HORIZONTAL = 5;
   private static final int PANIC_DISTANCE_VERTICAL = 4;
   private final float speedMultiplier;

   public AnimalPanic(float pSpeedMultiplier) {
      super(ImmutableMap.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT), 100, 120);
      this.speedMultiplier = pSpeedMultiplier;
   }

   protected boolean canStillUse(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      return true;
   }

   protected void start(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      pEntity.getBrain().setMemory(MemoryModuleType.IS_PANICKING, true);
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
   }

   protected void stop(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      brain.eraseMemory(MemoryModuleType.IS_PANICKING);
   }

   protected void tick(ServerLevel pLevel, PathfinderMob pOwner, long pGameTime) {
      if (pOwner.getNavigation().isDone()) {
         Vec3 vec3 = this.getPanicPos(pOwner, pLevel);
         if (vec3 != null) {
            pOwner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedMultiplier, 0));
         }
      }

   }

   @Nullable
   private Vec3 getPanicPos(PathfinderMob pPathfinder, ServerLevel pLevel) {
      if (pPathfinder.isOnFire()) {
         Optional<Vec3> optional = this.lookForWater(pLevel, pPathfinder).map(Vec3::atBottomCenterOf);
         if (optional.isPresent()) {
            return optional.get();
         }
      }

      return LandRandomPos.getPos(pPathfinder, 5, 4);
   }

   private Optional<BlockPos> lookForWater(BlockGetter pLevel, Entity pEntity) {
      BlockPos blockpos = pEntity.blockPosition();
      return !pLevel.getBlockState(blockpos).getCollisionShape(pLevel, blockpos).isEmpty() ? Optional.empty() : BlockPos.findClosestMatch(blockpos, 5, 1, (p_196646_) -> {
         return pLevel.getFluidState(p_196646_).is(FluidTags.WATER);
      });
   }
}