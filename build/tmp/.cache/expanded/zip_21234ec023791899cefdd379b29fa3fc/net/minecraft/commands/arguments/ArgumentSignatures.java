package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PreviewableCommand;

public record ArgumentSignatures(List<ArgumentSignatures.Entry> entries) {
   public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
   private static final int MAX_ARGUMENT_COUNT = 8;
   private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

   public ArgumentSignatures(FriendlyByteBuf pBuffer) {
      this(pBuffer.<ArgumentSignatures.Entry, List<ArgumentSignatures.Entry>>readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), ArgumentSignatures.Entry::new));
   }

   public MessageSignature get(String pKey) {
      for(ArgumentSignatures.Entry argumentsignatures$entry : this.entries) {
         if (argumentsignatures$entry.name.equals(pKey)) {
            return argumentsignatures$entry.signature;
         }
      }

      return MessageSignature.EMPTY;
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeCollection(this.entries, (p_241214_, p_241215_) -> {
         p_241215_.write(p_241214_);
      });
   }

   public static boolean hasSignableArguments(PreviewableCommand<?> pCommand) {
      return pCommand.arguments().stream().anyMatch((p_242699_) -> {
         return p_242699_.previewType() instanceof SignedArgument;
      });
   }

   public static ArgumentSignatures signCommand(PreviewableCommand<?> pCommand, ArgumentSignatures.Signer pSigner) {
      List<ArgumentSignatures.Entry> list = collectPlainSignableArguments(pCommand).stream().map((p_242081_) -> {
         MessageSignature messagesignature = pSigner.sign(p_242081_.getFirst(), p_242081_.getSecond());
         return new ArgumentSignatures.Entry(p_242081_.getFirst(), messagesignature);
      }).toList();
      return new ArgumentSignatures(list);
   }

   public static List<Pair<String, String>> collectPlainSignableArguments(PreviewableCommand<?> pCommand) {
      List<Pair<String, String>> list = new ArrayList<>();

      for(PreviewableCommand.Argument<?> argument : pCommand.arguments()) {
         PreviewedArgument $$4 = argument.previewType();
         if ($$4 instanceof SignedArgument<?> signedargument) {
            String s = getSignableText(signedargument, argument.parsedValue());
            list.add(Pair.of(argument.name(), s));
         }
      }

      return list;
   }

   private static <T> String getSignableText(SignedArgument<T> pSignedArgument, ParsedArgument<?, ?> pParsedArgument) {
      return pSignedArgument.getSignableText((T)pParsedArgument.getResult());
   }

   public static record Entry(String name, MessageSignature signature) {
      public Entry(FriendlyByteBuf pBuffer) {
         this(pBuffer.readUtf(16), new MessageSignature(pBuffer));
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeUtf(this.name, 16);
         this.signature.write(pBuffer);
      }
   }

   @FunctionalInterface
   public interface Signer {
      MessageSignature sign(String pArgumentName, String pArgumentText);
   }
}