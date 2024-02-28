package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.StringUtil;

public record ServerboundChatPreviewPacket(int queryId, String query) implements Packet<ServerGamePacketListener> {
   public ServerboundChatPreviewPacket {
      query = StringUtil.trimChatMessage(query);
   }

   public ServerboundChatPreviewPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readInt(), pBuffer.readUtf(256));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.queryId);
      pBuffer.writeUtf(this.query, 256);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleChatPreview(this);
   }
}