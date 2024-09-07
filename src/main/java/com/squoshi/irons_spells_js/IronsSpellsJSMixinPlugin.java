package com.squoshi.irons_spells_js;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import org.objectweb.asm.tree.ClassNode;
import org.reflections.Reflections;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class IronsSpellsJSMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    // ------------------------------------------ WIP ------------------------------------------
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        IronsSpellsJSMod.LOGGER.info("Applying mixin " + mixinClassName + " to " + targetClassName);
        if (mixinClassName.equals("com.squoshi.irons_spells_js.mixin.AllSpellsMixin")) {
            IronsSpellsJSMod.LOGGER.info("Applying AllSpellsMixin");
            Set<Class<? extends AbstractSpell>> subtypes = new Reflections("io.redspace.ironsspellbooks.api.spells").getSubTypesOf(AbstractSpell.class);
//            subtypes.forEach(subtype -> {
//                mixinInfo.getTargetClasses().add(subtype.getName().replace(".", "/"));
//            });
            subtypes.forEach(subtype -> {
                IronsSpellsJSMod.LOGGER.info("Found subtype: " + subtype.getName());
            });
        }
    }
    // -----------------------------------------------------------------------------------------

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
