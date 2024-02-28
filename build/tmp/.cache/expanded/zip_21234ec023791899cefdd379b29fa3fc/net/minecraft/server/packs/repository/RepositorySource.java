package net.minecraft.server.packs.repository;

import java.util.function.Consumer;

public interface RepositorySource {
   void loadPacks(Consumer<Pack> pInfoConsumer, Pack.PackConstructor pInfoFactory);
}