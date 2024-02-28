package net.minecraft.network.chat;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;

public record MessageSigner(UUID profileId, Instant timeStamp, long salt) {
   public MessageSigner(FriendlyByteBuf pBuffer) {
      this(pBuffer.readUUID(), pBuffer.readInstant(), pBuffer.readLong());
   }

   public static MessageSigner create(UUID pSigner) {
      return new MessageSigner(pSigner, Instant.now(), Crypt.SaltSupplier.getLong());
   }

   public static MessageSigner system() {
      return create(Util.NIL_UUID);
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUUID(this.profileId);
      pBuffer.writeInstant(this.timeStamp);
      pBuffer.writeLong(this.salt);
   }

   public boolean isSystem() {
      return this.profileId.equals(Util.NIL_UUID);
   }
}