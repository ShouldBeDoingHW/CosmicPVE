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
