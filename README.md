# Mindustry Java Mod - Random Planet v0.1.3
A Java Mindustry mod, currently for PC only.

Randomly generate a planet and its sectors, including the starting sector.

I combined both technologies from Serpulo and Erekir as best as I could, including defenses, so some maps might even be unbeatable, especially Eradication maps.

## Features

1. 272 randomly generated sectors, no presets at all.
2. Merge both Serpulo and Erekir techtrees and environments.
3. Load *all* defensive schematics<sup>1</sup> for enemy defenses. 

## Future Plans

1. Make some new resources that utilize existing resources from both planets, such as copper-beryllium alloy, I'll try to restrain it to real-life alloys/intermetallic for the simplest reason that fictional stuff is literally limitless, I could just make up an alloy and call it "A" and be done with it, that's not how I do things. I'll also restrain from making new mineable resources as they'll inevitably occupy the space and prevent you from mining the resource "you really need".
2. Make some units and defenses that use the new resources, hopefully they could act as a "bridge" between the 2 highly unbalanced planets.
3. Make a custom wave generator that combines both Serpulo and Erekir units, yeah, you'll die!
4. Tweak Random Name Generator so it can remember the names that have already been generated, so the planet/sectors won't "change" any time you load the game, unless, of course, you imagine it as multi-universe, then everything make sense. XP

## Known Issues (Not bugs, it's just something that "make no sense")

1. Different liquids might share the same lake while they obviously can not be mixed.
2. Liquids other than slag appear in the volcanic environment while they obviously will be vaporized or set on fire (in the case of oil).
3. Chained factories (to produce higher-tier units) sometimes get stuck and are unable to finish production.

## Updates

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
- 
*v0.1.3*
- Now loading all available defensive schematics for enemy defense system

--- 

**1.** [Schematic Instruction](SchematicInstruction.md)