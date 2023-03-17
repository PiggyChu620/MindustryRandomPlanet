//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ec620.content;

import arc.Core;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.*;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.UnitType;

import static mindustry.content.Blocks.*;
import static mindustry.content.Blocks.swarmer;
import static mindustry.content.SectorPresets.*;
import static mindustry.content.TechTree.*;
import static mindustry.content.UnitTypes.*;
import static mindustry.content.UnitTypes.minke;

public class EC620TechTree {
    static TechTree.TechNode context = null;

    public EC620TechTree() {
    }

    public static void load()
    {
        TechNode root=nodeRoot("EC-620", coreShard, () -> {


            node(conveyor, () -> {

                node(junction, () -> {
                    node(router, () -> {
                        node(launchPad);

                        node(distributor);
                        node(sorter, () -> {
                            node(invertedSorter);
                            node(overflowGate, () -> {
                                node(underflowGate);
                            });
                        });
                        node(container,  () -> {
                            node(unloader);
                            node(vault);
                        });

                        node(itemBridge, () -> {
                            node(titaniumConveyor,  () -> {
                                node(phaseConveyor, () -> {
                                    node(massDriver, () -> {

                                    });
                                });

                                node(payloadConveyor, () -> {
                                    node(payloadRouter, () -> {

                                    });
                                });

                                node(armoredConveyor, () -> {
                                    node(plastaniumConveyor, () -> {

                                    });
                                });
                                node(duct, () -> {
                                    node(ductRouter, () -> {
                                        node(ductBridge, () -> {
                                            node(armoredDuct, () -> {
                                                node(surgeConveyor, () -> {
                                                    node(surgeRouter);
                                                });
                                            });

                                            node(unitCargoLoader, () -> {
                                                node(unitCargoUnloadPoint, () -> {

                                                });
                                            });
                                        });

                                        node(overflowDuct,  () -> {
                                            node(underflowDuct);
                                            node(reinforcedContainer, () -> {
                                                node(ductUnloader, () -> {

                                                });

                                                node(reinforcedVault, () -> {

                                                });
                                            });
                                        });

                                        node(reinforcedMessage,  () -> {
                                            node(canvas);
                                        });
                                    });

                                    node(reinforcedPayloadConveyor,  () -> {
                                        node(payloadMassDriver, Seq.with(new Objectives.Research(siliconArcFurnace)), () -> {
                                            node(payloadLoader, () -> {
                                                node(payloadUnloader, () -> {
                                                    node(largePayloadMassDriver, () -> {

                                                    });
                                                });
                                            });

                                            node(constructor,  () -> {
                                                node(smallDeconstructor,  () -> {
                                                    node(largeConstructor,  () -> {

                                                    });

                                                    node(deconstructor,  () -> {

                                                    });
                                                });
                                            });
                                        });

                                        node(reinforcedPayloadRouter, () -> {

                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });

            node(coreFoundation, () -> {
                node(coreNucleus, () -> {

                });
                node(coreBastion,()->{
                    node(coreCitadel,  () -> {
                        node(coreAcropolis,  () -> {

                        });
                    });
                });
            });

            node(mechanicalDrill, () -> {

                node(mechanicalPump, () -> {
                    node(conduit, () -> {
                        node(liquidJunction, () -> {
                            node(liquidRouter, () -> {
                                node(liquidContainer, () -> {
                                    node(liquidTank);
                                });

                                node(bridgeConduit);

                                node(pulseConduit,  () -> {
                                    node(phaseConduit, () -> {

                                    });

                                    node(platedConduit, () -> {

                                    });

                                    node(rotaryPump, () -> {
                                        node(impulsePump, () -> {

                                        });
                                    });
                                });
                            });
                        });
                        node(reinforcedConduit,  () -> {
                            node(reinforcedPump,  () -> {
                            });

                            node(reinforcedLiquidJunction, () -> {
                                node(reinforcedBridgeConduit, () -> {

                                });

                                node(reinforcedLiquidRouter, () -> {
                                    node(reinforcedLiquidContainer, () -> {
                                        node(reinforcedLiquidTank,  () -> {

                                        });
                                    });
                                });
                            });
                        });
                    });
                });


                node(graphitePress, () -> {
                    node(pneumaticDrill,  () -> {
                        node(cultivator,  () -> {

                        });

                        node(laserDrill, () -> {
                            node(blastDrill,  () -> {

                            });

                            node(waterExtractor,  () -> {
                                node(oilExtractor, () -> {

                                });
                            });
                        });
                        node(plasmaBore, () -> {
                            node(impactDrill,  () -> {
                                node(largePlasmaBore,  () -> {
                                    node(eruptionDrill,  () -> {

                                    });
                                });
                            });
                        });
                    });

                    node(pyratiteMixer, () -> {
                        node(blastMixer, () -> {

                        });
                    });

                    node(siliconSmelter, () -> {

                        node(sporePress, () -> {
                            node(coalCentrifuge, () -> {
                                node(multiPress, () -> {
                                    node(siliconCrucible, () -> {

                                    });
                                });
                            });

                            node(plastaniumCompressor,  () -> {
                                node(phaseWeaver,  () -> {

                                });
                            });
                        });

                        node(kiln,  () -> {
                            node(pulverizer, () -> {
                                node(incinerator, () -> {
                                    node(melter, () -> {
                                        node(surgeSmelter, () -> {

                                        });

                                        node(separator, () -> {
                                            node(disassembler, () -> {

                                            });
                                        });

                                        node(cryofluidMixer, () -> {

                                        });
                                    });
                                });
                            });
                        });

                        //logic disabled until further notice
                        node(microProcessor, () -> {
                            node(switchBlock, () -> {
                                node(message, () -> {
                                    node(logicDisplay, () -> {
                                        node(largeLogicDisplay, () -> {

                                        });
                                    });

                                    node(memoryCell, () -> {
                                        node(memoryBank, () -> {

                                        });
                                    });
                                });

                                node(logicProcessor, () -> {
                                    node(hyperProcessor, () -> {

                                    });
                                });
                            });
                        });

                        node(illuminator, () -> {

                        });
                    });node(cliffCrusher, () -> {
                        node(siliconArcFurnace, () -> {
                            node(electrolyzer,  () -> {
                                node(oxidationChamber, Seq.with(new Objectives.Research(tankRefabricator)), () -> {

                                    node(surgeCrucible, () -> {

                                    });
                                    node(heatRedirector,  () -> {
                                        node(electricHeater, Seq.with(new Objectives.Research(afflict)), () -> {
                                            node(slagHeater,  () -> {

                                            });

                                            node(atmosphericConcentrator,  () -> {
                                                node(cyanogenSynthesizer,  () -> {

                                                });
                                            });

                                            node(carbideCrucible,  () -> {
                                                node(phaseSynthesizer,  () -> {
                                                    node(phaseHeater, Seq.with(new Objectives.Research(phaseSynthesizer)), () -> {

                                                    });
                                                });
                                            });

                                            node(heatRouter, () -> {

                                            });
                                        });
                                    });
                                });

                                node(slagIncinerator,  () -> {

                                    //TODO these are unused.
                                    node(slagCentrifuge, () -> {});
                                    node(heatReactor, () -> {});
                                });
                            });
                        });
                    });

                });


                node(combustionGenerator, Seq.with(new Objectives.Research(Items.coal)), () -> {
                    node(powerNode, () -> {
                        node(powerNodeLarge, () -> {
                            node(diode, () -> {
                                node(surgeTower, () -> {

                                });
                            });
                        });

                        node(battery, () -> {
                            node(batteryLarge, () -> {

                            });
                        });

                        node(mender, () -> {
                            node(mendProjector, () -> {
                                node(forceProjector,  () -> {
                                    node(overdriveProjector,  () -> {
                                        node(overdriveDome,  () -> {

                                        });
                                    });
                                });

                                node(repairPoint, () -> {
                                    node(repairTurret, () -> {

                                    });
                                });
                            });
                        });

                        node(steamGenerator,  () -> {
                            node(thermalGenerator, () -> {
                                node(differentialGenerator, () -> {
                                    node(thoriumReactor, Seq.with(new Objectives.Research(Liquids.cryofluid)), () -> {
                                        node(impactReactor, () -> {

                                        });

                                        node(rtgGenerator, () -> {

                                        });
                                    });
                                });
                            });
                        });

                        node(solarPanel, () -> {
                            node(largeSolarPanel, () -> {

                            });
                        });
                    });
                    node(turbineCondenser, () -> {
                        node(beamNode, () -> {
                            node(ventCondenser,  () -> {
                                node(chemicalCombustionChamber,  () -> {
                                    node(pyrolysisGenerator,  () -> {
                                        node(fluxReactor, Seq.with(new Objectives.Research(cyanogenSynthesizer)), () -> {
                                            node(neoplasiaReactor,  () -> {

                                            });
                                        });
                                    });
                                });
                            });

                            node(beamTower,  () -> {

                            });


                            node(regenProjector,  () -> {
                                node(buildTower,  () -> {
                                    node(shockwaveTower,  () -> {

                                    });
                                });
                            });
                        });




                    });
                });
            });

            node(duo, () -> {
                node(copperWall, () -> {
                    node(copperWallLarge, () -> {
                        node(titaniumWall, () -> {
                            node(titaniumWallLarge);

                            node(door, () -> {
                                node(doorLarge);
                            });
                            node(plastaniumWall, () -> {
                                node(plastaniumWallLarge, () -> {

                                });
                            });
                            node(thoriumWall, () -> {
                                node(thoriumWallLarge);
                                node(surgeWall, () -> {
                                    node(surgeWallLarge);
                                    node(phaseWall, () -> {
                                        node(phaseWallLarge);
                                    });
                                });
                            });
                        });
                    });
                    node(berylliumWall, () -> {
                        node(berylliumWallLarge, () -> {

                        });

                        node(tungstenWall, () -> {
                            node(tungstenWallLarge, () -> {
                                node(blastDoor, () -> {

                                });
                            });

                            node(reinforcedSurgeWall, () -> {
                                node(reinforcedSurgeWallLarge, () -> {
                                    node(shieldedWall, () -> {

                                    });
                                });
                            });

                            node(carbideWall, () -> {
                                node(carbideWallLarge, () -> {

                                });
                            });
                        });
                    });

                });

                node(scatter, () -> {
                    node(hail,  () -> {
                        node(salvo, () -> {
                            node(swarmer, () -> {
                                node(cyclone, () -> {
                                    node(spectre,  () -> {

                                    });
                                });
                            });

                            node(ripple, () -> {
                                node(fuse, () -> {

                                });
                            });
                        });
                    });
                });

                node(scorch, () -> {
                    node(arc, () -> {
                        node(wave, () -> {
                            node(parallax, () -> {
                                node(segment, () -> {

                                });
                            });

                            node(tsunami, () -> {

                            });
                        });

                        node(lancer, () -> {
                            node(meltdown, () -> {
                                node(foreshadow, () -> {

                                });
                            });

                            node(shockMine, () -> {

                            });
                        });
                    });
                });
                node(breach, Seq.with(new Objectives.Research(siliconArcFurnace), new Objectives.Research(tankFabricator)), () -> {


                    node(diffuse,  () -> {
                        node(sublimate,  () -> {
                            node(afflict, () -> {
                                node(titan,  () -> {
                                    node(lustre,  () -> {
                                        node(smite,  () -> {

                                        });
                                    });
                                });
                            });
                        });

                        node(disperse,  () -> {
                            node(scathe,  () -> {
                                node(malign,  () -> {

                                });
                            });
                        });
                    });


                    node(radar, Seq.with(new Objectives.Research(beamNode), new Objectives.Research(turbineCondenser), new Objectives.Research(tankFabricator)), () -> {

                    });
                });
            });

            node(groundFactory, () -> {

                node(dagger, () -> {
                    node(mace, () -> {
                        node(fortress, () -> {
                            node(scepter, () -> {
                                node(reign, () -> {

                                });
                            });
                        });
                    });

                    node(nova, () -> {
                        node(pulsar, () -> {
                            node(quasar, () -> {
                                node(vela, () -> {
                                    node(corvus, () -> {

                                    });
                                });
                            });
                        });
                    });

                    node(crawler, () -> {
                        node(atrax, () -> {
                            node(spiroct, () -> {
                                node(arkyid, () -> {
                                    node(toxopid, () -> {

                                    });
                                });
                            });
                        });
                    });
                });

                node(airFactory, () -> {
                    node(flare, () -> {
                        node(horizon, () -> {
                            node(zenith, () -> {
                                node(antumbra, () -> {
                                    node(eclipse, () -> {

                                    });
                                });
                            });
                        });

                        node(mono, () -> {
                            node(poly, () -> {
                                node(mega, () -> {
                                    node(quad, () -> {
                                        node(oct, () -> {

                                        });
                                    });
                                });
                            });
                        });
                    });

                    node(navalFactory,  () -> {
                        node(risso, () -> {
                            node(minke, () -> {
                                node(bryde, () -> {
                                    node(sei, () -> {
                                        node(omura, () -> {

                                        });
                                    });
                                });
                            });

                            node(retusa,  () -> {
                                node(oxynoe,  () -> {
                                    node(cyerce, () -> {
                                        node(aegires, () -> {
                                            node(navanax,  () -> {

                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });

                node(additiveReconstructor,  () -> {
                    node(multiplicativeReconstructor, () -> {
                        node(exponentialReconstructor,  () -> {
                            node(tetrativeReconstructor, () -> {

                            });
                        });
                    });
                });
                node(tankFabricator, Seq.with(new Objectives.Research(siliconArcFurnace), new Objectives.Research(plasmaBore), new Objectives.Research(turbineCondenser)), () -> {
                    node(UnitTypes.stell);

                    node(unitRepairTower, Seq.with( new Objectives.Research(mechRefabricator)), () -> {

                    });

                    node(shipFabricator,  () -> {
                        node(UnitTypes.elude);

                        node(mechFabricator,  () -> {
                            node(UnitTypes.merui);

                            node(tankRefabricator,  () -> {
                                node(UnitTypes.locus);

                                node(mechRefabricator,  () -> {
                                    node(UnitTypes.cleroi);

                                    node(shipRefabricator,  () -> {
                                        node(UnitTypes.avert);

                                        //TODO
                                        node(primeRefabricator,  () -> {
                                            node(UnitTypes.precept);
                                            node(UnitTypes.anthicus);
                                            node(UnitTypes.obviate);
                                        });

                                        node(tankAssembler, Seq.with(new Objectives.Research(constructor), new Objectives.Research(atmosphericConcentrator)), () -> {

                                            node(UnitTypes.vanquish, () -> {
                                                node(UnitTypes.conquer,  () -> {

                                                });
                                            });

                                            node(shipAssembler,  () -> {
                                                node(UnitTypes.quell, () -> {
                                                    node(UnitTypes.disrupt,  () -> {

                                                    });
                                                });
                                            });

                                            node(mechAssembler, () -> {
                                                node(UnitTypes.tecta, () -> {
                                                    node(UnitTypes.collaris,  () -> {

                                                    });
                                                });
                                            });

                                            node(basicAssemblerModule, () -> {

                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });



            nodeProduce(Items.copper, () -> {
                nodeProduce(Liquids.water, () -> {
                    nodeProduce(Liquids.ozone, () -> {
                        nodeProduce(Liquids.hydrogen, () -> {
                            nodeProduce(Liquids.nitrogen, () -> {

                            });

                            nodeProduce(Liquids.cyanogen, () -> {
                                nodeProduce(Liquids.neoplasm, () -> {

                                });
                            });
                        });
                    });
                });

                nodeProduce(Items.lead, () -> {
                    nodeProduce(Items.titanium, () -> {
                        nodeProduce(Liquids.cryofluid, () -> {

                        });

                        nodeProduce(Items.thorium, () -> {
                            nodeProduce(Items.surgeAlloy, () -> {

                            });

                            nodeProduce(Items.phaseFabric, () -> {

                            });
                        });
                    });

                    nodeProduce(Items.metaglass, () -> {

                    });
                });

                nodeProduce(Items.sand, () -> {
                    nodeProduce(Items.scrap, () -> {
                        nodeProduce(Liquids.slag, () -> {

                        });
                    });

                    nodeProduce(Items.coal, () -> {
                        nodeProduce(Items.graphite, () -> {
                            nodeProduce(Items.silicon, () -> {

                            });
                        });

                        nodeProduce(Items.pyratite, () -> {
                            nodeProduce(Items.blastCompound, () -> {

                            });
                        });

                        nodeProduce(Items.sporePod, () -> {

                        });

                        nodeProduce(Liquids.oil, () -> {
                            nodeProduce(Items.plastanium, () -> {

                            });
                        });
                    });
                });
                nodeProduce(Items.beryllium,()->{
                    nodeProduce(Items.oxide, () -> {
                        nodeProduce(Items.fissileMatter, () -> {});
                    });
                    nodeProduce(Items.tungsten, () -> {
                        nodeProduce(Liquids.arkycite, () -> {

                        });
                        nodeProduce(Items.carbide, () -> {

                            nodeProduce(Liquids.gallium, () -> {});
                        });
                    });
                });
                nodeProduce(EC620Items.iron,()->{
                    nodeProduce(EC620Items.aluminum,()->{
                        nodeProduce(EC620Items.duralumin,()->{

                        });
                    });
                });

            });
        });
        root.planet= EC620Planets.ec620;
        root.children.each(c -> c.planet =EC620Planets.ec620);
        /*mergeNode(Liquids.water, () -> {
            node(Liquids.ozone,()->{
                node(Liquids.hydrogen);
            });
        });
        mergeNode(Items.copper,()->{
            node(Items.beryllium,()->{
                node(Items.tungsten,()->{
                    node(Liquids.arkycite);
                });
            });
        });*/
    }
    /*private static void mergeNode(UnlockableContent parent, Runnable children)
    {
        context = (TechTree.TechNode)TechTree.all.find((t) -> {
            return t.content == parent;
        });
        children.run();
    }
    private static void node(UnlockableContent content, ItemStack[] requirements, Seq<Objectives.Objective> objectives, Runnable children) {
        TechTree.TechNode node = new TechTree.TechNode(context, content, requirements);
        if (objectives != null) {
            node.objectives = objectives;
        }

        TechTree.TechNode prev = context;
        context = node;
        children.run();
        context = prev;
    }

    private static void node(UnlockableContent content, ItemStack[] requirements, Runnable children) {
        node(content, requirements, (Seq)null, children);
    }

    private static void node(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children) {
        node(content, content.researchRequirements(), objectives, children);
    }

    private static void node(UnlockableContent content, Runnable children) {
        node(content, content.researchRequirements(), children);
    }

    private static void node(UnlockableContent block) {
        node(block, () -> {
        });
    }*/
}
