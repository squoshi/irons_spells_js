package net.minecraft.network.chat;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.Signer;

public class SignedMessageChain {
   @Nullable
   private MessageSignature previousSignature;

   private SignedMessageChain.Link pack(Signer p_242326_, MessageSigner p_242397_, ChatMessageContent p_242431_, LastSeenMessages p_242419_) {
      MessageSignature messagesignature = pack(p_242326_, p_242397_, this.previousSignature, p_242431_, p_242419_);
      this.previousSignature = messagesignature;
      return new SignedMessageChain.Link(messagesignature);
   }

   private static MessageSignature pack(Signer pSigner, MessageSigner pMessageSigner, @Nullable MessageSignature pMessageSignature, ChatMessageContent pMessageContent, LastSeenMessages pLastSeenMessages) {
      SignedMessageHeader signedmessageheader = new SignedMessageHeader(pMessageSignature, pMessageSigner.profileId());
      SignedMessageBody signedmessagebody = new SignedMessageBody(pMessageContent, pMessageSigner.timeStamp(), pMessageSigner.salt(), pLastSeenMessages);
      byte[] abyte = signedmessagebody.hash().asBytes();
      return new MessageSignature(pSigner.sign((p_241520_) -> {
         signedmessageheader.updateSignature(p_241520_, abyte);
      }));
   }

   private PlayerChatMessage unpack(SignedMessageChain.Link p_242429_, MessageSigner p_242380_, ChatMessageContent p_242233_, LastSeenMessages p_242352_) {
      PlayerChatMessage playerchatmessage = unpack(p_242429_, this.previousSignature, p_242380_, p_242233_, p_242352_);
      this.previousSignature = p_242429_.signature;
      return playerchatmessage;
   }

   private static PlayerChatMessage unpack(SignedMessageChain.Link p_242261_, @Nullable MessageSignature pMessageSignature, MessageSigner pMessageSigner, ChatMessageContent pMessageContent, LastSeenMessages pLastSeenMessages) {
      SignedMessageHeader signedmessageheader = new SignedMessageHeader(pMessageSignature, pMessageSigner.profileId());
      SignedMessageBody signedmessagebody = new SignedMessageBody(pMessageContent, pMessageSigner.timeStamp(), pMessageSigner.salt(), pLastSeenMessages);
      return new PlayerChatMessage(signedmessageheader, p_242261_.signature, signedmessagebody, Optional.empty(), FilterMask.PASS_THROUGH);
   }

   public SignedMessageChain.Decoder decoder() {
      return this::unpack;
   }

   public SignedMessageChain.Encoder encoder() {
      return this::pack;
   }

   @FunctionalInterface
   public interface Decoder {
      SignedMessageChain.Decoder UNSIGNED = (p_243332_, p_243220_, p_243212_, p_243282_) -> {
         return PlayerChatMessage.unsigned(p_243220_, p_243212_);
      };

      PlayerChatMessage unpack(SignedMessageChain.Link p_241336_, MessageSigner pMessageSigner, ChatMessageContent pMessageContent, LastSeenMessages pLastSeenMessages);
   }

   @FunctionalInterface
   public interface Encoder {
      SignedMessageChain.Link pack(Signer pSigner, MessageSigner pMessageSigner, ChatMessageContent pMessageContent, LastSeenMessages pLastSeenMessages);
   }

   public static record Link(MessageSignature signature) {
   }
}