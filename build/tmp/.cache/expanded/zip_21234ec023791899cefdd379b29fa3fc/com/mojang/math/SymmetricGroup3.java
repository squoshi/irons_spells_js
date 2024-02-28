package com.mojang.math;

import java.util.Arrays;
import net.minecraft.Util;

/**
 * The symmetric group S3, also known as all the permutation orders of three elements.
 */
public enum SymmetricGroup3 {
   P123(0, 1, 2),
   P213(1, 0, 2),
   P132(0, 2, 1),
   P231(1, 2, 0),
   P312(2, 0, 1),
   P321(2, 1, 0);

   private final int[] permutation;
   private final Matrix3f transformation;
   private static final int ORDER = 3;
   private static final SymmetricGroup3[][] cayleyTable = Util.make(new SymmetricGroup3[values().length][values().length], (p_109188_) -> {
      for(SymmetricGroup3 symmetricgroup3 : values()) {
         for(SymmetricGroup3 symmetricgroup31 : values()) {
            int[] aint = new int[3];

            for(int i = 0; i < 3; ++i) {
               aint[i] = symmetricgroup3.permutation[symmetricgroup31.permutation[i]];
            }

            SymmetricGroup3 symmetricgroup32 = Arrays.stream(values()).filter((p_175577_) -> {
               return Arrays.equals(p_175577_.permutation, aint);
            }).findFirst().get();
            p_109188_[symmetricgroup3.ordinal()][symmetricgroup31.ordinal()] = symmetricgroup32;
         }
      }

   });

   private SymmetricGroup3(int pFirst, int pSecond, int pThird) {
      this.permutation = new int[]{pFirst, pSecond, pThird};
      this.transformation = new Matrix3f();
      this.transformation.set(0, this.permutation(0), 1.0F);
      this.transformation.set(1, this.permutation(1), 1.0F);
      this.transformation.set(2, this.permutation(2), 1.0F);
   }

   public SymmetricGroup3 compose(SymmetricGroup3 pOther) {
      return cayleyTable[this.ordinal()][pOther.ordinal()];
   }

   public int permutation(int pElement) {
      return this.permutation[pElement];
   }

   public Matrix3f transformation() {
      return this.transformation;
   }
}