package com.squoshi.irons_spells_js.mixin;

import com.squoshi.irons_spells_js.util.ClientInitISSKJS;
import dev.latvian.mods.kubejs.client.ClientInitEventJS;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientInitEventJS.class)
public class ClientInitEventJSMixin implements ClientInitISSKJS {
}