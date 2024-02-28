package net.minecraft.network.chat;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record LastSeenMessages(List<LastSeenMessages.Entry> entries) {
   public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
   public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 5;

   public LastSeenMessages(FriendlyByteBuf pBuffer) {
      this(pBuffer.<LastSeenMessages.Entry, List<LastSeenMessages.Entry>>readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 5), LastSeenMessages.Entry::new));
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeCollection(this.entries, (p_242176_, p_242457_) -> {
         p_242457_.write(p_242176_);
      });
   }

   public void updateHash(DataOutput pOutput) throws IOException {
      for(LastSeenMessages.Entry lastseenmessages$entry : this.entries) {
         UUID uuid = lastseenmessages$entry.profileId();
         MessageSignature messagesignature = lastseenmessages$entry.lastSignature();
         pOutput.writeByte(70);
         pOutput.writeLong(uuid.getMostSignificantBits());
         pOutput.writeLong(uuid.getLeastSignificantBits());
         pOutput.write(messagesignature.bytes());
      }

   }

   public static record Entry(UUID profileId, MessageSignature lastSignature) {
      public Entry(FriendlyByteBuf pBuffer) {
         this(pBuffer.readUUID(), new MessageSignature(pBuffer));
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeUUID(this.profileId);
         this.lastSignature.write(pBuffer);
      }
   }

   public static record Update(LastSeenMessages lastSeen, Optional<LastSeenMessages.Entry> lastReceived) {
      public Update(FriendlyByteBuf pBuffer) {
         this(new LastSeenMessages(pBuffer), pBuffer.readOptional(LastSeenMessages.Entry::new));
      }

      public void write(FriendlyByteBuf pBuffer) {
         this.lastSeen.write(pBuffer);
         pBuffer.writeOptional(this.lastReceived, (p_242427_, p_242226_) -> {
            p_242226_.write(p_242427_);
         });
      }
   }
}