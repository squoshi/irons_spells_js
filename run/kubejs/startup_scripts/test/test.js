// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded startup TEST example script)')

StartupEvents.registry("attribute", event => {
    // Attributes should be registered here first before using on the event below, like schools
    event.create("test_spell_power", "spell")
        .range(6.0, 0, 10)
        .attachToPlayers()

    event.create("test_spell_resistance", "spell")
        .range(4.0, 0, 10)
        .attachToPlayers()

    event.create("test_spell_default", "spell")
        .range(2.0, 0, 10)
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
    event.create("test_spellbook", "spellbook")
        .setMaxSpellSlots(3)
        .rarity("UNCOMMON")

    event.create("test_attribute_spellbook", "spellbook")
        .setMaxSpellSlots(8)
        .addAttribute("kubejs:test_spell_power", 2.0, "add_multiplied_total")
        .addAttribute("kubejs:test_spell_resistance", 5.0, "add_value")
        .rarity("RARE")

    event.create("test_unique_spellbook", "spellbook")
        .setMaxSpellSlots(4)
        .addAttribute("kubejs:test_spell_power", 4.0, "add_multiplied_total")
        .addAttribute("kubejs:test_spell_resistance", 2.0, "add_value")
        .addSpell("irons_spellbooks:firebolt", 1)
        .rarity("EPIC")

    event.create("test_affinity_spellbook", "spellbook")
        .setMaxSpellSlots(3)
        .addAttribute("kubejs:test_spell_power", 2.0, "add_multiplied_total")
        .addAttribute("kubejs:test_spell_resistance", 5.0, "add_value")
        .addSpell("irons_spellbooks:firebolt", 3)
        .setAffinitySpell("irons_spellbooks:raise_dead")
        .rarity("EPIC")

    event.create("test_staff", "staff")
        .setEnchantmentValue(30)
        .setTier(tier => {
            // valid tiers are: GRAYBEARD, ARTIFICER, ICE_STAFF, LIGHTNING_ROD, BLOOD_STAFF
            // boolean is to merge or not Tier with your attributes
            tier.useBaseTier("ICE_STAFF", true)
                .addAttribute("kubejs:test_spell_power", 2.0, "add_multiplied_total")
                .addAttribute("kubejs:test_spell_resistance", 5.0, "add_value")
                .setSpeed(-2)
                .setDamage(42)
        })

    event.create("test_magic_sword", "magic_sword")
        .addSpell("irons_spellbooks:firebolt", 1)
        .addSpell("irons_spellbooks:raise_dead", 2)
        .setTier(tier => {
            // valid tiers are: KEEPER_FLAMBERGE, DREADSWORD, MISERY,
            // METAL_MAGEHUNTER, CRYSTAL_MAGEHUNTER, SPELLBREAKER, TRUTHSEEKER,
            // CLAYMORE, AMETHYST_RAPIER
            // boolean is to merge or not Tier with your attributes
            tier.useBaseTier("CRYSTAL_MAGEHUNTER", true)
                .addAttribute("kubejs:test_spell_power", 2.0, "add_multiplied_total")
                .setUses(666)
                .setDamage(12)
                .setSpeed(-3)
                .setEnchantmentValue(6)
                .setIncorrectBlocksForDrops("minecraft:incorrect_for_gold_tool")
                .setRepairIngredient(() => Ingredient.of("minecraft:gold_ingot"))
        })
        .food(builder => {
            builder.nutrition(4)
                .saturation(0.4)
                .alwaysEdible()
                .eatSeconds(3)
                // .fastToEat() // 0.8s
                .usingConvertsTo("minecraft:bucket")
                .effect("minecraft:invisibility", 100, 0, 0.5)
                .eaten(ctx => ctx.entity.tell("Did you just eat a sword, fam?"))
        })
        // .component("...", value)
        .maxStackSize(1)
        .maxDamage(500)
        .containerItem("minecraft:acacia_boat")
        .rarity("epic")
        .fireResistant()
        .disableRepair()
        .jukeboxPlayable("minecraft:cat", true)
})
