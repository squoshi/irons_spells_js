package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerFaceRenderer {
   public static final int SKIN_HEAD_U = 8;
   public static final int SKIN_HEAD_V = 8;
   public static final int SKIN_HEAD_WIDTH = 8;
   public static final int SKIN_HEAD_HEIGHT = 8;
   public static final int SKIN_HAT_U = 40;
   public static final int SKIN_HAT_V = 8;
   public static final int SKIN_HAT_WIDTH = 8;
   public static final int SKIN_HAT_HEIGHT = 8;
   public static final int SKIN_TEX_WIDTH = 64;
   public static final int SKIN_TEX_HEIGHT = 64;

   public static void draw(PoseStack pPoseStack, int pX, int pY, int pSize) {
      draw(pPoseStack, pX, pY, pSize, true, false);
   }

   public static void draw(PoseStack pPoseStack, int pX, int pY, int pSize, boolean pDrawHat, boolean pUpsideDown) {
      int i = 8 + (pUpsideDown ? 8 : 0);
      int j = 8 * (pUpsideDown ? -1 : 1);
      GuiComponent.blit(pPoseStack, pX, pY, pSize, pSize, 8.0F, (float)i, 8, j, 64, 64);
      if (pDrawHat) {
         drawHat(pPoseStack, pX, pY, pSize, pUpsideDown);
      }

   }

   private static void drawHat(PoseStack pPoseStack, int pX, int pY, int pSize, boolean pUpsideDown) {
      int i = 8 + (pUpsideDown ? 8 : 0);
      int j = 8 * (pUpsideDown ? -1 : 1);
      RenderSystem.enableBlend();
      GuiComponent.blit(pPoseStack, pX, pY, pSize, pSize, 40.0F, (float)i, 8, j, 64, 64);
      RenderSystem.disableBlend();
   }
}