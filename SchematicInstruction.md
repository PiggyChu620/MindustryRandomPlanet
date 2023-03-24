# Schematic Instruction
My mod will search for all available schematics, including your own schematics, to use as enemy defenses.

There are 3 types of schematics that will be loaded:
1. Core schematics: Any schematic with a core in it.
2. Turret schematics: Any schematic with turret(s) in it. The system will loop through the ammo types of each turret and make copies for each mineable resource (except graphite, which is the only item that could be made with a single resource - coal, so if you're expecting to make a defense schematic that uses graphite as the ammo, please remember to add Graphite Press in it so it could operate correctly). The system will also replace walls according to the difficulty of the sector, so all you need to do is surround it with Copper Walls and let the system take over.
3. Factory schematics: Any schematic with factory(ies) in it.

**★ Sandbox-only items/blocks are enabled, so you could make all kind of cheating schematics for the enemy to use.**