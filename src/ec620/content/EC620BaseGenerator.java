package ec620.content;

import arc.func.Cons;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.ai.Astar;
import mindustry.ai.BaseRegistry;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.maps.generators.BaseGenerator;
import mindustry.type.Item;
import mindustry.type.Sector;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.payloads.PayloadBlock;
import mindustry.world.blocks.payloads.PayloadConveyor;

import static mindustry.Vars.*;
import static mindustry.Vars.state;

public class EC620BaseGenerator extends BaseGenerator
{
    private Tiles tiles;
    private Seq<Tile> cores;
    private static final int range = 160;

    public void generate(Tiles tiles, Seq<Tile> cores, Tile spawn, Team team, Sector sector, float difficulty)
    {
        this.tiles = tiles;
        this.cores = cores;

        //don't generate bases when there are no loaded schematics
        if(bases.cores.isEmpty()) return;

        Mathf.rand.setSeed(sector.id);

        float bracketRange = 0.17f;
        float baseChance = Mathf.lerp(0.7f, 2.1f, difficulty);
        int wallAngle = 70; //180 for full coverage
        double resourceChance = 0.5 * baseChance;
        double nonResourceChance = 0.0005 * baseChance;
        BaseRegistry.BasePart coreschem = bases.cores.getFrac(difficulty);
        int passes = difficulty < 0.4 ? 1 : difficulty < 0.8 ? 2 : 3;

        Block wall = getDifficultyWall(1, difficulty), wallLarge = getDifficultyWall(2, difficulty);

        for(Tile tile : cores)
        {
            tile.clearOverlay();
            //Schematics.placeLoadout(, tile.x, tile.y, team, false);

            //fill core with every type of item (even non-material)
            Building entity = tile.build;
            for(Item item : content.items()){
                entity.items.add(item, entity.block.itemCapacity);
            }
        }

        for(int i = 0; i < passes; i++){
            //random schematics
            pass(tile -> {
                if(!tile.block().alwaysReplace) return;

                if(((tile.overlay().asFloor().itemDrop != null || (tile.drop() != null && Mathf.chance(nonResourceChance)))
                        || (tile.floor().liquidDrop != null && Mathf.chance(nonResourceChance * 2))) && Mathf.chance(resourceChance)){
                    Seq<BaseRegistry.BasePart> parts = bases.forResource(tile.drop() != null ? tile.drop() : tile.floor().liquidDrop);
                    if(!parts.isEmpty()){
                        tryPlace(parts.getFrac(difficulty + Mathf.range(bracketRange)), tile.x, tile.y, team);
                    }
                }else if(Mathf.chance(nonResourceChance)){
                    tryPlace(bases.parts.getFrac(difficulty + Mathf.range(bracketRange)), tile.x, tile.y, team);
                }
            });
        }

        //replace walls with the correct type (disabled)
        if(false)
            pass(tile -> {
                if(tile.block() instanceof Wall && tile.team() == team && tile.block() != wall && tile.block() != wallLarge){
                    tile.setBlock(tile.block().size == 2 ? wallLarge : wall, team);
                }
            });

        if(wallAngle > 0){

            //small walls
            pass(tile -> {

                if(tile.block().alwaysReplace){
                    boolean any = false;

                    for(Point2 p : Geometry.d4){
                        Tile o = tiles.get(tile.x + p.x, tile.y + p.y);

                        //do not block payloads
                        if(o != null && (o.block() instanceof PayloadConveyor || o.block() instanceof PayloadBlock)){
                            return;
                        }
                    }

                    for(Point2 p : Geometry.d8){
                        if(Angles.angleDist(Angles.angle(p.x, p.y), spawn.angleTo(tile)) > wallAngle){
                            continue;
                        }

                        Tile o = tiles.get(tile.x + p.x, tile.y + p.y);
                        if(o != null && o.team() == team && !(o.block() instanceof Wall)){
                            any = true;
                            break;
                        }
                    }

                    if(any){
                        tile.setBlock(wall, team);
                    }
                }
            });

            //large walls
            pass(curr -> {
                int walls = 0;
                for(int cx = 0; cx < 2; cx++){
                    for(int cy = 0; cy < 2; cy++){
                        Tile tile = tiles.get(curr.x + cx, curr.y + cy);
                        if(tile == null || tile.block().size != 1 || (tile.block() != wall && !tile.block().alwaysReplace)) return;

                        if(tile.block() == wall){
                            walls ++;
                        }
                    }
                }

                if(walls >= 3){
                    curr.setBlock(wallLarge, team);
                }
            });
        }

        //clear path for ground units
        for(Tile tile : cores){
            Astar.pathfind(tile, spawn, t -> t.team() == state.rules.waveTeam && !t.within(tile, 25f * 8) ? 100000 : t.floor().hasSurface() ? 1 : 10, t -> !t.block().isStatic()).each(t -> {
                if(!t.within(tile, 25f * 8)){
                    if(t.team() == state.rules.waveTeam){
                        t.setBlock(Blocks.air);
                    }

                    for(Point2 p : Geometry.d8){
                        Tile other = t.nearby(p);
                        if(other != null && other.team() == state.rules.waveTeam){
                            other.setBlock(Blocks.air);
                        }
                    }
                }
            });
        }
    }
    void pass(Cons<Tile> cons){
        Tile core = cores.first();
        core.circle(range, (x, y) -> cons.get(tiles.getn(x, y)));
    }
}
