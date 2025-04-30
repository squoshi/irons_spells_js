// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded server TEST example script)')

ISSEvents.changeMana(event => {
	console.log("-- @" + event.entity.scriptType)
	console.log("--- CHANGE-MANA ---")
	console.log(event.entity ?? undefined)
	console.log(event.magicData ?? undefined)
	if (!event.magicData.casting) { // only when not casting
		console.log(event.oldMana ?? undefined)

  	event.setNewMana(event.oldMana + 0.5)

  	console.log(event.newMana ?? undefined)
	}
})

ISSEvents.spellPreCast(event => {
	console.log("-- @" + event.entity.scriptType)
	console.log("--- PRE-CAST ---")
	console.log(event.entity ?? undefined)
	console.log(event.spellId ?? undefined)
	console.log(event.schoolType ?? undefined)
	console.log(event.spellLevel ?? undefined)
	console.log(event.castSource ?? undefined)
//	console.log(event.entity.magicData ?? undefined)
})

ISSEvents.spellOnCast(event => {
	console.log("-- @" + event.entity.scriptType)
	console.log("--- ON-CAST ---")
	console.log(event.entity ?? undefined)
  console.log(event.spellId ?? undefined)
  console.log(event.schoolType ?? undefined)
  console.log("Old Spell Level: " + event.originalSpellLevel ?? undefined)

  event.setSpellLevel(event.originalSpellLevel + 1)

  console.log("New Spell Level: " + event.spellLevel ?? undefined)
  console.log(event.castSource ?? undefined)

	console.log("Old Mana Cost: " + event.manaCost ?? undefined)
	event.setManaCost(event.manaCost + 1)
	console.log("New Mana Cost: " + event.manaCost ?? undefined)

//	console.log(event.entity.magicData ?? undefined)
})

ISSEvents.spellPostCast(event => {
	console.log("-- @" + event.entity.scriptType)
	console.log("--- POST-CAST ---")
	console.log(event.entity ?? undefined)
	console.log(event.spell ?? undefined)
	console.log(event.spellLevel ?? undefined)
	console.log(event.level ?? undefined)
	console.log(event.magicData ?? undefined)
//	console.log(event.entity.magicData ?? undefined)
})

ServerEvents.loaded(event => {
	console.log("Status: " + Spell.checkStatus("irons_spellbooks:raise_dead"))
	console.log("Enabled: " + Spell.isEnabled("irons_spellbooks:raise_dead"))
})

ServerEvents.recipes(event => {
	let brew = event.recipes.irons_spellbooks.alchemist_cauldron_brew
	// results - list of fluids to be created in the caldron
	// input - ingredient (item)
	// base_fluid - Fluid to be consumed in the cauldron
	// byproduct - itemstack (optional)

	// This fills cauldron with 500x milk, consuming white terracotta and 1000x water
	brew(["0.5B x minecraft:milk"], "minecraft:white_terracotta", "1B x minecraft:water")
	// with optional byproduct
	// brew(["0.5B x minecraft:milk"], "minecraft:white_terracotta", "1B x minecraft:water", "minecraft:terracotta")

	let empty = event.recipes.irons_spellbooks.alchemist_cauldron_empty
	// result - itemstack (at your hand)
  // input - ingredient (item)
  // fluid - Fluid to be consumed in the cauldron
  // sound - Sound to be played (optional, default is glass bottle filling sound)

	// This returns white concrete, consuming dirt and 250x milk
	empty("minecraft:white_concrete", "minecraft:dirt", "250x minecraft:milk")
	// with optional sound
	// empty("minecraft:white_concrete", "minecraft:dirt", "250x minecraft:milk", "irons_spellbooks:cast.generic.lightning")

  let fill = event.recipes.irons_spellbooks.alchemist_cauldron_fill
	// result - itemstack (at your hand)
	// input - ingredient (item)
	// fluid - Fluid filled in the cauldron
	// mustFitAll - true or false (optional, default is true)
	// sound - Sound to be played (optional, default is glass bottle emptying sound)

	// This fills cauldron with 1000x milk, consuming a milk_bucket and returns an empty bucket
	fill("1000x minecraft:milk", "minecraft:milk_bucket", "minecraft:bucket")
	// with optional mustFitAll and sound
	// fill("1000x minecraft:milk", "minecraft:milk_bucket", "minecraft:bucket", false)
	// fill("1000x minecraft:milk", "minecraft:milk_bucket", "minecraft:bucket", false, "irons_spellbooks:cast.generic.lightning")
})