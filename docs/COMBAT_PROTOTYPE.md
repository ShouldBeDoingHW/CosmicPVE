# Step 3A combat prototype notes

## Event boundary verified against NeoForge 21.11.45

The prototype uses `LivingIncomingDamageEvent` to run ordinary custom damage math before vanilla mitigation. At that
point vanilla critical-hit scaling and weapon/enchantment attack damage have already been included by the attacker,
but target shield blocking, hurt-immunity comparison, armor, enchantment/magic reductions, mob effects, and absorption
have not yet run.

`LivingDamageEvent.Pre` occurs only after the damage has passed the target hurt-immunity decision. It occurs after
armor, enchantment/magic, and mob-effect reductions, but before absorption. The bridge uses the event's
`DamageContainer` identity to promote only that accepted candidate.

`LivingDamageEvent.Post` reports final health lost after absorption. Only a matching result with positive final health
loss is committed and eligible for tracing or future procs. A rapid hit rejected by the normal target immunity window
never reaches this commit point. CosmicPVE does not read, clear, shorten, or otherwise modify `invulnerableTime`.

## Legacy attack behavior

Vanilla wooden, copper, stone, golden, iron, diamond, and netherite swords and axes receive a main-hand attack-speed
modifier of `+16`, producing approximately `20` total attack speed with the player's base `4`. This yields a one-tick
charge interval (up to about 20 input attacks per second) and explicitly sets their minimum attack-charge gate to zero.
The target's normal immunity window still controls how many of those inputs can actually remove health; against an
unchanged same-strength target this is normally about two committed hits per second.

`SweepAttackEvent` is set to non-sweeping on both logical sides. The prototype does not replace the player attack
method, so vanilla critical hits, sprint knockback, attack sounds, durability, exhaustion, and direct-hit behavior are
left intact.

## Deliberate Step 3A limits

True-damage packets are represented and calculated separately, but are not applied yet. Child sequences can be
created with `NO_PROCS` or `LIMITED_OFFENSIVE_REROLL`; no child attack is delivered in this step. Custom enchantments,
effective enchantment resolution, Doublestrike, Hysteria, procs, cooldowns, stacks, execution kills, and all later
systems remain deferred.

## Step 3B combat primitives

### True-damage delivery

Effect code delivers true damage through `ChildCombatActionService`, never by calling `hurt` directly. The service
creates a unique child sequence linked to its parent and opens a server-thread delivery scope. The event bridge
recognizes that scope, retains the supplied attribution and recursion policy, and records a TRUE trace rather than
creating a fresh ordinary root attack.

Both Cosmic true-damage types bypass shields and the normal target damage cooldown. This is narrowly scoped by damage
type and does not clear or change `invulnerableTime`. A packet with `bypassesArmor=true` also uses the
`cosmicpve:true_damage` type, which is tagged to bypass armor and therefore does not consume armor durability. With
`bypassesArmor=false`, it uses `cosmicpve:mitigated_true_damage` and vanilla armor may reduce it.

The remaining packet flags operate through NeoForge's damage container:

- `bypassesAbsorption` prevents absorption reduction and does not consume absorption hearts.
- `bypassesCustomReduction` bypasses enchantment, mob-effect/resistance, and innate-resistance reductions.
- All true packets bypass the Cosmic ordinary damage formula, including ordinary outgoing/incoming modifiers, Aegis,
  and ordinary caps/floors.

True damage does not bypass general entity invulnerability or vanilla/Cosmic death prevention. Lethal true damage can
therefore still use a totem; only execution is terminal.

### Execution

`ExecutionService` is not a damage service. It sets health to zero and invokes the normal death lifecycle directly,
without calling `hurt`, creating a damage container, consuming absorption, applying mitigation, or checking a totem.
It records a stable execution cause and sequence receipt. An active-execution guard is exposed and is mandatory for
all future Cosmic pre-death/Phoenix hooks. NeoForge's ordinary death notification still runs so entity/player death
lifecycle behavior remains intact, but it is not an ordinary or true-damage proc event.

### Effective enchantments

`EffectiveEnchantmentsResolver` reads actual item enchantments and merges runtime-only
`VirtualEnchantmentGrant` values. Effective level is the maximum level from all sources; provenance is retained and
sorted deterministically by actual/virtual kind, source ID, and level. Virtual grants never mutate the stack, occupy
custom-enchantment slots, or become available to item-removal operations such as Black Scrolls.

Step 3B still does not implement enchantment behavior, procs, cooldowns, stacks, Doublestrike, Hysteria, Bleed,
Lightning, armor sets, masks, or weapon skins.

## Step 4A proc and cooldown foundation

### True-damage default clarification

`TrueDamagePacket.standard` now bypasses armor, shields, the normal target hurt cooldown, ordinary Cosmic incoming
reductions, and Aegis, but does **not** bypass absorption. Standard Cosmic true damage therefore consumes absorption
before red health. The packet retains an explicit `bypassesAbsorption` flag for narrowly configured future effects.

### Proc entry points

`LivingDamageEvent.Post` remains the only offensive valid-hit entry point. After the combat result has positive
committed health damage, the bridge dispatches `ON_VALID_HIT`, `ON_DAMAGE_TAKEN`, projectile-hit when applicable, and
kill when applicable using the combat sequence and recursion policy. Rejected attacks, absorption-only hits, and
zero-health-damage events do not enter these hooks. Execution never enters the proc engine.

Thin logical-server adapters dispatch `ON_PRE_DEATH`, successful non-cancelled block breaks, completed food use, and
periodic living-entity ticks. The pre-death adapter returns immediately during an `ExecutionService` execution. Candidate
resolution is intentionally empty in Step 4A; future enchantments, sets, masks, and skins register candidate-source
resolvers without changing event adapters or the engine. Development fixtures are reachable only through commands
and tests and are not survival content.

### Proc evaluation

`ProcEngine` owns condition checks, recursion filtering, once-per-event claims, cooldown checks, probability math,
the single authoritative roll, cooldown activation, action invocation, and tracing. Chance modifiers are relative:
`baseChance * product(multipliers)`, clamped to `[0, 1]`. One server-seeded random source is created for an event and
each eligible candidate consumes exactly one roll. Filtered or cooldown-blocked candidates do not roll.

`NO_PROCS` rejects every candidate. `LIMITED_OFFENSIVE_REROLL` accepts only candidates that explicitly opt in and are
not named in the event's excluded-effect set. This is the future Doublestrike seam: its child can exclude
Doublestrike's own stable effect ID while allowing other eligible offensive effects.

### Cooldowns and diagnostics

`CooldownService` stores entity UUID plus stable cooldown key to an absolute server-tick expiry. Effective duration is
calculated once with `ceil(baseTicks * product(durationMultipliers))`. Entries are classified as persistent-player,
ephemeral-combat, or instance-session cooldowns. Step 4A stores them only in memory, but exposes immutable snapshots
so a later persistence adapter does not change gameplay callers.

Development commands are permission-gated:

- `/cosmic proc trace on|off|last`
- `/cosmic proc test hit|no-procs`
- `/cosmic cooldown list`
- `/cosmic cooldown set <namespaced-key> <ticks>`
- `/cosmic cooldown clear <namespaced-key|all>`

Actual enchantment behaviors, Luck, armor-set cooldown modifiers, runtime stacks, and Doublestrike delivery remain
deferred.

## Step 4B runtime combat stacks

`CombatStackService` is the only mutation boundary for stack state. Each `LivingEntity` acquires a
`COMBAT_STACKS` attachment only when a stack is actually applied. The container stores an individual immutable
instance for every stack unit, including definition ID/revision, original source entity, credited player,
application/refresh/expiration ticks, scope, and transfer audit data. Per-instance provenance means future effects
can aggregate by source without changing the container format.

Every definition-dependent operation resolves the current immutable `StackDefinition` from `CosmicContent` rather
than copying polarity, limits, duration, or flags into runtime state. A removed definition remains inspectable,
removable, and expirable; cleanse and transfer skip it because its flags can no longer be established safely.

### Refresh policies

- `INDEPENDENT`: each successful application gets `applicationTick + durationTicks`; older instances never refresh.
- `REFRESH_ALL`: each application renews every existing instance to `currentTick + durationTicks`, then adds one new
  instance if below the maximum.
- `REFRESH_ONE`: applications add normally until the definition is full. At maximum, exactly the earliest-expiring
  instance is renewed. Ties use application tick and then stack-instance UUID, so map iteration order is irrelevant.
- `FIXED`: the first instance establishes the group's expiry. Later instances may fill remaining capacity but inherit
  that original expiry. Nothing extends the lifetime until the group expires or is explicitly removed and re-added.

Expiration occurs when `expirationTick <= currentServerTick`. Containers cache their next expiry, so entities without
an attachment or without a due expiry avoid scanning stack state or content definitions on tick.

### Transfer and persistence boundary

Transfer moves exactly one deterministic earliest-expiring instance and preserves its definition revision, original
source, credited player, application tick, and remaining duration. The transfer actor and tick are recorded
separately. A transfer into an existing `FIXED` group is capped by that recipient group's earlier deadline, preventing
transfer from extending a fixed lifetime. Steal selection considers only currently resolved, transferable positive
definitions with recipient capacity and sorts definition IDs before selecting.

Definitions marked `persistent` force instances into `PERSISTENT_ENTITY` scope. The attachment codec writes only that
scope; ephemeral combat and instance-session stacks remain runtime-only. Player death clones copy only persistent
instances, while non-death clones retain all runtime scopes. Full migration and broader persistence/recovery remain
deferred.

Development commands are permission-gated:

- `/cosmic stack add <target> <stack-id> [count]`
- `/cosmic stack remove <target> <stack-id> [count|all]`
- `/cosmic stack list <target>`
- `/cosmic stack cleanse <target> <positive|negative>`
- `/cosmic stack steal <from> <to>`

The development datapack definitions are explicitly named `development_*`; none represent canonical Bleed, Feeding
Frenzy, Hysteria, or other gameplay balance. Stack-driven damage, movement, Luck, healing, and proc behavior remain
deferred.

## Step 5A first Cosmic enchantment slice

Execute, Angelic, Lightning, Ender Shift, and Doublestrike are real Minecraft dynamic-registry enchantments. Their
definitions use CosmicPVE item tags for exact applicability, but are deliberately absent from Minecraft's
`in_enchanting_table`, `tradeable`, `on_random_loot`, `on_traded_equipment`, and `on_mob_spawn_equipment` enchantment
tags. For this milestone they are acquired with the permission-gated vanilla `/enchant` command.

`CosmicEnchantmentBehaviorResolver` is the small Java composition layer for this slice. It is both a proc-candidate
source and, through `ExecuteBehavior`, an ordinary outgoing-damage contributor. It is not a behavior scripting
language. Execute contributes `0.02 * level` to the existing additive outgoing bucket only when the target's health
is strictly below 50 percent immediately before incoming damage calculation. Named additive contributions are
included in combat traces.

Angelic sums the actual levels on head, chest, legs, and feet into one candidate, so one damage event consumes at
most one roll and heals at most 1 HP. Lightning is an `ON_PROJECTILE_HIT` candidate. Its lightning bolt is
`visualOnly`; the authoritative gameplay effect is a separate standard Cosmic true-damage child packet of exactly
2 HP, retaining the parent's projectile-owner attribution and consuming absorption normally.

Ender Shift tests the wearer's post-hit health directly: any committed damaging event leaving a living wearer
strictly below 25 percent health is eligible; crossing the threshold from above is not required. It applies Speed I
and Regeneration I for 60/120/180 ticks and uses the shared `cosmicpve:ender_shift` cooldown with a 600-tick base.
Generic proc-event cooldown multipliers are combined centrally by `ProcEngine`, so future equipment cooldown
modifiers do not require changes to Ender Shift.

Doublestrike halves the parent's `CombatBreakdown.finalOrdinaryDamage`: the finalized ordinary amount entering
vanilla target mitigation, before separately delivered true-damage proc packets. The child then passes through
normal armor, absorption, custom reductions, and defensive reactions. It receives a unique sequence linked to the
parent and `LIMITED_OFFENSIVE_REROLL`, with `cosmicpve:doublestrike` explicitly excluded. Other candidates that opt
into the limited child policy may reroll, while Doublestrike itself cannot recurse.

The child uses the internal `cosmicpve:doublestrike` ordinary damage type, whose only bypass tag is
`minecraft:bypasses_cooldown`. This lets that one linked delivery survive hurt immunity created by its parent without
reading, clearing, shortening, or restoring `invulnerableTime`. `CombatDeliveryScope` validates the child channel,
policy, parent link, exclusion, target, source identity, and internal damage type. Unrelated hits still use normal
hurt immunity.

### Deferred death-order verification

Before Phoenix or any other cancel-death behavior is implemented, test the pinned NeoForge 21.11.45 event ordering
to prove that `ON_KILL` cannot activate for a death later prevented by `LivingDeathEvent`. Step 5A does not redesign
kill dispatch or introduce death prevention.

## Step 5B stack and proc enchantment slice

Bleed, Luck, Poison, and Pummel are real Minecraft dynamic-registry enchantments and remain absent from vanilla
acquisition pools. Their Java behaviors compose through the existing proc and stack services; no behavior scripting
language or enchant-specific event adapter was added. Bleed, Poison, and Pummel listen only to committed damaging
melee hits and opt into `LIMITED_OFFENSIVE_REROLL`, so a Doublestrike child may reroll them while Doublestrike remains
excluded from its own child.

The canonical `cosmicpve:bleed` stack definition is negative, independently timed, capped at 10 units, and lasts 100
server ticks. Each unit preserves source/credited-player attribution and ticks for 1 HP of standard Cosmic true damage
at 30, 60, and 90 ticks after its own application. The bridge expires due units before running effects, preventing a
tick at or after expiry. Coincident units are delivered separately through `ChildCombatActionService` with
`NO_PROCS`; the true-damage type bypasses hurt cooldown and armor/custom reductions but still consumes absorption.
One transient movement-speed modifier represents the authoritative active count at -1 percent per unit and is removed
after expiry, cleanse/removal, or a clone that does not retain Bleed.

Luck is a generic `ProcModifierResolver` source rather than candidate-specific behavior. Actual Luck levels on boots
and leggings are summed and contribute `1 + 0.01 * totalLevel` to the event's central relative chance multiplier.
That means total Luck XX changes 1 percent to 1.2 percent and 20 percent to 24 percent for all probabilistic candidates,
including Angelic, Lightning, Doublestrike, Bleed, Poison, and Pummel. Execute remains deterministic outgoing damage
math and never enters this modifier path. Named modifier values are included in proc traces.

Poison applies vanilla Poison I for 60 ticks at 5 percent per level. Pummel applies vanilla Slowness III for 50 ticks
at 2 percent per level. Stack runtime generalization, additional stack effects, books, loot acquisition, GUIs, armor
sets, masks, skins, instances, and all later enchantments remain deferred.
