package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpectatorPage {
   public static final int NO_SELECTION = -1;
   private final List<SpectatorMenuItem> items;
   private final int selection;

   public SpectatorPage(List<SpectatorMenuItem> pItems, int pSelection) {
      this.items = pItems;
      this.selection = pSelection;
   }

   public SpectatorMenuItem getItem(int pIndex) {
      return pIndex >= 0 && pIndex < this.items.size() ? MoreObjects.firstNonNull(this.items.get(pIndex), SpectatorMenu.EMPTY_SLOT) : SpectatorMenu.EMPTY_SLOT;
   }

   public int getSelectedSlot() {
      return this.selection;
   }
}