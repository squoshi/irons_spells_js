package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundSetDisplayChatPreviewPacket(boolean enabled) implements Packet<ClientGamePacketListener> {
   public ClientboundSetDisplayChatPreviewPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readBoolean());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBoolean(this.enabled);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetDisplayChatPreview(this);
   }
}