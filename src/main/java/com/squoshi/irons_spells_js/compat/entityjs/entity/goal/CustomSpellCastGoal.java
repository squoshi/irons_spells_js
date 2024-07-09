package com.squoshi.irons_spells_js.compat.entityjs.entity.goal;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.NavigableMap;

public class CustomSpellCastGoal extends Goal {
    protected final PathfinderMob mob;
    protected LivingEntity target;
    protected final double speedModifier;
    protected final int attackIntervalMin;
    protected final int attackIntervalMax;
    protected float attackRadius;
    protected float attackRadiusSqr;
    protected boolean shortCircuitTemp = false;

    protected boolean hasLineOfSight;
    protected int seeTime = 0;
    protected int strafeTime;
    protected boolean strafingClockwise;
    protected int attackTime = -1;
    protected int projectileCount;

    protected AbstractSpell singleUseSpell = SpellRegistry.none();
    protected int singleUseDelay;
    protected int singleUseLevel;

    protected boolean isFlying;
    protected boolean allowFleeing;
    protected int fleeCooldown;

    protected final ArrayList<AbstractSpell> attackSpells = new ArrayList<>();
    protected final ArrayList<AbstractSpell> defenseSpells = new ArrayList<>();
    protected final ArrayList<AbstractSpell> movementSpells = new ArrayList<>();
    protected final ArrayList<AbstractSpell> supportSpells = new ArrayList<>();
    protected ArrayList<AbstractSpell> lastSpellCategory = attackSpells;

    protected float minSpellQuality = .1f;
    protected float maxSpellQuality = .4f;

    protected boolean drinksPotions;

    public CustomSpellCastGoal(PathfinderMob pathfinderMob, double pSpeedModifier, int pAttackInterval) {
        this(pathfinderMob, pSpeedModifier, pAttackInterval, pAttackInterval);
    }

    public CustomSpellCastGoal(PathfinderMob abstractSpellCastingMob, double pSpeedModifier, int pAttackIntervalMin, int pAttackIntervalMax) {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.mob = abstractSpellCastingMob;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackIntervalMax = pAttackIntervalMax;
        this.attackRadius = 20;
        this.attackRadiusSqr = attackRadius * attackRadius;
        allowFleeing = true;
    }

    public CustomSpellCastGoal setSpells(List<AbstractSpell> attackSpells, List<AbstractSpell> defenseSpells, List<AbstractSpell> movementSpells, List<AbstractSpell> supportSpells) {
        this.attackSpells.clear();
        this.defenseSpells.clear();
        this.movementSpells.clear();
        this.supportSpells.clear();

        this.attackSpells.addAll(attackSpells);
        this.defenseSpells.addAll(defenseSpells);
        this.movementSpells.addAll(movementSpells);
        this.supportSpells.addAll(supportSpells);

        return this;
    }

    public CustomSpellCastGoal setSpellQuality(float minSpellQuality, float maxSpellQuality) {
        this.minSpellQuality = minSpellQuality;
        this.maxSpellQuality = maxSpellQuality;
        return this;
    }

    public CustomSpellCastGoal setSingleUseSpell(AbstractSpell abstractSpell, int minDelay, int maxDelay, int minLevel, int maxLevel) {
        this.singleUseSpell = abstractSpell;
        this.singleUseDelay = Utils.random.nextIntBetweenInclusive(minDelay, maxDelay);
        this.singleUseLevel = Utils.random.nextIntBetweenInclusive(minLevel, maxLevel);
        return this;
    }

    public CustomSpellCastGoal setIsFlying() {
        isFlying = true;
        return this;
    }

    public CustomSpellCastGoal setDrinksPotions() {
        drinksPotions = true;
        return this;
    }

    public CustomSpellCastGoal setAllowFleeing(boolean allowFleeing) {
        this.allowFleeing = allowFleeing;
        return this;
    }

    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            return true;
        } else {
            return false;
        }
    }

    public boolean canContinueToUse() {
        return this.canUse() || this.target.isAlive() && !this.mob.getNavigation().isDone();
    }

    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.setAggressive(false);
        this.mob.getMoveControl().strafe(0, 0);

    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        if (target == null) {
            return;
        }

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }

        doMovement(distanceSquared);

        if (mob.getLastHurtByMobTimestamp() == mob.tickCount - 1) {
            int t = (int) (Mth.lerp(.6f, attackTime, 0) + 1);
            attackTime = t;
        }

        handleAttackLogic(distanceSquared);

        singleUseDelay--;
    }

    protected void handleAttackLogic(double distanceSquared) {
        if (seeTime < -50) {
            return;
        }
        if (--this.attackTime == 0) {
            resetAttackTimer(distanceSquared);
            if (!mob.isCasting() && !mob.isDrinkingPotion()) {
                doSpellAction();
            }
        } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt(distanceSquared) / (double) this.attackRadius, (double) this.attackIntervalMin, (double) this.attackIntervalMax));
        }
        if (mob.isCasting()) {
            var spellData = MagicData.getPlayerMagicData(mob).getCastingSpell();
            if (target.isDeadOrDying() || spellData.getSpell().shouldAIStopCasting(spellData.getLevel(), mob, target)) {
                mob.cancelCast();
            }
        }
    }

    protected void resetAttackTimer(double distanceSquared) {
        float f = (float) Math.sqrt(distanceSquared) / this.attackRadius;
        this.attackTime = Mth.floor(f * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
    }

    protected void doMovement(double distanceSquared) {
        double speed = (mob.isCasting() ? .75f : 1f) * movementSpeed();
        mob.lookAt(target, 30, 30);
        float fleeDist = .275f;
        if (allowFleeing && (!mob.isCasting() && attackTime > 10) && --fleeCooldown <= 0 && distanceSquared < attackRadiusSqr * (fleeDist * fleeDist)) {
            Vec3 flee = DefaultRandomPos.getPosAway(this.mob, 16, 7, target.position());
            if (flee != null) {
                this.mob.getNavigation().moveTo(flee.x, flee.y, flee.z, speed * 1.5);
            } else {
                mob.getMoveControl().strafe(-(float) speed, (float) speed);
            }
        } else if (distanceSquared < attackRadiusSqr && seeTime >= 5) {
            this.mob.getNavigation().stop();
            if (++strafeTime > 25) {
                if (mob.getRandom().nextDouble() < .1) {
                    strafingClockwise = !strafingClockwise;
                    strafeTime = 0;
                }
            }
            float strafeForward = (distanceSquared * 6 < attackRadiusSqr ? -1 : .5f) * .2f * (float) speedModifier;
            int strafeDir = strafingClockwise ? 1 : -1;
            mob.getMoveControl().strafe(strafeForward, (float) speed * strafeDir);
            if (mob.horizontalCollision && mob.getRandom().nextFloat() < .1f) {
                tryJump();
            }
        } else {
            if (mob.tickCount % 5 == 0) {
                if (isFlying) {
                    this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY() + 2, target.getZ(), speedModifier);
                } else {
                    this.mob.getNavigation().moveTo(this.target, speedModifier);
                }
            }
        }
    }

    protected double movementSpeed() {
        return speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2;
    }

    protected void tryJump() {
        Vec3 nextBlock = new Vec3(mob.xxa, 0, mob.zza).normalize();

        BlockPos blockpos = BlockPos.containing(mob.position().add(nextBlock)) ;
        BlockState blockstate = this.mob.level().getBlockState(blockpos);
        VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level(), blockpos);
        if (!voxelshape.isEmpty() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
            BlockPos blockposAbove = blockpos.above();
            BlockState blockstateAbove = this.mob.level().getBlockState(blockposAbove);
            VoxelShape voxelshapeAbove = blockstateAbove.getCollisionShape(this.mob.level(), blockposAbove);
            if (voxelshapeAbove.isEmpty()) {
                this.mob.getJumpControl().jump();
                mob.setXxa(mob.xxa * 5);
                mob.setZza(mob.zza * 5);
            }

        }
    }

    protected void doSpellAction() {
        if (!mob.hasUsedSingleAttack && singleUseSpell != SpellRegistry.none() && singleUseDelay <= 0) {
            mob.hasUsedSingleAttack = true;
            mob.initiateCastSpell(singleUseSpell, singleUseLevel);
            fleeCooldown = 7 + singleUseSpell.getCastTime(singleUseLevel);
        } else {
            var spell = getNextSpellType();
            int spellLevel = (int) (spell.getMaxLevel() * Mth.lerp(mob.getRandom().nextFloat(), minSpellQuality, maxSpellQuality));
            spellLevel = Math.max(spellLevel, 1);

            if (!spell.shouldAIStopCasting(spellLevel, mob, target)) {
                mob.initiateCastSpell(spell, spellLevel);
                fleeCooldown = 7 + spell.getCastTime(spellLevel);
            } else {
                attackTime = 5;
            }
        }
    }

    protected AbstractSpell getNextSpellType() {
        NavigableMap<Integer, ArrayList<AbstractSpell>> weightedSpells = new TreeMap<>();
        int attackWeight = getAttackWeight();
        int defenseWeight = getDefenseWeight() - (lastSpellCategory == defenseSpells ? 100 : 0);
        int movementWeight = getMovementWeight() - (lastSpellCategory == movementSpells ? 50 : 0);
        int supportWeight = getSupportWeight() - (lastSpellCategory == supportSpells ? 100 : 0);
        int total = 0;

        if (!attackSpells.isEmpty() && attackWeight > 0) {
            total += attackWeight;
            weightedSpells.put(total, attackSpells);
        }
        if (!defenseSpells.isEmpty() && defenseWeight > 0) {
            total += defenseWeight;
            weightedSpells.put(total, defenseSpells);
        }
        if (!movementSpells.isEmpty() && movementWeight > 0) {
            total += movementWeight;
            weightedSpells.put(total, movementSpells);
        }
        if ((!supportSpells.isEmpty() || drinksPotions) && supportWeight > 0) {
            total += supportWeight;
            weightedSpells.put(total, supportSpells);
        }

        if (total > 0) {
            int seed = mob.getRandom().nextInt(total);
            var spellList = weightedSpells.higherEntry(seed).getValue();
            lastSpellCategory = spellList;
            if (drinksPotions && spellList == supportSpells) {
                if (supportSpells.isEmpty() || mob.getRandom().nextFloat() < .5f) {
                    mob.startDrinkingPotion();
                    return SpellRegistry.none();
                }
            }
            return spellList.get(mob.getRandom().nextInt(spellList.size()));
        } else {
            return SpellRegistry.none();
        }
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    protected int getAttackWeight() {
        int baseWeight = 80;
        if (!hasLineOfSight || target == null) {
            return 0;
        }

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) ((1 - targetHealth) * baseWeight * .75f);

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        int distanceWeight = (int) (1 - (distanceSquared / attackRadiusSqr) * -60);

        return baseWeight + targetHealthWeight + distanceWeight;
    }

    protected int getDefenseWeight() {
        int baseWeight = -20;

        if (target == null) {
            return baseWeight;
        }

        float x = mob.getHealth();
        float m = mob.getMaxHealth();
        int healthWeight = (int) (50 * (-(x * x * x) / (m * m * m) + 1));

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) (1 - targetHealth) * -35;

        int threatWeight = projectileCount * 95;

        return baseWeight + healthWeight + targetHealthWeight + threatWeight;
    }

    protected int getMovementWeight() {
        if (target == null) {
            return 0;
        }

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double distancePercent = Mth.clamp(distanceSquared / attackRadiusSqr, 0, 1);

        int distanceWeight = (int) ((distancePercent) * 50);

        int losWeight = hasLineOfSight ? 0 : 80;

        float healthInverted = 1 - mob.getHealth() / mob.getMaxHealth();
        float distanceInverted = (float) (1 - distancePercent);
        int runWeight = (int) (400 * healthInverted * healthInverted * distanceInverted * distanceInverted);

        return distanceWeight + losWeight + runWeight;
    }

    protected int getSupportWeight() {
        int baseWeight = -15;

        if (target == null) {
            return baseWeight;
        }

        float health = 1 - mob.getHealth() / mob.getMaxHealth();
        int healthWeight = (int) (200 * health);

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double distancePercent = Mth.clamp(distanceSquared / attackRadiusSqr, 0, 1);
        int distanceWeight = (int) ((1 - distancePercent) * -75);

        return baseWeight + healthWeight + distanceWeight;
    }
}
