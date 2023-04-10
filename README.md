# Mindustry Java Mod - Random Planet v0.2.7.1
A Java Mindustry mod.

Randomly generate a planet and its sectors, including the starting sector.

I combined both technologies from Serpulo and Erekir as best as I could, including defenses, so some maps might even be unbeatable, especially Eradication maps.

## Features

1. 272 randomly generated sectors, no presets at all.
2. Merge both Serpulo and Erekir techtrees and environments.
3. Load *all* defensive schematics<sup>1</sup> for enemy defenses. 
4. 95 custom defensive schematics for the enemies to use as defenses (2 cores, 43 turrets, 50 factories)
4. Custom settings under *Settings* menu.
5. Custom blocks and items that add a little spice to the game, it'll be constantly added.
6. Unlock hidden items<sup>3</sup> and enable all environments for all units
7. Load all activated ores for ore placements.

## Future Plans

1. Make some new resources that utilize existing resources from both planets, such as copper-beryllium alloy, I'll try to restrain it to real-life alloys/intermetallic for the simplest reason that fictional stuff is literally limitless, I could just make up an alloy and call it "A" and be done with it, that's not how I do things. I'll also restrain from making new mineable resources as they'll inevitably occupy the space and prevent you from mining the resource "you really need".
2. Make some units and defenses that use the new resources, hopefully they could act as a "bridge" between the 2 highly unbalanced planets.
3. Make a custom wave generator that combines both Serpulo and Erekir units, yeah, you'll die!

## Known Issues (Not bugs, it's just something that "make no sense")

1. Liquids other than slag appear in the volcanic environment while they obviously will be vaporized or set on fire (in the case of oil).
2. Chained factories (to produce higher-tier units) sometimes get stuck and are unable to finish production.

## Changelog

*v0.1.0322.1*
- Fix many sector generator abnormalities

*v0.1.1*
- Change versioning for easier readability
- Fix: Starting sector got flooded by waters

*v0.1.2*
- Fix: Only Low and Eradication difficulties
- Change: Random planet color
- Fix: Endless waves for Low difficulty
- Fix: Sector difficulties are not really randomized

*v0.1.3*
- Now loading all available defensive schematics for enemy defense system

*v0.2*
- Fix: Different liquids share the same lake while they obviously can not be mixed
- Add custom settings under *Settings* menu
- Fix: Random Name Generator sometimes crashs the game
- Add:
  - Cryogenic Freezer: Use cryofluid to freeze most of the liquids
  - Hyper Thawer: User slag to melt most of the items created from Cryogenic Freezer
  - Solid form (ice) of Water, Cryofluid, Oil, Slag, Arkycite, Neoplasm
- Add Discord Server<sup>2</sup>
- Officially released, now can be installed from the Mod Browser

*v0.2.1*
- Fix: Random Name Generator bugs
- Mobile support, FINALLY!

*v0.2.2*
- Sorry I forgot to add the new contents to the tecttree and restore the initial resources for the starting sector

*v0.2.3*
- Move schematics I made to "buid-in", you won't see it in the schematics menu anymore, because I have to say, it's freaking annoying!
- Remove any unit environmental restrictions, now every unit can survive in any environment

*v0.2.4*
- Load all available ores from all activated mods for ore placements in random map generations
- Set minimum resources in any map to 5, 2 of which is Copper and Lead

*v0.2.5*
- Remove factory schematics from the starting sector

*v0.2.6*
- Save sector threats so it won't change any time you load the game

*v0.2.7*
- Change sector available ores to according to the *Build Cost* of the item to be mined<sup>4</sup>
- Fix the sector naming and threat generating problem
- Fix enemy ground units getting trapped in the air, hopefully
- Add Planet Size and Map Size to settings
- 
*v0.2.7.1*
- When I change the random map generation algorithm, I forgot to add the banned blocks back, sorry

--- 

**1.** [Schematic Instruction](SchematicInstruction.md)

**2.** [Discord Server](https://discord.gg/WSmUApPfpj)

**3.** Caution: There is a reason why Anuke hide these blocks/items, most likely it's still WIP and not fully tested and might not be balanced, use it at your own risk

**4.** The formula for calculating the chance of appearance for each ore is **Max(0.01,(2-*Build Cost*)/2)** (except Copper and Lead, which is always 100%). For example, the *Build Cost* of Thorium is 110%, so the chance for Thorium to appear in a new sector is (2-1.1)/2 = 0.45 (45%), please visit the *Items* section on [Mindustry Wiki](https://mindustrygame.github.io/wiki/) for reference