package net.minecraft.network.protocol.login;

import java.security.PublicKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ClientboundHelloPacket implements Packet<ClientLoginPacketListener> {
   private final String serverId;
   private final byte[] publicKey;
   private final byte[] nonce;

   public ClientboundHelloPacket(String pServerId, byte[] pPublicKey, byte[] pNonce) {
      this.serverId = pServerId;
      this.publicKey = pPublicKey;
      this.nonce = pNonce;
   }

   public ClientboundHelloPacket(FriendlyByteBuf pBuffer) {
      this.serverId = pBuffer.readUtf(20);
      this.publicKey = pBuffer.readByteArray();
      this.nonce = pBuffer.readByteArray();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUtf(this.serverId);
      pBuffer.writeByteArray(this.publicKey);
      pBuffer.writeByteArray(this.nonce);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientLoginPacketListener pHandler) {
      pHandler.handleHello(this);
   }

   public String getServerId() {
      return this.serverId;
   }

   public PublicKey getPublicKey() throws CryptException {
      return Crypt.byteToPublicKey(this.publicKey);
   }

   public byte[] getNonce() {
      return this.nonce;
   }
}