# Enchantments

| Name | Max Level | Goes On: | Rarity | Effect |
| :---- | :---- | :---- | :---- | :---- |

| Aegis | 6 | Chestplate | Legendary | Limits max hearts lost from base attack to 7 \- (0.5 x level) |
| :---- | :---- | :---- | :---- | :---- |
| Angelic | 5 | Any Armor | Ultimate | Gives a 1% x level chance of healing half a heart when damaged. Can stack proc chances if multiple pieces of armor have enchant, but can only proc the heal once per damage event. |
| Armored | 4 | Any Armor | Legendary | Each level is equal to half a level of protection. |
| Auto Smelt | 1 | Pickaxe | Simple | Automatically smelts drops. |
| Bleed | 6 | Axe | Ultimate | 1% chance per level to give a bleed stack to enemy for 5 seconds. Each entity can have up to 10 bleed stacks at a time which give them perma 1% less movement speed per stack and deal 1 true damage every 1.5 seconds. |
| Death Pact | 5 | Chestplate | Mastery | Deal 3% less damage but take (2% x level) less damage. |
| Divine Immolation | 4 | Sword | Mastery | 3% chance per level to proc on hit. On proc, set yourself on fire for 5 seconds and deal \+10% damage. 30 second cooldown. |
| Eagle Eye | 6 | Bow/Crossbow | Ultimate | Deal (3% x level) more damage to targets that are 18 blocks away or further. |
| Ender Shift | 3 | Helmet | Unique | Once below 25% health, gain speed 1 and regen 1 for (3 x level) seconds. 30 second cooldown.  |
| Execute | 5 | Sword | Elite | Deal (2% x level) more damage against targets that are below 50% HP. |
| Experience | 3 | Pickaxe | Simple | Gives 50% more exp from breaking blocks per level. (250% max) |
| Gears | 3 | Boots | Legendary | Gives \+10% movement speed per level. |
| Glowing | 1 | Helmet | Simple | Gives perm night vision |
| Greatsword | 4 | Sword | Elite | Deal (5% x level) more damage to enemies that are 2.5 blocks away or further. |
| Hero Killer | 3 | Axe | Mastery | Deal 3% per level more damage to enemies that have an active armor set bonus. |
| Insanity | 8 | Axe | Legendary | Deal 1% more damage per missing heart, up to a max of (2% x level) |
| Lightning | 4 | Bow/Crossbow | Simple | 5% chance per level to strike lightning on target dealing 2 true damage. |
| Luck | 10 | Boots/Leggings | Ultimate | Makes all enchantments you have proc 1% more per level. (20 total luck levels would make a 1% proc chance a 1.2% proc chance. |
| Molten | 4 | Any Armor | Unique | 1% x level chance of setting your attacker on fire for 3 seconds when hit. |
| Mortal Coil | 3 | Sword/Axe | Mastery | 2% chance to proc on hit. On proc gain 2 full hearts of absorption and your next attack does (5% x level) more damage. |
| Nutrition | 3 | Leggings | Unique | Eating a piece of food restores \+1 hunger and \+0.25 saturation per level. |
| Obsidianshield | 1 | Leggings | Ultimate | Gives perm fire resistance. |
| Oxygenate | 2 | Pickaxe | Simple | After breaking a block while underwater, refills 1 air bubble per level. |
| Pheonix | 4 | Boots | Mastery | When hit by a killing blow, heal to 40% HP and your next hit deals (10% x level) more damage. 90 second cooldown. |
| Poison | 3 | Sword | Elite | (5% x level) chance to give poison 1 for 3 seconds to target. |
| Pummel | 3 | Axe | Elite | (2% x level) chance to give slowness 3 to your target for 2.5 seconds. |
| Rage | 6 | Sword/Axe | Legendary | Deal 5% more damage to enemies that have damaged you three times or more in the last (4 \+ level) seconds. |
| Self Destruct | 3 | Leggings | Unique | When below 15% HP, summon 4 lit TNT that you are immune to. 60 second cooldown. |
| Venom | 3 | Bow/Crossbow | Elite | (15% x level) chance to give poison 1 for 3 seconds to target. |
| Virus | 3 | Bow/Crossbow | Unique | If target is poisoned, deals 0.25 true damage per level of enchant.  |
| Doublestrike | 3 | Sword | Legendary | (1% x level) chance to hit the target a second time for 50% damage and to roll all enchantments the second time as well. |

# Core Rules

# Core Rules

# Project Scope

This mod is primarily designed for singleplayer or small cooperative worlds of 2-4 players. Balance is primarily PVE-focused. Players explore a custom survival world and obtain Cosmic-style gear through normal survival, custom structures, bosses, Invasions, Dungeons, and Trials.

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

True damage is handled separately from ordinary damage and is not unintentionally modified by effects intended only for base/ordinary attack damage. Aegis caps the base/ordinary attack component and should not cap separate true-damage effects.

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
| Dimensional Traveler | \+7.5% out, \+10% movement speed. \-20% proc cooldown (pheonix, ender shift, ect) | Invasions |
| Engineer | \-25% inc, \+15% movement speed | Invasions |
| Yeti | \+10% out, \-10% inc, immune to freeze, frozen, permafrost, ice aspect. | Trials |
| Ancient | \+7.5% out, \-7.5% inc, doubled when under 50% health. | Trials |
| Ranger | \+20% bow/crossbow damage, \+20% movement speed. | Trials |

# Kits

Kits are not a routine part of this game. Instead, you unlock the kit. However, instead of using a persistent time gated cooldown, kits are refreshed by “Refresh Crystals” which allow you to claim all kits of the corresponding type.

G-kit:

Standing for God Kits, these kits contain either a weapon or an armor piece (non heroic) that has 3-5 custom enchantments on it.

Kit Variants. Unlock items are all a Diamond.

Gladiator, Viking, Paladin, Butcher, Grandmaster.

M-Kit:

Standing for Mastery Kits. These powerful kits contain already heroic armor but no weapons. Each piece of armor has exactly 3 custom enchantments. On top of this, each time you claim your M-Kit, you receive a mastery enchantment book for free.

Kit variants and unlock item textures:

Death Knight (purple dye), Necromancer (green dye), and Ghost (blue dye). 

# Invasions

Invasions are a PVE instance that occurs every 10th minecraft day. Starting 3 days out, the game automatically sends a chat message to the player telling them that the Deep Space Invasion starts in x days\! Once the 10th day hits, each player is given a custom ender pearl (that disappears after 10 minutes) that they can throw to teleport into the instance dimension and join the invasion. Like all instances, Invasions are keep inventory events.

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
| \+1 Insurance Trial Trinket | 9 | 1 |
| \+1 Minute Trial Trinket | 9 | 1 |
| 25% Dimensional Traveler Crystal | 5 | 1 |
| 25% Engineer Crystal | 5 | 1 |
| 60% blackscroll | 10 | 1 |
| 70% blackscroll | 15 | 1 |
| 80% blackscroll | 15 | 1 |
| Cow Spawner | 5 | 1 |
| Elite Enchantment Book | 25 | 1 |
| Gkit Unlock Item | 7 | 1 |
| Heroic Crystal | 5 | 1 |
| Legendary Enchantment Book | 15 | 1 |
| Mkit Unlock Item | 3 | 1 |
| Repair Scroll | 20 | 2 |
| Repair Scroll | 20 | 1 |
| Sheep Spawner | 5 | 1 |
| Skip 1 Room Trial Trinket | 9 | 1 |
| Trial Portal | 8 | 1 |
| Trial Portal | 5 | 2 |
| Ultimate Enchantment Book | 20 | 1 |
| Whitescroll | 20 | 1 |
| Zombie Spawner | 5 | 1 |

Advanced Loot Table:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| \+2 Insurance Trial Trinket | 7 | 1 |
| \+3 Insurance Trial Trinket | 4 | 1 |
| \+5 Minutes Trial Trinket | 4 | 1 |
| 100% blackscroll | 15 | 1 |
| 50% Dimensional Traveler Crystal | 5 | 1 |
| 50% Engineer Crystal | 5 | 1 |
| 75% Dimensional Traveler Crystal | 5 | 1 |
| 75% Engineer Crystal | 5 | 1 |
| 90% blackscroll | 15 | 1 |
| Armor Enchantment Orb | 8 | 1 |
| Blaze Spawner | 5 | 1 |
| Heroic Crystal | 10 | 1 |
| Iron Golem Spawner | 5 | 1 |
| Legendary Enchantment Book | 15 | 1 |
| Legendary Enchantment Book | 10 | 2 |
| Mastery Enchantment Book | 20 | 1 |
| Mkit Unlock Item | 6 | 1 |
| Repair Scroll | 10 | 2 |
| Skip 2 Rooms Trial Trinket | 7 | 1 |
| Skip 3 Rooms Trial Trinket | 4 | 1 |
| Super Kit Refresher | 8 | 1 |
| Trial Portal | 10 | 1 |
| Trial Portal | 10 | 2 |
| Weapon Enchantment Orb | 8 | 1 |
| Random Double Mask | 8 | 1 |
| Witch Spawner | 5 | 1 |
| Wither Skeleton Spawner | 5 | 1 |

Invasion Implementation and Edge Cases  
\- The 30-minute timer advances on active game/server ticks. In singleplayer, pausing the game pauses the Invasion timer.  
\- The hostile-mob generators around quadrant bosses should use controlled encounter-spawner logic rather than relying solely on ordinary vanilla spawner behavior. They may still be represented visually by spawner blocks if desired.  
\- Each boss tracks a per-player damage ledger for reward eligibility. Qualifying contribution includes direct melee damage, projectile damage, custom-enchantment damage, damage-over-time effects attributed to that player, and damage from owned/tamed/summoned entities where ownership can be determined.  
\- Each awarded Default or Advanced item is an independent weighted loot-table roll. Duplicate results are allowed unless a particular future item explicitly states otherwise.  
\- Invasion temporary buffs are personal to the player who earns them and expire when the Invasion ends or that player leaves the instance.  
\- Invasions use the shared global instance protection, return-location, persistence, and 1-4 player scaling rules from the Core Rules tab.

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
| \+3 Minutes Trial Trinket | 4 | 1 |
| 60% Blackscroll | 20 | 1 |
| 70% Blackscroll | 10 | 1 |
| Armor Enchantment Orb | 5 | 1 |
| Cow Spawner | 5 | 1 |
| Elite Enchantment Book | 20 | 1 |
| Legendary Enchantment Book | 10 | 1 |
| Repair Scroll | 10 | 1 |
| Sheep Spawner | 5 | 1 |
| Simple Enchantment Book | 30 | 1 |
| Ultimate Enchantment Book | 15 | 1 |
| Unique Enchantment Book | 25 | 1 |
| Weapon Enchantment Orb | 5 | 1 |
| Whitescroll | 10 | 1 |
| Zombie Spawner | 5 | 1 |

Hardcore Room Loot Pool:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| \+3 Minutes Trial Trinket | 12 | 1 |
| 80% Blackscroll | 10 | 1 |
| Armor Enchantment Orb | 8 | 1 |
| Gkit Refresher | 6 | 1 |
| Gkit Unlock | 2 | 1 |
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

Demonic Room Loot Pool:

| Heroic Crystal | 12 | 1 |
| :---- | :---- | :---- |
| 50% Yeti Crystal | 6 | 1 |
| 50% Ranger Crystal | 6 | 1 |
| 50% Ancient Crystal | 6 | 1 |
| Gkit Unlock | 10 | 1 |
| Iron Golem Spawner | 10 | 2 |
| Witch Spawner | 10 | 2 |
| Blaze Spawner | 10 | 2 |
| Wither Skeleton Spawner | 10 | 2 |
| Skip 3 Rooms Trial Trinket | 5 | 1 |
| \+3 Insurance Trial Trinket | 5 | 1 |
| Random Boss Spawn Egg | 8 | 1 |
| Mkit Refresher | 6 | 1 |
| Mastery Enchantment Book | 5 | 1 |
| Random Double Mask | 10 | 1 |

Trial Edge Cases and Multiplayer Rules  
\- Trial inventories are normalized by the room system. Before entry, each player's full outside inventory, armor, offhand, and required persistent player state are safely snapshotted. Trial-room items are cleared between rooms and do not carry into later rooms. The outside snapshot is restored when the player leaves the Trial.  
\- In multiplayer, the Trial pot is shared while players remain together, but each player makes an individual cash-out decision in the Decision Box. A player who accepts the deal receives the current pot and leaves permanently; remaining players may continue.  
\- If an individual player dies, that player is removed from the Trial and receives no pot loot except anything salvaged by their Insurance Trinket. Other surviving players may continue.  
\- If the shared Trial timer reaches zero, all remaining players fail and are removed. Insurance applies individually.  
\- The timer remains paused during Decision Box periods.  
\- Disconnects, crashes, or world closure must never destroy the player's outside inventory snapshot or return-location data. The implementation should persist enough Trial state to recover the player safely rather than duplicate or delete items.  
\- Trial-specific exceptions to normal instance protection are defined per room. For example, Bomb Squad may allow only its intended Creeper explosions to destroy designated walls.

# Dungeons

Dungeons are an instance where teams of players can go and fight a predetermined series of challenges in a set order. All surviving players who beat the final boss without dying or running out of time earn a dungeon lootbag with valuable loot.

Basic rules:

No respawn. No breaking blocks unless necessary for puzzle. Keep inventory enabled.

Each Dungeon has a set time limit. Failure to complete the dungeon within the time limit results in automatic expulsion from the dungeon without a lootbag.

Each Dungeon portal can be placed anywhere outside the instance dimension, and up to 4 players can go inside. After 30s, the portal closes, and the timer begins. Players who die in the dungeon are immediately sent back to where they entered the portal.

A heroic crystal can be applied to a dungeon portal to create the heroic version of the dungeon. In this version, the time limit is decreased by 50%, the enemies deal 25% more damage, and golden apples and ender pearls are disabled. 

Each dungeon lootbag contains 3 items, with one of them guaranteed to be a custom block. Heroic dungeon lootbags contain 4 items, and their pools are expanded to add additional valuable loot.

Dungeon \#1–The Abandoned Spaceship order of challenges:

Boss fight vs Yjiki. Boss alternates between taking axe and sword damage while shooting permanent holes into the ground that kill you if you fall through. 

Parkour through Air Lock.

Maze with white/yellow/orange/red wool floor that weakens a stage every time you step on it.

Mother of Yjiki fight. More HP and more damage. Charges every 20-30 seconds and kills you instantly if you aren’t behind the divider walls.

Cargo Bay Parkour

Teleportation Room Puzzle (input 8 colors in order while fighting off Space Pirates)

Abandoned Spaceship Lootbag:

| Loot Item | Weight | Quantity |
| :---- | :---- | :---- |
| Legendary Enchantment Book | 10 | 2 |
| Gkit Unlock | 8 | 1 |
| 40% Phantom Armor Crystal | 10 | 1 |
| 40% Yjiki Armor Crystal | 10 | 1 |
| Heroic Crystal | 8 | 1 |
| White Scroll | 12 | 1 |
| Trial Portal | 10 | 1 |
| Random Boss Spawn Egg | 15 | 1 |
| Random Mask | 10 | 1 |
|  |  |  |
|  |  |  |
|  |  |  |

Heroic Abandoned Spaceship Lootbag:

| 80% Yjiki Armor Crystal | 10 | 1 |
| :---- | :---- | :---- |
| 80% Phantom Armor Crystal | 10 | 1 |
| Trial Portal | 10 | 2 |
| Gkit Gem | 12 | 1 |
| Mkit Unlock | 6 | 1 |
|  |  |  |
|  |  |  |
|  |  |  |
|  |  |  |
|  |  |  |

# Misc Items

Repair Scroll \- can be applied to any item to refill durability up to maximum. Item texture is paper.

Heroic Crystal \- Can be applied one time maximum to dungeon portals, pickaxes and shovels, and armor. When applied to armor, it turns the model into leather and gives \+250 maximum durability. Pickaxes and shovels turn to a gold texture and gain \+250 maximum durability. If an eligible item is already damaged when the crystal is applied, both its current durability and maximum durability increase by 250\. When applied to dungeon portals, it turns them into their Heroic variants. Item texture is an amethyst shard.

Kit Refreshers- Consume to renew all of your available kits, allowing you to claim them.Comes in “Mkit”, “Gkit”, or “Super” variants, which allow you to refresh your GKits, Mkits, or both respectively. Item texture is the item quartz. 

# Masks / Skins

# Masks / Skins

# Masks

Masks are an additional customization and buildcraft layer. Most masks are found individually. A player may use a dedicated custom NPC or an appropriate combination item/mechanic to combine masks into Multi-Masks containing up to 3 different individual masks. Duplicate copies of the same mask cannot occupy multiple slots in one Multi-Mask.

Masks should attach to or be associated with the player's helmet rather than replacing the armor-set helmet slot. A Multi-Mask provides the effects of all masks contained within it. One For rendering, multi masks use a black box with a question mark on the front.  The contained masks still provide their effects without requiring the visuals to be physically layered on the player's face.

## Mask List

Santa — \+1 maximum heart.  
Reindeer — \+3% movement speed.  
Purge — \+3% outgoing damage.  
Party — \+1% outgoing damage, \-1% incoming damage, and \+1% movement speed.  
Lover — Passively heals 1 HP every 5 seconds.  
Scarecrow — Hunger does not decrease. Golden apples grant \+2 additional absorption hearts.

# Weapon Skins

Weapon skins apply a cosmetic model/texture and a unique passive effect to an existing weapon rather than creating a separate parallel weapon item. A weapon can have one active skin at a time. Skins should be implemented as data on the existing item so vanilla/custom enchantments, Heroic status, durability, and other item properties remain intact.

## Axe Skins

Whisk Taker — Hitting a player under 40% HP grants a Feeding Frenzy stack for 3 seconds. Feeding Frenzy increases outgoing damage by 5% and increases the wearer's Luck factor by 1% per stack, up to a maximum of 10%. Feeding Frenzy is a POSITIVE stack/status for the generalized stack system.

Boosted Chainsaw — Grants Doublestrike III to the axe. Doublestrike is a sword enchantment planned for addition to the custom enchantment pool; this skin grants the equivalent level-3 effect even though the weapon is an axe.

Stormbringer — Each successful hit has a 3% chance to strike lightning on the target, applying Slowness II for 1.5 seconds and dealing 2 HP true damage. While Stormbringer is being held, the wielder takes 2% less incoming damage.

## Sword Skins

Spinal Tap — Successful hits apply one Hysteria stack to the target for 4 seconds. Hysteria is a NEGATIVE stack. Each active Hysteria stack gives the affected entity a 0.5% chance per stack, whenever it lands an otherwise valid hit, to damage itself instead of its intended target.

Maui's Hook — \+4% outgoing damage. On each successful hit, there is a 10% chance to steal one POSITIVE stack from the target and transfer it to the wielder. NEGATIVE stacks such as Bleed or Hysteria cannot be stolen by this effect.

Party Blade — Deal \+1% outgoing damage for each mob within 15 blocks, up to \+15%. Deal an additional \+5% outgoing damage while the wielder has any absorption hearts.

## Skin Persistence

Each skin can be dragged in the inventory and right click applied to the appropriate item. It is then tied to that specific item until removed with a simple left click. 

# Comments

Mastery \- \#AA0000  
Legendary \- \#FFAA00  
Ultimate \- \#FFFF55  
Elite \- \#00AAAA  
Unique \- \#55FF55  
Simple \- \#AAAAAA

Each enchant book has a random 1-100% chance to be successful, and if it is not successful, than a 1-100% chance to destroy the item (both chances are displayed on the book). Mastery enchants have a 1-49% chance to succeed, and a 51-100% chance to destroy (so bottom half of distribution for both outcomes). 

Black scrolls work on Simple-Legendary enchantments, randomly pulling one out, turning it into a book with a set success chance (like 40%, 75%, 100%, ect) and a random destroy rate 1-100%. 

White scrolls can be applied to an item to give it a 1 time protection against a book/orb that would otherwise destroy an item.

Each item can be enchanted up to 5 times with custom enchantments. Armor and weapon enchantment orbs can be applied to items to subsequently increase the limit by 1\. Each armor and weapon orb has a 1-100% success rate, and a 1-100% destroy rate.

