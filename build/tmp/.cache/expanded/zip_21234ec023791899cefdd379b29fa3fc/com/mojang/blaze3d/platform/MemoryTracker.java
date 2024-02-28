package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class MemoryTracker {
   private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

   public static ByteBuffer create(int pSize) {
      long i = ALLOCATOR.malloc((long)pSize);
      if (i == 0L) {
         throw new OutOfMemoryError("Failed to allocate " + pSize + " bytes");
      } else {
         return MemoryUtil.memByteBuffer(i, pSize);
      }
   }

   public static ByteBuffer resize(ByteBuffer pBuffer, int pByteSize) {
      long i = ALLOCATOR.realloc(MemoryUtil.memAddress0(pBuffer), (long)pByteSize);
      if (i == 0L) {
         throw new OutOfMemoryError("Failed to resize buffer from " + pBuffer.capacity() + " bytes to " + pByteSize + " bytes");
      } else {
         return MemoryUtil.memByteBuffer(i, pByteSize);
      }
   }
}