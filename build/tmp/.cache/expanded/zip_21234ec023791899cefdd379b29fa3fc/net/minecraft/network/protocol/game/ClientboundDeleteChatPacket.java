package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;

public record ClientboundDeleteChatPacket(MessageSignature messageSignature) implements Packet<ClientGamePacketListener> {
   public ClientboundDeleteChatPacket(FriendlyByteBuf pBuffer) {
      this(new MessageSignature(pBuffer));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      this.messageSignature.write(pBuffer);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleDeleteChat(this);
   }
}