package net.minecraft.network.chat;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;

public class FilterMask {
   public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), FilterMask.Type.FULLY_FILTERED);
   public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), FilterMask.Type.PASS_THROUGH);
   private static final char HASH = '#';
   private final BitSet mask;
   private final FilterMask.Type type;

   private FilterMask(BitSet pMask, FilterMask.Type pType) {
      this.mask = pMask;
      this.type = pType;
   }

   public FilterMask(int pSize) {
      this(new BitSet(pSize), FilterMask.Type.PARTIALLY_FILTERED);
   }

   public static FilterMask read(FriendlyByteBuf pBuffer) {
      FilterMask.Type filtermask$type = pBuffer.readEnum(FilterMask.Type.class);
      FilterMask filtermask;
      switch (filtermask$type) {
         case PASS_THROUGH:
            filtermask = PASS_THROUGH;
            break;
         case FULLY_FILTERED:
            filtermask = FULLY_FILTERED;
            break;
         case PARTIALLY_FILTERED:
            filtermask = new FilterMask(pBuffer.readBitSet(), FilterMask.Type.PARTIALLY_FILTERED);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return filtermask;
   }

   public static void write(FriendlyByteBuf pBuffer, FilterMask pMask) {
      pBuffer.writeEnum(pMask.type);
      if (pMask.type == FilterMask.Type.PARTIALLY_FILTERED) {
         pBuffer.writeBitSet(pMask.mask);
      }

   }

   public void setFiltered(int pBitIndex) {
      this.mask.set(pBitIndex);
   }

   @Nullable
   public String apply(String pText) {
      String s;
      switch (this.type) {
         case PASS_THROUGH:
            s = pText;
            break;
         case FULLY_FILTERED:
            s = null;
            break;
         case PARTIALLY_FILTERED:
            char[] achar = pText.toCharArray();

            for(int i = 0; i < achar.length && i < this.mask.length(); ++i) {
               if (this.mask.get(i)) {
                  achar[i] = '#';
               }
            }

            s = new String(achar);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return s;
   }

   @Nullable
   public Component apply(ChatMessageContent pContent) {
      String s = pContent.plain();
      return Util.mapNullable(this.apply(s), Component::literal);
   }

   public boolean isEmpty() {
      return this.type == FilterMask.Type.PASS_THROUGH;
   }

   public boolean isFullyFiltered() {
      return this.type == FilterMask.Type.FULLY_FILTERED;
   }

   static enum Type {
      PASS_THROUGH,
      FULLY_FILTERED,
      PARTIALLY_FILTERED;
   }
}