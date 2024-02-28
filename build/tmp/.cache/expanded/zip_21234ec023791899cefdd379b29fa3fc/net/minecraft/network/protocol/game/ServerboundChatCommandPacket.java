package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.StringUtil;

public record ServerboundChatCommandPacket(String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, boolean signedPreview, LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener> {
   public ServerboundChatCommandPacket {
      command = StringUtil.trimChatMessage(command);
   }

   public ServerboundChatCommandPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readUtf(256), pBuffer.readInstant(), pBuffer.readLong(), new ArgumentSignatures(pBuffer), pBuffer.readBoolean(), new LastSeenMessages.Update(pBuffer));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUtf(this.command, 256);
      pBuffer.writeInstant(this.timeStamp);
      pBuffer.writeLong(this.salt);
      this.argumentSignatures.write(pBuffer);
      pBuffer.writeBoolean(this.signedPreview);
      this.lastSeenMessages.write(pBuffer);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleChatCommand(this);
   }
}