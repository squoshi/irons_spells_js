package net.minecraft.world.entity.ai.village.poi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.VisibleForDebug;

public class PoiRecord {
   private final BlockPos pos;
   private final Holder<PoiType> poiType;
   private int freeTickets;
   private final Runnable setDirty;

   public static Codec<PoiRecord> codec(Runnable pExecutable) {
      return RecordCodecBuilder.create((p_27246_) -> {
         return p_27246_.group(BlockPos.CODEC.fieldOf("pos").forGetter((p_148673_) -> {
            return p_148673_.pos;
         }), RegistryFixedCodec.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY).fieldOf("type").forGetter((p_218017_) -> {
            return p_218017_.poiType;
         }), Codec.INT.fieldOf("free_tickets").orElse(0).forGetter((p_148669_) -> {
            return p_148669_.freeTickets;
         }), RecordCodecBuilder.point(pExecutable)).apply(p_27246_, PoiRecord::new);
      });
   }

   private PoiRecord(BlockPos p_218008_, Holder<PoiType> p_218009_, int p_218010_, Runnable p_218011_) {
      this.pos = p_218008_.immutable();
      this.poiType = p_218009_;
      this.freeTickets = p_218010_;
      this.setDirty = p_218011_;
   }

   public PoiRecord(BlockPos pPod, Holder<PoiType> pPoiType, Runnable pSetDirty) {
      this(pPod, pPoiType, pPoiType.value().maxTickets(), pSetDirty);
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public int getFreeTickets() {
      return this.freeTickets;
   }

   protected boolean acquireTicket() {
      if (this.freeTickets <= 0) {
         return false;
      } else {
         --this.freeTickets;
         this.setDirty.run();
         return true;
      }
   }

   protected boolean releaseTicket() {
      if (this.freeTickets >= this.poiType.value().maxTickets()) {
         return false;
      } else {
         ++this.freeTickets;
         this.setDirty.run();
         return true;
      }
   }

   public boolean hasSpace() {
      return this.freeTickets > 0;
   }

   public boolean isOccupied() {
      return this.freeTickets != this.poiType.value().maxTickets();
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Holder<PoiType> getPoiType() {
      return this.poiType;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther != null && this.getClass() == pOther.getClass() ? Objects.equals(this.pos, ((PoiRecord)pOther).pos) : false;
      }
   }

   public int hashCode() {
      return this.pos.hashCode();
   }
}