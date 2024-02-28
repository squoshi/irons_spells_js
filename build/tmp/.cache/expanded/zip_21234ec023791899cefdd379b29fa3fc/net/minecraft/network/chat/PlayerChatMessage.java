package net.minecraft.network.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(SignedMessageHeader signedHeader, MessageSignature headerSignature, SignedMessageBody signedBody, Optional<Component> unsignedContent, FilterMask filterMask) {
   public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
   public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

   public PlayerChatMessage(FriendlyByteBuf pBuffer) {
      this(new SignedMessageHeader(pBuffer), new MessageSignature(pBuffer), new SignedMessageBody(pBuffer), pBuffer.readOptional(FriendlyByteBuf::readComponent), FilterMask.read(pBuffer));
   }

   public static PlayerChatMessage system(ChatMessageContent pContent) {
      return unsigned(MessageSigner.system(), pContent);
   }

   public static PlayerChatMessage unsigned(MessageSigner pSigner, ChatMessageContent pContent) {
      SignedMessageBody signedmessagebody = new SignedMessageBody(pContent, pSigner.timeStamp(), pSigner.salt(), LastSeenMessages.EMPTY);
      SignedMessageHeader signedmessageheader = new SignedMessageHeader((MessageSignature)null, pSigner.profileId());
      return new PlayerChatMessage(signedmessageheader, MessageSignature.EMPTY, signedmessagebody, Optional.empty(), FilterMask.PASS_THROUGH);
   }

   public void write(FriendlyByteBuf pBuffer) {
      this.signedHeader.write(pBuffer);
      this.headerSignature.write(pBuffer);
      this.signedBody.write(pBuffer);
      pBuffer.writeOptional(this.unsignedContent, FriendlyByteBuf::writeComponent);
      FilterMask.write(pBuffer, this.filterMask);
   }

   public PlayerChatMessage withUnsignedContent(Component pMessage) {
      Optional<Component> optional = !this.signedContent().decorated().equals(pMessage) ? Optional.of(pMessage) : Optional.empty();
      return new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, optional, this.filterMask);
   }

   public PlayerChatMessage removeUnsignedContent() {
      return this.unsignedContent.isPresent() ? new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, Optional.empty(), this.filterMask) : this;
   }

   public PlayerChatMessage filter(FilterMask pMask) {
      return this.filterMask.equals(pMask) ? this : new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, this.unsignedContent, pMask);
   }

   public PlayerChatMessage filter(boolean pShouldFilter) {
      return this.filter(pShouldFilter ? this.filterMask : FilterMask.PASS_THROUGH);
   }

   public boolean verify(SignatureValidator pValidator) {
      return this.headerSignature.verify(pValidator, this.signedHeader, this.signedBody);
   }

   public boolean verify(ProfilePublicKey pPublicKey) {
      SignatureValidator signaturevalidator = pPublicKey.createSignatureValidator();
      return this.verify(signaturevalidator);
   }

   public boolean verify(ChatSender pSender) {
      ProfilePublicKey profilepublickey = pSender.profilePublicKey();
      return profilepublickey != null && this.verify(profilepublickey);
   }

   public ChatMessageContent signedContent() {
      return this.signedBody.content();
   }

   public Component serverContent() {
      return this.unsignedContent().orElse(this.signedContent().decorated());
   }

   public Instant timeStamp() {
      return this.signedBody.timeStamp();
   }

   public long salt() {
      return this.signedBody.salt();
   }

   public boolean hasExpiredServer(Instant pTimestamp) {
      return pTimestamp.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_SERVER));
   }

   public boolean hasExpiredClient(Instant pTimestamp) {
      return pTimestamp.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_CLIENT));
   }

   public MessageSigner signer() {
      return new MessageSigner(this.signedHeader.sender(), this.timeStamp(), this.salt());
   }

   @Nullable
   public LastSeenMessages.Entry toLastSeenEntry() {
      MessageSigner messagesigner = this.signer();
      return !this.headerSignature.isEmpty() && !messagesigner.isSystem() ? new LastSeenMessages.Entry(messagesigner.profileId(), this.headerSignature) : null;
   }

   public boolean hasSignatureFrom(UUID pUuid) {
      return !this.headerSignature.isEmpty() && this.signedHeader.sender().equals(pUuid);
   }

   public boolean isFullyFiltered() {
      return this.filterMask.isFullyFiltered();
   }
}