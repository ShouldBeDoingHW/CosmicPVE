# CosmicPVE Proposed Technical Architecture

## 1. Scope and current project state

This document proposes the technical foundation for the design in `Cosmic_Design.md`. It intentionally does not specify every room, boss, item, or enchantment as a separate implementation. The goal is to establish reusable systems that later content can configure or extend.

The repository is currently a nearly unchanged NeoForge MDK. The only Java sources are the example common mod class, example client class, and example config class. There is no existing gameplay architecture to preserve. Before gameplay work begins, the example package (`com.example.examplemod`), example registrations, and example configuration should be replaced with a real `cosmicpve` package structure in a dedicated cleanup change. The Minecraft 1.21.11, NeoForge 21.11.45, Java 21, Gradle 9.2.1, and ModDevGradle 2.0.144 versions should remain unchanged.

## 2. Architectural principles

1. **Server authoritative.** Damage, procs, cooldowns, instance transitions, inventories, rewards, and persistent progression are computed on the logical server. The client receives only presentation state and UI payloads.
2. **Prefer composition.** Enchantments, armor sets, masks, skins, encounters, and rooms contribute behavior through typed hooks and modifiers rather than large inheritance trees.
3. **Separate definitions from runtime state.** Definitions are registry/datapack content; mutable state belongs on item data components, entity attachments, or world saved data.
4. **One combat path.** Melee, projectiles, damage-over-time, pets, bosses, and enchantment-triggered damage enter a shared combat pipeline with explicit attribution and recursion rules.
5. **One instance lifecycle.** Dungeons, Trials, and Invasions are definitions or modes hosted by a common instance engine.
6. **Transactional player safety.** Moving a player into an instance is a durable transaction. The outside inventory and return location are committed before the instance inventory is changed.
7. **Stable identifiers.** Persistent data stores namespaced IDs, UUIDs, and schema versions—not Java class names or registry ordinals.
8. **Data-driven where it is genuinely useful.** Values, tables, scaling curves, room sequences/pools, and effect composition should be data-driven. Complex bespoke mechanics remain small Java behavior types referenced by data.

## 3. Suggested source layout

Use packages similar to the following. Names are illustrative, but the ownership boundaries are important.

```text
com.cosmicpve
  CosmicPVE
  registry/          ModItems, ModDataComponents, ModAttachments, ModEnchantments, ...
  content/           codecs, reloadable definitions, validation, definition registries
  item/              books, scrolls, crystals, portals, lootbags, interaction services
  combat/
    api/             CombatContext, CombatResult, CombatContributor, CombatTags
    pipeline/        CombatEngine and ordered calculation stages
    proc/            ProcEngine, ProcDefinition, ProcState, CooldownService
    stack/           CombatStackService, StackDefinition, StackInstance
    effect/          reusable effect/action implementations
    event/           thin NeoForge event adapters only
  equipment/         set identity/resolution, masks, skins, effective enchantments
  persistence/       player profile, world state, codecs, migrations
  instance/
    api/             InstanceDefinition, RoomDefinition, EncounterDefinition
    runtime/         InstanceManager, InstanceSession, ParticipantState
    inventory/       InventorySnapshot, InventoryTransactionService
    protection/      InstanceProtectionService, interaction policies
    encounter/       EncounterController, objectives, boss ledger, spawners
    mode/            DungeonMode, TrialMode, InvasionMode
  loot/              weighted tables, loot context, rewards, lootbags
  network/           custom payloads and client synchronization
  command/           development/admin commands
  client/            renderers, models, overlays, screens; never loaded server-side
  gametest/          focused GameTests and test fixtures
```

Registration classes should only register objects. NeoForge event subscribers should translate events into domain calls; they should not contain damage formulas, encounter rules, or persistence logic.

## 4. Content definition layer

Introduce a validated content repository loaded from datapack JSON. It should expose immutable definitions by namespaced ID and reject invalid references during reload. A small Java codec should exist for each definition type:

- `EnchantBehaviorDefinition`: proc hooks, chance expression, cooldown group, conditions, and effect actions.
- `ArmorSetDefinition`: four-piece identity, outgoing/incoming modifiers, conditional modifiers, immunities, and proc/cooldown modifiers.
- `MaskDefinition` and `WeaponSkinDefinition`: compatible slots/items, visuals, modifiers, hooks, and stack interactions.
- `StackDefinition`: polarity, maximum stacks, duration/refresh policy, tick interval, visibility, and effect actions.
- `InstanceDefinition`: mode, template/arena placement, timers, party scaling, protection policy, room graph or pool, encounters, and rewards.
- `EncounterDefinition`: actors, phases, objectives, controlled spawners, failure conditions, and transitions.
- `LootPoolDefinition`: independently weighted entries, quantities, conditions, and guaranteed groups.
- `ScalingProfile`: curves or explicit values for party sizes 1–4.

Definitions should reference a constrained registry of Java behavior/action types such as `deal_true_damage`, `apply_stack`, `heal`, `modify_damage`, `spawn_controlled_entity`, or a named bespoke boss action. Do not permit arbitrary class names in JSON. This layer is configuration and composition, not a general-purpose encounter scripting language. Complex Trial rooms, dungeon puzzles, and boss mechanics should remain small, testable Java behavior modules referenced by stable IDs from data; data supplies their tunable parameters and content references.

Reload validation should catch duplicate IDs, unknown references, invalid min/max values, unsupported item targets, empty loot pools, impossible room graphs, and missing scaling entries. Existing active sessions should hold a resolved definition revision or immutable snapshot so a datapack reload cannot mutate a run halfway through.

## 5. Item data, enchantments, masks, and skins

### 5.1 Item data components

Register typed, codec-backed data components for mutable per-stack state. Likely components are:

- `CUSTOM_ENCHANT_META`: custom enchant slot limit, orb upgrades, white-scroll protection, and format version.
- `BOOK_ROLL`: enchantment ID/level, success chance, destroy chance, and provenance.
- `ARMOR_SET_ID`: one armor-set ID. Crystals set this identity; the equipped four-piece resolver activates it.
- `HEROIC`: heroic status and any extra-durability bookkeeping required for safe application.
- `MASK_LOADOUT`: one to three unique mask IDs, stored on the helmet.
- `WEAPON_SKIN_ID`: one active skin ID on the existing weapon.
- `PORTAL_MODIFIERS`: heroic dungeon flag or Trial trinket values consumed into a portal/run.
- `LOOTBAG_CONTENT` or `LOOTBAG_SEED`: only if rewards are intentionally rolled before opening; otherwise roll authoritatively on open.
- `KIT_UNLOCK`/`REFRESHER_KIND` only where distinct item behavior cannot be represented by an item registry ID.

Use data components instead of custom item subclasses for state that must survive copying, saving, commands, recipes, and networking. Codecs must validate ranges and deduplicate mask IDs. Tooltip rendering reads these components but never decides gameplay.

### 5.2 Enchantment representation

Register custom enchantments through Minecraft's enchantment system so levels, applicability, commands, explicitly configured loot, and the standard enchantment component remain interoperable. Bespoke runtime behavior should be supplied by the CosmicPVE combat behavior registry keyed by enchantment ID. This allows a custom enchantment to be present on players, mobs, ordinary weapons, or armor without inventing a parallel enchantment container.

Cosmic enchantments must be excluded from normal vanilla acquisition pools by default: enchanting tables, ordinary random enchanted loot/equipment, and librarian trades must not select them. Acquisition is opt-in through Cosmic books, Cosmic loot tables, kits, explicitly configured mobs, and development commands. Any future vanilla acquisition must require an explicit data/configuration change rather than occurring merely because the enchantment is registered.

`EffectiveEnchantments` should produce one normalized view for an entity or attack source. It combines actual enchantments with virtual grants such as Boosted Chainsaw's Doublestrike III. It also records provenance, preventing a virtual grant from being written permanently to the item or counted twice.

Book, black-scroll, white-scroll, and orb application belongs in an `ItemModificationService`. It validates applicability and slot limits, performs exactly one server-side random roll, produces an auditable result, and replaces item stacks atomically. Destructive outcomes must honor white-scroll protection. Inventory gestures should send a request describing source and target slots; the server revalidates both stacks to prevent stale-slot exploits. The visible success/destroy chances and deliberate application gesture are sufficient confirmation; ordinary book application should not add another confirmation dialog.

### 5.3 Armor sets

`ArmorSetResolver` examines all four equipped armor stacks and returns either no active set or exactly one `ResolvedArmorSet`. It activates only when all four `ARMOR_SET_ID` values match and all four slots contain eligible armor. Cache the result on the entity but invalidate it whenever equipment changes.

Set effects contribute to the same combat and attribute systems as enchantments. `hasActiveArmorSet(target)` is therefore reusable by Hero Killer. Instance-aware conditions (for example, doubled Yjiki effects in dungeons) query `InstanceMembership`, not dimension names or coordinates.

### 5.4 Masks and weapon skins

Masks remain data on the helmet, so they do not replace an armor slot or set identity. `MaskLoadoutResolver` combines up to three distinct definitions and contributes attributes, periodic effects, and combat hooks. Multi-mask rendering uses the supplied question-mark texture while effects continue to use the contained IDs.

Weapon skins remain data on an existing weapon. `WeaponSkinResolver` contributes a cosmetic model key plus passive combat behaviors. Applying or removing a skin must preserve all unrelated components, including enchantments, damage, Heroic status, and custom slot metadata.

Client item-model selection should be based on synchronized item components. Do not replace an item solely to change its appearance.

## 6. LivingEntity combat architecture

### 6.1 Combat context and attribution

Every custom combat calculation uses a `CombatContext` containing at least:

- direct source and credited/root source;
- attacker and target `LivingEntity` when present;
- owning player UUID when attribution can be resolved;
- damage source/category, ordinary or true-damage channel;
- explicit execution classification for environmental/encounter kills;
- melee/projectile/DoT/environment flags;
- weapon snapshot and effective enchantments;
- instance/session and encounter IDs;
- attack sequence ID and parent sequence ID;
- recursion/proc policy (`NORMAL`, `NO_PROCS`, or an explicit limited reroll policy).

`DamageAttributionService` walks projectile owners, tame/summon ownership, and explicit custom-effect ownership. It supplies the invasion contribution ledger and avoids scattering `instanceof Player` checks throughout combat code.

### 6.2 Central damage pipeline

`CombatEngine` is the only place that combines custom outgoing and incoming modifiers. Its ordered stages should be documented and covered by tests:

1. Validate target, source, hit acceptance, and instance rules.
2. Capture the ordinary/base damage component.
3. Resolve attacker, equipment, instance, target state, and conditions.
4. Sum ordinary custom outgoing bonuses into one additive bucket.
5. Apply explicitly separate outgoing multipliers only when the definition says so.
6. Apply ordinary-only caps such as Aegis to the ordinary component.
7. Apply separate defensive percentage modifiers multiplicatively.
8. Apply other explicit caps/floors and final ordinary damage.
9. Resolve separately queued true-damage packets, which bypass only the defenses their definition declares.
10. Commit damage, attribution, and post-hit effects only if the hit actually dealt damage.

Environmental or encounter execution is a separate terminal operation, not a damage packet. `ExecutionService` marks the cause for logging/encounter state and kills the target through a path that bypasses Phoenix and every other death-prevention hook, armor, absorption, and damage reduction. It must not be representable as an unusually large ordinary or true-damage value because that would allow unrelated mitigation or revival behavior to intercept it.

Use an immutable calculation result containing a breakdown for debug inspection. Never call ordinary `hurt` recursively from an enchantment without a child context: Doublestrike, Bleed, lightning, Hysteria, and reflect effects otherwise create proc loops or double-count contribution.

The NeoForge incoming/final damage events should be thin integration points around this pipeline. Exact event ordering must be verified with focused prototypes before content is implemented, particularly for armor, absorption, invulnerability frames, death prevention, and vanilla critical hits.

### 6.3 Legacy/no-cooldown rules

Implement legacy combat as a dedicated `LegacyCombatService`, not per-weapon overrides:

- supply the intended attack-speed modifier to vanilla swords and axes through the appropriate global equipment/default-component mechanism;
- disable vanilla sweeping through the relevant attack/event path;
- retain critical-hit and sprint knockback behavior;
- consider a hit proc-eligible only after the server accepts it and it causes positive damage;
- never clear `invulnerableTime` globally merely to make spam clicking work.

Attack charge and the target's damage-immunity window are different mechanics. CosmicPVE removes meaningful attack-charge delay but retains the normal target hurt/damage-immunity system. Rapid attacks that the server rejects or that deal no positive damage are not proc eligible. The accepted-hit cadence and resulting feel should be tuned through playtesting without globally clearing `invulnerableTime`.

### 6.4 Proc and cooldown engine

`ProcEngine` evaluates registered hooks such as `ON_VALID_HIT`, `ON_DAMAGE_TAKEN`, `ON_PRE_DEATH`, `ON_KILL`, `ON_BLOCK_BREAK`, `ON_FOOD_EATEN`, and periodic ticks. Each candidate has:

- stable effect ID and optional cooldown-group ID;
- base chance and luck/proc modifiers;
- conditions;
- once-per-attack/event key;
- allowed child-proc policy;
- effect actions.

Use one deterministic random source per server event and expose the roll in debug tracing. Define chance modification centrally. The Luck example implies multiplicative relative chance (`1%` with `20%` Luck becomes `1.2%`), not a flat percentage-point increase. Clamp final probability.

`CooldownService` stores server-tick expiry by entity UUID and cooldown key. Effective duration is calculated once at activation from all cooldown-rate modifiers: `ceil(baseTicks * product(durationMultipliers))`. Cooldowns use active server ticks rather than wall-clock time. Persistent cooldowns are saved; temporary encounter cooldowns can be scoped to and removed with the session. Client UI receives sanitized remaining durations.

Once-per-event keys handle Angelic across multiple armor pieces. Cooldown groups handle multiple sources of the same effect. Doublestrike creates a genuine child hit at 50% of the parent strike's damage. It may reroll other eligible offensive on-hit enchantments but is explicitly forbidden from triggering Doublestrike again. Because the parent has just activated normal hurt immunity, `CombatEngine` needs a narrowly scoped child-strike path that can deliver this intended damage without clearing or weakening the target's global `invulnerableTime`. This bypass is valid only for the linked Doublestrike child sequence and must not make unrelated rapid attacks eligible.

### 6.5 Generalized combat stacks

Attach a `CombatStackContainer` to every relevant `LivingEntity`. It maps stack ID to one or more `StackInstance`s containing source attribution, count, start/expiry ticks, and definition revision. `StackDefinition` declares:

- `POSITIVE` or `NEGATIVE` polarity;
- maximum count;
- duration and refresh policy (`REFRESH_ALL`, `REFRESH_ONE`, `INDEPENDENT`, `FIXED`);
- source aggregation policy;
- tick interval and actions;
- attribute/damage modifiers;
- whether it is transferable, cleansable, visible, or persistent.

All mutation goes through `CombatStackService`: add, remove, cleanse by polarity, steal one transferable positive stack, copy, expire, and clear by scope. Maui's Hook queries polarity and transfer policy. Bleed damage retains its source attribution. Hysteria evaluates only after a hit would otherwise be valid, cancels all damage to the intended target, and applies equal ordinary self-damage to the attacker through a `NO_PROCS` child context.

Keep short combat stacks server-side and synchronize only UI-relevant summaries. Persist only stack types explicitly marked persistent; encounter buffs such as Invasion bonuses are session-owned and are cleared on leave/end.

## 7. Persistence strategy

Use three layers with explicit schema versions and codecs:

### Entity/player attachments

Persist player-owned progression and durable runtime data in registered NeoForge attachments:

- unlocked kits and refresh/claim state;
- persistent cooldown expiries if the design requires cooldowns to survive logout;
- pending instance recovery record;
- instance membership pointer;
- any persistent combat state.

Attachments on general `LivingEntity` hold stacks, cooldowns, and combat memory such as Rage attackers. Most mob state should remain ephemeral unless the entity itself is saved. Copy behavior on player death must be defined per attachment rather than copying everything blindly.

### World saved data

`CosmicWorldData` stores global scheduler and instance records:

- schema version and next instance sequence;
- the single active or recovering instance session;
- the current instance arena and protected bounds;
- invasion cadence and active invasion ID;
- participant UUIDs and lifecycle state;
- encounter/room state needed to resume or safely abort;
- committed reward/recovery records.

Mark data dirty on every durable transition. Store it in a stable server-level data store and access it through one repository service rather than from gameplay classes.

### Item components

All per-item identity and state travels with the `ItemStack` through the typed components described earlier.

Every durable object starts with a `dataVersion`. Add explicit migrations as formats evolve. Unknown definition IDs should degrade safely: preserve serialized identity where possible, disable the missing behavior, log a clear diagnostic, and provide an admin inspection/recovery command.

## 8. Shared instance framework

### 8.1 Runtime model

`InstanceManager` owns the session in the dedicated instance dimension. The initial implementation permits at most **one active gameplay instance session per world/server**, shared by its one 1–4 player party. Attempts to create another Dungeon, Trial, or Invasion while a session is preparing, joining, active, transitioning, or recovering must fail cleanly with a useful message. The APIs should continue to use session IDs and avoid singleton assumptions inside combat/encounter code so concurrency can be added later without redesigning the domain model. An `InstanceSession` has:

- UUID, definition ID/revision, and mode (`DUNGEON`, `TRIAL`, `INVASION`);
- lifecycle state: `PREPARING`, `JOINING`, `ACTIVE`, `TRANSITIONING`, `SUCCEEDED`, `FAILED`, `RECOVERING`, `CLOSED`;
- allocated arena/region and template placement status;
- party roster and immutable initial party size;
- active participant/spectator/removed states;
- active-tick timers, pause reasons, and deadline counters;
- current room, encounter, objectives, and random seed;
- mode-specific state payload;
- reward commitments and cleanup progress.

Modes implement narrow lifecycle hooks. Dungeon mode supplies an ordered challenge graph, Trial mode supplies room pools/decision boxes/shared pot, and Invasion mode supplies scheduled entry, quadrants, contribution rewards, and a global timer. Teleportation, persistence, recovery, protection, scaling, and cleanup stay shared.

Use one fixed, well-known arena region in the dedicated dimension for the initial implementation. Structure templates or data-driven placement plans build the active arena, and a new session cannot begin until the previous session is closed and cleanup has completed successfully. Defer arena leasing, concurrent-cell allocation, quarantine systems, and multiple simultaneous parties. Preserve an `InstanceLocation`/placement abstraction so a later concurrency milestone can replace the single-region policy without changing session, encounter, or protection APIs.

### 8.2 Party scaling

Freeze the scaling party size when active play begins; joins after that point are normally disallowed. Departures should not reduce boss maximum health mid-fight. Definitions reference a `ScalingProfile` that can provide explicit 1/2/3/4-player values or a formula with caps.

Scale dimensions independently: boss health, mob count/caps, spawn interval, damage, and objective counts. Avoid scaling all damage and health by one global factor. When maximum health changes, initialize the entity at the scaled maximum and preserve health percentage during phase transformations.

### 8.3 Inventory and return transaction

`InventoryTransactionService` must implement a durable state machine per player:

1. Acquire a per-player transition lock.
2. Serialize the complete outside inventory: main inventory, hotbar, armor, offhand, carried/menu cursor stack if applicable, experience if a mode changes it, and other explicitly affected state.
3. Record original dimension, position, yaw, pitch, safe fallback, session ID, and a unique transaction ID.
4. Persist the snapshot as `SNAPSHOT_COMMITTED` before clearing or replacing inventory. Do not assume that merely marking saved data dirty makes this crash durable, and do not force a global world save on every transition. Prototype the exact NeoForge/Minecraft persistence boundary and choose the narrowest reliable durability mechanism before relying on this step.
5. Prepare the instance inventory and teleport; then persist `IN_INSTANCE`.
6. On exit, clear instance-only inventory first, restore the exact snapshot once, teleport to a validated safe return point, and persist `RESTORED`.
7. Retain a short-lived receipt/tombstone so retries cannot restore twice. Remove the snapshot only after the restored state is durably acknowledged.

Recovery on login/server start is idempotent. If session state is missing, corrupt, closed, or inconsistent, prefer restoring the committed outside snapshot and returning the player to a safe fallback. Commands must allow an operator to inspect, export, and restore a snapshot, but never silently overwrite an occupied inventory without a deliberate recovery policy.

This service is mandatory for Trials. Dungeons and Invasions may keep the outside inventory active, but still use the same return-position and membership transaction machinery. The design's phrase “keep inventory” must not be implemented through a global gamerule.

Trial mode applies these settled multiplayer and skip rules:

- The pot is shared as a value/state while the party continues, but cash-out is individual. A player who accepts receives their own full copy of the current pot and permanently leaves; the pot remains intact for continuing players.
- `Skip N` treats the first N rooms as completed for room number and Apprentice/Hardcore/Demonic phase progression. It generates and adds each corresponding room-tier loot roll to the pot, consumes no Trial time, and awards none of those rooms' normal completion-time bonuses.

### 8.4 Protection and allowed interactions

`InstanceProtectionService` resolves an `InteractionPolicy` from session, room, actor, position, block/entity type, and cause. Default deny covers:

- player break/place/use;
- explosions and their affected-block lists;
- piston movement across protected boundaries;
- fire ignition/spread and burning;
- fluid placement/flow;
- entity griefing and block transformation;
- buckets, projectiles, vehicles, portals, and other bypasses;
- item/entity movement across arena boundaries where exploitable.

Room definitions add narrow capabilities, such as “the intended Creeper explosion may destroy blocks tagged `bomb_squad_wall` inside this room.” Check cause and block tag, not merely explosion type. Debug bypass requires an explicit permission and should be logged.

Defense in depth is needed: cancel mutation events, filter explosion block lists, constrain encounter entities, snapshot important template regions, and restore/clean arenas after use. Test each bypass class with GameTests.

### 8.5 Encounter and boss state

`EncounterController` is a server-side state machine composed from reusable objectives:

- kill tracked actors;
- reduce boss to a phase threshold;
- survive or complete within a timer;
- interact with ordered controls;
- collect/place/mine permitted objective items;
- traverse checkpoints;
- custom objective adapter for mechanics that truly cannot compose.

Bosses use reusable abilities with telegraph, wind-up, active, recovery, cooldown, targeting policy, and cancellation rules. Persist coarse encounter phase/objective state and reconstruct ephemeral AI scheduling on load. Controlled encounter spawners track caps, ownership, session IDs, and despawn cleanup rather than relying on vanilla spawners.

`DamageLedger` records credited damage per player UUID after final accepted damage, including attributed projectiles, DoTs, summons, and custom true damage. Cap credited damage to the target's remaining health to prevent overkill inflation. Define whether healing/reset phases reduce the denominator. Persist ledgers needed for rewards.

## 9. Loot and reward generation

Build `CosmicLootService` on top of validated weighted definitions and vanilla loot infrastructure where practical. A `LootContext` contains instance type, definition ID, heroic flag, room tier, participant, party size, contribution flags, and deterministic session/reward seed.

Support:

- guaranteed entries/groups;
- N independent weighted rolls;
- quantity ranges;
- duplicate policy (default allowed);
- conditional/expanded pools;
- item factories that attach rolled components such as book success/destroy rates.

Rewards should be committed to persistent session/player records before delivery. Delivery is idempotent by reward ID: mark generated, attempt inventory insertion, drop/mail to a controlled recovery mechanism if full, and mark claimed only once. Lootbags can carry a committed reward ID or pre-generated contents; they should not be rerollable by disconnecting or copying UI state.

Automated validation should flag the malformed or incomplete tables currently present in the design (for example blank Dungeon entries and the Demonic table's header row).

## 10. Networking and client boundary

Use custom payloads only for information the vanilla synchronization does not provide: proc feedback, cooldown HUD, visible stacks, instance timers/objectives, Trial decision UI/pot, and debug traces. Payload handlers must validate session membership, screen/container identity, permissions, slot indexes, and monotonically increasing request IDs.

The client may animate a predicted attack but must not roll procs, decide damage, modify items, award loot, or transition instance state. Keep all client imports under the `client` package or explicitly client-only registration paths so dedicated servers can load safely.

## 11. Debugging, observability, and tests

Register a `/cosmic` command tree with permission-gated subcommands:

- `enchant give-book|apply|list`;
- `item inspect|set-slots|set-set|set-mask|set-skin|set-heroic`;
- `combat trace on|off|last`, `stack add|remove|list`, `cooldown list|set|clear`;
- `instance create|join|leave|inspect|advance|fail|reset|cleanup`;
- `snapshot inspect|restore`;
- `encounter start|phase|complete`, `ledger inspect`;
- `loot roll|preview`;
- `invasion schedule|start|stop` and `trial set-time|set-pot`.

Commands should call the same services as gameplay and return calculation IDs/state summaries. Add structured log context for session ID, encounter ID, attack sequence ID, player UUID, and reward ID.

Prioritize GameTests/unit tests for:

- exact damage-stage ordering, additive offense, multiplicative defense, Aegis, and true damage;
- once-per-event procs, Luck math, cooldown reduction, and proc recursion prevention;
- stack expiry/refresh/steal/cleanse and source attribution;
- four-piece armor-set resolution and equipment invalidation;
- item modification atomicity and white-scroll protection;
- 1–4 player scaling;
- instance lifecycle reload at every transition;
- crash/disconnect recovery before and after inventory clearing/restoration;
- duplicate reward prevention;
- every terrain-protection bypass category.

## 12. Difficult, ambiguous, or dangerous requirements

The following unresolved design points remain difficult or dangerous and should be resolved or prototyped before their dependent content is implemented:

1. **“Perma” stack wording.** Bleed lasts five seconds but also describes a permanent movement penalty per stack. The intended refresh/expiry model is contradictory.
2. **Skin removal by simple left click.** Normal inventory clicking is already heavily overloaded and client-predicted. Use an explicit modifier-click, menu, or removal tool with server validation to avoid accidental removal and duplication.
3. **Arbitrary portal placement.** A 2x1 portal placed anywhere creates collision, chunk-loading, ownership, and safe-return issues. Treat the blocks as a session anchor with placement validation, protected ownership, expiry, and a safe fallback return position.
4. **Crash-safe inventory swapping.** This is the highest data-loss/duplication risk. A snapshot stored only on the player or only in memory is insufficient. Prototype the narrow persistence boundary, then implement and test the durable, idempotent transaction before building Trial rooms.
5. **Terrain protection breadth.** NeoForge does not provide one switch covering every mutation path. Explosions, pistons, fluids, fire, mob griefing, buckets, projectiles, and special block behaviors require layered interception and tests.
6. **Timed Invasion schedule.** “Every 10th Minecraft day” needs a rule for sleeping/time commands, first occurrence, missed events while the server is offline, and multiple worlds. Store a monotonic schedule marker rather than relying only on `dayTime % 10`.
7. **Invasion entry item.** Giving every player a pearl includes offline players, late joiners, death/drop behavior, and inventory-full cases. Prefer an expiring entitlement plus a bound token, with server validation at use.
8. **Contribution threshold.** Define denominator behavior for boss healing, phase health bars, summons, damage while a boss is invulnerable, and players who leave. Credit only accepted capped damage.
9. **Items between Trial rooms.** Clearing all room items can delete rewards or externally injected items; every Trial stack needs a scope/provenance rule, and the outside snapshot must be isolated.
10. **Heroic durability/model conversion.** Changing armor “into leather” or tools “to a gold texture” should be cosmetic data, not replacing the underlying item. The exact durability math and repair behavior must be specified to avoid component loss.
11. **Attribute units and caps.** “HP,” “heart,” percent movement speed, damage, and incoming modifiers are mixed. Normalize all definitions to documented units and define global caps/floors, especially negative incoming damage and movement stacking.
12. **Incomplete/inconsistent content tables.** Several loot tables contain duplicates (sometimes intentional), blank rows, inconsistent naming/case, a malformed Demonic header, and incomplete dungeon pools. Content validation should block release but allow marked development placeholders.

## 13. Suggested implementation order

1. **Foundation cleanup and registration:** real package names, registration modules, codecs, data components, attachments, payload registration, and removal of MDK examples.
2. **Content repository and validation:** definition codecs, reload flow, stable IDs, diagnostics, and test fixtures.
3. **Combat kernel:** context/attribution, damage stages, recursion policy, effective enchantments, legacy combat prototype, and calculation tracing.
4. **Proc/cooldown/stack services:** implement generic services and prove them with a few representative effects (not the full enchant list).
5. **Equipment composition:** armor-set resolution, mask loadouts, weapon skins, and item modification transactions.
6. **Persistence and recovery:** versioned player/world data, migrations, durable reward records, inventory snapshot transaction, and recovery commands/tests.
7. **Instance kernel:** enforce the one-session-per-world policy, fixed arena placement, lifecycle, teleport/return, membership, active timers, protection, cleanup, and party scaling while preserving future concurrency seams.
8. **Encounter and loot frameworks:** objective state machines, abilities, controlled spawners, contribution ledgers, weighted rewards, and idempotent delivery.
9. **One vertical slice:** one small Trial room or test dungeon using every layer, including disconnect/restart tests. Revise the abstractions based on this slice.
10. **Mode specializations:** Trial decision/pot logic, Dungeon sequencing/heroic modifiers, then scheduled Invasions and quadrant contribution rewards.
11. **Content expansion and presentation:** remaining enchantments, sets, masks, skins, rooms, bosses, models, UI, sound, and balance.

Do not start by implementing every enchantment or boss. The first milestone should demonstrate one ordinary modifier, one proc with cooldown, one positive and one negative stack, one four-piece set, one skin, one crash-safe instance transition, and one persisted reward through the shared systems.
