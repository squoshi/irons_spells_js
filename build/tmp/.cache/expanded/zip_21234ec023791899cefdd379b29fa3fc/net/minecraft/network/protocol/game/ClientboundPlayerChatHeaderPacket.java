package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerChatHeaderPacket(SignedMessageHeader header, MessageSignature headerSignature, byte[] bodyDigest) implements Packet<ClientGamePacketListener> {
   public ClientboundPlayerChatHeaderPacket(PlayerChatMessage pMessage) {
      this(pMessage.signedHeader(), pMessage.headerSignature(), pMessage.signedBody().hash().asBytes());
   }

   public ClientboundPlayerChatHeaderPacket(FriendlyByteBuf pBuffer) {
      this(new SignedMessageHeader(pBuffer), new MessageSignature(pBuffer), pBuffer.readByteArray());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      this.header.write(pBuffer);
      this.headerSignature.write(pBuffer);
      pBuffer.writeByteArray(this.bodyDigest);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlayerChatHeader(this);
   }
}