package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import org.slf4j.Logger;

public abstract class Settings<T extends Settings<T>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final Properties properties;

   public Settings(Properties pProperties) {
      this.properties = pProperties;
   }

   public static Properties loadFromFile(Path pPath) {
      Properties properties = new Properties();

      try {
         InputStream inputstream = Files.newInputStream(pPath);

         try {
            properties.load(inputstream);
         } catch (Throwable throwable1) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable throwable) {
                  throwable1.addSuppressed(throwable);
               }
            }

            throw throwable1;
         }

         if (inputstream != null) {
            inputstream.close();
         }
      } catch (IOException ioexception) {
         LOGGER.error("Failed to load properties from file: {}", (Object)pPath);
      }

      return properties;
   }

   public void store(Path pPath) {
      try {
         OutputStream outputstream = Files.newOutputStream(pPath);

         try {
            net.minecraftforge.common.util.SortedProperties.store(properties, outputstream, "Minecraft server properties");
         } catch (Throwable throwable1) {
            if (outputstream != null) {
               try {
                  outputstream.close();
               } catch (Throwable throwable) {
                  throwable1.addSuppressed(throwable);
               }
            }

            throw throwable1;
         }

         if (outputstream != null) {
            outputstream.close();
         }
      } catch (IOException ioexception) {
         LOGGER.error("Failed to store properties to file: {}", (Object)pPath);
      }

   }

   private static <V extends Number> Function<String, V> wrapNumberDeserializer(Function<String, V> pParseFunc) {
      return (p_139845_) -> {
         try {
            return pParseFunc.apply(p_139845_);
         } catch (NumberFormatException numberformatexception) {
            return (V)null;
         }
      };
   }

   protected static <V> Function<String, V> dispatchNumberOrString(IntFunction<V> pById, Function<String, V> pByName) {
      return (p_139856_) -> {
         try {
            return pById.apply(Integer.parseInt(p_139856_));
         } catch (NumberFormatException numberformatexception) {
            return pByName.apply(p_139856_);
         }
      };
   }

   @Nullable
   private String getStringRaw(String pKey) {
      return (String)this.properties.get(pKey);
   }

   @Nullable
   protected <V> V getLegacy(String pKey, Function<String, V> p_139816_) {
      String s = this.getStringRaw(pKey);
      if (s == null) {
         return (V)null;
      } else {
         this.properties.remove(pKey);
         return p_139816_.apply(s);
      }
   }

   protected <V> V get(String pKey, Function<String, V> p_139823_, Function<V, String> pToString, V pValue) {
      String s = this.getStringRaw(pKey);
      V v = MoreObjects.firstNonNull((V)(s != null ? p_139823_.apply(s) : null), pValue);
      this.properties.put(pKey, pToString.apply(v));
      return v;
   }

   protected <V> Settings<T>.MutableValue<V> getMutable(String pKey, Function<String, V> p_139870_, Function<V, String> pToString, V pValue) {
      String s = this.getStringRaw(pKey);
      V v = MoreObjects.firstNonNull((V)(s != null ? p_139870_.apply(s) : null), pValue);
      this.properties.put(pKey, pToString.apply(v));
      return new Settings.MutableValue(pKey, v, pToString);
   }

   protected <V> V get(String pKey, Function<String, V> p_139828_, UnaryOperator<V> p_139829_, Function<V, String> pToString, V pValue) {
      return this.get(pKey, (p_139849_) -> {
         V v = p_139828_.apply(p_139849_);
         return (V)(v != null ? p_139829_.apply(v) : null);
      }, pToString, pValue);
   }

   protected <V> V get(String pKey, Function<String, V> p_139819_, V pValue) {
      return this.get(pKey, p_139819_, Objects::toString, pValue);
   }

   protected <V> Settings<T>.MutableValue<V> getMutable(String pKey, Function<String, V> p_139866_, V pValue) {
      return this.getMutable(pKey, p_139866_, Objects::toString, pValue);
   }

   protected String get(String pKey, String pValue) {
      return this.get(pKey, Function.identity(), Function.identity(), pValue);
   }

   @Nullable
   protected String getLegacyString(String p_139804_) {
      return this.getLegacy(p_139804_, Function.identity());
   }

   protected int get(String pKey, int pValue) {
      return this.get(pKey, wrapNumberDeserializer(Integer::parseInt), pValue);
   }

   protected Settings<T>.MutableValue<Integer> getMutable(String pKey, int pValue) {
      return this.getMutable(pKey, wrapNumberDeserializer(Integer::parseInt), pValue);
   }

   protected int get(String pKey, UnaryOperator<Integer> p_139834_, int pValue) {
      return this.get(pKey, wrapNumberDeserializer(Integer::parseInt), p_139834_, Objects::toString, pValue);
   }

   protected long get(String pKey, long pValue) {
      return this.get(pKey, wrapNumberDeserializer(Long::parseLong), pValue);
   }

   protected boolean get(String pKey, boolean pValue) {
      return this.get(pKey, Boolean::valueOf, pValue);
   }

   protected Settings<T>.MutableValue<Boolean> getMutable(String pKey, boolean pValue) {
      return this.getMutable(pKey, Boolean::valueOf, pValue);
   }

   @Nullable
   protected Boolean getLegacyBoolean(String pKey) {
      return this.getLegacy(pKey, Boolean::valueOf);
   }

   protected Properties cloneProperties() {
      Properties properties = new Properties();
      properties.putAll(this.properties);
      return properties;
   }

   protected abstract T reload(RegistryAccess pRegistryAccess, Properties pProperties);

   public class MutableValue<V> implements Supplier<V> {
      private final String key;
      private final V value;
      private final Function<V, String> serializer;

      MutableValue(String pKey, V pValue, Function<V, String> pSerializer) {
         this.key = pKey;
         this.value = pValue;
         this.serializer = pSerializer;
      }

      public V get() {
         return this.value;
      }

      public T update(RegistryAccess p_139896_, V p_139897_) {
         Properties properties = Settings.this.cloneProperties();
         properties.put(this.key, this.serializer.apply(p_139897_));
         return Settings.this.reload(p_139896_, properties);
      }
   }
}
