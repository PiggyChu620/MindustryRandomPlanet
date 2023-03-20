package ec620;

import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.scene.ui.Dialog;
import arc.struct.Seq;
import arc.util.*;
import ec620.content.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.type.ItemStack;
import mindustry.ui.Fonts;
import mindustry.ui.dialogs.*;

import java.io.IOException;


public class EC620JavaMod extends Mod
{
    public EC620JavaMod()
    {

        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                Dialog.DialogStyle dd=new Dialog.DialogStyle();

                dd.titleFont= Fonts.def;
                dd.titleFontColor=new Color(1,0,0);
                BaseDialog dialog = new BaseDialog("Random Planet", dd);
                dialog.cont.add("Welcome to Random Planet v0.1",Color.green,1.2f).row();
                dialog.cont.add("This mod randomly generate a planet and its sectors, no presets, nothing.",Color.cyan).row();
                dialog.cont.add("If you like some varieties and changes, then this mod is for you.",Color.cyan).row();
                dialog.cont.add("I combined both Serpulo and Erekir techs together as best as I could,",Color.cyan).row();
                dialog.cont.add("plus some of my own creations, in general, it's a hard game,",Color.cyan).row();
                dialog.cont.add("some of the sectors (especially Eradication maps) might even be unbeatable,",Color.cyan).row();
                dialog.cont.add("so if you're a newbie, I recommend you to play the original games",Color.cyan).row();
                dialog.cont.add(" and get familiar with the contents first.",Color.cyan).row();
                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                //dialog.cont.image(Core.atlas.find("example-java-mod-frog")).pad(20f).row();
                dialog.cont.button("OK", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
        Events.on(PlayEvent.class, event -> {
            //Log.info("Listening to PlayEvent: Sector "+Vars.state.rules.sector.id);
            if (Vars.state.rules.sector != null && Vars.state.rules.sector.id == 0)
            {
                Vars.state.rules.sector.planet.allowLaunchLoadout = false;
                Vars.state.rules.loadout = ItemStack.list(Items.copper,100);
            }

        });

        Log.info("Loaded EC620JavaMod constructor.");
    }

    @Override
    public void loadContent()
    {
        Log.info("Loading some example content.");

        EC620Items.load();
        EC620Blocks.load();
        EC620Planets.load();
        //EC620SectorPresents.load();
        EC620TechTree.load();
        Log.info("All contents loaded.");
    }

}
