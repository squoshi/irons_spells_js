package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditServerScreen extends Screen {
   private static final Component NAME_LABEL = Component.translatable("addServer.enterName");
   private static final Component IP_LABEL = Component.translatable("addServer.enterIp");
   private Button addButton;
   private final BooleanConsumer callback;
   private final ServerData serverData;
   private EditBox ipEdit;
   private EditBox nameEdit;
   private final Screen lastScreen;

   public EditServerScreen(Screen pLastScreen, BooleanConsumer pCallback, ServerData pServerData) {
      super(Component.translatable("addServer.title"));
      this.lastScreen = pLastScreen;
      this.callback = pCallback;
      this.serverData = pServerData;
   }

   public void tick() {
      this.nameEdit.tick();
      this.ipEdit.tick();
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 66, 200, 20, Component.translatable("addServer.enterName"));
      this.nameEdit.setFocus(true);
      this.nameEdit.setValue(this.serverData.name);
      this.nameEdit.setResponder((p_169304_) -> {
         this.updateAddButtonStatus();
      });
      this.addWidget(this.nameEdit);
      this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 106, 200, 20, Component.translatable("addServer.enterIp"));
      this.ipEdit.setMaxLength(128);
      this.ipEdit.setValue(this.serverData.ip);
      this.ipEdit.setResponder((p_169302_) -> {
         this.updateAddButtonStatus();
      });
      this.addWidget(this.ipEdit);
      this.addRenderableWidget(CycleButton.builder(ServerData.ServerPackStatus::getName).withValues(ServerData.ServerPackStatus.values()).withInitialValue(this.serverData.getResourcePackStatus()).create(this.width / 2 - 100, this.height / 4 + 72, 200, 20, Component.translatable("addServer.resourcePack"), (p_169299_, p_169300_) -> {
         this.serverData.setResourcePackStatus(p_169300_);
      }));
      this.addButton = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20, Component.translatable("addServer.add"), (p_96030_) -> {
         this.onAdd();
      }));
      this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20, CommonComponents.GUI_CANCEL, (p_169297_) -> {
         this.callback.accept(false);
      }));
      this.updateAddButtonStatus();
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      String s = this.ipEdit.getValue();
      String s1 = this.nameEdit.getValue();
      this.init(pMinecraft, pWidth, pHeight);
      this.ipEdit.setValue(s);
      this.nameEdit.setValue(s1);
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   private void onAdd() {
      this.serverData.name = this.nameEdit.getValue();
      this.serverData.ip = this.ipEdit.getValue();
      this.callback.accept(true);
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   private void updateAddButtonStatus() {
      this.addButton.active = ServerAddress.isValidAddress(this.ipEdit.getValue()) && !this.nameEdit.getValue().isEmpty();
   }

   public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pPoseStack);
      drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 17, 16777215);
      drawString(pPoseStack, this.font, NAME_LABEL, this.width / 2 - 100, 53, 10526880);
      drawString(pPoseStack, this.font, IP_LABEL, this.width / 2 - 100, 94, 10526880);
      this.nameEdit.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
      this.ipEdit.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
      super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
   }
}