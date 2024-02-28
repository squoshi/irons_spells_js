package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(byte[] bytes) {
   public static final MessageSignature EMPTY = new MessageSignature(ByteArrays.EMPTY_ARRAY);

   public MessageSignature(FriendlyByteBuf pBuffer) {
      this(pBuffer.readByteArray());
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByteArray(this.bytes);
   }

   public boolean verify(SignatureValidator pValidator, SignedMessageHeader pHeader, SignedMessageBody pHashBytes) {
      if (!this.isEmpty()) {
         byte[] abyte = pHashBytes.hash().asBytes();
         return pValidator.validate((p_241242_) -> {
            pHeader.updateSignature(p_241242_, abyte);
         }, this.bytes);
      } else {
         return false;
      }
   }

   public boolean verify(SignatureValidator pValidator, SignedMessageHeader pHeader, byte[] pHashBytes) {
      return !this.isEmpty() ? pValidator.validate((p_241245_) -> {
         pHeader.updateSignature(p_241245_, pHashBytes);
      }, this.bytes) : false;
   }

   public boolean isEmpty() {
      return this.bytes.length == 0;
   }

   @Nullable
   public ByteBuffer asByteBuffer() {
      return !this.isEmpty() ? ByteBuffer.wrap(this.bytes) : null;
   }

   public boolean equals(Object pOther) {
      if (this != pOther) {
         if (pOther instanceof MessageSignature) {
            MessageSignature messagesignature = (MessageSignature)pOther;
            if (Arrays.equals(this.bytes, messagesignature.bytes)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   public String toString() {
      return !this.isEmpty() ? Base64.getEncoder().encodeToString(this.bytes) : "empty";
   }
}