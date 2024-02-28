package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacketData {
   private final BitSet skyYMask;
   private final BitSet blockYMask;
   private final BitSet emptySkyYMask;
   private final BitSet emptyBlockYMask;
   private final List<byte[]> skyUpdates;
   private final List<byte[]> blockUpdates;
   private final boolean trustEdges;

   public ClientboundLightUpdatePacketData(ChunkPos pChunkPos, LevelLightEngine pLevelLightEngine, @Nullable BitSet pSkyLight, @Nullable BitSet pBlockLight, boolean pTrustEdges) {
      this.trustEdges = pTrustEdges;
      this.skyYMask = new BitSet();
      this.blockYMask = new BitSet();
      this.emptySkyYMask = new BitSet();
      this.emptyBlockYMask = new BitSet();
      this.skyUpdates = Lists.newArrayList();
      this.blockUpdates = Lists.newArrayList();

      for(int i = 0; i < pLevelLightEngine.getLightSectionCount(); ++i) {
         if (pSkyLight == null || pSkyLight.get(i)) {
            this.prepareSectionData(pChunkPos, pLevelLightEngine, LightLayer.SKY, i, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
         }

         if (pBlockLight == null || pBlockLight.get(i)) {
            this.prepareSectionData(pChunkPos, pLevelLightEngine, LightLayer.BLOCK, i, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
         }
      }

   }

   public ClientboundLightUpdatePacketData(FriendlyByteBuf pBuffer, int p_195738_, int p_195739_) {
      this.trustEdges = pBuffer.readBoolean();
      this.skyYMask = pBuffer.readBitSet();
      this.blockYMask = pBuffer.readBitSet();
      this.emptySkyYMask = pBuffer.readBitSet();
      this.emptyBlockYMask = pBuffer.readBitSet();
      this.skyUpdates = pBuffer.readList((p_195756_) -> {
         return p_195756_.readByteArray(2048);
      });
      this.blockUpdates = pBuffer.readList((p_195753_) -> {
         return p_195753_.readByteArray(2048);
      });
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBoolean(this.trustEdges);
      pBuffer.writeBitSet(this.skyYMask);
      pBuffer.writeBitSet(this.blockYMask);
      pBuffer.writeBitSet(this.emptySkyYMask);
      pBuffer.writeBitSet(this.emptyBlockYMask);
      pBuffer.writeCollection(this.skyUpdates, FriendlyByteBuf::writeByteArray);
      pBuffer.writeCollection(this.blockUpdates, FriendlyByteBuf::writeByteArray);
   }

   private void prepareSectionData(ChunkPos pChunkPos, LevelLightEngine pLevelLightEngine, LightLayer p_195744_, int p_195745_, BitSet pSkyLight, BitSet pBlockLight, List<byte[]> p_195748_) {
      DataLayer datalayer = pLevelLightEngine.getLayerListener(p_195744_).getDataLayerData(SectionPos.of(pChunkPos, pLevelLightEngine.getMinLightSection() + p_195745_));
      if (datalayer != null) {
         if (datalayer.isEmpty()) {
            pBlockLight.set(p_195745_);
         } else {
            pSkyLight.set(p_195745_);
            p_195748_.add((byte[])datalayer.getData().clone());
         }
      }

   }

   public BitSet getSkyYMask() {
      return this.skyYMask;
   }

   public BitSet getEmptySkyYMask() {
      return this.emptySkyYMask;
   }

   public List<byte[]> getSkyUpdates() {
      return this.skyUpdates;
   }

   public BitSet getBlockYMask() {
      return this.blockYMask;
   }

   public BitSet getEmptyBlockYMask() {
      return this.emptyBlockYMask;
   }

   public List<byte[]> getBlockUpdates() {
      return this.blockUpdates;
   }

   public boolean getTrustEdges() {
      return this.trustEdges;
   }
}