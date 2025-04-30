package com.squoshi.irons_spells_js.spell;

import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CustomSpell extends AbstractSpell {
	private final ResourceLocation spellResource;
	private final DefaultConfig defaultConfig;
	private final CastType castType;
	private final Holder<SoundEvent> startSound, finishSound;
	private final Consumer<CastContext> onCast;
	private final Consumer<CastClientContext> onClientCast;
	private final Consumer<PreCastContext> onPreCast;
	private final Consumer<PreCastClientContext> onPreClientCast;
	private final boolean allowLooting;
	private final boolean needsLearning;
	private final Predicate<Player> canBeCrafted;
	private final BiFunction<Integer, LivingEntity, List<MutableComponent>> uniqueInfo;
	private final AnimationHolder castStartAnimation;
	private final AnimationHolder castFinishAnimation;
	private final Predicate<PreCastTargetingContext> preCastConditions;

	public CustomSpell(Builder b) {
		this.spellResource = b.spellResource;
		this.defaultConfig = new DefaultConfig()
			.setMinRarity(b.minRarity)
			.setSchoolResource(b.school)
			.setMaxLevel(b.maxLevel)
			.setCooldownSeconds(b.cooldownSeconds)
			.build();
		this.castType = b.castType;
		this.startSound = b.startSound;
		this.finishSound = b.finishSound;
		this.onCast = b.onCast;
		this.onClientCast = b.onClientCast;
		this.onPreCast = b.onPreCast;
		this.onPreClientCast = b.onPreClientCast;
		this.manaCostPerLevel = b.manaCostPerLevel;
		this.baseSpellPower = b.baseSpellPower;
		this.spellPowerPerLevel = b.spellPowerPerLevel;
		this.castTime = b.castTime;
		this.baseManaCost = b.baseManaCost;
		this.allowLooting = b.allowLooting;
		this.needsLearning = b.needsLearning;
		this.canBeCrafted = b.canBeCrafted;
		this.uniqueInfo = b.uniqueInfo;
		this.castStartAnimation = b.castStartAnimation;
		this.castFinishAnimation = b.castFinishAnimation;
		this.preCastConditions = b.preCastConditions;
	}

	@Override
	public ResourceLocation getSpellResource() {
		return spellResource;
	}

	@Override
	public DefaultConfig getDefaultConfig() {
		return defaultConfig;
	}

	@Override
	public CastType getCastType() {
		return castType;
	}

	@Override
	public Optional<SoundEvent> getCastStartSound() {
		return startSound != null ? Optional.of(startSound.value()) : super.getCastStartSound();
	}

	@Override
	public Optional<SoundEvent> getCastFinishSound() {
		return finishSound != null ? Optional.of(finishSound.value()) : super.getCastFinishSound();
	}

	@Override
	public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
		if (onCast != null) {
			var context = new CastContext(level, spellLevel, entity, castSource, playerMagicData);
			ISSKJSUtils.safeCallback(onCast, context, "Error while calling onCast");
		}
		super.onCast(level, spellLevel, entity, castSource, playerMagicData);
	}

	@Override
	public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
		if (onClientCast != null) {
			var context = new CastClientContext(level, spellLevel, entity, castData);
			ISSKJSUtils.safeCallback(onClientCast, context, "Error while calling onClientCast");
		}
		super.onClientCast(level, spellLevel, entity, castData);
	}

	@Override
	public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
		if (onPreCast != null) {
			var context = new PreCastContext(level, spellLevel, entity, playerMagicData);
			ISSKJSUtils.safeCallback(onPreCast, context, "Error while calling onPreCast");
		}
		super.onServerPreCast(level, spellLevel, entity, playerMagicData);
	}

	@Override
	public void onClientPreCast(Level level, int spellLevel, LivingEntity entity, InteractionHand hand, MagicData playerMagicData) {
		if (onPreClientCast != null) {
			var context = new PreCastClientContext(level, spellLevel, entity, hand, playerMagicData);
			ISSKJSUtils.safeCallback(onPreClientCast, context, "Error while calling onPreClientCast");
		}
		super.onClientPreCast(level, spellLevel, entity, hand, playerMagicData);
	}

	@Override
	public boolean allowLooting() {
		return allowLooting;
	}

	@Override
	public boolean requiresLearning() {
		return needsLearning;
	}

	@Override
	public boolean canBeCraftedBy(Player player) {
		if (canBeCrafted != null) {
			return canBeCrafted.test(player);
		}
		return true;
	}

	@Override
	public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
		if (this.uniqueInfo != null) {
			return this.uniqueInfo.apply(spellLevel, caster);
		}
		return super.getUniqueInfo(spellLevel, caster);
	}

	@Override
	public AnimationHolder getCastStartAnimation() {
		if (castStartAnimation != null) {
			return castStartAnimation;
		}
		return super.getCastStartAnimation();
	}

	@Override
	public AnimationHolder getCastFinishAnimation() {
		if (castFinishAnimation != null) {
			return castFinishAnimation;
		}
		return super.getCastFinishAnimation();
	}

	@Override
	public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
		if (this.preCastConditions != null) {
			return this.preCastConditions.test(new PreCastTargetingContext(level, spellLevel, entity, playerMagicData, this));
		}
		return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
	}

	public record CastContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, CastSource getCastSource,
					   MagicData getPlayerMagicData) {
	}

	public record CastClientContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, ICastData getCastData) {
	}

	public record PreCastContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, MagicData getPlayerMagicData) {
	}

	public record PreCastClientContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, InteractionHand getHand,
								MagicData getPlayerMagicData) {
	}

	public record PreCastTargetingContext(Level getLevel, int getSpellLevel, LivingEntity getEntity,
								   MagicData getPlayerMagicData, AbstractSpell getSpell) {
	}

	@SuppressWarnings({"unused"})
	public static class Builder extends BuilderBase<CustomSpell> {
		private final ResourceLocation spellResource;
		private SpellRarity minRarity = SpellRarity.COMMON;
		private ResourceLocation school = SchoolRegistry.BLOOD_RESOURCE;
		private int maxLevel = 10;
		private int cooldownSeconds = 20;
		private CastType castType = CastType.INSTANT;
		private Holder<SoundEvent> startSound = null;
		private Holder<SoundEvent> finishSound = null;
		private Consumer<CastContext> onCast = null;
		private Consumer<CastClientContext> onClientCast = null;
		private Consumer<PreCastContext> onPreCast = null;
		private Consumer<PreCastClientContext> onPreClientCast = null;
		private int manaCostPerLevel = 20;
		private int baseSpellPower = 0;
		private int spellPowerPerLevel = 1;
		private int castTime = 0;
		private int baseManaCost = 40;
		private boolean allowLooting = false;
		private boolean needsLearning = false;
		private Predicate<Player> canBeCrafted = null;
		private BiFunction<Integer, LivingEntity, List<MutableComponent>> uniqueInfo;
		private AnimationHolder castStartAnimation = null;
		private AnimationHolder castFinishAnimation = null;
		private Predicate<PreCastTargetingContext> preCastConditions = null;

		public Builder(ResourceLocation i) {
			super(i);
			this.spellResource = i;
		}

		@Info(value = """
			    Sets the cast type. Can be `CONTINUOUS`, `INSTANT`, `LONG`, or `NONE`.
			""")
		public Builder setCastType(CastType type) {
			this.castType = type;
			return this;
		}

		@Info(value = """
			    Sets the sound that the spell will play when it starts casting.
			""")
		public Builder setStartSound(Holder<SoundEvent> soundEvent) {
			this.startSound = soundEvent;
			return this;
		}

		@Info(value = """
			    Sets the sound that the spell will play after it is done casting.
			""")
		public Builder setFinishSound(Holder<SoundEvent> soundEvent) {
			this.finishSound = soundEvent;
			return this;
		}

		@Info(value = """
			    Sets the rarity of the spell. Can be `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, or `LEGENDARY`.
			""")
		public Builder setMinRarity(SpellRarity rarity) {
			this.minRarity = rarity;
			return this;
		}

		@Info(value = """
			    Sets the school of the spell. The different schools each are a resource location.
			
			    Example: `.setSchool(SchoolRegistry.BLOOD_RESOURCE`
			    Another example: `setSchool('irons_spellbooks:blood')`
			""")
		public Builder setSchool(Holder<SchoolType> schoolHolder) {
			this.school = ResourceLocation.parse(schoolHolder.getRegisteredName());
			return this;
		}

		@Info(value = """
			    Sets the max level of the spell. Goes up to `10` from `1`.
			""")
		public Builder setMaxLevel(int level) {
			this.maxLevel = level;
			return this;
		}

		@Info(value = """
			    Sets the cooldown of the spell in seconds. Cannot be a decimal value for some reason.
			""")
		public Builder setCooldownSeconds(int seconds) {
			this.cooldownSeconds = seconds;
			return this;
		}

		@Info(value = """
			    Sets the mana cost per the spell's level. For example, you could input `10` into this method, and each level of the spell will multiply that value by the level.
			""")
		public Builder setManaCostPerLevel(int cost) {
			this.manaCostPerLevel = cost;
			return this;
		}

		@Info(value = """
			    Sets the base spell power. Can be from `1` to `10`. The spell power per level adds on to this.
			""")
		public Builder setBaseSpellPower(int power) {
			this.baseSpellPower = power;
			return this;
		}

		@Info(value = """
			    Sets the spell power per level.
			""")
		public Builder setSpellPowerPerLevel(int power) {
			this.spellPowerPerLevel = power;
			return this;
		}

		@Info(value = """
			    Sets the cast time. This is used for `LONG` or `CONTINUOUS` spell types.
			""")
		public Builder setCastTime(int time) {
			this.castTime = time;
			return this;
		}

		@Info(value = """
			    Sets the base mana cost. The mana cost per level adds on to this.
			""")
		public Builder setBaseManaCost(int cost) {
			this.baseManaCost = cost;
			return this;
		}

		@Info(value = """
			    Sets the callback for when the spell is cast. This is what the spell does when it is casted.
			""")
		public Builder onCast(Consumer<CastContext> consumer) {
			this.onCast = consumer;
			return this;
		}

		@Info(value = """
			    Sets the callback for when the spell is cast on the client side. This is what the spell does when it is casted.
			""")
		public Builder onClientCast(Consumer<CastClientContext> consumer) {
			this.onClientCast = consumer;
			return this;
		}

		@Info(value = """
			    Sets the callback for when the spell is about to be cast. This is what the spell does before it is casted.
			""")
		public Builder onPreCast(Consumer<PreCastContext> consumer) {
			this.onPreCast = consumer;
			return this;
		}

		@Info(value = """
			    Sets the callback for when the spell is about to be cast on the client side. This is what the spell does before it is casted.
			""")
		public Builder onPreClientCast(Consumer<PreCastClientContext> consumer) {
			this.onPreClientCast = consumer;
			return this;
		}

		@Info(value = """
			    Sets whether or not the spell can be looted from a loot table.
			""")
		public Builder setAllowLooting(boolean allow) {
			this.allowLooting = allow;
			return this;
		}

		@Info(value = """
			    Sets whether or not the spell needs to be learned before it can be casted.
			""")
		public Builder needsLearning(boolean needs) {
			this.needsLearning = needs;
			return this;
		}

		@Info(value = """
			    Sets the predicate for whether or not the spell can be crafted by a player.
			""")
		public Builder canBeCraftedBy(Predicate<Player> predicate) {
			this.canBeCrafted = predicate;
			return this;
		}

		@Info(value = """
			    Sets the unique info for the spell. It is what is displayed on the spell in-game, e.g how some spells have damage values listed.
			""")
		public Builder setUniqueInfo(BiFunction<Integer, LivingEntity, List<MutableComponent>> info) {
			this.uniqueInfo = info;
			return this;
		}

		@Info(value = """
			    Sets the cast start animation for the spell.
			""")
		public Builder setCastStartAnimation(String path, boolean playOnce, boolean animatesLegs) {
			this.castStartAnimation = new AnimationHolder(path, playOnce, animatesLegs);
			return this;
		}

		@Info(value = """
			    Sets the cast finish animation for the spell.
			""")
		public Builder setCastFinishAnimation(String path, boolean playOnce, boolean animatesLegs) {
			this.castFinishAnimation = new AnimationHolder(path, playOnce, animatesLegs);
			return this;
		}

		@Info(value = """
			    Sets the pre-cast conditions for the spell. It is a Predicate, which means it requires a boolean return value. This can be used for targeting spells and for cancelling the spell before it is casted.
			
			    Example: ```js
			    .checkPreCastConditions(ctx => {
			        return ISSUtils.preCastTargetHelper(ctx.level, ctx.entity, ctx.playerMagicData, ctx.spell, 48, 0.35)
			    })
			    ```
			""")
		public Builder checkPreCastConditions(Predicate<PreCastTargetingContext> predicate) {
			this.preCastConditions = predicate;
			return this;
		}

		@Override
		public CustomSpell createObject() {
			return new CustomSpell(this);
		}
	}
}