package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundServerDataPacket implements Packet<ClientGamePacketListener> {
   private final Optional<Component> motd;
   private final Optional<String> iconBase64;
   private final boolean previewsChat;
   private final boolean enforcesSecureChat;

   public ClientboundServerDataPacket(@Nullable Component pMotd, @Nullable String pIconBase64, boolean pPreviewsChat, boolean pEnforcesSecureChat) {
      this.motd = Optional.ofNullable(pMotd);
      this.iconBase64 = Optional.ofNullable(pIconBase64);
      this.previewsChat = pPreviewsChat;
      this.enforcesSecureChat = pEnforcesSecureChat;
   }

   public ClientboundServerDataPacket(FriendlyByteBuf pBuffer) {
      this.motd = pBuffer.readOptional(FriendlyByteBuf::readComponent);
      this.iconBase64 = pBuffer.readOptional(FriendlyByteBuf::readUtf);
      this.previewsChat = pBuffer.readBoolean();
      this.enforcesSecureChat = pBuffer.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeOptional(this.motd, FriendlyByteBuf::writeComponent);
      pBuffer.writeOptional(this.iconBase64, FriendlyByteBuf::writeUtf);
      pBuffer.writeBoolean(this.previewsChat);
      pBuffer.writeBoolean(this.enforcesSecureChat);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleServerData(this);
   }

   public Optional<Component> getMotd() {
      return this.motd;
   }

   public Optional<String> getIconBase64() {
      return this.iconBase64;
   }

   public boolean previewsChat() {
      return this.previewsChat;
   }

   public boolean enforcesSecureChat() {
      return this.enforcesSecureChat;
   }
}