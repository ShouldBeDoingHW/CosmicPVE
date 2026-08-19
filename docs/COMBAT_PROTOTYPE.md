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
