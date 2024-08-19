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