package net.minecraft.client.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccessibilityOptionsScreen extends SimpleOptionsSubScreen {
   private static final String GUIDE_LINK = "https://aka.ms/MinecraftJavaAccessibility";

   private static OptionInstance<?>[] options(Options pOptions) {
      return new OptionInstance[]{pOptions.narrator(), pOptions.showSubtitles(), pOptions.textBackgroundOpacity(), pOptions.backgroundForChatOnly(), pOptions.chatOpacity(), pOptions.chatLineSpacing(), pOptions.chatDelay(), pOptions.autoJump(), pOptions.toggleCrouch(), pOptions.toggleSprint(), pOptions.screenEffectScale(), pOptions.fovEffectScale(), pOptions.darkMojangStudiosBackground(), pOptions.hideLightningFlash(), pOptions.darknessEffectScale()};
   }

   public AccessibilityOptionsScreen(Screen pLastScreen, Options pOptions) {
      super(pLastScreen, pOptions, Component.translatable("options.accessibility.title"), options(pOptions));
   }

   protected void createFooter() {
      this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 27, 150, 20, Component.translatable("options.accessibility.link"), (p_95509_) -> {
         this.minecraft.setScreen(new ConfirmLinkScreen((p_169232_) -> {
            if (p_169232_) {
               Util.getPlatform().openUri("https://aka.ms/MinecraftJavaAccessibility");
            }

            this.minecraft.setScreen(this);
         }, "https://aka.ms/MinecraftJavaAccessibility", true));
      }));
      this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 27, 150, 20, CommonComponents.GUI_DONE, (p_95507_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
   }
}