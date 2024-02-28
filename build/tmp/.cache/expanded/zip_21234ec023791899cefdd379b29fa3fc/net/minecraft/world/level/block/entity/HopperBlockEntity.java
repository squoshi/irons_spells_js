package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public class HopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper {
   public static final int MOVE_ITEM_SPEED = 8;
   public static final int HOPPER_CONTAINER_SIZE = 5;
   private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
   private int cooldownTime = -1;
   private long tickedGameTime;

   public HopperBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.HOPPER, pPos, pBlockState);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(pTag)) {
         ContainerHelper.loadAllItems(pTag, this.items);
      }

      this.cooldownTime = pTag.getInt("TransferCooldown");
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (!this.trySaveLootTable(pTag)) {
         ContainerHelper.saveAllItems(pTag, this.items);
      }

      pTag.putInt("TransferCooldown", this.cooldownTime);
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.items.size();
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      this.unpackLootTable((Player)null);
      return ContainerHelper.removeItem(this.getItems(), pIndex, pCount);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      this.unpackLootTable((Player)null);
      this.getItems().set(pIndex, pStack);
      if (pStack.getCount() > this.getMaxStackSize()) {
         pStack.setCount(this.getMaxStackSize());
      }

   }

   protected Component getDefaultName() {
      return Component.translatable("container.hopper");
   }

   public static void pushItemsTick(Level pLevel, BlockPos pPos, BlockState pState, HopperBlockEntity pBlockEntity) {
      --pBlockEntity.cooldownTime;
      pBlockEntity.tickedGameTime = pLevel.getGameTime();
      if (!pBlockEntity.isOnCooldown()) {
         pBlockEntity.setCooldown(0);
         tryMoveItems(pLevel, pPos, pState, pBlockEntity, () -> {
            return suckInItems(pLevel, pBlockEntity);
         });
      }

   }

   private static boolean tryMoveItems(Level pLevel, BlockPos pPos, BlockState pState, HopperBlockEntity pBlockEntity, BooleanSupplier pValidator) {
      if (pLevel.isClientSide) {
         return false;
      } else {
         if (!pBlockEntity.isOnCooldown() && pState.getValue(HopperBlock.ENABLED)) {
            boolean flag = false;
            if (!pBlockEntity.isEmpty()) {
               flag = ejectItems(pLevel, pPos, pState, pBlockEntity);
            }

            if (!pBlockEntity.inventoryFull()) {
               flag |= pValidator.getAsBoolean();
            }

            if (flag) {
               pBlockEntity.setCooldown(8);
               setChanged(pLevel, pPos, pState);
               return true;
            }
         }

         return false;
      }
   }

   private boolean inventoryFull() {
      for(ItemStack itemstack : this.items) {
         if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   private static boolean ejectItems(Level pLevel, BlockPos pPos, BlockState pState, HopperBlockEntity pSourceContainer) {
      if (net.minecraftforge.items.VanillaInventoryCodeHooks.insertHook(pSourceContainer)) return true;
      Container container = getAttachedContainer(pLevel, pPos, pState);
      if (container == null) {
         return false;
      } else {
         Direction direction = pState.getValue(HopperBlock.FACING).getOpposite();
         if (isFullContainer(container, direction)) {
            return false;
         } else {
            for(int i = 0; i < pSourceContainer.getContainerSize(); ++i) {
               if (!pSourceContainer.getItem(i).isEmpty()) {
                  ItemStack itemstack = pSourceContainer.getItem(i).copy();
                  ItemStack itemstack1 = addItem(pSourceContainer, container, pSourceContainer.removeItem(i, 1), direction);
                  if (itemstack1.isEmpty()) {
                     container.setChanged();
                     return true;
                  }

                  pSourceContainer.setItem(i, itemstack);
               }
            }

            return false;
         }
      }
   }

   private static IntStream getSlots(Container pContainer, Direction pDirection) {
      return pContainer instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)pContainer).getSlotsForFace(pDirection)) : IntStream.range(0, pContainer.getContainerSize());
   }

   /**
    * @return {@code false} if the {@code container} has any room to place items in
    */
   private static boolean isFullContainer(Container pContainer, Direction pDirection) {
      return getSlots(pContainer, pDirection).allMatch((p_59379_) -> {
         ItemStack itemstack = pContainer.getItem(p_59379_);
         return itemstack.getCount() >= itemstack.getMaxStackSize();
      });
   }

   /**
    * @return whether the given {@code container} is empty from the given face
    */
   private static boolean isEmptyContainer(Container pContainer, Direction pDirection) {
      return getSlots(pContainer, pDirection).allMatch((p_59319_) -> {
         return pContainer.getItem(p_59319_).isEmpty();
      });
   }

   public static boolean suckInItems(Level pLevel, Hopper pHopper) {
      Boolean ret = net.minecraftforge.items.VanillaInventoryCodeHooks.extractHook(pLevel, pHopper);
      if (ret != null) return ret;
      Container container = getSourceContainer(pLevel, pHopper);
      if (container != null) {
         Direction direction = Direction.DOWN;
         return isEmptyContainer(container, direction) ? false : getSlots(container, direction).anyMatch((p_59363_) -> {
            return tryTakeInItemFromSlot(pHopper, container, p_59363_, direction);
         });
      } else {
         for(ItemEntity itementity : getItemsAtAndAbove(pLevel, pHopper)) {
            if (addItem(pHopper, itementity)) {
               return true;
            }
         }

         return false;
      }
   }

   /**
    * Pulls from the specified slot in the container and places in any available slot in the hopper.
    * @return {@code true} if the entire stack was moved.
    */
   private static boolean tryTakeInItemFromSlot(Hopper pHopper, Container pContainer, int pSlot, Direction pDirection) {
      ItemStack itemstack = pContainer.getItem(pSlot);
      if (!itemstack.isEmpty() && canTakeItemFromContainer(pContainer, itemstack, pSlot, pDirection)) {
         ItemStack itemstack1 = itemstack.copy();
         ItemStack itemstack2 = addItem(pContainer, pHopper, pContainer.removeItem(pSlot, 1), (Direction)null);
         if (itemstack2.isEmpty()) {
            pContainer.setChanged();
            return true;
         }

         pContainer.setItem(pSlot, itemstack1);
      }

      return false;
   }

   public static boolean addItem(Container pContainer, ItemEntity pItem) {
      boolean flag = false;
      ItemStack itemstack = pItem.getItem().copy();
      ItemStack itemstack1 = addItem((Container)null, pContainer, itemstack, (Direction)null);
      if (itemstack1.isEmpty()) {
         flag = true;
         pItem.discard();
      } else {
         pItem.setItem(itemstack1);
      }

      return flag;
   }

   /**
    * Attempts to place the passed stack in the container, using as many slots as required.
    * @return any leftover stack
    */
   public static ItemStack addItem(@Nullable Container pSource, Container pDestination, ItemStack pStack, @Nullable Direction pDirection) {
      if (pDestination instanceof WorldlyContainer worldlycontainer && pDirection != null) {
         int[] aint = worldlycontainer.getSlotsForFace(pDirection);

         for(int k = 0; k < aint.length && !pStack.isEmpty(); ++k) {
            pStack = tryMoveInItem(pSource, pDestination, pStack, aint[k], pDirection);
         }
      } else {
         int i = pDestination.getContainerSize();

         for(int j = 0; j < i && !pStack.isEmpty(); ++j) {
            pStack = tryMoveInItem(pSource, pDestination, pStack, j, pDirection);
         }
      }

      return pStack;
   }

   private static boolean canPlaceItemInContainer(Container pContainer, ItemStack pStack, int pSlot, @Nullable Direction pDirection) {
      if (!pContainer.canPlaceItem(pSlot, pStack)) {
         return false;
      } else {
         return !(pContainer instanceof WorldlyContainer) || ((WorldlyContainer)pContainer).canPlaceItemThroughFace(pSlot, pStack, pDirection);
      }
   }

   private static boolean canTakeItemFromContainer(Container pContainer, ItemStack pStack, int pSlot, Direction pDirection) {
      return !(pContainer instanceof WorldlyContainer) || ((WorldlyContainer)pContainer).canTakeItemThroughFace(pSlot, pStack, pDirection);
   }

   private static ItemStack tryMoveInItem(@Nullable Container pSource, Container pDestination, ItemStack pStack, int pSlot, @Nullable Direction pDirection) {
      ItemStack itemstack = pDestination.getItem(pSlot);
      if (canPlaceItemInContainer(pDestination, pStack, pSlot, pDirection)) {
         boolean flag = false;
         boolean flag1 = pDestination.isEmpty();
         if (itemstack.isEmpty()) {
            pDestination.setItem(pSlot, pStack);
            pStack = ItemStack.EMPTY;
            flag = true;
         } else if (canMergeItems(itemstack, pStack)) {
            int i = pStack.getMaxStackSize() - itemstack.getCount();
            int j = Math.min(pStack.getCount(), i);
            pStack.shrink(j);
            itemstack.grow(j);
            flag = j > 0;
         }

         if (flag) {
            if (flag1 && pDestination instanceof HopperBlockEntity) {
               HopperBlockEntity hopperblockentity1 = (HopperBlockEntity)pDestination;
               if (!hopperblockentity1.isOnCustomCooldown()) {
                  int k = 0;
                  if (pSource instanceof HopperBlockEntity) {
                     HopperBlockEntity hopperblockentity = (HopperBlockEntity)pSource;
                     if (hopperblockentity1.tickedGameTime >= hopperblockentity.tickedGameTime) {
                        k = 1;
                     }
                  }

                  hopperblockentity1.setCooldown(8 - k);
               }
            }

            pDestination.setChanged();
         }
      }

      return pStack;
   }

   @Nullable
   private static Container getAttachedContainer(Level pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = pState.getValue(HopperBlock.FACING);
      return getContainerAt(pLevel, pPos.relative(direction));
   }

   @Nullable
   private static Container getSourceContainer(Level pLevel, Hopper pHopper) {
      return getContainerAt(pLevel, pHopper.getLevelX(), pHopper.getLevelY() + 1.0D, pHopper.getLevelZ());
   }

   public static List<ItemEntity> getItemsAtAndAbove(Level pLevel, Hopper pHopper) {
      return pHopper.getSuckShape().toAabbs().stream().flatMap((p_155558_) -> {
         return pLevel.getEntitiesOfClass(ItemEntity.class, p_155558_.move(pHopper.getLevelX() - 0.5D, pHopper.getLevelY() - 0.5D, pHopper.getLevelZ() - 0.5D), EntitySelector.ENTITY_STILL_ALIVE).stream();
      }).collect(Collectors.toList());
   }

   @Nullable
   public static Container getContainerAt(Level pLevel, BlockPos pPos) {
      return getContainerAt(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D);
   }

   /**
    * @return the container for the given position or {@code null} if none was found
    */
   @Nullable
   private static Container getContainerAt(Level pLevel, double pX, double pY, double pZ) {
      Container container = null;
      BlockPos blockpos = new BlockPos(pX, pY, pZ);
      BlockState blockstate = pLevel.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      if (block instanceof WorldlyContainerHolder) {
         container = ((WorldlyContainerHolder)block).getContainer(blockstate, pLevel, blockpos);
      } else if (blockstate.hasBlockEntity()) {
         BlockEntity blockentity = pLevel.getBlockEntity(blockpos);
         if (blockentity instanceof Container) {
            container = (Container)blockentity;
            if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
               container = ChestBlock.getContainer((ChestBlock)block, blockstate, pLevel, blockpos, true);
            }
         }
      }

      if (container == null) {
         List<Entity> list = pLevel.getEntities((Entity)null, new AABB(pX - 0.5D, pY - 0.5D, pZ - 0.5D, pX + 0.5D, pY + 0.5D, pZ + 0.5D), EntitySelector.CONTAINER_ENTITY_SELECTOR);
         if (!list.isEmpty()) {
            container = (Container)list.get(pLevel.random.nextInt(list.size()));
         }
      }

      return container;
   }

   private static boolean canMergeItems(ItemStack pStack1, ItemStack pStack2) {
      if (!pStack1.is(pStack2.getItem())) {
         return false;
      } else if (pStack1.getDamageValue() != pStack2.getDamageValue()) {
         return false;
      } else if (pStack1.getCount() > pStack1.getMaxStackSize()) {
         return false;
      } else {
         return ItemStack.tagMatches(pStack1, pStack2);
      }
   }

   /**
    * Gets the world X position for this hopper entity.
    */
   public double getLevelX() {
      return (double)this.worldPosition.getX() + 0.5D;
   }

   /**
    * Gets the world Y position for this hopper entity.
    */
   public double getLevelY() {
      return (double)this.worldPosition.getY() + 0.5D;
   }

   /**
    * Gets the world Z position for this hopper entity.
    */
   public double getLevelZ() {
      return (double)this.worldPosition.getZ() + 0.5D;
   }

   public void setCooldown(int pCooldownTime) {
      this.cooldownTime = pCooldownTime;
   }

   private boolean isOnCooldown() {
      return this.cooldownTime > 0;
   }

   public boolean isOnCustomCooldown() {
      return this.cooldownTime > 8;
   }

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> pItems) {
      this.items = pItems;
   }

   public static void entityInside(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity, HopperBlockEntity pBlockEntity) {
      if (pEntity instanceof ItemEntity && Shapes.joinIsNotEmpty(Shapes.create(pEntity.getBoundingBox().move((double)(-pPos.getX()), (double)(-pPos.getY()), (double)(-pPos.getZ()))), pBlockEntity.getSuckShape(), BooleanOp.AND)) {
         tryMoveItems(pLevel, pPos, pState, pBlockEntity, () -> {
            return addItem(pBlockEntity, (ItemEntity)pEntity);
         });
      }

   }

   protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer) {
      return new HopperMenu(pId, pPlayer, this);
   }

   @Override
   protected net.minecraftforge.items.IItemHandler createUnSidedHandler() {
      return new net.minecraftforge.items.VanillaHopperItemHandler(this);
   }

   public long getLastUpdateTime() {
      return this.tickedGameTime;
   }
}
