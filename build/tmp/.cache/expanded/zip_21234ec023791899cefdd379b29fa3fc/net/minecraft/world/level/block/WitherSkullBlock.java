package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.material.Material;

public class WitherSkullBlock extends SkullBlock {
   @Nullable
   private static BlockPattern witherPatternFull;
   @Nullable
   private static BlockPattern witherPatternBase;

   public WitherSkullBlock(BlockBehaviour.Properties p_58254_) {
      super(SkullBlock.Types.WITHER_SKELETON, p_58254_);
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof SkullBlockEntity) {
         checkSpawn(pLevel, pPos, (SkullBlockEntity)blockentity);
      }

   }

   public static void checkSpawn(Level pLevel, BlockPos pPos, SkullBlockEntity pBlockEntity) {
      if (!pLevel.isClientSide) {
         BlockState blockstate = pBlockEntity.getBlockState();
         boolean flag = blockstate.is(Blocks.WITHER_SKELETON_SKULL) || blockstate.is(Blocks.WITHER_SKELETON_WALL_SKULL);
         if (flag && pPos.getY() >= pLevel.getMinBuildHeight() && pLevel.getDifficulty() != Difficulty.PEACEFUL) {
            BlockPattern blockpattern = getOrCreateWitherFull();
            BlockPattern.BlockPatternMatch blockpattern$blockpatternmatch = blockpattern.find(pLevel, pPos);
            if (blockpattern$blockpatternmatch != null) {
               for(int i = 0; i < blockpattern.getWidth(); ++i) {
                  for(int j = 0; j < blockpattern.getHeight(); ++j) {
                     BlockInWorld blockinworld = blockpattern$blockpatternmatch.getBlock(i, j, 0);
                     pLevel.setBlock(blockinworld.getPos(), Blocks.AIR.defaultBlockState(), 2);
                     pLevel.levelEvent(2001, blockinworld.getPos(), Block.getId(blockinworld.getState()));
                  }
               }

               WitherBoss witherboss = EntityType.WITHER.create(pLevel);
               BlockPos blockpos = blockpattern$blockpatternmatch.getBlock(1, 2, 0).getPos();
               witherboss.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.55D, (double)blockpos.getZ() + 0.5D, blockpattern$blockpatternmatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
               witherboss.yBodyRot = blockpattern$blockpatternmatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
               witherboss.makeInvulnerable();

               for(ServerPlayer serverplayer : pLevel.getEntitiesOfClass(ServerPlayer.class, witherboss.getBoundingBox().inflate(50.0D))) {
                  CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, witherboss);
               }

               pLevel.addFreshEntity(witherboss);

               for(int k = 0; k < blockpattern.getWidth(); ++k) {
                  for(int l = 0; l < blockpattern.getHeight(); ++l) {
                     pLevel.blockUpdated(blockpattern$blockpatternmatch.getBlock(k, l, 0).getPos(), Blocks.AIR);
                  }
               }

            }
         }
      }
   }

   public static boolean canSpawnMob(Level pLevel, BlockPos pPos, ItemStack pStack) {
      if (pStack.is(Items.WITHER_SKELETON_SKULL) && pPos.getY() >= pLevel.getMinBuildHeight() + 2 && pLevel.getDifficulty() != Difficulty.PEACEFUL && !pLevel.isClientSide) {
         return getOrCreateWitherBase().find(pLevel, pPos) != null;
      } else {
         return false;
      }
   }

   private static BlockPattern getOrCreateWitherFull() {
      if (witherPatternFull == null) {
         witherPatternFull = BlockPatternBuilder.start().aisle("^^^", "###", "~#~").where('#', (p_58272_) -> {
            return p_58272_.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
         }).where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
      }

      return witherPatternFull;
   }

   private static BlockPattern getOrCreateWitherBase() {
      if (witherPatternBase == null) {
         witherPatternBase = BlockPatternBuilder.start().aisle("   ", "###", "~#~").where('#', (p_58266_) -> {
            return p_58266_.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
         }).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
      }

      return witherPatternBase;
   }
}