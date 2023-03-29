package ec620.content;

import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.type.UnitType;
import mindustry.world.meta.Env;

import java.lang.reflect.Field;

import static mindustry.content.UnitTypes.aegires;
import static mindustry.content.UnitTypes.elude;

public class EC620Units
{
    public static void load()
    {
        for(Field f: UnitTypes.class.getDeclaredFields())
        {
            f.setAccessible(true);
            Object fv;
            try
            {
                fv = f.get(UnitTypes.class);
            }
            catch (IllegalAccessException e)
            {
                continue;
            }
            try
            {
                ((UnitType)fv).envDisabled=Env.none;
            }
            catch (Exception e)
            {

            }
        }
        //elude.envDisabled= Env.none;
    }
}
