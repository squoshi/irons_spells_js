package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

/**
 * A codec that wraps a single element, or "file", within a registry. Possibly allows inline definitions, and always
 * falls back to the element codec (and thus writing the registry element inline) if it fails to decode from the
 * registry.
 */
public final class RegistryFileCodec<E> implements Codec<Holder<E>> {
   private final ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<E> elementCodec;
   private final boolean allowInline;

   /**
    * Creates a codec for a single registry element, which is held as an un-resolved {@code Supplier<E>}. Both inline
    * definitions of the object, and references to an existing registry element id are allowed.
    * @param pRegistryKey The registry which elements may belong to.
    * @param pElementCodec The codec used to decode either inline definitions, or elements before entering them into the
    * registry.
    */
   public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> pRegistryKey, Codec<E> pElementCodec) {
      return create(pRegistryKey, pElementCodec, true);
   }

   public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> pRegistryKey, Codec<E> pElementCodec, boolean pAllowInline) {
      return new RegistryFileCodec<>(pRegistryKey, pElementCodec, pAllowInline);
   }

   private RegistryFileCodec(ResourceKey<? extends Registry<E>> pRegistryKey, Codec<E> pElementCodec, boolean pAllowInline) {
      this.registryKey = pRegistryKey;
      this.elementCodec = pElementCodec;
      this.allowInline = pAllowInline;
   }

   public <T> DataResult<T> encode(Holder<E> pInput, DynamicOps<T> pOps, T pPrefix) {
      if (pOps instanceof RegistryOps<?> registryops) {
         Optional<? extends Registry<E>> optional = registryops.registry(this.registryKey);
         if (optional.isPresent()) {
            if (!pInput.isValidInRegistry(optional.get())) {
               return DataResult.error("Element " + pInput + " is not valid in current registry set");
            }

            return pInput.unwrap().map((p_206714_) -> {
               return ResourceLocation.CODEC.encode(p_206714_.location(), pOps, pPrefix);
            }, (p_206710_) -> {
               return this.elementCodec.encode(p_206710_, pOps, pPrefix);
            });
         }
      }

      return this.elementCodec.encode(pInput.value(), pOps, pPrefix);
   }

   public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> pOps, T pInput) {
      if (pOps instanceof RegistryOps<?> registryops) {
         Optional<? extends Registry<E>> optional = registryops.registry(this.registryKey);
         if (optional.isEmpty()) {
            return DataResult.error("Registry does not exist: " + this.registryKey);
         } else {
            Registry<E> registry = optional.get();
            DataResult<Pair<ResourceLocation, T>> dataresult = ResourceLocation.CODEC.decode(pOps, pInput);
            if (dataresult.result().isEmpty()) {
               return !this.allowInline ? DataResult.error("Inline definitions not allowed here") : this.elementCodec.decode(pOps, pInput).map((p_206720_) -> {
                  return p_206720_.mapFirst(Holder::direct);
               });
            } else {
               Pair<ResourceLocation, T> pair = dataresult.result().get();
               ResourceKey<E> resourcekey = ResourceKey.create(this.registryKey, pair.getFirst());
               Optional<RegistryLoader.Bound> optional1 = registryops.registryLoader();
               if (optional1.isPresent()) {
                  return optional1.get().overrideElementFromResources(this.registryKey, this.elementCodec, resourcekey, registryops.getAsJson()).map((p_206706_) -> {
                     return Pair.of(p_206706_, pair.getSecond());
                  });
               } else {
                  DataResult<Holder<E>> dataresult1 = registry.getOrCreateHolder(resourcekey);
                  return dataresult1.map((p_214215_) -> {
                     return Pair.of(p_214215_, pair.getSecond());
                  }).setLifecycle(Lifecycle.stable());
               }
            }
         }
      } else {
         return this.elementCodec.decode(pOps, pInput).map((p_214212_) -> {
            return p_214212_.mapFirst(Holder::direct);
         });
      }
   }

   public String toString() {
      return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
   }
}