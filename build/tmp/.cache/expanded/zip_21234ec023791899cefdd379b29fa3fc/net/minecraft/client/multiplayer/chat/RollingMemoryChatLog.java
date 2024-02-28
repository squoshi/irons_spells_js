package net.minecraft.client.multiplayer.chat;

import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RollingMemoryChatLog implements ChatLog {
   private final LoggedChatEvent[] buffer;
   private int newestId = -1;
   private int oldestId = -1;

   public RollingMemoryChatLog(int pSize) {
      this.buffer = new LoggedChatEvent[pSize];
   }

   public void push(LoggedChatEvent pEvent) {
      int i = this.nextId();
      this.buffer[this.index(i)] = pEvent;
   }

   private int nextId() {
      int i = ++this.newestId;
      if (i >= this.buffer.length) {
         ++this.oldestId;
      } else {
         this.oldestId = 0;
      }

      return i;
   }

   @Nullable
   public LoggedChatEvent lookup(int pId) {
      return this.contains(pId) ? this.buffer[this.index(pId)] : null;
   }

   private int index(int pId) {
      return pId % this.buffer.length;
   }

   public boolean contains(int pId) {
      return pId >= this.oldestId && pId <= this.newestId;
   }

   public int offset(int pId, int pOffset) {
      int i = pId + pOffset;
      return this.contains(i) ? i : -1;
   }

   public int newest() {
      return this.newestId;
   }

   public int oldest() {
      return this.oldestId;
   }
}