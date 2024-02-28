package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class JukeboxBlockEntity extends BlockEntity implements Clearable {
   private ItemStack record = ItemStack.EMPTY;
   private int ticksSinceLastEvent;
   private long tickCount;
   private long recordStartedTick;
   private boolean isPlaying;

   public JukeboxBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.JUKEBOX, pPos, pBlockState);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      if (pTag.contains("RecordItem", 10)) {
         this.setRecord(ItemStack.of(pTag.getCompound("RecordItem")));
      }

      this.isPlaying = pTag.getBoolean("IsPlaying");
      this.recordStartedTick = pTag.getLong("RecordStartTick");
      this.tickCount = pTag.getLong("TickCount");
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (!this.getRecord().isEmpty()) {
         pTag.put("RecordItem", this.getRecord().save(new CompoundTag()));
      }

      pTag.putBoolean("IsPlaying", this.isPlaying);
      pTag.putLong("RecordStartTick", this.recordStartedTick);
      pTag.putLong("TickCount", this.tickCount);
   }

   public ItemStack getRecord() {
      return this.record;
   }

   public void setRecord(ItemStack pRecord) {
      this.record = pRecord;
      this.setChanged();
   }

   public void playRecord() {
      this.recordStartedTick = this.tickCount;
      this.isPlaying = true;
   }

   public void clearContent() {
      this.setRecord(ItemStack.EMPTY);
      this.isPlaying = false;
   }

   public static void playRecordTick(Level pLevel, BlockPos pPos, BlockState pState, JukeboxBlockEntity pJukebox) {
      ++pJukebox.ticksSinceLastEvent;
      if (recordIsPlaying(pState, pJukebox)) {
         Item item = pJukebox.getRecord().getItem();
         if (item instanceof RecordItem) {
            RecordItem recorditem = (RecordItem)item;
            if (recordShouldStopPlaying(pJukebox, recorditem)) {
               pLevel.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, pPos, GameEvent.Context.of(pState));
               pJukebox.isPlaying = false;
            } else if (shouldSendJukeboxPlayingEvent(pJukebox)) {
               pJukebox.ticksSinceLastEvent = 0;
               pLevel.gameEvent(GameEvent.JUKEBOX_PLAY, pPos, GameEvent.Context.of(pState));
            }
         }
      }

      ++pJukebox.tickCount;
   }

   private static boolean recordIsPlaying(BlockState pState, JukeboxBlockEntity pJukebox) {
      return pState.getValue(JukeboxBlock.HAS_RECORD) && pJukebox.isPlaying;
   }

   private static boolean recordShouldStopPlaying(JukeboxBlockEntity pJukebox, RecordItem pRecord) {
      return pJukebox.tickCount >= pJukebox.recordStartedTick + (long)pRecord.getLengthInTicks();
   }

   private static boolean shouldSendJukeboxPlayingEvent(JukeboxBlockEntity pJukebox) {
      return pJukebox.ticksSinceLastEvent >= 20;
   }
}