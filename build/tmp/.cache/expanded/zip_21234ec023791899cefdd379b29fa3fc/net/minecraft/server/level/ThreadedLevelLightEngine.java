package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;

public class ThreadedLevelLightEngine extends LevelLightEngine implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ProcessorMailbox<Runnable> taskMailbox;
   private final ObjectList<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> lightTasks = new ObjectArrayList<>();
   private final ChunkMap chunkMap;
   private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> sorterMailbox;
   private volatile int taskPerBatch = 5;
   private final AtomicBoolean scheduled = new AtomicBoolean();

   public ThreadedLevelLightEngine(LightChunkGetter pLightChunk, ChunkMap pChunkMap, boolean pSkyLight, ProcessorMailbox<Runnable> pTaskMailbox, ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> pSorterMailbox) {
      super(pLightChunk, true, pSkyLight);
      this.chunkMap = pChunkMap;
      this.sorterMailbox = pSorterMailbox;
      this.taskMailbox = pTaskMailbox;
   }

   public void close() {
   }

   public int runUpdates(int pPos, boolean pIsQueueEmpty, boolean pUpdateBlockLight) {
      throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
   }

   public void onBlockEmissionIncrease(BlockPos pPos, int pEmissionLevel) {
      throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
   }

   public void checkBlock(BlockPos pPos) {
      BlockPos blockpos = pPos.immutable();
      this.addTask(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()), ThreadedLevelLightEngine.TaskType.POST_UPDATE, Util.name(() -> {
         super.checkBlock(blockpos);
      }, () -> {
         return "checkBlock " + blockpos;
      }));
   }

   protected void updateChunkStatus(ChunkPos p_9331_) {
      this.addTask(p_9331_.x, p_9331_.z, () -> {
         return 0;
      }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.retainData(p_9331_, false);
         super.enableLightSources(p_9331_, false);

         for(int i = this.getMinLightSection(); i < this.getMaxLightSection(); ++i) {
            super.queueSectionData(LightLayer.BLOCK, SectionPos.of(p_9331_, i), (DataLayer)null, true);
            super.queueSectionData(LightLayer.SKY, SectionPos.of(p_9331_, i), (DataLayer)null, true);
         }

         for(int j = this.levelHeightAccessor.getMinSection(); j < this.levelHeightAccessor.getMaxSection(); ++j) {
            super.updateSectionStatus(SectionPos.of(p_9331_, j), true);
         }

      }, () -> {
         return "updateChunkStatus " + p_9331_ + " true";
      }));
   }

   public void updateSectionStatus(SectionPos pPos, boolean pIsEmpty) {
      this.addTask(pPos.x(), pPos.z(), () -> {
         return 0;
      }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.updateSectionStatus(pPos, pIsEmpty);
      }, () -> {
         return "updateSectionStatus " + pPos + " " + pIsEmpty;
      }));
   }

   public void enableLightSources(ChunkPos pChunkPos, boolean pIsQueueEmpty) {
      this.addTask(pChunkPos.x, pChunkPos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.enableLightSources(pChunkPos, pIsQueueEmpty);
      }, () -> {
         return "enableLight " + pChunkPos + " " + pIsQueueEmpty;
      }));
   }

   public void queueSectionData(LightLayer pType, SectionPos pPos, @Nullable DataLayer pArray, boolean p_9342_) {
      this.addTask(pPos.x(), pPos.z(), () -> {
         return 0;
      }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.queueSectionData(pType, pPos, pArray, p_9342_);
      }, () -> {
         return "queueData " + pPos;
      }));
   }

   private void addTask(int pChunkX, int pChunkZ, ThreadedLevelLightEngine.TaskType p_9315_, Runnable p_9316_) {
      this.addTask(pChunkX, pChunkZ, this.chunkMap.getChunkQueueLevel(ChunkPos.asLong(pChunkX, pChunkZ)), p_9315_, p_9316_);
   }

   private void addTask(int pChunkX, int pChunkZ, IntSupplier p_9320_, ThreadedLevelLightEngine.TaskType p_9321_, Runnable p_9322_) {
      this.sorterMailbox.tell(ChunkTaskPriorityQueueSorter.message(() -> {
         this.lightTasks.add(Pair.of(p_9321_, p_9322_));
         if (this.lightTasks.size() >= this.taskPerBatch) {
            this.runUpdate();
         }

      }, ChunkPos.asLong(pChunkX, pChunkZ), p_9320_));
   }

   public void retainData(ChunkPos pPos, boolean pRetain) {
      this.addTask(pPos.x, pPos.z, () -> {
         return 0;
      }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.retainData(pPos, pRetain);
      }, () -> {
         return "retainData " + pPos;
      }));
   }

   public CompletableFuture<ChunkAccess> retainData(ChunkAccess p_215137_) {
      ChunkPos chunkpos = p_215137_.getPos();
      return CompletableFuture.supplyAsync(Util.name(() -> {
         super.retainData(chunkpos, true);
         return p_215137_;
      }, () -> {
         return "retainData: " + chunkpos;
      }), (p_215152_) -> {
         this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, p_215152_);
      });
   }

   public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess pChunk, boolean p_9355_) {
      ChunkPos chunkpos = pChunk.getPos();
      pChunk.setLightCorrect(false);
      this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         LevelChunkSection[] alevelchunksection = pChunk.getSections();

         for(int i = 0; i < pChunk.getSectionsCount(); ++i) {
            LevelChunkSection levelchunksection = alevelchunksection[i];
            if (!levelchunksection.hasOnlyAir()) {
               int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
               super.updateSectionStatus(SectionPos.of(chunkpos, j), false);
            }
         }

         super.enableLightSources(chunkpos, true);
         if (!p_9355_) {
            pChunk.getLights().forEach((p_215147_) -> {
               super.onBlockEmissionIncrease(p_215147_, pChunk.getLightEmission(p_215147_));
            });
         }

      }, () -> {
         return "lightChunk " + chunkpos + " " + p_9355_;
      }));
      return CompletableFuture.supplyAsync(() -> {
         pChunk.setLightCorrect(true);
         super.retainData(chunkpos, false);
         this.chunkMap.releaseLightTicket(chunkpos);
         return pChunk;
      }, (p_215135_) -> {
         this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.POST_UPDATE, p_215135_);
      });
   }

   public void tryScheduleUpdate() {
      if ((!this.lightTasks.isEmpty() || super.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
         this.taskMailbox.tell(() -> {
            this.runUpdate();
            this.scheduled.set(false);
         });
      }

   }

   private void runUpdate() {
      int i = Math.min(this.lightTasks.size(), this.taskPerBatch);
      ObjectListIterator<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> objectlistiterator = this.lightTasks.iterator();

      int j;
      for(j = 0; objectlistiterator.hasNext() && j < i; ++j) {
         Pair<ThreadedLevelLightEngine.TaskType, Runnable> pair = objectlistiterator.next();
         if (pair.getFirst() == ThreadedLevelLightEngine.TaskType.PRE_UPDATE) {
            pair.getSecond().run();
         }
      }

      objectlistiterator.back(j);
      super.runUpdates(Integer.MAX_VALUE, true, true);

      for(int k = 0; objectlistiterator.hasNext() && k < i; ++k) {
         Pair<ThreadedLevelLightEngine.TaskType, Runnable> pair1 = objectlistiterator.next();
         if (pair1.getFirst() == ThreadedLevelLightEngine.TaskType.POST_UPDATE) {
            pair1.getSecond().run();
         }

         objectlistiterator.remove();
      }

   }

   public void setTaskPerBatch(int pTaskPerBatch) {
      this.taskPerBatch = pTaskPerBatch;
   }

   static enum TaskType {
      PRE_UPDATE,
      POST_UPDATE;
   }
}