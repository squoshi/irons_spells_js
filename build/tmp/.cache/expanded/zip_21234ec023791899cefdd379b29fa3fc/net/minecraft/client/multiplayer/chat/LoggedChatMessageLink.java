package net.minecraft.client.multiplayer.chat;

import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LoggedChatMessageLink extends LoggedChatEvent {
   static LoggedChatMessageLink.Header header(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, byte[] pBodyDigest) {
      return new LoggedChatMessageLink.Header(pHeader, pHeaderSignature, pBodyDigest);
   }

   SignedMessageHeader header();

   MessageSignature headerSignature();

   byte[] bodyDigest();

   @OnlyIn(Dist.CLIENT)
   public static record Header(SignedMessageHeader header, MessageSignature headerSignature, byte[] bodyDigest) implements LoggedChatMessageLink {
      public SignedMessageHeader header() {
         return this.header;
      }

      public MessageSignature headerSignature() {
         return this.headerSignature;
      }

      public byte[] bodyDigest() {
         return this.bodyDigest;
      }
   }
}