// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded startup TEST example script)')

const $MobEffectInstance = Java.loadClass("net.minecraft.world.effect.MobEffectInstance")

StartupEvents.registry("attribute", event => {
    // Attributes should be registered here first before using on the event below, like schools
    event.create("test_spell_power", "spell")
        .setDefaultValue(1.0)
        .setMinimumValue(0.0)
        .setMaximumValue(10.0)

    event.create("test_spell_resistance", "spell")
        .setDefaultValue(1.0)
        .setMinimumValue(0.0)
        .setMaximumValue(10.0)
})

StartupEvents.registry("irons_spellbooks:schools", event => {
    event.create("test")
        .setName(Component.of("Test"))
        .addFocusItemTags("minecraft:planks")
        .addFocusItems(["minecraft:diamond_ore", "minecraft:apple"])
        // default is for this example: `kubejs:test_focus`, only change if you know what you are doing
        .setDefaultFocusTag("some_mod_id:some_path")
        .setPowerAttribute("kubejs:test_spell_power")
        .setResistanceAttribute("kubejs:test_spell_resistance")
        .setDefaultCastSound('minecraft:entity.chicken.death')
        .setDamageType("irons_spellbooks:ender_magic")
        .disableLooting()
        .requiresLearning()
})

StartupEvents.registry('irons_spellbooks:spells', event => {
    event.create('kubejs:test_with_ctx')
        .setCastTime(20)
        .setCooldownSeconds(5)
        .setBaseManaCost(1)
        .setManaCostPerLevel(1)
        .setCastType('instant')
        .setSchool('kubejs:test')
        .canBeCraftedBy(player => true)
        .onCast(ctx => {
            console.log(ctx.level)
            console.log(ctx.spellLevel)
            console.log(ctx.entity)
            console.log(ctx.castSource)
            console.log(ctx.playerMagicData)
            console.log("From onCast With Context")
            ctx.entity.heal(1)
        })

    event.create('kubejs:test_with_ctx_2')
        .setCastTime(10)
        .setCooldownSeconds(8)
        .setBaseManaCost(1)
        .setManaCostPerLevel(15)
        .setCastType('continuous')
        .setSchool("irons_spellbooks:fire")
        .onCast(ctx => {
            console.log(ctx.level)
            console.log(ctx.spellLevel)
            console.log(ctx.entity)
            console.log(ctx.castSource)
            console.log(ctx.playerMagicData)
            console.log("From onCast With Context 2")
            ctx.entity.heal(3)
        })
})

StartupEvents.registry("item", event => {
//    event.create("test_spellbook", "irons_spells_js:spellbook")
//        .setMaxSpellSlots(3)
//
//    event.create("test_attribute_spellbook", "irons_spells_js:spellbook")
//        .setMaxSpellSlots(8)
//        .addDefaultAttribute("kubejs:test_spell_power", "Test Spell Power", 2.0, "multiply_total")
//        .addDefaultAttribute("minecraft:generic.movement_speed", "Test Movement Speed", 1.2, "multiply_total")
//
//    event.create("test_unique_spellbook", "irons_spells_js:spellbook")
//        .setMaxSpellSlots(4)
//        .addDefaultAttribute("kubejs:test_spell_power", "Test Spell Power", 2.0, "multiply_total")
//        .addDefaultAttribute("minecraft:generic.movement_speed", "Test Movement Speed", 1.2, "multiply_total")
//        .addDefaultSpell(SpellRegistry.FIREBOLT_SPELL, 1)
//
//    event.create("test_staff", "irons_spells_js:staff")
//        .addAdditionalAttribute("kubejs:test_spell_power", "Test Spell Power", 2.0, "multiply_total")
//        .attackDamageBaseline(50)
//        .speedBaseline(-2.4)

    event.create("test_magic_sword", "magic_sword")
        .modifyTier(tier => {
            tier.setUses(666)
                .setDamage(12)
                .setSpeed(-3)
                .setEnchantmentValue(6)
                .setIncorrectBlocksForDrops("minecraft:incorrect_for_gold_tool")
                .setRepairIngredient(() => Ingredient.of("minecraft:gold_ingot"))
                .addAdditionalAttribute(["kubejs:test_spell_power", 2.0, "add_multiplied_total"])
        })
        .modifyProperties(prop => {
            prop.food([4, 4, true, 3, Item.of("minecraft:bucket"), [[() => new $MobEffectInstance("minecraft:invisibility", 30), 0.5]]])
                .stacksTo(1)
                .durability(500)
                .craftRemainder("minecraft:acacia_boat")
                .rarity("epic")
                .fireResistant()
                .jukeboxPlayable("cat")
                .setNoRepair()
                // .component("...", value)
                // DO NOT USE THIS, USE .addAdditionalAttribute on .modifyTier(tier => tier.addAdditionalAttribute(...))
                // .attributes([[["kubejs:test_spell_resistance", ["kubejs:some_id", 1.0, "add_value"], "mainhand"]], true])
        })
        .addDefaultSpell("irons_spellbooks:firebolt", 1)
        .addDefaultSpell("irons_spellbooks:raise_dead", 2)


//    event.create("test_magic_sword_2", "irons_spells_js:magic_sword")
//        .addAdditionalAttribute("kubejs:test_spell_power", "Test Spell Power", 2.0, "multiply_total")
//        .attackDamageBaseline(100)
//        .speedBaseline(3)
//        .addDefaultSpell("irons_spellbooks:starfall", 1)
//        .addDefaultSpell("irons_spellbooks:planar_sight", 2)
})

NativeEvents.onEvent("net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent", event => {
    event.types.forEach(type => {
        event.add(type, "kubejs:test_spell_power")
        event.add(type, "kubejs:test_spell_resistance")
    })
})