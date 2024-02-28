package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Locale;

public class AddNewChoices extends DataFix {
   private final String name;
   private final DSL.TypeReference type;

   public AddNewChoices(Schema pOutputSchema, String pName, DSL.TypeReference pType) {
      super(pOutputSchema, true);
      this.name = pName;
      this.type = pType;
   }

   public TypeRewriteRule makeRule() {
      TaggedChoice.TaggedChoiceType<?> taggedchoicetype = this.getInputSchema().findChoiceType(this.type);
      TaggedChoice.TaggedChoiceType<?> taggedchoicetype1 = this.getOutputSchema().findChoiceType(this.type);
      return this.cap(this.name, taggedchoicetype, taggedchoicetype1);
   }

   protected final <K> TypeRewriteRule cap(String pName, TaggedChoice.TaggedChoiceType<K> p_14639_, TaggedChoice.TaggedChoiceType<?> p_14640_) {
      if (p_14639_.getKeyType() != p_14640_.getKeyType()) {
         throw new IllegalStateException("Could not inject: key type is not the same");
      } else {
         return this.fixTypeEverywhere(pName, p_14639_, (TaggedChoice.TaggedChoiceType<K>)p_14640_, (p_14636_) -> {
            return (p_145061_) -> {
               if (!((TaggedChoice.TaggedChoiceType<K>)p_14640_).hasType(p_145061_.getFirst())) {
                  throw new IllegalArgumentException(String.format(Locale.ROOT, "Unknown type %s in %s ", p_145061_.getFirst(), this.type));
               } else {
                  return p_145061_;
               }
            };
         });
      }
   }
}