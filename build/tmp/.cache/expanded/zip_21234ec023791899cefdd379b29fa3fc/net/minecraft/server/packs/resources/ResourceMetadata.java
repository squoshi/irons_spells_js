package net.minecraft.server.packs.resources;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
   ResourceMetadata EMPTY = new ResourceMetadata() {
      public <T> Optional<T> getSection(MetadataSectionSerializer<T> p_215584_) {
         return Optional.empty();
      }
   };

   static ResourceMetadata fromJsonStream(InputStream pStream) throws IOException {
      BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(pStream, StandardCharsets.UTF_8));

      ResourceMetadata resourcemetadata;
      try {
         final JsonObject jsonobject = GsonHelper.parse(bufferedreader);
         resourcemetadata = new ResourceMetadata() {
            public <T> Optional<T> getSection(MetadataSectionSerializer<T> p_215589_) {
               String s = p_215589_.getMetadataSectionName();
               return jsonobject.has(s) ? Optional.of(p_215589_.fromJson(GsonHelper.getAsJsonObject(jsonobject, s))) : Optional.empty();
            }
         };
      } catch (Throwable throwable1) {
         try {
            bufferedreader.close();
         } catch (Throwable throwable) {
            throwable1.addSuppressed(throwable);
         }

         throw throwable1;
      }

      bufferedreader.close();
      return resourcemetadata;
   }

   <T> Optional<T> getSection(MetadataSectionSerializer<T> pSerializer);
}