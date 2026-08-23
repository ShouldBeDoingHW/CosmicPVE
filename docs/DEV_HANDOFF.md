# CosmicPVE Development Handoff

Current verified and accepted gameplay commit: `14415b3c1bc51c4f489599131814f68f76744c15` (`Add Space Chests and Blessed`). This is the accepted Steps 6A–6L baseline.

Repository state: Steps 6A–6M are implemented, automated/runtime verified, and manually accepted. The root `assets` source-art folder and root `trial rooms` structure-development folder remain deliberately untracked and unrelated.

Last handoff update: 2026-08-23

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

1. `docs/DEV_HANDOFF.md` — current implemented/verified state, immediate next work, and do-not-assume items.
2. `docs/Cosmic_Design.md` — the current gameplay-design source for future milestones. It has been substantially updated since the prior handoff baseline; its newly documented systems remain design-only until implemented and verified.
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

Implemented a reloadable, namespaced, codec-driven content repository. Candidate definitions are decoded and validated before an immutable snapshot is published atomically; failed reloads do not partially replace active content. Current definition families are `ScalingProfile`, `StackDefinition`, `ArmorSetDefinition`, and `RewardTable`. Development fixtures use explicit `development_*` IDs rather than pretending to be canonical gameplay content.

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

### Step 6E — COMPLETE; MANUALLY VERIFIED AND ACCEPTED

Implemented the typed/versioned weapon-skin foundation and the first three skins: Boosted Chainsaw, Maui's Hook, and Stormbringer. One generic non-stackable application item carries a stable skin ID; the active identity is stored on the original weapon together with its exact previous item-model component, so removal restores presentation without reconstructing or replacing the stack. Drag + left-click applies to a compatible weapon. With an empty cursor, right-clicking a skinned weapon in an inventory removes the skin and returns the corresponding application item directly to the cursor. One active skin is allowed, and both transactions revalidate authoritative stack identity before mutation.

Boosted Chainsaw grants runtime-only Doublestrike III with `cosmicpve:boosted_chainsaw` virtual provenance. It writes no actual enchantment, consumes no capacity, is invisible to Black Scroll extraction, and disappears on removal. Maui's Hook contributes +4% to the shared additive ordinary outgoing bucket and exposes a 10% Luck-modified ProcEngine candidate only when the target has an eligible transferable POSITIVE stack; activation delegates the deterministic one-instance move to `CombatStackService`. Stormbringer exposes a 3% Luck-modified limited-offensive-reroll candidate that creates visual-only lightning, applies Slowness II for 30 ticks, and delivers exactly 2 HP standard true damage with `NO_PROCS`. Holding the skinned axe in the main hand contributes ×0.98 ordinary incoming damage; true damage and execution bypass this ordinary seam.

The three supplied transparent source images were converted with nearest-neighbor scaling and centered on transparent 32×32 canvases: `Boosted Chainsaw.png` → `boosted_chainsaw.png`, `Maui's Hook.png` → `mauis_hook.png`, and `Stormbringer.png` → `stormbringer.png`. Item presentation uses the 1.21.11 `minecraft:item_model` stack component, with the prior value persisted for exact restoration. Applying a skin plays leather-armor equip at normal pitch; removal uses the same sound at pitch 0.7. Black Scroll's model now uses vanilla Ink Sac presentation only; its data and extraction path are unchanged.

The full build and all 134 tests passed; 71 JSON resources validated. The dedicated server reached `Done`, and the client completed resource reload/model-atlas creation with no missing-texture or invalid-model errors. The user manually verified the weapon-skin foundation, all three implemented skin passives and interactions, supplied artwork presentation, and the Black Scroll Ink Sac appearance, then accepted the milestone.

### Step 6F — COMPLETE; MANUALLY VERIFIED AND ACCEPTED

Standardized Weapon Skin application lore through per-skin presentation metadata and one reusable renderer. Application items are named `Weapon Skin (<Skin Name>)`; Boosted Chainsaw uses `#CCA00A`, Maui's Hook uses `#404242`, and Stormbringer uses `#224B57`, as specified by the canonical design. Effect summaries are always yellow, while the weapon-kind and exact attach/detach instructions are gray. Attached weapons show one concise active-skin line in the skin's canonical name color. Gameplay data, models, glint, attachment/removal behavior, and Transmog/enchantment ordering are unchanged.

Standardized every Cosmic Book through `CosmicEnchantmentSpec` metadata and localized descriptions. Lore order is Success Rate, Destroy Rate, a tier-colored rarity name with a yellow plain-language effect body, gray applicability, and gray drag/drop instruction. The specification owns the stable display/description/applicability keys, so future registered enchantments do not require tooltip switch logic.

Implemented real Unique enchantments `cosmicpve:molten` IV and `cosmicpve:nutrition` III, bringing the total to exactly 17. Molten is valid on any armor. A committed positive-red-health hit creates at most one defensive candidate when a meaningful living attacker exists; its effective level is the highest equipped Molten level, never the sum, and its Luck-modified base chance is 2% per level. Activation ignites the responsible attributed living attacker for three seconds. Nutrition is leggings-only and runs once after completed food consumption without ProcEngine RNG: Minecraft `FoodData.eat` adds exactly +1 hunger and +0.25 saturation per level while retaining vanilla caps. Both use real Minecraft enchantment data, generic Books, capacity, Black Scroll eligibility, Transmog sorting, and remain outside vanilla acquisition pools.

### Step 6G — COMPLETE; MANUALLY VERIFIED AND ACCEPTED

Implemented one versioned `HEROIC` item component and one generic non-stackable Heroic Crystal. Its displayed name uses `#AA00AA`, and its concise purpose/instruction lore uses the standard yellow effect-description style. A server-authoritative drag/drop transaction applies once to armor, pickaxes, or shovels, mutating the original stack in place and preserving wear and unrelated components. Successful application plays the standard player-level-up item-application cue. It raises the stack's real `MAX_DAMAGE` by exactly 250 while leaving its damage value unchanged. Heroic armor retains its registry item and defensive statistics but uses leather equipment/item presentation; Heroic pickaxes and shovels retain their underlying mining behavior but use gold-style item presentation. Heroic and armor-set identities compose in either application order. Heroic Dungeon Portal behavior remains designed and unimplemented.

Implemented real `cosmicpve:glowing` I (Simple, helmet) and `cosmicpve:obsidianshield` I (Ultimate, leggings), bringing the real enchantment count to 19 at that milestone. One LivingEntity-compatible equipped-effect service maintains hidden Night Vision or Fire Resistance leases while tracking only effects it created; external effects are not removed when equipment changes. Glowing's former short-lease visual flicker was resolved in Step 6J through the same shared ownership service.

Expanded the armor-set inventory tint item-definition path to leather, chainmail, iron, gold, diamond, and netherite armor, including trim variants. Heroic leather-style armor uses the same tint source, so set identity remains visible in item UIs. Maui's Hook now mirrors the held model's X scale in first/third-person and right/left-hand transforms so its curved end faces away from the wielder; the inventory artwork and gameplay are unchanged.

### Step 6H — COMPLETE; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED AND ACCEPTED

Implemented the first reusable custom hostile-mob foundation and explicit entity types `cosmicpve:space_pirate_variant_1` and `cosmicpve:space_pirate_variant_2`. Both share ordinary hostile melee AI, authoritative one-time equipment initialization, normal LivingEntity combat integration, and zero drop chance for all generated equipment. They have no natural spawn placement or custom attacks. Variant 1 uses the vanilla Zombified Piglin presentation at 1.35 scale, 25 HP, a Diamond Axe with actual Insanity VIII and an independent 50% Pummel III chance. Variant 2 uses the vanilla Wither Skeleton presentation at 1.35 scale, 35 HP, an Iron Sword with actual Poison I–III and an independent 25% Execute I–V chance. Both have zero innate armor, toughness, and bonus attack damage, independently roll Iron/Diamond plus Protection I–IV for each armor slot, and use movement speed `0.35` through the normal entity attribute. Generated equipment is saved as normal entity item data and guarded against spawn-time rerolls. The user manually verified and accepted both variants, including the final movement-speed tuning.

Implemented real Simple enchantment `cosmicpve:oxygenate` II for pickaxes, bringing the registered real-enchantment count to 20. A completed underwater block break restores exactly one displayed air bubble (30 internal air units) per effective level and clamps to the normal maximum. It is deterministic and outside ProcEngine, Luck, cooldown, and stack handling. Oxygenate uses generic Books, capacity counting, Black Scroll extraction, Transmog, and remains absent from vanilla acquisition pools.

The centralized Simple rarity color is now `#FFFFFF`; Cosmic Book effect-description bodies remain yellow while their tier name uses the tier color. Ancient's data-driven presentation color is now `#0A4A3D` with gameplay unchanged. Combat/proc trace output now lets a traced player inspect committed mob attacks, identifying the attacker, weapon/effective enchantments, outgoing contributors, and proc candidates/results through the existing trace systems.

### Step 6I — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Iron is now the normal crafted player-armor ceiling. A required highest-priority built-in server datapack disables all four Diamond armor crafting recipes and all four Netherite armor smithing upgrades without restricting Diamond/Netherite tools or weapons. A server-start/datapack-sync invariant verifies those eight IDs are absent from the live recipe manager and fails loudly if another resource reintroduces them. This does not prevent explicitly configured encounter mobs, including Space Pirates and Trial Chamber encounter equipment, from using stronger armor.

One generic stackable `cosmicpve:unexamined_enchantment_book` carries a versioned rarity component and uses the existing Cosmic tier metadata for naming, tint, glint, and base rate ranges. Server-authoritative right-click opening selects one registered/implemented enchantment of the same rarity, rolls a uniformly random valid level, independently rolls Success and Destroy rates, consumes exactly one Unexamined Book, launches a non-damaging upward firework, and returns the existing typed Cosmic Enchantment Book. The use transaction explicitly transforms an exhausted final source stack into its revealed book; when source books remain, vanilla inventory insertion safely drops the reward if no slot can accept it. Rarities with no implemented enchantments reject safely without consumption. `BookOpeningRateService` composes registered modifiers in order and validates the result against the tier; no modifier-providing gameplay effect exists yet.

A data-scoped global loot modifier replaces generated Diamond helmet/chestplate/leggings/boots results with exactly one Unexamined Book: 65% Simple and 35% Unique. The audited target tables are Ancient City, Bastion Treasure, End City Treasure, Woodland Mansion, and the normal/ominous rare Trial Chamber reward chests. The separate Trial Chamber encounter-equipment table is deliberately excluded. A permission-gated `/cosmic enchant unexamined give <player> <tier> [count]` command supports manual verification.

### Step 6J — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Implemented one cent-safe persistent `long` money balance in the existing copy-on-death player profile attachment and centralized all mutations in `MoneyService`. `/bal`, `/balance`, and exact-decimal `/withdraw` are server-authoritative. Generic non-stackable Banknotes carry versioned positive-cent data, redeem atomically with the arrow-hit-player sound, and use a reusable exact-denomination factory. Repair Scrolls use the established server-authoritative drag/drop seam and repair the original damageable stack to its current maximum without reconstructing it. Exact returned-success Black Scroll and fixed-success/random-destroy Armor/Weapon Orb construction share reusable server-side reward factories now consumed by the generic reward and Space Chest systems.

Implemented Legendary `cosmicpve:armored` IV on any armor through Minecraft's native `damage_protection` enchantment effect at 0.5 Protection-equivalent points per level. Equipped pieces therefore aggregate with vanilla Protection and remain inside vanilla's normal protection cap; true-damage delivery continues to bypass enchantment reduction. Implemented the first Mastery enchantment, chestplate-only `cosmicpve:death_pact` V: every level contributes exactly -3% to the shared ordinary outgoing bucket and applies a separate ordinary incoming multiplier of `1 - 0.02 × level`. Mastery Book rates, Unexamined Mastery reveal, capacity/Transmog integration, and Black Scroll exclusion derive from existing generic tier/spec systems. There are now 22 real Cosmic enchantments.

Glowing now maintains a hidden 600-tick Cosmic-owned Night Vision lease and refreshes at 300 ticks, comfortably before vanilla's sub-200-tick warning range without recreating the effect every tick. The shared ownership rules remain intact: removing Glowing removes only its owned lease, external effects are preserved, and Glowing reacquires its lease after a superseding external effect expires.

### Step 6K — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Added atomically reloadable `cosmicpve/reward_tables` definitions with positive weighted entries, validated quantity ranges, and typed descriptors for static items, exact Banknotes, same-rarity generated Cosmic Books, fixed-success Black Scrolls, fixed-success/random-destroy Armor and Weapon Orbs, typed Mob Spawners, and generated equipment. Generation accepts an explicit random source, while delivery is a separate reusable service that inserts safely and drops overflow at the player. One development fixture proves all descriptor families without defining production Space Chest tables.

One generic versioned Mob Spawner item stores a namespaced entity-type ID, validates a sensible Mob boundary, and places/configures Minecraft's real spawner block without inventing custom spawning or recovery rules. Generated equipment currently supports uniformly selected Iron armor pieces, distinct compatible actual Cosmic enchantments under a rarity ceiling, bounded enchantment counts, and maximum or independently random valid levels. The three Space Chest armor patterns are consumed through this same definition.

Implemented Simple `cosmicpve:auto_smelt` I and `cosmicpve:experience` III, bringing the total to 24 real Cosmic enchantments. Auto Smelt transforms the finalized normal block-drop stacks through ordinary furnace recipes, preserving generated quantity and producing no furnace XP. Experience scales only the finalized block XP by `1 + 0.5 × level` and floors the result. Both share the completed player block-drop event, coexist without duplication, and integrate through normal Books, Unexamined Simple Books, capacity, Black Scroll, Transmog, lore, and persistence systems.

### Step 6L — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Implemented one generic typed/versioned, non-stackable Space Chest item for Ultimate, Legendary, and Mastery tiers, using canonical tier-colored names, Minecraft's special vanilla normal-Chest item renderer, and tier-specific virtual panes. Three production `cosmicpve:space_chest/*` reward tables use the existing reloadable weighted-reward repository and Step 6K factories. Each rarity-labelled Book row now generates the corresponding generic Unexamined Book, deferring enchantment, level, Success, and Destroy rolls until each book is opened; the separate random-actual-Cosmic-Book reward primitive remains available for other content. Each of the 27 positions is one independent table roll, and identical Unexamined Books may compact into a truthful quantity stack inside one position.

The server-authoritative 27-slot menu escrows exactly one chest, permits five distinct blind selections, and commits all 27 results only on the fifth choice. Missed rewards reveal left-to-right/top-to-bottom over 80 server ticks, remain fully visible for a 30-tick pause, then clear together with one chicken egg sound. The five selections retain their leather-equip sounds; each newly claimed selected reward plays one player-only experience-orb pickup sound, and closing a committed menu plays one player-only chest-close sound. Selected bundles remain guaranteed: clicking a selected pane delivers and marks its complete bundle exactly once, while any post-commit close delivers all still-pending bundles. A persistent copy-on-death player attachment records tier, phase, selected indices, committed selected ItemStacks, and delivery markers; login/respawn recovery returns an uncommitted chest or idempotently delivers committed pending rewards without globally saving on each click. Client code owns only screen rendering and synchronized display stacks.

Implemented Ultimate `cosmicpve:blessed` IV on axes, bringing the total to 25 real Cosmic enchantments. Each valid committed melee hit creates at most one Luck-modified candidate at 2% per level, and only when the attacker has an eligible cleansable negative Cosmic stack instance or harmful vanilla effect. Doublestrike children may reroll it through the normal limited-offensive peer policy. Activation uniformly selects across individual negative stack instances and harmful effects, then removes exactly one stack instance or the entire selected vanilla effect. Blessed has no cooldown, stack, true damage, or extra packet.

The generic spawner placement audit confirms `SpawnerBlockEntity.setEntityId` writes `minecraft:creeper` into vanilla `SpawnData` while retaining the normal 20-tick initial delay, 200–800-tick subsequent delay, spawn count 4, and 16-block player range. Placement now centralizes the vanilla configuration, dirty-mark, chunk notification, and client update boundary, and `/cosmic reward spawner inspect <position>` exposes the saved state for controlled testing. Creepers still obey ordinary non-Peaceful difficulty, darkness, collision, nearby-mob, and player-range rules; no mob-specific spawning exception was introduced.

### Step 6M — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Added the shared `cosmicpve:cosmic_instance` void dimension and a centralized single-active-session Trial lifecycle (`JOINING`, `DECISION`, `ROOM_INTRO`, `ROOM_ACTIVE`, `ENDING`, `CLOSED`) with a persistent UUID, participant/removed rosters, portal context, current room, protected bounds, transition serial, and the shared 12,000-tick gameplay timer. Decision states and the five-second room intro pause the gameplay timer. Debug completion commits once, clears the development-room state/loadout, and moves the whole active party back to the Decision Box; duplicate completion calls reject.

Trial entry closes the current container, records exact main/hotbar, armor, offhand, cursor, selected slot, dimension, position, yaw, and pitch through registry-aware `ItemStack` codecs, attaches a unique transaction/session ID, and synchronously saves only that player file before clearing anything. The cleared Trial inventory and retained snapshot are then saved together. Exit first clears Trial-only state, rewrites the outside stacks in place, teleports to the saved return context (Overworld spawn is the missing-dimension/failed-position fallback), replaces the snapshot with a persistent restored tombstone, and saves the restored inventory/tombstone together. Retries are idempotent. Disconnect removes only that participant while preserving their snapshot; login restores when they no longer belong to a valid active session. Server restart conservatively aborts any interrupted session and restores affected players as they become available. Trial death clears Trial-only inventory before normal drops, removes only the dead participant, and restores through the copy-on-death snapshot after respawn without changing `keepInventory`.

The stackable Eye-of-Ender-presented Trial Portal creates a temporary two-block custom gateway only after the instance dimension and Decision Box are ready, permits at most four participants during the 30-second join period, and rejects a fifth participant, placement in the instance dimension, obstructed placement, or a second active session. The supplied `decision_box.nbt` is imported with its sole Emerald Block at local `[23,13,23]` as a consumed spawn marker. The supplied Raiding Rainbow structure is imported only as `development_room.nbt` placement test content; its gameplay is not implemented or in a production room pool. Reloadable Trial room definitions own display/category, one or more offset/rotated structure pieces, marker policy, and declared bounds; two-piece composition is covered for future Deadeye-style rooms.

Default scoped instance protection denies participant break, place, item-use/bucket/ignition, explosion block mutation, mob-grief checks, living block destruction, piston mutation, and fluid block formation inside active bounds, with creative bypass and a narrow per-session/room/cause/position allow-policy seam. Structure cleanup removes non-player entities and deterministically clears declared room bounds before reuse. NeoForge exposes no complete cancellable hook for every scheduled vanilla fire-spread or flowing-fluid state change; direct ignition/use and fluid block formation are denied, and deterministic room cleanup is the current defense for residual scheduled mutations. This limitation must be revisited before a room intentionally contains persistent fire or flowing fluid.

Implemented Ultimate `cosmicpve:implants` III on helmets, bringing the total to 26 real Cosmic enchantments. Continuous equipped operation schedules exactly 1 HP healing every 85/70/55 ticks for levels I/II/III, advances the schedule even at full health to avoid catch-up bursts, stops when unequipped, caps through normal healing at maximum health, and uses no proc, Luck, cooldown, CombatStack, or damage modifier. Normal Book, Unexamined Ultimate, Black Scroll, capacity, Transmog, lore, and persistence integration comes from the shared real-enchantment metadata.

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
| `cosmicpve:molten` | IV | Any armor | Unique | On committed positive-red-health damage taken, has 2% per highest equipped Molten level to ignite the responsible living attacker for 3 seconds. Multiple pieces produce one candidate using the highest level, not summed levels. |
| `cosmicpve:nutrition` | III | Leggings | Unique | Once a food item is fully consumed, adds +1 hunger and +0.25 saturation per level through vanilla bounded food state. It is deterministic and does not use Luck or RNG. |
| `cosmicpve:glowing` | I | Helmet | Simple | Maintains subtle Night Vision while actively equipped without deleting externally supplied Night Vision. |
| `cosmicpve:obsidianshield` | I | Leggings | Ultimate | Maintains subtle Fire Resistance while actively equipped without deleting externally supplied Fire Resistance. |
| `cosmicpve:oxygenate` | II | Pickaxe | Simple | After a completed underwater block break with the enchanted pickaxe, restores one displayed air bubble (30 internal air units) per level, clamped to normal maximum air. Deterministic; does not use Luck or RNG. |
| `cosmicpve:armored` | IV | Any armor | Legendary | Adds one-half vanilla Protection-equivalent point per level through Minecraft's native protection effect, aggregating across equipped pieces and sharing vanilla Protection's normal cap. |
| `cosmicpve:death_pact` | V | Chestplate | Mastery | Adds -3% ordinary outgoing damage at every level and a separate `1 - 0.02 × level` ordinary incoming multiplier. True damage and execution bypass the ordinary pipeline. |
| `cosmicpve:auto_smelt` | I | Pickaxe | Simple | Converts each finalized block-drop stack through one ordinary smelting recipe, preserving the vanilla-generated quantity and awarding no furnace XP. |
| `cosmicpve:experience` | III | Pickaxe | Simple | Multiplies finalized player block-break XP by `1 + 0.5 × level` and floors the integral result. Other XP sources are unaffected. |
| `cosmicpve:blessed` | IV | Axe | Ultimate | On a committed melee hit, has 2% per level to uniformly remove either one eligible negative Cosmic stack instance or one entire harmful vanilla effect from the attacker. Luck modifies the chance relatively. |
| `cosmicpve:implants` | III | Helmet | Ultimate | While continuously equipped, heals exactly 1 HP every 85/70/55 ticks at levels I/II/III, without overhealing or catch-up bursts. |

### DESIGNED BUT NOT IMPLEMENTED

The design also names Divine Immolation, Gears, Hero Killer, Mortal Coil, Phoenix, Self Destruct, and Virus. None currently has a registered real enchantment or runtime behavior. There are exactly 26 implemented real Cosmic enchantments; Death Pact is currently the sole implemented Mastery enchantment. Do not add behavior assumptions beyond `Cosmic_Design.md`.

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
- **Ancient** (`cosmicpve:ancient`, `#0A4A3D`): at or above exactly 50% health, +7.5% additive ordinary outgoing damage and ×0.925 ordinary incoming damage. Strictly below 50%, these become +15% and ×0.85. Outgoing reads attacker health at calculation; incoming reads target health before the current hit, so a threshold-crossing hit affects only later events. Standard true damage bypasses the set's ordinary reduction.

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
- Simple `#FFFFFF`

Cosmic Book tint/name and actual enchantment tooltip lines resolve `CosmicEnchantmentTier`. Book lore is Success, Destroy, tier-colored rarity name plus yellow effect-description body, gray applicability, then gray usage instruction. Success lines use `#4DFF74`; Destroy lines use `#C92C2C`. Descriptions and applicability come from generic enchantment specification/localization metadata. Phantom uses `#FF6969`; Yeti uses `#A3FFF5`.

Weapon Skin names use the canonical per-skin design colors: Boosted Chainsaw `#CCA00A`, Maui's Hook `#404242`, and Stormbringer `#224B57`. Skin effect summaries are yellow; `Axe Skin`/`Sword Skin` applicability and both attach/detach instructions are gray.

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

- **Trials:** the most accessible activity, with a high mastery ceiling and harsh learning failures. The current design defines party-wide atomic room completion into a Decision Box, a shared timer, individual death/removal, and individual cash-out: a player who cashes out receives a full copy of the current pot and leaves while remaining players may continue. Skip N advances phase/room progression and adds N corresponding loot rolls without consuming time or awarding skipped-room time bonuses. The detailed current room designs remain future implementation input.
- **Dungeons:** high entry cost through encounter portals, high mastery ceiling, strong average loot, fixed challenge sequences, keep-inventory enabled, and a harder Heroic variant. Portal and activity runtime remain unimplemented.
- **Invasions:** scheduled cooperative events with medium accessibility, comparatively lower mechanical mastery ceiling, stronger baseline preparation requirements, and extraordinary loot potential through boss contribution. Invasions have keep-inventory **off**. Implement their death and inventory behavior through the shared activity/session architecture rather than changing the global `keepInventory` gamerule; the exact activity-specific implementation is deferred to the Invasion milestone.

This Invasion rule is activity-specific and must not change the global `keepInventory` gamerule.

## 12. Major Designed Content Not Yet Implemented

The following remain future content or infrastructure:

- all enchantments beyond the 22 listed above; Death Pact is the sole implemented Mastery enchantment
- Space Pirate natural spawning, Conquest Chest spawning/rewards, and Abandoned Spaceship encounters; only the reusable entities and canonical generated combat equipment exist
- Dimensional Traveler, Engineer, Ranger, Yjiki, and Dragonslayer armor sets
- masks and Multi-Masks
- Whisk Taker, Spinal Tap, Party Blade, and Doomsday Machete weapon skins. Boosted Chainsaw, Maui's Hook, and Stormbringer are implemented. The settled inventory UX is drag and left-click to apply; empty-cursor inventory right-click removes and returns the skin item. This supersedes older design text that reverses those gestures.
- Feeding Frenzy and Hysteria runtime behavior
- Heroic Dungeon Portals and future Heroic repair-specific systems; armor/pickaxe/shovel Heroic conversion is implemented
- armor-set signature weapons
- G-Kits, M-Kits, unlocks, and refreshers
- Trials, Dungeons, Invasions, bosses, rooms, portals, keys, scaling runtime, protection, and recovery
- loot generation, lootbags, production acquisition tables, spawners, and contribution rewards
- Space Chests and their reward prerequisites; Cosmic Crates, Memory Chests, Secret Weapon Caches, and seasonal utility blocks
- final enchanting/loot GUIs; confirmation dialogs are not desired for ordinary deliberate item application

## 13. Future Reward Systems

Space Chests and Cosmic Crates are designed but absent from current code. The near-term direction is to build generic reward prerequisites toward Space Chests before beginning the Trial instance foundation. Memory Chests and Cosmic Crates are deliberately deferred and are not near-term scope.

The current `Cosmic_Design.md` contains the live gameplay design and reward tables for these future systems. Treat those as design input, not implemented behavior; reconcile and validate each system in its own bounded milestone rather than copying the tables into this implementation handoff.

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
- `/cosmic enchant unexamined give <player> <tier> [count]`
- `/cosmic enchant black-scroll give <player> <returned-success>`
- `/cosmic enchant white-scroll give <player> [count]`
- `/cosmic enchant transmog give <player> [count]`
- `/cosmic enchant orb give <player> <armor|weapon> <success> <destroy>`
- `/cosmic enchant orb give-random <player> <armor|weapon>`
- `/cosmic enchant orb give-fixed <player> <armor|weapon> <success>`
- `/cosmic skin give <player> <skin-id>`
- `/cosmic heroic crystal give <player>`
- `/cosmic food show`
- `/cosmic food set <hunger> <saturation>`
- `/bal` and `/balance`
- `/withdraw <amount>`
- `/cosmic money <player> [set|add|subtract <amount>]`
- `/cosmic money banknote give <player> <amount>`
- `/cosmic money repair-scroll give <player> [count]`
- `/cosmic reward list|inspect|reload`
- `/cosmic reward roll <table> [count]`
- `/cosmic reward book give <player> <rarity> [count]`
- `/cosmic reward spawner give <player> <entity-type> [count]`
- `/cosmic reward equipment give <player> <maximum-rarity> <minimum> <maximum> <maximum|random_valid>`
- `/cosmic trial portal give <player> [count]`
- `/cosmic trial debug status|complete-room|continue|exit|abort`
- `/cosmic trial debug timer set|add|remove <seconds>`
- `/cosmic trial debug restore <player>`
- `/feed`, `/heal`, and `/restore` are separate permission-gated development convenience commands.

Commands call gameplay services or create the same typed components used by gameplay; production loot acquisition remains deferred.

## 15. Unresolved / Do Not Assume

- **Trial scaling after departures:** architecture freezes initial party size so boss maximum health does not shrink, but detailed remaining objective/mob scaling and late-disconnect behavior still need acceptance criteria.
- **Creeper Spawner runtime spawning:** the typed item and placed vanilla spawner preserve `minecraft:creeper` in `SpawnData`, and diagnostics/configuration appear correct, but manual testing has not produced Creepers under apparently valid conditions. This is deliberately deferred and does not block Trials.
- **Scheduled instance mutation hooks:** direct block use/ignition, fluid block formation, explosions, pistons, entity grief, placement, and breaking are scoped and denied, but NeoForge does not expose a complete cancellable hook for every scheduled fire-spread or flowing-fluid state transition. Baseline cleanup currently contains residual mutation; rooms that intentionally use persistent fire/fluids need a bounded stronger policy.
- **Loot tables:** several current design tables are incomplete or malformed and contain ambiguous duplicates. Validation must distinguish intentional duplicate weights from mistakes.
- **Attribute units/caps:** future health, movement, incoming damage, cooldown, and stacked modifier caps/floors need normalization before large content expansion.
- **Pheonix/Phoenix identity and death order:** settle the stable spelling/ID and verify pinned NeoForge kill/death-prevention event ordering before implementing it.
- **Activity key/portal terminology and acquisition:** Dungeons are intended as costly/keyed activities, but current design text primarily describes portals; reconcile this before item implementation.
- **Seasonal crate finalization:** substantial draft tables and mechanics exist, but their exact balance, rewards, acquisition rates, prerequisites, and implementation details remain subject to a bounded reconciliation milestone.
- **Armorer trade policy:** Step 6I disables Diamond/Netherite armor recipes and replaces generated Diamond armor in the six audited chest tables. Vanilla Armorer villagers remain a separate Diamond-armor acquisition path because the current bounded rule did not specify trade replacement; settle whether those offers should be removed or replaced before declaring Iron the ceiling for every normal acquisition route.

The final post-Orb capacity is no longer unresolved for current target classes: armor is 8 and swords/axes/bows/crossbows are 10. Do not reopen those limits incidentally during unrelated work.

## 16. Current Next Milestone

**Next milestone: Step 6N — First Playable Trial Vertical Slice.**

Step 6M is implemented, automated/runtime verified, and manually accepted. Step 6N should add the production Decision Box DEAL/NO DEAL interface, Trial pot and Apprentice reward table, room selection/appearance weighting, Raiding Rainbow and Circuit Circus mechanics, and the first real repeatable Trial loop. Do not treat the Step 6M placement-only development room as implemented production room gameplay.

After Step 6M acceptance, proceed to the bounded Step 6N playable Trial vertical slice described above. Memory Chests and Cosmic Crates remain deliberately deferred. Maintain the normal cadence of roughly one or two enchantments per milestone; fewer are acceptable for especially risky infrastructure work.

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

For the accepted Step 6M implementation:

- `gradlew.bat test` passes all 247 automated tests across 74 suites with 0 failures and 0 errors. New coverage includes the four-player boundary, participant removal, paused/active timer semantics, duplicate-completion predicate, persistent session codecs, exact component-bearing inventory snapshot codecs and restored tombstones, one- and two-piece room definitions, imported NBT dimensions/marker counts, title/countdown copy and colors, default/creative/explicit-allow protection policy, Implants intervals/scheduling, and the 26-enchantment invariant.
- The dedicated server decoded and atomically published both Trial room definitions, loaded the dedicated void dimension and imported structures, retained 1,462 recipes and progression invariants, and reached `Done` without relevant datapack, attachment, command, dimension, structure, common-side classloading, or persistence errors.
- Fresh client startup completed mod/resource reload, OpenAL, and all texture atlases with the Trial Portal item definition, gateway block model, localization, and Implants data present and no relevant missing-model, missing-texture, malformed-resource, or client/server loading errors.
- The user manually verified the complete portal-to-Decision-to-development-room-to-Decision-to-outside loop, multiplayer lifecycle behavior, restart recovery, terrain protection, titles/timer, and Implants timing and accepted Step 6M.

For the accepted Step 6L implementation:

- The automated suite currently passes all 223 tests across 67 suites with 0 failures and 0 errors; focused coverage includes tier/table identity, exact production rows/weights and Unexamined Book semantics, persistence codecs and transaction invariants, the 80-tick reveal plus 30-tick pause, canonical presentation/sound policy, non-stackability, vanilla Creeper `SpawnData` and timing defaults, reward-bundle compaction, Blessed chances, and the 25-enchantment registration invariant.
- The dedicated server atomically published all three production Space Chest tables plus the existing development table, loaded 1,462 recipes, retained the progression recipe invariant, and reached `Done` without relevant registry, datapack, command, menu, attachment, or sided-classloading errors.
- Client startup completed resource reload, sound initialization, and texture-atlas creation with the Space Chest menu screen/item model registered and no relevant missing-model, malformed-resource, or localization errors. The client was deliberately terminated at the smoke-test boundary.
- Pinned environment values remain unchanged. The user manually verified and accepted the corrected Space Chest presentation, timing, sounds, Unexamined rewards, transactional recovery, and Blessed behavior. Creeper Spawner runtime spawning remains a deferred, non-blocking issue: the typed item and placed vanilla spawner preserve `minecraft:creeper` in `SpawnData`, and configuration diagnostics appear correct, but manual testing has not yet produced Creepers under apparently valid conditions.
- Step 6L is committed at `14415b3c1bc51c4f489599131814f68f76744c15` (`Add Space Chests and Blessed`).

For the accepted Step 6K implementation:

- `gradlew.bat cleanTest test build` succeeded; all 203 automated tests across 61 suites passed with 0 failures and 0 errors.
- All 125 main-resource JSON files parsed successfully and `git diff --check` reported no whitespace errors (only normal Windows line-ending notices).
- Dedicated-server startup loaded 1,462 recipes, atomically published the new development reward table alongside existing content, retained the progression recipe invariant, and reached `Done` without relevant registry, datapack, command, or sided-classloading errors.
- Client startup completed resource reload, sound initialization, and all texture atlases including the item atlas without missing-model, missing-texture, malformed-resource, localization, or item-registration errors. The client was deliberately terminated after the smoke-test boundary.
- Pinned environment values remain unchanged. The user manually verified reward generation, spawner placement, generated equipment, Auto Smelt, and Experience and accepted the milestone.

For the accepted Step 6J implementation:

- `gradlew.bat cleanTest test build` succeeded; all 187 automated tests passed with 0 failures and 0 errors.
- All 120 main-resource JSON files decoded successfully and `git diff --check` reported no whitespace errors (only the repository's normal Windows line-ending notices).
- Dedicated-server startup loaded 1,462 recipes, decoded both new enchantments and all item/component registrations, published current content, verified the progression recipe invariant, and reached `Done` without relevant registry, datapack, command, or sided-classloading errors.
- Client startup completed CosmicPVE resource reload, sound initialization, and item-atlas creation without missing model/texture, malformed resource, localization, or tooltip registration errors.
- Pinned environment values remain unchanged. The user manually tested the money/Banknote flow, Repair Scroll, fixed-percent reward items, Armored, Death Pact, and Glowing lease fix and accepted the milestone.

For the accepted Step 6I implementation:

- `gradlew.bat cleanTest test build` succeeded; 178 automated tests passed across 51 suites with 0 failures, 0 errors, and 0 skipped.
- All 115 JSON/datapack metadata resources decoded successfully and `git diff --check` reported no whitespace errors.
- Dedicated-server startup automatically enabled the required progression datapack, loaded 1,462 recipes, verified all eight forbidden armor recipe IDs absent and eight Iron/tool control recipes present in the live recipe manager, published current content, and reached `Done` without datapack, recipe, modifier, registry, or sided-classloading errors.
- Client startup completed CosmicPVE resource reload, sound initialization, and item-atlas creation without missing-model, missing-texture, malformed-resource, tint-source, localization, or item-registration errors in the runtime log.
- Pinned environment values remain unchanged. The user manually re-tested and accepted final-stack/full-stack Unexamined opening, Diamond crafting and recipe-book removal after a fresh restart, Netherite armor smithing removal, and Diamond-armor loot replacement. Glowing's visual flicker was subsequently fixed and runtime verified in Step 6J.
- Step 6I gameplay is committed at `8af58c5e7b8a39ff1f312f5d0ff6a1b25ce23ad1` (`Add progression cleanup and Unexamined Books`).

For the accepted Step 6H implementation and final accepted refinements:

- `gradlew.bat cleanTest test build` succeeded; 161 automated tests passed across 46 suites with 0 failures, 0 errors, and 0 skipped.
- All 102 main-resource JSON files decoded successfully and `git diff --check` reported no whitespace errors.
- Dedicated-server startup registered both entities and their attributes, published current content, and reached `Done` on NeoForge 21.11.45 without entity, datapack, or sided-classloading errors.
- Client startup completed resource reload, sound initialization, texture-atlas creation, and client entity-renderer/model-layer setup without missing-texture, invalid-model, malformed-resource, or renderer exceptions in the runtime log.
- Pinned environment values remain unchanged. The user manually verified and accepted Step 6H, including the shared `0.35` movement-speed attribute, and accepted the Heroic Crystal presentation/sound/non-stacking refinements. Glowing's later lease fix is recorded under Step 6J.
- Step 6H gameplay is committed at `da3a6334dabda9c148935a6e6a27cda5334af1ba` (`Add Space Pirates and Oxygenate`).

For the accepted Step 6G implementation:

- `gradlew.bat build` succeeded; 148 automated tests passed with 0 failures and 0 errors.
- Every main-resource JSON file decoded successfully and `git diff --check` reported no whitespace errors.
- Dedicated-server startup published current content and reached `Done` on NeoForge 21.11.45.
- Client startup completed resource reload, sound initialization, armor-trim and item-atlas creation without missing-model, malformed-resource, or CosmicPVE loading errors.
- Pinned environment values remain unchanged.
- The user manually verified the Heroic equipment flow, Glowing, Obsidianshield, armor-set inventory tinting, and Maui's Hook orientation and accepted Step 6G.
- Step 6G gameplay is committed at `3cda6081572024c2223db5016f3725f8da32caf1` (`Add Heroic equipment and passive enchantments`).

For the accepted Step 6F implementation:

- `gradlew.bat build` succeeded; 142 automated tests passed with 0 failures and 0 errors.
- All 74 main-resource JSON files decoded successfully.
- Dedicated-server startup loaded both new enchantment definitions, published current reloadable content, and reached `Done`.
- Client startup completed mod/resource loading, sound initialization, and item-atlas creation with no missing localization, malformed style, tooltip, model, or CosmicPVE resource errors in the runtime log.
- Pinned environment values remain unchanged.
- Step 6E is manually accepted at gameplay commit `bf64c77d5ac62952f801a81241801f3401c17e78`; its handoff/design baseline is `8fa9a650845ea631bf2a9be719c371ab1772089d`.
- The user manually verified the lore cleanup, Molten IV, and Nutrition III and accepted Step 6F.

Step 6F gameplay is committed at `b5148760420b658cd7006a73b230199f9311bc73` (`Add equipment lore Molten and Nutrition`).

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
