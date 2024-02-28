package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GenericWaitingScreen extends Screen {
   private static final int TITLE_Y = 80;
   private static final int MESSAGE_Y = 120;
   private static final int MESSAGE_MAX_WIDTH = 360;
   @Nullable
   private final Component messageText;
   private final Component buttonLabel;
   private final Runnable buttonCallback;
   @Nullable
   private MultiLineLabel message;
   private Button button;
   private int disableButtonTicks;

   public static GenericWaitingScreen createWaiting(Component pTitle, Component pButtonLabel, Runnable pButtonCallback) {
      return new GenericWaitingScreen(pTitle, (Component)null, pButtonLabel, pButtonCallback, 0);
   }

   public static GenericWaitingScreen createCompleted(Component pTitle, Component pMessageText, Component pButtonLabel, Runnable pButtonCallback) {
      return new GenericWaitingScreen(pTitle, pMessageText, pButtonLabel, pButtonCallback, 20);
   }

   protected GenericWaitingScreen(Component pTitle, @Nullable Component pMessageText, Component pButtonLabel, Runnable pButtonCallback, int pDisableButtonTicks) {
      super(pTitle);
      this.messageText = pMessageText;
      this.buttonLabel = pButtonLabel;
      this.buttonCallback = pButtonCallback;
      this.disableButtonTicks = pDisableButtonTicks;
   }

   protected void init() {
      super.init();
      if (this.messageText != null) {
         this.message = MultiLineLabel.create(this.font, this.messageText, 360);
      }

      int i = 150;
      int j = 20;
      int k = this.message != null ? this.message.getLineCount() : 1;
      int l = Math.max(k, 5) * 9;
      int i1 = Math.min(120 + l, this.height - 40);
      this.button = this.addRenderableWidget(new Button((this.width - 150) / 2, i1, 150, 20, this.buttonLabel, (p_239908_) -> {
         this.onClose();
      }));
   }

   public void tick() {
      if (this.disableButtonTicks > 0) {
         --this.disableButtonTicks;
      }

      this.button.active = this.disableButtonTicks == 0;
   }

   public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pPoseStack);
      drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 80, 16777215);
      if (this.message == null) {
         String s = LoadingDotsText.get(Util.getMillis());
         drawCenteredString(pPoseStack, this.font, s, this.width / 2, 120, 10526880);
      } else {
         this.message.renderCentered(pPoseStack, this.width / 2, 120);
      }

      super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
   }

   public boolean shouldCloseOnEsc() {
      return this.message != null && this.button.active;
   }

   public void onClose() {
      this.buttonCallback.run();
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(this.title, this.messageText != null ? this.messageText : CommonComponents.EMPTY);
   }
}