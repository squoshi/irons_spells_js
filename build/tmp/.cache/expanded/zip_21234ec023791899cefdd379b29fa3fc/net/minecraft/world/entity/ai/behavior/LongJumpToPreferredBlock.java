package net.minecraft.world.entity.ai.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LongJumpToPreferredBlock<E extends Mob> extends LongJumpToRandomPos<E> {
   private final TagKey<Block> preferredBlockTag;
   private final float preferredBlocksChance;
   private final List<LongJumpToRandomPos.PossibleJump> notPrefferedJumpCandidates = new ArrayList<>();
   private boolean currentlyWantingPreferredOnes;

   public LongJumpToPreferredBlock(UniformInt pTimeBetweenLongJumps, int pMaxLongJumpHeight, int pMaxLongJumpWidth, float pMaxLongJumpVelocity, Function<E, SoundEvent> pGetJumpSound, TagKey<Block> pPreferredBlockTag, float pPreferredBlocksChance, Predicate<BlockState> pAcceptableLandingSpot) {
      super(pTimeBetweenLongJumps, pMaxLongJumpHeight, pMaxLongJumpWidth, pMaxLongJumpVelocity, pGetJumpSound, pAcceptableLandingSpot);
      this.preferredBlockTag = pPreferredBlockTag;
      this.preferredBlocksChance = pPreferredBlocksChance;
   }

   protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
      super.start(pLevel, pEntity, pGameTime);
      this.notPrefferedJumpCandidates.clear();
      this.currentlyWantingPreferredOnes = pEntity.getRandom().nextFloat() < this.preferredBlocksChance;
   }

   protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel pLevel) {
      if (!this.currentlyWantingPreferredOnes) {
         return super.getJumpCandidate(pLevel);
      } else {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         while(!this.jumpCandidates.isEmpty()) {
            Optional<LongJumpToRandomPos.PossibleJump> optional = super.getJumpCandidate(pLevel);
            if (optional.isPresent()) {
               LongJumpToRandomPos.PossibleJump longjumptorandompos$possiblejump = optional.get();
               if (pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(longjumptorandompos$possiblejump.getJumpTarget(), Direction.DOWN)).is(this.preferredBlockTag)) {
                  return optional;
               }

               this.notPrefferedJumpCandidates.add(longjumptorandompos$possiblejump);
            }
         }

         return !this.notPrefferedJumpCandidates.isEmpty() ? Optional.of(this.notPrefferedJumpCandidates.remove(0)) : Optional.empty();
      }
   }

   protected boolean isAcceptableLandingPosition(ServerLevel pLevel, E pEntity, BlockPos pPos) {
      return super.isAcceptableLandingPosition(pLevel, pEntity, pPos) && this.willNotLandInFluid(pLevel, pPos);
   }

   private boolean willNotLandInFluid(ServerLevel pLevel, BlockPos pPos) {
      return pLevel.getFluidState(pPos).isEmpty() && pLevel.getFluidState(pPos.below()).isEmpty();
   }
}