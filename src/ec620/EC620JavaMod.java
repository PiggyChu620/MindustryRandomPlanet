package ec620;

import arc.*;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.scene.ui.Dialog;
import arc.struct.Seq;
import arc.util.*;
import ec620.content.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.Platform;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Fonts;
import mindustry.ui.dialogs.*;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.UnitFactory;

import java.io.IOException;
import mindustry.Vars.*;

import static mindustry.Vars.content;

public class EC620JavaMod extends Mod
{
    //public static EC620NameGenerator nameGenerator;
    public EC620JavaMod()
    {

            //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
              if(!Core.settings.getBool("ec620.dontShowAgain"))
              {
                    //show dialog upon startup
                    Time.runTask(10f, () -> {
                        Dialog.DialogStyle dd=new Dialog.DialogStyle();

                        dd.titleFont= Fonts.def;
                        dd.titleFontColor=new Color(1,0,0);
                        BaseDialog dialog = new BaseDialog("Random Planet", dd);
                        dialog.cont.add("Welcome to Random Planet v0.4.5.1",Color.green,1.2f).center().row();
                        dialog.cont.add("Please see Settings for customization",Color.cyan).center().row();
                        //dialog.cont.add("in order to see the setting info,",Color.cyan).row();
                        //dialog.cont.add("I can not figure out how to make it wrap,",Color.cyan).row();
                        //dialog.cont.add("sorry.", Color.cyan).row();
                        //                dialog.cont.add("some of the sectors (especially Eradication maps) might even be unbeatable,",Color.cyan).row();
                        //                dialog.cont.add("so if you're a newbie, I recommend you to play the original games",Color.cyan).row();
                        //                dialog.cont.add(" and get familiar with the contents first.",Color.cyan).row();
                        //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                        //dialog.cont.image(Core.atlas.find("example-java-mod-frog")).pad(20f).row();
                        dialog.cont.button("OK", dialog::hide).size(100f, 50f).left();
                        dialog.cont.button("Don't Show Again",()->{
                            Core.settings.put("ec620.dontShowAgain",true);
                            dialog.hide();
                        }).size(200f, 50f).right();
                        dialog.show();
                    });
            }
              //EC620TechTree.postProcess();
            Log.info("Load dialog complete");
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
    private void dontShowAgain()
    {

    }
    @Override
    public void init()
    {
        EC620Setting.loadUI();
    }
    @Override
    public void loadContent()
    {
        Log.info("Loading Random Planet content.");
//        try {
//            nameGenerator=new EC620NameGenerator();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        EC620NameGenerator.load();
        EC620Items.load();
        EC620Blocks.load();
        EC620Units.load();
        EC620Planets.load();
        //EC620SectorPresents.load();

        EC620TechTree.load();
        EC620Setting.load();
        Log.info("Random Planet loaded successfully.");
    }
}
