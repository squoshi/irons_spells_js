package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface SignedMessageValidator {
   static SignedMessageValidator create(@Nullable ProfilePublicKey pPublicKey, boolean pEnforcesSecureChat) {
      return (SignedMessageValidator)(pPublicKey != null ? new SignedMessageValidator.KeyBased(pPublicKey.createSignatureValidator()) : new SignedMessageValidator.Unsigned(pEnforcesSecureChat));
   }

   SignedMessageValidator.State validateHeader(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, byte[] pBodyDigest);

   SignedMessageValidator.State validateMessage(PlayerChatMessage pMessage);

   public static class KeyBased implements SignedMessageValidator {
      private final SignatureValidator validator;
      @Nullable
      private MessageSignature lastSignature;
      private boolean isChainConsistent = true;

      public KeyBased(SignatureValidator pValidator) {
         this.validator = pValidator;
      }

      private boolean validateChain(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, boolean pEnforcesSecureChat) {
         if (pHeaderSignature.isEmpty()) {
            return false;
         } else if (pEnforcesSecureChat && pHeaderSignature.equals(this.lastSignature)) {
            return true;
         } else {
            return this.lastSignature == null || this.lastSignature.equals(pHeader.previousSignature());
         }
      }

      private boolean validateContents(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, byte[] pBodyDigest, boolean pEnforcesSecureChat) {
         return this.validateChain(pHeader, pHeaderSignature, pEnforcesSecureChat) && pHeaderSignature.verify(this.validator, pHeader, pBodyDigest);
      }

      private SignedMessageValidator.State updateAndValidate(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, byte[] pBodyDigest, boolean pEnforcesSecureChat) {
         this.isChainConsistent = this.isChainConsistent && this.validateContents(pHeader, pHeaderSignature, pBodyDigest, pEnforcesSecureChat);
         if (!this.isChainConsistent) {
            return SignedMessageValidator.State.BROKEN_CHAIN;
         } else {
            this.lastSignature = pHeaderSignature;
            return SignedMessageValidator.State.SECURE;
         }
      }

      public SignedMessageValidator.State validateHeader(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, byte[] pBodyDigest) {
         return this.updateAndValidate(pHeader, pHeaderSignature, pBodyDigest, false);
      }

      public SignedMessageValidator.State validateMessage(PlayerChatMessage pMessage) {
         byte[] abyte = pMessage.signedBody().hash().asBytes();
         return this.updateAndValidate(pMessage.signedHeader(), pMessage.headerSignature(), abyte, true);
      }
   }

   public static enum State {
      SECURE,
      NOT_SECURE,
      BROKEN_CHAIN;
   }

   public static class Unsigned implements SignedMessageValidator {
      private final boolean enforcesSecureChat;

      public Unsigned(boolean pEnforcesSecureChat) {
         this.enforcesSecureChat = pEnforcesSecureChat;
      }

      private SignedMessageValidator.State validate(MessageSignature pMessageSignature) {
         if (!pMessageSignature.isEmpty()) {
            return SignedMessageValidator.State.BROKEN_CHAIN;
         } else {
            return this.enforcesSecureChat ? SignedMessageValidator.State.BROKEN_CHAIN : SignedMessageValidator.State.NOT_SECURE;
         }
      }

      public SignedMessageValidator.State validateHeader(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, byte[] pBodyDigest) {
         return this.validate(pHeaderSignature);
      }

      public SignedMessageValidator.State validateMessage(PlayerChatMessage pMessage) {
         return this.validate(pMessage.headerSignature());
      }
   }
}