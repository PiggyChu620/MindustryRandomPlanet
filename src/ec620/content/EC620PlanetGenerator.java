package ec620.content;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.ai.*;
import mindustry.content.*;
import mindustry.ctype.Content;
import mindustry.game.*;
import mindustry.graphics.g3d.*;
import mindustry.maps.generators.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.storage.CoreBlock;

import java.lang.reflect.Field;
import java.lang.Class;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mindustry.Vars.*;
import static mindustry.content.Blocks.*;

import static ec620.content.EC620Classes.*;
/**
 * Planet generator thingy.
 * @author Anuke(n)
 * todo: remove ruin generation
 * todo: new weathers
 * todo: replace tiles and remove atmosphere
 */
public class EC620PlanetGenerator extends PlanetGenerator
{
    //alternate, less direct generation (wip)
    public static boolean alt = false;
    static final int seed = (int)(System.currentTimeMillis()%Integer.MAX_VALUE);
    final Color borudaMidColor = new Color();

    BaseGenerator basegen = new BaseGenerator();
    float scl = 2.7f;
    float waterOffset = 0.07f;
    boolean genLakes = false;
    private boolean customCoreBasePartsAdded=false;
    private Rand pgRand=new Rand(seed);

    Seq<Block> liquids=Seq.with(Blocks.water,Blocks.tar,Blocks.slag, Blocks.cryofluid, Blocks.arkyciteFloor);
    //^ EQUATOR
    //                  <- LOW                   HIGH ->
    Block[][] arr =
            {
                    //{EC620Blocks.oreIron}
            };
    //v POLES

    //todo (how many todos are even here?
    /*ObjectMap<Block, Block> dec = ObjectMap.of(
            Blocks.sporeMoss, Blocks.sporeCluster,
            Blocks.moss, Blocks.sporeCluster
    );

    ObjectMap<Block, Block> tars = ObjectMap.of(
            Blocks.sporeMoss, Blocks.shale,
            Blocks.moss, Blocks.shale
    );

    ObjectMap<Block, Block> toMoss = ObjectMap.of(
            borudalite, twilightMoss,
            starryBorudaliteWater, starryMossWater
    );

    ObjectMap<Block, Block> toShallowMoss = ObjectMap.of(
            borudalite, mossyBorudalite,
            starryBorudaliteWater, starryMossWater
    );

    ObjectSet<Block> shallowBois = ObjectSet.with(
            starrySandWater, starryMossWater, starryBorudaliteWater
    );*/

    float water = 2f; // / arr[0].length;

    Vec3[] craters = new Vec3[4];
    float[] craterSize = new float[craters.length];
    void initCraters(){
        if(craters[0] != null) return;
        for(int i = 0; i < craters.length; i++){
            rand.setSeed(seed + i + 66);
            craters[i] = new Vec3(scl, 0, 0).setToRandomDirection(rand).setLength2(scl * scl);
            craterSize[i] = rand.nextFloat() * 0.15f + 0.03f;
        }
    }

    float rawHeight(Vec3 position){
        position = Tmp.v33.set(position).scl(scl);
        float tnoise = riverNoise(position);
        boolean isRiver = tnoise > 0.49f && tnoise < 0.65f;

        //do not touch tnoise here, tweak the riverNoise() instead
        float simp = isRiver ? 0f : Simplex.noise3d(seed, 5, 0.5f, 1f/8f, position.x, position.y, position.z) + craterNoise(position, true) * 1.4f;
        return (simp * 0.7f + waterOffset) / (1f + waterOffset);
    }

    @Override
    public void generateSector(Sector sector){

        //these always have bases
        if(sector.id == 0)
        {
            sector.generateEnemyBase = true;
//            ItemSeq is=new ItemSeq();
//
//            is.add(Items.copper,100);
//            sector.info.items=is;
            //return;
        }
        //else sector.generateEnemyBase=rand.chance(.5);
        sector.setName(EC620Name.generate()+"-"+sector.id);
        PlanetGrid.Ptile tile = sector.tile;

        boolean any = false;
        float poles = Math.abs(tile.v.y);
        float noise = Noise.snoise3(tile.v.x, tile.v.y, tile.v.z, 0.001f, 0.58f);

        if(noise + poles/7.1 > 0.12 && poles > 0.23){
            any = true;
        }

        if(noise < 0.16){
            for(PlanetGrid.Ptile other : tile.tiles){
                var osec = sector.planet.getSector(other);

                //no sectors near start sector!
                if(
                        osec.id == sector.planet.startSector || //near starting sector
                                osec.generateEnemyBase && poles < 0.85 || //near other base
                                (sector.preset != null && noise < 0.11) //near preset
                ){
                    return;
                }
            }
        }

        sector.generateEnemyBase = sector.id==0 || any;
    }

    @Override
    public float getHeight(Vec3 position){
        float height = rawHeight(position);
        return Math.max(water, height - 0.3f); //a bit more than the max crater noise is subtracted
        //return waterOffset / (1f + waterOffset); //eh fuck it
    }

    @Override
    public Color getColor(Vec3 position){
        Block block = getBlock(position);
        float tnoise = 1f;
        //float tnoise = Simplex.noise3d(seed, 8, 0.56, 1f/16f, position.x, position.y + 9999f, position.z);
        //tnoise = (1f - Mathf.clamp(tnoise)) * 0.3f + 0.7f;

        //if(block == mossyBorudalite) return Tmp.c1.set(borudaMidColor.set(borudalite.mapColor).lerp(twilightMoss.mapColor, 0.5f)).mul(tnoise).a(1f);
        return Tmp.c1.set(block.mapColor).mul(tnoise).a(1f - block.albedo);
    }

    //@Override
    //public boolean skip(Vec3 position){
    //    return Simplex.noise3d(seed, 8, 0.56, 1f/16f, position.x, position.y + 999f, position.z) > 0.72f; //test
    //}

    @Override
    public void genTile(Vec3 position, TileGen tile){
        tile.floor = getBlock(position);
        if(!generatedFloors.contains(tile.floor.name)) generatedFloors.add(tile.floor.name);
        try
        {
            tile.block = tile.floor.asFloor().wall;

            if (Ridged.noise3d(1, position.x, position.y, position.z, 2, 22) > 0.31) {
                tile.block = Blocks.air;
            }
        }
        catch(Exception e)
        {
            tile.block=Blocks.water;
        }
    }

    float riverNoise(Vec3 position){
        return Simplex.noise3d(seed, 7, 0.56, .1f, position.x, position.y + 999f, position.z) * 0.76f;
    }

    float craterNoise(Vec3 position, boolean smooth){
        initCraters();
        float d = scl * scl * 4f, s = 0.3f;
        for(int i = 0; i < craters.length; i++){
            float dt = craters[i].dst2(position);
            if(dt < d){
                d = dt;
                s = craterSize[i];
            }
        }

        d /= scl * scl; //d = [0 ~ 2]
        if(d <= s){
            //inside crater
            if(smooth) return d / s * 1.3f -0.7f;
            return -0.7f;
        }
        if(d >= s + 0.15f) return 0.2f; //not a crater
        float a = (d - s) / 0.15f;
        //outside crater
        return (1f - Mathf.sqrt(a)) * 0.38f + 0.22f;
    }
    /*ArrayList<Block> tempBlocks=new ArrayList<>(List.of(
            deepwater,
            Blocks.water,
            taintedWater,
            deepTaintedWater,
            darksandTaintedWater,
            sandWater,
            darksandWater,
            tar,
            cryofluid,
            slag,
            basalt,
            hotrock,
            magmarock,
            sand,
            darksand,
            dirt,
            mud,
            dacite,
            rhyolite,
            rhyoliteCrater,
            roughRhyolite,
            regolith,
            yellowStone,
            carbonStone,
            ferricStone,
            ferricCraters,
            beryllicStone,
            crystallineStone,
            crystalFloor,
            yellowStonePlates,
            redStone,
            denseRedStone,
            redIce,
            arkyciteFloor,
            arkyicStone,
            redmat,
            bluemat,
            grass,
            salt,
            snow,
            ice,
            iceSnow,
            shale,
            moss,
            sporeMoss,

            ));*/
    Seq<Floor> availableBlocks=null;
    Seq<SteamVent> availableVents=null;   //new ArrayList<>(List.of(rhyoliteVent,carbonVent,arkyicVent,yellowStoneVent,redStoneVent,crystallineVent,));
    Seq<String> generatedFloors=new Seq<>();
    Block getBlock(Vec3 position)
    {
        //todo refill poles with milksand, try reviving the arr usage
        float height = rawHeight(position);
        Tmp.v31.set(position);
        position = Tmp.v33.set(position).scl(scl);
        float rad = scl;
        float temp = Mathf.clamp(Math.abs(position.y * 2f) / (rad));
        float tnoise = riverNoise(position);
        float cnoise = craterNoise(position, false);
        //temp = Mathf.lerp(temp, tnoise, 0.5f);
        //height *= 1.2f;
        //height = Mathf.clamp(height);

        //float tar = Simplex.noise3d(seed, 4, 0.55f, 1f/2f, position.x, position.y + 999f, position.z) * 0.3f + Tmp.v31.dst(0, 0, 1f) * 0.2f;

        //Block res = arr[Mathf.clamp((int)(temp * arr.length), 0, arr[0].length - 1)][Mathf.clamp((int)(height * arr[0].length), 0, arr[0].length - 1)];

        //do not touch tnoise here, tweak the riverNoise() instead
        //Block res = (tnoise > 0.52f) ? (tnoise > 0.53f ? (tnoise > 0.57f ? (tnoise > 0.58f ? milksand : starrySandWater) : starryWater) : starryBorudaliteWater) : borudalite;
        if(availableBlocks==null)
        {
            Seq<Floor> tempBlocks=new Seq();
            availableBlocks = new Seq<>();
            availableVents=new Seq<>();
            for (Field f : Blocks.class.getDeclaredFields())
            {
                f.setAccessible(true);
                Object fv;
                try
                {
                    fv = f.get(Blocks.class);
                }
                catch (IllegalAccessException e)
                {
                    continue;
                }
                try
                {
                    if(fv instanceof SteamVent) availableVents.add((SteamVent)fv);
                    else if (fv instanceof Floor &&
                            fv != air &&
                            fv != empty &&
                            fv != coreZone &&
                            fv != spawn &&
                            fv != space &&
                            !((Floor) fv).name.contains("panel") &&
                            !(fv instanceof OreBlock) &&
                            !(fv instanceof OverlayFloor) &&
                            !((Floor)fv).isLiquid) tempBlocks.add((Floor) fv);
                }
                catch (Exception e)
                {

                }
            }
            while(tempBlocks.size>0)
            {
                int c=pgRand.nextInt(tempBlocks.size);

                availableBlocks.add(tempBlocks.get(c));
                tempBlocks.remove(c);
            }
        }
        if(pgRand.chance(.01)) return availableVents.getFrac(pgRand.nextFloat());
        Block res;
        if(sector.id==0)
        {
            Seq<Block> sands=Seq.with( sand, darksand);
            return sands.get((int)(tnoise*sands.size)%sands.size);
        }
        else res= availableBlocks.get((int)(tnoise*availableBlocks.size)%availableBlocks.size);
        //res=Blocks.sand;
        float moss = Ridged.noise3d(seed, position.x, position.y, position.z, 8, 0.29f);//freq = 1 / scl?
        if(cnoise < 0f || cnoise > 0.4f) return res; //inside of crater is safe from moss
        else if(cnoise > 0.2f) moss -= cnoise * 1.4f;
        if(moss > 0.45f){
            //return toMoss.get(res, res);
            return Blocks.sporeMoss;
        }
        else if(moss > 0.21f){
            //return toShallowMoss.get(res, res);
            return Blocks.moss;
        }
        return res;
    }
    private int s=0;
    private Block randomBlock(Block[] blocks)
    {
        return blocks[new Rand(s++).nextInt(blocks.length)];
    }
    private Block randomBlock(ArrayList blocks){return (Block)blocks.get(new Rand(s++).nextInt(blocks.size()));}
    @Override
    protected float noise(float x, float y, double octaves, double falloff, double scl, double mag){
        Vec3 v = sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z) * (float)mag;
    }

    @Override
    protected void generate()
    {

        class Room
        {
            int x, y, radius;
            ObjectSet<Room> connected = new ObjectSet<>();

            Room(int x, int y, int radius)
            {
                this.x = x;
                this.y = y;
                this.radius = radius;
                connected.add(this);
            }

            void join(int x1, int y1, int x2, int y2)
            {
                float nscl = pgRand.random(100f, 140f) * 6f;
                int stroke = pgRand.random(3, 9);
                brush(pathfind(x1, y1, x2, y2, tile -> (tile.solid() ? 50f : 0f) + noise(tile.x, tile.y, 2, 0.4f, 1f / nscl) * 500, Astar.manhattan), stroke);
            }

            void connect(Room to)
            {
                if(!connected.add(to) || to == this) return;

                Vec2 midpoint = Tmp.v1.set(to.x, to.y).add(x, y).scl(0.5f);
                pgRand.nextFloat();

                if(alt)
                {
                    midpoint.add(Tmp.v2.set(1, 0f).setAngle(Angles.angle(to.x, to.y, x, y) + 90f * (rand.chance(0.5) ? 1f : -1f)).scl(Tmp.v1.dst(x, y) * 2f));
                }
                else
                {
                    //add randomized offset to avoid straight lines
                    midpoint.add(Tmp.v2.setToRandomDirection(pgRand).scl(Tmp.v1.dst(x, y)));
                }

                midpoint.sub(width/2f, height/2f).limit(width / 2f / Mathf.sqrt3).add(width/2f, height/2f);

                int mx = (int)midpoint.x, my = (int)midpoint.y;

                join(x, y, mx, my);
                join(mx, my, to.x, to.y);
            }

            void joinLiquid(int x1, int y1, int x2, int y2)
            {
                float nscl = pgRand.random(100f, 140f) * 6f;
                int rad = pgRand.random(5, 10);
                int avoid = 2 + rad;
                var path = pathfind(x1, y1, x2, y2, tile -> (tile.solid() || !tile.floor().isLiquid ? 70f : 0f) + noise(tile.x, tile.y, 2, 0.4f, 1f / nscl) * 500, Astar.manhattan);
                path.each(t -> {
                    //don't place liquid paths near the core
                    if(Mathf.dst2(t.x, t.y, x2, y2) <= avoid * avoid)
                    {
                        return;
                    }

                    for(int x = -rad; x <= rad; x++)
                    {
                        for(int y = -rad; y <= rad; y++)
                        {
                            int wx = t.x + x, wy = t.y + y;
                            if(Structs.inBounds(wx, wy, width, height) && Mathf.within(x, y, rad))
                            {
                                Tile other = tiles.getn(wx, wy);
                                other.setBlock(Blocks.air);
                                if(Mathf.within(x, y, rad - 1) && !other.floor().isLiquid)
                                {
                                    Floor floor = other.floor();
                                    //TODO does not respect tainted floors
                                    other.setFloor((Floor)(floor == Blocks.sand || floor == Blocks.salt ? Blocks.sandWater : Blocks.darksandTaintedWater));
                                }
                            }
                        }
                    }
                });
            }

            void connectLiquid(Room to)
            {
                if(to == this) return;

                Vec2 midpoint = Tmp.v1.set(to.x, to.y).add(x, y).scl(0.5f);
                pgRand.nextFloat();

                //add randomized offset to avoid straight lines
                midpoint.add(Tmp.v2.setToRandomDirection(pgRand).scl(Tmp.v1.dst(x, y)));
                midpoint.sub(width/2f, height/2f).limit(width / 2f / Mathf.sqrt3).add(width/2f, height/2f);

                int mx = (int)midpoint.x, my = (int)midpoint.y;

                joinLiquid(x, y, mx, my);
                joinLiquid(mx, my, to.x, to.y);
            }
        }

        cells(4);
        distort(10f, 12f);

        float constraint = 1.3f;
        float radius = width / 2f / Mathf.sqrt3;
        int rooms = pgRand.random(2, 5);
        Seq<Room> roomseq = new Seq<>();


        for(int i = 0; i < rooms; i++)
        {
            Tmp.v1.trns(pgRand.random(360f), pgRand.random(radius / constraint));
            float rx = (width/2f + Tmp.v1.x);
            float ry = (height/2f + Tmp.v1.y);
            float maxrad = radius - Tmp.v1.len();
            float rrad = Math.min(pgRand.random(9f, maxrad / 2f), 30f);
            roomseq.add(new Room((int)rx, (int)ry, (int)rrad));
        }

        //check positions on the map to place the player spawn. this needs to be in the corner of the map
        Room spawn = null;
        Seq<Room> enemies = new Seq<>();
        int enemySpawns = pgRand.random(1, Math.max((int)(sector.threat * 4), 1));
        int offset = pgRand.nextInt(360);
        float length = width/2.55f - pgRand.random(13, 23);
        int angleStep = 5;
        int waterCheckRad = 5;
        for(int i = 0; i < 360; i+= angleStep)
        {
            int angle = offset + i;
            int cx = (int)(width/2 + Angles.trnsx(angle, length));
            int cy = (int)(height/2 + Angles.trnsy(angle, length));

            int waterTiles = 0;

            //check for water presence
            for(int rx = -waterCheckRad; rx <= waterCheckRad; rx++)
            {
                for(int ry = -waterCheckRad; ry <= waterCheckRad; ry++)
                {
                    Tile tile = tiles.get(cx + rx, cy + ry);
                    if(tile == null || tile.floor().liquidDrop != null)
                    {
                        waterTiles ++;
                    }
                }
            }

            if(waterTiles <= 4 || (i + angleStep >= 360))
            {
                roomseq.add(spawn = new Room(cx, cy, pgRand.random(8, 15)));

                for(int j = 0; j < enemySpawns; j++)
                {
                    float enemyOffset = pgRand.range(60f);
                    Tmp.v1.set(cx - width/2, cy - height/2).rotate(180f + enemyOffset).add(width/2, height/2);
                    Room espawn = new Room((int)Tmp.v1.x, (int)Tmp.v1.y, pgRand.random(8, 16));
                    roomseq.add(espawn);
                    enemies.add(espawn);
                }

                break;
            }
        }

        //clear radius around each room
        for(Room room : roomseq)
        {
            erase(room.x, room.y, room.radius);
        }

        //randomly connect rooms together
        int connections = pgRand.random(Math.max(rooms - 1, 1), rooms + 3);
        for(int i = 0; i < connections; i++)
        {
            roomseq.random(pgRand).connect(roomseq.random(pgRand));
        }

        for(Room room : roomseq)
        {
            spawn.connect(room);
        }

        Room fspawn = spawn;

        cells(1);

        int tlen = tiles.width * tiles.height;
        int total = 0, waters = 0;

        for(int i = 0; i < tlen; i++)
        {
            Tile tile = tiles.geti(i);
            if(tile.block() == Blocks.air)
            {
                total ++;
                if(tile.floor().liquidDrop == Liquids.water){
                    waters ++;
                }
            }
        }

        boolean naval = (float)waters / total >= 0.26f;

        //create water pathway if the map is flooded
        if(naval)
        {
            for(Room room : enemies)
            {
                room.connectLiquid(spawn);
            }
        }

        distort(10f, 6f);

        boolean isWater=pgRand.chance(.5f) || sector.id==0;
        Seq<LakeHandler> lakeSeq=new Seq<>();
        //rivers
        pass((x, y) ->
        {
            //if(block.solid) return;

            Vec3 v = sector.rect.project(x, y);

            float rr = Simplex.noise2d(seed, (float)2, 0.6f, 1f / 7f, x, y) * 0.1f;
//            float value = Ridged.noise3d(seed, v.x, v.y, v.z, 1, 1f / 53f) + rr - rawHeight(v) * 0f;
            float rrscl = rr * 44 - 2;
            float value=noise(x,y,5,.8,80);

            //Log.info(value);
            if(value > .65f && !Mathf.within(x, y, fspawn.x, fspawn.y, 12 + rrscl))
            {
                boolean deep = !Mathf.within(x, y, fspawn.x, fspawn.y, 15 + rrscl);
                boolean spore = floor != Blocks.sand && floor != Blocks.salt;
                //do not place rivers on ice, they're frozen
                //ignore pre-existing liquids

                if(!(floor == Blocks.ice || floor == Blocks.iceSnow || floor == Blocks.snow || floor.asFloor().isLiquid))
                {
                    if(isWater)
                    {
                        floor = spore ?
                                (deep ? Blocks.taintedWater : Blocks.darksandTaintedWater) :
                                (deep ? Blocks.water :
                                        (floor == Blocks.sand || floor == Blocks.salt ? Blocks.sandWater : Blocks.darksandWater));
                    }
                    else
                    {
                        if(lakeSeq.size==0)
                        {
                            floor=liquids.get(pgRand.nextInt(liquids.size));
                            lakeSeq.add(new LakeHandler(x,y,floor));
                            //Log.info("Initializing ("+x+", "+y+", "+floor+")");
                        }
                        else
                        {
                            floor=null;
                            for(LakeHandler lh:lakeSeq)
                            {
                                if(lh.Within(x,y))
                                {
                                    lh.Add(x,y);
                                    floor=lh.block;
                                    break;
                                }
                            }
                            if(floor==null)
                            {
                                floor=liquids.get(pgRand.nextInt(liquids.size));
                                lakeSeq.add(new LakeHandler(x,y,floor));
                                //Log.info("Adding ("+x+", "+y+", "+floor+")");
                            }
                        }
                    }
                }
            }
        });

        //shoreline setup
        pass((x, y) ->
        {
            int deepRadius = 3;

            if(floor.asFloor().isLiquid && floor.asFloor()==Blocks.water)
            {

                for(int cx = -deepRadius; cx <= deepRadius; cx++)
                {
                    for(int cy = -deepRadius; cy <= deepRadius; cy++)
                    {
                        if((cx) * (cx) + (cy) * (cy) <= deepRadius * deepRadius)
                        {
                            int wx = cx + x, wy = cy + y;

                            Tile tile = tiles.get(wx, wy);
                            if(tile != null && (!tile.floor().isLiquid || tile.block() != Blocks.air))
                            {
                                //found something solid, skip replacing anything
                                return;
                            }
                        }
                    }
                }

                floor = deepwater;
            }
        });

        /*
        if(naval){
            int deepRadius = 2;

            pass((x, y) -> {
                if(floor.asFloor().isLiquid && !floor.asFloor().isDeep() && !floor.asFloor().shallow){

                    for(int cx = -deepRadius; cx <= deepRadius; cx++){
                        for(int cy = -deepRadius; cy <= deepRadius; cy++){
                            if((cx) * (cx) + (cy) * (cy) <= deepRadius * deepRadius){
                                int wx = cx + x, wy = cy + y;

                                Tile tile = tiles.get(wx, wy);
                                if(tile != null && (tile.floor().shallow || !tile.floor().isLiquid)){
                                    //found something shallow, skip replacing anything
                                    return;
                                }
                            }
                        }
                    }

                    floor = floor == Blocks.water ? Blocks.deepwater : Blocks.deepTaintedWater;
                }
            });
        }*/

        Block[] availableOres=new Block[]{oreLead,oreCoal,oreTitanium,oreBeryllium,oreTungsten,oreThorium,oreCrystalThorium,oreScrap,wallOreBeryllium, wallOreTungsten};
        Seq<Block> ores = new Seq<Block>();
        float poles = Math.abs(sector.tile.v.y);
        float nmag = 0.5f;
        float scl = 1f;
        float addscl = 1.3f;
        int l= availableOres.length;

        ores.add(oreCopper);
        if(sector.id==0)
        {
            ores.add(oreLead);
            ores.add(oreCoal);
            ores.add(oreTitanium);
        }
        else
        {
            for(int i=0;i<l;i++)
            {
                if(Simplex.noise3d(seed, 2, 0.5, scl, sector.tile.v.x+i, sector.tile.v.y, sector.tile.v.z)*nmag + poles > (i+1)*addscl/(l+1))
                {
                    ores.add(availableOres[i]);
                }
            }
            for(int i=ores.size;i<Math.min(pgRand.nextInt(l),5);i++)
            {
                ores.add(randomBlock((Arrays.stream(availableOres).filter(x->!ores.contains(x)).toArray(Block[]::new))));
            }
            if(ores.size==1) ores.add(randomBlock((Arrays.stream(availableOres).filter(x->!ores.contains(x)).toArray(Block[]::new))));
        }



        FloatSeq frequencies = new FloatSeq();
        for(int i = 0; i < ores.size; i++){
            frequencies.add(pgRand.random(-0.1f, 0.01f) - i * 0.01f + poles * 0.04f);
        }

        pass((x, y) -> {
            if(!floor.asFloor().hasSurface()) return;

            int offsetX = x - 4, offsetY = y + 23;
            for(int i = ores.size - 1; i >= 0; i--)
            {
                Block entry = ores.get(i);
                float freq = frequencies.get(i);
                if(Math.abs(0.5f - noise(offsetX, offsetY + i*999, 2, 0.7, (40 + i * 2))) > 0.22f + i*0.01 &&
                        Math.abs(0.5f - noise(offsetX, offsetY - i*999, 1, 1, (30 + i * 4))) > 0.37f + freq)
                {
                    ore = entry;
                    break;
                }
            }

            if(ore == Blocks.oreScrap && pgRand.chance(0.33)){
                floor = Blocks.metalFloorDamaged; //todo
            }
        });

        trimDark();

        median(2);

        inverseFloodFill(tiles.getn(spawn.x, spawn.y));

        tech();

        pass((x, y) ->
        {
            //random moss
            /*if(floor == twilightMoss){
                if(Math.abs(0.5f - noise(x - 90, y, 4, 0.8, 65)) > 0.02){
                    floor = starryMoss;
                }
            }*/

            //tar
            if(floor == Blocks.darksand){
                if(Math.abs(0.5f - noise(x - 40, y, 2, 0.7, 80)) > 0.25f &&
                        Math.abs(0.5f - noise(x, y + sector.id*10, 1, 1, 60)) > 0.41f && !(roomseq.contains(r -> Mathf.within(x, y, r.x, r.y, 15)))){
                    floor = Blocks.tar;
                }
            }

            //todo below
            //hotrock tweaks
            if(floor == Blocks.hotrock || floor== magmarock)
            {
                if(pgRand.chance(.1f)) floor=availableVents.getFrac(pgRand.nextFloat());
                else if(Math.abs(0.5f - noise(x - 90, y, 4, 0.8, 80)) > 0.035)
                {
                    floor = Blocks.basalt;
                }
                else
                {
                    ore = Blocks.air;
                    boolean all = true;
                    for(Point2 p : Geometry.d4)
                    {
                        int px=x + p.x;
                        int py=y + p.y;
                        Tile other = tiles.get(px, py);
                        if(other == null || (other.floor() != Blocks.hotrock && other.floor() != Blocks.magmarock))
                        {
                            all = false;
                        }
                        else if(other.floor().isLiquid && other.floor()!= slag)
                        {
                            for(LakeHandler lh:lakeSeq)
                            {
                                if(lh.Within(px,py))
                                {
                                    lh.block=slag;
                                    for(Vec2 v:lh.pos)
                                    {
                                        Tile t=tiles.get((int)v.x,(int)v.y);
                                        t.setBlock(slag);
                                    }
                                    Log.info("Lake replaced");
                                    break;
                                }
                            }
                        }
                    }
                    if(all)
                    {
                        floor = Blocks.magmarock;
                    }
                }
            }
            else if(genLakes && floor != Blocks.basalt && floor != Blocks.ice && floor.asFloor().hasSurface())
            {
                float noise = noise(x + 782, y, 5, 0.75f, 260f, 1f);
                if(noise > 0.67f && !roomseq.contains(e -> Mathf.within(x, y, e.x, e.y, 14)))
                {
                    if(noise > 0.72f)
                    {
                        floor = noise > 0.78f ? Blocks.taintedWater : (floor == Blocks.sand ? Blocks.sandWater : Blocks.darksandTaintedWater);
                    }
                    else
                    {
                        floor = (floor == Blocks.sand ? floor : Blocks.darksand);
                    }
                }
            }

            if(pgRand.chance(0.0075))
            {
                //random spore trees
                boolean any = false;
                boolean all = true;
                for(Point2 p : Geometry.d4){
                    Tile other = tiles.get(x + p.x, y + p.y);
                    if(other != null && other.block() == Blocks.air){
                        any = true;
                    }else{
                        all = false;
                    }
                }
                if(any && ((block == Blocks.snowWall || block == Blocks.iceWall) || (all && block == Blocks.air && floor == Blocks.snow && pgRand.chance(0.03))))
                {
                    block = pgRand.chance(0.5) ? Blocks.whiteTree : Blocks.whiteTreeDead;//todo starry large tree
                }
            }

            //random stuff
            /*dec: {
                for(int i = 0; i < 4; i++)
                {
                    Tile near = world.tile(x + Geometry.d4[i].x, y + Geometry.d4[i].y);
                    if(near != null && near.block() != Blocks.air)
                    {
                        break dec;
                    }
                }

                if(rand.chance(0.01) && floor.asFloor().hasSurface() && block == Blocks.air)
                {
                    block = randomBlock(new Block[]{sporePine, basalt, stone, arkyciteFloor, arkyicStone}); //Add more stuff
                }
            }*/
        });

        float difficulty = sector.threat;
        ints.clear();
        ints.ensureCapacity(width * height / 4);

        int ruinCount = pgRand.random(-2, -1); //disabled ruins

        if(ruinCount > 0){
            int padding = 25;

            //create list of potential positions
            for(int x = padding; x < width - padding; x++){
                for(int y = padding; y < height - padding; y++){
                    Tile tile = tiles.getn(x, y);
                    if(!tile.solid() && (tile.drop() != null || tile.floor().liquidDrop != null)){
                        ints.add(tile.pos());
                    }
                }
            }

            ints.shuffle(rand);

            int placed = 0;
            float diffRange = 0.4f;
            //try each position
            for(int i = 0; i < ints.size && placed < ruinCount; i++){
                int val = ints.items[i];
                int x = Point2.x(val), y = Point2.y(val);

                //do not overwrite player spawn
                if(Mathf.within(x, y, spawn.x, spawn.y, 18f)){
                    continue;
                }

                float range = difficulty + rand.random(diffRange);

                Tile tile = tiles.getn(x, y);
                BaseRegistry.BasePart part = null;
                if(tile.overlay().itemDrop != null){
                    part = bases.forResource(tile.drop()).getFrac(range);
                }else if(tile.floor().liquidDrop != null && rand.chance(0.05)){
                    part = bases.forResource(tile.floor().liquidDrop).getFrac(range);
                }else if(rand.chance(0.05)){ //ore-less parts are less likely to occur.
                    part = bases.parts.getFrac(range);
                }

                //actually place the part
                if(part != null && BaseGenerator.tryPlace(part, x, y, Team.derelict, (cx, cy) -> {
                    Tile other = tiles.getn(cx, cy);
                    if(other.floor().hasSurface()){
                        other.setOverlay(Blocks.oreScrap);
                        for(int j = 1; j <= 2; j++){
                            for(Point2 p : Geometry.d8){
                                Tile t = tiles.get(cx + p.x*j, cy + p.y*j);
                                if(t != null && t.floor().hasSurface() && rand.chance(j == 1 ? 0.4 : 0.2)){
                                    t.setOverlay(Blocks.oreScrap);
                                }
                            }
                        }
                    }
                })){
                    placed ++;

                    int debrisRadius = Math.max(part.schematic.width, part.schematic.height)/2 + 3;
                    Geometry.circle(x, y, tiles.width, tiles.height, debrisRadius, (cx, cy) -> {
                        float dst = Mathf.dst(cx, cy, x, y);
                        float removeChance = Mathf.lerp(0.05f, 0.5f, dst / debrisRadius);

                        Tile other = tiles.getn(cx, cy);
                        if(other.build != null && other.isCenter()){
                            if(other.team() == Team.derelict && rand.chance(removeChance)){
                                other.remove();
                            }else if(rand.chance(0.5)){
                                other.build.health = other.build.health - rand.random(other.build.health * 0.9f);
                            }
                        }
                    });
                }
            }
        }



        //remove invalid ores
        for(Tile tile : tiles)
        {
            if(tile.overlay().needsSurface && !tile.floor().hasSurface())
            {
                tile.setOverlay(Blocks.air);
            }
        }

        Schematics.placeLaunchLoadout(spawn.x, spawn.y);

        for(Room espawn : enemies)
        {
            tiles.getn(espawn.x, espawn.y).setOverlay(Blocks.spawn);
        }

        if(sector.hasEnemyBase())
        {
            if(!customCoreBasePartsAdded)
            {
                customCoreBasePartsAdded=true;

                addCorePart(3,"bXNjaAF4nC2QzW6EMAyEB8KGn5D90R77DBx67LmPUVUVZbMVEgtVWLrq23fs9EA+x3bGZnDG2aCY+1uAfV1i+HiGu4R1iOP3fVxmAHbqP8O0In97t3iKYZyvSxzCpVu3+BW6Rz9N3dQzRHHd1oByffTxFiJO9/Hez+N264Zl/gm/S0S1zdPSX1hsBw7r5m2YwrZyygs/VMgFjSCDRZ7BoJZbDpfgBUZqQMFDwSDn4VggvNx2bDGGqNKtlpq84s2yyRREtQNKjRyjvZFSmxr3omxxFJTcKS8Iq10iVsuCokm5ivIUqdCqnGHkkUn5IK8rnAS1iOREK3s16U2jkqWRf9YuR1mFKrvU5VRZ1nNUNlI+yEItyv98yylGEo3KSULN8Mkan8zwnAJJqoleRDIeavA+GXxQUwk1+JiSJ/66gp1/ctIv+A==");
                addCorePart(-1,"bXNjaAF4nF1W224bRwzlzkp7v18k25tfEIp+T9EH1d62LhTJkGL4uwv0oY5jbUie6WRqB6vD5XLIQw6HGfpEn0JaHfefZwp+pvxhvtyfH5++PJ6ORBQd9r/NhwuZX37NqHg6vczn3eX0fL6faTrPj8ffTyw+7C7P5z/m3cv+cNgd9ixSrR92T+fTX/P9l9OZqs/z8cF7j+R9PlMDn8fTw2yXZrx0vvy5fzi9UPbjKyXPx8NpL2uqe7bY7e/Pp6fT4fFCFPxE8pcEtKKVETFIVEOGdfyrcmh1K9EtV5XXVhd5drHVJZ5danWZZ5dbXeHZld73yn6vE2O/Q99Yfeut66yu99YPVjd+WL+x+q23/sbqbhMKBe8sTqpnu0R+5Z+Rn5ACgTUgBqSAHFACakAL6AEjYAu4Ff8BhwopXL7Rmh+zvIKuhjeWRijvgUYnY2SvRBdSRIFAAsgABaACNIAOMFAgy7ekqdzCN8dgHittA0OQJyevbey1xF4xcFCBGJAAUkAGyAEFoARUgBrQAFpAB+gBA2BEWGW5tiwjZmP+29JAKhAKq0hoyltERhgmSb1cuZy0vFPAj+En5Ge1vC8Lf9E/1iysWYTb8sbSG9u98bsxEaeXk0INj5145C1alm/8+F6MeJLTQXKKpLyh7hAXdPmbLf/h56sRcgOobkB1q8eH8/qRDx8hW+0Y1Y4lL4ENabtpLWJbi0R2TNYG0gJah0TsI96NWMinXFBVasQEERPZW13Fh9NGSxEtRbQU0VJES220zPWHdJpGy8Q+ZsiShGJb4gBlZFnwVfqZf688VzLK2b5QqXBS6aTKSbWTGpUkRptoVD18hp+QN+OVS/vKe/BKklGk9TQUBUJNc86Qc+b1c25zzpFzjgbPKZNIJuVuLaFoRUFioeXIUY7clqP40IgFSlJgA0rZgITf4FUkuJNPPQyVYAGCxYc2KC3JEiRLkCydu9KxK8GuBLvSsqu81qjArIKPyvmonI8KXCpwqbwGqd2m60ATPzX81M5P7fzU4FKDSy05Yaipr4YwqBrwaVCpFpVqnLvGVapFpRqwa8CusRm2LkOdsWLYoh1bbUeJ/M6n+SonmlsxYtsAzSJNw83Voh1DkQonlU6qnFQ7SdsxlBhtEi//6lxZYy5IPI6yyHzUFmxRjRbVaL2qdrYSHSrRoRI9Jk4vE0eUmnWHrDubde/tR4+sexzaHtF6ROu9hh9stAHRBozKAaNykFGp//sYPVcywOSUGR1r3JJcrHf95aHnxinmrgw6oz468aHr/7dWy2N4YMrgveqoXblBGao1U1muRugMILcBOc16sFmP3l6PyHpE1iOyHpH16NV4o1kbdrQSw40wFogBCSAFZIAcUABKQAWoAQ2gBXSAHjAARgolrGawsRlsvX3bSgYhAzMSSAAZoABUgAbQATiALNdct94O32iugoh269VL7hihQAhYA2JACsgBJaAGtIAeMAK2AKVw65X7zlK4sxQmeTeCuFBO3oVyshfKybtQTvZCOXkXysleKCfvQjnZC+XkXSgne6Gc+EIp3fbV2ZaeTWVt9FJJxtk0Vt96MTqr6731g9WNH9ZvrH7rrb+xOlwqJ75UhnIKhJuMH9VxncLvFrFk0w==");

                addPart(Liquids.cryofluid,"bXNjaAF4nCWOW27DIBBFL8PDTpqfLCBL8EfXU/WDOqiyhA3CsbL6yp0ZZJkzcB+ACy4WbotrgvnExzPtc1vqaykbgJDjT8o76Ovb4z6XWlOb3jHnKcf2m3Ct5c0nW3km2Nhm3Ja1HnlPUz3WiluX93K0OXHdg38QjMB1BIFMZFih8cprd5FOlj8xOA2QkvjMGVIqXIeHEUsY2X6eRkoGaAePYmaT1QYruwCNkmhOdvIoDfge8F3zohleaDTnH+SuIO8SuA4PKw1hlLtCLxk4YwW2gz3/X54lVA==");
                addPart(Liquids.water,"bXNjaAF4nCWOXW7DIBCEh+XHTpqXHCBH8EPPU/WBOqiyhA3CsXL6yt1dhOBbmNlhccHFwm1xTTCf+HimfW5LfS1lAxBy/El5B319e9znUmtq0zvmPOXYfhOutbz5ZSvPBBvbjNuy1iPvaarHWnHr8l6ONieOe/AGwQhcRxBIRYYVGq98dhdpZXmJwWkDKYnfnCGlwnV4GLGEke3naSRkgGZwKWY2WU2wcgvQVhLNyU2G0gbfG3zXvGiGDxrN+Qf5K8hcAtfhYSUhjDJz6CED91iB7WDPP1+BJVM=");
                addPart(Items.copper,"bXNjaAF4nCVM0QrDIBCLnpbCHkY/xId9z9iDa2UIVxXb0t+fp4QjJLkEM2aCSX4PUC88tnCsNZYz5gRgYv8NfEC/P4RlzaWE6m7P7NjXXwBtV8azpHDt/oyr22pkbr2lHagBqqGTBSloMYWMmCRPEmuMZCg7ypMoI5lQN62M9alBfcV2Le+gPwnfGJM=");
                addPart(Items.coal,"bXNjaAF4nDWPzW6DMBCExz/YmEbtoU/RA4c8T9SDC1ZqyRBkjPL62WUVAfrM7sx4FxdcDOwalwR1xcec9qnmreXHCsCV+JfKDn37tfhuucU1H8v4jKWMJdZ7gpmPBz7vNW7/uaVxq2nf8bWt6Vhiy9M411wKJf3Qhw4aUDACz9B00PRSzzAcdQmBi4YfhoXRhA6K/7wUe1ZaSiWlJYvWrINieFZaSgFLBr6N79accaZ00nNid2J3Yndid2/J8B6WJF7sXno9L6POSU5YgRecA/aiDCIJsmaQNQMrQflUfAFLpyDT");
                addPart(Items.thorium,"bXNjaAF4nCVQy26DMBAcP7AN9FSpn8Eh31P1QIPTIhkc2UH5+ah012uEh52ZtYfFiNHA7vMWoS4Yl1ivZb0/1rwDcGn+jqlCf355vD9+c1mPbXrOKU1pLj8RHyWu+y2Xa1ymehDTRNjbUSPcFvclFgz3/Ixl2vMSMaa50vdSVrK9iVDzQQfQdRPa0rJpRWBhGDoBB8OaF0sPTS7SlPRow10wDEbACxlY19xAPKuWDcG0Cyxw0tLM+dBq6POP2Nd5QrWWwGms5OroVR34UVwFKNYGUI6OLQytclI5rjh1i9jCWwIT9PliA93NEek4R7QPdDyRLLE7SG/L3v6DwQh4gSDTaFPo2wwIvEDPyQeZ4yBzHHiO/7hMPac=");
                addPart(Items.titanium,"bXNjaAF4nC1Qy26EMBBzHhAevVX9DA77PVUP6ZKtkFhYBdD+/Kp0nCmRcDx2ZqxBj97BL/GeYC7ox7Rd8/TYp3UBUM/xO80b7OdXwPs+7XGZjvvwjPM8zDH/JHzkNC23NV/TOGyHVIoIfzu2hPqeljFldI/1mfKwrGNCP8dN7mOexPamwrYe0kDmDSif4c/CED0coVKo4agFtbQwVnwVmeU7B3JLcApBi41aWrJikrZwjSsDPHDKZ1gLUvPsfb7OUxLQ3jCJ10wVc1XgMWT/WsfQlWavlNXKajImLvECk3kB19jzxZrMZTxpV0tZxp+/copUxgcRLd+X7Lw7glMICuRiKJtrmZAQFFom7HSPne6x4x7/AG0bQAM=");
                addPart(Items.copper,"bXNjaAF4nCWM3Q7CIAxGP8aPy7wwxufYhc9jvEBGDEk3CJv6+raQUE779QAmTBpm82uEuuO8xD3UVI6UNwCO/CvSjuHx1LiGXEqs888TzeTrO8Lunr4Zl7LFz+qPFOalJiJ+eeOCwQAovhqcgA8GiQwnjFE2mhslcBIaUUQwfWqK7Yrtiuu/uK44UYCTKIKmjLL7AxE/GY8=");
                addPart(Items.lead,"bXNjaAF4nCWM0Q7CIAxFLxtjy3wwxu/Yg99jfEDWGJJuI4Dx921HQjnt7QHMmHvY3W8E88BlpRJyTDUeOwDH/k1c0D1fPW7hSIny8vPMC/v8IYwl+Fop45p2+m6+xrCsOTLL27sULDrAyHXCKeSg08hKIph000tjFE5Dq4oKtk2nMjRlaIprv7imOFWAURXFqUy6+wO2rxpi");
                addPart(Items.scrap,"bXNjaAF4nCWM0Q7CIAxFLxtjy3wwxu/Yg99jfEDWGJJuI4Dx921HQjnt7QHMmHvY3W8E88BlpRJyTDUeOwDH/k1c0D1fPW7hSIny8vPMC/v8IYwl+Fop45p2+m6+xrCsOTLL27sULDrAyHXCKeSg08hKIph000tjFE5Dq4oKtk2nMjRlaIprv7imOFWAURXFqUy6+wO2rxpi");
                addPart(Items.coal,"bXNjaAF4nCVMywoDIRCLr2Whh7If4qHfU3qwrrTCrIru0t+vo4QhJJkEK1YFndwRIB647aH5GssZcwKwkHsHapDPl8Lmcymh2p8jsuTqJ2BpPlf/xb2kcB3ujN7uNRL16tYPqgOiY5CBEpBsMmk2FT9xLDGTqcwsL6w0Z0zDNDw2piaNFTM0v0P9Ae8yGdA=");
                addPart(Items.copper,"bXNjaAF4nDWOWwrCMBBFb95SP8QduIF+uB7xI7ZBArENaYvbdyaDJOHA5J6ZwYDBwC7xk6DuOM9pm1que14XAL7EVyob9ONpcJ3WWlMbv7GUscT2TjDzseJSl3R84p6ncW65FPJu9GCgGI6h+qEboBQ0/zGswAk8RwwpWhE0NOeMwEkxSOMTt7IcYQQZ0z0nnhPPief+ke558bwUA2/Ge9EohhU4ged9Qg+RzNsQ6O8HTR0ZXw==");
                addPart(Items.coal,"bXNjaAF4nDWQzW6EIBSFr4Cgo7GLPoeL7vsmTRd0JFMSdIw/6ev3Hk8mIl+AwwcXGWSw4pY4J6k+pJvSft/yeuTnIiK+xJ9UdjFf307ej3zEJZ/z+BdLGUvcHknsdD5leGxx/c1HGtct7bu8rUs653jk+zhtuRQ1feovtVRAACqxYoAWI6Od0YYI4IlANIx0GFn9LjipjKIWCwSxmGy5dsMGp0ep0+lOYxQWSacRi8kON3HS4wo1IlCpDLhkNSOeFk+Lp8XT4l+RyxJoCbQEWgIjDS0NLQ0tDS3NK9LzOa5Iy8JaWlpGUJaObtgOOKIlMK1dh9o7RjpYAE8EAucrrpfvEQF07R/hMyJm");
                addPart(Items.coal,"bXNjaAF4nDWOQW7DIBBFv4GA7Ubtoqfowouep+qCJihBwq6FiXr9zmcUYevZzPszgzPOFm6La8LwiZdrOi417y3/bgB8iT+pHDBf3w7vLbe45ce6/MVSlhLrLcHdYy54vdW433NLy17TceBt39JjjS1flmvNpUirD3lxggEGWEUgjHwYeaQ2EF7+BRMvLQ/hYI2gK1ZyHSNNJ13FdBIxhh4GoitOuoDKzGmcbdijdzlpzWvca9xr3GvcP5X5uawoQeNBa1xBFhx1z5HzicDAKKcr3ZxUmSTewVGCQMy8/AfEeCEc");
                addPart(Items.coal,"bXNjaAF4nDWQwW6EIBRFn4Ago7GLfoeL7vsnTRd0hsyQoDXqpL/fd72ZiJwAlwMPGWW04pY0Z2k+pL/l/bqV9Si/i4j4mn5y3cV8fTt5P8qRlvKcp79U61TTds/iHqlUGe9bWh/lyNO65X2Xt3XJzzkd5TrdtlKrqj71l1YaIACNWDFAxMhoZ7QhAngiEB0jPUZWvxNOGqNoxQJBLCYj1y7Y4PQodTrdaYzCIuk0YjHZ4yZOBlyhRQQqlQGnrGXE0+Jp8bR4WvwrcloCLYGWQEtgpKOlo6WjpaOle0UGPscZiSws0hIZQVk6umA74IhIYFq7HrX3jPSwAJ4IBM5XnC8/IALo2j+PhSK9");
                addPart(Liquids.cryofluid,"bXNjaAF4nFWQSW7DMAxFvzVZbtJVT9ADeNHzFF24jlAYkAd4QO6eJlU5dFMY9KfIr0faOOFk4aZuTKjecLqkrV+HZR/mCUDI3WfKG8z7R8DLPuzdNBxje+1ybnO3fiU8/yviaZmvaW2n+ZL48tSnFedhXI68pXY5xgVndWzzsfaJRrxSwKBicSpBJbJwZioyGMjLxga2FFQU2rVUdfJ4yvxfRasigX2GcDKogTgotdIVbyQqE5nsyoPiG6bcgHIrpdwpfuj8kBmerzvUurDgvOJ4PvWC9oL2ODeGxMSm3CHTiWZlQoEnqqfc8ddQnQE178fiefPIVE9SR1g+Rv05Qqe12Uz/RMWrEOEXxmlRDA==");
                addPart(Liquids.water,"bXNjaAF4nFVQSW7DMAwca7PcpKe+oA/woe8penAdoTAgL/CC/D1NqnLppTDoocjRDEWccLJwUzcmVG84XdLWr8OyD/MEIOTuM+UN5v0j4GUf9m4ajrG9djm3uVu/Ep7/FfG0zNe0ttN8SXx56tOK8zAuR95SuxzjgrMytvlY+0QWrxQwqBicQlCIDJyZiggG8rOxgS0FFYV2LVWdfJ4y/1fRqkBgniE5MWogDEqtdIUbSZUVWdmVB8U3TLkB5VZKuVP80PkhHp6vO9Q6sMh5lWN/6gXtBe1xbgyBiU25Q9xJzYpDgSdVT7nj11CdBWqej8Hz5JFVPUFNC+Fj1OWIOo3NZNqJglcghV/GE1EK");
                addPart(Items.copper,"bXNjaAF4nCWPQQ6DIBRER0FEa9L0Ar2Bi56n6YIqaUioErTt9ct3FuSF4fEHMGBQ0It7e1Q3nGa/TTmkPawLABPd08cN9f2hcJnWlHwefy7GMbr88mg2F78rzmnxn7fbwzTOOcRYbl7LgoYSGEGFmrCCusRKIk0Yhr3slJiCw9RiiqcJw/AwG5oNTUPTlNNaYKTF0GxptjQt2y3bLWdaMYGO7+xo9vxDL8of/zoafg==");
                addPart(Items.coal,"bXNjaAF4nDWPyw7CIBBFb+kL26Yu/AaXXfg9xgUqURJaG0D9fZneGBJOZsK5zGDEWKJazGxRnNDfbbwFtyb3WgA03lytj1DnS4VDcsks7j1PX+P95E14WNTR+M8L4yOY9emSndZgY8R+Xex7Nsndpntw3uesIyQQCihQEVqg5BT5alh1gjK/zc1SXgo0m8NfViqjYdVJcC2Cyqgko87C1twECVZlRsNqE1oKLYWWQktB8wdNQVPYcaSdCALN5iDouEMnglTbDj3X7Dn8IGP8APFJIZM=");
                addPart(Items.coal,"bXNjaAF4nDXQW26DMBQE0IvNwxhKInUdfETqbqp+uImVWjIEAWm23zuMiiVGfpzrh5zlbKWcwxSluEh3i9t1TcueHrOI1Dl8x7yJ+fwq5X1Pe5jTcxpfIecxh/UepdpC/n3IcF/D8pP2OC5r3DY5LXN8TmFP1/G2ppy11oegoBQIhyikFIPw6Bm0Qn+6BOE42COsEp3Tc2LOElgZ/msURqMWi3BiMXi4Cs5olJir1FkMHq6GsxrqEIer6Rq6hq6ha+gcnaNzdI6upWvpWrqWzvOcnuf0dJ6u4/063q/j/To40QUGK3u+S8936bXp94aNEB69AaURWvoPLy0jBw==");
                addPart(Items.thorium,"bXNjaAF4nCWMSw7CMAwFX5pPi7JjxyG64DyIRaARRHIpSgpcHztWFI/1PDYiooV7pTXDnBGX3O61vPeyvQAESrdMDcPlanHcn1stn3X+JaKZUn1k+JbouyFSarnOSy1EvHbiDwcjCIpJIF3HACPFKboyqGJFMQxWBnh+nDqMMnM4CLwqXq8EnQWdSc/hyAc6vISTKpMqUjqsgpU/ME0Ysg==");
                addPart(Items.thorium,"bXNjaAF4nCWPSw7CMAxEpw0NIa1YcAh2XXAexCJABJUMRSmf6+OJFVUvtd+4LrbYOqye6ZHRHNBf83Ip0+s9zU8AXtI5y4L2eHLYve9zmT6P8ZdERknlltEtSb4zeklLLuO1TCIa2+uDFRrCGwLRoNWjiHzTG1qW1CS8IVhvoOcYIKJN1EaLTg+IYMVqdmZ2ZnrU8d56a/Y4uOYCcyxG26uawcyNTdlYL9qCUb/hCG8Iuo9ioNJrwBH1jwaahDeo+QdJGRnU");
                addPart(Items.lead,"bXNjaAF4nCXNUQ6DIAwG4F9BZM5k2QW8gQ87z7IHhmQhQSXIsuuvtSHkh/LRYsSooDe3BjQPXJdw+BJzjfsGwCT3DulA+3wp3P2ecyjzz6U0J1c+Af3hXa2h4Ja38F1djX5eSkyJ/k60odFwGI4GLS0Kyzc6oeWShuIwUFwcGChxiuTZgx7YUTOOs5kW2YnsRBqRRqQRaUT2InuRVqZbmW5lumUJXEgqDssxMOEg8gfPEBsi");
                addPart(Items.scrap,"bXNjaAF4nFVUW27bMBBcPSxRFEXSTtBb+KPnKfqhOkIhVJYMWU7QHqyXaxp3h9MUKGB7uPQ+ZrkPeZQPhZRzfx4k+yjt03A9reNlG5dZRKqp/zJMV8k/ffbysI1bP4+38/Gln6bj1K9fB6mvp37bhlWqdbkB9/+0Tsv8PHxfVun+M5Ty2zjN0o7zaZyHtd9Uo7ouK4ztZXkZ1uO8PA16vk3Pwzr+0PvmOlz6v6rnYYJquMzD7dxv4+n4tI7q1tH2utzW06Dcf+pXcskAJaEiGIIlOIInRMIBAONcP1JIAdgRakJDaAVa0hECYc/YD/BS4pgp5JLnCirrg8MZpIpSTclQaihZ+CgRAZfKE1JHyVMKlCKlPaKXSj6l/Ij/doiXKWg8BK0JRgpcNYSWlx3BI7Gdui5gvufDpRwq5lAxh0qdZQBD6GBQwRwqiUtFLhW51LBDrjtJxlqOWqFWMcuMnoyx6d2gzKLt+Oz6hbPd/U3k/qa/WdGovtIHWEJLcFLAV2e0aqmYOSwKRPIpEkIHyUAkktaeXZFyNMzRkKsBV6ugL9AqGPSL0cAJWl52kkPF0yAiA8P8DfM3zL+hzwY+WwVDUHNcRqrs2ZqJiwWXQqGEiiUXSy6WXCy5WHKx5GLJxdKnVZ/J2YGuHyW1beLSkktLLi25tMyhVbsMzhMXRy6OXBxr6FhDxxrquzvUEFV6f/f773Qu7q96fk3n8n7XSv5KNXSsoWMNHWvoUMNOf1DDNIao+P21QKT3GjrW0DFHxxwdc3TMsWPvd+igQiG1a4d2Ra83hJaXHVVS73cYK5in/D3z98zfM3/PQfIYJEBH8PDiycWTiyeXwJkJnPvAuQ+c+8C5D5z7wLkPZBY490EjJMlTCpQipcQTm0vJR26ryG0Vua0it1XktorcVpHbKrLlom6rtPiw7hRKQkUwBEtwBE+IhIMUfwDzMXjZ");
                addPart(Items.scrap,"bXNjaAF4nCXNUQ6DIAwG4F9BZM5k2QW8gQ87z7IHhmQhQSXIsuuvtSHkh/LRYsSooDe3BjQPXJdw+BJzjfsGwCT3DulA+3wp3P2ecyjzz6U0J1c+Af3hXa2h4Ja38F1djX5eSkyJ/k60odFwGI4GLS0Kyzc6oeWShuIwUFwcGChxiuTZgx7YUTOOs5kW2YnsRBqRRqQRaUT2InuRVqZbmW5lumUJXEgqDssxMOEg8gfPEBsi");
                addPart(Items.coal,"bXNjaAF4nDWOUQ7CIAyGf1ZgZj4Yb+AF9uB5jA/IiC7BjbAZr29LY4B8pe3XFAMGgl3CO8FccZzSFutc9nldAPgcHilv6G53wjmupaQ6fkPOYw71meC3uNb4wqks6fMO+xzHqc45s3rhB4IROIHh0wl6kESksAqn8JyWi84wuL31gQSthVhvgw/yszrRatKp59Rz6jn13L+leV49r8meQ2p7KazCKdpKfauKrODaD728Gq0=");
                addPart(Liquids.water,"bXNjaAF4nEWQwW7DIAyGHaCBpr3ssMfIYc8z7ZAlbIpEAoJGffmqzD+uNHL4Ymz//jFd6KLJ7NPmqfugy+LLnNd0W+NORH2Yvn0opD6/LL3NMSWfx/sUwhim/OvpPft1/4l59stYDr5pSbK3crDiSv3m98VnGlK8c+ceF0/XdUtHKH5Mx5boKpkSDxbhiSP9H0XUMQxp4CToSSNnpeSMKsU5KSdOIlaAFljqAActhQYtRQY/jt/ehEzlo3FnHQvVR63ch1Ju7KQIcxSM4GuRg0NDA9A8AC3qJeoRvdyyNFwZBuZi5gNFPBf2Tnib5fG6PusTFri/a11ONJp3Bw1AC6xcutc2BEpgBWc4GWSPg+xxwB7/ADmIQPA=");
                addPart(Liquids.slag,"bXNjaAF4nEWQy26DMBREx49gQrLpop/BIt9TdUHBrZB4WBCUn4/q3vGNVHtxuM8ZjAsuDn7p5ghzw2WIe7+N6T6uC4Bq6r7itMN+fAa89WtKcWsf3TS1U7f9RLxvcVy+162PQ7sfkilFhPt+yMYR1RyXIW5o0vqQyWUdIq7jnI5pj2065oSrVvb1kCWi2OL/WMAIPBxxUlRwrAVtObPLSk3bIUXGlnCKAEPU3GU54LTJ86N2RcADWY5hLkjOc1V+5gxj2C7DRpPUsjTDW6KaLj0aovggSlRpVDF6OZb1dOYFrrb5yQbRpb0T/y1QPv/KLaUiHyjB+eK95jzhFEGT9es1FFYRFGduavQdG33Hhu/4ByjIQOs=");
                addPart(Liquids.oil,"bXNjaAF4nEVQy26DMBAc2wQ7JJce+hkc+j1VDxTcComHZYLy81HojjdSzWFY7+zMeHHBxaFaujnCfOAyxK3PY7qN6wKgnrrvOG2wn18eb/2aUsztvZumduryb8R7juPys+Y+Du22y01pwt+2XRRH1HNchpjRpPUuk8s6RFzHOe3TFtu0zwlX7WzrLiLi2OL/WMAIVHCEk0INx55XypksKz2lQ5qsLcEpeBhCoJblgFNSxZ8gby9C1SGHVvBBhI6HVKZQZdAoiT6WQfiVKjBhhYZQMhBKVWtVs3qlFWmmqgToS88HSeLEeCe+zYu9O57HkxFk3hhOBdUo2QM1CE7B62V4bUPBKniFM5M0usdG99hwj38zFUDo");
                addPart(Liquids.cryofluid,"bXNjaAF4nC1Qy26DMBAc2wQbklvVz+DQ76l6oMGtLPESYOXno9Adb43EeB8zs15ccXWo5n6KMB+4DnG/b2k90jIDqMf+O4477OeXx9uRjn5Oeeoe/Th2Y7/9RrxvMc0/y3aPQ7dnyZQi/LFn0UyopzgPcUO7Lo+4dfMyRNzStOZxj92apxU3rexLFhHx7FCO1Z81AhUc4aJQw7HmwQANjHRJ7Z9jHVkKTkE6CYEES4JDaRJZuCCvL4bVKafkfBCh83meIs1WIRptoo/lIPxKFADWWkKZgVCiWqOaEacFnEgLyVQC9KXnk03iy/EufJsXe3e+zhdHEL4prKAaDco4lk8Ici3gNRl0GwwErILXZMNJWt1jq3tsucc/9rlB4A==");
                addPart(Items.coal,"bXNjaAF4nG2RwW6DMBBEJxhCiBtsRe2p38Chf9D/qHqgiZsiEYJMov5+d5hrbaFne3aW9RpHPDuUU39N2LzBn9NyysN8H24TgO3Yf6VxQfHxWeE1p2H6vuVTOnfLI19S99uPYzf2tsTLvyK2lmseE9pL7uef4Z66OadlQZin9Lj29+HUnfNggcC7fahREI3giQ0q4YBiYysHR5RwXO9QEF5akBYJx8m4iiElWoY402yUFlQQkajspHCGWmgEL+1Ie21OO9wyGXHQ4WqvaS8NtdAIXtqRhp18O/p4O9C+Z87SdrXQCF5aVORq38u+l92rXK9yvcr1+p+nAXji/QoLr4RWh5G7Vj1r1bPWigARiaBdsM7YCOp8UOeDzVVbOxj1VFFPFZnsD1xYMMg=");
                addPart(Items.coal,"bXNjaAF4nDWS3XKjMAyFhX+AEMAk07s+Axf7PDt7QRNvlx0CjEmm794m8Uo+2zDiM/KxpEimF3rRZObh4in7Qfuz305hXK/jMhNRPg1vftpI/fxV0Gvw4/x7CSd/7rdbePf9xzBN/TTwkvKLn88+ULGt/nQNnlxS9mtY/rJjCdS+h2H9M17F57eN6nX58KHflhvryK2zv12G63jqz2GcJk7+RumXyUsBBsiBEqiAGmiBDjgKlATIGArQpAUGsKTFVVDa2UtCxVHSngM64CCRFMdkiZZg3Dh+EpyE1pAY5DPIZ76d6ZyF08KZi1Jxbi17OT/K8KugTLAD9pTJXgOlQwM4mOWvvCzjIz75uTPv8StGNvmS9YPtrkXXiI5UfJJhmvhFGZuK97TWbEZLQ0kKKKWOnFGSFlSkxVmj50f0PBW+l8INf6UDFQ5UOFDJAZE4HDhiSOyzjLzccdpnKkGz2RjZpDxZP+InCy0Xb1hbUMIO4JwSoJEAaaY8Rzb7v7kmrTUCpH9Vo/ONFClopPMtZtRijE6GJeNOt8DJLRCng+SAe5WiOMzPwdkhSgdn9+1MShmwFmjAABZoAQd0wAFXVpIzFGCAHCiBCqiBFugADvAP0I99uQ==");
                addPart(Items.thorium,"bXNjaAF4nC2R3W6DMAyFHUIo4ydFvd0zcLE32HtMu2Alm5hSQKFVn33q2swnXkX9kcjHxzZ0oIOmfB5OjtQL1aPbjmFaz9MyE1Hhhw/nN8re3gt6Dm6aP5dwdGO/XcKX66+D970f+JXsyc1jv4bl2x3PS6ACZxdot618ERw163J1od+WCxeg2g8bn8Ywec8+r5R+mQQF5IJCUAoqQSOwgg6ANFMMliPkpAFDWpOmHSmgJoUUK4IOQSOTHzKljjHe+Zgxo0YHnKBRAiVzlmlgL911CIYsy+5IjL8xsgss99I4Xxp2Zm+gQgs7auFdQI2hksETDHDiS8N5SVDJrJWk1KKrRNfgMmNkaL0hXUrTD8rjDYvj9jUrDCxbrq+gSeO20ABYB0+UlmaxNMzXlOZ/lAf/b/GHS6o0lGUJITPtzkqxPToBWvkMhJpdWiojFxSCUlAJGoEVdKT/ABqTUDQ=");
                addPart(Items.beryllium,"bXNjaAF4nCWQ226EMAxEJyHLJay2L/0NpPZ7qj4EiFqkcFFYtOrP79aDieBge2yNgxtuBdwS5gjziXaM+5Cn7T6tC4AyhT6mHfbr+4L3Pua/lKZj7h4hpS6F/BNRznEZY8Z1Wx8xd/t65EGyfY5h+MXbtsRjDvdp6MY8pSQzP3A+Vj+GcIpSUSu84kpQbI3AoiAKFPx3tdReTyOhNFuiUV2r06+QlJNXkoUmHQvSirY2ryc43VHncJFjiYqjSjFg6QmMKs5gdCorVVZU0u8pabShpsTSR00jEHNeWixxyr3u4vUGPOVGrFmcthuuJHWmPJdt5RS8BiiswilKRa3wCtH+AwoTKQI=");
                addPart(Items.tungsten,"bXNjaAF4nCWQ0Y7CIBBFp1AJYsXG/Y4+7Pv+yWYfaiWuBltDa/z1NdGwM9w25TBlzg1Ae9prqsf+Gqj6pM0xzEM635bzNBKRif0hxJnU94+hj+U+nuYljN2jj7GLfToF8tcwHrtbmi5hWKZERurAPKTQD7/U3KZHSN083dMQaHOI/bx0x3SOkeO/qDwKQyWoAQNYwAEN4IFWIKqqGPzKUJPSpEUXrAH2pMVLt2KPu/jItuKRJa5IrJWUNVeeV1R+KUnbUWlgZ8WhHCtwEms4r2y0bMJIi+b9aqksEtdosdAddIfTOLRsIDjoDYQGmY38VLSVs9UMbVV+k85P0mx62bDAApJUdL4OD8XT1ur8ym/+nvkvv8tSuQaP7F1Jl7nYLc8LasAAFnBAA3igJf0Prxc8Sw==");
                addPart(Items.coal,"bXNjaAF4nC1QW3KjMBBsSRiEIdiVe/CRK+QaW/tBjJIlhYESduXi+/5YpUezDKjRzHTPA494dCiW4RpgntCMYb/EabtN6wKgnIeXMO+wX756nPd7fAv9xzDP/TzwF+U1LGOIqMbp9fW+B5y39SPEflnH8D+lk5R+i+t7uNzWiFYz9vUeL4y+xWH7Nt0CM8K+47Qt4X4dbtOlH+M0z2zhmR8cjc9BoVKoFRoBgwdYgZOAFTM8CjjLcKlAgqOz8R4u/YVJfwB+R9Ja+o/0iUqTfhtkbWo40eDLiBMn9W3ug85Cax808yBuOUoPUxLZpkAtxUt0mnqGLSFuCDQ6DkTUi2gFMStHLZNUSvBK8EKwjGSxWniSVylkQi0Emxtj9aPyjlpIDitFK+TatYi1KtYIr+AtD9FySTCyllaCD9yQKfIKOEsnrQo0GiPR8XbwVfqXhyEVPv3icn+klH6iTN9TolqnS+xo7PQEo+BkeyeNnVnfClSMEmqFRoF9fAJc1FAr");
                addPart(Liquids.water,"bXNjaAF4nCWSXW6DMBCE1zb+wRBF6j146DF6hqoPJLEqJBMQBEXt1ZOG7nqE4o/N7ngGAx3paKi69mMi9U7NJa3nZZhvw3QlIpf7U8or6c8vR2/nfjkNl9Td+5y73C/fidp5uqelW6dtOSdyY7pe0kL1up3yMPY3Hkg5nW/LlH9+udEO47zlNXXzNs68/wf/SJMSVIADAhCBViB3WvG4DnWRaWmR4Qv6qtDy/1ZWJROGCirAAg7wQABqIAINKbFq2arY7C/Y7H+w2Z+8mfBBdn8piXNAGr4VW97ElExaFo8qotdSiaSlqqSS9AeEL3KLnsWkk0r2qMhYhkdVkjrIHeQecg+5hzygCpgMmKwxWSNnRM4oG0gV0SvyCHmEPELeQN6g10hP8aJDkAYfVjkLPiw5qPJ8fFDlATlzK+9MUAEWcIAHAlADEWjIiE0bIiz2l1jIW4HN/oTN/uDOQy4lH0+JfOCERmAAC3igBtjgH1AYVXc=");
                addPart(Items.tungsten,"bXNjaAF4nCWRYW6DMAyFHQIZBRpS1mvwY5fYJab9oG1UMaVQhVQ9fFUxmxeRfGDnOXmGjnTUlE/DzZP6ovril3Mc72mcJyIyYTj5sFD282voMz2m65L81D+HEPowxKsne/PTpb/H+c+f0xzJyLePVKQxDRM19/npY7/Mj3j2VJ/CsKT+EscQuPo3bSPDogQ5YIASqIAGsIADOoFUyBSDq8iSkxYUpDVp+gBqBC22tKRE4CDvZNGi44eKMudXKWNWjLeSuC15y/peV1I8MyWSVgrkcr6W8+SogoNagh2scK7gS/A1BLXkDHIldDvodtCVyFXQ1dDV0FXINdDtodtD1yBnkWthu4XOIufQJ4c+Ofh14nd9UcbTbM5o8+vE7/pa3+xQbUKRtERSYOubQ98O6PdB/Assgi1+D8n53dZeRg4YoAQqoAEs4AAu8A/TX0WF");
                addPart(Liquids.slag,"bXNjaAF4nF2U627aQBCFx15f1uBg8C2N+gz86PNU/eGCm1AZjGxQnqmvmJLQOXswUiNkvrU9c2bnspYneTISHJp9K943mW/bcTPsjqddfxCRqGt+tt0o/vcfiXwd2t3hVz9s2u16PA/P7fq16bp11+hSZsf+tR3Wh37bSrRvD9t2kMwZr49D/7vdnPpB5c7jaWjlS3Pa9+PxpR12m/WmP2zaw2loYJFSZuzP6inzsWue1y9tc1K1dLc/nrtR9c77o+7tj17iiwcERERYYkakxIJYEjlREjWAle+pom9jVRIJnbCIcXEYLZDAqIU+83x36xASERETlkjEg+jMRmqtMte/el30etfrQ4LrxRiVS2m2sFM0c4+VUWhJrIicKIiSqChS2+QmwQSQyKeY4l8/DAI9ypQhklIRFxdyBkXA2ooBMjF4mHNrJU1qFsWHewAHICdqdsZFCBkhpGUIS/ylvHMOIcUimkQqboCACMVEisgmunlNQP/NbfWhKYm73m8Jigfb3EaT7fR8ytWDZEHlkqiImnPkNh1z0zF2hDIEKIRlPSzqATiVmHu3sIy15S4vSzFLsYRiCdNL6DDDnaeote/+9Trt02B6ndscbhgPnbhQESFAqv7GKlJsMsWE4F1OywqjnNL9gVEXTCFjChlSiBWpuL5mfKiZeGpZ21BLevlvgjClOEJOM6NmxkwyZrLk3ZI5L2m5ouUK79DllHfOIadDzj7n7HPOPufa57mbPXTv3XXwcn3T2rxphXR075u79d0LdPeuLwWygUJuLbo/qSDa/SzfVh4CFoxbEhVR8xvhkiiYRMHDUfJwFJz1gseh5HEomFnJzEqYGFVM8a7kkJeULildUbqiQ0XNipYVLD39862dNs5kb2cZ35E3t8ZnomYda9axRh2BiIgJSyRodK2fpGCqH2Twc88X1p2qe9ApQEbvJbEicqIgSqJigFoP4P3gfQqEj66rwaMmaQBDhERMJMSceCAyYkUUhMb8Bz4B0y8=");

                addPart(null,"bXNjaAF4nDWM0Q6CMAxF77oNUNQ/4cHvMT4Aq2YJbDggxn83YsGYZe3JPW1hkWuYUPcMdUbpeGyTHyYfA4CsqxvuRtDlmiPrOThO2A/xyakK0TEOPx7jnFpG2cy+c9W0Zij9xP3fnO4pzsFVt7qdYnrh2PnH7N1fA6V8EEhJ04UCLR8iYVMQzLKIMkpLFW+kaAvsyEpg1w0oI7iGigz0RpYyGSVrZFAJydXlvXl54jP6AqBmMCk=");
                addPart(null,"bXNjaAF4nDWMUQ6CMBBEp9sWVNSb8ON1jB9AV9MEWiwQ492NuGBIs+3LvNnCItcwoeoY6oLC8dAk348+BgBZW9XcDqDrLUfWcXCccOjji1MZomMc/zzEKTWMop5868pxyVD4kbvNnB8pTsGV96oZY3rj1Prn5N2mgUIGBFLy6J0CzV8iYSNs5llpkeKMXNoCe7IS2KWtlBFcQkUGeiVLmVTJGinK/vLj/Fm9HPEZ/QDpFjAk");
                addPart(null,"bXNjaAF4nDWMUQ6CMBBEp9sWVNSb8OVxjB9AV9MEWiwQ492NuGBIs+3LvNnCItcwoeoY6oLC8dAk348+BgBZW9XcDqDrLUfWcXCccOjji1MZomMc/zzEKTWMop5868pxyVD4kbvNnB8pTsGV96oZY3rj1Prn5N2mgUIGBFLy6J0CzV8iYSNs5llpkeKMXNoCe7IS2KVNygguoSIDvZKlTKpkjRRlf/lx/qxejviMfuo3MCY=");
                addPart(null,"bXNjaAF4nD2NwVLEIBBEewbIxl3d4/5FDn6P5SEbRouqBFYCWv67ZRxi6QF40930wOFoYOO4COgRJy/rlMOthBQBdPN4lXkFPz336BaJXjKOt/QheYjJC+5/eU01T4JTKLL8D9caZj+UFsD5Naca/fAyTiXlT1xG70MJ7zJkmVJcS67NwMMc3mrwfx3AWQ8YTPqYnsDbN7Oy7VWEhd02MjofHHBHVjVNGr1ME9ip4NpfUKfYQ8O8e8RGc40cH7SIndU8Kema7Wv3tV79bqe2wNAPIxc5mQ==");
                addPart(null,"bXNjaAF4nD2NwVLEIBBEewbIxl3d4/5FDn6P5SEbRouqBFYCWv67ZRxi6QF40930wOFoYOO4COgRJy/rlMOthBQBdPN4lXkFPz336BaJXjKOt/QheYjJC+5/eU01T4JTKLL8D9caZj+UFsD5Naca/fAyTiXlT1xG70MJ7zJkmVJcS67NwMMc3mrwfx3AWQ8YTPqYnsDbN7Oy7VWEhd02MjofHHBHVjVNGr1ME9ip4Npfok6xh4Z594iN5ho5PmgRO6t5UtI129fua7363U5tgaEfI145mg==");
                addPart(null,"bXNjaAF4nD2NwVLEIBBEewbIxl3d4/5FDn6P5SEbRouqBFYCWv67ZRxi6QF40930wOFoYOO4COgRJy/rlMOthBQBdPN4lXkFPz336BaJXjKOt/QheYjJC+5/eU01T4JTKLL8D9caZj+UFsD5Naca/fAyTiXlT1xG70MJ7zJkmVJcS67NwMMc3mrwfx3AWQ8YTPqYnsDbN7Oy7VWEhd02MjofHHBHVjVNGr1ME9ip4Npfpk6xh4Z594iN5ho5PmgRO6t5UtI129fua7363U5tgaEfI6U5mw==");
                addPart(null,"bXNjaAF4nG1Py07EMAycPLe7ZREnTpy498D3IA7dJqBIfSxpC+LX0UKxHe2NRo7diT3jwQ53BnZshwj1hDrEucvpvKRpBOD79hT7Gfr5ZY/DefqMuRmnEOGHOIaYcVOweVpzF1GnJQ7Xn4dh7Zd07lPXLukjNjl20zgvee2WKaM+rakPzcLjuH3L0zqG5rXlty/ctyGkf4aOfXpfU7gqAI8UqCsNtf1SfCsFxZCSY/jSJRkqKNnKQ1MvJH4oLjR3IeyiiQWustRotw2eaKQSTl9YdjCGUuWAveD7AhwEsKRyhGJN+lzR5MvysyYO+LIe8dCo4rXALUZalKYFC2AFcAS44scVP479VMztLFGyiq0qNiFjEGm3bYRAQmxqQ32OmLwQ+sIkZpyYMYqBfQHYjFe0K2qqtPoDymtgpQ==");
                addPart(null,"bXNjaAF4nG1Py07EMAycPLe7ZREnTpy498D3IA7dJqBIfSxpC+LX0UKxHe2NRo7diT3jwQ53BnZshwj1hDrEucvpvKRpBOD79hT7Gfr5ZY/DefqMuRmnEOGHOIaYcVOweVpzF1GnJQ7Xn4dh7Zd07lPXLukjNjl20zgvee2WKaM+rakPzcLjuH3L0zqG5rXlty/ctyGkf4aOfXpfU7gqAI8UqCsNtf1SfCsFxZCSY/jSJRkqKNnKQ1MvJH4oLjR3IeyiiQWustRotw2eaKQSTl9YdjCGUuWAveD7AhwEsKRyhGJN+lzR5MvysyYOeF5PKeKhUcVrgVuMtChNCxbACuAIcMWPK34c+6mY21miZBVbVWxCxiDSbtsIgYTY1Ib6HDF5IfSFScw4MWMUA/sCsBmvaFfUVGn1B8saYKY=");
                addPart(null,"bXNjaAF4nG2Py07EMAxFb57TmTKIFStW7LvgexCLThNQpD6GtAXx62ig2I5mRyMn6Yl97Ysd7gzs2A4R6gl1iHOX03lJ0wjA9+0p9jP088seh/P0GXMzTiHCD3EMMeOmsHlacxdRpyUO15+HYe2XdO5T1y7pIzY5dtM4L3ntlimjPq2pD83C5bh9y9M6hua15bcv3LchpH+Kjn16X1O4dgAeKVBXGmr7pfhWCoqRkmV40+UwdKHDVh6aciHxQ3GhuguxiyYVuMpSot02eJKRm2j6orKDMXRUDtgL3xdwEGCpyxGKe9LnSk/eLD9r0oDn8bQiHSpVPBY4xUiK0jRgAVaAI+CKH1f8OPZTsbazJMldbFWxCSmDtHbbRgQSYlMbynOk5EXQFyUx48SMUQz2BbAZr2hW1I6H/QPLyWCn");
                addPart(null,"bXNjaAF4nG1R0Y7UMBBzkjabbtk7JCQQ0v3BqQ98D+Kh1wYU1G1Ktr2DX0fLFk8CLwhV0aSe2B4naPDOoJr7s4f6gHb0lyGFZQ1xBmCn/slPF+iPn1rcnf08dkuKX/2wxoTjEl986uY4erRh9efuErc0eLz335c4+3kN/dQlP8T5sqYtc6xo+IRXhfuH8HDepjUsUxj6NTz7fzjt0xamsVuFgbsvKW4c43MvvR94249j+A/pNIVvWxj/OgCPXDjBKJZ7p1HxU/vVaP6/roGjUlBonSF62/fc/ak0sXt2G8VLwhuoCkKAFAUjRZdioBVL5Sz0fgPy+sV1FRtiV12zXzs5XNHAUibvsqYtKgcYUXTZUoCmAMcMWPqeZAYjMxzyFNm6krZ2BKzkg2q4beRkhWKTjyhdoS5AnYGagC15bMljRe8o49RUYCR6OicheMpkKmn7TgR55ZhaAtQUtFnQFqUcxuYwRgnQFEDCWCVhW+7owd1JWod815Yv4fhGvLb9xlkc34eR1W/+/npr");
                addPart(null,"bXNjaAF4nG1R0Y7TMBCctRPXaegdEhII6f7glAe+B/GQSwwySuPgJnfw66g07K7hBaHIWmfWM7Njo8E7i2ruzwH0Ae0YLkOOyxrTDMBN/VOYLjAfP7W4O4d57JacvoZhTRnHJb2E3M1pDGjjGs7dJW15CHgfvi9pDvMa+6nLYUjzZc2bcpxohIxXhfuH8HDepjUuUxz6NT6Hfzjt0xansVuFgbsvOW08xudeej/wth/H+B/SaYrftjj+dQAeeeEES1zuvUHFH+1Xa/j/dQ0ciUBovWX0tu/a/UmGsXvuNsSXhDegCkKAFIKVYkqxMMSl8g5mvwG6fvG6ig1jV1Nzv/ZyuGIDxzK6U01XVA6woujVUoCmAEcFHPueZAYrMxx0CrWupG08A07yETW8beRkhWKjR8hUqAtQK1Az4EoeV/I40TvKODUrcCT29F5C8CmrVKbtOyPQpTGNBKhZ0KmgK0oaxmkYSwI0BZAwjiRsyzv24N1JWge9a8cv4fmN+Nr2G8/i+X04Mv0G/9R6bA==");
                addPart(null,"bXNjaAF4nG1Ry27cMAwcSZZWXneTAgVaFMgfBD7ke4oeHFstVHgtV2vn8evBdh2SSi5BYRCUh5whR0KNbwbV1B0D1B2aIZz6HOclpgmAG7v7MJ6gf/xscHUM09DOOf0J/ZIy9nN6DLmd0hDQxCUc21Nacx/wPTzNaQrTEruxzaFP02nJq3Aca4SMT4X7Rrg5ruMS5zH23RIfwgdOc7/GcWgXZuDqd04rrfGr49ozvnbDEP9DOozx7xqH9wnALQUOMIrStdeo6FPb2Wj6/2yBvVJQaLwh9LJtUn1RmrBrqtaKLglfoCowAZwUDCddkoFWlCrvoLcLIPGP4sxjCDtrS3XrubmiAY5k5CSarqjsYFjRy0gG6gLsBXA098A7GN5hJ1vI6IrL2hPg2J9WNR1r7qxQxkiL0hVsAawAlgBX/Ljix7HentexpECWaKb3bIK6jFCJtm2EQEJsajZgSdCJoCtKYsaJGaMYqAvAZpxis43lZdnKgUs7uWtHL+Hpjejatgvt4ul9yLJ6BQC5em0=");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/kl0KqOxScOAHcuDGv1AcvLGgRDl2cGwevw4hpmfMXqg9yDOemW51S0KNtw6+b44R5h3qNp4PYzpNaegBhK55iN0Z9sPHDW6OsW/3p3H4Eg/TMCLIfxxRpyke9+dhHg8R1Wn4Hsd9P7QRd1OcxmZK3+J+jIehP0/jrMhX8cdp6GM/pab7r7VZ8f/Y3hznbkqnLh2eoqkf5tS1+0kQuPk8DjP1fWqk9xO7pm3TE6Btl77OqX3cAXjPhedwEl7CGYYdnGW4y4CNkcZ94ZAtv4HlwnXljMEWzjHcsuXhl4UMZrkQaPCMwMpItmNWGo8Mr0Emi7qQsSvHPeMvDlnc6lDGoRcwZlUiwYgaAWlgIh9fBNjlKjK4/qgk7svaxQb2s8ITIXoCaTRTzrCy5GLNodAtpVCuhUoLOTVsRYMXDQX/rHLAS9uWLASj8iqmpUxm0G0yHTHW6LYsBC1kLOSrn3z1k4ufWnRknpSiyBeFmOCUUyhhy6InfXm0aS3nMhIGJQwrk5rJ1YwzUijXgpgJPNoCNTMimG2lVehZByO2eXHLRdjVgqOmkhfn9QWU64Mo5UFYhp1eqEG1Po1Kn0Ztamb3CvkLxpKONA==");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/kl0KqOxScOAHcuDGv1AcvLGgRDl2cGwevw4hpmfMXqg9yDOemW51S0KNtw6+b44R5h3qNp4PYzpNaegBhK55iN0Z9sPHDW6OsW/3p3H4Eg/TMCLIfxxRpyke9+dhHg8R1Wn4Hsd9P7QRd1OcxmZK3+J+jIehP0/jrMhX8cdp6GM/pab7r7VZ8f/Y3hznbkqnLh2eoqkf5tS1+0kQuPk8DjP1fWqk9xO7pm3TE6Btl77OqX3cAXjPhedwEl7CGYYdnGW4y4CNkcZ94ZAtv4HlwnXljMEWzjHcsuXhl4UMZrkQaPCMwMpItmNWGo8Mr0Emi7qQsSvHPeMvDlnc6lDGoRcwZlUiwYgaAWlgIh9fBNjlKjK4/qgk7svaxQb2s8ITIXoCaTRTzrCy5GLNodAtpVCuhUoLOTVsRYMXDQX/rHLAS9uWLAQ5IGMqpqVMZtBtMh0x1ui2LAQtZCzkq5989ZOLn1p0ZJ6UosgXhZjglFMoYcuiJ315tGkt5zISBiUMK5OaydWMM1Io14KYCTzaAjUzIphtpVXoWQcjtnlxy0XY1YKjppIX5/UFlOuDKOVBWIadXqhBtT6NSp9GbWpm9wr5C8ejjjU=");
                addPart(null,"bXNjaAF4nG1Sy5KUQBDMftLAzK6hxuwaevAHOHjzXwwPLLRGGwwgAz5+XXGwqnAvxh6aKqoqszO7GyXeGti+PkeodyjbeGmmNM5p6AH4rn6I3QX6w8cDbs6xb6txGr7EZh4meP6PE8o0x3N1GZapiSjG4Xucqn5oI+7mOE/1nL7FaorN0F/maRHkq/hjHPrYz6nu/msddvw/tjfnpZvT2KXmKZryYUldW82MwM3naVhI36eaez9xqts2PQE6dunrktrHHYD3tPAchsNLGEXhBKMp3DngoLhxHwzc9hvYVlpXmlE4whgKt9SysNtGDGpbCajwjICF4uxEWa4sHF6DmDTKwGNXGrcUf9GQxq0MORp6AaV2JRwUq2GQBEr4Y4OH3q4sg9YfkUT7Um3VnvouWEKwHk80kgmn31kytmYQZEsu5HuhkEJGGo6swbKGQH9aOGC5rXMqeD4grQpKc550kG2cjCitZFsqeCk4KmS7n2z3k7GfknU4S5SsyIbAJmjKCJRg2yYnvT7a1JrmHBF6IfQ7k5jJxIxRXMj3ApvxdLQBpWOxbOXIrSBn7RXbpovbVmYXC4Y05XRxVl5Avj+InB+EpnCSC1Uo9qdRyNMoVUnZvUD+Asi0jjY=");
                addPart(null,"bXNjaAF4nEVOy27EIAwcHCBp0977Ezn0e1Y9ZAOVkJKwSxJV++/bUhv6AFmDPZ5hYNA10Ou4eKhX9M5vUwqXPcQVgJ3Hs5830OmthV386nzC4yV++DSs0Xk81fcWjzR59OcjzG7YZYY+7H75Y8aQhvdx2mO64XkO1yO4Xw54wf9RaASoQgNSDLqzoPwFlPrkukNxUb4TMW86Khqds6zDVnUroH58+EJzpw3wQIZbq+qHwghHhVOEqtYMMjBFiDoq8WS1sbJgNHuxJydsJROqlnLmDqVKYuINGDazxcxWF873DQOAQyQ=");
                addPart(null,"bXNjaAF4nEVOy27EIAwcHCBp0977Ezn0e1Y9ZAOVkJKwSxJV++/bUhv6AFmDPZ5hYNA10Ou4eKhX9M5vUwqXPcQVgJ3Hs5830OmthV386nzC4yV++DSs0Xk81fcWjzR59OcjzG7YZYY+7H75Y8aQhvdx2mO64XkO1yO4Xw54wf9RaASoQgNSDLqzoPwFlPrkukNxUb4TMW86Khqds6zDVnUroH58+EJzpw3wQIZbK5RSIpQ/RM+cIlS1ZpCBKULUUYknq42VBaPZiz05YSuZULWUM3coVRITb8CwmS1mtrpwvm8D/0Ml");
                addPart(null,"bXNjaAF4nEWPzU7EMAyEJ7/bpXDlxiP0wPMgDt0mSJG67W6agnj3hWA7AipZTsbjL1M49AZ2Gc8R6hl9iNuU06WkdQHg5/EU5w365bWDP8clxIy7y/oR87CsIeK+nbd1z1NEn0o8/11Oe5rDUNiAfkx5eBunsuZPPI4hpJLe45DjtC5byTsP8DCn657CLwB4wv+nYLjp1gy0omY7D12/AakvqhsUla43rWnuOjLCyp6tlVfgG+EAw71zwFH0I+tysuTW/IaC5bF2dPUyUvQeORUDWwyxKLGLYERwGvKs0CQ6Q82BmnGWkISm9B3nRduVvLWSAin5I004OAJ6AfpGkuxWshvFAmX/AY/rTMI=");
                addPart(null,"bXNjaAF4nEWPzU7EMAyEJ7/bpXDlxiP0wPMgDt0mSJG67W6agnj3hWA7AipZTsbjL1M49AZ2Gc8R6hl9iNuU06WkdQHg5/EU5w365bWDP8clxIy7y/oR87CsIeK+nbd1z1NEn0o8/11Oe5rDUNiAfkx5eBunsuZPPI4hpJLe45DjtC5byTsP8DCn657CLwB4wv+nYLjp1gy0omY7D12/AakvqhsUla43rWnuOjLCyp6tlVfgG+EAw71zwFH0I+tysuTW/IaC5bF2dPU8UoreI6diYIshFiV2EYwITkOeFZpEZ6g5UDPOEpLQlL7jvGi7krdWUiAlf6QJB0dAL0DfSJLdSnajWKDsP5CGTMM=");
                addPart(null,"bXNjaAF4nG1Qy06EQBCsGWAWFtd48uTRKwe/x3hgYUzG8HIAjb9uVrG6yd4Maaanqru6a1DgLkE61L2HeULZ+rmJYVrCOABwXX323Qz7/HLEbe+Htpri+OabZYw4TuOnj9Uwth5OOB9xs2PzuMbGowyL76+Xh37tljB1oamX8OGr6JtxmJe4qlh5XkPXVou0o6xDrF5rIb5wX7dt+Kfj1IX3NbRXeeCRgRMSA4MytzDbL+PbWFhh5DBIrCZ6JLCGR5o7WNZC44dxYd+F2MWyBFmeStW2wakMM9V0u8oBifB5BhSKFztwVCBDyp2MDJP3hDD88cmFtgfmzghlcqaFVKZyTeVjibFcYAcyBTJrdQ9dQP04sVLIOhkVaIlT8lxMsCrRSrZtGxFoqE3Lwcgo6FTQ7UpqxqmZxAhQ7ICYcSYlXzLjDGZ86j8Mm2V2");
                addPart(null,"bXNjaAF4nG1Qy07DMBAcO4mbNBRx4sSRaw58D+KQJkYyygsnAfHrqBBmN+oNVVtvZmbHO0aBuwTpUPce5gll6+cmhmkJ4wDAdfXZdzPs88sRt70f2mqK45tvljHiOI2fPlbD2Ho44XzEzY7N4xobjzIsvr9+PPRrt4SpC029hA9fRd+Mw7zEVc3K8xq6tlpkHGUdYvVaC/GF+7ptwz8Tpy68r6G92gOPLJyQGBiUuYXZflnfxsIKI4dBYrXRI4E1PNLcwVILrR/WhXMXYhdLCbI8FdW2wakNO/V0u8sBifB5BhSKFztwVCBDyp2MXCbvCWH4xycX2h7YO0PGmJxtIcpUlKn8KDGWC+xApkBmre6hC2geJ1EKWSejAyPxljyXEFQlquTYthGBlsa0vBgZDZ0aut1JwzgNkxgBih2QMM6k5Et2vIMdn/oPDVFldw==");
                addPart(null,"bXNjaAF4nG1R266UQBCsuTAMi+sxR6Mx8ReI8XuMDxwYkzlhAVnw8utmPVjd6IvxodNN9VR1F40abxz82F4SzAfUfbp2S57XPI0AwtA+pOEK+/FTjeeXNPbNvEyPqVunBad5+paWZpz6hDqv6dJcp23pEt6m7/M0pnHN7dAsqZvG67psygmikRY8O7h/CO8u27Dmechdu+av6R9O/bDloW9WYaBu89J8bqXxA6/bvs//YZyH/GXL/V954D0D93CSXkkyOMMZprtI9/D7TtTsN2eJvSiAkzGwqKOgT+x65p/GErtjtzIOBV7C+ENTkoGTZI/EggB8DLD7E6Dxi3GTMcRutmC/iJ4MGR8oo5VqhkOlBBdyiDpSgOoATgoE7nCWHbzsUPLLqga8tG0kEIy0TMWykpcFdEyhT4z1OpZAUKAgUB5+ysNPKX5OskfhKWlY+RjFBF85pZK270SgoTatlW0oGFQwHEpqplQzzghQHYCYCfy1ETUrzmB1llbUfx14iUrutN9EXS047lTxTl6OyEpPW8lpfwOoenu8");
                addPart(null,"bXNjaAF4nG1R266UQBCsuTAMi+sxR6Mx8ReI8XuMDxwYkzlhAVnw8utmPVjd6IvxodNN9VR1F40abxz82F4SzAfUfbp2S57XPI0AwtA+pOEK+/FTjeeXNPbNvEyPqVunBad5+paWZpz6hDqv6dJcp23pEt6m7/M0pnHN7dAsqZvG67psygmikRY8O7h/CO8u27Dmechdu+av6R9O/bDloW9WYaBu89J8bqXxA6/bvs//YZyH/GXL/V954D0D93CSXkkyOMMZprtI9/D7TtTsN2eJvSiAkzGwqKOgT+x65p/GErtjtzIOBV7C+ENTkoGTZI/EggB8DLD7E6Dxi3GTMcRutmC/iJ4MGR8oo5VqhkOlBBdyiDpSgOoATgoE7nCWHbzsUPLLqga8tG0kEAw7xlQsK3lZQMcU+sRYr2MJBAUKAuXhpzz8lOLnJHsUnpKGlY9RTPCVUypp+04EGmrTWtmGgkEFw6GkZko144wA1QGImcBfG1Gz4gxWZ2lF/deBl6jkTvtN1NWC404V7+TliKz0tJWc9jepZnu9");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/EpZaqOxScOAHfODGv1AcvLG2SpRjB8fh8esQYnrG7IXagzzjmelWtyTUeO/gh/YQYT6g7uJpP6XjnMYBQOjbh9ifYD993uDFIQ5dc5zGL3E/jxOC/McJdZrjoTmN52kfUR3H73FqhrGLuJvjPLVz+habKe7H4TRPZ0W+iT+O4xCHObX9f63Niv/H9u5w7ud07NP+OZr64Zz6rpkFgbpNU/PYSuMndm3XpWcQ2z59PafuiR74yIVbOAmv4QzDDs4y3GXAxkjjvnDIlt/AcuG6csZgC+cYbtjy8MtCBrNcCDR4SWBlJNsxK41Hhrcgk0VdyNiV457xF4csbnQo49ArGLMqkWBEjYA0MJGPLwLschUZXH9UEvdl7WID+1nhiRA9gTSaKWdYWXKx5lDollIo10KlhZwatqLBi4aCf1Y54KVtSxaCUXkV01ImM+g2mY4Ya3RbFoIWMhby1U+++snFTy06Mk9KUeSLQkxwyimUsGXRk7482bSWcxkJgxKGlUnN5GrGGSmUa0HMBB5tgZoZEcy20ir0rIMR27y45SLsasFRU8mL8/oCyvVBlPIgLMNOL9SgWp9GpU+jNjWze4X8Baf1jN4=");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/EpZaqOxScOAHfODGv1AcvLG2SpRjB8fh8esQYnrG7IXagzzjmelWtyTUeO/gh/YQYT6g7uJpP6XjnMYBQOjbh9ifYD993uDFIQ5dc5zGL3E/jxOC/McJdZrjoTmN52kfUR3H73FqhrGLuJvjPLVz+habKe7H4TRPZ0W+iT+O4xCHObX9f63Niv/H9u5w7ud07NP+OZr64Zz6rpkFgbpNU/PYSuMndm3XpWcQ2z59PafuiR74yIVbOAmv4QzDDs4y3GXAxkjjvnDIlt/AcuG6csZgC+cYbtjy8MtCBrNcCDR4SWBlJNsxK41Hhrcgk0VdyNiV457xF4csbnQo49ArGLMqkWBEjYA0MJGPLwLschUZXH9UEvdl7WID+1nhiRA9gTSaKWdYWXKx5lDollIo10KlhZwatqLBi4aCf1Y54KVtSxaCHJAxFdNSJjPoNpmOGGt0WxaCFjIW8tVPvvrJxU8tOjJPSlHki0JMcMoplLBl0ZO+PNm0lnMZCYMShpVJzeRqxhkplGtBzAQebYGaGRHMttIq9KyDEdu8uOUi7GrBUVPJi/P6Asr1QZTyICzDTi/UoFqfRqVPozY1s3uF/AWpBozf");
                addPart(null,"bXNjaAF4nEVOy04EIRAsmseMjnv3J+bg9xgP7AwmJAzszkPjv69iA26EVJruqi4KGr2EinZxEC8YZrdNq7/sPkUAJtizCxvo9a2DWVyc3YrHS/p06xjT7PDU3ls61slhOB8+zONeZhj87pY7c4r2w4bx3U57Wr9wCv56+PnOAs/4PwKyFGpFggQX1RtQ/gEqvhk3CAblGxHzuqe6o3Iucpi23ZUi/nz4QnGnNPBAmlsj2oeFKRxVrhjKNpB1oOsi2qjGK1JpikAr9mJPTtiVTGi7lDN3qKiJiRXQbGaqmWkunO8XLjJD/Q==");
                addPart(null,"bXNjaAF4nEVOW07EMAycOI8Wyv5ziX5wHsRHtw1SpDTd7QPE3ReCnbAikeXY88jAotUwaZg91Au6yW/jGi57WBIAF4ezjxvo9a2Bm32a/IrHy/Lp1z4tk8dTfW/LsY4e3fkIcep32aELu5/vyCkNH0Ps34dxX9YvnGK4HmG6o8Az/o+Clka1aZDiZloHyj9AqW+uGxQX5RsR47alojE5Cx2uqhtp6s+HLwxPxgIPZHl0AiklQvlD9IyJoa4LXRa2CFFXJZ5QtROCNezFnpywkUyoWsqZJ5QqiYkZsGzmipmrLpzvFy6xQ/4=");
                addPart(null,"bXNjaAF4nEWPzU7DMBCEx+ufpoReufEIOfA8iEMaG8lSYrdOUsS7F8J6IyDSau3Z2c8TWLQaJvVTgHpB68M8lHhZYk4A3NifwziDXt8auCkkHwoeLvkjlC5lH/C4n+e8liGgjUuY/i7nNY6+W6oBp9Tf+rF774cll0889d7HJd5CV8KQ07yUtQ5wGuN1jf4XATzj/1PQtdHeNEhxM40Dbd+A1BfXHYqLtjsRz23DRhjZM9tWV+B2wgG69sYCR9GPVZeTYTfVNxRMHZPlq5OR4vfYqSpwjyEWErsIWgRLkGeFJtErVB+4aWsYyWhO39S82Hcl77axAin5I2IcLAOdAN1OkuxGsmtVBc7+A/hSTZs=");
                addPart(null,"bXNjaAF4nEWPzU7DMBCEx79NCb1y4xFy4HkQhzQ2kqX8tI5TxLsXwu5aQKTV2rOznydwaA3s3E8R6gVtiOuQ06WkZQbgx/4cxxX69a2Bn+IcYsbDZfmIuZuXEPFYz+uy5SGiTSVOf5fzlsbQFTbgNPe3fuze+6Es+RNPfQippFvschyWeS154wFOY7puKfwigGf8fwqGm67NQCtqtvHQ+zcg9UV1h6LS+11rmruGjLCyZ/edV+Ar4QDDvXHAUfQj63Ky5Nb8hoLlsXZ09TxSit4jp2JgjSEWLXYRjAhOQ54VmkRnqDlQM84SktCUvuG8qLuSd99JgZT8kSYcHAG9AH0lSXYr2Y1igbL/APjtTZw=");
                addPart(null,"bXNjaAF4nG1Qy07EMAycpE023bKIEyeOXHvgexCHbBOkoL5I20X8OlootituqHLjzNhjT1DhrkA5+D5CPaEOcW5zmpY0DgBs58+xm6GfX4647eMQmimPb7FdxozjNH7E3AxjiLDMxYybHZvHNbcRdVpi/3d56NduSVOXWr+kS2xybMdhXvIqYvV5TV1oFm7HafAX3zWvnqlP3PsQ0j89py69ryn8DQAeKXBCoaBQOw21/VB8KQ3NDB8KhZZEjgJa0VE6C021kPimuFLflbCrphIYV3LVtsGKDGWiaXeVAwrmnQEqwasdOApgUNJOiofxi4IZ+tGjM60PlFvFlHKUVlxZ8rXkj0q0pgV2wAhgtJY9ZAHxY9lKxesYUiBLNMU5NkFVhVRS27YRAgmxqWkwDAlaEbS7kpixYqZQDFQ7wGasKomveSlSPvBT/wLULWZP");
                addPart(null,"bXNjaAF4nG1Qy07DMBAcO7HrNBRx4sSRaw58D+LgxkYyygsnKeLXUSHsbsQNVVtvZmbHO0aFuwLl4PsI9YQ6xLnNaVrSOACwnT/HboZ+fjnito9DaKY8vsV2GTOO0/gRczOMIcIyFzNudmwe19xG1GmJ/d/HQ792S5q61PolXWKTYzsO85JXMavPa+pCs/A4ToO/+K559Ux94t6HkP6ZOXXpfU3h7wLgkQonFAoKtdNQ2w/Vl9LQzPChUGhp5CigFR2ls9CkhdQ31ZXmroRdNUlgXMmqbYMVG+rE0+4uBxTMOwNUglc7cBTAoKSdFF/GLwpm6I8enWl9oN4qYpRy1FasLFlZ8o8kWtMCO2AEMFrLHrKA5LEcpeJ1DDlQJLrFOQ5BqkKUNLZthEBKYmq6GIYMrRja3UnCWAlTKAaqHeAwVpXE17wUOR/4qX8B1ONmUA==");
                addPart(null,"bXNjaAF4nG1RXY+UQBCs+WAYFtc1d0Zj4l/YGH+P8YGDMRnDArJw6l8362F1oy8XHybdVHdVd9Go8dbBD80lwXxE3aVrO+dpyeMAIPTNQ+qvsJ8+13h5SUN3nubxa2qXccZhGr+n+TyMXUKdl3Q5X8d1bhPepR/TOKRhyU1/nlM7DtdlXpUTRCPNeLFz/xLeX9Z+yVOf22bJj+kZp35Yc9+dF2HgODSPVP3SSOkn3jRdl//DOfb525q7fwOAD3y4g5PwWoLBEc4wnCL9w28bUbPdnCX2qgAOxsCijoI+seoZfxlL7MRqZRwK3MP4XVOCgZNg98CEAHwMsNsToO83303GELvZgvUiejJkfKCMZqoZdpUSXMgh6kgBqh04KBC4w1F28LJDyS+rGvBStpFAMFIyFdNKOgvomEJbrPU6lkBQoCBQ7n7K3U8pfg6yR+EpaZj5GMUEu5xSSds2ItCnNq2VbSgYVDDsSmqmVDPOCFDtgJgJ/LURtSxF5SjXEeCkJcNO3mm7ibpacNyp4p28HJGZnraS0/4B6eF8lQ==");
                addPart(null,"bXNjaAF4nG1RXY+UQBCs+WAYFtc1d0Zj4l/YGH+P8YGDMRnDArJw6l8362F1oy8XHybdVHdVd9Go8dbBD80lwXxE3aVrO+dpyeMAIPTNQ+qvsJ8+13h5SUN3nubxa2qXccZhGr+n+TyMXUKdl3Q5X8d1bhPepR/TOKRhyU1/nlM7DtdlXpUTRCPNeLFz/xLeX9Z+yVOf22bJj+kZp35Yc9+dF2HgODSPVP3SSOkn3jRdl//DOfb525q7fwOAD3y4g5PwWoLBEc4wnCL9w28bUbPdnCX2qgAOxsCijoI+seoZfxlL7MRqZRwK3MP4XVOCgZNg98CEAHwMsNsToO83303GELvZgvUiejJkfKCMZqoZdpUSXMgh6kgBqh04KBC4w1F28LJDyS+rGvBStpFAMKwYUzGtpLOAjim0xVqvYwkEBQoC5e6n3P2U4ucgexSekoaZj1FMsMsplbRtIwJ9atNa2YaCQQXDrqRmSjXjjADVDoiZwF8bUctSVI5yHQFOWjLs5J22m6irBcedKt7JyxGZ6WkrOe0f6s18lg==");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/YpYCKrsUHPiBHLjxL9QevLGoEuXYwXEW+HUIMT1j9kLtQZ7xzHSrWxJqfHDwQ3uIMB9Rd/G0n9JxTuMAIPTtQ+xPsJ/vN3hxiEO3O07j17ifxwlB/uOEOs3xsDuN52kfUR3H73HaDWMXcTvHeWrn9Bh3U9yPw2mezop8G38cxyEOc2r7/1qbFf+P7f3h3M/p2Kf9czT1wzn13W4WBJqhfSTbl1ZaP7Ftuy49g2n69O2cuqcNgE9ceAUn4Q2cYdjCWYbbDNgYadwVDtnyG1guXFfOGDRwjuGGLQ+/LGQwy4VAg5cEVkayLbPSeGR4BzJZ1IWMXTnuGX9xyOJGhzIOvYYxqxIJRtQISAMT+fgiwC5XkcH1RyVxX9YuNrCfFZ4I0RNIo5lyhpUlF2sOhW4phXItVFrIqaERDV40FPyzygEvbVuyEIzKq5iWMplBt8l0xFqj27IQtJCxkK9+8tVPLn5q0ZF5UooiXxRiglNOoYQti5705cmmtZzLSBiUMKxMaiZXM85IoVwLYibwaAvUIsqIlUZahZ51MGKbF7dchF0tOGoqeXFeX0C5PohSHoRl2OqFGlTr06j0adSmZnankL9J3o23");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/YpYCKrsUHPiBHLjxL9QevLGoEuXYwXEW+HUIMT1j9kLtQZ7xzHSrWxJqfHDwQ3uIMB9Rd/G0n9JxTuMAIPTtQ+xPsJ/vN3hxiEO3O07j17ifxwlB/uOEOs3xsDuN52kfUR3H73HaDWMXcTvHeWrn9Bh3U9yPw2mezop8G38cxyEOc2r7/1qbFf+P7f3h3M/p2Kf9czT1wzn13W4WBJqhfSTbl1ZaP7Ftuy49g2n69O2cuqcNgE9ceAUn4Q2cYdjCWYbbDNgYadwVDtnyG1guXFfOGDRwjuGGLQ+/LGQwy4VAg5cEVkayLbPSeGR4BzJZ1IWMXTnuGX9xyOJGhzIOvYYxqxIJRtQISAMT+fgiwC5XkcH1RyVxX9YuNrCfFZ4I0RNIo5lyhpUlF2sOhW4phXItVFrIqaERDV40FPyzygEvbVuyEOSAjKmYljKZQbfJdMRao9uyELSQsZCvfvLVTy5+atGReVKKIl8UYoJTTqGELYue9OXJprWcy0gYlDCsTGomVzPOSKFcC2Im8GgL1CLKiJVGWoWedTBimxe3XIRdLThqKnlxXl9AuT6IUh6EZdjqhRpU69Oo9GnUpmZ2p5C/Su+NuA==");
                addPart(null,"bXNjaAF4nEVOXU/EIBAcPq9ajffon+iDv8f4QAsmxJaetI1//hSXxYuQzbIzO8PAoFPQyS0B4gW9D9uU42WPawJgZzeGeYN8fTvBLiH5kHF/Wb9CHtLqAx7ae1uPPAX04xFnP+wVQx/3sNyYp92lj+HdjTlObl8zHuf4eUR/44Fn/B8BVZtsTUEKarqzkOUH4PqmukJQyXKVknjTaVLoUmBZS68qo4ldTrWJPz+60DRpA9xJQ6MV7ePKVE4yV41VAxQDZxaiQRyzripbF4wmL/KkpF3NhqblbKUQAi5OL2kLhgwtG9rmRBl/AWeURt0=");
                addPart(null,"bXNjaAF4nE1PW07EMAyc5rUpBcEegCP0g/MgPvoIUkQfS9qKyy8E29EKIlm2x+PxBBaNhlm6OaB6QTOGbUjxssd1AeCmrg/TBvX65uHmsIwh4e6yfoXULusYcF/qbT3SEPAwxc8jjre26Y84je3OFDRxD/Nt8rh3y0f73vUpDt2+JpwFSOEfBDzj71XQnFRJGqqiZLyDyj+AxDfFFRWFylelaG69zhlO9kzOvEKdKJygOXtr4AWvGZfKEJu2LXXG0kDRETgZVScqa6axPFGUUPiYLoAW4EmxDIqaWGdRumig6WJN7g259+wXZVf85kwIJORHyhLPkqATQVeUyDvjnvBzxQB5/wVnzk42");
                addPart(null,"bXNjaAF4nFVQy07DMBAcP+MkFFq+gWsOfA/ikCZGMuSFm4p/R6Vm11EliLQa7+x4vBOU2CvoqR09xDPq3p+6GJY1zBMAO7RHP5wgX14r3I9+6pslzu++W+eIapm/fGymufewPPMRdxt3ms+x89gN4fMc+ltbh9WPt+ZxiWH0TfRv7TGGrmXH+ngOQ9+s7IGHtZ0+mj/TQyb+XQCeqLCDEhConYZI15TA+A2VrkJCsoJBQMl8yKAgBYF2FjJdgVw/VBe6eyHuIkkC4xT52WyhU8p+dnMooDSBMxou8yWUIagMUApLS+wg+CH6CrAUhjrNY+mIsIJHoqRjyUrNrSFgiZT0+EaYTOylzHvkBXIWyzE0r0MrlBSH3nSOA5BKZSVdT4kY5MoRpSGdIUObDe3mVPDulsIAB8FEuVlXbC0MzWsaFYKj0O/+BcPBZHE=");
                addPart(null,"bXNjaAF4nDVQSVLEMBCTHS/JhGF5AE/IgfdQHLKYKhdZBicp/sl7BoJsD4e2utRSt8qocS6g5nZyEC+oB7f2wV82v8wAzNh2blwhX98q1P0yr1vY+20JqP3mpmZd9tA7mMnNgws4XZYvF5p5GRzucn9T3G/t/NG06+qmbqSy7nY/Ds0WNXhIw/e2C75v4/Lz6D93P/ybgWdAoNIC30QJq4EnKVFwhAgCRQSZgU18VGkgj18g1Q/rCsGSx1UqznWpkl0dB19NlRLRbvIWi0JD4wSSUUheU8dbCooBKmlJGBFHlOiEhDiTDGcyYRLxSMLmtDantTFtma4o7or3VGljxKg6DnZIlcPzgBTUaC4zaZnJdkv7SQiUt19hsPxT8g+lWFQM");
                addPart(null,"bXNjaAF4nE1RW3LVMAw9VpzEefYWaLuKMMN6GD7ycDspedw6ybBUNsJnIUg2TPuhSDmSjnQsPOAugl7a2UJ9QTHYrXfjdR/XBUAytZ2dNtDXbzXuu3Yb+6bdNjt3k3XNvA7HZFHPdhmaq1ufbb+vDvrx2CyKfl223R0eKsbdzs22Hq63uFzXH9y9rINtptY9WSTCYB3ytwzKEP/rqfd2+f42GkV3jNPQ7FKDG598bDs39q2Mu/WAs++gahpfjnH4zwd8BggfAIUYtcQpDFTErowVfkJzlMfARxVzVBswYjySqgQZ5HXYKUTiKLgIlLLTJgGdfwBvv9leodjofCXD+dhEkJH6POXr+ZLAkCLS7AzPyUgAHiGUuRFC4jJSGQpU4K5ctsj5j2S7HNp3FQwkSlKq5DCTykJ+NTspISYuA1B64MJAFRRVQVEliip2UczrsKhKRIkMWfw8OYI3L5A052MmSjxRElpTbs2VpETNLQmWMWaURLnPEm7k2TWfIDxtxIA/AMlZaiOK9fmLV9WcCQdRuMjJgE98QPoLwSd3DA==");
                addPart(null,"bXNjaAF4nDVQSVLEMBCTHS+ZCdsHeEIOvIfikMUUrsoy2EnxTR40EGR7OLTVpZa6VUaD+wpq6WYH8YJmdHEI/rL5dQFgpq53U4R8fTuhGdYlbmEftjWg8Zub27juYXAws1tGF3C+rF8utMs6OtyV/qZ4iB/+0nYxurmfqGz63U9juyUNHvPwveuDH7q0/H7yn7sf/83AMyBw0gLfRAmrgScpUXGEBAJVAlmATXpUbSCPXyDXD+sKwZLHVSrOda2yXR0HX02VEsluyhaLSkPjDJJJSF5Tx1sKigFO0pIwIo0o0RkJaSYZzhTCZKIiYUtaW9LalLbOVxR3pXuqtiliUh0HO+Qq4XlACmo0l5m8zBS7pf0sBOrbrzBY+Sn5B6yIVAU=");
                addPart(null,"bXNjaAF4nE1RWZLVMAxsL0mcdWZYTxGqOA/FRxYPmMrysJPiqFyEz4Eg2VAzH4qUVmtpC+/xVkFvw2ohPqKebZi8ux1u3wDkyzDaJUB++tzh3TgEN/VDCHYdF+v7dZ/PxaJb7Tb3N79/s9Oxe+jHM1jU076Fw58Rqt1h1z7sp58s7m/7D6re9tn2y+C/WOTcwXpUzxk0Kf5X04Wv7vY8GvV4umXuD+bgLiYfh9G7aeBxDxHw9gXULu776eb//YAPgMQrQCBDx3EBA6HINZnAT2iKqgx4LTKKOgNCTEQKkaMEvw45AcVOJqcgC3La5JDXHyDab7InCDJ5PUlD+cwo8Eh9XfyN/fLUoYDS5AzNKSUDNIJbVoYbSqJJUaJGC6qqeIuK/iRvV0HHqpqAXHBKNBSWzKz5V5NjiqTGTQKaCCgC2qSoTYpaVtSSUxmtQ6JaFsUyePHrogjRokDJzIyYJpbmqbQgoBKaIlbzEEklkwRHVcxK3PGzazpBelpFQDyA5LN0hhXr6xetqimTDiJwzycD3tAB5V/jC3ch");
                addPart(null,"bXNjaAF4nDVQSVLEMBCTHS/JhGF5AE/IgfdQHLKYwlVZBicp/sl7BoJsD4e2utRSt8qocS6g5nZyEC+oB7f2wV82v8wAzNh2blwhX98q1P0yr1vY+20JqP3mpmZd9tA7mMnNgws4XZYvF5p5GRzucn9T3E+u/2jadXVTN1JZd7sfh2aLGjyk4XvbBd+3cfl59J+7H/7NwDMgUGmBb6KE1cCTlCg4QgSBIoLMwCY+qjSQxy+Q6od1hWDJ4yoV57pUya6Og6+mSoloN3mLRaGhcQLJKCSvqeMtBcUAlbQkjIgjSnRCQpxJhjOZMIl4JGFzWpvT2pi2TFcUd8V7qrQxYlQdBzukyuF5QApqNJeZtMxku6X9JATK268wWP4p+Qd2NVPq");
                addPart(null,"bXNjaAF4nE1RWXLVMBBsjWVbXvMCJDmFqeI8FB9elMSUlxfZLo7KRfgMmBkJKvmY6lHP2ho84C6CXtrZQn1BMditd+N1H9cFQDK1nZ020NdvNe67dhv7pt02O3eTdc28DsdkUc92GZqrW7/bfl8d9OOxWRT9umy7OzxVjLudm209XG9xua4/uHpZB9tMrXuySKSDdcjfIiiD/6+GZ/TPb6NRdMc4Dc0uObjxwce2c2PfyrhbTzj7jqqm8eUYh//9gM8A4QOgEKMWP4WBihjKWOEnNHt5DHxUMXu1ATPGM6lKkEF+h0EhEqAAEShl0CYBnX8Ab7/ZXqHY6Hwlw/HYEGSkPk/fKwnVKSLNYHhGRkJwe2mXm4gbEaeRtFAZClTgyly2yPlFsl0O7SsLJhIlIVWym0lmIU/NICnEzctAlJ64MFEFRVVQVImiiiGKeSUWVYkokSGLnyd78OYFkmTGnGl8aRJKUyZypdkTRbc+KZMkJV7uo4Qb+XbNJwhfGzHhD0ByltqIan3+4lU1R8JBFC5yMuATH5D+AoOEeOA=");


                bases.cores.sort(b -> b.tier);
                bases.parts.sort();
                bases.reqParts.each((key, arr) -> arr.sort());
            }
            basegen.generate(tiles, enemies.map(r -> tiles.getn(r.x, r.y)), tiles.get(spawn.x, spawn.y), state.rules.waveTeam, sector, difficulty);
            //Log.info("Core size in PlanetGenerator: "+bases.cores.size);
            //Log.info("Core schematic name: "+bases.cores.getFrac(difficulty).schematic.name());
            state.rules.attackMode = sector.info.attack = true;
        }
        else
        {
            state.rules.winWave = sector.info.winWave = (int)(20f*Mathf.pow(10,difficulty));
//            Log.info("Difficulty: "+difficulty);
//            Log.info("Rules Win Waves: "+state.rules.winWave);
//            Log.info("Info Win Waves: "+sector.info.winWave);
        }

        float waveTimeDec = 0.4f;

        state.rules.waveSpacing = Mathf.lerp(60 * 65 * 2, 60f * 60f * 1f, Math.max(difficulty - waveTimeDec, 0f));
        state.rules.waves = sector.info.waves = true;
        state.rules.env = EC620Planets.ec620.defaultEnv;
        state.rules.enemyCoreBuildRadius = 600f;

        //spawn air only when spawn is blocked
        state.rules.spawns = Waves.generate(difficulty, pgRand, state.rules.attackMode, state.rules.attackMode && spawner.countGroundSpawns() == 0);
        //state.rules.spawns = Waves.generate(difficulty, new Rand(sector.id), state.rules.attackMode, state.rules.attackMode && spawner.countGroundSpawns() == 0, naval);

    }
    private void addPart(Content c, String schematic)
    {
        Schematic schem=Schematics.readBase64(schematic);
        BaseRegistry.BasePart part=new BaseRegistry.BasePart(schem);

        part.tier = schem.tiles.sumf(s -> Mathf.pow(s.block.buildCost / s.block.buildCostMultiplier, 1.4f));
        if(c==null)
        {
            bases.parts.add(part);
        }
        else
        {
            part.required=c;
            bases.reqParts.get(c, Seq::new).add(part);
        }
    }
    private void addCorePart(int index,String schematic)
    {
        Schematic schem=Schematics.readBase64(schematic);
        BaseRegistry.BasePart part=new BaseRegistry.BasePart(schem);

        for(Schematic.Stile tile:schem.tiles)
        {
            if(tile.block instanceof CoreBlock)
            {
                part.core=tile.block;
                break;
            }
        }
        if(part.core==null) throw new IllegalArgumentException("There is no core in the schematic!");
        part.tier = schem.tiles.sumf(s -> Mathf.pow(s.block.buildCost / s.block.buildCostMultiplier, 1.4f));
        if(index<0) bases.cores.add(part);
        else bases.cores.insert(index,part);
    }
    @Override
    public void postGenerate(Tiles tiles){
        if(sector.hasEnemyBase()){
            basegen.postGenerate();
        }
    }

    private Block nearLiquid(int x,int y,int r)
    {
        for(Block b:liquids)
        {
            if(near(x,y,r,b)) return b;
        }
        return null;
    }

}