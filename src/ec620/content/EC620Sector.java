package ec620.content;

import mindustry.graphics.g3d.PlanetGrid;
import mindustry.type.Planet;
import mindustry.type.Sector;

public class EC620Sector extends Sector
{

    public EC620Sector(Planet planet, PlanetGrid.Ptile tile)
    {
        super(planet, tile);
    }
    @Override
    public boolean unlocked()
    {
        return true;
    }
}
