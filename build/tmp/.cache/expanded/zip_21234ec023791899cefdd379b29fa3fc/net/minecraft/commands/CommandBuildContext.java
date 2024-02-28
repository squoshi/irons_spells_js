package net.minecraft.commands;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public final class CommandBuildContext {
   private final RegistryAccess registryAccess;
   CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy = CommandBuildContext.MissingTagAccessPolicy.FAIL;

   public CommandBuildContext(RegistryAccess pRegistryAccess) {
      this.registryAccess = pRegistryAccess;
   }

   public void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy pMissingTagAccessPolicy) {
      this.missingTagAccessPolicy = pMissingTagAccessPolicy;
   }

   public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> pRegistryResourceKey) {
      return new HolderLookup.RegistryLookup<T>(this.registryAccess.registryOrThrow(pRegistryResourceKey)) {
         public Optional<? extends HolderSet<T>> get(TagKey<T> p_227142_) {
            Optional optional1;
            switch (CommandBuildContext.this.missingTagAccessPolicy) {
               case FAIL:
                  optional1 = this.registry.getTag(p_227142_);
                  break;
               case CREATE_NEW:
                  optional1 = Optional.of(this.registry.getOrCreateTag(p_227142_));
                  break;
               case RETURN_EMPTY:
                  Optional<? extends HolderSet<T>> optional = this.registry.getTag(p_227142_);
                  optional1 = Optional.of(optional.isPresent() ? optional.get() : HolderSet.direct());
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            return optional1;
         }
      };
   }

   public static enum MissingTagAccessPolicy {
      CREATE_NEW,
      RETURN_EMPTY,
      FAIL;
   }
}