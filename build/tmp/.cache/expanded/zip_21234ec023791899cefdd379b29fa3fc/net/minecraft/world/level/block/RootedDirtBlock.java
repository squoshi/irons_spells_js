package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class RootedDirtBlock extends Block implements BonemealableBlock {
   public RootedDirtBlock(BlockBehaviour.Properties p_154359_) {
      super(p_154359_);
   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(BlockGetter p_154366_, BlockPos p_154367_, BlockState p_154368_, boolean p_154369_) {
      return p_154366_.getBlockState(p_154367_.below()).isAir();
   }

   public boolean isBonemealSuccess(Level p_221979_, RandomSource p_221980_, BlockPos p_221981_, BlockState p_221982_) {
      return true;
   }

   public void performBonemeal(ServerLevel p_221974_, RandomSource p_221975_, BlockPos p_221976_, BlockState p_221977_) {
      p_221974_.setBlockAndUpdate(p_221976_.below(), Blocks.HANGING_ROOTS.defaultBlockState());
   }
}