# CosmicPVE Development Handoff

Current accepted baseline: Step 7G/7G.1. Trials 1.1 and its corrective/presentation pass are implemented, automated/runtime verified, and manually verified/accepted. The ordinary Enchantments 1.0 content pool remains exactly 46 real enchantments.

Repository state: Steps 6A–7G.1 are implemented, automated/runtime verified, and manually accepted. The root `assets` source-art folder and root `trial rooms` structure-development folder remain deliberately untracked and unrelated.

Last handoff update: 2026-08-29

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

### Step 6N — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

The production Apprentice Trial loop now uses the Step 6M durable session rather than a parallel activity. Every completed room atomically appends one resolved reward bundle to the persistent ordered shared pot, adds 600 gameplay ticks, increments the room count once, and enters the paused Decision Box. The canonical reloadable Apprentice table produces Unexamined rarity books, scrolls, typed spawners, the Ultimate Space Chest, and three Orange-Dye-presented tier-1 Trial Trinket identities now consumed by the Step 6S portal-modifier system. Pot entries retain independent acquisition identities, so separately rolled duplicates do not merge or reroll.

The read-only 27-slot Decision menu exposes four DEAL panes, one blocked center pane, four NO DEAL panes, and ordered pot previews across rows two and three. DEAL is individual: the server durably records the payout, removes that player, restores the exact outside snapshot/location first, then safely delivers a full pot copy with overflow at the returned location. NO DEAL preserves both participation and the shared pot. Decisions resolve early when everyone responds; timeout treats undecided players as NO DEAL. The room selector uses `5 - 2 × appearances + modifiers`, excludes the immediately previous room, accepts injectable RNG, and has a contributor seam for future Snow Globes. The fourth Apprentice completion moves the persisted phase to Hardcore and applies the one-time 3-minute bonus. Because Step 6N has no Hardcore production room, NO DEAL is temporarily rejected safely at that final Decision Box while DEAL remains available; Step 6O must remove this development gate.

Raiding Rainbow and Circuit Circus use their actual tracked structure resources. Only the explicit resolved Emerald spawn-marker coordinate is consumed; an optional definition override or deterministic cardinal-neighbor inference replaces it with matching floor before teleport, leaving legitimate Emerald mechanics untouched. Raiding Rainbow owns one persisted shuffled eight-color sequence, shared progress, and a canonical active-Zombie factory shared by initial activation and every wrong-order reset; each fresh Zombie has 1 HP, normal AI/gravity/damage behavior, no loot/XP, wool identity plus authoritative color tags, and is added server-side as a new entity. Wrong kills retain the sequence, reset progress, remove survivors, and create a fresh full set. Circuit Circus persists a two-of-each randomized pillar assignment, independently claimable targets, 1-or-2 issued glass, bounded six-direction connectivity, one Beacon cue per newly completed circuit, and lever completion only after all four circuits are valid. Its scoped four-color placement exception is evaluated at item-use against the intended vanilla placement position and again at the resulting block-placement event; vanilla remains authoritative for placement and consumes exactly one block. Cleanup removes encounter entities/items and clears the declared room bounds before the next structure placement.

Generic Trial title presentation sends a targeted server sound packet to each participant: one basedrum per displayed one-second Decision/room countdown update and one Ender Dragon growl when a room crosses `ROOM_INTRO` to `ROOM_ACTIVE`; Decision entry never plays the growl. The same delivery seam is independently testable with `/cosmic trial debug sound countdown` and `/cosmic trial debug sound start`. Implemented Elite sword enchantment `cosmicpve:trap` III, bringing the total to 27 real Cosmic enchantments. Each committed positive-health-loss melee hit has a flat 4% Luck-modified chance at every level to apply Slowness V for 25/30/35 ticks; eligible Doublestrike child hits may reroll it through the established limited offensive policy. Trap has no cooldown, stack, bonus damage, or hurt-immunity manipulation.

### Steps 6O/6P — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

The production Trial lifecycle now advances from Apprentice to Hardcore after room four and from Hardcore to the temporary Demonic boundary after room eight. The fourth Apprentice completion retains its reward and +600 ticks, then applies the persisted one-time +3,600-tick Hardcore entry bonus. Each Hardcore completion appends exactly one resolved Hardcore reward bundle, adds +300 ticks, and advances once. The eighth overall completion retains that reward/time before applying the persisted one-time +3,600-tick Demonic entry bonus. DEAL remains available at the Demonic boundary; NO DEAL safely reports that production Demonic rooms are not enabled. Hardcore selection reuses the existing weighted/no-consecutive-repeat selector with exactly Fire Colony and Zero-G.

The development-partial Hardcore reward table contains the currently generatable canonical rows at their canonical weights and quantities: +3-minute and Skip-2 trinkets, 75% Black Scroll, randomly rated Armor/Weapon Orbs, Blaze/Creeper spawners, Unexamined Legendary and Ultimate Books in both quantity forms, Skip-1 Trinket, White Scroll, Repair Scroll, Legendary Space Chest, and the Step 6T Mask, Mask Splicer, and 35% Yeti Crystal prerequisites. Random Boss Spawn Egg, Abandoned Spaceship Dungeon Portal, 35% Ranger Crystal, and broader unsupported Demonic dependencies remain deliberately unresolved rather than represented by fake rewards.

Fire Colony uses its tracked production structure, explicit Emerald-marker replacement, red leather/no-enchantment armor, and one Golden Apple. Only its final lever completes the room. Its normal fire, soul-fire, and lava damage remains active while a cached bounded hazard-neighborhood baseline is restored every ten ticks to prevent lasting fire spread, burning, and lava-flow mutation without global gamerule changes. Zero-G uses its tracked production structure, ten persisted one-shot Cherry pressure-plate objectives, eight fixed real Shulker fixtures, and deterministic iron armor/loadout. Each armor piece has Protection IV, Angelic V, and Unbreaking III; the hotbar contains two Golden Apples and three Milk Buckets. The Shulkers retain normal targeting/projectiles but are invulnerable, UUID-tracked, re-pinned to their fixtures, drop no loot/XP, and are cleaned with bounded Shulker-bullet removal. Only participating players can claim plates; each claim has an independent 25% Ender Pearl chance for its activator, and the tenth completes the room once.

Nutrition III was already a registered, accepted real enchantment from Step 6F and remains unchanged: successful food completion with enchanted leggings adds +1 hunger and +0.25 saturation per level through bounded vanilla food state. It already integrates with Books, Unexamined Unique Books, Black Scroll, capacity, Transmog, lore, and persistence. Step 6O verified this existing implementation rather than duplicating it, so the real-enchantment count remains exactly 27, not 28.

Step 6P added the bounded four-room QOL/performance corrective pass. Zero-G's manual `1/10` reset was caused by the same tick publishing the updated encounter and then republishing a timer-decremented copy of the stale pre-activation session. Objective processing now carries one authoritative immutable completed-position set forward through the timer update, checks at a bounded ten-tick cadence, broadcasts its exact set size, and publishes the ten-position state before the normal exactly-once completion transaction. `/cosmic trial debug status` exposes the count and completed positions.

Trial participants receive a client-only sidebar-style `M:SS` HUD driven by authoritative server payloads only when the ceiling-formatted displayed second changes; Decision states retain the frozen value, and all participant-removal/recovery paths hide it. This does not replace or mutate the world's scoreboard. Decision announcements are brief flashes only at 30, 25, 20, 15, 10, 5, 4, 3, 2, and 1 seconds, with one basedrum per visible flash; room 5–1 countdowns and the room-start growl remain unchanged. Solo NO DEAL already resolves through the shared ready predicate and proceeds directly into the normal 100-tick `ROOM_INTRO`, not immediate active gameplay.

The 27-slot Decision screen now renders the vanilla three-row region plus the actual seven-pixel lower frame from the canonical container texture; its logical slot origins/hitboxes are unchanged, and the unused player-inventory label is suppressed. Each genuine Decision entry performs cleanup and teleport, then directly invokes the same `PlayerUtilityService.restore` heal/feed behavior as `/restore`, once, before presentation/menu opening. Menu reopening does not repeat restore. Raiding Rainbow now supplies full temporary Netherite armor with Unbreaking III, its existing Wooden Sword with Unbreaking III, and 16 Cooked Porkchops, with no Ender Pearls.

Successful DEAL schedules one harmless client-side Minecraft firework burst per actually completed room at offsets 0, 10, 20, ... ticks after outside restoration and payout delivery. These payload-driven particles create no rocket items, entities, explosion damage, or terrain effects; disconnection simply cancels subsequent scheduled launches. Confirmed waste removed: the Decision menu no longer rebuilds 27 preview stacks every broadcast tick. Likely hot paths improved: Zero-G fixture/objective maintenance changed from every tick to every ten ticks, stable positions/UUIDs remain cached, and volatile timer SavedData checkpoints changed from forced synchronous one-second flushes to five-second flushes. Snapshot creation, entry/exit/DEAL/death/disconnect, room/phase transitions, and other meaningful transactions retain immediate durable persistence.

### Step 6Q — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Closure commit: `4bf477fa2fa7a9fd03a2474856a154347814af7d` (`Add Cold Snap, Trial owner HUD, and Cactus`).

Cold Snap is the third production Apprentice room. Its tracked structure uses the explicit Emerald marker at local `[5,10,17]`, inferred Packed-Ice floor replacement, Gold plate `[11,14,9]`, Iron plate `[1,14,32]`, two-block Iron Door `[3,14,32]`/`[3,15,32]`, and final lever `[36,15,2]`. Initialization caches and hides only the exact bounded 26 ordinary-Ice shortcut blocks at local Y 13–14 before teleport; the other 2,450 ordinary-Ice structural/decorative blocks and 217 Powder Snow blocks are untouched. Plate behavior is event-driven and idempotent: the Gold plate removes both door halves without drops, the Iron plate restores the full cached route in one activation, and only the final lever completes the room. Cleanup discards cached state and structure cleanup restores the pristine room. The temporary loadout is Diamond Boots with Feather Falling IV and 16 Cooked Porkchops. The production Apprentice pool is exactly Circuit Circus, Raiding Rainbow, and Cold Snap under the existing weighted/no-consecutive-repeat selector.

Trial sessions persist immutable portal-owner UUID and name-snapshot identity. The owner identity survives owner departure and all room/Decision transitions without per-tick packets; Trial exit clears participant HUD state. The current possessive owner/phase/timer presentation is recorded in Step 6R below.

Implemented Elite `cosmicpve:cactus` II on leggings, bringing the total to 28 real Cosmic enchantments. A committed damaging hit with a meaningful attributed living attacker creates one 3%/6% Luck-modified defensive candidate. Success delivers exactly 1.5 HP standard Cosmic true damage to the attributed attacker (including projectile owners) through a `NO_PROCS` child, preserving absorption semantics while preventing retaliation chains. Environmental/unattributed, rejected, zero-health-loss, self, and dead-attacker cases produce no useful candidate. Cactus has no cooldown, CombatStack, status effect, or other damage packet and integrates through the normal Book, Unexamined Elite, Black Scroll, capacity, Transmog, lore, persistence, and command seams.

### Step 6R — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Closure commit: `44fb15489b7b10479a0aa25278e43ebc7dd9c2cf` (`Add Bomb Squad and Trial phase HUD`).

Bomb Squad is the third production Hardcore room. Its tracked `45×21×45` structure contains four shared-party Emerald starts, four event-driven Gold egg-supply plates, nineteen cached Diamond exit markers, and a stable seven-block 6×6 grid mapping. Each attempt chooses one start and two distinct exit cells outside the start's clipped 3×3 neighborhood. All Emerald/Diamond metadata is replaced before arrival; selected exits become protected Heavy Weighted Pressure Plates. The temporary loadout is full Leather Armor with Blast Protection I/Unbreaking III, Unbreaking III Flint and Steel, Knockback II/Unbreaking III Wooden Sword, five Steak, and five Golden Apples.

Room-issued typed Creeper Eggs create tracked ordinary Creepers only during the active attempt. Per-player supply eligibility is inventory-based, so one player holding an egg cannot block another. Tracked Creepers retain vanilla AI, fuse, ignition, damage, and knockback while awarding no XP/loot. Their localized mob-grief permission and detonation exception allow only ordinary Stone inside current Bomb Squad bounds; the structure's low-resistance glass and Sea Lantern surfaces remain protected along with Obsidian, Bedrock, all Gold supply plates, and both active Iron exits. No gamerule is changed. Cleanup discards tracked Creepers and the existing structure lifecycle restores every destroyed passage before replay. The production Hardcore pool is exactly Fire Colony, Zero-G, and Bomb Squad under the shared weighted/no-consecutive-repeat selector.

The participant HUD now renders the immutable possessive portal-owner heading (`Dev's Trial`), authoritative phase, and existing `M:SS` timer. Phase colors are Apprentice `#E6E032`, Hardcore `#E6A732`, and Demonic `#E65C32`. Owner and phase packets are cached and sent only on initialization/change; timer suppression is unchanged. The future `Room #N---Room Name` line remains deferred.

### Step 6S — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

All nine Orange-Dye Trial Trinkets now share typed/versioned family-and-value data: Time +1/+3/+5 minutes, Skip 1/2/3 rooms, and Insurance 1/2/3. Server-authoritative drag-and-left-click application stores one independent value per category on a Trial Portal; stronger same-category values replace weaker ones, while equal/weaker or stale applications reject without consumption. Portal tooltips show only applied categories. Portal creation copies the immutable values into the persistent Trial session, after which the consumed item is no longer consulted.

Time is incorporated once into the initial 12,000-tick clock, producing 13,200/15,600/18,000 ticks for +1/+3/+5. Skip resolves and persists one independent Apprentice reward bundle per skipped room after the initial joining period and before the first playable room, increments overall progression without room appearances or +600 completion bonuses, and never creates a pre-game DEAL opportunity. Insurance selects up to its level in complete pot entries without replacement for each failed participant, durably stages the exact selected reward contents, restores outside state first, and then safely delivers the copied salvage. It applies only to participant death and timer expiration; DEAL, voluntary/admin restoration, and disconnect alone do not invoke it.

The existing 27-slot Decision menu now pages the ordered pot in 18-entry views through a per-menu top-center control. Navigation wraps, is player-local and read-only, and never mutates, merges, rerolls, or limits the persisted pot. DEAL and NO DEAL remain independent of page index, and DEAL always pays the complete pot. The participant HUD now adds lifecycle-cached `Room #N---Room Name` during room intro/active and `Decision Box` during both Decision states; N is overall progressed room ordinal, including Skip. Timer packets retain their displayed-second suppression.

Eligible equipment now presents `N Enchantment Slots` in `#55FF55`. An Orb suffix appears only above base capacity and is derived from effective capacity minus the canonical base five, for example `8 Enchantment Slots (Orb [+3])`; actual capacity/application rules are unchanged. Armor and Weapon Enchantment Orb names now use the same `#55FF55` green while preserving their Eye-of-Ender model, no-glint state, rates, and transactions.

The Design Doc subsection `Enchantments → Balance Changes` is prospective staging material. It does not change implemented enchantment behavior unless a later prompt explicitly authorizes a balance patch.

### Step 6S.1 — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Trial Trinkets now reject any target Trial Portal stack whose count is not exactly one. A successful application stores both the typed portal modifiers and vanilla `max_stack_size=1`, so modified portals cannot merge with unmodified, differently modified, or identically modified portals. The exact same invariant owns command-created modified portals, persists with the stack, and is revalidated when a portal is used. Trinket names and matching portal modifier lines now use Skip `#2BC2B8`, Time `#0A5751`, and Insurance `#0A5721`.

Every production room now anchors each participant to the resolved room spawn during `ROOM_INTRO`, zeros accumulated movement, preserves camera rotation, and stops anchoring immediately on `ROOM_ACTIVE`. Because the lock is derived only from the authoritative lifecycle and does not persist attributes/effects, abort, death, disconnect, restoration, debug transitions, and restart cannot leave an outside player immobilized. A valid first-time Circuit Circus target hit plays `minecraft:entity.arrow.hit_player` only to its shooter. The participant HUD retains normal text scale but uses a 132-pixel-tall card, at least 150 pixels of width, 18-pixel horizontal padding, and 27-pixel line spacing; dynamic width is clamped to the screen and long owner/room text is ellipsized.

The bounded performance pass moved Fire Colony's already ten-tick maintenance gate to the central room dispatcher, so the service is no longer invoked on the other nine of ten ticks; Zero-G remains ten-tick-only; room dispatch now resolves its room ID once; participant delivery uses allocation-free bounded loops; unchanged HUD room content is cached before content/name resolution; stale HUD participant scans allocate only when a participant actually becomes stale; protection checks reuse one active-session lookup and bounded loops; and volatile timer updates no longer dirty SavedData every tick, while the established five-second checkpoint explicitly marks and synchronously flushes it. `/cosmic trial debug perf` reports a rolling 200-active-tick average/max without per-tick logging.

The generic placed Spawner investigation traced the pinned 1.21.11 `BaseSpawner` path and found no Cosmic entity-ID, SpawnData, timing, update, or synchronization defect. All representative typed items use the same configuration path and retain vanilla delay/count/range fields. The observed split is explained by vanilla `SpawnPlacements`: Blaze accepts any light, Iron Golem uses the basic mob predicate, ordinary hostile mobs such as Creeper/Zombie still require their spawner-reason darkness predicate, and Pig/Sheep still require an animal-spawnable floor and sufficient light. `/cosmic reward spawner inspect <x> <y> <z>` exposes the complete live state/environment and `/cosmic reward spawner debug-delay <x> <y> <z> <ticks>` shortens only the current delay for controlled testing; normal subsequent delays remain vanilla.

### Step 6S.2 — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Trial Portals now present concise creation guidance followed by a bold warm-gold `Trinkets:` section. Active modifiers use gold `✖` bullets, stable Skip → Extra Time → Insurance ordering, their established family colors, and muted player-facing explanations with correct singular/plural grammar; an unmodified portal shows muted `None`. Tooltip construction is read-only, and the accepted single-stack invariant for modified portals is unchanged.

The participant HUD is a fixed 120-pixel-wide card anchored flush to the right GUI edge and vertically centered. It presents the immutable possessive owner heading, `Tier (1/3)` / `(2/3)` / `(3/3)` with the established phase colors, separate `Room (#N)` and room-name lines, and `Time Left` with `Xm YYs`. Long content ellipsizes inside the stable width. Room ordinal/name remain lifecycle-cached, and the existing owner/phase/room change suppression plus displayed-second timer suppression are unchanged.

Normal Ultimate, Legendary, and Mastery Space Chests now play `minecraft:item.armor.equip_netherite` exactly once for each newly selected valid pane. Rejected/repeated selections, committed rewards, reveal/close behavior, and Memory Chests are unchanged. The earlier `Enchantments → Balance Candidates` staging material was prospective at this milestone; Step 7C later explicitly authorized the canonical Design Doc enchantment table as the implemented balance baseline.

### Step 6T — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Implemented nine reloadable, validated mask definitions and one generic non-stackable typed Mask item. Helmet attachments persist an ordered, distinct 1–5-mask loadout without replacing the helmet or altering its other components. Single masks use their canonical profile data; 2–5-power Multi-Masks use the shared canonical profile and stable `Multi-Mask (A, B, ...)` presentation. Each created stack also synchronizes its resolved immutable display name, effect summary, color, and visual profile, so dedicated-server clients render tooltips and heads without owning the server datapack repository; gameplay continues to resolve stable IDs against the authoritative server snapshot. Server-authoritative drag/drop applies the layer only to helmets, and empty-cursor secondary click removes it and returns exactly one single- or Multi-Mask with the same order. The client uses Minecraft's vanilla player-head render state over the unchanged armor model.

The concrete Santa, Reindeer, Purge, Party, Lover, Scarecrow, Zeus, Turkey, and Dragon effects use the established attribute, combat-contributor, ProcEngine, effect-applicability, and periodic-tick seams. Reindeer grants +5% movement speed. Turkey contributes one Luck-modified 2% targeted Dodge candidate before red-health loss; Zeus/Dragon immunities are scoped to their documented lightning/fire/poison effects. Loose Mask lore uses the canonical Design Doc information text, while an attached helmet shows one compact identity-only `ATTACHED:` line plus its removal instruction.

Normal Multi-Mask creation uses the vanilla anvil and flattens left input followed by right input without nesting or duplicate identities. The persistent global `/masklimit [2..5]` setting defaults to 3 and gates only newly created anvil outputs; the absolute typed-data limit remains five, so existing/admin/reward-created 4/5-power items remain valid when the setting is lowered. Minecraft 1.21.11 refuses anvil pickup at a literal zero displayed cost, so the supported hook uses a one-level pickup gate and immediately refunds that level after a valid craft for exactly zero net XP and no prior-work escalation. The one-use Mask Splicer is disassembly-only: dragging it onto a loose 2–5-power Multi-Mask consumes both items and safely returns ordered single Masks through inventory/overflow delivery.

Single and Multi-Mask render paths both suppress only the client helmet model and render their selected vanilla player-head profile while leaving the helmet mechanically equipped. Equipment identity metadata stays ahead of the capacity line. The complete attached Mask/Multi-Mask identity is reserved as one physical row: its full rendered width expands the tooltip instead of entering ordinary text wrapping. A focused client-only inventory/container tooltip scroller activates only for an actually overflowing item tooltip, accumulates approximately one row per wheel notch through the full geometric overflow range, clamps only at the real top/bottom bounds, and resets on item/slot/screen changes without packets or persistent state.

Reward descriptors can now generate a random distinct Mask bundle or an exact-rate existing Armor Set Crystal through shared factories. The current Hardcore development table adds a single Mask, Mask Splicer, and 35% Yeti Crystal without inventing Ranger or other missing systems. Permissioned `/cosmic mask give`, `give-multi`, and `splicer give` tooling covers exact 1–5-power testing. The user manually verified and accepted the complete Mask/Multi-Mask flow, anvil creation, persistent creation limit, Splicer disassembly, rendering, effects, reward integration, full-range tooltip scrolling, and the non-wrapping attached identity row.

### Step 6U — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Implemented `cosmicpve:undead_corpse` as a reusable hostile mob, not room-private logic. It uses Zombie AI/model/texture at 0.9 visual and collision scale, 15 HP, movement speed 0.4, zero innate attack/armor/toughness, and an Iron Axe whose Sharpness III, Bleed III, and Rage III rolls are independently 30%. Its axe remains the source of melee damage, and actual Cosmic enchantments resolve through the shared LivingEntity combat path. The canonical entity has no default loot; Trial variants additionally suppress XP and all equipment drops. Permissioned corpse spawn/inspection commands expose generated equipment without changing production behavior.

Hidden Graveyard is the first production Demonic Trial room and uses the existing durable session, inventory, transition, protection, room weighting, and cleanup foundations. The supplied 40×18×43 structure is tracked as `trial/hidden_graveyard`; its exact spawn marker, eleven authored Coal chest markers, central well, and three exact six-grave block groups are resolved once from audited structure coordinates. A bounded room-local Pale Garden biome override is applied for the attempt and restored to the instance baseline during cleanup. The accepted post-room-eight Demonic transition now continues through the normal mixed Apprentice/Hardcore/Demonic selection pool instead of stopping at the former no-room development gate.

One typed/versioned vanilla-presented Trial Key is active at a time and is bound to the persistent Trial session, room attempt, and key sequence. The directly tracked ordinary chest contains exactly one current key and disappears without drops when emptied. Only the current key ItemEntity inside the tightly bounded well region advances a wave; ordinary, stale, wrong-session, and misplaced keys are not consumed. Low-cadence recovery examines only active participants and bounded room ItemEntities, and deterministically respawns a missing current key without duplicating a logically active one.

Each key destroys only its cached authored Cobblestone/Andesite/Stone grave group and spawns six provenance-tagged Corpses. Wave 1 uses Leather; Wave 2 uses Chainmail Protection I plus independent Luck I–X boots/leggings; Wave 3 uses Iron Protection II plus independent Luck V–X boots/leggings and Aegis I–VI chestplate. Three kills from the matching first or second wave unlock the next key exactly once; earlier survivors remain, permitting the intended 3+3+6 overlap. Completion requires Wave 3 to have started and every tracked Corpse from all waves to be dead, then commits one Demonic pot result and returns the party immediately to Decision. Demonic room completion adds no ordinary per-room time bonus. Cleanup removes Corpses, keys/items, chest/runtime state, temporary inventory, biome override, and placed structure state.

Hidden Graveyard's hot path is event-driven for deaths/completion. Fixed marker, well, and grave data are cached; spawned entities are tracked by UUID; and only the tightly bounded key/chest maintenance runs at a 10-tick cadence. No per-tick structure parsing, full-room block scan, world-wide entity scan, or stream-heavy participant search was added. The currently supported Demonic development reward table contains only already implemented canonical reward primitives and deliberately omits unresolved Ranger, Conquest Flare, M-Kit, boss-egg, Dungeon Portal, and Memory Chest rows rather than faking them.

### Step 6V — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Deadeye is the second production Demonic Trial room. Its authored West 41×31×43 and East 30×31×43 structures use the existing multi-piece placement service, with East placed at a +41 X offset and one combined 71×31×43 protected/cleanup footprint. The explicit West Emerald marker supplies the participant spawn and `spawnY - 10` execution threshold; the authored East lever is the only completion trigger after progression is complete. The temporary loadout is Leather Leggings with Nutrition III, 16 Golden Apples, exactly one Arrow, and a Transmogged Bow with Infinity I, Power V, Lightning IV, Unbreaking III, Flame I, and Eagle Eye V.

The authored structures contain four actual target blocks across the Design Doc's five visual themes. Deadeye therefore uses four exact ordered, room-local target coordinates and four cached immutable reveal groups: Diamond, Gold, Resin/Purpur transition, and Crimson. The resin target reveals the authored resin route plus the purpur West/East transition; this preserves every authored theme without inventing a fifth target. Only a participating player's arrow on the current target advances shared state. Old, future, wrong, and nonparticipant hits do nothing. Reveals restore the cached authored BlockStates without drops, and the exact final lever refuses premature completion.

Falling to or below the cached threshold invokes the existing terminal environmental execution path rather than damage. Armor, absorption, ordinary mitigation, Phoenix-style prevention, and normal procs are bypassed; normal individual Trial death/Insurance handling removes only that participant and survivors continue. The active-room hot path is one cached integer comparison per online Deadeye participant per tick, with no structure scans or metadata discovery.

Trial 1.0 production registration is now three Apprentice rooms (Circuit Circus, Raiding Rainbow, Cold Snap), three Hardcore rooms (Fire Colony, Zero-G, Bomb Squad), and two Demonic rooms (Hidden Graveyard, Deadeye). The accepted Demonic mixed-pool selection, declining appearance weights, no-consecutive-repeat rule, Snow Globe modifier seam, Skip behavior, Decision/pot semantics, persistence, and snapshot restoration remain unchanged. Room teardown now performs a second ItemEntity-only purge inside the outgoing room AABB after block removal, closing the generic ladder/pressure-plate/support-drop leak without scanning the dimension or touching outside-world items.

### Step 7A — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Conquest Chests are persistent Overworld events managed independently by stable UUID. A low-frequency scheduler records each handled seventh Minecraft day before attempting one natural spawn, preventing restart/time-skip duplication without allowing Flare events to consume the natural schedule. Natural placement uses 32 bounded attempts with independent X/Z coordinates from -5000 through +5000, the surface heightmap, a sturdy dry floor, an empty chest position, at least two air-adjacent faces, and rejection of overlapping 20×20 event regions. Multiple non-overlapping natural and Flare events may coexist.

The event block is a real non-dropping Chest block with vanilla Chest rendering, 80 hardness, an explicit `minecraft:mineable/pickaxe` tag, required-correct-tool semantics, and unchanged 1,200 blast resistance. Its Survival break path starts with Minecraft's actual pickaxe speed, Efficiency attribute, effects, and harvest check, then compresses acceleration above the ordinary Iron Pickaxe baseline so the accepted bands can coexist: the real-level test observes 20.05 seconds with unenchanted Iron and 10.05 seconds with Netherite Efficiency V. Active event bounds deny ordinary survival breaking except the discovered Conquest Chest, placement, item-on-block mutation, explosions, pistons, fluids, LivingEntity block destruction, and mob griefing without changing global gamerules; creative development bypass remains available. NeoForge does not expose a single complete cancellable hook for every scheduled vanilla fire mutation, so fire protection uses the available scoped mutation hooks rather than a global fire gamerule. Event ownership/protection disappears on completion, expiration, or administrative removal.

The first valid interaction prepares and awakens exactly 3–5 of the existing canonical Space Pirate variants in bounded valid nearby positions, persists the once-only ambush receipt before entity insertion, and rejects discovery/mining when fewer than three guards can be placed. These are persistent world mobs with their existing zero-equipment-drop policy; surviving Pirates deliberately remain after the chest event ends rather than being silently despawned. Natural events announce coordinates globally, repeat a persisted remaining-time reminder every five minutes while untouched, and expire without loot after 30 minutes only if never interacted with. A stackable Redstone-Torch-presented Conquest Chest Flare (`#BF0000`) performs the same bounded surface/overlap validation within ±100 X/Z, consumes exactly one only after success, does not announce globally, and has no invented unattended expiry.

Mining a discovered chest commits completion once, makes exactly three independent rolls from the reloadable `cosmicpve:conquest` reward table, and adds one guaranteed Banknote from $100,000 through $1,000,000 inclusive in exact $10,000 increments. Concrete resolved stacks and the recipient are persisted before safe inventory/overflow delivery, with recovery for committed pending delivery. The supported production table retains the authored weights for Unexamined Simple/Unique/Elite/Ultimate/Legendary Books, Trial Portals, Transmog Scroll, White Scroll, 50%/65% Black Scrolls, Repair Scroll, and Legendary/Ultimate Space Chests. Random Boss Spawn Egg and Gkit Refresher rows are omitted without substitution because those reward primitives remain unimplemented.

Permission-gated tooling supports natural/local Flare event creation, listing, UUID inspection, administrative expiration, and Flare delivery. Runtime work is bounded: scheduling/expiry/reminders/recovery run every 100 ticks, placement retries are capped, active-event collections remain naturally small, Pirate search is a fixed nearby grid, and protection checks use cached event bounds rather than terrain/entity scans.

The first Step 7A candidate passed unit/build and startup checks but failed its core manual test: every shared command/Flare creation path reached block placement, where inherited `ChestBlock.newBlockEntity` constructed a vanilla `minecraft:chest` block entity for the custom `cosmicpve:conquest_chest` state. Minecraft rejected that mismatched block-entity state, so no usable chest could be created. `ConquestChestBlock` now explicitly constructs `ConquestChestBlockEntity`; the final placement transaction immediately verifies the block and custom block entity before publishing SavedData, and rolls the block back if persistence fails. Surface resolution now performs bounded chunk access, accepts ordinary replaceable dry surface vegetation, and retains sturdy/dry/exposure/overlap validation. `/cosmic conquest spawn-here` provides deterministic nearest-surface diagnosis through the same production creation transaction; `list`/`inspect` expose live block and block-entity consistency.

Registered NeoForge real-level GameTests now cover an ordinary flat/replaceable surface, buried/lava/occupied rejection, no ghost event, physical block plus custom block entity plus persisted event, first interaction with 3–5 physical Space Pirates, exactly-once reward completion including the guaranteed Banknote, actual server Flare success/one-item consumption and failure/zero consumption, all three registered admin creation routes, and the real Survival mining calculation with loaded tags and Efficiency attributes. These regressions exist specifically because the original pure unit/startup suite could not detect invalid runtime block-entity or tool-tag behavior.

### Step 7B — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

`/tinkerer` is a normal-player command that opens a three-row chest-style server menu. Slot 0 is a read-only Red Stained Glass `Tinker Items` confirmation control; the other 26 menu slots accept only examined Cosmic Enchantment Books through ordinary clicks or shift-click. Vanilla books, Unexamined Books, and unrelated items are rejected. Closing before confirmation returns deposited books through the normal menu lifecycle. Confirmation is a single server-thread transaction: each eligible book yields `min(10, 1 + level + floor(success/10))`, Destroy Rate is ignored, outputs aggregate by rarity, the deposited books are consumed once, Dust is inserted into player inventory with normal safe overflow behavior, and `minecraft:entity.chicken.egg` plays once. Empty or replayed confirmation produces nothing and no success sound.

Cosmic Dust is one typed, versioned, stackable Sugar-presented item carrying a `CosmicEnchantmentTier`. Simple, Unique, Elite, Ultimate, Legendary, and Mastery variants use their canonical rarity names and colors. Primary drag/drop onto an examined Cosmic Book enters the existing server-authoritative item-application path; unrelated targets and empty slots retain vanilla behavior. Matching Dust adds one flat Success percentage point per unit, bulk-consuming only the useful amount. Ordinary books cap at 100%, Mastery caps at exactly 50%, and wrong-rarity or capped interactions consume nothing. The application replaces only the typed book Success Rate field, preserving enchantment ID, level, Destroy Rate, schema version, custom presentation, and all other stack components.

Permission-gated development support is available at `/cosmic tinkerer dust give <player> <tier> [count]`; the established exact Cosmic Book command remains `/cosmic enchant book give <player> <enchantment-id> <level> <success> <destroy>`.

### Step 7C — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Implemented Legendary boots enchantment `cosmicpve:gears` III at +5% movement speed per level through one stable transient equipped modifier. The modifier is reconciled for players and other LivingEntities, updates on equipment changes, and is removed when the boots are removed. Implemented Mastery chestplate enchantment `cosmicpve:permafrost` VI through the generalized independent CombatStack system: 2.5% per level defensive proc chance, 60-second independent negative stacks, one binary +2% incoming/-2% outgoing ordinary-damage penalty while any stack exists, threshold `10 - level`, full stack clear, and a 6-HP standard Cosmic true-damage burst. Active Yeti set immunity filters Permafrost before RNG. Implemented the newly canonical Mastery helmet enchantment `cosmicpve:mortal_coil` II: a 3% per level committed ordinary-damage defensive proc grants two full absorption hearts for five seconds, with legacy over-level data clamped to II behavior.

The approved canonical balance baseline is active: Bleed is 1.5% per level; Death Pact outgoing is `-(7.5 - level)%` and incoming is `-(1 + level)%`; Auto Smelt is Ultimate; Experience is Unique; Molten is 3% per highest equipped level; Obsidianshield II now reduces fire-category damage by 25% per level without maintaining Fire Resistance; Poison is Unique at 7% per level; Pummel is 3% per level with Slowness II for 3 seconds; Rage gives `(5 + level)%` within a `(4 + level)`-second three-hit window; and Doublestrike children use 75% of parent ordinary damage. Rarity changes flow through generic Unexamined Books, lore, Dust, Black Scroll, Transmog, and capacity behavior because the canonical specs are the single metadata source.

### Step 7D — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Implemented Unique leggings enchantment `cosmicpve:self_destruct` III. A committed ordinary hit that leaves the living wearer below 15% health triggers once per 1,200-tick cooldown and creates four owner-attributed vanilla lit TNT exactly one block north, south, east, and west. Only those tagged TNT grant their owner damage immunity, and their detonation clears the affected-block list without changing gamerules or suppressing damage to other entities.

Implemented Mastery boots enchantment `cosmicpve:phoenix` III through the existing pre-death/death-prevention hook. An ordinary lethal combat death with a meaningful living attacker is canceled when Phoenix is ready, health becomes exactly 40% of current maximum, and the 1,800-tick cooldown begins. The terminal `ExecutionService` bypass remains first-class, and unattributed environmental deaths do not qualify. Phoenix has no obsolete next-hit bonus and no level-dependent survival scaling.

Implemented Mastery sword enchantment `cosmicpve:divine_immolation` IV at 3% per level with a 600-tick cooldown. Activation ignites the wielder for five seconds, delivers a separate 2-HP standard Cosmic true self-damage child with `NO_PROCS`, and maintains one non-stacking +10% ordinary outgoing contribution for 100 ticks. Implemented deterministic Unique bow/crossbow enchantment `cosmicpve:virus` III: a committed projectile hit against a target that was already poisoned deals 0.4 HP standard Cosmic true damage per level and, after accepted child delivery, heals the credited living shooter for exactly 1 HP. Virus consumes no RNG.

Implemented Legendary axe enchantment `cosmicpve:devour` IV through a dedicated pre-calculation proc hook so its 5% Luck-eligible roll can add 5% ordinary parent-hit damage per level in the shared additive bucket. A zero-hunger player is filtered before rolling. Hunger, the exact 1-HP heal, and the normal eating sound commit once only after positive red-health loss; absorption-only/canceled/rejected hits do not consume hunger or heal, and Doublestrike children cannot reroll Devour.

### Step 7E — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Implemented Elite any-armor `cosmicpve:undead_ruse` X through one defensive committed-hit candidate using only the highest equipped level. Its Luck-modified chance is 0.5% per level and its bounded per-owner active cap is 1 at I–IV, 2 at V–IX, and 3 at X. It reuses the real `cosmicpve:undead_corpse`, canonical independently rolled equipment, and LivingEntity combat pipeline. Summoned Corpses persist a vanilla-compatible owner reference and independent 900-tick expiry, award no XP or loot, cannot target their owner or same-owner allies, and use narrow defend/assist-owner goals. Bounded owner-to-Corpse IDs provide cap accounting without world-wide tick scans.

Implemented one reusable owned-ally resolver over vanilla `OwnableEntity`; it covers ordinary tameables and summoned Corpses while excluding players and arbitrary nearby mobs. Legendary chestplate/leggings `cosmicpve:leadership` X resolves the owning LivingEntity's current two eligible armor pieces when the ally attacks, sums them to a 20-level cap, and adds 1% per level only to the shared ordinary outgoing bucket.

Implemented Simple all-weapons `cosmicpve:obliterate` III as a fixed 10% Luck-eligible committed-hit proc available only while the wielder/shooter is strictly below 20% maximum health. It adds no damage and applies real collision-respecting knockback tuned around the intended 3/6/9-block identity. Implemented Mastery axe `cosmicpve:soul_tether` III as a 10% Luck-eligible proc with a 600-tick cooldown and owner-specific 120/140/160-tick relationship. A tether supplies one stable -20% movement modifier while active and adds uncapped `5% × current distance` ordinary outgoing damage only to its creating owner; expiration/death/removal cleanup is folded into the existing shared LivingEntity tick bridge.

Implemented Ultimate boots `cosmicpve:dodge` V at 0.5% per level at the accepted pre-health-loss damage boundary. Turkey Mask contributes +2 flat percentage points to the same single candidate before centralized relative Luck multiplication. Activation sets the pending ordinary health loss to zero; no committed hit is produced, so the attacker's committed offensive proc chain and committed-damage defensive effects do not run. Turkey without Dodge retains its accepted 2% behavior.

### Step 7F — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED / ACCEPTED

Implemented Mastery axe `cosmicpve:hero_killer` III as a 3%-per-level shared ordinary outgoing contribution that qualifies only when the central armor-set resolver reports an active target set. Implemented Mastery any-weapon `cosmicpve:soul_siphon` IV at the committed positive-red-health boundary: any qualifying hit whose resulting target health is below 50% heals exactly 1 HP and starts its per-wielder `(7 - level)`-second cooldown. This intentionally does not require a strict above-to-below threshold crossing.

Implemented one ephemeral armor-set suppression service used directly by the authoritative resolver. Mastery sword `cosmicpve:blackout` IV has a centralized Luck-modified 2%-per-level chance to replace/refresh suppression for 1–4 seconds without mutating equipment identity. While suppressed, ordinary armor-set combat bonuses, proc modifiers, immunities, and Hero Killer eligibility all resolve inactive; the intact set resumes automatically after expiry.

Implemented Ultimate boots `cosmicpve:ender_walker` V as a 10%-per-level incoming reduction only for Minecraft's Wither damage type and NeoForge's dedicated Poison damage type. It does not reduce Wither Skeleton melee, Wither Skull damage, generic magic, or standard Cosmic true packets. Implemented Elite helmet `cosmicpve:voodoo` VI as a Luck-eligible offensive 1%-per-level proc using the generalized `INDEPENDENT` negative-stack policy: each of at most five stacks expires after its own 200 ticks and contributes -3% to the affected entity's shared ordinary outgoing bucket, capped at -15%.

### Step 7G/7G.1 — IMPLEMENTED; AUTOMATED/RUNTIME VERIFIED; MANUALLY VERIFIED/ACCEPTED

Implemented production Hardcore room Haze and Seek from its tracked `35×15×33` structure. The room resolves one Emerald spawn marker, nine Gold objective candidates, and one Diamond exit marker. A frozen room-entry party size selects 3/4/5/6 distinct Iron pressure plates for 1/2/3/4 players. Only participating players satisfy the shared objectives. Each newly activated plate disappears without a drop, plays one targeted party-wide Wither-spawn cue, and sends the exact remaining-count message in bold italic `#1B4F2C`; the final message directs players to the exit. Active play runs one synchronized five-second Blindness II / ten-second clear cycle with ownership-safe cleanup and one targeted Beacon activate/deactivate cue at each real phase transition. The exact temporary loadout is Diamond Boots with Gears I, Iron Leggings with Nutrition III, and 32 Pumpkin Pies. The final plate creates one room-owned 2×1 gateway; the room completes only when a surviving participant subsequently enters it.

Implemented production Demonic room Warzone Giants as tracked West/East `26×17×36` pieces composed along X. Its six-color wool floor is indexed once per attempt. A frozen room-entry party size creates 3/4/5/6 distinct Zombie-based Giants selected without replacement from Leather, Gold, Chainmail, Iron, Diamond, and Netherite tiers. Giants are six-times scale, have 100 HP, hidden-particle Strength II for 30 minutes, and no innate armor/toughness/attack bonus; they use full tier armor with independent Protection I–IV and carry tier axes with the canonical real Cosmic enchantments. Initial placement refreshes and validates the actual scale-six collision geometry against present floor, solid geometry, participants, and other Giants. A narrow in-wall damage hook rejects suffocation and uses the same validated recovery path, supplemented by a ten-tick active-Giant solid-collision check. Only the weakest surviving spawned tier accepts damage; locked hits are canceled before committed-damage/proc dispatch. The exact player build uses the real Yeti set, Lover/Santa/Purge Multi-Mask, White Scroll/Transmog metadata, canonical armor enchantments, and a real Maui's Hook Iron Sword. Maui's Hook was already implemented and required no dependency work.

Warzone's bounded floor cycle is ten seconds normal, five seconds warning naming two distinct safe colors, then three seconds with the four unsafe colors absent before exact restoration. The pre-indexed per-color maps are mutated without rescanning the structure. Floor removal sends three targeted Warped Forest mood cues at 0/5/10 ticks. Falling participants are terminally removed at six blocks below indexed floor Y. Giants falling three blocks below floor Y or intersecting solid arena geometry move to a collision-safe currently present safe-color coordinate with health, tier, equipment, and vulnerability state preserved. Cleanup restores every indexed floor state, removes remaining Giants, cancels pending sound state, and clears room state/loadouts.

Deadeye now treats its four target blocks as permanently visible trigger coordinates that cannot enter any hidden reveal group. The Resin target restores only its authored Resin path and West-side Purpur transition; the East Purpur parkour remains hidden until the separately visible Purpur target is struck, at which point the Purpur/Crimson continuation is restored. Diamond, Gold, final lever, and West/East composition remain unchanged.

All nine Trial Trinket item names retain their canonical Skip/Time/Insurance family colors and are now bold; mechanics and applied-portal modifier presentation are unchanged.

Trials 1.1 balance now uses exactly six of Raiding Rainbow's eight authored color platforms per attempt, preserving the same selected colors/platforms and hidden order across wrong resets. Hidden Graveyard snapshots entry party size and independently rolls each grave for a second Corpse at `20% × (partySize - 1)` while retaining the three-kill key threshold and completing only after every actually spawned Corpse dies. Zero-G is now Apprentice and its helmet additionally carries Implants III plus the real attached Lover Mask. The one-time Hardcore and Demonic phase-entry bonuses are each 2,400 ticks/two minutes; completion and Time Trinket awards are unchanged. Current pools are Apprentice four rooms, Hardcore three rooms, and Demonic ten rooms.

## 3. Current Real Enchantments

These are registered through Minecraft's enchantment infrastructure, use actual enchantment data on the item, and are not present in normal enchanting-table, librarian, random-loot, random-equipment, or mob-equipment acquisition pools.

### IMPLEMENTED

| ID | Max | Valid equipment | Rarity | Current canonical behavior |
|---|---:|---|---|---|
| `cosmicpve:execute` | V | Sword | Elite | Adds 2% ordinary outgoing damage per level when the target is strictly below 50% maximum health. Uses the shared additive outgoing bucket. |
| `cosmicpve:angelic` | V | Any armor | Ultimate | On committed damage taken, has 1% per total equipped Angelic level to heal 1 HP. Levels across armor pieces aggregate into one roll and at most one heal per damage event. |
| `cosmicpve:lightning` | IV | Bow/crossbow | Simple | On a committed projectile hit, has 5% per level to create a visual-only lightning bolt and deliver exactly 2 HP standard Cosmic true damage with `NO_PROCS`. |
| `cosmicpve:ender_shift` | III | Helmet | Unique | After committed damage leaves the wearer alive and strictly below 25% health, grants Speed I and Regeneration I for 3 seconds per level. Base cooldown is 600 ticks/30 seconds. |
| `cosmicpve:doublestrike` | III | Sword | Legendary | On a committed hit, has 1% per level to deliver a linked child strike for 75% of the parent's finalized ordinary pre-vanilla-mitigation damage. It cannot reroll itself; eligible peer offensive procs may reroll. |
| `cosmicpve:bleed` | VI | Axe | Ultimate | On committed melee damage, has 1.5% per level to add one independently timed Bleed stack. See the canonical stack rules below. |
| `cosmicpve:luck` | X | Boots/leggings | Ultimate | Each total equipped level multiplies eligible proc chances by 1.01 relative to base. Levels on boots and leggings add. |
| `cosmicpve:poison` | III | Sword | Unique | On committed melee damage, has 7% per level to apply Poison I for 60 ticks/3 seconds. |
| `cosmicpve:pummel` | III | Axe | Elite | On committed melee damage, has 3% per level to apply Slowness II for 60 ticks/3 seconds. |
| `cosmicpve:greatsword` | IV | Sword | Elite | At an inclusive attacker-to-target entity distance of 2.5 blocks or farther, adds 5% ordinary outgoing damage per level in the shared additive bucket. |
| `cosmicpve:insanity` | VIII | Axe | Legendary | Adds 1% ordinary outgoing damage per missing heart, including fractional hearts, capped at 2% per level in the shared additive bucket. |
| `cosmicpve:venom` | III | Bow/crossbow | Elite | On a committed positive red-health projectile hit, has 15% per level to apply Poison I for 60 ticks/3 seconds through the shared proc system. |
| `cosmicpve:aegis` | VI | Chestplate | Legendary | Caps the outgoing-finalized ordinary attack component at `14 - level` HP in the pre-defense bounds stage. Aegis VI caps at 8 HP; later incoming/vanilla mitigation still applies, while true damage and execution bypass it. |
| `cosmicpve:eagle_eye` | VI | Bow/crossbow | Ultimate | At an inclusive attacker-to-target entity distance of 18 blocks or farther, adds 3% ordinary outgoing damage per level in the shared additive bucket. |
| `cosmicpve:rage` | VI | Sword/axe | Legendary | Adds `(5 + level)%` ordinary outgoing damage when this exact target damaged the attacker at least three committed positive-red-health times within the rolling `(4 + level)`-second window. History is not consumed. |
| `cosmicpve:molten` | IV | Any armor | Unique | On committed positive-red-health damage taken, has 3% per highest equipped Molten level to ignite the responsible living attacker for 3 seconds. Multiple pieces produce one candidate using the highest level, not summed levels. |
| `cosmicpve:nutrition` | III | Leggings | Unique | Once a food item is fully consumed, adds +1 hunger and +0.25 saturation per level through vanilla bounded food state. It is deterministic and does not use Luck or RNG. |
| `cosmicpve:glowing` | I | Helmet | Simple | Maintains subtle Night Vision while actively equipped without deleting externally supplied Night Vision. |
| `cosmicpve:obsidianshield` | II | Leggings | Ultimate | Reduces ordinary fire-category damage by 25% per level without granting the vanilla Fire Resistance effect. |
| `cosmicpve:oxygenate` | II | Pickaxe | Simple | After a completed underwater block break with the enchanted pickaxe, restores one displayed air bubble (30 internal air units) per level, clamped to normal maximum air. Deterministic; does not use Luck or RNG. |
| `cosmicpve:armored` | IV | Any armor | Legendary | Adds one-half vanilla Protection-equivalent point per level through Minecraft's native protection effect, aggregating across equipped pieces and sharing vanilla Protection's normal cap. |
| `cosmicpve:death_pact` | V | Chestplate | Mastery | Reduces outgoing ordinary damage by `(7.5 - level)%` and incoming ordinary damage by `(1 + level)%`. True damage and execution bypass the ordinary pipeline. |
| `cosmicpve:auto_smelt` | I | Pickaxe | Ultimate | Converts each finalized block-drop stack through one ordinary smelting recipe, preserving the vanilla-generated quantity and awarding no furnace XP. |
| `cosmicpve:experience` | III | Pickaxe | Unique | Multiplies finalized player block-break XP by `1 + 0.5 × level` and floors the integral result. Other XP sources are unaffected. |
| `cosmicpve:blessed` | IV | Axe | Ultimate | On a committed melee hit, has 2% per level to uniformly remove either one eligible negative Cosmic stack instance or one entire harmful vanilla effect from the attacker. Luck modifies the chance relatively. |
| `cosmicpve:implants` | III | Helmet | Ultimate | While continuously equipped, heals exactly 1 HP every 85/70/55 ticks at levels I/II/III, without overhealing or catch-up bursts. |
| `cosmicpve:trap` | III | Sword | Elite | Each committed positive-health-loss melee hit has a flat 4% Luck-modified chance to apply Slowness V for 25/30/35 ticks. Eligible Doublestrike child hits may reroll it. |
| `cosmicpve:cactus` | II | Leggings | Elite | A committed damaging hit has 3% per level, modified relatively by Luck, to retaliate against the attributed living attacker for exactly 1.5 HP standard Cosmic true damage through a non-proccing child. |
| `cosmicpve:gears` | III | Boots | Legendary | While equipped, adds 5% movement speed per level through one stable transient modifier. |
| `cosmicpve:permafrost` | VI | Chestplate | Mastery | On committed ordinary damage taken, has 2.5% per level to add one 60-second independent Permafrost stack to the attacker. Any stack applies one binary +2% incoming/-2% outgoing penalty; reaching `10 - level` clears all stacks and deals 6 HP standard Cosmic true damage. Yeti is immune. |
| `cosmicpve:mortal_coil` | II | Helmet | Mastery | On committed ordinary damage taken, has 3% per level to grant 4 absorption HP for 100 ticks/5 seconds. |
| `cosmicpve:self_destruct` | III | Leggings | Unique | A committed ordinary hit leaving the wearer below 15% health summons four owner-safe, terrain-safe lit TNT at the cardinal adjacent blocks. Cooldown is 1,200 ticks. |
| `cosmicpve:phoenix` | III | Boots | Mastery | Prevents an otherwise ordinary lethal combat death, restores exactly 40% maximum health, and starts a 1,800-tick cooldown. Execution and unattributed environmental deaths bypass it. |
| `cosmicpve:divine_immolation` | IV | Sword | Mastery | On committed melee damage, has 3% per level to ignite and deal 2 HP standard true self-damage, then grant one +10% ordinary outgoing bonus for 100 ticks. Cooldown is 600 ticks. |
| `cosmicpve:virus` | III | Bow/crossbow | Unique | A committed projectile hit against an already poisoned living target deterministically deals 0.4 HP standard Cosmic true damage per level and heals the shooter for exactly 1 HP after accepted delivery. |
| `cosmicpve:devour` | IV | Axe | Legendary | A 5% Luck-eligible parent-hit proc consumes exactly one hunger after committed red-health damage, adds 5% ordinary parent damage per level, heals exactly 1 HP, and plays the normal eating sound. Zero hunger filters before RNG. |
| `cosmicpve:undead_ruse` | X | Any armor | Elite | On committed ordinary damage taken, has 0.5% per highest equipped level to summon an owned allied Undead Corpse for 900 ticks, capped at 1/2/3 active allies by level band. |
| `cosmicpve:obliterate` | III | All weapons | Simple | While strictly below 20% health, committed hits have a fixed 10% Luck-modified chance to apply real level-scaled knockback without bonus damage. |
| `cosmicpve:soul_tether` | III | Axe | Mastery | Committed melee hits have a 10% Luck-modified chance to apply an owner-specific 6/7/8-second tether with a 600-tick cooldown. The target is slowed 20% and takes +5% ordinary damage per current distance block from that tether owner only. |
| `cosmicpve:dodge` | V | Boots | Ultimate | An otherwise valid ordinary incoming hit has 0.5% per level to be rejected before health loss and committed proc dispatch. Turkey adds +2 flat percentage points to the same Luck-modified roll. |
| `cosmicpve:leadership` | X | Chestplate/leggings | Legendary | Current equipped levels sum to at most 20; reliably owned allies gain that percentage in their ordinary outgoing bucket at attack time. |
| `cosmicpve:hero_killer` | III | Axe | Mastery | Deals 3% more ordinary damage per level against targets whose armor-set bonus is currently active. |
| `cosmicpve:soul_siphon` | IV | Any weapon | Mastery | A committed hit that leaves its target below half health heals exactly 1 HP, with a `(7 - level)`-second cooldown. |
| `cosmicpve:blackout` | IV | Sword | Mastery | Has 2% chance per level to suppress the target's active armor-set bonus for one second per level. |
| `cosmicpve:ender_walker` | V | Boots | Ultimate | Reduces Poison- and Wither-effect damage by 10% per level without removing either status effect. |
| `cosmicpve:voodoo` | VI | Helmet | Elite | Hits have 1% chance per level to add an independently expiring ten-second stack; each stack reduces ordinary outgoing damage by 3%, capped at five stacks. |

### DESIGNED BUT NOT IMPLEMENTED

The ordinary Cosmic enchantment pool is content-complete for the current Enchantments 1.0 cycle at exactly 46 implemented real enchantments. Death Pact, Permafrost, Mortal Coil, Phoenix, Divine Immolation, Soul Tether, Hero Killer, Soul Siphon, and Blackout are the implemented Mastery enchantments. Heroic enchantments and the final Enchantments 1.0 balance pass remain separate future work; do not add behavior assumptions beyond `Cosmic_Design.md`.

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

Doublestrike takes 75% of the parent's `finalOrdinaryDamage`: the ordinary value after Cosmic outgoing/incoming calculation but before vanilla target mitigation. It does not automatically copy separately delivered Lightning, Bleed, or other true-damage packets. It creates a unique child sequence linked to the parent with `LIMITED_OFFENSIVE_REROLL`, explicitly excludes Doublestrike, and uses a narrowly validated internal damage type that bypasses only the hurt cooldown created by its parent. Other attacks do not gain this bypass. Eligible peer offensive effects such as Bleed, Poison, and Pummel may reroll on the child.

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

- Divine Immolation, Hero Killer, Phoenix, Self Destruct, Virus, and other enchantments not present in the current implemented table; current Mastery implementations are Death Pact, Permafrost, and Mortal Coil
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
- `/cosmic trial debug progress set <0..8>`
- `/cosmic trial debug force-room <raiding_rainbow|circuit_circus|cold_snap|zero_g|fire_colony|bomb_squad|haze_seek|hidden_graveyard|deadeye|warzone_giants>`
- `/cosmic trial debug restore <player>`
- `/feed`, `/heal`, and `/restore` are separate permission-gated development convenience commands.

Commands call gameplay services or create the same typed components used by gameplay; production loot acquisition remains deferred.

## 15. Unresolved / Do Not Assume

- **Trial scaling after departures:** architecture freezes initial party size so boss maximum health does not shrink, but detailed remaining objective/mob scaling and late-disconnect behavior still need acceptance criteria.
- **Scheduled instance mutation hooks:** direct block use/ignition, fluid block formation, explosions, pistons, entity grief, placement, and breaking are scoped and denied. Fire Colony adds a bounded cached baseline guard for its intentional fire/lava hazard neighborhood. NeoForge still does not expose a complete cancellable hook for every scheduled vanilla mutation, so other future hazard rooms need an equally scoped policy rather than global gamerule changes.
- **Loot tables:** several current design tables are incomplete or malformed and contain ambiguous duplicates. Validation must distinguish intentional duplicate weights from mistakes.
- **Attribute units/caps:** future health, movement, incoming damage, cooldown, and stacked modifier caps/floors need normalization before large content expansion.
- **Pheonix/Phoenix identity and death order:** settle the stable spelling/ID and verify pinned NeoForge kill/death-prevention event ordering before implementing it.
- **Activity key/portal terminology and acquisition:** Dungeons are intended as costly/keyed activities, but current design text primarily describes portals; reconcile this before item implementation.
- **Seasonal crate finalization:** substantial draft tables and mechanics exist, but their exact balance, rewards, acquisition rates, prerequisites, and implementation details remain subject to a bounded reconciliation milestone.
- **Armorer trade policy:** Step 6I disables Diamond/Netherite armor recipes and replaces generated Diamond armor in the six audited chest tables. Vanilla Armorer villagers remain a separate Diamond-armor acquisition path because the current bounded rule did not specify trade replacement; settle whether those offers should be removed or replaced before declaring Iron the ceiling for every normal acquisition route.
- **Remaining Trial reward dependencies:** Ranger Armor is not implemented, and Random Boss Spawn Egg, Dungeon Portal, and broader Demonic reward dependencies still require their own bounded systems. Step 6T deliberately does not fake those rows; the supported development table now includes Mask, Splicer, and 35% Yeti Crystal prerequisites only.

The final post-Orb capacity is no longer unresolved for current target classes: armor is 8 and swords/axes/bows/crossbows are 10. Do not reopen those limits incidentally during unrelated work.

## 16. Current Next Milestone

**Immediate next action: Step 8A — Armor Sets 1.0 + Mystery Spawners.**

Current intended sequence:

Step 8A is the authorized next bounded milestone: complete all eight canonical normal armor sets and their shared movement/category/cooldown/Omni foundations, then add the three Mystery Spawner reward items. Space Chest rebalance, Gear Tinkering, Personal Vaults, Heroic Enchantments, and Dungeon #1 remain deferred.

The ordinary-enchantment expansion remains split across bounded patches. Step 7C is the explicit instruction that made the current canonical Design Doc enchantment-table values implementation-authoritative; later design edits still require their own explicit implementation milestone.

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

For the accepted Step 7G/7G.1 closure:

- `gradlew.bat cleanTest test build` passes all 432 JUnit tests across 116 suites with 0 failures, errors, or skips. Focused coverage pins both imported structures and composition, all four Deadeye targets outside hidden data, Resin/Purpur reveal separation, Haze party scaling/timings/exact formatted feedback and sound identities, Warzone party scaling/tier uniqueness/weakest-tier gate, two-safe/four-removed hazard semantics, three-pulse schedule, Strength II configuration, six-color Rainbow behavior, Hidden Graveyard party formula, room-pool distribution, Zero-G helmet additions, bold item/Trial Trinket presentation, and the two-minute phase-entry bonuses.
- NeoForge `runGameTestServer` completes all three required GameTests successfully. Dedicated-server startup loads 1,462 recipes, atomically publishes 12 Trial rooms, and reaches `Done` without relevant codec, structure, datapack, entity, registry, or common-side classloading errors. Fresh-client startup completes resource reload, OpenAL initialization, and texture-atlas construction without relevant resource or sided-loading failures; the development machine's existing OpenGL debug-context `id=1167` messages remain unrelated to CosmicPVE resources.
- All 201 tracked main-resource JSON files parse successfully. The tracked runtime resources contain Haze and Seek plus both Warzone Giants structure halves; the original root `trial rooms/` files remain untouched and untracked. `git diff --check` reports no whitespace errors beyond normal Windows line-ending notices. Pinned versions, identifiers, and the 46-enchantment registry remain unchanged.
- Step 7G plus the Step 7G.1 correction is manually verified/accepted and ready for its closure commit. The user explicitly excluded approximate Leather/Iron/Netherite runtime damage measurement from this pass; Strength II is the only Giant offensive change.

For the Step 7F candidate:

- `gradlew.bat cleanTest test build` passes all 423 JUnit tests across 114 suites with 0 failures, errors, or skips. New and updated coverage pins the five specifications and 46-enchantment registry; Hero Killer's 3/6/9% ordinary contribution and Mastery axe metadata; Soul Siphon's literal below-half rule and 6/5/4/3-second cooldown; Blackout's 2/4/6/8% chance and 1/2/3/4-second suppression; Ender Walker's 10–50% multiplier and Ultimate boots metadata; Voodoo's 1–6% chance, five-stack cap, independent 200-tick definition, and -3% per-stack ordinary outgoing penalty; rarity pools; and Black Scroll eligibility/exclusion.
- NeoForge `runGameTestServer` completes all three required GameTests successfully.
- All 199 main-resource JSON files parse successfully. Dedicated-server startup atomically publishes the content snapshot including nine stack definitions and reaches `Done`; fresh-client startup completes resource reload, OpenAL/SoundEngine initialization, and every texture atlas without relevant registry, datapack, stack-definition, localization, resource, or common/client classloading errors.
- Runtime loading decodes all five new enchantment definitions and `cosmicpve:voodoo`. `git diff --check` reports no whitespace errors beyond normal Windows line-ending notices.
- The user manually verified and accepted Hero Killer, Soul Siphon, Blackout, Ender Walker, Voodoo, and their cross-system behavior. Step 7F is closed before Trials 1.1 begins.

For the Step 7E candidate:

- `gradlew.bat cleanTest test build` passes all 416 JUnit tests across 113 suites with 0 failures or errors. Focused and updated coverage pins the five specifications and 41-enchantment registry; Undead Ruse chance/caps/lifetime and canonical Corpse data; Obliterate's strict health threshold, fixed chance, and level-scaled knockback identity; Soul Tether's cooldown/durations/stable slow/continuous owner-specific distance scaling; Dodge's single Turkey-composed base chance and relative Luck calculation; Leadership aggregation/cap; rarity pools; and Mastery extraction restrictions.
- Dedicated-server startup publishes the complete CosmicPVE content snapshot and reaches `Done`; fresh-client startup completes resource reload, OpenAL/SoundEngine initialization, and every texture atlas without relevant registry, datapack, localization, resource, or common/client classloading errors.
- All new enchantment definitions and tags decode during runtime loading. `git diff --check` reports no whitespace errors beyond normal Windows line-ending notices. The user manually verified and accepted Undead Ruse, Obliterate, Soul Tether, Dodge, Leadership, their shared ownership behavior, and Turkey's single-roll Dodge integration.

For the Step 7D candidate:

- `gradlew.bat cleanTest test build` passes all 410 JUnit tests across 112 suites with 0 failures or errors. Focused coverage pins Self Destruct's strict threshold/cardinal TNT/cooldown, Phoenix's exact 40% restoration and cooldown, Divine Immolation's chances/cooldown/fire/true self-damage/buff, Virus's per-level damage and exact 1-HP heal, Devour's fixed chance/hunger/heal/shared outgoing contribution, deterministic no-RNG candidates, tier integration, Black Scroll rules, and the 36-spec registry.
- All 186 main JSON resources parse successfully. Dedicated-server startup publishes the complete CosmicPVE content snapshot and reaches `Done`; fresh-client startup completes resource reload, OpenAL/SoundEngine initialization, and all texture atlases without relevant registry, datapack, localization, resource, or common/client classloading errors. The server smoke caught and corrected Phoenix's initial invalid custom boots tag by using Minecraft's established `#minecraft:enchantable/foot_armor` tag.
- `git diff --check` reports no Step 7D whitespace errors when the separately maintained canonical Design Doc is excluded; that Design Doc retains its pre-existing Markdown trailing spaces. The user manually verified and accepted all five Step 7D enchantments and their interactions.

For the Step 7C candidate:

- `gradlew.bat cleanTest test build` passes all 403 JUnit tests across 111 suites with 0 failures, errors, or skips. New and updated coverage pins Gears' 5/10/15% stable modifier, Permafrost chance/threshold/binary penalties/independent 1,200-tick definition/6-HP standard true packet/Yeti immunity metadata, Mortal Coil's two-level 3% scaling and five-second two-heart absorption, every authorized balance formula, changed rarity pools, max levels, localization, and 31-spec registration.
- Dedicated-server and fresh-client smoke verification complete without relevant enchantment-registry, datapack, stack-definition, component, resource, localization, or common/client-side classloading errors. `git diff --check` reports no Step 7C whitespace errors; the separately user-updated canonical Design Doc retains its pre-existing Markdown trailing spaces.
- The user manually verified and accepted Step 7C. Its source, resources, tests, canonical Design Doc synchronization, and handoff closure are committed before Step 7D begins.

For the Step 7B candidate:

- `gradlew.bat cleanTest test build` passes all 396 JUnit tests across 110 suites with 0 failures, errors, or skips. New coverage pins salvage formula boundaries/capping, all-six-rarity examined-book eligibility, rarity aggregation, exactly-once confirmation/replay behavior, flat and bulk same-rarity application, ordinary/Mastery caps, stale/wrong-rarity rejection, book-data preservation, typed stackable Dust, and Sugar/tint model resources.
- Dedicated-server and fresh-client smoke verification complete without relevant registry, menu, command, component-codec, model, tint-source, resource, or common-side classloading errors. Main resources decode/load successfully and `git diff --check` reports no whitespace errors beyond normal Windows line-ending notices.
- The user manually verified and accepted the complete Step 7B Tinkerer and Cosmic Dust loop, including examined-book eligibility, salvage output, all rarity-specific Dust types, bulk same-rarity Success improvement, ordinary/Mastery caps, menu return behavior, and overflow safety.

For the Step 7A candidate:

- `gradlew.bat cleanTest test build` passes all 387 JUnit tests across 109 suites with 0 failures, errors, or skips. `runGameTestServer` passes all three required NeoForge GameTests. The Conquest tests exercise a real `ServerLevel`: physical block/custom-block-entity creation, persisted event identity, ordinary/replaceable surface acceptance, buried/lava/occupied rejection with no ghost event, 3–5 physical Pirates on first interaction, generated completion rewards including the Banknote, actual Flare success/failure consumption, deterministic/NATURAL/FLARE admin command routes with `list`/`inspect` consistency, and real Survival mining progress. The mining test confirms the pickaxe tag, correct-tool path, Iron speed 6, Efficiency V's +26 attribute, and records Iron at 401 ticks / 20.05 seconds and Netherite Efficiency V at 201 ticks / 10.05 seconds.
- All 175 main-resource JSON files decode successfully, including the additive Conquest pickaxe tag. Dedicated-server/GameTest startup loads 1,462 recipes, atomically publishes eight reward tables including `cosmicpve:conquest`, preserves progression controls, and reaches the running test server without relevant registry, block-entity, reward, command, SavedData, tag, or common-side classloading errors.
- The final controlled real-level run recorded `/cosmic conquest spawn-here` at `12394093 -40 2456504`, NATURAL admin creation at `12394176 -60 2456529`, and FLARE admin creation at `12394167 -60 2456404`. Each matching `list`/`inspect` call reported a loaded chunk, `cosmicpve:conquest_chest`, `expectedBlock=true`, and `ConquestChestBlockEntity`; the same run separately exercised the actual Flare item and the interaction/Pirate/reward/cleanup lifecycle.
- Fresh client startup completes resource reload, OpenAL initialization, and block/item/chest atlas construction with the Conquest Chest renderer and Flare model present and without relevant missing-model, missing-texture, malformed-resource, or sided-loading errors. `git diff --check` reports no whitespace errors, only normal Windows line-ending notices. Pinned versions, mod ID, and package remain unchanged.
- The user manually verified and accepted the complete Step 7A candidate, including the corrected physical/custom block-entity placement, bounded surface handling, Flare flow, Pirate ambush, rewards, persistence/recovery, protection, and final real-tool mining behavior. The accepted Step 7A source, resources, tests, and handoff state are closed together before Step 7B begins.

For the manually accepted Step 6V implementation:

- `gradlew.bat cleanTest test build` passes all 373 automated tests across 107 suites with 0 failures, errors, or skips. New coverage pins the two authored dimensions and +41 X composition, West spawn and East final lever, four exact targets, nonempty/nonduplicated authored reveal groups, ordered/no-skip progression predicates, inclusive fall boundary, canonical loadout quantities/levels, exact eight-room mixed Demonic pool, and Deadeye definition/category/resource decoding. Existing Trial suites continue to cover party removal, pot ordering/distinct duplicates, Decision pagination, phase timing, room weighting, persistence codecs, and snapshot tombstones.
- All 170 main-resource JSON files decode. Dedicated-server startup loads 1,462 recipes, atomically publishes seven reward tables, ten Trial rooms including `cosmicpve:trial/deadeye`, and nine mask definitions, verifies progression controls, and reaches `Done` without relevant codec, structure, room-definition, registry, command, datapack, or common-side loading errors.
- Fresh client startup loads both entrypoints, completes resource reload, initializes OpenAL, and builds the block/item/GUI/chest atlases without relevant model, texture, resource, or sided-loading errors. `git diff --check` reports no whitespace errors, only normal Windows line-ending notices. Pinned versions, mod ID, and package remain unchanged.
- The user manually verified Deadeye's authored two-piece flow, ordered reveals, execution/survivor behavior, final-lever completion, Demonic reward/timing behavior, and Trial 1.0 stabilization. The original root `assets/` and `trial rooms/` development directories remain untouched and untracked.

For the manually accepted Step 6U implementation:

- `gradlew.bat cleanTest test build` passes all 366 automated tests across 105 suites with 0 failures, errors, or skips. New coverage pins canonical Corpse attributes/scale/equipment and independent rolls; the exact imported Hidden Graveyard structure dimensions, spawn/chest/well/grave coordinates; typed session/attempt/sequence key identity; wave thresholds, overlap, completion, loadouts, and armor ranges; Demonic eligibility/mixed-pool weighting and phase timing; and the partial supported Demonic reward table.
- All 169 main-resource JSON files decode. The dedicated server loads 1,462 recipes, atomically publishes seven reward tables, nine Trial rooms, and nine mask definitions, verifies progression controls, and reaches `Done` without relevant entity, renderer, structure, biome, room-definition, reward, component, command, datapack, or common-side classloading errors.
- Fresh client startup loads the common/client entry points and Undead Corpse renderer, reloads all resources, initializes OpenAL, and builds the block/item/GUI/chest atlases without relevant missing-model, missing-texture, entity-renderer, localization, or sided-loading errors. The client smoke was deliberately terminated after the initialization boundary.
- Resource validation and Step 6U-owned paths pass `git diff --check`; the existing Design Doc Markdown line-break whitespace remains untouched. Minecraft 1.21.11, NeoForge 21.11.45, Java 21, Gradle 9.2.1, ModDevGradle 2.0.144, mod ID `cosmicpve`, and package `com.cosmicpve` remain unchanged. The user manually verified the reusable Corpse behavior, Pale Garden room presentation, typed key/chest flow, all three overlapping waves and equipment profiles, immediate final-Corpse completion, Demonic continuation, cleanup, and representative Trial regressions, then accepted the milestone.

For the manually accepted Step 6T implementation:

- `gradlew.bat cleanTest test build` passes all 356 automated tests across 102 suites with 0 failures, errors, or skips. Coverage pins definition decoding/validation, ordered 1–5-mask component and synchronized presentation round trips, all supported anvil input shapes and limit rejection, persistent setting codec round trips, 2/3/5-power Splicer disassembly and stale rejection, application/removal preservation, compact one-row identity-only equipment lore and name colors, Reindeer's exact +5% value, single/Multi helmet suppression, cumulative multi-notch scrolling through the real rendered bottom bound, tooltip item/screen reset behavior, canonical item models, and the expanded Hardcore development reward rows.
- All 167 main-resource JSON files decode, including the canonical empty blockstate variant key. The dedicated server loads 1,462 recipes, atomically publishes six reward tables, eight Trial rooms, and all nine mask definitions, verifies progression controls, and reaches `Done` without relevant codec, datapack, registry, component, command, or common-side classloading errors.
- Fresh client startup passes mask renderer and client-only tooltip-scroll registration, reloads resources, initializes OpenAL, and builds block/item/GUI/chest atlases without relevant model, texture, tooltip, renderer, or sided-loading errors. The client smoke was deliberately terminated after the initialization boundary.
- Step 6T-owned paths pass `git diff --check` with only normal Windows line-ending notices. The separate pre-existing user edit to `docs/Cosmic_Design.md` retains intentional Markdown trailing-space line breaks and is not rewritten by this milestone. Pinned versions, mod ID, and package remain unchanged.

For the manually accepted Step 6S.2 checkpoint:

- `gradlew.bat cleanTest test build` passes all 331 automated tests across 95 suites with 0 failures, errors, or skips. Focused coverage pins the empty and fully modified portal tooltip, all nine canonical modifier values, stable category order/colors and data purity, fixed flush-right/vertically-centered 120-pixel HUD layout, phase tier headings/colors, separate room ordinal/name semantics, `Xm YYs` formatting, unchanged update suppression, and Netherite-equip pane-selection feedback.
- All 156 main-resource JSON files decode. Dedicated-server startup publishes six reward tables and eight Trial rooms, loads 1,462 recipes, verifies progression controls, and reaches `Done` without relevant common-side, payload, codec, tooltip, or resource errors. Fresh client startup loads both entrypoints, reloads resources, initializes OpenAL, and builds block/item/GUI/chest atlases without relevant localization, component, HUD registration, model, texture, audio, or sided-loading errors.
- `git diff --check` passes with only normal Windows line-ending notices. Pinned Minecraft 1.21.11, NeoForge 21.11.45, Java 21, Gradle 9.2.1, ModDevGradle 2.0.144, mod ID `cosmicpve`, and package `com.cosmicpve` remain unchanged.
- The user manually verified the revised Trial Portal tooltip, narrow right-edge Trial HUD and tier/room/time hierarchy, Netherite Space Chest pane-selection feedback, all previously accepted 6S/6S.1 behavior, and placed Cosmic spawners under their applicable vanilla environmental requirements.

For the manually accepted Step 6S.1 corrective checkpoint:

- `gradlew.bat cleanTest test build` passes all 328 automated tests across 95 suites with 0 failures, errors, or skips. Added coverage pins stacked-portal rejection without mutation/consumption, single-stack modified portal components and command invariant, exact Trinket/portal-line colors, room-intro anchor correction, bounded performance sampling, expanded HUD layout constants, Circuit target sound identity, and identical representative vanilla Spawner configuration for Blaze, Iron Golem, Creeper, Zombie, Pig, and Sheep.
- All 156 main-resource JSON files decode. Dedicated-server startup publishes six reward tables and eight Trial rooms, loads 1,462 recipes, verifies the progression invariant, and reaches `Done`. Fresh client startup loads both entrypoints, reloads resources, initializes OpenAL, and builds block/item/GUI/chest atlases without relevant model, texture, component, command, payload, codec, datapack, or sided-loading errors.
- `git diff --check` passes with only normal Windows line-ending notices. Pinned Minecraft 1.21.11, NeoForge 21.11.45, Java 21, Gradle 9.2.1, ModDevGradle 2.0.144, mod ID `cosmicpve`, and package `com.cosmicpve` remain unchanged.
- The user manually verified stacked-portal rejection and non-merging, Circuit Circus target feedback, Trinket colors, room-intro movement locking, acceptable Trial performance, and representative placed Cosmic spawners under their vanilla spawning conditions. The former generic/Creeper spawning concern is resolved; the permissioned inspect and debug-delay tools remain available.

For the manually accepted Step 6S checkpoint:

- `gradlew.bat cleanTest test build` passes all 320 automated tests across 94 suites with 0 failures, errors, or skips. New coverage pins all nine typed Trinket identities and exact values, independent upgrade/rejection rules, persistent portal modifiers, the four initial timer totals, Skip pot/progression semantics, deterministic Insurance selection without replacement, complete-bundle salvage, Decision pagination/wrapping, complete-pot payout independence from page, lifecycle-cached room-line presentation, capacity wording/suffix derivation, and green Orb names.
- All 156 main-resource JSON files decode; the one blockstate with the canonical empty variant key is validated with an object parser that preserves empty property names. The dedicated server publishes six reward tables and eight Trial rooms, verifies the progression invariant, and reaches `Done` without relevant component, codec, datapack, payload, or common-side loading errors. Fresh client startup loads both entry points, completes resource reload, initializes OpenAL, and builds the block/item/GUI/chest atlases without relevant missing-model, missing-texture, payload, or sided-loading errors.
- `git diff --check` passes with only normal Windows line-ending notices. Pinned Minecraft 1.21.11, NeoForge 21.11.45, Java 21, Gradle 9.2.1, ModDevGradle 2.0.144, mod ID `cosmicpve`, and package `com.cosmicpve` remain unchanged.

For the manually accepted Step 6R checkpoint:

- `gradlew.bat cleanTest test build` passes all 308 automated tests across 91 suites with 0 failures, errors, or skips. New coverage pins the Bomb Squad NBT dimensions/palette/markers, all four start-cell exit eligibility, seeded two-exit selection, canonical loadout, the material-scoped terrain exception, typed encounter egg identity, the exact three-room Hardcore pool, possessive owner formatting, phase colors, and phase update suppression.
- All 152 main-resource JSON files decode. The dedicated server publishes six reward tables and eight Trial rooms including Bomb Squad and reaches `Done`; fresh client startup completes resource/audio/atlas initialization with the new participant-only phase payload and no relevant model, resource, payload, or sided-loading error.
- `git diff --check` passes with only normal Windows line-ending notices. Pinned versions, mod ID, and root package remain unchanged.

For the manually accepted Step 6Q checkpoint:

- `gradlew.bat cleanTest test build` passes all 300 automated tests across 89 suites with 0 failures, 0 errors, and 0 skipped. New coverage pins the Cold Snap NBT dimensions and exact marker/mechanic positions, the 26-of-2,476 bounded Ice subset, preserved Powder Snow, Packed-Ice spawn-floor inference, nonlethal highest intended fall calculation with Feather Falling IV, ordered/idempotent shortcut flags, participant/active-room activation boundary, the exact three-room Apprentice pool, persistent immutable portal ownership, owner-packet update suppression, 28 real enchantments, Cactus 3%/6% chance, relative chance multiplication, 1.5-HP standard true packet, absorption semantics, and `NO_PROCS` recursion policy.
- All 151 main-resource JSON files parse successfully. The dedicated server loaded 1,462 recipes, atomically published six reward tables and seven Trial rooms including `trial/cold_snap`, and reached `Done` without relevant codec, datapack, structure, dimension, registry, event-handler, payload, or common-side classloading errors.
- Fresh client startup loaded both common and client entry points, registered the owner/timer payload and participant HUD layer, completed resource reload, initialized OpenAL, and created block/item/GUI/chest atlases without relevant missing-model, missing-texture, malformed-resource, payload, or client/server loading errors. The client was terminated at the smoke-test boundary.
- `git diff --check` passes with only normal Windows line-ending notices. Pinned Minecraft 1.21.11, NeoForge 21.11.45, Java 21, Gradle 9.2.1, ModDevGradle 2.0.144, mod ID `cosmicpve`, and package `com.cosmicpve` remain unchanged.

For the accepted Step 6O/6P implementation:

- `gradlew.bat cleanTest test build` passes all 289 automated tests across 87 suites with 0 failures, 0 errors, and 0 skipped. Coverage pins the Apprentice/Hardcore/Demonic transition bonuses and one-time flags, +600/+300 phase-specific room awards, the exact two-room Hardcore pool and shared weighting rules, Fire Colony and Zero-G production structures/objectives/fixtures, authoritative ten-objective Zero-G progression without stale timer publication, the supported development-partial Hardcore rows, random unspecified-Orb rates, the revised Raiding Rainbow loadout, Decision-entry restore ordering, participant timer formatting/update suppression, title cadence, Decision-menu framing, solo NO DEAL transition, DEAL celebration cadence, and existing Nutrition behavior.
- The dedicated server loaded 1,462 recipes, atomically published six reward tables and six Trial rooms including `trial/hardcore_development`, Fire Colony, and Zero-G, and reached `Done` without relevant codec, datapack, structure, entity, dimension, registry, or common-side classloading errors.
- Fresh client startup loaded CosmicPVE and NeoForge at the pinned versions, registered the client-only Trial payload handlers and HUD layer, completed resource reload, initialized OpenAL, and created the block, item, GUI, chest, and other texture atlases without relevant missing-model, missing-texture, malformed-resource, menu, or sided-loading errors. It was deliberately terminated at the smoke-test boundary.
- All 149 main-resource JSON files parse successfully. `git diff --check` passes with only the repository's normal Windows line-ending notices. Pinned Minecraft 1.21.11, NeoForge 21.11.45, Java 21, Gradle 9.2.1, ModDevGradle 2.0.144, mod ID `cosmicpve`, and package `com.cosmicpve` remain unchanged. The user manually verified Circuit Circus, Raiding Rainbow, Fire Colony, corrected Zero-G progression/completion, DEAL/NO DEAL, the Trial timer HUD, Decision presentation/cadence, automatic restore, DEAL fireworks, and the targeted performance improvements, then accepted the combined milestone.

For the accepted Step 6N implementation:

- `gradlew.bat cleanTest test build` passes all 271 automated tests across 82 suites with 0 failures and 0 errors. New coverage pins the 27-enchantment registry, Trap chance/duration/child-proc metadata, persistent pot and pending-payout codec round trips, pot ordering and distinct duplicate acquisitions, the fourth-room Hardcore boundary and one-time time bonus, individual decisions, declining/modifiable room weights and no consecutive duplicate, immutable Decision-menu layout, seeded eight-color Rainbow sequencing and repeated-reset active-Zombie invariants, bounded orthogonal Circuit connectivity, the intended-placement protection exception and its validation boundaries, imported production structure dimensions/marker semantics, matching-floor inference, canonical Apprentice rows/weights/deferred Unexamined Books, and deduplicated basedrum/Dragon presentation tokens.
- All 144 JSON resources parse successfully. The dedicated server loaded 1,462 recipes, atomically published five reward tables and four Trial rooms including the Apprentice table and both production rooms, retained the progression recipe invariant, and reached `Done` without relevant datapack, codec, dimension, structure, menu, registry, attachment, or common-side classloading errors.
- Fresh client startup loaded both common and client entry points, completed resource reload, initialized OpenAL, and created the block, item, and GUI atlases with the Decision screen and new item definitions present. No relevant missing-model, missing-texture, malformed-resource, menu, or sided-loading errors were observed.
- `git diff --check` passes. Pinned Minecraft, NeoForge, Java, Gradle, ModDevGradle, mod ID, and package remain unchanged. The user manually verified the corrected Circuit Circus placement path, repeatable Raiding Rainbow resets, Trial presentation sounds, DEAL/NO DEAL, and spawn-marker replacement.

For the accepted Step 6M implementation:

- `gradlew.bat test` passes all 247 automated tests across 74 suites with 0 failures and 0 errors. New coverage includes the four-player boundary, participant removal, paused/active timer semantics, duplicate-completion predicate, persistent session codecs, exact component-bearing inventory snapshot codecs and restored tombstones, one- and two-piece room definitions, imported NBT dimensions/marker counts, title/countdown copy and colors, default/creative/explicit-allow protection policy, Implants intervals/scheduling, and the 26-enchantment invariant.
- The dedicated server decoded and atomically published both Trial room definitions, loaded the dedicated void dimension and imported structures, retained 1,462 recipes and progression invariants, and reached `Done` without relevant datapack, attachment, command, dimension, structure, common-side classloading, or persistence errors.
- Fresh client startup completed mod/resource reload, OpenAL, and all texture atlases with the Trial Portal item definition, gateway block model, localization, and Implants data present and no relevant missing-model, missing-texture, malformed-resource, or client/server loading errors.
- The user manually verified the complete portal-to-Decision-to-development-room-to-Decision-to-outside loop, multiplayer lifecycle behavior, restart recovery, terrain protection, titles/timer, and Implants timing and accepted Step 6M.

For the accepted Step 6L implementation:

- The automated suite currently passes all 223 tests across 67 suites with 0 failures and 0 errors; focused coverage includes tier/table identity, exact production rows/weights and Unexamined Book semantics, persistence codecs and transaction invariants, the 80-tick reveal plus 30-tick pause, canonical presentation/sound policy, non-stackability, vanilla Creeper `SpawnData` and timing defaults, reward-bundle compaction, Blessed chances, and the 25-enchantment registration invariant.
- The dedicated server atomically published all three production Space Chest tables plus the existing development table, loaded 1,462 recipes, retained the progression recipe invariant, and reached `Done` without relevant registry, datapack, command, menu, attachment, or sided-classloading errors.
- Client startup completed resource reload, sound initialization, and texture-atlas creation with the Space Chest menu screen/item model registered and no relevant missing-model, malformed-resource, or localization errors. The client was deliberately terminated at the smoke-test boundary.
- Pinned environment values remain unchanged. The user manually verified and accepted the corrected Space Chest presentation, timing, sounds, Unexamined rewards, transactional recovery, and Blessed behavior. Later controlled testing confirmed the generic placed Cosmic spawners work under the applicable vanilla spawning conditions.
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
