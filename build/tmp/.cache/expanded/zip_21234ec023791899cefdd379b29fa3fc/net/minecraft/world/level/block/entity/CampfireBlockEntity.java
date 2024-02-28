package net.minecraft.world.level.block.entity;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class CampfireBlockEntity extends BlockEntity implements Clearable {
   private static final int BURN_COOL_SPEED = 2;
   private static final int NUM_SLOTS = 4;
   private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
   private final int[] cookingProgress = new int[4];
   private final int[] cookingTime = new int[4];
   private final RecipeManager.CachedCheck<Container, CampfireCookingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);

   public CampfireBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.CAMPFIRE, pPos, pBlockState);
   }

   public static void cookTick(Level pLevel, BlockPos pPos, BlockState pState, CampfireBlockEntity pBlockEntity) {
      boolean flag = false;

      for(int i = 0; i < pBlockEntity.items.size(); ++i) {
         ItemStack itemstack = pBlockEntity.items.get(i);
         if (!itemstack.isEmpty()) {
            flag = true;
            int j = pBlockEntity.cookingProgress[i]++;
            if (pBlockEntity.cookingProgress[i] >= pBlockEntity.cookingTime[i]) {
               Container container = new SimpleContainer(itemstack);
               ItemStack itemstack1 = pBlockEntity.quickCheck.getRecipeFor(container, pLevel).map((p_155305_) -> {
                  return p_155305_.assemble(container);
               }).orElse(itemstack);
               Containers.dropItemStack(pLevel, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), itemstack1);
               pBlockEntity.items.set(i, ItemStack.EMPTY);
               pLevel.sendBlockUpdated(pPos, pState, pState, 3);
               pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pState));
            }
         }
      }

      if (flag) {
         setChanged(pLevel, pPos, pState);
      }

   }

   public static void cooldownTick(Level pLevel, BlockPos pPos, BlockState pState, CampfireBlockEntity pBlockEntity) {
      boolean flag = false;

      for(int i = 0; i < pBlockEntity.items.size(); ++i) {
         if (pBlockEntity.cookingProgress[i] > 0) {
            flag = true;
            pBlockEntity.cookingProgress[i] = Mth.clamp(pBlockEntity.cookingProgress[i] - 2, 0, pBlockEntity.cookingTime[i]);
         }
      }

      if (flag) {
         setChanged(pLevel, pPos, pState);
      }

   }

   public static void particleTick(Level pLevel, BlockPos pPos, BlockState pState, CampfireBlockEntity pBlockEntity) {
      RandomSource randomsource = pLevel.random;
      if (randomsource.nextFloat() < 0.11F) {
         for(int i = 0; i < randomsource.nextInt(2) + 2; ++i) {
            CampfireBlock.makeParticles(pLevel, pPos, pState.getValue(CampfireBlock.SIGNAL_FIRE), false);
         }
      }

      int l = pState.getValue(CampfireBlock.FACING).get2DDataValue();

      for(int j = 0; j < pBlockEntity.items.size(); ++j) {
         if (!pBlockEntity.items.get(j).isEmpty() && randomsource.nextFloat() < 0.2F) {
            Direction direction = Direction.from2DDataValue(Math.floorMod(j + l, 4));
            float f = 0.3125F;
            double d0 = (double)pPos.getX() + 0.5D - (double)((float)direction.getStepX() * 0.3125F) + (double)((float)direction.getClockWise().getStepX() * 0.3125F);
            double d1 = (double)pPos.getY() + 0.5D;
            double d2 = (double)pPos.getZ() + 0.5D - (double)((float)direction.getStepZ() * 0.3125F) + (double)((float)direction.getClockWise().getStepZ() * 0.3125F);

            for(int k = 0; k < 4; ++k) {
               pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 5.0E-4D, 0.0D);
            }
         }
      }

   }

   /**
    * @return the items currently held in this campfire
    */
   public NonNullList<ItemStack> getItems() {
      return this.items;
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.items.clear();
      ContainerHelper.loadAllItems(pTag, this.items);
      if (pTag.contains("CookingTimes", 11)) {
         int[] aint = pTag.getIntArray("CookingTimes");
         System.arraycopy(aint, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, aint.length));
      }

      if (pTag.contains("CookingTotalTimes", 11)) {
         int[] aint1 = pTag.getIntArray("CookingTotalTimes");
         System.arraycopy(aint1, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, aint1.length));
      }

   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      ContainerHelper.saveAllItems(pTag, this.items, true);
      pTag.putIntArray("CookingTimes", this.cookingProgress);
      pTag.putIntArray("CookingTotalTimes", this.cookingTime);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundTag getUpdateTag() {
      CompoundTag compoundtag = new CompoundTag();
      ContainerHelper.saveAllItems(compoundtag, this.items, true);
      return compoundtag;
   }

   public Optional<CampfireCookingRecipe> getCookableRecipe(ItemStack pStack) {
      return this.items.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.quickCheck.getRecipeFor(new SimpleContainer(pStack), this.level);
   }

   public boolean placeFood(@Nullable Entity pEntity, ItemStack pStack, int pCookTime) {
      for(int i = 0; i < this.items.size(); ++i) {
         ItemStack itemstack = this.items.get(i);
         if (itemstack.isEmpty()) {
            this.cookingTime[i] = pCookTime;
            this.cookingProgress[i] = 0;
            this.items.set(i, pStack.split(1));
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(pEntity, this.getBlockState()));
            this.markUpdated();
            return true;
         }
      }

      return false;
   }

   private void markUpdated() {
      this.setChanged();
      this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
   }

   public void clearContent() {
      this.items.clear();
   }

   public void dowse() {
      if (this.level != null) {
         this.markUpdated();
      }

   }
}