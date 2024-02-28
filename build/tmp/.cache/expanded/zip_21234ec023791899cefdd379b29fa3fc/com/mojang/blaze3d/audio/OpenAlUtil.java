package com.mojang.blaze3d.audio;

import com.mojang.logging.LogUtils;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class OpenAlUtil {
   private static final Logger LOGGER = LogUtils.getLogger();

   private static String alErrorToString(int pErrorCode) {
      switch (pErrorCode) {
         case 40961:
            return "Invalid name parameter.";
         case 40962:
            return "Invalid enumerated parameter value.";
         case 40963:
            return "Invalid parameter parameter value.";
         case 40964:
            return "Invalid operation.";
         case 40965:
            return "Unable to allocate memory.";
         default:
            return "An unrecognized error occurred.";
      }
   }

   static boolean checkALError(String pOperationState) {
      int i = AL10.alGetError();
      if (i != 0) {
         LOGGER.error("{}: {}", pOperationState, alErrorToString(i));
         return true;
      } else {
         return false;
      }
   }

   private static String alcErrorToString(int pErrorCode) {
      switch (pErrorCode) {
         case 40961:
            return "Invalid device.";
         case 40962:
            return "Invalid context.";
         case 40963:
            return "Illegal enum.";
         case 40964:
            return "Invalid value.";
         case 40965:
            return "Unable to allocate memory.";
         default:
            return "An unrecognized error occurred.";
      }
   }

   static boolean checkALCError(long pDeviceHandle, String pOperationState) {
      int i = ALC10.alcGetError(pDeviceHandle);
      if (i != 0) {
         LOGGER.error("{}{}: {}", pOperationState, pDeviceHandle, alcErrorToString(i));
         return true;
      } else {
         return false;
      }
   }

   static int audioFormatToOpenAl(AudioFormat pFormat) {
      AudioFormat.Encoding encoding = pFormat.getEncoding();
      int i = pFormat.getChannels();
      int j = pFormat.getSampleSizeInBits();
      if (encoding.equals(Encoding.PCM_UNSIGNED) || encoding.equals(Encoding.PCM_SIGNED)) {
         if (i == 1) {
            if (j == 8) {
               return 4352;
            }

            if (j == 16) {
               return 4353;
            }
         } else if (i == 2) {
            if (j == 8) {
               return 4354;
            }

            if (j == 16) {
               return 4355;
            }
         }
      }

      throw new IllegalArgumentException("Invalid audio format: " + pFormat);
   }
}