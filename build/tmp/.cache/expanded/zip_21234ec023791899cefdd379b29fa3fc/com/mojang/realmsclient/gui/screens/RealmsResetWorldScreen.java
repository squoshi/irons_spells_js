package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import com.mojang.realmsclient.util.task.LongRunningTask;
import com.mojang.realmsclient.util.task.ResettingGeneratedWorldTask;
import com.mojang.realmsclient.util.task.ResettingTemplateWorldTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsResetWorldScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private final Screen lastScreen;
   private final RealmsServer serverData;
   private Component subtitle = Component.translatable("mco.reset.world.warning");
   private Component buttonTitle = CommonComponents.GUI_CANCEL;
   private int subtitleColor = 16711680;
   private static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
   private static final ResourceLocation UPLOAD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/upload.png");
   private static final ResourceLocation ADVENTURE_MAP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/adventure.png");
   private static final ResourceLocation SURVIVAL_SPAWN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/survival_spawn.png");
   private static final ResourceLocation NEW_WORLD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/new_world.png");
   private static final ResourceLocation EXPERIENCE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/experience.png");
   private static final ResourceLocation INSPIRATION_LOCATION = new ResourceLocation("realms", "textures/gui/realms/inspiration.png");
   WorldTemplatePaginatedList templates;
   WorldTemplatePaginatedList adventuremaps;
   WorldTemplatePaginatedList experiences;
   WorldTemplatePaginatedList inspirations;
   public int slot = -1;
   private Component resetTitle = Component.translatable("mco.reset.world.resetting.screen.title");
   private final Runnable resetWorldRunnable;
   private final Runnable callback;

   public RealmsResetWorldScreen(Screen pLastScreen, RealmsServer pServerData, Component pTitle, Runnable pResetWorldRunnable, Runnable pCallback) {
      super(pTitle);
      this.lastScreen = pLastScreen;
      this.serverData = pServerData;
      this.resetWorldRunnable = pResetWorldRunnable;
      this.callback = pCallback;
   }

   public RealmsResetWorldScreen(Screen pLastScreen, RealmsServer pServerData, Runnable pResetWorldRunnable, Runnable pCallback) {
      this(pLastScreen, pServerData, Component.translatable("mco.reset.world.title"), pResetWorldRunnable, pCallback);
   }

   public RealmsResetWorldScreen(Screen pLastScreen, RealmsServer pServerData, Component pTitle, Component pSubtitle, int pSubtitleColor, Component pButtonTitle, Runnable pResetWorldRunnable, Runnable pCallback) {
      this(pLastScreen, pServerData, pTitle, pResetWorldRunnable, pCallback);
      this.subtitle = pSubtitle;
      this.subtitleColor = pSubtitleColor;
      this.buttonTitle = pButtonTitle;
   }

   public void setSlot(int pSlot) {
      this.slot = pSlot;
   }

   public void setResetTitle(Component pResetTitle) {
      this.resetTitle = pResetTitle;
   }

   public void init() {
      this.addRenderableWidget(new Button(this.width / 2 - 40, row(14) - 10, 80, 20, this.buttonTitle, (p_89419_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
      (new Thread("Realms-reset-world-fetcher") {
         public void run() {
            RealmsClient realmsclient = RealmsClient.create();

            try {
               WorldTemplatePaginatedList worldtemplatepaginatedlist = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.NORMAL);
               WorldTemplatePaginatedList worldtemplatepaginatedlist1 = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.ADVENTUREMAP);
               WorldTemplatePaginatedList worldtemplatepaginatedlist2 = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.EXPERIENCE);
               WorldTemplatePaginatedList worldtemplatepaginatedlist3 = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.INSPIRATION);
               RealmsResetWorldScreen.this.minecraft.execute(() -> {
                  RealmsResetWorldScreen.this.templates = worldtemplatepaginatedlist;
                  RealmsResetWorldScreen.this.adventuremaps = worldtemplatepaginatedlist1;
                  RealmsResetWorldScreen.this.experiences = worldtemplatepaginatedlist2;
                  RealmsResetWorldScreen.this.inspirations = worldtemplatepaginatedlist3;
               });
            } catch (RealmsServiceException realmsserviceexception) {
               RealmsResetWorldScreen.LOGGER.error("Couldn't fetch templates in reset world", (Throwable)realmsserviceexception);
            }

         }
      }).start();
      this.addLabel(new RealmsLabel(this.subtitle, this.width / 2, 22, this.subtitleColor));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(1), row(0) + 10, Component.translatable("mco.reset.world.generate"), NEW_WORLD_LOCATION, (p_89417_) -> {
         this.minecraft.setScreen(new RealmsResetNormalWorldScreen(this::generationSelectionCallback, this.title));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(2), row(0) + 10, Component.translatable("mco.reset.world.upload"), UPLOAD_LOCATION, (p_89415_) -> {
         this.minecraft.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this, this.callback));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(3), row(0) + 10, Component.translatable("mco.reset.world.template"), SURVIVAL_SPAWN_LOCATION, (p_89412_) -> {
         this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.template"), this::templateSelectionCallback, RealmsServer.WorldType.NORMAL, this.templates));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(1), row(6) + 20, Component.translatable("mco.reset.world.adventure"), ADVENTURE_MAP_LOCATION, (p_89407_) -> {
         this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.adventure"), this::templateSelectionCallback, RealmsServer.WorldType.ADVENTUREMAP, this.adventuremaps));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(2), row(6) + 20, Component.translatable("mco.reset.world.experience"), EXPERIENCE_LOCATION, (p_89402_) -> {
         this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.experience"), this::templateSelectionCallback, RealmsServer.WorldType.EXPERIENCE, this.experiences));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(3), row(6) + 20, Component.translatable("mco.reset.world.inspiration"), INSPIRATION_LOCATION, (p_89381_) -> {
         this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.inspiration"), this::templateSelectionCallback, RealmsServer.WorldType.INSPIRATION, this.inspirations));
      }));
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   private int frame(int p_89393_) {
      return this.width / 2 - 130 + (p_89393_ - 1) * 100;
   }

   public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pPoseStack);
      drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 7, 16777215);
      super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
   }

   void drawFrame(PoseStack pPoseStack, int pX, int pY, Component pText, ResourceLocation p_89359_, boolean p_89360_, boolean p_89361_) {
      RenderSystem.setShaderTexture(0, p_89359_);
      if (p_89360_) {
         RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
      } else {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      GuiComponent.blit(pPoseStack, pX + 2, pY + 14, 0.0F, 0.0F, 56, 56, 56, 56);
      RenderSystem.setShaderTexture(0, SLOT_FRAME_LOCATION);
      if (p_89360_) {
         RenderSystem.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
      } else {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      GuiComponent.blit(pPoseStack, pX, pY + 12, 0.0F, 0.0F, 60, 60, 60, 60);
      int i = p_89360_ ? 10526880 : 16777215;
      drawCenteredString(pPoseStack, this.font, pText, pX + 30, pY, i);
   }

   private void startTask(LongRunningTask pTask) {
      this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, pTask));
   }

   public void switchSlot(Runnable p_89383_) {
      this.startTask(new SwitchSlotTask(this.serverData.id, this.slot, () -> {
         this.minecraft.execute(p_89383_);
      }));
   }

   private void templateSelectionCallback(@Nullable WorldTemplate p_167454_) {
      this.minecraft.setScreen(this);
      if (p_167454_ != null) {
         this.resetWorld(() -> {
            this.startTask(new ResettingTemplateWorldTask(p_167454_, this.serverData.id, this.resetTitle, this.resetWorldRunnable));
         });
      }

   }

   private void generationSelectionCallback(@Nullable WorldGenerationInfo p_167456_) {
      this.minecraft.setScreen(this);
      if (p_167456_ != null) {
         this.resetWorld(() -> {
            this.startTask(new ResettingGeneratedWorldTask(p_167456_, this.serverData.id, this.resetTitle, this.resetWorldRunnable));
         });
      }

   }

   private void resetWorld(Runnable p_167465_) {
      if (this.slot == -1) {
         p_167465_.run();
      } else {
         this.switchSlot(p_167465_);
      }

   }

   @OnlyIn(Dist.CLIENT)
   class FrameButton extends Button {
      private final ResourceLocation image;

      public FrameButton(int pX, int pY, Component pMessage, ResourceLocation pImage, Button.OnPress pOnPress) {
         super(pX, pY, 60, 72, pMessage, pOnPress);
         this.image = pImage;
      }

      public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
         RealmsResetWorldScreen.this.drawFrame(pPoseStack, this.x, this.y, this.getMessage(), this.image, this.isHoveredOrFocused(), this.isMouseOver((double)pMouseX, (double)pMouseY));
      }
   }
}