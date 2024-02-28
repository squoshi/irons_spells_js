package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class MangroveRootPlacer extends RootPlacer {
   public static final int ROOT_WIDTH_LIMIT = 8;
   public static final int ROOT_LENGTH_LIMIT = 15;
   public static final Codec<MangroveRootPlacer> CODEC = RecordCodecBuilder.create((p_225856_) -> {
      return rootPlacerParts(p_225856_).and(MangroveRootPlacement.CODEC.fieldOf("mangrove_root_placement").forGetter((p_225849_) -> {
         return p_225849_.mangroveRootPlacement;
      })).apply(p_225856_, MangroveRootPlacer::new);
   });
   private final MangroveRootPlacement mangroveRootPlacement;

   public MangroveRootPlacer(IntProvider p_225817_, BlockStateProvider p_225818_, Optional<AboveRootPlacement> p_225819_, MangroveRootPlacement p_225820_) {
      super(p_225817_, p_225818_, p_225819_);
      this.mangroveRootPlacement = p_225820_;
   }

   public boolean placeRoots(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> p_225841_, RandomSource pRandom, BlockPos p_225843_, BlockPos p_225844_, TreeConfiguration pTreeConfig) {
      List<BlockPos> list = Lists.newArrayList();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = p_225843_.mutable();

      while(blockpos$mutableblockpos.getY() < p_225844_.getY()) {
         if (!this.canPlaceRoot(pLevel, blockpos$mutableblockpos)) {
            return false;
         }

         blockpos$mutableblockpos.move(Direction.UP);
      }

      list.add(p_225844_.below());

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = p_225844_.relative(direction);
         List<BlockPos> list1 = Lists.newArrayList();
         if (!this.simulateRoots(pLevel, pRandom, blockpos, direction, p_225844_, list1, 0)) {
            return false;
         }

         list.addAll(list1);
         list.add(p_225844_.relative(direction));
      }

      for(BlockPos blockpos1 : list) {
         this.placeRoot(pLevel, p_225841_, pRandom, blockpos1, pTreeConfig);
      }

      return true;
   }

   private boolean simulateRoots(LevelSimulatedReader pLevel, RandomSource pRandom, BlockPos p_225825_, Direction pDirection, BlockPos p_225827_, List<BlockPos> p_225828_, int pLength) {
      int i = this.mangroveRootPlacement.maxRootLength();
      if (pLength != i && p_225828_.size() <= i) {
         for(BlockPos blockpos : this.potentialRootPositions(p_225825_, pDirection, pRandom, p_225827_)) {
            if (this.canPlaceRoot(pLevel, blockpos)) {
               p_225828_.add(blockpos);
               if (!this.simulateRoots(pLevel, pRandom, blockpos, pDirection, p_225827_, p_225828_, pLength + 1)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected List<BlockPos> potentialRootPositions(BlockPos p_225851_, Direction pDirection, RandomSource pRandom, BlockPos p_225854_) {
      BlockPos blockpos = p_225851_.below();
      BlockPos blockpos1 = p_225851_.relative(pDirection);
      int i = p_225851_.distManhattan(p_225854_);
      int j = this.mangroveRootPlacement.maxRootWidth();
      float f = this.mangroveRootPlacement.randomSkewChance();
      if (i > j - 3 && i <= j) {
         return pRandom.nextFloat() < f ? List.of(blockpos, blockpos1.below()) : List.of(blockpos);
      } else if (i > j) {
         return List.of(blockpos);
      } else if (pRandom.nextFloat() < f) {
         return List.of(blockpos);
      } else {
         return pRandom.nextBoolean() ? List.of(blockpos1) : List.of(blockpos);
      }
   }

   protected boolean canPlaceRoot(LevelSimulatedReader pLevel, BlockPos pPos) {
      return super.canPlaceRoot(pLevel, pPos) || pLevel.isStateAtPosition(pPos, (p_225858_) -> {
         return p_225858_.is(this.mangroveRootPlacement.canGrowThrough());
      });
   }

   protected void placeRoot(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> p_225835_, RandomSource pRandom, BlockPos pPos, TreeConfiguration pTreeConfig) {
      if (pLevel.isStateAtPosition(pPos, (p_225847_) -> {
         return p_225847_.is(this.mangroveRootPlacement.muddyRootsIn());
      })) {
         BlockState blockstate = this.mangroveRootPlacement.muddyRootsProvider().getState(pRandom, pPos);
         p_225835_.accept(pPos, this.getPotentiallyWaterloggedState(pLevel, pPos, blockstate));
      } else {
         super.placeRoot(pLevel, p_225835_, pRandom, pPos, pTreeConfig);
      }

   }

   protected RootPlacerType<?> type() {
      return RootPlacerType.MANGROVE_ROOT_PLACER;
   }
}