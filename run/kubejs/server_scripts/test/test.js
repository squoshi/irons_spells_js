// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded server TEST example script)')

PlayerEvents.changeMana(event => {
	// This makes it so that casting any spell consumes only 10 mana
	if (event.getMagicData().getCastSource() != 'SPELLBOOK') return
	event.setNewMana(event.getOldMana() - 10)
	// event.cancel()
})

PlayerEvents.spellOnCast(event => {
	console.log("--- ON-CAST ---")
	console.log(event.entity)
	console.log(event.entity.magicData ?? undefined)
})

PlayerEvents.spellPreCast(event => {
	console.log("--- PRE-CAST ---")
	console.log(event.entity)
	console.log(event.entity.magicData ?? undefined)
})
