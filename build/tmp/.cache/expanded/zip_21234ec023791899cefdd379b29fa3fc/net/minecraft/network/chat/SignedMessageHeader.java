package net.minecraft.network.chat;

import java.security.SignatureException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageHeader(@Nullable MessageSignature previousSignature, UUID sender) {
   public SignedMessageHeader(FriendlyByteBuf pBuffer) {
      this(pBuffer.readNullable(MessageSignature::new), pBuffer.readUUID());
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeNullable(this.previousSignature, (p_241348_, p_241289_) -> {
         p_241289_.write(p_241348_);
      });
      pBuffer.writeUUID(this.sender);
   }

   public void updateSignature(SignatureUpdater.Output p_241383_, byte[] pBodyDigest) throws SignatureException {
      if (this.previousSignature != null) {
         p_241383_.update(this.previousSignature.bytes());
      }

      p_241383_.update(UUIDUtil.uuidToByteArray(this.sender));
      p_241383_.update(pBodyDigest);
   }
}