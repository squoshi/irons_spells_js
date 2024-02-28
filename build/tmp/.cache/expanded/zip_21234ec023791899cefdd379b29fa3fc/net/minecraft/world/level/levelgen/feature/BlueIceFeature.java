package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;

public class BlueIceFeature extends Feature<NoneFeatureConfiguration> {
   public BlueIceFeature(Codec<NoneFeatureConfiguration> p_65285_) {
      super(p_65285_);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> p_159475_) {
      BlockPos blockpos = p_159475_.origin();
      WorldGenLevel worldgenlevel = p_159475_.level();
      RandomSource randomsource = p_159475_.random();
      if (blockpos.getY() > worldgenlevel.getSeaLevel() - 1) {
         return false;
      } else if (!worldgenlevel.getBlockState(blockpos).is(Blocks.WATER) && !worldgenlevel.getBlockState(blockpos.below()).is(Blocks.WATER)) {
         return false;
      } else {
         boolean flag = false;

         for(Direction direction : Direction.values()) {
            if (direction != Direction.DOWN && worldgenlevel.getBlockState(blockpos.relative(direction)).is(Blocks.PACKED_ICE)) {
               flag = true;
               break;
            }
         }

         if (!flag) {
            return false;
         } else {
            worldgenlevel.setBlock(blockpos, Blocks.BLUE_ICE.defaultBlockState(), 2);

            for(int i = 0; i < 200; ++i) {
               int j = randomsource.nextInt(5) - randomsource.nextInt(6);
               int k = 3;
               if (j < 2) {
                  k += j / 2;
               }

               if (k >= 1) {
                  BlockPos blockpos1 = blockpos.offset(randomsource.nextInt(k) - randomsource.nextInt(k), j, randomsource.nextInt(k) - randomsource.nextInt(k));
                  BlockState blockstate = worldgenlevel.getBlockState(blockpos1);
                  if (blockstate.getMaterial() == Material.AIR || blockstate.is(Blocks.WATER) || blockstate.is(Blocks.PACKED_ICE) || blockstate.is(Blocks.ICE)) {
                     for(Direction direction1 : Direction.values()) {
                        BlockState blockstate1 = worldgenlevel.getBlockState(blockpos1.relative(direction1));
                        if (blockstate1.is(Blocks.BLUE_ICE)) {
                           worldgenlevel.setBlock(blockpos1, Blocks.BLUE_ICE.defaultBlockState(), 2);
                           break;
                        }
                     }
                  }
               }
            }

            return true;
         }
      }
   }
}