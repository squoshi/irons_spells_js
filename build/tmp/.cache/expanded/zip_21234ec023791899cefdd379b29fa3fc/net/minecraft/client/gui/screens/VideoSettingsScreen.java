package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
   private static final Component FABULOUS = Component.translatable("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC);
   private static final Component WARNING_MESSAGE = Component.translatable("options.graphics.warning.message", FABULOUS, FABULOUS);
   private static final Component WARNING_TITLE = Component.translatable("options.graphics.warning.title").withStyle(ChatFormatting.RED);
   private static final Component BUTTON_ACCEPT = Component.translatable("options.graphics.warning.accept");
   private static final Component BUTTON_CANCEL = Component.translatable("options.graphics.warning.cancel");
   private OptionsList list;
   private final GpuWarnlistManager gpuWarnlistManager;
   private final int oldMipmaps;

   private static OptionInstance<?>[] options(Options pOptions) {
      return new OptionInstance[]{pOptions.graphicsMode(), pOptions.renderDistance(), pOptions.prioritizeChunkUpdates(), pOptions.simulationDistance(), pOptions.ambientOcclusion(), pOptions.framerateLimit(), pOptions.enableVsync(), pOptions.bobView(), pOptions.guiScale(), pOptions.attackIndicator(), pOptions.gamma(), pOptions.cloudStatus(), pOptions.fullscreen(), pOptions.particles(), pOptions.mipmapLevels(), pOptions.entityShadows(), pOptions.screenEffectScale(), pOptions.entityDistanceScaling(), pOptions.fovEffectScale(), pOptions.showAutosaveIndicator()};
   }

   public VideoSettingsScreen(Screen pLastScreen, Options pOptions) {
      super(pLastScreen, pOptions, Component.translatable("options.videoTitle"));
      this.gpuWarnlistManager = pLastScreen.minecraft.getGpuWarnlistManager();
      this.gpuWarnlistManager.resetWarnings();
      if (pOptions.graphicsMode().get() == GraphicsStatus.FABULOUS) {
         this.gpuWarnlistManager.dismissWarning();
      }

      this.oldMipmaps = pOptions.mipmapLevels().get();
   }

   protected void init() {
      this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
      int i = -1;
      Window window = this.minecraft.getWindow();
      Monitor monitor = window.findBestMonitor();
      int j;
      if (monitor == null) {
         j = -1;
      } else {
         Optional<VideoMode> optional = window.getPreferredFullscreenVideoMode();
         j = optional.map(monitor::getVideoModeIndex).orElse(-1);
      }

      OptionInstance<Integer> optioninstance = new OptionInstance<>("options.fullscreen.resolution", OptionInstance.noTooltip(), (p_232806_, p_232807_) -> {
         if (monitor == null) {
            return Component.translatable("options.fullscreen.unavailable");
         } else {
            return p_232807_ == -1 ? Options.genericValueLabel(p_232806_, Component.translatable("options.fullscreen.current")) : Options.genericValueLabel(p_232806_, Component.literal(monitor.getMode(p_232807_).toString()));
         }
      }, new OptionInstance.IntRange(-1, monitor != null ? monitor.getModeCount() - 1 : -1), j, (p_232803_) -> {
         if (monitor != null) {
            window.setPreferredFullscreenVideoMode(p_232803_ == -1 ? Optional.empty() : Optional.of(monitor.getMode(p_232803_)));
         }
      });
      this.list.addBig(optioninstance);
      this.list.addBig(this.options.biomeBlendRadius());
      this.list.addSmall(options(this.options));
      this.addWidget(this.list);
      this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (p_232810_) -> {
         this.minecraft.options.save();
         window.changeFullscreenVideoMode();
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public void removed() {
      if (this.options.mipmapLevels().get() != this.oldMipmaps) {
         this.minecraft.updateMaxMipLevel(this.options.mipmapLevels().get());
         this.minecraft.delayTextureReload();
      }

      super.removed();
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      int i = this.options.guiScale().get();
      if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
         if (this.options.guiScale().get() != i) {
            this.minecraft.resizeDisplay();
         }

         if (this.gpuWarnlistManager.isShowingWarning()) {
            List<Component> list = Lists.newArrayList(WARNING_MESSAGE, CommonComponents.NEW_LINE);
            String s = this.gpuWarnlistManager.getRendererWarnings();
            if (s != null) {
               list.add(CommonComponents.NEW_LINE);
               list.add(Component.translatable("options.graphics.warning.renderer", s).withStyle(ChatFormatting.GRAY));
            }

            String s1 = this.gpuWarnlistManager.getVendorWarnings();
            if (s1 != null) {
               list.add(CommonComponents.NEW_LINE);
               list.add(Component.translatable("options.graphics.warning.vendor", s1).withStyle(ChatFormatting.GRAY));
            }

            String s2 = this.gpuWarnlistManager.getVersionWarnings();
            if (s2 != null) {
               list.add(CommonComponents.NEW_LINE);
               list.add(Component.translatable("options.graphics.warning.version", s2).withStyle(ChatFormatting.GRAY));
            }

            this.minecraft.setScreen(new PopupScreen(WARNING_TITLE, list, ImmutableList.of(new PopupScreen.ButtonOption(BUTTON_ACCEPT, (p_232816_) -> {
               this.options.graphicsMode().set(GraphicsStatus.FABULOUS);
               Minecraft.getInstance().levelRenderer.allChanged();
               this.gpuWarnlistManager.dismissWarning();
               this.minecraft.setScreen(this);
            }), new PopupScreen.ButtonOption(BUTTON_CANCEL, (p_232814_) -> {
               this.gpuWarnlistManager.dismissWarningAndSkipFabulous();
               this.minecraft.setScreen(this);
            }))));
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      int i = this.options.guiScale().get();
      if (super.mouseReleased(pMouseX, pMouseY, pButton)) {
         return true;
      } else if (this.list.mouseReleased(pMouseX, pMouseY, pButton)) {
         if (this.options.guiScale().get() != i) {
            this.minecraft.resizeDisplay();
         }

         return true;
      } else {
         return false;
      }
   }

   public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pPoseStack);
      this.list.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
      drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 5, 16777215);
      super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
      List<FormattedCharSequence> list = tooltipAt(this.list, pMouseX, pMouseY);
      this.renderTooltip(pPoseStack, list, pMouseX, pMouseY);
   }
}