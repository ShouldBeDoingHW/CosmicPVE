# Armor Sets

| Name | Set Bonus | Primary Procurement Method |
| :---- | :---- | :---- |
| Phantom | \+25% out, \+10 inc. Mastery enchants proc 25% more often. | Dungeons |
| Yjiki | \-15% inc, \+5% out. Doubled in dungeons. | Dungeons |
| Dimensional Traveler | \+7.5% out, \+10% movement speed. \-20% proc cooldown (Phoenix, Ender Shift, etc.) | Invasions |
| Engineer | \-20% inc, \+15% movement speed | Invasions |
| Yeti | \+10% out, \-10% inc, immune to freeze, frozen, permafrost, ice aspect. | Trials |
| Ancient | \+7.5% out, \-7.5% inc, doubled when under 50% health. | Ancient City/Crafting |
| Ranger | \+20% bow/crossbow damage, \+20% movement speed. | Trials |
| Dragonslayer | \+15% Outgoing, immunity to poison and fire/lava damage.  | Primal Dragon Boss |

Specific Armorset procurement information:

Ancient — 40% crystals can be crafted with nether star \+ 4 echo shards. 80% crystals can be crafted with nether star \+ 8 echo shards.

Dragonslayer — Guaranteed drop for the player with the highest damage against the Primal Dragon Boss in the form of a 75% Crystal.  

# Changelog

* Renamed Latest Changes to Changelog  
* Added Trial Room Entry Title Display rules  
* Changed “Candycane Blade” Sword Skin to “Ornamental Carnage” and added details  
* Created a list of proposed balance changes  
* Added Technical details for masks and guidance on implementation

# Comments

Mastery \- \#AA0000  
Legendary \- \#FFAA00  
Ultimate \- \#FFFF55  
Elite \- \#A3FFF5  
Unique \- \#55FF55  
Simple \- \#FFFFFF

Each ordinary enchantment book has an independently rolled 1-100% Success Rate and 1-100% Destroy Rate, both displayed on the book. Success is resolved first; the destroy roll occurs only if the enchantment application fails. A higher-level book upgrades directly to its level. An equal-level book increases the enchantment by exactly 1 level if that would not exceed the enchantment's maximum; lower-level books are rejected. Mastery enchantments use a 1-49% Success Rate and a 51-100% Destroy Rate.

Black scrolls work on Simple-Legendary enchantments, randomly pulling one out, turning it into a book with a set success chance (like 40%, 75%, 100%, ect) and a random destroy rate 1-100%. 

White Scrolls can be applied to equipment to provide one-time protection against a destructive failed item application. This currently includes Cosmic Enchantment Books, Armor Set Crystals, Armor Enchantment Orbs, and Weapon Enchantment Orbs. Protection is consumed only when it actually prevents the item from being destroyed.

Each eligible item begins with capacity for 5 actual Cosmic enchantments; vanilla and virtual enchantments do not count toward this limit. Armor Enchantment Orbs increase an individual armor piece's capacity by \+1 per successful application, up to 8\. Weapon Enchantment Orbs increase swords, axes, bows, and crossbows by \+1 per successful application, up to 10\. Orbs are non-stackable, use an Eye of Ender presentation without enchanted glint, and each carries an independent 1-100% Success Rate and 1-100% Destroy Rate.

Armor Set Crystals are non-stackable Nether Star-based items with a permanent enchanted shimmer. They carry a 1-100% Success Rate and currently have a guaranteed destructive failure outcome if the success roll fails; White Scroll protection may prevent that destruction. Applying a crystal adds set identity to the existing armor item rather than replacing the item.

Transmog Scrolls use a Paper presentation and affect tooltip presentation only. When applied to equipment, they sort enchantments from top to bottom as: vanilla enchantments first, then Mastery, Legendary, Ultimate, Elite, Unique, and Simple. Within each Cosmic rarity, higher enchantment levels appear first; ties use a deterministic order. Future enchantments added to a Transmogged item remain automatically sorted.

For destructive item-application systems, the standard feedback language is: successful application plays the vanilla level-up sound; a failed application plays the passive lava sound; and the anvil-breaking sound is added only when the target item is actually destroyed.

Unexamined Enchantment Books  
An Unexamined Enchantment Book is associated with one Cosmic enchantment rarity. Right-clicking it opens the book: a firework is launched into the air and the Unexamined Book is converted into one random actual Cosmic Enchantment Book from that same rarity at a random valid level for the selected enchantment.

For example, an Unexamined Mastery Enchantment Book may reveal Phoenix at any valid Phoenix level, Permafrost at any valid Permafrost level, or any other eligible Mastery enchantment and valid level.

The revealed book receives independently generated Success Rate and Destroy Rate values. Its base rolls follow the normal rules for its rarity, including the special Mastery ranges. The generation of these values must be data-driven and expose a reusable modifier path so future custom blocks, upgrades, temporary buffs, debuffs, or other systems may favorably or negatively influence the rates of books opened while the modifier is active. For example, a future custom block could grant \+3 percentage points to the Success Rate of Unexamined Books opened during a five-minute effect window. Exact modifier stacking and clamping are defined by the effect providing the modifier rather than hard-coded into the Unexamined Book item.

# Conquest Chests

Conquest Chests are periodic Overworld events that allow players to gain loot. One Conquest Chest spawns every 7th Minecraft day. Its X and Z coordinates are selected independently within the square from \-5,000 to \+5,000 on each axis. Each Conquest Chest must be mined to obtain the loot held inside and takes 1.5× as long to mine as obsidian. Upon the first player interaction with the chest, 3-5 Space Pirates spawn in a 5x5 area around it. Players cannot break or place blocks aside from the Conquest Chest itself within the existing 20x20 protected area around the chest. If no player has interacted with the chest within 30 minutes of its spawn, it despawns. Once successfully broken, the Conquest Chest rolls 3 items from its loot table.

Spawning rules: After X/Z selection, the chest must be placed at a valid surface position rather than underground or embedded inside terrain. A valid placement must have at least 2 of the chest block's 6 faces adjacent to air. If a selected position does not satisfy these rules, choose another valid candidate rather than forcing the placement. After spawning, and subsequently every 5 minutes while the chest is still active, a global chat message is sent that says “Look alive cosmonaut\! A Conquest Chest has spawned at (coordinates) and will disappear in (minutes remaining on chest) minutes\!”

Conquest Chests can also be summoned with a player right clicking a “Conquest Chest Flare” which spawns a conquest chest within \+/- 100 blocks in either direction from their location in a valid spawn.

Loot when broken: 

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| Simple Enchantment Book | 15 | 2 |
| Unique Enchantment Book | 15 | 2 |
| Elite Enchantment Book | 12 | 2 |
| Ultimate Enchantment Book | 12 | 1 |
| Legendary Enchantment Book | 8 | 1 |
| Random Boss Spawn Egg | 5 | 1 |
| Trial Portal | 10 | 1 |
| Trial Portal | 5 | 2 |
| Transmog Scroll | 10 | 1 |
| White Scroll | 9 | 1 |
| 50% Black Scroll | 10 | 1 |
| 65% Black Scroll | 6 | 1 |
| Repair Scroll | 10 | 1 |
| Gkit Refresher | 4 | 1 |
| Legendary Space Chest | 8 | 1 |
| Ultimate Space Chest | 10 | 1 |

In addition to the random loot, each conquest chest gives a guaranteed banknote that ranges between 100k and 1 million, always a multiple of 10,000. Examples: 330k, 970k, 560k. 

# Core Rules

# Core Rules

# Project Scope

This mod is primarily designed for singleplayer or small cooperative worlds of 2-4 players. Balance is primarily PVE-focused. Players explore a custom survival world and obtain Cosmic-style gear through normal survival, custom structures, bosses, Invasions, Dungeons, and Trials.

The three major repeatable activities should have distinct identities rather than forming one linear difficulty ladder. Trials are the most accessible and have a very high mastery ceiling, but are punishing while players learn them. Dungeons require costly keys, have a high mastery ceiling, and should provide very strong average loot when successfully completed. Invasions are medium-access scheduled events with a lower mastery ceiling but strong baseline preparation requirements and extraordinary reward potential.

# Legacy Combat Rules

Combat should emulate old pre-1.9 / 1.8-style melee as closely as practical while remaining on modern Minecraft. Vanilla swords and axes should be modified rather than replaced with duplicate weapon items.

\- Melee weapons should have effectively no meaningful attack cooldown. Target implementation is approximately 20 total attack speed with minimum attack charge set to 0, subject to playtesting.  
\- Vanilla sweep attacks are disabled.  
\- Normal critical hits remain enabled.  
\- Sprint-hit knockback remains enabled.  
\- Shields remain enabled initially and can be revisited through playtesting.  
\- Custom enchantment procs roll only on successful damaging hits, not on raw mouse clicks or attacks rejected by normal damage immunity.  
\- Combat enchantments and armor-set effects should operate on LivingEntity wherever logically possible so players, normal mobs, elites, and bosses can use the same combat systems. Player-only checks should be reserved for inherently player-specific mechanics such as mining, eating, inventory interaction, or UI.

# Damage and Modifier Rules

To avoid runaway multiplicative scaling, ordinary outgoing custom-damage bonuses are combined into a shared additive custom outgoing-damage bucket unless an effect explicitly states otherwise. Defensive percentage modifiers from separate systems are multiplicative with one another rather than simply added together. For example, two separate 25% incoming-damage reductions produce 0.75 x 0.75 \= 56.25% damage received before later portions of the damage pipeline.

True damage is handled separately from ordinary damage and is not unintentionally modified by effects intended only for base/ordinary attack damage. Standard Cosmic true damage bypasses armor/Protection-style mitigation, shields, normal target hurt immunity, Aegis, and ordinary Cosmic incoming-damage reductions. It does not bypass absorption by default; absorption is consumed before red health. Aegis caps the base/ordinary attack component and should not cap separate true-damage effects.

Environmental or encounter execution is a terminal operation rather than an oversized damage packet. Execution bypasses armor, absorption, ordinary Cosmic reductions, Aegis, Phoenix, and other death-prevention effects. Instant-failure mechanics such as designated dungeon pits or encounter kill zones should use execution semantics when they are intended to be unavoidable by normal defensive gear.

All custom cooldown-based enchantments should use a shared cooldown system. A 20% cooldown reduction means multiplying the base cooldown by 0.8 (for example, 90 seconds becomes 72 seconds).

# Positive and Negative Stacks

The combat system needs a generalized stack framework. Every stack type is marked POSITIVE or NEGATIVE and may optionally have a source entity, duration, maximum stack count, and per-stack effect. This framework is used by effects such as Bleed, Hysteria, and future buffs/debuffs.

Examples:  
\- Bleed: NEGATIVE  
\- Hysteria: NEGATIVE  
\- Feeding Frenzy: POSITIVE

Effects that steal, copy, cleanse, or otherwise interact with stacks must query this classification rather than hard-coding individual stack names.

# Armor Set Rules

An entity can have only one active armor-set bonus at a time. A player or mob receives a set bonus only when helmet, chestplate, leggings, and boots all have the same armor-set identity. Mixed sets do not provide partial or combination bonuses. KADP, DADY, and other combo-set crystal systems are intentionally omitted.

# Instance Rules

Invasions, Dungeons, and Trials use a dedicated instance dimension rather than hiding arenas at extreme Overworld coordinates. Before entering an instance, each player's original dimension, position, yaw, and pitch are saved so the player can be returned to the correct location afterward.

Instance terrain is protected by default:  
\- Players cannot break or place blocks unless a room explicitly allows it.  
\- Player explosions, TNT, mob explosions, pistons, fire spread, fluids, and similar mechanics cannot permanently damage or bypass protected instance terrain unless the encounter explicitly permits that interaction.  
\- Specific rooms may designate selected blocks or interactions as destructible/usable. Bomb Squad is an example where designated walls can be destroyed by the intended Creeper mechanic.  
\- Creative/debug mode may bypass instance protection for building and testing.

Instance and player state must persist safely enough that closing the world or disconnecting does not permanently lose the player's pre-instance inventory or return location.

# Small-Coop Scaling

The intended supported party size is 1-4 players. Encounter data should support party-size scaling, especially boss health. Exact scaling values remain a playtesting decision; the architecture should not assume that four players use the same boss health as one player.

# Debugging

Development builds should include debug commands/tools for giving specific enchant books, applying enchantments and armor sets, inspecting custom item data, modifying cooldowns, entering test instances, and resetting instance state so normal progression never needs to be replayed during testing.

Armor Progression  
Iron armor is the highest normal player armor tier in CosmicPVE. Diamond Armor and Netherite Armor cannot be crafted. Diamond and Netherite weapons and tools are not restricted by this rule.

Diamond Armor results in generated loot tables, including End City loot and other applicable vanilla loot sources, are removed. Whenever one of these removed Diamond Armor loot results would otherwise be generated, it is replaced by one Unexamined Enchantment Book: 65% chance of  Unexamined Simple and 35% for an Unexamined Unique.

This progression rule does not prevent encounter mobs from visually or mechanically using stronger armor when an encounter explicitly defines that equipment; it governs normal player armor progression and acquisition.

# Cosmic Crates

Cosmic Crates are apex reward crates with four variants: Spring, Summer, Fall, and Winter. A crate becomes openable only by combining a matching Left Half and Right Half of the same season. 

Each seasonal crate is intended to contain some of the strongest loot in the game, including two season-exclusive utility custom blocks and two season-exclusive weapon skins, alongside powerful general rewards; seasonal Mastery enchantments may also be tied to individual crates. Custom blocks should provide utility rather than being purely decorative. The detailed seasonal tables below are active design material, but they are not yet implementation-ready balance and are expected to change as prerequisite systems are built and balancing continues.

A Secret Weapon Cache awards one random signature weapon associated with an armor set. Signature set weapons deal a flat \+5% damage while the wielder has the matching full armor-set bonus active (for example, Phantom Scythe, Yeti Maul, Ranger Bow).

**Winter Cosmic Crate Loot:**

Guaranteed loot:

90% OR 100% blackscroll x 2  
Random 50% Armor Set Crystal  
1-3 Iron Golem OR Mooshroom Spawners  
2-5 Trial Portal  
Random Tier 3 Trial Trinket  
Snowglobe OR Milk and Cookies custom block  
3-4x Legendary Enchantment Book  
35% Permafrost Mastery Enchantment Book  
Ornamental Carnage Sword Skin OR Icicle Hatchet Axe Skin

1 Extraordinary item:

Dungeon Key Ring  
Memory Chest  
Secret Weapon Cache  
Maxed out Trial Portal

Snowglobe: Right click on the block to open a simple interface that shows all trial rooms that you have successfully completed (each represented with a blank map that has been renamed to the name of that room). Each Snow Globe you have allows you to select one Trial room, giving that room \+1 weight in Trials you participate in. Snow Globe weight is added on top of the room's normal appearance-based weight reduction and can keep a favored room eligible beyond its natural third appearance. Multiple Snow Globes may be assigned to the same room; with enough added weight, that room can appear more than three times in one Trial.

Milk and Cookies: Right click to eat milk and cookies, giving you \+5 random invasion modifier stacks at the start of the next invasion you join\! Cooldown: 12 Minecraft Days.

**Summer Cosmic Crate Loot:**

Guaranteed loot:

1-2 100% Blackscroll  
Random 50% Armor Set Crystal  
1-3 Cow OR Guardian Spawners  
3-6 Trial Portals  
Random Tier 3 Trial Trinket OR 2x Random Tier 2 Trial Trinket  
Sand Castle OR Potted Cactus Custom Block  
1x Abandoned Spaceship Dungeon Key OR 1x Destroyed Outpost Dungeon Key  
35% Blackout Mastery Enchantment Book  
Jumbo Popsicle Sword Skin OR Spiked Baseball Bat Axe Skin

1 Extraordinary item:

Super Kit Refresher  
Memory Chest  
Secret Weapon Cache  
Maxed out Trial Portal

Potted Cactus: Generates 1 cactus every 90s, stores up to 9 stacks at a time. Hoppers/chests do not work on it.

Sand Castle: Each Sand Castle that you’ve placed gives you a \+1% chance to gain a bonus item from the Advanced Invasion loot table. Up to a maximum of \+10%. Cannot gain more than 1 bonus loot item, and it only rolls if the invasion is completely won, including the final boss.

# Current Issues

Creeper Spawner spawning issue — UNRESOLVED:

Cosmic Creeper Spawner items preserve minecraft:creeper in vanilla SpawnData and placement diagnostics show the expected vanilla spawner configuration, but manual testing has still not produced Creepers from placed Creeper Spawners under apparently valid conditions. Other tested spawner types are not currently known to share the issue. Treat Creeper Spawner runtime spawning as unresolved and circle back later; do not block current Space Chest acceptance or Trial implementation on it. 

# Dungeons

Dungeons are an instance where teams of players can go and fight a predetermined series of challenges in a set order. All surviving players who beat the final boss without dying or running out of time earn a dungeon lootbag with valuable loot.

Basic rules:

No respawn. No breaking blocks unless necessary for puzzles. Keep inventory enabled.

Each Dungeon has a set time limit. Failure to complete the dungeon within the time limit results in automatic expulsion from the dungeon without a lootbag.

Each Dungeon requires its corresponding Dungeon Key to open. A Dungeon portal can be placed anywhere outside the instance dimension, and up to 4 players can enter. After 30 seconds, the portal closes and the timer begins. Players who die in the dungeon are immediately sent back to where they entered the portal.

A heroic crystal can be applied to a dungeon portal to create the heroic version of the dungeon. In this version, the time limit is decreased by 50%, the enemies deal 25% more damage, and golden apples and ender pearls are disabled. 

Each dungeon lootbag contains 3 items, with one of them guaranteed to be a custom block. Heroic dungeon lootbags contain 4 items, and their pools are expanded to add additional valuable loot.

Dungeon \#1–The Abandoned Spaceship order of challenges:

Boss fight vs Yjiki. The boss alternates between taking axe and sword damage while shooting persistent holes into the arena floor for the duration of the run. Falling through a hole is an environmental execution death and bypasses Phoenix/death-prevention effects.

Parkour through Air Lock.

Maze with white/yellow/orange/red wool floor that weakens a stage every time you step on it.

Mother of Yjiki fight. More HP and more damage. Charges every 20-30 seconds and executes players who are not behind the divider walls; this instant-failure mechanic bypasses Phoenix/death-prevention effects.

Cargo Bay Parkour

Teleportation Room Puzzle (input 8 colors in order while fighting off Space Pirates)

Abandoned Spaceship Lootbag:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| Legendary Enchantment Book | 6 | 2 |
| Gkit Unlock Gem | 8 | 1 |
| 40% Phantom Armor Crystal | 15 | 1 |
| 40% Yjiki Armor Crystal | 15 | 1 |
| Heroic Crystal | 8 | 1 |
| White Scroll | 12 | 2 |
| Trial Portal | 5 | 1 |
| Random Boss Spawn Egg | 15 | 1 |
| Random Mask | 10 | 1 |
| Random Tier 1 Trial Trinket | 12 | 1 |
| Gkit Refresher | 6 | 1 |
| Doomsday Machete Skin | 5 | 1 |
| Ultimate Enchantment Book | 12 | 2 |

Heroic Abandoned Spaceship Lootbag:

| 60% Yjiki Armor Crystal | 10 | 1 |
| :---- | :---- | :---- |
| 60% Phantom Armor Crystal | 10 | 1 |
| Trial Portal | 10 | 2 |
| Gkit Unlock Gem | 12 | 1 |
| Mkit Unlock | 6 | 1 |
| Memory Chest | 4 | 1 |
| Mastery Enchantment Book | 8 | 1 |
| Tier 2 Trial Trinket | 10 | 1 |
| Random Double Mask | 6 | 1 |

# Enchantments

| Name | Max Level | Goes On: | Rarity | Effect |
| :---- | :---- | :---- | :---- | :---- |

| Aegis | 6 | Chestplate | Legendary | Limits max hearts lost from base attack to 7 \- (0.5 x level) |
| :---- | :---- | :---- | :---- | :---- |
| Angelic | 5 | Any Armor | Ultimate | Gives a 1% × level chance to heal 0.5 heart (1 HP) after a committed damaging event. Angelic levels across equipped armor pieces are summed into one proc chance, and the heal can occur at most once per damage event. |
| Armored | 4 | Any Armor | Legendary | Each level is equal to half a level of protection. |
| Auto Smelt | 1 | Pickaxe | Simple | Automatically smelts drops. |
| Bleed | 6 | Axe | Ultimate   | 1% chance per level on a valid damaging hit to apply one Bleed stack for 5 seconds. Each entity can have up to 10 Bleed stacks. Every stack has its own independent 5-second lifetime; applying a new stack does not refresh older stacks. Each active stack reduces movement speed by 1% while it remains active and deals 1 true damage every 1.5 seconds. |
| Death Pact | 5 | Chestplate | Mastery | Deal 3% less damage but take (2% x level) less damage. |
| Divine Immolation | 4 | Sword | Mastery | 3% chance per level to proc on hit. On proc, set yourself on fire for 5 seconds and deal \+10% damage. 30 second cooldown. |
| Eagle Eye | 6 | Bow/Crossbow | Ultimate | Deal (3% x level) more damage to targets that are 18 blocks away or further. |
| Ender Shift | 3 | Helmet | Unique | After a committed hit leaves you below 25% of max health, gain Speed I and Regeneration I for (3 × level) seconds. 30-second cooldown. Being below 25% after the hit is sufficient; the hit does not need to cross the threshold from above. |
| Execute | 5 | Sword | Elite | Deal (2% x level) more damage against targets that are below 50% HP. |
| Experience | 3 | Pickaxe | Simple | Each level adds \+50% XP from breaking blocks. At level III, block XP is 250% of the vanilla amount (2.5× total). |
| Gears | 3 | Boots | Legendary | Gives \+10% movement speed per level. |
| Glowing | 1 | Helmet | Simple | Gives perm night vision |
| Greatsword | 4 | Sword | Elite | Deal (5% x level) more damage to enemies that are 2.5 blocks away or further. |
| Hero Killer | 3 | Axe | Mastery | Deal 3% per level more damage to enemies that have an active armor set bonus. |
| Insanity | 8 | Axe | Legendary | Deal 1% more damage per missing heart, up to a max of (2% x level) |
| Lightning | 4 | Bow/Crossbow | Simple | 5% chance per level on a committed projectile hit to strike visual lightning on the target and deal exactly 2 HP of standard Cosmic true damage. The visual lightning itself does not deal additional vanilla lightning damage. |
| Luck | 10 | Boots/Leggings | Ultimate | Increases eligible Cosmic enchantment proc chances by 1% relative per total Luck level across equipped Luck pieces. For example, 20 total Luck levels multiply a 1% proc chance by 1.20, producing a 1.2% final chance; Luck does not add flat percentage points. |
| Molten | 4 | Any Armor | Unique | 2% x level chance of setting your attacker on fire for 3 seconds when hit. |
| Mortal Coil | 3 | Helmet | Mastery | 2% chance to proc on hit. On proc gain 2 full hearts of absorption and your next attack does (5% x level) more damage. |
| Nutrition | 3 | Leggings | Unique | Eating a piece of food restores \+1 hunger and \+0.25 saturation per level. |
| Obsidianshield | 1 | Leggings | Ultimate | Gives perm fire resistance. |
| Oxygenate | 2 | Pickaxe | Simple | After breaking a block while underwater, refills 1 air bubble per level. |
| Phoenix | 4 | Boots | Mastery | When an otherwise ordinary lethal hit would kill you, survive at 40% HP and make your next hit deal (10% × level) more damage. 90-second cooldown. Phoenix does not prevent environmental/encounter execution deaths. |
| Poison | 3 | Sword | Elite | (5% x level) chance to give poison 1 for 3 seconds to target. |
| Pummel | 3 | Axe | Elite | (2% x level) chance to give slowness 3 to your target for 2.5 seconds. |
| Rage | 6 | Sword/Axe | Legendary | Deal 5% more damage to enemies that have damaged you three times or more in the last (4 \+ level) seconds. |
| Self Destruct | 3 | Leggings | Unique | When below 15% HP, summon 4 lit TNT that you are immune to. 60 second cooldown. |
| Venom | 3 | Bow/Crossbow | Elite | (15% x level) chance to give poison 1 for 3 seconds to target. |
| Virus | 3 | Bow/Crossbow | Unique | If target is poisoned, deals 0.25 true damage per level of enchant.  |
| Doublestrike | 3 | Sword | Legendary | (1% × level) chance on a valid hit to make a linked second hit for 50% of the parent's ordinary attack damage. The child hit may reroll other eligible offensive on-hit enchantments, but Doublestrike cannot trigger itself recursively. Separate true-damage packets from the parent are not automatically duplicated. |
| Undead Ruse | 10 | Any Armor (only highest lvl counts, no stacking)  | Elite | (Level/2) percent chance to spawn an ally Undead Corpse when hit. Maximum corpse count is 1 at levels 1-4, 2 at levels 5-9, and 3 at level 10\. Allies despawn after 45 seconds. |
| Cactus | 2 | Leggings | Elite | (3% x level) chance to deal 1.5 true damage to your attacker when hit. |
| Devour | 4 | Axe | Legendary | 5% chance on a valid damaging hit, provided the attacker has at least 1 hunger available, to consume exactly 1 hunger and make that hit deal (level x 5%) more damage while healing the attacker for 2 HP. On proc, play the normal eating sound for approximately 0.5 seconds. |
| Obliterate | 3 | All Weapons | Simple | If your current health is below 20%,  you have a 10% chance to deal (3 x level) blocks of knockback with your hit.  |
| Blessed | 4 | Axe | Ultimate | A (level x 2%) chance to be blessed each time you hit an enemy. When blessed, a random negative effect or stack on you is removed, such as a bleed stack or the weakness potion effect. |
| Trap | 3 | Sword | Elite | A 4% chance every time you hit an enemy to give them slowness 5 for (1 \+ (0.25 x level)) seconds.  |
| Implants | 3 | Helmet | Ultimate | Passively heal 1 HP every (100 \- (level x 15)) ticks.  |
| Soul Tether | 3 | Axe | Mastery | 10% Chance to tether an enemy for (level \+ 5 seconds) upon hitting them. While tethered to you, enemies are slowed by 20% and take 5% more damage from the tether owner for each block in their distance from you. (2.1 blocks away would take 10.5% more damage). 30 second cooldown after each proc.  |
| Dodge | 5 | Boots | Ultimate | When targeted by an otherwise valid incoming hit, you have a (level x 0.5%) chance to Dodge it. A successful Dodge rejects the ordinary hit and the attacker's offensive proc chain, causing zero damage. Defensive effects that require committed damage do not proc from the dodged hit; defensive effects triggered merely by being targeted or hit may still proc if their own semantics allow it. Turkey Mask contributes \+2 flat percentage points to this same Dodge probability. |
| Permafrost | 6 | Chestplate | Mastery | When the wearer is hit, they have a (level x 2.5%) chance to give one Permafrost stack to the attacker. Permafrost is a NEGATIVE stack. Having one or more Permafrost stacks applies one binary penalty of \+2% incoming damage and \-2% outgoing damage; the penalty does not scale per stack. If an entity reaches (10 \- level) Permafrost stacks from this effect, all of its Permafrost stacks are removed and it takes exactly 6 HP of standard Cosmic true damage. Individual stacks expire after 60 seconds. An entity with the active Yeti armor-set bonus is immune to receiving Permafrost stacks. |
| Leadership | 10 | Chestplate/Leggings | Legendary | All of your active owned allies deal level% more damage with attacks. Owned allies include summoned entities such as Undead Ruse Corpses, tamed mobs, and other summoned/owned entities whose owner can be determined; human party members are not owned allies. Having this enchantment on multiple pieces of armor stacks the effect up to \+20%. |

# Balance Candidate

**Poison:**  
Rarity: Elite \-\> Unique  
Proc Chance: 5% \-\> 7% per level

**Pummel:**  
Proc Chance: 2 \-\> 3% per level  
Slowness Duration: 2.5 \-\> 3 seconds  
Slowness Strength: Slowness III \-\> Slowness II

**Rage:**  
Bonus Damage: 5% \-\> 5% \+ level (Rage 3 would give 8% bonus damage)

**Auto Smelt:**  
Rarity: Simple \-\> Ultimate

**Experience:**  
Rarity: Simple \-\> Unique

**Mortal Coil:**  
Max Level: 3 \-\> 2  
Goes On: Sword/Axe \-\> Helmet  
Procs On: Outgoing hit \-\> Incoming Hit  
Proc Chance: 2% \-\> 3% x level  
Absorption Duration: Unspecified \-\> 5 seconds

**Molten:**  
Proc Chance: 2% x level \-\> 3% x level

**Gears:**  
Movement Speed Bonus: 10% x level \-\> 5% x level

**Doublestrike:**  
Child Attack Base Damage: 50% of Parent Attack \-\> 75% of Parent Attack

**Obsidianshield:**  
Max Level: I \-\> II  
Effect: Permanent Fire Resistance \- \> 25/50% reduction to Fire based damage such as Lava, Burning, touching Blazes, future mobs/bosses (this can be made data-driven). 

**Death Pact:**  
Outgoing Damage Reduction: 3% \-\> (7.5 \- level)%  
Incoming Damage Reduction: (level x 2%) \-\> (1 \+ level)%

**Bleed:**  
Proc Chance: 1% per level \-\> 1.5% per level

# Invasions

Invasions are a PVE instance that occurs every 10th minecraft day. Starting 3 days out, the game automatically sends a chat message to the player telling them that the Deep Space Invasion starts in x days\! Once the 10th day hits, each player is given a custom ender pearl (that disappears after 10 minutes) that they can throw to teleport into the instance dimension and join the invasion. Invasions do not use keep inventory.

Invasions have 2 stages and take place in a massive square battlefield for exactly 30 active minutes. The instance may visually progress through a day/night cycle, but the gameplay timer is independent of Minecraft's normal 20-minute day/night cycle.

During the beginning stage of the invasion, 4 quadrant bosses independently roll from a pool of 3 possible bosses, so duplicate boss types are allowed, with each spawning in its own corner of the battlefield. Around the boss are a handful of spawners that spawn hostile mobs. Killing these hostile mobs has a 10% chance to give you a buff for the duration of the invasion. These bonuses are as follows: \+1% outgoing damage (50 cap), \-1% incoming damage (25 cap), \+ 0.5 max HP (4 cap, lower roll weight), and \+1% movement speed (10 cap). More may be added later.

Each quadrant boss is difficult and has 2-3 abilities/attacks/attributes. Once the players have collectively slain all 4 quadrant bosses, the invasion boss spawns in the center of the map. With 2-3x more HP, increased damage, and \~5 abilities/attacks/attributes, the invasion boss is much more difficult than a quadrant boss. There are no spawners for the hostile mobs near the invasion boss, so stacking buffs is recommended WHILE you are engaged in killing the quadrant bosses.

If the invasion boss has not been summoned and defeated by the end of 30 minutes, the invasion is failed.

At the end of the invasion, each player who participated receives an invasion lootbag. Each lootbag contains at a minimum, 1 default item.

Each player’s lootbag contains additional loot as follows:

For each defeated quadrant boss they did 10% or more damage to: 1-2 default items.  
Doing 10% or more damage to the defeated invasion boss: 1-2 advanced items.

This allows for a maximum of 9 default items and 2 advanced items.

Default Loot Table:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| \+2 Insurance Trial Trinket | 9 | 1 |
| \+3 Minute Trial Trinket | 9 | 1 |
| 35% Dimensional Traveler Crystal | 7 | 1 |
| Abandoned Spaceship Dungeon Portal | 5 | 1 |
| 35% Engineer Crystal | 7 | 1 |
| 80% blackscroll | 10 | 1 |
| 90% blackscroll | 15 | 1 |
| 100% blackscroll | 15 | 1 |
| Enderman Spawner | 10 | 1 |
| Gkit Unlock Item | 7 | 1 |
| Heroic Crystal | 10 | 1 |
| 60% Weapon Enchantment Orb | 10 | 1 |
| 60% Armor Enchantment Orb | 10 | 1 |
| Mastery Enchantment Book | 9 | 1 |
| Repair Scroll | 20 | 2 |
| Repair Scroll | 20 | 1 |
| Wither Skeleton Spawner | 5 | 1 |
| Skip 1 Room Trial Trinket | 9 | 1 |
| Trial Portal | 8 | 1 |
| Trial Portal | 5 | 2 |
| Zombie Spawner | 5 | 1 |

Advanced Loot Table:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| \+2 Insurance Trial Trinket | 7 | 1 |
| \+3 Insurance Trial Trinket | 4 | 1 |
| \+5 Minutes Trial Trinket | 4 | 1 |
| 50% Dimensional Traveler Crystal | 5 | 1 |
| 50% Engineer Crystal | 5 | 1 |
| 75% Dimensional Traveler Crystal | 5 | 1 |
| 75% Engineer Crystal | 5 | 1 |
| 80% Armor Enchantment Orb | 8 | 1 |
| Memory Chest | 10 | 1 |
| Mastery Enchantment Book | 16 | 1 |
| Mkit Unlock Item | 6 | 1 |
| Skip 2 Rooms Trial Trinket | 7 | 1 |
| Skip 3 Rooms Trial Trinket | 4 | 1 |
| Super Kit Refresher | 8 | 1 |
| Trial Portal | 10 | 2 |
| Trial Portal | 10 | 3 |
| 80% Weapon Enchantment Orb | 8 | 1 |
| Random Double Mask | 8 | 1 |
| Secret Weapon Cache | 4 | 1 |
| Abandoned Spaceship Portal | 7 | 2 |

Invasion Implementation and Edge Cases  
\- The 30-minute timer advances on active game/server ticks. In singleplayer, pausing the game pauses the Invasion timer.  
\- The hostile-mob generators around quadrant bosses should use controlled encounter-spawner logic rather than relying solely on ordinary vanilla spawner behavior. They may still be represented visually by spawner blocks if desired.  
\- Each boss tracks a per-player damage ledger for reward eligibility. Qualifying contribution includes direct melee damage, projectile damage, custom-enchantment damage, damage-over-time effects attributed to that player, and damage from owned/tamed/summoned entities where ownership can be determined.  
\- Each awarded Default or Advanced item is an independent weighted loot-table roll. Duplicate results are allowed unless a particular future item explicitly states otherwise.  
\- Invasion temporary buffs are personal to the player who earns them and expire when the Invasion ends or that player leaves the instance.  
\- Invasions use the shared global instance protection, return-location, persistence, and 1-4 player scaling rules from the Core Rules tab.  
\- Invasions do not enable keep inventory. This is an activity-specific rule and must never be implemented by changing the global keepInventory gamerule.

# Kits

Kits are not a routine part of this game. Instead, you unlock the kit. However, instead of using a persistent time gated cooldown, kits are refreshed by “Refresh Crystals” which allow you to claim all unlocked kits of the corresponding type once.

G-kit:

Standing for God Kits, these kits contain either a netherite weapon or an iron armor piece (non heroic) that has 3-5 custom enchantments on it.

Kit Variants. Unlock items are all a Diamond.

Gladiator, Viking, Paladin, Butcher, Grandmaster.

M-Kit:

Standing for Mastery Kits. These powerful kits contain 1 piece of already heroic armor but no weapons. Each piece of armor has exactly 3 custom enchantments. On top of this, each time you claim your M-Kit, you receive a mastery enchantment book for free.

Kit variants and unlock item textures:

Death Knight (purple dye), Necromancer (green dye), and Ghost (blue dye). 

# Masks / Skins

# Masks / Skins

# Masks

Masks are an additional customization and buildcraft layer. Most masks are found individually. Inside an anvil, players may combine single masks into double, and a double \+ single mask into a triple. No multi mask may contain more than 3 masks at this time, though this should be a variable that can be changed up or down.

Masks should attach to or be associated with the player's helmet rather than replacing the armor-set helmet slot. A Multi-Mask provides the effects of all masks contained within it. For rendering, Multi-Masks use a black box with a question mark on the front. The contained masks still provide their effects without requiring the visuals to be physically layered on the player's face.

### **Masks — Implementation-facing decisions**

* Masks attach to helmets using the same application/removal interaction as Skins.  
* Underlying helmet item, stats, enchantments, and armor-set identity remain unchanged.  
* A helmet may carry **up to 5 masks technically**.  
* Normal player-facing combination/progression is capped at **3 masks** unless future content explicitly overrides this.  
* A mask with 1 contained mask renders using that mask’s assigned player-head texture.  
* A mask with 2 or more contained masks renders using the shared **Multi-Mask** player-head texture.  
* Tooltips dynamically list every contained mask and its effect.  
* Mask effects derive from the actual contained masks, not from Multi-Mask tier/type.

| Name | Effect | Hex Color | Base 64 Data | Flavor/Information Text |
| :---- | :---- | :---- | :---- | :---- |
| Santa | \+2 maximum HP. | \#C95757 | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTY0YTEwZmJkYjQ5MTNjZjc1ZDU3YjZhZDU1Y2MwZTM0ZGUxYWYxZjMzODgyMTA2ZWFlMGY4ZWMwMGE2ZjY4In19fQ== | Gain \+2 Maximum Health\! |
| Reindeer | \+5% movement speed. | \#634F34 | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmVkZDViMDBhNWZiYzJkYWFiNThmMzNkODZiYmM0ZmY4ZDI1OGM5YmRhNjU3NTVjMDMwNTg5MGJiYTA5NjAxZiJ9fX0= | Gain \+5% Movement Speed\! |
| Purge | \+3% outgoing damage. | \#4D1805 | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTlmOWI3MjlhNTk2MWI3YTJlMmM0NTViOTIzYmM0Njg0MzlkYjVjMzY3MzkxYTAzMWExMTQ0ZGI5Y2Y0ZjQifX19 | Deal \+3% Outgoing Damage\! |
| Party | \+1% outgoing damage, \-1% incoming damage, and \+1% movement speed. | P \#0EEDE9; A \#0EEDA3; R \#0EED46; T \#2FED0E; Y \#B2ED0E | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDhhYTJmOWJmZmQ4ODUzNjQ2YzEzNzVkM2RlNzFiODE4N2JkNDI2MzMyZDlmOWQ3MTIyMTZmN2U5MmE5MzBkIn19fQ== | \+1% Outgoing Damage, \-1% Incoming Damage, and Gain \+1% Movement Speed\! |
| Lover | Passively heals 1 HP every 5 seconds. | \#D67CA6 | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjdkYTk0MDAzODYxOTIxOGM1YWY3MWFhMmQ0NDZmOGE4MDkzZjhhZWYyNmU3MTAyMjRkYjhiOTA2NmYyNmZkIn19fQ== | Passively Restores 1 HP Every 5 Seconds\! |
| Scarecrow | Passively restores 1 hunger and 0.5 saturation every 8 seconds. | \#66550F | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY4NTgxM2FiNDQxZTI4ZWQ3MzA3NjFiY2UxYmI2ZmQ4OTNkZjg1ZDdkNTNmMmNiNjgzZDkwNzE2YWNhOTM2OSJ9fX0= | Passively Restores Hunger and Saturation\! |
| Zeus | Immune to lightning strike effects such as Nature’s Wrath and Lightning. | \#175753 | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmM1ZjgxY2I1NDk3NDRkMGQxZTM1NDAxYTgyMTBjYTczOGJhNDQ1YmNiODU0ODFhNzhmMmQ4ZGRjOTRlZDAwMyJ9fX0= | Gives Full Immunity to Lightning Based Damage\! |
| Turkey | \+2 percentage points to Dodge Chance, using the same Dodge roll whether or not the Dodge enchantment is present. | \#BAAD00 | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjRlM2IyNzZmZWI1NWI2MmU2MzNhZTZlNDE0ZTVlMjk3NTRjOWNkZGQ4YWU5MjVmYzM3ZWUyNTRjODRiNTQxIn19fQ== | Gain \+2% Dodge Chance\! |
| Dragon | \+2% damage. Immune to fire and poison damage. | \#FFF24D | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhhYTNjNTNlNDY3NTIzYzZjM2Q4MzNiYmJlZTM3YTY2ZmMxNGYzMDUzMGYzOWE2YTljMDQ1N2ZmZTgwNWMyNSJ9fX0= | Deal \+2% Outgoing Damage and Become Immune to Fire and Lava Damage\! |
| Multi Mask | N/A | N/A | eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjM4YzUzZTY2ZjI4Y2YyYzdmYjE1MjNjOWU1ZGUxYWUwY2Y0ZDdhMWZhZjU1M2U3NTI0OTRhOGQ2ZDJlMzIifX19 | N/A |

# Weapon Skins

Weapon skins apply a cosmetic model/texture and a unique passive effect to an existing weapon rather than creating a separate parallel weapon item. A weapon can have one active skin at a time. Skins should be implemented as data on the existing item so vanilla/custom enchantments, Heroic status, durability, and other item properties remain intact.

## Axe Skins

Whisk Taker (\#B08E00) — Hitting an enemy under 40% HP grants a Feeding Frenzy stack for 3 seconds. Having 1 or more Feeding Frenzy stacks increases outgoing damage by 5% and increases the wearer's Luck factor by 1% per stack, up to a maximum of 10%. Feeding Frenzy is a POSITIVE stack/status for the generalized stack system.

Boosted Chainsaw (\#CCA00A)— Grants Doublestrike III to the axe. Doublestrike is a sword enchantment planned for addition to the custom enchantment pool; this skin grants the equivalent level-3 effect even though the weapon is an axe.

Stormbringer (\#224B57)— Each successful hit has a 3% chance to strike lightning on the target, applying Slowness II for 1.5 seconds and dealing 2 HP true damage. While Stormbringer is being held, the wielder takes 2% less incoming damage.

## Sword Skins

Spinal Tap (\#00F02C) — Successful hits apply one Hysteria stack to the target for 4 seconds. Hysteria is a NEGATIVE stack. Each active Hysteria stack gives the affected entity a 0.5% chance per stack, whenever it lands an otherwise valid hit, to redirect that hit onto itself. On a Hysteria redirect, the original target takes zero damage and the attacker receives equal ordinary self-damage with no additional proc chain.

Maui's Hook (\#404242) — \+4% outgoing damage. On each successful hit, there is a 10% chance to steal one POSITIVE stack from the target and transfer it to the wielder. NEGATIVE stacks such as Bleed or Hysteria cannot be stolen by this effect.

Party Blade (P, 0EEDE9) (A, 0EEDA3) (R, 0EED46) (T, 2FED0E) (Y, B2ED0E) (B, EDD30E) (L, EDAA0E) (A, ED760E) (D, ED3E0E) (E, ED0E63) — Deal \+1% outgoing damage for each mob within 15 blocks, up to \+15%. Deal an additional \+5% outgoing damage while the wielder has any absorption hearts.

Doomsday Machete (\#162B0D) — Deal \+2% outgoing damage for each Mastery Enchantment on this weapon.

Ornamental Carnage (\#1B943A) — \-5% incoming damage. Deal \+10% damage to enemies that have a current slowness potion effect.

## Skin Persistence

Each skin can be dragged in the inventory and left click applied to the appropriate item. It is then tied to that specific item until removed with a simple right click. 

# Misc Items

Repair Scroll \- can be applied to any item to refill durability up to maximum. Item texture is paper.

Heroic Crystal \- Can be applied one time maximum to dungeon portals, pickaxes and shovels, and armor. When applied to armor, it turns the model into leather and gives \+250 maximum durability. Pickaxes and shovels turn to a gold texture and gain \+250 maximum durability. If an eligible item is already damaged when the crystal is applied, both its current durability and maximum durability increase by 250\. When applied to dungeon portals, it turns them into their Heroic variants. Item texture is an amethyst shard.

Kit Refreshers- Consume to renew all of your available kits, allowing you to claim them.Comes in “Mkit”, “Gkit”, or “Super” variants, which allow you to refresh your GKits, Mkits, or both respectively. Item texture is the item quartz. 

Dungeon Key Ring \- Item that once right clicked drops 1 of each type of dungeon key into the player’s inventory. Cannot be opened if there isn’t inventory space. 

Conquest Chest Flare \- spawns a conquest chest in a valid spawn location within \+/- 100 blocks in either direction from the player’s current location. Uses a redstone torch base model. Description text and name text is \#BF0000. 

Mask Splicer \- Uses \#450000 for it’s name and standard yellow color for item lore text. One time drag n drop item that separates a 2–5-power Multi-Mask into each individual mask and drops it into your inventory or on the ground if there is no space. 

# Mobs

**SPACE PIRATES**

There are two variants of space pirates, and everything will be formatted for Space Pirates in (variant 1/variant 2\) fashion. If an aspect of the mob is laid out and only 1 concrete detail is mentioned, it is shared between the two variants.

Appearance: Zombified piglin/wither skeleton model. Both are 1.35 size.

Armor: No default armor points or toughness. Randomly spawns in with a full set of armor that is randomized between iron and diamond. Each piece has protection randomized between 1 and 4, creating a large variance of armor each mob can have.

Health: 25/35

Base Attack Damage: 0 

Held Item: Diamond Axe/Iron Sword

Held Item Modifiers: Guaranteed to have insanity 8, 50% chance of having pummel 3/Random Poison enchant 1-3, 25% chance to have Execute 1-5.

Custom attacks/abilities: N/A

Speed: 0.35

**Primal Dragon**

The Primal Dragon is a spawnable boss that players can fight for loot and a challenge. Most specifically, to obtain the Dragonslayer set. Each kill of the primal dragon has a 50-50 chance of giving 1 ultimate or legendary space chest to all players that dealt 10% of the bosses health or more. On top of this, the player that dealt the most damage gets a guaranteed 75% dragonslayer crystal.

Appearance: Ender Dragon model at 1.1× visual scale, with appropriately scaled collision/hit detection. 

Armor: Equivalent to full iron armor. Nothing visual. 

Health: 300

Attack Damage: 12 HP for its principal contact attack 

Custom attacks/abilities: The boss retains normal Dragon Fireballs and its normal flight/perching behavior.

Every 22 seconds while not perched, it fires an undeflectable explosive fireball toward a player. On impact, the projectile produces TNT-like explosion radius/knockback, deals 8 HP damage, and does not damage terrain.

Speed: 0.75

Spawning: The Primal Dragon Egg may only be activated at the central End exit-portal area while no other Dragon fight is active. Activation begins a vanilla-like End Crystal restoration sequence and summons the Primal Dragon. Primal Dragon kills do not generate additional Dragon Eggs, duplicate End Gateways, or repeat vanilla first-Dragon progression rewards.

Any player dealing at least 10% of the Primal Dragon's maximum health receives one Space Chest, independently chosen 50/50 between Ultimate and Legendary.

The eligible player who dealt the greatest total attributed damage additionally receives one guaranteed 75% Dragonslayer Armor Set Crystal.

The Primal dragon egg is crafted using a regular dragon egg and 4 eyes of ender \+ 4 end crystals (end crystals are in the corners, dragon egg in center). Regular dragon eggs can now be crafted via egg in center surrounded by 8 obsidian. 

**Undead Corpse**

Undead corpses are standard mobs that can appear in a variety of instances but are primarily tied to the Undead Ruse enchantment. They are naturally hostile to the player, but can be summoned as a friend/ally. 

Appearance: Zombie base model. Scaled to 0.9 size.

Armor: No default armor or toughness. 

Health: 15

Base Attack Damage: 0 

Held Item: Iron Axe

Held Item Modifiers: 30 percent chance of each of the following: sharpness 3, bleed 3, rage 3\. 

Custom attacks/abilities: N/A

Speed: 0.4  
Drops: Nothing by default. 

# Money and Economy

Money is something that all cosmic players have, can be gained by a variety of sources, and is primarily spent on dungeon keys.

Associated commands:

**/bal** — sends a message in chat to the player saying what their current balance is. Alternative form to call the same procedure: /balance

**/withdraw x** — creates a new banknote in the amount of x, removing that amount of money from their account.

**/sell hand** — if current item in hand is sellable, it removes all matching items of that kind in your inventory, multiplies that number by the item’s sell price, and adds the total amount to your bank account. Sends a message to that player that says: “You sold x y\! Z has been added to your account\!” X being the amount of items of that type sold, y being the name of the item that was sold, and z being the total amount added to the player’s account.

**/sell all** — Remove all sellable items from inventory. Multiply each item by their sell price and add that total amount to the player’s bank account. Sends a message to that player that lists the amount of each item sold, as well as the overall amount added to that player’s account. Message uses the same format as the /sell hand message.

**Flash Sales:**

Flash sales are events that happen every 45-75 minutes in the world. During this event, a chat message is sent to all players in the world, describing the item being sold and the price. Players can purchase the flash sale by typing /flashsale buy or simply /buy.

Each item in the flash sale has a low, medium, and high price, which is chosen randomly. 4 minutes after the flash sale went up, another message is sent to all players. After 5 minutes, the flash sale closes, and any /flashsale buy or /buy commands are returned with: “There is no current flash sale genius\!”

All items available in the flash sale are of equal weight, and each player can individually buy the flash sale once. Player 1 buying or not buying the sale does not impact player’s 2 and 3’s ability to buy the sale. 

Example flash sale message: “FLASH SALE\! 50% Engineer Crystal at the low price of $4,000,000\!”

Flash sale availability table:

| Item | Amount | Low Price | Medium Price | High Price |
| :---- | :---- | :---- | :---- | :---- |
| Trial Portal | 1 | 200k | 350k | 500k |
| Trial Portal | 2 | 350k | 550k | 875k |
| Memory Chest | 1 | 1.75m | 2.25m | 2.75m |
| Armor Enchantment Orb | 1 | 200k | 300k | 400k |
| Weapon Enchantment Orb | 1 | 200k | 300k | 400k |
| 50% Engineer Crystal | 1 | 1.4m | 1.6m | 1.8m |
| 50% Phantom Crystal | 1 | 1.4m | 1.6m | 1.8m |
| 50% Ranger Crystal | 1 | 1.4m | 1.6m | 1.8m |
| 50% Dragonslayer Crystal | 1 | 1.4m | 1.6m | 1.8m |
| 75% Yeti Crystal | 1 | 1.75m | 2m | 2.25m |
| 75% Dimensional Traveler Crystal | 1 | 1.75m | 2m | 2.25m |
| 75% Yjiki Crystal | 1 | 1.75m | 2m | 2.25m |
| 75% Ancient Crystal | 1 | 1.75m | 2m | 2.25m |
| Abandoned Spaceship Portal | 1 | 1m | 1.35m | 1.7m |
| Iron Golem Spawner | 1 | 1m | 1.3m | 1.6m |
| Blaze Spawner | 1 | 600k | 800k | 1m |
| Repair Scroll | 1 | 50k | 70k | 90k |
| Repair Scroll | 5 | 200k | 400k | 600k |
| Heroic Crystal | 1 | 450k | 650k | 775k |
| Random Mkit unlock | 1 | 4m | 5m | 6m |
| Super Kit Refresher | 1 | 3.3m | 3.6m | 3.9m |

Dungeon Keys can also be purchased with money, inside the /dungeonmaster menu.

Abandoned Spaceship keys cost 1m base, and increase by 10k for each Abandoned Spaceship key that you have already purchased. Destroyed Outpost Keys cost 2m base \+20k increase, and Planet Null keys are 3m base \+ 30k increase. 

/sell values:

| Item | Amount (in $) |
| :---- | :---- |
| Raw Beef | 1.25 |
| Steak | 2.25 |
| Rotten Flesh | 1 |
| Bone | 1.35 |
| String | 1.35 |
| Potato | 3 |
| Carrot | 3 |
| Iron Ingot | 4.75 |
| Blaze Rod | 6.25 |
| Poppy | 1 |
| Coal | 2.25 |
| Wither Skull | 100 |
| Any Wool | 1.50 |
| Raw Mutton | 1.15 |
| Leather | 2.75 |
| Cooked Mutton | 2.15 |
| Ender Pearl | 2.5 |
| Raw Porkchop | 1.25 |
| Cooked Porkchop | 2.25 |
| Prismarine Shard | 2 |
| Prismarine Crystal | 2.5 |
| Raw Cod | 2 |
| Cooked Cod | 3 |
| Raw Salmon | 2 |
| Cooked Salmon | 3 |
| Pufferfish | 5 |
| Tropical Fish | 10 |
| Gunpowder | 4 |
| Spider Eye | 2.5 |

# Space Chests

Space Chests are medium/minor loot bags that contain useful though tame loot to help the player advance through the world. Space Chests are mainly dropped via bosses, though there are alternative methods of obtaining. There are also Memory Chests, a variant of Space Chests that contain a single drop of potentially immense value. 

Space Chests come in 3 main rarities: Ultimate, Legendary, and Mastery. Each variant contains 5 items of loot from their respective loot table. Non memory chests have their name formatted in the color that corresponds to that rarity. Memory chests use a rainbow gradient as follows:

| Letter | Hex Code |
| :---- | :---- |
| M \#1 | BF0F0F |
| E | BF0F76 |
| M \#2 | 870FBF |
| O | 0F26BF |
| R | 0FB0BF |
| Y | 0FBF3E |

Space Chests use a default chest texture. Memory chests use an ender chest texture. Right clicking the Space Chest will open a menu to select your 5 loot items. Memory chests have no menu and simply pop in your hand into whatever you roll.

Ultimate Space Chest Loot:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| Ultimate Enchantment Book | 10 | 1 |
| Legendary Enchantment Book | 5 | 1 |
| Elite Enchantment Book | 10 | 2 |
| 50% Blackscroll | 10 | 1 |
| Pig Spawner | 4 | 1 |
| Sheep Spawner | 4 | 1 |
| Golden Apple | 10 | 16 |
| Repair Scroll | 12 | 1 |
| Iron Armor Piece with 1-2 random level Ultimate or lower valid enchantment | 5 | 1 |
| Transmog Scroll | 9 | 1 |
| 50k Banknote | 10 | 1 |
| 75k Banknote | 5 | 1 |

Legendary space chest loot: 

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| Ultimate Enchantment Book | 10 | 2 |
| Legendary Enchantment Book | 10 | 1 |
| White Scroll | 10 | 1 |
| 50% Blackscroll | 10 | 1 |
| 75% Blackscroll | 8 | 1 |
| Creeper Spawner | 4 | 1 |
| Spider Spawner | 9 | 1 |
| Zombie Spawner | 5 | 1 |
| Repair Scroll | 10 | 1 |
| Iron Armor Piece with 1-2 Legendary or lower valid enchantments (random level) | 5 | 1 |
| 100k Banknote | 10 | 1 |
| 250k Banknote | 5 | 1 |
| 50% Armor Orb | 8 | 1 |
| 50% Weapon Orb | 8 | 1 |

Mastery Space Chest Loot: 

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| Ultimate Enchantment Book | 10 | 2 |
| Legendary Enchantment Book | 10 | 2 |
| Mastery Enchantment Book | 10 | 1 |
| 60% Blackscroll | 10 | 1 |
| 80% Blackscroll | 8 | 1 |
| Blaze Spawner | 4 | 1 |
| Iron Golem Spawner | 3 | 1 |
| Creeper Spawner | 5 | 1 |
| Repair Scroll | 10 | 2 |
| Iron Armor Piece with 2-3 Legendary or lower valid enchantments (random level). | 5 | 1 |
| 300k Banknote | 12 | 1 |
| 500k Banknote | 6 | 1 |
| 75% Weapon Orb | 8 | 1 |
| 75% Armor Orb | 8 | 1 |

Memory Chest Loot Table:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| Ultimate Space Chest | 30 | 1 |
| Legendary Space Chest | 20 | 1 |
| Mastery Space Chest | 12 | 1 |
| Spring Cosmic Crate Left Half | 4 | 1 |
| Spring Cosmic Crate Right Half | 4 | 1 |
| Summer Cosmic Crate Left Half | 4 | 1 |
| Summer Cosmic Crate Right Half | 4 | 1 |
| Fall Cosmic Crate Left Half | 4 | 1 |
| Fall Cosmic Crate Right Half | 4 | 1 |
| Winter Cosmic Crate Left Half | 4 | 1 |
| Winter Cosmic Crate Right Half | 4 | 1 |

The animation for non memory space chests works as follows:

An inventory filled with 27 glass panes is opened. The color of the glass panes is either Yellow, Orange, or Red based on the rarity of the chest being opened. The player can click on 5 of these panes, turning them white. They cannot click the same pane twice. Each click plays the leather armor equip sound. Once 5 panes have turned white, the rest of the colored panes are revealed from left to right, top to bottom, over the span of 4 seconds to show the loot they did not earn, and then the egg being laid sound is played, and everything disappears from the menu except for the 5 white panes. The player can then click on each pane to reveal one of the pieces of loot from the loot table. Duplicates are allowed. If the player closes the menu before reaching 5 white panes, the chest is returned to them, if they have turned 5 panes white when the close, regardless of where the animation is, exiting out will simply give them the loot. If they do not have inventory space, the loot is dropped on the ground.

# Trials

Trials are a spawnable instance that players can take part in using Trial Portals (eyes of ender). No inventory is persisted from the outside, and items you use in one room do not come with you into future rooms.

Each trial is comprised of a series of rooms, with all or nothing stakes and ever increasing prizes. Each trial by default starts on Room 1 with 10 minutes on the clock. Players can spawn a 2x1 portal anywhere in the overworld to begin their Trial. Between 1 and 4 players can join, and will be sent to the “Decision Box” room for 30 seconds while people join.

Once players have joined, the trial begins. There are three kinds of rooms in Trials. Apprentice, Hardcore, and Demonic. Players will work together to beat rooms, 1 at a time. Dying or running out of time causes you to teleport out of the trial to where the portal was placed, and no loot is earned.

The timer is paused in the Decision Box. Each time players successfully complete a room, the active party is sent to the Decision Box for the normal 30-second decision period and each player is shown an individual chest-style Deal interface displaying the current shared Trial pot.

The Deal interface uses a 3-row, 27-slot chest-style GUI. On the top row, the 4 leftmost slots are Green Stained Glass Panes named “DEAL” in \#55FF55 green. Clicking any of these accepts the deal for that player: they receive a copy of the current pot, leave the Trial permanently, and cannot return to that Trial run. The 4 rightmost slots are Red Stained Glass Panes named “NO DEAL” in red. Clicking any of these declines the deal for that player; they remain in the Trial and continue to the next room with any other players who also declined.

The current accumulated loot is displayed beginning in row 2 from left to right. Each individual loot result occupies its own slot. Duplicate rewards do not stack together and must appear as separate entries. If all 9 slots in row 2 are occupied, additional pot items continue into row 3 from left to right. The ordering of already-earned loot is persistent: once an item has been assigned a pot-display position, later room completions append new loot after the existing entries rather than re-sorting or moving prior rewards.

Every GUI slot not currently occupied by a DEAL pane, NO DEAL pane, or accumulated loot item is filled with a White Stained Glass Pane named “???” in \#FFFFFF white. The unused center slot of the top row therefore also uses this placeholder.

The deal can only accumulate loot, not shrink. After each Apprentice room completion, 1 new loot item is added to the deal and 30 seconds are added to the clock. Hardcore and Demonic room timing changes are described below. 

Successfully beating 4 rooms moves you into the Hardcore room pool, where the rooms are harder, the loot is better, and you get a 3 minute bonus added to the clock as a one time bonus, at the cost of beating hardcore rooms only adding 15 seconds to the clock instead of 30\.

After beating 8 rooms–4 Apprentice and 4 hardcore, you move into the demonic room phase, getting another 3 minute bonus, but you no longer gain bonus time after completing a room. However, the loot in the Demonic loot table is extremely valuable. 

Rooms can be duplicated across a run, but the same room cannot appear twice consecutively. The pool of possible rooms expands over time. A Demonic room cannot appear during the Apprentice phase, though Hardcore rooms and Apprentice rooms can both appear during the Demonic phase. Each room has a base weight of 5 to be rolled. Each time a room appears in that specific trial instance, it’s weight in the pool is reduced by 2\. This means that each room, before modifiers like Snow Globes, can appear up to 3 times. 

### **Global Trial-room completion rule**

> When a Trial room’s completion condition is satisfied, the room immediately ends. The entire active party is transitioned together to the Decision Box. Trial rooms do not require players to enter a physical exit portal after completing the objective.

> For rooms with a final lever, successfully activating the lever after satisfying the room requirements completes the room. For rooms without a lever, satisfying the room’s objective directly completes the room.

> The transition is party-wide and atomic: one player completing the room completes it for all active party members. Room-specific temporary equipment, items, effects, entities, and state are cleaned up through the normal Trial transition before or as the party enters the Decision Box.

> The Decision Box then begins its normal 30-second decision period.

Trial portals can be modified with Trial Trinkets (orange dye item) that give various buffs to the player. There are 3 kinds of trinkets, with several variants each. 

Skip: Allows the player to skip 1, 2, or 3 rooms at the beginning of the Trial with no time spent and the skipped-room loot already added to the deal pot. Skipped rooms do not grant their normal room-completion time bonuses.

Time bonus: Adds 1, 3, or 5 minutes to the Trial’s initial duration.

Insurance: Randomly salvages 1, 2, or 3, loot items from the accumulated pot should the player die or run out of time in their trial.

Apprentice Room Loot Pool:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| \+1 Minute Trial Trinket | 8 | 1 |
| Elite Enchantment Book | 14 | 1 |
| Unique Enchantment Book | 18 | 1 |
| 50% Blackscroll | 10 | 1 |
| Sheep Spawner | 5 | 1 |
| Cow Spawner | 5 | 1 |
| Ultimate Enchantment Book | 10 | 1 |
| Legendary Enchantment Book | 6 | 1 |
| Repair Scroll | 10 | 1 |
| Simple Enchantment Book | 30 | 2 |
| Transmog Scroll | 12 | 2 |
| \+1 Insurance Trial Trinket | 8 | 1 |
| Skip 1 Room Trial Trinket | 8 | 1 |
| Whitescroll | 10 | 1 |
| Zombie Spawner | 5 | 1 |
| Ultimate Space Chest | 7 | 1 |

Hardcore Room Loot Pool:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| \+3 Minutes Trial Trinket | 12 | 1 |
| 75% Blackscroll | 10 | 1 |
| Armor Enchantment Orb | 8 | 1 |
| Blaze Spawner | 10 | 1 |
| Creeper Spawner | 10 | 1 |
| Legendary Enchantment Book | 10 | 1 |
| Legendary Enchantment Book | 3 | 2 |
| Skip 1 Room Trial Trinket | 12 | 1 |
| Skip 2 Room Trial Trinket | 6 | 1 |
| Ultimate Enchantment Book | 10 | 1 |
| Ultimate Enchantment Book | 5 | 2 |
| Weapon Enchantment Orb | 8 | 1 |
| Whitescroll | 15 | 1 |
| Random Boss Spawn Egg | 8 | 1 |
| Random Mask | 10 | 1 |
| Repair Scroll | 10 | 1 |
| Abandoned Spaceship Dungeon Portal | 6 | 1 |
| 35% Yeti Crystal | 4 | 1 |
| 35% Ranger Crystal | 4 | 1 |
| Mask Splicer | 7 | 1 |
| Legendary Space Chest | 8 | 1 |

Demonic Room Loot Pool:

| Heroic Crystal | 12 | 1 |
| :---- | :---- | :---- |
| 75% Yeti Crystal | 6 | 1 |
| 75% Ranger Crystal | 6 | 1 |
| Conquest Chest Flare | 10 | 1 |
| Mkit Unlock | 4 | 1 |
| Iron Golem Spawner | 10 | 2 |
| Witch Spawner | 10 | 2 |
| Blaze Spawner | 10 | 2 |
| Wither Skeleton Spawner | 10 | 2 |
| \+5 Minutes Trial Trinket | 5 | 1 |
| Skip 3 Rooms Trial Trinket | 5 | 1 |
| \+3 Insurance Trial Trinket | 5 | 1 |
| Random Boss Spawn Egg | 8 | 1 |
| Mkit Refresher | 6 | 1 |
| Mastery Enchantment Book | 12 | 1 |
| Random Double Mask | 10 | 1 |
| Heroic Abandoned Spaceship Dungeon Portal | 4 | 1 |
| Abandoned Spaceship Dungeon Portal | 10 | 1 |
| Memory Chest | 3 | 1 |
| Mastery Space Chest | 6 | 1 |

Trial Edge Cases and Multiplayer Rules  
\- Trial inventories are normalized by the room system. Before entry, each player's full outside inventory, armor, offhand, and required persistent player state are safely snapshotted. Trial-room items are cleared between rooms and do not carry into later rooms. The outside snapshot is restored when the player leaves the Trial.  
\- In multiplayer, the Trial pot is shared while players remain together, but each player makes an individual cash-out decision in the Decision Box. A player who accepts the deal receives a copy of the current pot and leaves permanently; remaining players may continue.  
\- If an individual player dies, that player is removed from the Trial and receives no pot loot except anything salvaged by their Insurance Trinket. Other surviving players may continue.  
\- If the shared Trial timer reaches zero, all remaining players fail and are removed. Insurance applies individually.  
\- The timer remains paused during Decision Box periods.  
\- Disconnects, crashes, or world closure must never destroy the player's outside inventory snapshot or return-location data. The implementation should persist enough Trial state to recover the player safely rather than duplicate or delete items.  
\- Trial-specific exceptions to normal instance protection are defined per room. For example, Bomb Squad may allow only its intended Creeper explosions to destroy designated walls.

### **Trial Room Entry Title Display**

When players enter a Trial Room, a large centered on-screen title should appear, styled similarly to a vanilla title/subtitle message.

#### **Format**

* **Title:** the **Trial Room name**  
* **Subtitle:** a short countdown message

#### **Visual rules**

* The **room name is always orange**.  
* Recommended hex color for Trial Room titles: **`#FFAA00`**  
* Subtitle text should use a neutral light color, with the countdown number highlighted in green.

#### **Behavior**

* The title stays on screen for **5 seconds total**.  
* The subtitle counts down once per second while visible.  
* Example for a room named **Celestial Searching**:  
  * Title: `Celestial Searching` in `#FFAA00`  
  * Subtitle sequence:  
    * `Starting in... 5s`  
    * `Starting in... 4s`  
    * `Starting in... 3s`  
    * `Starting in... 2s`  
    * `Starting in... 1s`

This message is purely a start-of-room presentation and countdown indicator.

---

### **Decision Box Entry Title Display**

When players are teleported into the **Decision Box**, a similar large centered title should appear.

#### **Format**

* **Title:** `Decision Box`  
* **Subtitle:** a decision timer message

#### **Visual rules**

* `Decision Box` should use the same orange Trial title color: **`#FFAA00`**  
* The subtitle should indicate that the player must choose whether to **DEAL** or **NO DEAL**.

#### **Behavior**

* The title stays on screen for **5 seconds total**.  
* The subtitle counts down during those 5 seconds using the current decision timer.  
* Recommended subtitle format:  
  * `30 seconds to choose!`  
  * then updating each second while displayed:  
    * `29 seconds to choose!`  
    * `28 seconds to choose!`  
    * etc.

If preferred, the subtitle can be phrased as:

* `Choose DEAL or NO DEAL! 30s remaining`  
* or  
* `30 seconds remaining to choose!`

The important part is that the player is clearly told:

1. they are in the **Decision Box**  
2. they have a **limited time to choose**

---

### **Decision Box GUI**

When a player enters the Decision Box, a **27-slot chest-style GUI** opens to process the decision.

#### **Top row**

* Slots **1–4:** **Green Stained Glass Panes**  
  * Display name: **`DEAL`** in green  
* Slot **5:** **White Stained Glass Pane**  
  * Display name: **`???`** in `#FFFFFF`  
* Slots **6–9:** **Red Stained Glass Panes**  
  * Display name: **`NO DEAL`** in red

#### **Loot display**

* The **accumulated Trial loot** appears starting on **row 2**, left to right.  
* If row 2 fills completely, continue onto **row 3**, left to right.  
* **Duplicate loot does not stack**. Each duplicate occupies its own slot.  
* The order of displayed loot must remain **consistent with acquisition order** and **must not reshuffle** between rooms.

#### **Empty slots**

* Any unused slots in the GUI are filled with **White Stained Glass Panes**  
* Display name: **`???`** in `#FFFFFF`

#### **Multiplayer behavior**

* Deal decisions are handled **per-player**  
* Choosing **DEAL** cashes that player out with a copy of the current shared pot  
* Choosing **NO DEAL** keeps that player in the Trial

#### **Overflow note**

* This 3-row interface holds up to **18 loot items**  
* If a Trial ever exceeds 18 displayed items, a **pagination system** should be used rather than expanding the default interface

# Rooms

***Circuit Circus — Apprentice Room***

Players spawn at the room’s standalone emerald-block marker. The room contains 8 fixed placeholder pillar locations. At the beginning of each run, these are randomized into exactly two 2×1 Redstone Block pillars, two 2×1 Gold Block pillars, two 2×1 Emerald Block pillars, and two 2×1 Diamond Block pillars.

The floating stained-glass targets above the arena always use the same fixed layout. Each color corresponds to one circuit:

* Red stained glass → Redstone  
* Yellow stained glass → Gold  
* Green stained glass → Emerald  
* Blue stained glass → Diamond

The player enters the room with an Infinity bow. Successfully shooting a floating stained-glass target removes the glass and awards 1 stained-glass block of that target’s color, with a 50% chance to award 2 instead.

The goal is to use the collected stained glass to create a continuous path between the two matching pillars of each color. Circuit connections count only through orthogonally adjacent blocks; diagonal contact does not count. Circuits may cross by building one glass path above another.

The fixed target layout contains approximately 25–39 available targets of each color. Even under the longest valid randomized pillar arrangements, each color has at least roughly 3–6 more available glass blocks than the expected maximum route length, providing limited tolerance for inefficient placement without making resource management irrelevant.

Once all four circuits are valid and complete, the player may activate the room’s lever. Activating the lever before all four circuits are complete does not complete the room.

When the lever is successfully activated, Circuit Circus immediately completes and the entire active party is transitioned directly to the Decision Box under the global Trial-room completion rules. No exit portal or additional extraction period is used.

Room-specific temporary equipment and building materials are cleared when the player leaves the room.

***Zero-G — Hardcore Room***

Zero-G is a vertical movement room built around controlled Shulker levitation. Players spawn in the water at the standalone Emerald Block marker at the bottom of the shaft. The room contains 10 pressure-plate platforms distributed vertically throughout the shaft. Eight fixed encounter Shulkers fire at the players and provide the Levitation used to navigate the room.

Players enter the room wearing a full set of Iron Armor with Protection IV, Angelic V, and Unbreaking III on each piece. Each player's starting hotbar contains 2 Golden Apples in slot 1 followed by 3 Milk Buckets in slots 2–4. These items are temporary Trial-room equipment and do not persist after leaving the room.

The objective is to personally step on all 10 pressure plates throughout the shaft. When a player activates an uncompleted pressure plate, that pressure plate disappears and permanently counts as completed for the current room attempt. Each completed pressure plate independently has a 25% chance to give the activating player one Ender Pearl. Ender Pearls may be used to make more deliberate movement choices within the shaft.

Milk may be used to remove Levitation and deliberately begin falling. It retains normal vanilla milk behavior, including removing other active potion effects. Golden Apples provide limited emergency survivability.

All 10 pressure plates are shared objectives in multiplayer; a plate completed by any player counts toward room completion for the whole party. Any Ender Pearl awarded by a plate goes to the player who activated that plate.

The room's Shulkers are encounter fixtures rather than kill objectives. They cannot be permanently killed, displaced/teleported away, or used to obtain loot during the Trial.

Once all 10 pressure plates have been activated, Zero-G immediately completes and the entire active party is transitioned directly to the Decision Box under the global Trial-room completion rules. No exit portal or additional extraction period is used.

Room-specific armor, consumables, Ender Pearls, and other temporary inventory are cleared through the normal Trial-room inventory transition when the party leaves Zero-G.

***Fire Colony — Hardcore Room***

Fire Colony is a narrow vertical traversal maze built around Lava, Fire, Soul Fire, ladders, confined passages, and difficult platforming. The room is presented as a cross-section of a burning subterranean colony enclosed behind red stained glass.

Players spawn at the room's standalone Emerald Block marker.

Each player enters Fire Colony wearing a full set of **red-dyed Leather Armor with no enchantments** and receives **1 Golden Apple**. All supplied equipment and consumables are temporary Trial-room items and are removed through the normal Trial transition when the room ends.

Fire Resistance is not provided. Fire, Soul Fire, and Lava retain their normal damaging and ignition behavior against players. The Golden Apple is intended as a single emergency survivability resource rather than general protection from the room's hazards.

The objective is to navigate through the colony's tunnels, ladders, platforms, Fire, Soul Fire, and Lava until reaching the lever at the end of the maze.

**Successfully flicking the end lever immediately completes Fire Colony.** Completing the room transitions the entire active party directly to the Decision Box; no exit portal or additional extraction period is used.

The room environment is protected against permanent mutation. Its hazards continue to affect players normally, but fire spread, structural burning, unintended lava propagation, and other environmental changes must not alter the intended room layout.

***Raiding Rainbow — Apprentice Room***

Raiding Rainbow is a party-wide trial-and-error sequence room built around eight color-coded Zombies.

Players spawn at the room's standalone Emerald Block marker in the center of the arena. Eight fixed Gold Block locations around the arena serve as the spawn positions for eight Zombies.

Each Zombie has exactly **1 HP** and wears Wool corresponding to one of eight colors:

Red, Orange, Yellow, Lime, Green, Cyan, Light Blue, and Blue.

Each color appears exactly once. The Zombies retain normal basic movement and may wander around the arena and attack players, but they are not intended to present meaningful combat danger. They are encounter targets and do not drop equipment, Wool, normal Zombie loot, or XP.

At the beginning of each room attempt, the server generates a random permutation of all eight colors. **The sequence is hidden from the players and remains unchanged for the entire room attempt.**

The party must discover and complete this sequence through trial and error. Sequence progress is shared across the party.

When a player kills the currently correct Zombie, the kill is accepted and the party advances one position in the sequence. A chat message is sent to the party identifying the successful color and current progress.

Example:

`Green was correct! 1/8!`

The correctly killed Zombie remains dead while the party continues attempting the sequence.

If any player kills a Zombie whose color is not currently correct, the attempt immediately resets. All remaining room Zombies are despawned and a fresh set of all eight color-coded Zombies respawns at the original Gold Block locations. Party sequence progress returns to 0/8, but **the hidden randomized sequence does not change**.

When this occurs, all party members receive:

`You killed a zombie in the wrong order!`

Players are expected to use the information learned from previous successful kills to progressively discover the complete sequence.

When the eighth Zombie is killed in the correct position, **Raiding Rainbow immediately completes** and the entire active party transitions directly to the Decision Box under the normal Trial-room completion rules.

Each player spawns into the room with an unbreaking 3 wooden sword, 16 cooked porkchop, and 2 enderpearls. 

All Raiding Rainbow Zombies and associated temporary encounter state are cleared during the transition.

***Hidden Graveyard — Demonic Room***

Hidden Graveyard is a three-wave combat room built around escalating Undead Corpse waves, searching for randomized Trial Keys, and intentionally allowing players to overlap waves in exchange for faster completion.

The entire Hidden Graveyard room area uses the **Pale Garden biome** so biome-tinted grass and vegetation use the intended pale coloration.

Players spawn at the room's standalone **Emerald Block marker**.

The room contains **11 fixed Coal Block markers** representing possible chest spawn locations. Only one Hidden Graveyard chest may exist at a time. Whenever a chest is generated, one of the available Coal Block locations is selected randomly and the chest appears there.

Each chest contains exactly one room-issued **vanilla Trial Key**. Once the key is removed and the chest becomes empty, the chest immediately disappears without dropping itself.

Throwing the current room-issued Trial Key into the water inside the central well consumes the key and summons the next eligible wave.

When any wave begins:

* play `minecraft:block.end_portal.spawn` fairly loudly for all players participating in Hidden Graveyard;  
* send all participating players the chat message:

`Wave X has spawned!`

where `X` is the newly summoned wave number.

### **Player Loadout**

Each player enters Hidden Graveyard wearing temporary:

**Iron Helmet**

* Protection IV  
* Unbreaking III  
* Ender Shift III

**Iron Chestplate**

* Protection IV  
* Unbreaking III

**Iron Leggings**

* Protection IV  
* Unbreaking III  
* Nutrition III

**Iron Boots**

* Protection IV  
* Unbreaking III

Each player also receives a temporary **Diamond Axe** with:

* Sharpness V  
* Unbreaking III  
* Insanity VIII  
* Pummel III

Consumables:

* **5 Apples**  
* **4 Splash Potions of Instant Health II**

All supplied equipment, consumables, and other room-specific inventory are temporary Trial items and are removed through the normal Trial room-transition system when Hidden Graveyard ends.

### **Undead Corpse Base Rules**

Every enemy spawned by Hidden Graveyard is a normal hostile **Undead Corpse** and therefore retains the canonical Undead Corpse properties defined in the Mob section.

This includes:

* Zombie base model at 0.9× scale;  
* 15 HP;  
* no innate armor or armor toughness;  
* Iron Axe;  
* speed 0.4;  
* independent 30% chance for Sharpness III on its axe;  
* independent 30% chance for Bleed III;  
* independent 30% chance for Rage III;  
* no default mob drops.

Hidden Graveyard then adds the wave-specific armor and armor enchantments described below.

Encounter Undead Corpses award **no XP, normal mob loot, equipped weapons, armor, or enchantments** when killed.

---

### **Initial State**

No Undead Corpses are active when the room begins.

A chest containing the first Trial Key spawns at one of the 11 randomized chest locations.

Players must locate the chest, take its key, and throw the key into the central well to begin Wave 1\.

---

### **Wave 1 — Cobblestone Graves**

Using the first Trial Key simultaneously destroys all six **Cobblestone graves**.

All blocks belonging to those six Cobblestone grave structures disappear without dropping items. The destruction should occur effectively simultaneously and may use appropriate block-breaking particles/sounds.

One hostile Undead Corpse spawns from each destroyed grave, producing:

**6 Wave 1 Undead Corpses**

Wave 1 Corpses wear full:

**Leather Armor**

with no additional wave-specific armor enchantments.

The normal Undead Corpse weapon/enchantment generation still applies.

When **3 of the 6 Wave 1 Corpses have been killed**, another chest containing a Trial Key spawns at a randomly selected chest location.

The remaining Wave 1 Corpses do **not** need to be killed before the second key can be located and used.

---

### **Wave 2 — Andesite Graves**

Throwing the second Trial Key into the well simultaneously destroys all six **Andesite graves** and summons:

**6 Wave 2 Undead Corpses**

Play:

`minecraft:block.end_portal.spawn`

fairly loudly for the participating party and send:

`Wave 2 has spawned!`

Wave 2 Corpses wear full **Chainmail Armor**.

Every armor piece has:

**Protection I**

In addition:

* Boots receive a random **Luck I–X**  
* Leggings receive a random **Luck I–X**

The two Luck levels roll independently for every individual Corpse.

For example, one Corpse could receive:

* Luck VIII Boots  
* Luck X Leggings

while another could receive:

* Luck II Boots  
* Luck II Leggings.

The normal Undead Corpse axe and its independent Sharpness III/Bleed III/Rage III generation remain unchanged.

When **3 of the 6 Wave 2 Corpses have been killed**, the third and final Trial Key chest becomes available.

Only kills belonging to **Wave 2** count toward this threshold. Surviving Wave 1 Corpses do not.

The party may use the third key even while enemies from Waves 1 and 2 remain alive.

---

### **Wave 3 — Stone Graves**

Throwing the third Trial Key into the well simultaneously destroys all six **Stone graves** and summons:

**6 Wave 3 Undead Corpses**

Play:

`minecraft:block.end_portal.spawn`

fairly loudly for the participating party and send:

`Wave 3 has spawned!`

Wave 3 Corpses wear full **Iron Armor**.

Every armor piece has:

**Protection II**

In addition:

* Boots receive random **Luck V–X**  
* Leggings receive random **Luck V–X**  
* Chestplate receives random **Aegis I–VI**

All three enchantment levels roll independently for each Corpse.

The normal Undead Corpse axe and its independent Sharpness III/Bleed III/Rage III rolls remain active.

---

### **Wave Overlap**

Hidden Graveyard deliberately allows waves to overlap.

The next Trial Key becomes available after only **3 kills from the current wave**, rather than requiring the entire wave to be cleared.

A cautious party may fully clear each wave before using the next key.

A faster or more confident party may immediately search for and use the next key once its three-kill threshold has been reached.

At maximum intended overlap, the room can contain:

* 3 surviving Wave 1 Corpses  
* 3 surviving Wave 2 Corpses  
* all 6 Wave 3 Corpses

for a total of:

**12 active Undead Corpses simultaneously.**

This is an intentional risk/reward feature of the room.

Each Corpse retains its originating wave identity. Kills from an earlier wave never contribute toward the three-kill unlock threshold of a later wave.

---

### **Completion**

Hidden Graveyard completes only when:

1. Wave 1 has been summoned;  
2. Wave 2 has been summoned;  
3. Wave 3 has been summoned; and  
4. **every remaining Undead Corpse from all three waves has been killed.**

The final wave does not have an additional chest or key.

As soon as the final surviving Undead Corpse dies, **Hidden Graveyard immediately completes** and the entire active party is transitioned directly to the Decision Box under the global Trial-room completion rules.

No exit portal, final lever, or extraction period is used.

All remaining room-specific entities, items, temporary equipment, wave state, chest state, and other temporary encounter data are cleaned up during the transition.

---

### **Chest / Key Safeguards**

The Trial Keys used by Hidden Graveyard should be internally identifiable as room-issued Hidden Graveyard keys even though they use the vanilla Trial Key presentation.

Only the currently valid room key should advance the encounter when thrown into the central well.

A key thrown somewhere other than the well should remain recoverable rather than being silently destroyed.

Only one active key chest may exist at a time.

Once a valid key has triggered its wave, that key cannot trigger the wave again.

***Bomb Squad — Hardcore Room***

Bomb Squad is a destructive exploration room consisting of a **6×6 grid of rooms**. Each individual room is approximately **8×8 blocks**, with neighboring rooms separated by shared walls containing designated destructible Stone sections.

At the beginning of the room, one of four fixed **Emerald Block spawn markers** is selected randomly. The entire active party spawns together at the selected marker. Players are never split between multiple starting locations.

Each of the four possible spawn rooms contains a **Gold/Light Weighted Pressure Plate** serving as a Creeper Spawn Egg supply point.

When a participating player steps on one of these Gold Pressure Plates while they do not already possess a Creeper Spawn Egg, they receive exactly:

**1 Creeper Spawn Egg**

A player who already has a Creeper Spawn Egg receives nothing.

All four supply plates remain active throughout the room. Although only one spawn room is used as the party's initial starting point, players who later blast their way into another possible spawn room may use that room's Gold Pressure Plate as an additional Creeper Egg resupply location.

Players are expected to sometimes backtrack to known supply rooms after consuming their current Creeper Egg. Creeper Eggs are **not automatically replenished after an explosion**.

### **Player Loadout**

Each player enters Bomb Squad with:

**Armor**

* Full Leather Armor  
* Blast Protection I on every piece  
* Unbreaking III on every piece

**Tools / Weapons**

* Flint and Steel with Unbreaking III  
* Wooden Sword with Knockback II and Unbreaking III

**Food / Healing**

* 5 Steak  
* 5 Golden Apples

All supplied equipment, food, Creeper Eggs, and other room-specific items are temporary Trial resources and are removed through the normal Trial transition when Bomb Squad ends.

### **Creeper Mechanic**

Players use the supplied Creeper Spawn Eggs to summon encounter Creepers.

Encounter Creepers retain their normal aggression toward participating players. Players may:

* allow a Creeper to ignite naturally;  
* manually ignite it using the supplied Flint and Steel;  
* use the Knockback II Wooden Sword to reposition it before detonation.

Bomb Squad is one of the explicit exceptions to normal Trial terrain protection.

The shared walls between neighboring grid rooms contain specifically designated **Stone sections** that may be destroyed by Bomb Squad encounter Creeper explosions.

Destroying these sections opens passages into adjacent rooms and allows the party to explore the 6×6 grid.

Creeper explosions must **not** destroy unrelated Trial terrain. Floors, ceilings, structural/decorative blocks, markers, and other non-designated terrain remain protected.

Encounter Creepers award no XP or normal mob loot.

### **Exit Generation**

Bomb Squad contains a fixed set of possible **Diamond Block exit markers** distributed throughout the 6×6 room grid.

At the beginning of each attempt, exactly **2 eligible Diamond Block markers** are selected randomly.

Each selected location receives an **Iron/Heavy Weighted Pressure Plate** representing a valid exit.

Only one exit needs to be discovered.

No possible exit may spawn:

* inside the selected starting room;  
* inside any room directly orthogonally adjacent to the selected starting room;  
* inside any room diagonally adjacent to the selected starting room.

All other designated exit locations are eligible.

The locations of the two active exits are hidden from the party.

### **Pressure Plate Protection and Activation**

All Bomb Squad special pressure plates are **Creeper-proof and explosion-proof**.

This includes:

* Gold Creeper Egg supply plates;  
* Iron exit plates.

Bomb Squad Creeper explosions cannot destroy, displace, or invalidate these plates.

Although vanilla pressure plates are used visually, the room-specific effects are triggered only by a **participating player** stepping on the relevant plate.

Creepers, other mobs, dropped items, projectiles, or unrelated entities cannot:

* dispense Creeper Spawn Eggs;  
* complete the room.

### **Completion**

When any participating player steps on either active Iron exit pressure plate, **Bomb Squad immediately completes for the entire active party**.

Only one of the two exits must be found.

The entire party is immediately transitioned to the Decision Box under the normal Trial-room completion rules.

No exit portal, final lever, or additional extraction phase is used.

All Bomb Squad Creepers, Creeper Spawn Eggs, temporary equipment, active exit state, and other room-specific state are cleaned up during the transition. The room's destructible walls are restored before the structure is reused.

### ***Deadeye***

**Type:** Trial Room  
 **Difficulty:** Demonic  
 **Room Size:** 2x1 structure room  
 **Theme:** Precision / Parkour / Archery

**Overview:**  
 Deadeye is a Demonic parkour trial built across two connected room chunks: **Deadeye West** and **Deadeye East**. The player spawns on the **emerald block** in the West half and must complete a long parkour route to the **lever** at the end of the East half. The core gimmick is that major parkour segments begin **hidden**, and must be revealed by shooting **target blocks** with a bow. Each target reveals the next section of jumps, allowing the player to continue.

The room is intended to test aim, composure, and parkour execution. Falling is an instant environmental execution for the player who falls. That player is removed from the Trial under the normal individual-death rules, while surviving party members continue Deadeye.

---

### **Room Flow**

* The player spawns on the **emerald block** at the start of Deadeye West.  
* The player is given a powerful bow and must use it to hit target blocks placed throughout the room.  
* Each target reveals the next themed parkour subsection.  
* The player progresses through all revealed sections in order.  
* Reaching the final platform and flicking the **lever** completes the room.  
* On completion, the room is considered cleared and the party is teleported to the decision box under the newer trial flow rules.

---

### **Reveal Mechanic**

Each target block is surrounded by the primary material of the parkour section it controls. This is used to determine which hidden blocks should be revealed when that target is hit.

#### **Target / Section Mapping**

* **Diamond section target** → reveals the **diamond-themed section**  
  * Related blocks: **diamond blocks, prismarine walls, ladders**  
* **Gold section target** → reveals the **gold-themed section**  
  * Related blocks: **gold blocks, bamboo trapdoors, fences**  
* **Resin Brick section target** → reveals the **resin section**  
  * Related blocks: **resin bricks, resin brick walls**  
* **Purpur section target** → reveals the **purpur section**  
  * Related blocks: **purpur blocks / platforms, purpur slabs**  
* **Crimson/Hyphae section target** → reveals the **crimson-themed section**  
  * Related blocks: **crimson hyphae / warped-crimson themed blocks, crimson fences**

**Implementation note:**  
 All blocks belonging to a future section should begin **hidden/inactive**, then become visible/usable when that section’s target is successfully shot.

---

### **Loadout**

The player spawns with:

* **Leather Leggings**  
  * **Nutrition III**  
* **16 Golden Apples**  
* **1 Arrow**  
* **1 Bow**  
  * **Infinity I**  
  * **Power V**  
  * **Lightning IV**  
  * **Unbreaking III**  
  * **Flame**  
  * **Eagle Eye V**  
  * **Transmogged**

---

### **Failure Condition**

If a participating player falls below the room's fail threshold, that player alone is immediately executed and removed from the Trial. The room continues for any surviving party members.

**Recommended implementation:**  
 Use a **Y-level fail check** rather than direct tinted-glass contact detection.

#### **Suggested rule**

* Record the Y-level of the emerald spawn block.  
* If a player’s Y-value drops to 10 or more below spawn Y, that player is immediately executed and removed from the Trial; surviving party members continue the room.

This is probably the cleanest implementation.

**Why this is better than tinted-glass contact detection:**

* simpler,  
* fewer edge cases,  
* less block-contact weirdness,  
* easier for Codex to implement consistently.

The tinted glass floor still works well visually as the “death void,” but the **Y-threshold** is the better actual fail logic.

---

### **Completion**

* The room is completed when the player reaches the endpoint and **flicks the lever**.  
* Completion immediately clears the room and teleports the party to the decision box.

---

## **Implementation / Codex notes**

### **Chunk alignment**

Treat:

* **Deadeye West** as the **spawn / opening half**  
* **Deadeye East** as the **continuation / ending half**

They should be connected as a **2x1 room**, with the parkour route flowing naturally from West into East toward the final lever.

### **Hidden-section behavior**

Codex should:

1. identify the prebuilt parkour blocks belonging to each themed subsection,  
2. hide them at room start,  
3. listen for the correct target block to be struck,  
4. reveal the corresponding subsection.

### **Important room rules**

* The player must **not** start with more than 1 arrow.  
* Infinity handles the rest.  
* The lever is the only completion trigger.  
* Falling is instant failure for that player only; surviving party members continue.  
* This is a **precision room**, not a combat room.
