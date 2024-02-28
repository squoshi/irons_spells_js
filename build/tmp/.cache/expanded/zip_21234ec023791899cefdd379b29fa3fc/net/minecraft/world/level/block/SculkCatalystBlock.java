package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;

public class SculkCatalystBlock extends BaseEntityBlock {
   public static final int PULSE_TICKS = 8;
   public static final BooleanProperty PULSE = BlockStateProperties.BLOOM;
   private final IntProvider xpRange = ConstantInt.of(5);

   public SculkCatalystBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(PULSE, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(PULSE);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(PULSE)) {
         pLevel.setBlock(pPos, pState.setValue(PULSE, Boolean.valueOf(false)), 3);
      }

   }

   public static void bloom(ServerLevel pLevel, BlockPos pPos, BlockState pState, RandomSource pSource) {
      pLevel.setBlock(pPos, pState.setValue(PULSE, Boolean.valueOf(true)), 3);
      pLevel.scheduleTick(pPos, pState.getBlock(), 8);
      pLevel.sendParticles(ParticleTypes.SCULK_SOUL, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 1.15D, (double)pPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
      pLevel.playSound((Player)null, pPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + pSource.nextFloat() * 0.4F);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new SculkCatalystBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> GameEventListener getListener(ServerLevel pLevel, T pBlockEntity) {
      return pBlockEntity instanceof SculkCatalystBlockEntity ? (SculkCatalystBlockEntity)pBlockEntity : null;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, BlockEntityType.SCULK_CATALYST, SculkCatalystBlockEntity::serverTick);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   /**
    * Perform side-effects from block dropping, such as creating silverfish
    */
   public void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack, boolean pDropExperience) {
      super.spawnAfterBreak(pState, pLevel, pPos, pStack, pDropExperience);

   }

   @Override
   public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
      return silkTouchLevel == 0 ? this.xpRange.sample(randomSource) : 0;
   }
}
