package net.minecraft.core;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface HolderLookup<T> {
   Optional<Holder<T>> get(ResourceKey<T> pKey);

   Stream<ResourceKey<T>> listElements();

   Optional<? extends HolderSet<T>> get(TagKey<T> pKey);

   Stream<TagKey<T>> listTags();

   static <T> HolderLookup<T> forRegistry(Registry<T> pRegistry) {
      return new HolderLookup.RegistryLookup<>(pRegistry);
   }

   public static class RegistryLookup<T> implements HolderLookup<T> {
      protected final Registry<T> registry;

      public RegistryLookup(Registry<T> pRegistry) {
         this.registry = pRegistry;
      }

      public Optional<Holder<T>> get(ResourceKey<T> pKey) {
         return this.registry.getHolder(pKey);
      }

      public Stream<ResourceKey<T>> listElements() {
         return this.registry.entrySet().stream().map(Map.Entry::getKey);
      }

      public Optional<? extends HolderSet<T>> get(TagKey<T> pKey) {
         return this.registry.getTag(pKey);
      }

      public Stream<TagKey<T>> listTags() {
         return this.registry.getTagNames();
      }
   }
}