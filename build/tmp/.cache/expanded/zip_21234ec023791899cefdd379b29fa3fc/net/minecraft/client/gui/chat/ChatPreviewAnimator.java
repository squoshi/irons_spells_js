package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatPreviewAnimator {
   private static final long FADE_DURATION = 200L;
   @Nullable
   private Component residualPreview;
   private long fadeTime;
   private long lastTime;

   public void reset(long pLastTime) {
      this.residualPreview = null;
      this.fadeTime = 0L;
      this.lastTime = pLastTime;
   }

   public ChatPreviewAnimator.State get(long pMillis, @Nullable Component pPreview) {
      long i = pMillis - this.lastTime;
      this.lastTime = pMillis;
      return pPreview != null ? this.getEnabled(i, pPreview) : this.getDisabled(i);
   }

   private ChatPreviewAnimator.State getEnabled(long pResidualPreview, Component pPreview) {
      this.residualPreview = pPreview;
      if (this.fadeTime < 200L) {
         this.fadeTime = Math.min(this.fadeTime + pResidualPreview, 200L);
      }

      return new ChatPreviewAnimator.State(pPreview, alpha(this.fadeTime));
   }

   private ChatPreviewAnimator.State getDisabled(long p_242440_) {
      if (this.fadeTime > 0L) {
         this.fadeTime = Math.max(this.fadeTime - p_242440_, 0L);
      }

      return this.fadeTime > 0L ? new ChatPreviewAnimator.State(this.residualPreview, alpha(this.fadeTime)) : ChatPreviewAnimator.State.DISABLED;
   }

   private static float alpha(long pFadeTime) {
      return (float)pFadeTime / 200.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public static record State(@Nullable Component preview, float alpha) {
      public static final ChatPreviewAnimator.State DISABLED = new ChatPreviewAnimator.State((Component)null, 0.0F);
   }
}