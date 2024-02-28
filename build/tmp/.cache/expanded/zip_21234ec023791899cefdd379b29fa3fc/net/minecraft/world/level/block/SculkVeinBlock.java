package net.minecraft.world.level.block;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;

public class SculkVeinBlock extends MultifaceBlock implements SculkBehaviour, SimpleWaterloggedBlock {
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private final MultifaceSpreader veinSpreader = new MultifaceSpreader(new SculkVeinBlock.SculkVeinSpreaderConfig(MultifaceSpreader.DEFAULT_SPREAD_ORDER));
   private final MultifaceSpreader sameSpaceSpreader = new MultifaceSpreader(new SculkVeinBlock.SculkVeinSpreaderConfig(MultifaceSpreader.SpreadType.SAME_POSITION));

   public SculkVeinBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public MultifaceSpreader getSpreader() {
      return this.veinSpreader;
   }

   public MultifaceSpreader getSameSpaceSpreader() {
      return this.sameSpaceSpreader;
   }

   public static boolean regrow(LevelAccessor pLevel, BlockPos pPos, BlockState pState, Collection<Direction> pDirections) {
      boolean flag = false;
      BlockState blockstate = Blocks.SCULK_VEIN.defaultBlockState();

      for(Direction direction : pDirections) {
         BlockPos blockpos = pPos.relative(direction);
         if (canAttachTo(pLevel, direction, blockpos, pLevel.getBlockState(blockpos))) {
            blockstate = blockstate.setValue(getFaceProperty(direction), Boolean.valueOf(true));
            flag = true;
         }
      }

      if (!flag) {
         return false;
      } else {
         if (!pState.getFluidState().isEmpty()) {
            blockstate = blockstate.setValue(WATERLOGGED, Boolean.valueOf(true));
         }

         pLevel.setBlock(pPos, blockstate, 3);
         return true;
      }
   }

   public void onDischarged(LevelAccessor pLevel, BlockState pState, BlockPos pPos, RandomSource pRandom) {
      if (pState.is(this)) {
         for(Direction direction : DIRECTIONS) {
            BooleanProperty booleanproperty = getFaceProperty(direction);
            if (pState.getValue(booleanproperty) && pLevel.getBlockState(pPos.relative(direction)).is(Blocks.SCULK)) {
               pState = pState.setValue(booleanproperty, Boolean.valueOf(false));
            }
         }

         if (!hasAnyFace(pState)) {
            FluidState fluidstate = pLevel.getFluidState(pPos);
            pState = (fluidstate.isEmpty() ? Blocks.AIR : Blocks.WATER).defaultBlockState();
         }

         pLevel.setBlock(pPos, pState, 3);
         SculkBehaviour.super.onDischarged(pLevel, pState, pPos, pRandom);
      }
   }

   public int attemptUseCharge(SculkSpreader.ChargeCursor pCursor, LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom, SculkSpreader pSpreader, boolean p_222374_) {
      if (p_222374_ && this.attemptPlaceSculk(pSpreader, pLevel, pCursor.getPos(), pRandom)) {
         return pCursor.getCharge() - 1;
      } else {
         return pRandom.nextInt(pSpreader.chargeDecayRate()) == 0 ? Mth.floor((float)pCursor.getCharge() * 0.5F) : pCursor.getCharge();
      }
   }

   private boolean attemptPlaceSculk(SculkSpreader pSpreader, LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      TagKey<Block> tagkey = pSpreader.replaceableBlocks();

      for(Direction direction : Direction.allShuffled(pRandom)) {
         if (hasFace(blockstate, direction)) {
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate1 = pLevel.getBlockState(blockpos);
            if (blockstate1.is(tagkey)) {
               BlockState blockstate2 = Blocks.SCULK.defaultBlockState();
               pLevel.setBlock(blockpos, blockstate2, 3);
               Block.pushEntitiesUp(blockstate1, blockstate2, pLevel, blockpos);
               pLevel.playSound((Player)null, blockpos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
               this.veinSpreader.spreadAll(blockstate2, pLevel, blockpos, pSpreader.isWorldGeneration());
               Direction direction1 = direction.getOpposite();

               for(Direction direction2 : DIRECTIONS) {
                  if (direction2 != direction1) {
                     BlockPos blockpos1 = blockpos.relative(direction2);
                     BlockState blockstate3 = pLevel.getBlockState(blockpos1);
                     if (blockstate3.is(this)) {
                        this.onDischarged(pLevel, blockstate3, blockpos1, pRandom);
                     }
                  }
               }

               return true;
            }
         }
      }

      return false;
   }

   public static boolean hasSubstrateAccess(LevelAccessor pLevel, BlockState pState, BlockPos pPos) {
      if (!pState.is(Blocks.SCULK_VEIN)) {
         return false;
      } else {
         for(Direction direction : DIRECTIONS) {
            if (hasFace(pState, direction) && pLevel.getBlockState(pPos.relative(direction)).is(BlockTags.SCULK_REPLACEABLE)) {
               return true;
            }
         }

         return false;
      }
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(WATERLOGGED);
   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      return !pUseContext.getItemInHand().is(Items.SCULK_VEIN) || super.canBeReplaced(pState, pUseContext);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getPistonPushReaction} whenever possible.
    * Implementing/overriding is fine.
    */
   public PushReaction getPistonPushReaction(BlockState pState) {
      return PushReaction.DESTROY;
   }

   class SculkVeinSpreaderConfig extends MultifaceSpreader.DefaultSpreaderConfig {
      private final MultifaceSpreader.SpreadType[] spreadTypes;

      public SculkVeinSpreaderConfig(MultifaceSpreader.SpreadType... pSpreadTypes) {
         super(SculkVeinBlock.this);
         this.spreadTypes = pSpreadTypes;
      }

      public boolean stateCanBeReplaced(BlockGetter pLevel, BlockPos p_222406_, BlockPos p_222407_, Direction p_222408_, BlockState p_222409_) {
         BlockState blockstate = pLevel.getBlockState(p_222407_.relative(p_222408_));
         if (!blockstate.is(Blocks.SCULK) && !blockstate.is(Blocks.SCULK_CATALYST) && !blockstate.is(Blocks.MOVING_PISTON)) {
            if (p_222406_.distManhattan(p_222407_) == 2) {
               BlockPos blockpos = p_222406_.relative(p_222408_.getOpposite());
               if (pLevel.getBlockState(blockpos).isFaceSturdy(pLevel, blockpos, p_222408_)) {
                  return false;
               }
            }

            FluidState fluidstate = p_222409_.getFluidState();
            if (!fluidstate.isEmpty() && !fluidstate.is(Fluids.WATER)) {
               return false;
            } else {
               Material material = p_222409_.getMaterial();
               if (material == Material.FIRE) {
                  return false;
               } else {
                  return material.isReplaceable() || super.stateCanBeReplaced(pLevel, p_222406_, p_222407_, p_222408_, p_222409_);
               }
            }
         } else {
            return false;
         }
      }

      public MultifaceSpreader.SpreadType[] getSpreadTypes() {
         return this.spreadTypes;
      }

      public boolean isOtherBlockValidAsSource(BlockState pOtherBlock) {
         return !pOtherBlock.is(Blocks.SCULK_VEIN);
      }
   }
}