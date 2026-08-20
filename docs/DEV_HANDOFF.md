# CosmicPVE Development Handoff

Current verified and accepted gameplay commit: `7530ab293610a8ea6c1a02b396df9539454b36bb` (`Add Aegis Rage Eagle Eye and Ancient set`). This is the accepted Steps 6A–6D baseline.

Repository state after Step 6D closure: clean at gameplay commit `7530ab293610a8ea6c1a02b396df9539454b36bb`, apart from the deliberately untracked root `assets` folder containing user-supplied Step 6E source artwork and this documentation follow-up.

Last handoff update: 2026-08-20

Pinned environment:

- Minecraft 1.21.11
- NeoForge 21.11.45
- Java 21
- Gradle 9.2.1
- ModDevGradle 2.0.144
- Mod ID: `cosmicpve`
- Java package: `com.cosmicpve`

Repository: `C:\Dev\cosmicpve-template-1.21.11`

Primary project style:

- PvE-first
- singleplayer / 1–4 player small co-op
- designer/tester-led
- Codex handles most implementation
- one deliberately bounded milestone at a time

## Source Priority

Use project sources in this order:

1. `docs/DEV_HANDOFF.md` — current verified implementation, recently settled canonical decisions, immediate next work, and do-not-assume items.
2. `docs/Cosmic_Design.md` — gameplay vision and intended content.
3. `docs/ARCHITECTURE.md` — architectural principles, ownership, and system boundaries.
4. `docs/COMBAT_PROTOTYPE.md` — detailed verified combat implementation behavior.
5. Conversational or persistent AI memory — supplemental context only; never authoritative over repository documentation.

If sources conflict, surface the conflict. Do not silently select a convenient interpretation unless this handoff explicitly records a newer canonical decision.

## 1. Project Vision

CosmicPVE adapts nostalgic CosmicPvP-style gear progression and risk into a primarily PvE survival experience. Its intended loop combines custom enchantments, a risky item-upgrade economy, armor sets, masks and weapon skins, and increasingly demanding Trials, Dungeons, and scheduled Invasions. Progression occurs in a survival world and is eventually intended to support one player or a cooperative party of up to four.

## 2. Milestone Ledger

### Step 1 — COMPLETE: Foundation Cleanup and Registration

Implemented the real `com.cosmicpve` package and `cosmicpve` namespace, common/client entry points, dedicated-server-safe client isolation, and registration modules for items, data components, attachments, networking, and dynamic-registry enchantment keys. Removed the generated example-mod identity without changing pinned toolchain versions.

### Step 2A — COMPLETE: Content Repository Foundation

Implemented a reloadable, namespaced, codec-driven content repository. Candidate definitions are decoded and validated before an immutable snapshot is published atomically; failed reloads do not partially replace active content. Current definition families are `ScalingProfile`, `StackDefinition`, and `ArmorSetDefinition`. Development stack fixtures use explicit `development_*` IDs rather than pretending to be canonical gameplay content.

### Step 3A — COMPLETE: Combat Kernel and Legacy Combat Prototype

Implemented combat contexts, attribution, sequences, a centralized ordinary-damage calculation pipeline, named trace contributions, and the legacy-melee prototype. Vanilla swords and axes have approximately 20 total attack speed and zero minimum attack charge, sweeping is disabled, and vanilla crit/sprint behavior and target hurt immunity remain intact.

### Step 3B — COMPLETE: Combat Primitives

Implemented standard/explicit true-damage packets, scoped delivery, child combat sequences, recursion policies, effective actual-plus-virtual enchantment resolution, and terminal execution. True damage and execution are deliberately distinct operations.

### Step 4A — COMPLETE: ProcEngine and CooldownService

Implemented centralized server-authoritative proc evaluation, once-per-event claims, conditions, recursion filtering, relative chance modifiers, trace output, and cooldown activation. `CooldownService` stores server-tick expiries and applies duration multipliers centrally.

### Step 4B — COMPLETE: Runtime CombatStackService

Implemented generalized LivingEntity stack containers, all four refresh policies, expiration, cleanse, deterministic transfer/steal, polarity, source attribution, persistence scopes, and player-clone behavior. Runtime mutation is centralized in `CombatStackService`.

### Step 5A — COMPLETE: First Real Cosmic Enchantment Slice

Implemented real Minecraft enchantments and behavior for Execute, Angelic, Lightning, Ender Shift, and Doublestrike. They are intentionally absent from normal vanilla acquisition pools. Doublestrike proved linked child-hit delivery and limited peer-proc rerolls.

### Step 5B — COMPLETE: Stack and Proc Enchantment Slice

Implemented Bleed, Luck, Poison, and Pummel as real enchantments. Bleed uses the generalized stack system; Luck contributes a generic relative proc modifier. These additions brought the implemented total to nine at this milestone.

### Armor Set Foundation — COMPLETE

Implemented reloadable armor-set definitions, four-piece LivingEntity resolution, combat/proc/immunity contribution seams, equipment coloring/presentation, and the real Phantom and Yeti sets. Identity is typed data on ordinary vanilla armor and works across mixed armor materials.

### Armor Set Crystals — COMPLETE

Implemented one generic typed crystal item with real server-authoritative drag/drop, atomic success/failure handling, identity application, concise feedback, and White Scroll interception. Crystals use a Nether Star model, are non-stackable, and force the enchanted shimmer without containing fake enchantments.

### Step 6A — COMPLETE: Real Cosmic Enchanting Loop

Implemented generic typed Cosmic Enchantment Books, actual Minecraft enchantment application/upgrading, success-then-destroy resolution, Mastery rate constraints for future content, base five-slot Cosmic capacity, White Scroll protection, equipment tooltips, permission-gated give commands, and safe inventory interception. Book-applied enchantments use the same gameplay path as `/enchant`.

### Step 6B — COMPLETE; MANUALLY VERIFIED AND ACCEPTED

Implemented Cosmic Book sound feedback, Transmog Scrolls, Armor Enchantment Orbs, Weapon Enchantment Orbs, persistent capacity upgrades, Orb destruction/White Scroll semantics, commands, models, tooltips, and focused tests. The final Step 6B build passed, all 107 tests passed, the dedicated server reached ready, and the client completed resource/model loading. The user also manually verified the important in-world inventory, drag/drop, tooltip, visual, capacity, and audio interactions and accepted the milestone.

### Step 6C — COMPLETE; MANUALLY VERIFIED AND ACCEPTED

Implemented typed Black Scrolls and the real Greatsword, Insanity, and Venom enchantments. A valid Black Scroll always extracts one uniformly selected eligible actual Simple-through-Legendary Cosmic enchantment; its displayed percentage becomes the returned book's Success Rate, while Destroy Rate is rolled independently from 1–100. Mastery, vanilla, and virtual enchantments are excluded. The full build and all 115 tests passed, the dedicated server reached ready, and the client completed resource/model loading. The user manually verified all four additions and accepted the milestone.

### Step 6D — COMPLETE; MANUALLY VERIFIED AND ACCEPTED

Implemented real Aegis, Eagle Eye, and Rage enchantments plus the Ancient armor set. Aegis uses the generalized ordinary pre-defense bounds seam; Rage uses bounded recent committed-combat memory keyed by damaging and damaged entity UUID; Ancient uses its reloadable set definition plus a small health-dependent Java behavior module. The full build and all 124 tests passed, the dedicated server published all three armor sets and reached ready, and the client completed resource loading. The user manually verified Aegis, Eagle Eye, Rage, and Ancient in-world and accepted the milestone.

## 3. Current Real Enchantments

These are registered through Minecraft's enchantment infrastructure, use actual enchantment data on the item, and are not present in normal enchanting-table, librarian, random-loot, random-equipment, or mob-equipment acquisition pools.

### IMPLEMENTED

| ID | Max | Valid equipment | Rarity | Current canonical behavior |
|---|---:|---|---|---|
| `cosmicpve:execute` | V | Sword | Elite | Adds 2% ordinary outgoing damage per level when the target is strictly below 50% maximum health. Uses the shared additive outgoing bucket. |
| `cosmicpve:angelic` | V | Any armor | Ultimate | On committed damage taken, has 1% per total equipped Angelic level to heal 1 HP. Levels across armor pieces aggregate into one roll and at most one heal per damage event. |
| `cosmicpve:lightning` | IV | Bow/crossbow | Simple | On a committed projectile hit, has 5% per level to create a visual-only lightning bolt and deliver exactly 2 HP standard Cosmic true damage with `NO_PROCS`. |
| `cosmicpve:ender_shift` | III | Helmet | Unique | After committed damage leaves the wearer alive and strictly below 25% health, grants Speed I and Regeneration I for 3 seconds per level. Base cooldown is 600 ticks/30 seconds. |
| `cosmicpve:doublestrike` | III | Sword | Legendary | On a committed hit, has 1% per level to deliver a linked child strike for 50% of the parent's finalized ordinary pre-vanilla-mitigation damage. It cannot reroll itself; eligible peer offensive procs may reroll. |
| `cosmicpve:bleed` | VI | Axe | Ultimate | On committed melee damage, has 1% per level to add one independently timed Bleed stack. See the canonical stack rules below. |
| `cosmicpve:luck` | X | Boots/leggings | Ultimate | Each total equipped level multiplies eligible proc chances by 1.01 relative to base. Levels on boots and leggings add. |
| `cosmicpve:poison` | III | Sword | Elite | On committed melee damage, has 5% per level to apply Poison I for 60 ticks/3 seconds. |
| `cosmicpve:pummel` | III | Axe | Elite | On committed melee damage, has 2% per level to apply Slowness III for 50 ticks/2.5 seconds. |
| `cosmicpve:greatsword` | IV | Sword | Elite | At an inclusive attacker-to-target entity distance of 2.5 blocks or farther, adds 5% ordinary outgoing damage per level in the shared additive bucket. |
| `cosmicpve:insanity` | VIII | Axe | Legendary | Adds 1% ordinary outgoing damage per missing heart, including fractional hearts, capped at 2% per level in the shared additive bucket. |
| `cosmicpve:venom` | III | Bow/crossbow | Elite | On a committed positive red-health projectile hit, has 15% per level to apply Poison I for 60 ticks/3 seconds through the shared proc system. |
| `cosmicpve:aegis` | VI | Chestplate | Legendary | Caps the outgoing-finalized ordinary attack component at `14 - level` HP in the pre-defense bounds stage. Aegis VI caps at 8 HP; later incoming/vanilla mitigation still applies, while true damage and execution bypass it. |
| `cosmicpve:eagle_eye` | VI | Bow/crossbow | Ultimate | At an inclusive attacker-to-target entity distance of 18 blocks or farther, adds 3% ordinary outgoing damage per level in the shared additive bucket. |
| `cosmicpve:rage` | VI | Sword/axe | Legendary | Adds exactly 5% ordinary outgoing damage when this exact target damaged the attacker at least three committed positive-red-health times within the rolling `(4 + level)`-second window. History is not consumed. |

### DESIGNED BUT NOT IMPLEMENTED

The design also names Armored, Auto Smelt, Death Pact, Divine Immolation, Experience, Gears, Glowing, Hero Killer, Molten, Mortal Coil, Nutrition, Obsidianshield, Oxygenate, Pheonix/Phoenix, Self Destruct, and Virus. None currently has a registered real enchantment or runtime behavior. There are exactly 15 implemented real Cosmic enchantments. No Mastery enchantment is implemented. Do not add behavior assumptions beyond `Cosmic_Design.md`; the `Pheonix`/`Phoenix` spelling should be settled before choosing its stable ID.

## 4. Critical Combat Semantics

### Legacy melee

- Vanilla wooden through netherite (including copper) swords and axes target approximately 20 total attack speed.
- Minimum attack charge is zero, so there is no meaningful modern attack-charge delay.
- Vanilla sweeping is disabled.
- Normal critical hits and sprint-hit knockback remain.
- Vanilla target hurt/damage immunity remains authoritative. Never globally read, clear, shorten, or restore `invulnerableTime` to simulate old combat.

### Valid proc hit

Ordinary hit procs begin only after the logical server commits positive red-health damage in `LivingDamageEvent.Post`. Raw clicks, candidates rejected by target immunity, canceled attacks, zero-damage events, and absorption-only events are not normal proc hits. Execution never enters the proc engine.

The same commit boundary records bounded recent-combat memory for Rage. History is keyed by the responsible living attacker's UUID and damaged entity's UUID, so credited projectile owners and attributed child attacks follow normal combat attribution rather than using projectile identity. Queries count timestamps in an inclusive rolling server-tick window and do not consume them. Periodic pruning removes stale relationships, and tick-timeline regression clears memory between server lifecycles.

### Damage composition

- Ordinary custom outgoing percentage bonuses normally share one additive outgoing bucket unless an effect explicitly declares a separate multiplier.
- Separate incoming percentage systems multiply. For example, two 25% reductions produce `0.75 × 0.75`, not a single 50% reduction.
- True-damage packets remain separate from ordinary damage and are not implicitly altered by ordinary-only bonuses or caps.

### Standard Cosmic true damage

Standard true damage bypasses armor and Protection-style mitigation, shields, normal target hurt immunity, ordinary Cosmic incoming reduction, and Aegis/ordinary caps. It **does not bypass absorption by default**: absorption is consumed before red health. Individual packets have explicit bypass flags; do not assume every future packet uses the standard combination.

### Execution

Environmental or encounter execution is not a large damage value. `ExecutionService` sets health to zero and invokes death without calling `hurt`, producing a damage container, consuming absorption, checking mitigation, or allowing totems/future Phoenix-style prevention. It is intended for terminal instant-failure mechanics such as falling through an encounter kill plane. Future death-prevention hooks must exit when `ExecutionService.isExecuting(target)` is true.

### Doublestrike

Doublestrike takes 50% of the parent's `finalOrdinaryDamage`: the ordinary value after Cosmic outgoing/incoming calculation but before vanilla target mitigation. It does not automatically copy separately delivered Lightning, Bleed, or other true-damage packets. It creates a unique child sequence linked to the parent with `LIMITED_OFFENSIVE_REROLL`, explicitly excludes Doublestrike, and uses a narrowly validated internal damage type that bypasses only the hurt cooldown created by its parent. Other attacks do not gain this bypass. Eligible peer offensive effects such as Bleed, Poison, and Pummel may reroll on the child.

## 5. Proc and Cooldown Rules

`ProcEngine` is server-authoritative and owns condition checks, recursion filtering, once-per-event claims, cooldown checks, relative chance calculation, the single candidate roll, cooldown activation, action execution, and tracing. Each eligible candidate consumes at most one roll for an event; filtered, rejected, or cooldown-blocked candidates consume none.

Chance modifiers are relative multipliers, clamped to a valid probability. Recursion policies are:

- `NORMAL`: ordinary eligible proc evaluation.
- `NO_PROCS`: no candidates may activate.
- `LIMITED_OFFENSIVE_REROLL`: only explicitly opted-in candidates may reroll, and named excluded effects remain blocked.

`CooldownService` stores absolute server-tick expiry by entity UUID and stable cooldown key. Effective duration is calculated once at activation as the base ticks multiplied by all duration multipliers (rounded up). A 20% cooldown reduction therefore multiplies duration by `0.8`.

Luck is a generic proc modifier, not special logic inside each enchantment. One Luck level means `×1.01`; twenty combined Luck levels mean `×1.20`. Thus a 1% chance becomes 1.2%, and a 20% chance becomes 24%.

## 6. Stack Rules

Every definition is classified `POSITIVE` or `NEGATIVE`. `CombatStackService` is the only mutation boundary and supports add, remove, expiration, polarity cleanse, deterministic eligible-positive transfer/steal, source attribution, and persistent/ephemeral scopes. Definitions select `INDEPENDENT`, `REFRESH_ALL`, `REFRESH_ONE`, or `FIXED` refresh behavior.

Canonical Bleed behavior:

- `NEGATIVE`
- maximum 10 stacks
- every stack independently lasts exactly 100 ticks/5 seconds
- adding a stack never refreshes older stacks
- each active stack contributes -1% movement speed
- each individual stack deals 1 HP standard Cosmic true damage at ages +30, +60, and +90 ticks
- no tick occurs at or after expiration
- original source and credited-player attribution are retained
- periodic delivery uses `NO_PROCS`
- absorption still applies because Bleed uses standard Cosmic true damage

Feeding Frenzy and Hysteria are designed but not implemented. The existing `development_positive` stack is only a content-loader/runtime fixture and is not Feeding Frenzy. Canonical future Hysteria should redirect an otherwise valid hit into equal ordinary self-damage, cancel original target damage, and use `NO_PROCS`; this remains unimplemented.

## 7. Armor Set Rules

An entity has at most one active armor-set bonus. Head, chest, legs, and feet must all be eligible armor with the same `ARMOR_SET_ID`; mixed identities give no effect and there are no partial bonuses. Underlying materials may be mixed. Identity is synchronized, codec-backed data on the existing vanilla armor stack, not a parallel armor item, so names, trims, durability, enchantments, and other components remain intact.

Implemented sets:

- **Phantom** (`cosmicpve:phantom`, `#FF6969`): +25% additive ordinary outgoing damage, ×1.10 incoming damage, and a `×1.25` chance modifier for probabilistic Mastery procs. No real Mastery proc exists yet, so the final behavior is currently a tested integration seam.
- **Yeti** (`cosmicpve:yeti`, `#A3FFF5`): +10% additive ordinary outgoing damage, ×0.90 incoming damage, and immunity IDs for freeze, frozen, permafrost, and ice aspect. Current vanilla integration clears freezing for players and mobs wearing the full set; the custom named effects do not exist yet.
- **Ancient** (`cosmicpve:ancient`, `#050C59`): at or above exactly 50% health, +7.5% additive ordinary outgoing damage and ×0.925 ordinary incoming damage. Strictly below 50%, these become +15% and ×0.85. Outgoing reads attacker health at calculation; incoming reads target health before the current hit, so a threshold-crossing hit affects only later events. Standard true damage bypasses the set's ordinary reduction.

There are exactly three implemented armor sets. Dimensional Traveler, Engineer, Ranger, and Yjiki are designed but not implemented. Do not infer set behavior merely because the design table names it.

## 8. Item Application and Enchanting Economy

All custom inventory application is server-authoritative. Primary-clicking a recognized application item onto potential equipment enters a custom path; empty slots and unrelated non-equipment retain ordinary vanilla placement/swap behavior. The server revalidates cursor item, typed data, target identity/category, and stale slot state before mutation. Deliberate drag/drop is sufficient confirmation; no extra dialog is desired.

### Armor Set Crystals

- One generic item with versioned per-stack armor-set identity and Success Rate.
- Nether Star appearance, maximum stack size one, and forced vanilla enchanted shimmer without fake enchantment data.
- Success ranges from 1–100. A 100% outcome consumes no RNG.
- A valid failed application is currently always destructive; there is no separately stored crystal Destroy Rate.
- Success sets identity on the existing armor stack in place.
- Armor already carrying a set identity rejects another crystal.
- White Scroll protection intercepts destructive crystal failure and is consumed.

### Cosmic Enchantment Books

- One generic, non-stackable, glinting book item with versioned enchantment ID, level, Success Rate, and Destroy Rate.
- It resolves the actual registered Cosmic enchantment and writes through Minecraft's enchantment component; there is no parallel book-applied enchantment store.
- Ordinary tier rates are independently 1–100 Success and 1–100 Destroy. Future Mastery books are constrained by tier metadata to 1–49 Success and 51–100 Destroy.
- Validation and capacity checks happen before consumption or rolls.
- Success is resolved first. On success, no destroy roll occurs. On failure, the book is consumed and only then is destruction resolved.
- Deterministic 100% outcomes do not consume unnecessary RNG.
- Book level above current applies that level on success. Equal level applies exactly current+1 when below the real maximum. Lower level and equal-at-maximum reject without consumption or rolls.
- Successful application preserves the existing stack and unrelated data.

Book feedback is authoritative and exactly once: success plays `PLAYER_LEVELUP`; every rolled failure plays `LAVA_AMBIENT`; actual destruction additionally plays `ANVIL_DESTROY`. White Scroll protection prevents the anvil-destroy cue because the item survives. Rejections are silent.

### White Scroll

White Scroll protection is a persistent boolean in `CUSTOM_ENCHANT_META`. It is one-time protection against item destruction, not a chance modifier. It remains after success or a naturally non-destructive failure and is consumed only when it prevents an otherwise destructive outcome. It currently protects Cosmic Book failures, Armor Set Crystal failures, and Armor/Weapon Orb failures.

### Black Scroll

- One generic, non-stackable item carries versioned typed data containing a returned-book Success Rate from 1–100.
- The displayed percentage is not an extraction chance: every valid application extracts exactly one enchantment.
- Candidates are actual Minecraft enchantments whose registered Cosmic tier is Simple, Unique, Elite, Ultimate, or Legendary. Vanilla enchantments, Mastery enchantments, and virtual grants are never candidates.
- Eligible IDs are sorted deterministically, then one is selected uniformly when more than one exists. A single candidate consumes no selection RNG.
- The selected exact ID and level are removed in place. The cursor's consumed Black Scroll is atomically replaced with a real generic Cosmic Book using the stored Success Rate and an independently rolled 1–100 Destroy Rate, so a full inventory cannot lose or duplicate the output.
- The target stack is not reconstructed; all unrelated enchantments, durability, name, armor-set identity, White Scroll protection, Orb upgrades, Transmog state, and other components remain.
- Invalid or stale applications consume nothing, mutate nothing, create no output, and consume no RNG. Black Scroll extraction has no destructive result, so White Scroll protection is irrelevant.

### Custom enchant capacity and Orbs

- Base capacity is five distinct **actual** Cosmic enchantment IDs.
- Vanilla enchantments and virtual grants do not count.
- Level upgrades of an existing Cosmic identity consume no extra slot.
- `CustomEnchantCapacityService` is the sole source of truth.
- Persistent `CUSTOM_ENCHANT_META.orbUpgrades` stores a per-item bonus rather than an absolute final cap.
- Armor Orbs apply only to armor and permit +3 total bonus: effective capacity 5–8.
- Weapon Orbs apply only to swords, axes, bows, and crossbows and permit +5 total bonus: effective capacity 5–10. Pickaxes and other ordinary tools are currently excluded.
- Both Orb items are non-stackable, unglinting Eye of Ender presentations with versioned per-stack 1–100 Success and 1–100 Destroy rates.
- Orb transactions use the same success-first/destruction-second deterministic resolver and White Scroll interception as Books. Success increases the bonus by exactly one. Targets at their maximum reject before consumption or RNG.

The equipment tooltip reads used and effective capacity from the centralized service, including zero-used eligible armor/weapons.

## 9. Presentation Rules

Cosmic tiers are the sole color source; do not create per-enchantment color maps:

- Mastery `#AA0000`
- Legendary `#FFAA00`
- Ultimate `#FFFF55`
- Elite `#A3FFF5`
- Unique `#55FF55`
- Simple `#AAAAAA`

Cosmic Book tint/name and actual enchantment tooltip lines resolve `CosmicEnchantmentTier`. Success lines use `#4DFF74`; Destroy lines use `#C92C2C`. Phantom uses `#FF6969`; Yeti uses `#A3FFF5`.

Transmog Scrolls are ordinary paper presentations. Applying one sets persistent `CUSTOM_ENCHANT_META.transmogSorted=true` and changes presentation only: it does not rewrite, remove, re-add, or level enchantments. Future enchantments automatically participate because sorting occurs while rendering the tooltip.

Transmog order is exactly:

1. Vanilla enchantments
2. Mastery
3. Legendary
4. Ultimate
5. Elite
6. Unique
7. Simple

Vanilla lines retain their existing relative order and original Components/styles. Within one Cosmic rarity, higher level appears first; equal levels use alphabetical stable namespaced ID. Items without the flag retain their pre-Transmog order.

## 10. Instance Architecture

No instance runtime is implemented yet. The canonical architecture reserves a dedicated instance dimension shared by Trials, Dungeons, and Invasions. Entry must save the player's original dimension, position, yaw, and pitch. Instance terrain is protected by default against breaking, placement, explosions, pistons, fluids, fire, griefing, and other bypasses; rooms grant narrow cause/tag-specific exceptions.

The target party size is 1–4. Initial implementation deliberately supports only **one active gameplay instance session per world/server** using one fixed arena abstraction. Session IDs and placement interfaces must remain so concurrency can be added later, but arena leasing, concurrent-cell allocation, quarantine systems, and simultaneous instance parties are deferred.

Inventory and return recovery must be durable and idempotent across disconnects/crashes. Do not force a global save on every transition; prototype the precise reliable persistence boundary before trusting inventory clearing. Environmental encounter failures use terminal execution, not ordinary or true damage.

## 11. Activity Identities

- **Trials:** the most accessible activity, with very high mastery ceiling and harsh learning failures. Parties progress through rooms while building a pot; cash-out is individual. A player who cashes out receives a full copy of the current pot and leaves while remaining players may continue. Skip N advances phase/room progression and adds N corresponding loot rolls without consuming time or awarding skipped-room time bonuses.
- **Dungeons:** high entry cost, intended to require keys/portals, high mastery ceiling, strong average loot, fixed challenge sequences, and a harder Heroic variant. Exact key/portal terminology and acquisition still require a future content specification.
- **Invasions:** scheduled cooperative events with medium accessibility, comparatively lower mechanical mastery ceiling, stronger baseline preparation requirements, and extraordinary loot potential through boss contribution. Invasions have keep-inventory **off**. Implement their death and inventory behavior through the shared activity/session architecture rather than changing the global `keepInventory` gamerule; the exact activity-specific implementation is deferred to the Invasion milestone.

This Invasion rule supersedes older design text that described Invasions as keep-inventory activities. Dungeon death/inventory behavior remains subject to its own bounded implementation specification.

## 12. Major Designed Content Not Yet Implemented

The following remain future content or infrastructure:

- all enchantments beyond the 15 listed above, including every Mastery enchantment
- Dimensional Traveler, Engineer, Ranger, and Yjiki armor sets
- masks and Multi-Masks
- weapon skins and their virtual/passive behaviors. The settled inventory UX is drag and left-click to apply a skin to an appropriate item; the skin becomes tied to that item and a simple right-click removes it. This supersedes older design text that reverses those gestures.
- Feeding Frenzy and Hysteria runtime behavior
- Heroic equipment/portals and repair semantics
- armor-set signature weapons
- G-Kits, M-Kits, unlocks, and refreshers
- Trials, Dungeons, Invasions, bosses, rooms, portals, keys, scaling runtime, protection, and recovery
- loot generation, lootbags, production acquisition tables, spawners, and contribution rewards
- Cosmic Crates, Memory Chests, Secret Weapon Caches, and seasonal utility blocks
- final enchanting/loot GUIs; confirmation dialogs are not desired for ordinary deliberate item application

## 13. Cosmic Crates

Cosmic Crates are designed but absent from current code. The design includes four seasonal variants—Spring, Summer, Fall, and Winter—along with substantial draft reward tables and mechanics. Each crate is completed from its matching Left Half plus Right Half. A Memory Chest yields a random half from any seasonal crate.

Each seasonal crate is intended to contain an apex reward, two seasonal utility custom blocks, two seasonal weapon skins, strong general loot, and potentially a seasonal Mastery enchantment. A Secret Weapon Cache yields a random signature armor-set weapon; a signature weapon gains +5% damage while its matching full armor-set bonus is active. The existing tables are legitimate design input, but prerequisites remain unimplemented and their exact balance, rewards, rates, and bespoke mechanics are not frozen. Reconcile and validate them in a bounded crate milestone rather than treating every draft value as final or inventing replacements.

## 14. Utility and Debug Commands

The following are confirmed in current source and permission-gated under `/cosmic` unless noted:

- `/cosmic combat trace on|off|last`
- `/cosmic combat true-damage <target> <amount>`
- `/cosmic combat set-health <target> <amount>`
- `/cosmic combat execute <target>`
- `/cosmic proc trace on|off|last`
- `/cosmic proc test hit|no-procs`
- `/cosmic cooldown list`
- `/cosmic cooldown set <key> <ticks>`
- `/cosmic cooldown clear <key|all>`
- `/cosmic stack add <target> <stack-id> [count]`
- `/cosmic stack remove <target> <stack-id> <count|all>`
- `/cosmic stack list <target>`
- `/cosmic stack cleanse <target> <positive|negative>`
- `/cosmic stack steal <from> <to>`
- `/cosmic armor crystal give <player> <set-id> <success-rate>`
- `/cosmic enchant book give <player> <enchantment-id> <level> <success> <destroy>`
- `/cosmic enchant book give-random <player> <enchantment-id> <level>`
- `/cosmic enchant black-scroll give <player> <returned-success>`
- `/cosmic enchant white-scroll give <player> [count]`
- `/cosmic enchant transmog give <player> [count]`
- `/cosmic enchant orb give <player> <armor|weapon> <success> <destroy>`
- `/cosmic enchant orb give-random <player> <armor|weapon>`
- `/feed`, `/heal`, and `/restore` are separate permission-gated development convenience commands.

Commands call gameplay services or create the same typed components used by gameplay; production loot acquisition remains deferred.

## 15. Unresolved / Do Not Assume

- **Portal rules:** placement validation, owner/party authority, protected footprint, expiry, chunk-loading, collision, joining, and safe fallback return behavior require a bounded specification.
- **Trial scaling after departures:** architecture freezes initial party size so boss maximum health does not shrink, but detailed remaining objective/mob scaling and late-disconnect behavior still need acceptance criteria.
- **Crash-safe Trial inventory persistence:** the exact NeoForge durability boundary must be prototyped. Marking data dirty is not proof of a crash-durable snapshot, and global saves on every transition are explicitly rejected.
- **Heroic conversion:** cosmetic leather/gold presentation, maximum/current durability adjustment, damage state, repair, and component preservation need exact rules and prototypes.
- **Loot tables:** several current design tables are incomplete or malformed and contain ambiguous duplicates. Validation must distinguish intentional duplicate weights from mistakes.
- **Attribute units/caps:** future health, movement, incoming damage, cooldown, and stacked modifier caps/floors need normalization before large content expansion.
- **Pheonix/Phoenix identity and death order:** settle the stable spelling/ID and verify pinned NeoForge kill/death-prevention event ordering before implementing it.
- **Activity key/portal terminology and acquisition:** Dungeons are intended as costly/keyed activities, but current design text primarily describes portals; reconcile this before item implementation.
- **Seasonal crate finalization:** substantial draft tables and mechanics exist, but their exact balance, rewards, acquisition rates, prerequisites, and implementation details remain subject to a bounded reconciliation milestone.

The final post-Orb capacity is no longer unresolved for current target classes: armor is 8 and swords/axes/bows/crossbows are 10. Do not reopen those limits incidentally during unrelated work.

## 16. Current Next Milestone

The next bounded milestone is Step 6E: the weapon-skin foundation with Boosted Chainsaw, Maui's Hook, Stormbringer, and the Black Scroll Ink Sac presentation cleanup. All other skins and adjacent content remain deferred.

## 17. Development Workflow

1. Define one bounded milestone with explicit deferrals and acceptance tests.
2. Codex reads this handoff and the specifically relevant design/architecture sources, then implements only that scope.
3. Run the full build and automated tests; verify dedicated-server and client startup when the change affects registration, networking, resources, or sided loading.
4. The user manually tests important gameplay, inventory, sound, tooltip, model, and visual interactions that automation cannot prove.
5. Fix defects before expanding scope.
6. Commit the successfully verified milestone.
7. Update `docs/DEV_HANDOFF.md`, including commit/tree state and next milestone.
8. Only then begin the next milestone.

Preserve unrelated dirty changes. Never change pinned versions as a side effect of gameplay work. Prefer service-level rules and typed data over one-off event logic, and keep client-only classes isolated from dedicated-server loading.

## 18. Verification Snapshot

For the current uncommitted Step 6D implementation:

- `gradlew.bat build` succeeded.
- 124 automated tests passed with 0 failures and 0 errors.
- Dedicated-server startup published three armor sets, including `cosmicpve:ancient`, and reached `Done`.
- Client startup completed mod/resource loading and item-atlas creation without CosmicPVE model/resource errors.
- Changed JSON resources decoded successfully; Aegis, Eagle Eye, Rage, and Ancient resources loaded at runtime.
- Pinned environment values remained unchanged.
- Step 6C remains manually accepted at gameplay commit `0e8a21c93fa13760f6a3424e1c7ebc526cdab564`.
- Manual in-world Step 6D verification and acceptance remain pending.

This snapshot describes the current uncommitted working tree layered on accepted Step 6C gameplay commit `0e8a21c93fa13760f6a3424e1c7ebc526cdab564` and documentation closure commit `a7c49917e37c620db4fddd9a7c13852dd7f099bf`. Do not mark Step 6D accepted or assign it a commit hash until manual acceptance and commit actually occur.

## Maintaining This Handoff

`DEV_HANDOFF.md` is a living current-state document, not a historical changelog.

After **every** successfully verified major milestone:

- update the milestone ledger;
- update current implemented systems and content;
- incorporate newly settled canonical semantics;
- move resolved items out of Unresolved;
- add newly discovered unresolved issues;
- update the Current Next Milestone;
- update the verified Git commit after the milestone has been committed;
- remove obsolete implementation descriptions rather than preserving contradictory historical versions.

Do not let this file grow indefinitely. Keep it concise, consolidate completed history, and never record speculative ideas as implemented facts. When a Codex implementation prompt includes “Update DEV_HANDOFF.md,” the update is part of required milestone completion, not optional cleanup.
