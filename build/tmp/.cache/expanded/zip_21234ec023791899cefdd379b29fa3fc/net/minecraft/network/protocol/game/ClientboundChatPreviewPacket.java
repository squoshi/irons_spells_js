package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;

public record ClientboundChatPreviewPacket(int queryId, @Nullable Component preview) implements Packet<ClientGamePacketListener> {
   public ClientboundChatPreviewPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readInt(), pBuffer.readNullable(FriendlyByteBuf::readComponent));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.queryId);
      pBuffer.writeNullable(this.preview, FriendlyByteBuf::writeComponent);
   }

   /**
    * Whether decoding errors will be ignored for this packet.
    */
   public boolean isSkippable() {
      return true;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleChatPreview(this);
   }
}