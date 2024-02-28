package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TagMatchTest extends RuleTest {
   public static final Codec<TagMatchTest> CODEC = TagKey.codec(Registry.BLOCK_REGISTRY).fieldOf("tag").xmap(TagMatchTest::new, (p_205065_) -> {
      return p_205065_.tag;
   }).codec();
   private final TagKey<Block> tag;

   public TagMatchTest(TagKey<Block> p_205063_) {
      this.tag = p_205063_;
   }

   public boolean test(BlockState pState, RandomSource pRandom) {
      return pState.is(this.tag);
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.TAG_TEST;
   }
}