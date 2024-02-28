package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatAckPacket(LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener> {
   public ServerboundChatAckPacket(FriendlyByteBuf pBuffer) {
      this(new LastSeenMessages.Update(pBuffer));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      this.lastSeenMessages.write(pBuffer);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleChatAck(this);
   }
}