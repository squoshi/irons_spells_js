package net.minecraft.world.level.gameevent;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class DynamicGameEventListener<T extends GameEventListener> {
   private T listener;
   @Nullable
   private SectionPos lastSection;

   public DynamicGameEventListener(T pListener) {
      this.listener = pListener;
   }

   public void add(ServerLevel pLevel) {
      this.move(pLevel);
   }

   public void updateListener(T pListener, @Nullable Level pLevel) {
      T t = this.listener;
      if (t != pListener) {
         if (pLevel instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)pLevel;
            ifChunkExists(serverlevel, this.lastSection, (p_223640_) -> {
               p_223640_.unregister(t);
            });
            ifChunkExists(serverlevel, this.lastSection, (p_223633_) -> {
               p_223633_.register(pListener);
            });
         }

         this.listener = pListener;
      }
   }

   public T getListener() {
      return this.listener;
   }

   public void remove(ServerLevel pLevel) {
      ifChunkExists(pLevel, this.lastSection, (p_223644_) -> {
         p_223644_.unregister(this.listener);
      });
   }

   public void move(ServerLevel pLevel) {
      this.listener.getListenerSource().getPosition(pLevel).map(SectionPos::of).ifPresent((p_223621_) -> {
         if (this.lastSection == null || !this.lastSection.equals(p_223621_)) {
            ifChunkExists(pLevel, this.lastSection, (p_223637_) -> {
               p_223637_.unregister(this.listener);
            });
            this.lastSection = p_223621_;
            ifChunkExists(pLevel, this.lastSection, (p_223627_) -> {
               p_223627_.register(this.listener);
            });
         }

      });
   }

   private static void ifChunkExists(LevelReader pLevel, @Nullable SectionPos pSectionPos, Consumer<GameEventDispatcher> pDispatcherConsumer) {
      if (pSectionPos != null) {
         ChunkAccess chunkaccess = pLevel.getChunk(pSectionPos.x(), pSectionPos.z(), ChunkStatus.FULL, false);
         if (chunkaccess != null) {
            pDispatcherConsumer.accept(chunkaccess.getEventDispatcher(pSectionPos.y()));
         }

      }
   }
}