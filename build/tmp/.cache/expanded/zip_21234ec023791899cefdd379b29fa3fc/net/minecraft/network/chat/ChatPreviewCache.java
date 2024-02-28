package net.minecraft.network.chat;

import javax.annotation.Nullable;

public class ChatPreviewCache {
   @Nullable
   private ChatPreviewCache.Result result;

   public void set(String pQuery, Component pPreview) {
      this.result = new ChatPreviewCache.Result(pQuery, pPreview);
   }

   @Nullable
   public Component pull(String pQuery) {
      ChatPreviewCache.Result chatpreviewcache$result = this.result;
      if (chatpreviewcache$result != null && chatpreviewcache$result.matches(pQuery)) {
         this.result = null;
         return chatpreviewcache$result.preview();
      } else {
         return null;
      }
   }

   static record Result(String query, Component preview) {
      public boolean matches(String pQuery) {
         return this.query.equals(pQuery);
      }
   }
}