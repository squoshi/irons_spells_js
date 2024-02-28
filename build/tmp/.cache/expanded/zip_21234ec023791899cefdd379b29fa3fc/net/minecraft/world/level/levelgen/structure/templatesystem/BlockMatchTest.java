package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMatchTest extends RuleTest {
   public static final Codec<BlockMatchTest> CODEC = Registry.BLOCK.byNameCodec().fieldOf("block").xmap(BlockMatchTest::new, (p_74073_) -> {
      return p_74073_.block;
   }).codec();
   private final Block block;

   public BlockMatchTest(Block p_74067_) {
      this.block = p_74067_;
   }

   public boolean test(BlockState pState, RandomSource pRandom) {
      return pState.is(this.block);
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.BLOCK_TEST;
   }
}