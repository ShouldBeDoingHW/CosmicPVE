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
| Mortal Coil | 3 | Sword/Axe | Mastery | 2% chance to proc on hit. On proc gain 2 full hearts of absorption and your next attack does (5% x level) more damage. |
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

# Armor Sets

| Name | Set Bonus | Primary Procurement Method |
| :---- | :---- | :---- |
| Phantom | \+25% out, \+10 inc. Mastery enchants proc 25% more often. | Dungeons |
| Yjiki | \-15% inc, \+5% out. Doubled in dungeons. | Dungeons |
| Dimensional Traveler | \+7.5% out, \+10% movement speed. \-20% proc cooldown (Phoenix, Ender Shift, etc.) | Invasions |
| Engineer | \-25% inc, \+15% movement speed | Invasions |
| Yeti | \+10% out, \-10% inc, immune to freeze, frozen, permafrost, ice aspect. | Igloo |
| Ancient | \+7.5% out, \-7.5% inc, doubled when under 50% health. | Ancient City |
| Ranger | \+20% bow/crossbow damage, \+20% movement speed. | Trials |
| Dragonslayer | \+15% Outgoing, \-5% incoming. Immunity to poison and fire damage.  | Primal Dragon Boss |

# Kits

Kits are not a routine part of this game. Instead, you unlock the kit. However, instead of using a persistent time gated cooldown, kits are refreshed by “Refresh Crystals” which allow you to claim all unlocked kits of the corresponding type once.

G-kit:

Standing for God Kits, these kits contain either a weapon or an armor piece (non heroic) that has 3-5 custom enchantments on it.

Kit Variants. Unlock items are all a Diamond.

Gladiator, Viking, Paladin, Butcher, Grandmaster.

M-Kit:

Standing for Mastery Kits. These powerful kits contain 1 piece of already heroic armor but no weapons. Each piece of armor has exactly 3 custom enchantments. On top of this, each time you claim your M-Kit, you receive a mastery enchantment book for free.

Kit variants and unlock item textures:

Death Knight (purple dye), Necromancer (green dye), and Ghost (blue dye). 

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

# Trials

Trials are a spawnable instance that players can take part in using Trial Portals (eyes of ender). No inventory is persisted from the outside, and items you use in one room do not come with you into future rooms.

Each trial is comprised of a series of rooms, with all or nothing stakes and ever increasing prizes. Each trial by default starts on Room 1 with 10 minutes on the clock. Players can spawn a 2x1 portal anywhere in the overworld to begin their Trial. Between 1 and 4 players can join, and will be sent to the “Decision Box” room for 30 seconds while people join.

Once players have joined, the trial begins. There are three kinds of rooms in Trials. Apprentice, Hardcore, and Demonic. Players will work together to beat rooms, 1 at a time. Dying or running out of time causes you to teleport out of the trial to where the portal was placed, and no loot is earned.

The timer is paused in the decision box, and each time players successfully beat a room, they are sent to the decision box for 30 seconds where a menu will flash on screen with “the pot”, loot that they have accumulated thus far. If you accept the deal, you are teleported out of the trial and cannot return, but are given the loot. If you decline the deal, you proceed to the next room in pursuit of obtaining even more loot for the pool.

The deal can only accumulate loot, not shrink. After each Apprentice room completion, 1 new loot item is added to the deal and 30 seconds are added to the clock. Hardcore and Demonic room timing changes are described below. 

Successfully beating 4 rooms moves you into the Hardcore room pool, where the rooms are harder, the loot is better, and you get a 3 minute bonus added to the clock, at the cost of beating hardcore rooms only adding 15 seconds to the clock instead of 30\.

After beating 8 rooms–4 Apprentice and 4 hardcore, you get another 3 minute bonus, but you no longer gain bonus time after completing a room. However, the loot in the Demonic loot table is extremely valuable. 

Rooms can be duplicated across a run, but the same room cannot appear twice consecutively. The pool of possible rooms expands over time. A Demonic room cannot appear during the Apprentice phase, though Hardcore rooms and Apprentice rooms can both appear during the Demonic phase.

Trial portals can be modified with Trial Trinkets (orange dye item) that give various buffs to the player. There are 3 kinds of trinkets, with several variants each. 

Skip: Allows the player to skip 1, 2, or 3 rooms at the beginning of the Trial with no time spent and the skipped-room loot already added to the deal pot. Skipped rooms do not grant their normal room-completion time bonuses.

Time bonus: Adds 1, 3, or 5 minutes to the Trial’s initial duration.

Insurance: Randomly salvages 1, 2, or 3, loot items from the accumulated pot should the player die or run out of time in their trial.

Apprentice Rooms: raiding rainbow (zombies with colored wool in 7 variants spawn around a room, kill them in proper order), fire colony (a vertical maze based on an ant colony filled with fire and lava), bomb squad (spawn in the center of a 5x5 grid of rooms. The lever is somewhere on the perimeter, and creepers spawn in each room that you must light with flint and steel and punch into the walls to knock down), killing floor (a 20x30 grid of pressure plates. Only 1 path through is safe while the rest send you back to the start. Stepping on a safe plate turns it gold.)

Hardcore rooms: Dropper (self explanatory), Cinderwolf (a boss that shoots constant knockback fireballs), Portal Panic (spawn in a room with parkour in each direction leading to an ore. Possible ores are redstone, lapis, emerald, glowstone, or diamond. Mine the ore and head back to center where each end portal frame randomly needs 1 of the materials), Repo Man (a boss that is weak but spawns 2 bats every 20% health he loses that need to be killed before you can damage him further).

Demonic Rooms: Rock, Paper, Scissors (a boss that can only be damaged by stone, paper, or shears in your inventory and changes mode every 8-12 hits), Undertow (a blue stained glass maze that’s completely under water to make visibility terrible), Inventor (a boss that has a bunch of switches in the room, every 10 seconds highlights one that you need to flick within 8 seconds or you lose).

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
| Sheep Spawner | 5 | 1 |
| Simple Enchantment Book | 30 | 2 |
| Transmog Scroll | 12 | 2 |
| \+1 Insurance Trial Trinket | 8 | 1 |
| Skip 1 Room Trial Trinket | 8 | 1 |
| Whitescroll | 10 | 1 |
| Zombie Spawner | 5 | 1 |

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
| 25% Yeti Crystal | 4 | 1 |
| 25% Ranger Crystal | 4 | 1 |
| 25% Ancient Crystal | 4 | 1 |

Demonic Room Loot Pool:

| Heroic Crystal | 12 | 1 |
| :---- | :---- | :---- |
| 60% Yeti Crystal | 6 | 1 |
| 60% Ranger Crystal | 6 | 1 |
| 60% Ancient Crystal | 6 | 1 |
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

Trial Edge Cases and Multiplayer Rules  
\- Trial inventories are normalized by the room system. Before entry, each player's full outside inventory, armor, offhand, and required persistent player state are safely snapshotted. Trial-room items are cleared between rooms and do not carry into later rooms. The outside snapshot is restored when the player leaves the Trial.  
\- In multiplayer, the Trial pot is shared while players remain together, but each player makes an individual cash-out decision in the Decision Box. A player who accepts the deal receives a copy of the current pot and leaves permanently; remaining players may continue.  
\- If an individual player dies, that player is removed from the Trial and receives no pot loot except anything salvaged by their Insurance Trinket. Other surviving players may continue.  
\- If the shared Trial timer reaches zero, all remaining players fail and are removed. Insurance applies individually.  
\- The timer remains paused during Decision Box periods.  
\- Disconnects, crashes, or world closure must never destroy the player's outside inventory snapshot or return-location data. The implementation should persist enough Trial state to recover the player safely rather than duplicate or delete items.  
\- Trial-specific exceptions to normal instance protection are defined per room. For example, Bomb Squad may allow only its intended Creeper explosions to destroy designated walls.

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
| Legendary Enchantment Book | 10 | 2 |
| Gkit Unlock Gem | 8 | 1 |
| 40% Phantom Armor Crystal | 10 | 1 |
| 40% Yjiki Armor Crystal | 10 | 1 |
| Heroic Crystal | 8 | 1 |
| White Scroll | 12 | 2 |
| Trial Portal | 10 | 1 |
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

# Misc Items

Repair Scroll \- can be applied to any item to refill durability up to maximum. Item texture is paper.

Heroic Crystal \- Can be applied one time maximum to dungeon portals, pickaxes and shovels, and armor. When applied to armor, it turns the model into leather and gives \+250 maximum durability. Pickaxes and shovels turn to a gold texture and gain \+250 maximum durability. If an eligible item is already damaged when the crystal is applied, both its current durability and maximum durability increase by 250\. When applied to dungeon portals, it turns them into their Heroic variants. Item texture is an amethyst shard.

Kit Refreshers- Consume to renew all of your available kits, allowing you to claim them.Comes in “Mkit”, “Gkit”, or “Super” variants, which allow you to refresh your GKits, Mkits, or both respectively. Item texture is the item quartz. 

Dungeon Key Ring \- Item that once right clicked drops 1 of each type of dungeon key into the player’s inventory. Cannot be opened if there isn’t inventory space. 

Conquest Chest Flare \- spawns a conquest chest in a valid spawn location within \+/- 100 blocks in either direction from the player’s current location. Uses a redstone torch base model. Description text and name text is \#BF0000. 

# Masks / Skins

# Masks / Skins

# Masks

Masks are an additional customization and buildcraft layer. Most masks are found individually. Inside an anvil, players may combine single masks into double, and a double \+ single mask into a triple. No multi mask may contain more than 3 masks at this time, though this should be a variable that can be changed up or down.

Masks should attach to or be associated with the player's helmet rather than replacing the armor-set helmet slot. A Multi-Mask provides the effects of all masks contained within it. For rendering, Multi-Masks use a black box with a question mark on the front. The contained masks still provide their effects without requiring the visuals to be physically layered on the player's face.

## Mask List

Santa — \+2 maximum HP.  
Reindeer — \+3% movement speed.  
Purge — \+3% outgoing damage.  
Party — \+1% outgoing damage, \-1% incoming damage, and \+1% movement speed.  
Lover — Passively heals 1 HP every 5 seconds.  
Scarecrow — Hunger does not decrease. Golden apples grant \+2 additional absorption hearts.  
Zeus — Immune to lightning strike effects such as Nature’s Wrath and Lightning.   
Turkey — \+2% Dodge Chance  
Dragon Mask — \+2% damage. Immune to fire and poison damage. 

# Weapon Skins

Weapon skins apply a cosmetic model/texture and a unique passive effect to an existing weapon rather than creating a separate parallel weapon item. A weapon can have one active skin at a time. Skins should be implemented as data on the existing item so vanilla/custom enchantments, Heroic status, durability, and other item properties remain intact.

## Axe Skins

Whisk Taker (\#B08E00) — Hitting a player under 40% HP grants a Feeding Frenzy stack for 3 seconds. Having 1 or more Feeding Frenzy stacks increases outgoing damage by 5% and increases the wearer's Luck factor by 1% per stack, up to a maximum of 10%. Feeding Frenzy is a POSITIVE stack/status for the generalized stack system.

Boosted Chainsaw (\#CCA00A)— Grants Doublestrike III to the axe. Doublestrike is a sword enchantment planned for addition to the custom enchantment pool; this skin grants the equivalent level-3 effect even though the weapon is an axe.

Stormbringer (\#224B57)— Each successful hit has a 3% chance to strike lightning on the target, applying Slowness II for 1.5 seconds and dealing 2 HP true damage. While Stormbringer is being held, the wielder takes 2% less incoming damage.

## Sword Skins

Spinal Tap — Successful hits apply one Hysteria stack to the target for 4 seconds. Hysteria is a NEGATIVE stack. Each active Hysteria stack gives the affected entity a 0.5% chance per stack, whenever it lands an otherwise valid hit, to redirect that hit onto itself. On a Hysteria redirect, the original target takes zero damage and the attacker receives equal ordinary self-damage with no additional proc chain.

Maui's Hook (\#404242) — \+4% outgoing damage. On each successful hit, there is a 10% chance to steal one POSITIVE stack from the target and transfer it to the wielder. NEGATIVE stacks such as Bleed or Hysteria cannot be stolen by this effect.

Party Blade — Deal \+1% outgoing damage for each mob within 15 blocks, up to \+15%. Deal an additional \+5% outgoing damage while the wielder has any absorption hearts.

Doomsday Machete (\#162B0D) — Deal \+2% outgoing damage for each Mastery Enchantment on this weapon.

## Skin Persistence

Each skin can be dragged in the inventory and left click applied to the appropriate item. It is then tied to that specific item until removed with a simple right click. 

# Comments

Mastery \- \#AA0000  
Legendary \- \#FFAA00  
Ultimate \- \#FFFF55  
Elite \- \#A3FFF5  
Unique \- \#55FF55  
Simple \- \#AAAAAA

Each ordinary enchantment book has an independently rolled 1-100% Success Rate and 1-100% Destroy Rate, both displayed on the book. Success is resolved first; the destroy roll occurs only if the enchantment application fails. A higher-level book upgrades directly to its level. An equal-level book increases the enchantment by exactly 1 level if that would not exceed the enchantment's maximum; lower-level books are rejected. Mastery enchantments use a 1-49% Success Rate and a 51-100% Destroy Rate.

Black scrolls work on Simple-Legendary enchantments, randomly pulling one out, turning it into a book with a set success chance (like 40%, 75%, 100%, ect) and a random destroy rate 1-100%. 

White Scrolls can be applied to equipment to provide one-time protection against a destructive failed item application. This currently includes Cosmic Enchantment Books, Armor Set Crystals, Armor Enchantment Orbs, and Weapon Enchantment Orbs. Protection is consumed only when it actually prevents the item from being destroyed.

Each eligible item begins with capacity for 5 actual Cosmic enchantments; vanilla and virtual enchantments do not count toward this limit. Armor Enchantment Orbs increase an individual armor piece's capacity by \+1 per successful application, up to 8\. Weapon Enchantment Orbs increase swords, axes, bows, and crossbows by \+1 per successful application, up to 10\. Orbs are non-stackable, use an Eye of Ender presentation without enchanted glint, and each carries an independent 1-100% Success Rate and 1-100% Destroy Rate.

Armor Set Crystals are non-stackable Nether Star-based items with a permanent enchanted shimmer. They carry a 1-100% Success Rate and currently have a guaranteed destructive failure outcome if the success roll fails; White Scroll protection may prevent that destruction. Applying a crystal adds set identity to the existing armor item rather than replacing the item.

Transmog Scrolls use a Paper presentation and affect tooltip presentation only. When applied to equipment, they sort enchantments from top to bottom as: vanilla enchantments first, then Mastery, Legendary, Ultimate, Elite, Unique, and Simple. Within each Cosmic rarity, higher enchantment levels appear first; ties use a deterministic order. Future enchantments added to a Transmogged item remain automatically sorted.

For destructive item-application systems, the standard feedback language is: successful application plays the vanilla level-up sound; a failed application plays the passive lava sound; and the anvil-breaking sound is added only when the target item is actually destroyed.

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
Pile of Coal OR Christmas Stocking custom block  
3-4x Legendary Enchantment Book  
35% Permafrost Mastery Enchantment Book  
Candycane Sword Skin OR Icicle Hatchet Axe Skin

1 Extraordinary item:

Dungeon Key Ring  
Memory Chest  
Secret Weapon Cache  
Maxed out Trial Portal

Pile of Coal: Generates 1 coal every 90s, stores up to 9 stacks at a time. Hoppers/chests do not work on it.

Christmas Stocking: Increases your luck by 0.5% per stocking you’ve placed, up to \+2.5%.

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
Jumbo Popsicle Sword Skin OR Fire Axe Axe Skin

1 Extraordinary item:

Super Kit Refresher  
Memory Chest  
Secret Weapon Cache  
Maxed out Trial Portal

Potted Cactus: Generates 1 cactus every 90s, stores up to 9 stacks at a time. Hoppers/chests do not work on it.

Sand Castle: Each Sand Castle that you’ve placed gives you a \+1% chance to gain a bonus item from the Advanced Invasion loot table. Up to a maximum of \+10%. Cannot gain more than 1 bonus loot item, and it only rolls if the invasion is completely won, including the final boss.

# Space Chests

Space Chests are medium/minor loot bags that contain useful though tame loot to help the player advance through the world. Space Chests are mainly dropped via bosses, though there are alternative methods of obtaining. There are also Memory Chests, a variant of Space Chests that contain a single drop of potentially immense value. 

Space Chests come in 3 main rarities: Ultimate, Legendary, and Mastery. Each variant contains 5 items of loot from their respective loot table. 

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
| Iron Armor Piece with 1-2 max level Ultimate or lower valid enchantment | 5 | 1 |
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
| Spider Spawner | 6 | 1 |
| Zombie Spawner | 5 | 1 |
| Repair Scroll | 10 | 1 |
| Iron Armor Piece with 1-2 max level Legendary or lower valid enchantment | 5 | 1 |
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
| Iron Armor Piece with 2-3 max level Legendary or lower valid enchantment | 5 | 1 |
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