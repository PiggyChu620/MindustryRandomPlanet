package ec620;

import arc.*;
import arc.util.*;
import ec620.content.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;



public class EC620JavaMod extends Mod
{

    public EC620JavaMod(){
        Log.info("Loaded EC620JavaMod constructor.");

        EC620Items.load();
        EC620Blocks.load();
//        EC620Planets.load();
//        EC620SectorPresents.load();
//        EC620TechTree.load();
        Log.info("All contents loaded.");
        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("frog");
                dialog.cont.add("behold").row();
                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                //dialog.cont.image(Core.atlas.find("example-java-mod-frog")).pad(20f).row();
                dialog.cont.button("I see", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void loadContent(){
        Log.info("Loading some example content.");
    }

}
