package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractScrollWidget extends AbstractWidget implements Widget, GuiEventListener {
   private static final int BORDER_COLOR_FOCUSED = -1;
   private static final int BORDER_COLOR = -6250336;
   private static final int BACKGROUND_COLOR = -16777216;
   private static final int INNER_PADDING = 4;
   private double scrollAmount;
   private boolean scrolling;

   public AbstractScrollWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
      super(pX, pY, pWidth, pHeight, pMessage);
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (!this.visible) {
         return false;
      } else {
         boolean flag = this.withinContentAreaPoint(pMouseX, pMouseY);
         boolean flag1 = this.scrollbarVisible() && pMouseX >= (double)(this.x + this.width) && pMouseX <= (double)(this.x + this.width + 8) && pMouseY >= (double)this.y && pMouseY < (double)(this.y + this.height);
         this.setFocused(flag || flag1);
         if (flag1 && pButton == 0) {
            this.scrolling = true;
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      if (pButton == 0) {
         this.scrolling = false;
      }

      return super.mouseReleased(pMouseX, pMouseY, pButton);
   }

   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (this.visible && this.isFocused() && this.scrolling) {
         if (pMouseY < (double)this.y) {
            this.setScrollAmount(0.0D);
         } else if (pMouseY > (double)(this.y + this.height)) {
            this.setScrollAmount((double)this.getMaxScrollAmount());
         } else {
            int i = this.getScrollBarHeight();
            double d0 = (double)Math.max(1, this.getMaxScrollAmount() / (this.height - i));
            this.setScrollAmount(this.scrollAmount + pDragY * d0);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      if (this.visible && this.isFocused()) {
         this.setScrollAmount(this.scrollAmount - pDelta * this.scrollRate());
         return true;
      } else {
         return false;
      }
   }

   public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.visible) {
         this.renderBackground(pPoseStack);
         enableScissor(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1);
         pPoseStack.pushPose();
         pPoseStack.translate(0.0D, -this.scrollAmount, 0.0D);
         this.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
         pPoseStack.popPose();
         disableScissor();
         this.renderDecorations(pPoseStack);
      }
   }

   private int getScrollBarHeight() {
      return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
   }

   protected void renderDecorations(PoseStack pPoseStack) {
      if (this.scrollbarVisible()) {
         this.renderScrollBar();
      }

   }

   protected int innerPadding() {
      return 4;
   }

   protected int totalInnerPadding() {
      return this.innerPadding() * 2;
   }

   protected double scrollAmount() {
      return this.scrollAmount;
   }

   protected void setScrollAmount(double pScrollAmount) {
      this.scrollAmount = Mth.clamp(pScrollAmount, 0.0D, (double)this.getMaxScrollAmount());
   }

   protected int getMaxScrollAmount() {
      return Math.max(0, this.getContentHeight() - (this.height - 4));
   }

   private int getContentHeight() {
      return this.getInnerHeight() + 4;
   }

   private void renderBackground(PoseStack pPoseStack) {
      int i = this.isFocused() ? -1 : -6250336;
      fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, i);
      fill(pPoseStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, -16777216);
   }

   private void renderScrollBar() {
      int i = this.getScrollBarHeight();
      int j = this.x + this.width;
      int k = this.x + this.width + 8;
      int l = Math.max(this.y, (int)this.scrollAmount * (this.height - i) / this.getMaxScrollAmount() + this.y);
      int i1 = l + i;
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      bufferbuilder.vertex((double)j, (double)i1, 0.0D).color(128, 128, 128, 255).endVertex();
      bufferbuilder.vertex((double)k, (double)i1, 0.0D).color(128, 128, 128, 255).endVertex();
      bufferbuilder.vertex((double)k, (double)l, 0.0D).color(128, 128, 128, 255).endVertex();
      bufferbuilder.vertex((double)j, (double)l, 0.0D).color(128, 128, 128, 255).endVertex();
      bufferbuilder.vertex((double)j, (double)(i1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
      bufferbuilder.vertex((double)(k - 1), (double)(i1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
      bufferbuilder.vertex((double)(k - 1), (double)l, 0.0D).color(192, 192, 192, 255).endVertex();
      bufferbuilder.vertex((double)j, (double)l, 0.0D).color(192, 192, 192, 255).endVertex();
      tesselator.end();
   }

   protected boolean withinContentAreaTopBottom(int pTop, int pBottom) {
      return (double)pBottom - this.scrollAmount >= (double)this.y && (double)pTop - this.scrollAmount <= (double)(this.y + this.height);
   }

   protected boolean withinContentAreaPoint(double pX, double pY) {
      return pX >= (double)this.x && pX < (double)(this.x + this.width) && pY >= (double)this.y && pY < (double)(this.y + this.height);
   }

   protected abstract int getInnerHeight();

   protected abstract boolean scrollbarVisible();

   protected abstract double scrollRate();

   protected abstract void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick);
}