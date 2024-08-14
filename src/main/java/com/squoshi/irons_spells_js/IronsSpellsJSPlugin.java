package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.entity.attribute.SpellAttributeBuilderJS;
import com.squoshi.irons_spells_js.item.CustomMagicSwordItem;
import com.squoshi.irons_spells_js.spell.CustomSpell;
import com.squoshi.irons_spells_js.spell.school.SchoolTypeJSBuilder;
import com.squoshi.irons_spells_js.util.ClassUtils;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

import java.util.Collection;
import java.util.Set;

public final class IronsSpellsJSPlugin implements KubeJSPlugin {

    @Override
    public void init() {
        IronsSpellsJSMod.LOGGER.info("Initiating IronsSpellsJSPlugin");

//        RegistryType.register(SpellRegistry.SPELL_REGISTRY_KEY, TypeInfo.of(AbstractSpell.class));
    }

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.addDefault(SpellRegistry.SPELL_REGISTRY_KEY, CustomSpell.Builder.class, CustomSpell.Builder::new);
        registry.addDefault(SchoolRegistry.SCHOOL_REGISTRY_KEY, SchoolTypeJSBuilder.class, SchoolTypeJSBuilder::new);
        registry.of(Registries.ATTRIBUTE, reg -> reg.add("spell", SpellAttributeBuilderJS.class, SpellAttributeBuilderJS::new));
        registry.of(Registries.ITEM, reg -> reg.add("magic_sword", CustomMagicSwordItem.Builder.class, CustomMagicSwordItem.Builder::new));
    }

    @Override
    public void afterInit() {
        var ignoreNamespaces = Set.of("neoforge", "minecraft");
        BuiltInRegistries.REGISTRY.asLookup().listElements()
                .filter(entry -> !ignoreNamespaces.contains(entry.getKey().location().getNamespace()))
                .forEach(filtered -> {
                    var types = ClassUtils.getCommonTypes((Collection<Object>) filtered.value().asLookup().listElements().map(Holder.Reference::value).toList());
                    types.forEach(type -> {
                        System.out.println(filtered.getKey().location() + " / " + type.getTypeName());
                    });
                });
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
//        registry.register(ISSKJSUtils.AttributeHolder.class, ISSKJSUtils.AttributeHolder::of);
//        registry.register(ISSKJSUtils.SoundEventHolder.class, ISSKJSUtils.SoundEventHolder::of);
//        registry.register(ISSKJSUtils.SpellHolder.class, ISSKJSUtils.SpellHolder::of);
//        registry.register(ISSKJSUtils.SchoolHolder.class, ISSKJSUtils.SchoolHolder::of);
//        registry.register(ISSKJSUtils.DamageTypeHolder.class, ISSKJSUtils.DamageTypeHolder::of);
//        registry.register(AbstractSpell.class, o -> {
//            if (o instanceof AbstractSpell spell) return spell;
//            return SpellRegistry.getSpell(ISSKJSUtils.SpellHolder.of(o).getLocation());
//        });
//        registry.register(SchoolType.class, o -> {
//            if (o instanceof SchoolType school) return school;
//            return SchoolRegistry.getSchool(ISSKJSUtils.SchoolHolder.of(o).getLocation());
//        });
    }
}
