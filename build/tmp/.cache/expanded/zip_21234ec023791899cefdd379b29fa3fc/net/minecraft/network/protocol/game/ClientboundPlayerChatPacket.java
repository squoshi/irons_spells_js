package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerChatPacket(PlayerChatMessage message, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener> {
   public ClientboundPlayerChatPacket(FriendlyByteBuf pBuffer) {
      this(new PlayerChatMessage(pBuffer), new ChatType.BoundNetwork(pBuffer));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      this.message.write(pBuffer);
      this.chatType.write(pBuffer);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlayerChat(this);
   }

   /**
    * Whether decoding errors will be ignored for this packet.
    */
   public boolean isSkippable() {
      return true;
   }

   public Optional<ChatType.Bound> resolveChatType(RegistryAccess pRegistryAccess) {
      return this.chatType.resolve(pRegistryAccess);
   }
}