package net.minecraft.util;

import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.logging.LogUtils;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import org.slf4j.Logger;

public interface SignatureValidator {
   SignatureValidator NO_VALIDATION = (p_216352_, p_216353_) -> {
      return true;
   };
   Logger LOGGER = LogUtils.getLogger();

   boolean validate(SignatureUpdater pUpdater, byte[] p_216380_);

   default boolean validate(byte[] p_216376_, byte[] p_216377_) {
      return this.validate((p_216374_) -> {
         p_216374_.update(p_216376_);
      }, p_216377_);
   }

   private static boolean verifySignature(SignatureUpdater pUpdater, byte[] p_216356_, Signature pSignature) throws SignatureException {
      pUpdater.update(pSignature::update);
      return pSignature.verify(p_216356_);
   }

   static SignatureValidator from(PublicKey pPublicKey, String pAlgorithm) {
      return (p_216367_, p_216368_) -> {
         try {
            Signature signature = Signature.getInstance(pAlgorithm);
            signature.initVerify(pPublicKey);
            return verifySignature(p_216367_, p_216368_, signature);
         } catch (Exception exception) {
            LOGGER.error("Failed to verify signature", (Throwable)exception);
            return false;
         }
      };
   }

   static SignatureValidator from(ServicesKeyInfo pServicesKeyInfo) {
      return (p_216362_, p_216363_) -> {
         Signature signature = pServicesKeyInfo.signature();

         try {
            return verifySignature(p_216362_, p_216363_, signature);
         } catch (SignatureException signatureexception) {
            LOGGER.error("Failed to verify Services signature", (Throwable)signatureexception);
            return false;
         }
      };
   }
}