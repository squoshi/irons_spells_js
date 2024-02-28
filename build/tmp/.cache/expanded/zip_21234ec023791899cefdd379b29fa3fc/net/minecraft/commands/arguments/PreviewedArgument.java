package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public interface PreviewedArgument<T> extends ArgumentType<T> {
   @Nullable
   default CompletableFuture<Component> resolvePreview(CommandSourceStack pSource, ParsedArgument<CommandSourceStack, ?> pParsedArgument) throws CommandSyntaxException {
      return this.getValueType().isInstance(pParsedArgument.getResult()) ? this.resolvePreview(pSource, this.getValueType().cast(pParsedArgument.getResult())) : null;
   }

   CompletableFuture<Component> resolvePreview(CommandSourceStack pSource, T p_232865_) throws CommandSyntaxException;

   Class<T> getValueType();
}