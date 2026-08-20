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
