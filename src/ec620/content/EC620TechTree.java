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
import static mindustry.content.SectorPresets.craters;
import static mindustry.content.TechTree.*;
import static mindustry.content.UnitTypes.*;
import static mindustry.content.UnitTypes.minke;

public class EC620TechTree {
    static TechTree.TechNode context = null;

    public EC620TechTree() {
    }

    public static void load()
    {
        TechNode root=nodeRoot(EC620Planets.planetName, coreShard, () ->
        {
            node(conveyor, () -> {

                node(junction, () -> {
                    node(router, () -> {
                        node(launchPad, Seq.with(new Objectives.SectorComplete(extractionOutpost)), () -> {
                            //no longer necessary to beat the campaign
                            //node(interplanetaryAccelerator, Seq.with(new SectorComplete(planetaryTerminal)), () -> {

                            //});
                        });

                        node(distributor);
                        node(sorter, () -> {
                            node(invertedSorter);
                            node(overflowGate, () -> {
                                node(underflowGate);
                            });
                        });
                        node(container, Seq.with(new Objectives.SectorComplete(biomassFacility)), () -> {
                            node(unloader,()->{
                                node(ductUnloader, () -> {

                                });
                            });
                            node(vault, Seq.with(new Objectives.SectorComplete(stainedMountains)), () -> {
                                node(reinforcedVault, () -> {

                                });
                            });
                            node(reinforcedContainer, () -> {



                            });
                        });

                        node(itemBridge, () -> {
                            node(titaniumConveyor, Seq.with(new Objectives.SectorComplete(craters)), () -> {
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

                                        node(overflowDuct, Seq.with(new Objectives.OnSector(aegis)), () -> {
                                            node(underflowDuct);

                                        });


                                    });

                                    node(reinforcedPayloadConveyor, Seq.with(new Objectives.OnSector(atlas)), () -> {
                                        //TODO should only be unlocked in unit sector
                                        node(payloadMassDriver, Seq.with(new Objectives.Research(siliconArcFurnace), new Objectives.OnSector(split)), () -> {
                                            //TODO further limitations
                                            node(payloadLoader, () -> {
                                                node(payloadUnloader, () -> {
                                                    node(largePayloadMassDriver, () -> {

                                                    });
                                                });
                                            });

                                            node(constructor, Seq.with(new Objectives.OnSector(split)), () -> {
                                                node(smallDeconstructor, Seq.with(new Objectives.OnSector(peaks)), () -> {
                                                    node(largeConstructor, Seq.with(new Objectives.OnSector(siege)), () -> {

                                                    });

                                                    node(deconstructor, Seq.with(new Objectives.OnSector(siege)), () -> {

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
                    node(coreCitadel, Seq.with(new Objectives.SectorComplete(peaks)), () -> {
                        node(coreAcropolis, Seq.with(new Objectives.SectorComplete(siege)), () -> {

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

                                node(pulseConduit, Seq.with(new Objectives.SectorComplete(windsweptIslands)), () -> {
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
                        node(reinforcedConduit, Seq.with(new Objectives.OnSector(aegis)), () -> {
                            //TODO maybe should be even later
                            node(reinforcedPump, Seq.with(new Objectives.OnSector(basin)), () -> {
                                //TODO T2 pump, consume cyanogen or similar
                            });

                            node(reinforcedLiquidJunction, () -> {
                                node(reinforcedBridgeConduit, () -> {

                                });

                                node(reinforcedLiquidRouter, () -> {
                                    node(reinforcedLiquidContainer, () -> {
                                        node(reinforcedLiquidTank, Seq.with(new Objectives.SectorComplete(intersect)), () -> {

                                        });
                                    });
                                });
                            });
                        });
                    });
                });

                node(graphitePress, () -> {
                    node(pneumaticDrill, Seq.with(new Objectives.SectorComplete(frozenForest)), () -> {
                        node(cultivator, Seq.with(new Objectives.SectorComplete(biomassFacility)), () -> {

                        });

                        node(laserDrill, () -> {
                            node(blastDrill, Seq.with(new Objectives.SectorComplete(nuclearComplex)), () -> {

                            });

                            node(waterExtractor, Seq.with(new Objectives.SectorComplete(saltFlats)), () -> {
                                node(oilExtractor, () -> {

                                });
                            });
                        });
                        node(plasmaBore, () -> {
                            node(impactDrill, Seq.with(new Objectives.OnSector(aegis)), () -> {
                                node(largePlasmaBore, Seq.with(new Objectives.OnSector(caldera)), () -> {
                                    node(eruptionDrill, Seq.with(new Objectives.OnSector(stronghold)), () -> {

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

                            node(plastaniumCompressor, Seq.with(new Objectives.SectorComplete(windsweptIslands)), () -> {
                                node(phaseWeaver, Seq.with(new Objectives.SectorComplete(tarFields)), () -> {

                                });
                            });
                        });

                        node(kiln, Seq.with(new Objectives.SectorComplete(craters)), () -> {
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
                                    node(reinforcedMessage, Seq.with(new Objectives.OnSector(aegis)), () -> {
                                        node(canvas);
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
                    });
                    node(cliffCrusher, () -> {
                        node(siliconArcFurnace, () -> {
                            node(electrolyzer, Seq.with(new Objectives.OnSector(atlas)), () -> {
                                node(oxidationChamber, Seq.with(new Objectives.Research(tankRefabricator), new Objectives.OnSector(marsh)), () -> {

                                    node(surgeCrucible, Seq.with(new Objectives.OnSector(ravine)), () -> {

                                    });
                                    node(heatRedirector, Seq.with(new Objectives.OnSector(ravine)), () -> {
                                        node(electricHeater, Seq.with(new Objectives.OnSector(ravine), new Objectives.Research(afflict)), () -> {
                                            node(slagHeater, Seq.with(new Objectives.OnSector(caldera)), () -> {

                                            });

                                            node(atmosphericConcentrator, Seq.with(new Objectives.OnSector(caldera)), () -> {
                                                node(cyanogenSynthesizer, Seq.with(new Objectives.OnSector(siege)), () -> {

                                                });
                                            });

                                            node(carbideCrucible, Seq.with(new Objectives.OnSector(crevice)), () -> {
                                                node(phaseSynthesizer, Seq.with(new Objectives.OnSector(karst)), () -> {
                                                    node(phaseHeater, Seq.with(new Objectives.Research(phaseSynthesizer)), () -> {

                                                    });
                                                });
                                            });

                                            node(heatRouter, () -> {

                                            });
                                        });
                                    });
                                });

                                node(slagIncinerator, Seq.with(new Objectives.OnSector(basin)), () -> {

                                    //TODO these are unused.
                                    //node(slagCentrifuge, () -> {});
                                    //node(heatReactor, () -> {});
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
                                node(forceProjector, Seq.with(new Objectives.SectorComplete(impact0078)), () -> {
                                    node(overdriveProjector, Seq.with(new Objectives.SectorComplete(impact0078)), () -> {
                                        node(overdriveDome, Seq.with(new Objectives.SectorComplete(impact0078)), () -> {

                                        });
                                    });
                                });

                                node(repairPoint, () -> {
                                    node(repairTurret, () -> {

                                    });
                                });
                            });
                        });

                        node(steamGenerator, Seq.with(new Objectives.SectorComplete(craters)), () -> {
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
                            node(ventCondenser, Seq.with(new Objectives.OnSector(aegis)), () -> {
                                node(chemicalCombustionChamber, Seq.with(new Objectives.OnSector(basin)), () -> {
                                    node(pyrolysisGenerator, Seq.with(new Objectives.OnSector(crevice)), () -> {
                                        node(fluxReactor, Seq.with(new Objectives.OnSector(crossroads), new Objectives.Research(cyanogenSynthesizer)), () -> {
                                            node(neoplasiaReactor, Seq.with(new Objectives.OnSector(karst)), () -> {

                                            });
                                        });
                                    });
                                });
                            });

                            node(beamTower, Seq.with(new Objectives.OnSector(peaks)), () -> {

                            });


                            node(regenProjector, Seq.with(new Objectives.OnSector(peaks)), () -> {
                                //TODO more tiers of build tower or "support" structures like overdrive projectors
                                node(buildTower, Seq.with(new Objectives.OnSector(stronghold)), () -> {
                                    node(shockwaveTower, Seq.with(new Objectives.OnSector(siege)), () -> {

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
                    node(hail, Seq.with(new Objectives.SectorComplete(craters)), () -> {
                        node(salvo, () -> {
                            node(swarmer, () -> {
                                node(cyclone, () -> {
                                    node(spectre, Seq.with(new Objectives.SectorComplete(nuclearComplex)), () -> {

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


                    node(diffuse, Seq.with(new Objectives.OnSector(lake)), () -> {
                        node(sublimate, Seq.with(new Objectives.OnSector(marsh)), () -> {
                            node(afflict, Seq.with(new Objectives.OnSector(ravine)), () -> {
                                node(titan, Seq.with(new Objectives.OnSector(stronghold)), () -> {
                                    node(lustre, Seq.with(new Objectives.OnSector(crevice)), () -> {
                                        node(smite, Seq.with(new Objectives.OnSector(karst)), () -> {

                                        });
                                    });
                                });
                            });
                        });

                        node(disperse, Seq.with(new Objectives.OnSector(stronghold)), () -> {
                            node(scathe, Seq.with(new Objectives.OnSector(siege)), () -> {
                                node(malign, Seq.with(new Objectives.SectorComplete(karst)), () -> {

                                });
                            });
                        });
                    });


                    node(radar, Seq.with(new Objectives.Research(beamNode), new Objectives.Research(turbineCondenser), new Objectives.Research(tankFabricator), new Objectives.OnSector(SectorPresets.aegis)), () -> {

                    });
                });



                node(tankFabricator, Seq.with(new Objectives.Research(siliconArcFurnace), new Objectives.Research(plasmaBore), new Objectives.Research(turbineCondenser)), () -> {
                    node(UnitTypes.stell);

                    node(unitRepairTower, Seq.with(new Objectives.OnSector(ravine), new Objectives.Research(mechRefabricator)), () -> {

                    });

                    node(shipFabricator, Seq.with(new Objectives.OnSector(lake)), () -> {
                        node(UnitTypes.elude);

                        node(mechFabricator, Seq.with(new Objectives.OnSector(intersect)), () -> {
                            node(UnitTypes.merui);

                            node(tankRefabricator, Seq.with(new Objectives.OnSector(atlas)), () -> {
                                node(UnitTypes.locus);

                                node(mechRefabricator, Seq.with(new Objectives.OnSector(basin)), () -> {
                                    node(UnitTypes.cleroi);

                                    node(shipRefabricator, Seq.with(new Objectives.OnSector(peaks)), () -> {
                                        node(UnitTypes.avert);

                                        //TODO
                                        node(primeRefabricator, Seq.with(new Objectives.OnSector(stronghold)), () -> {
                                            node(UnitTypes.precept);
                                            node(UnitTypes.anthicus);
                                            node(UnitTypes.obviate);
                                        });

                                        node(tankAssembler, Seq.with(new Objectives.OnSector(siege), new Objectives.Research(constructor), new Objectives.Research(atmosphericConcentrator)), () -> {

                                            node(UnitTypes.vanquish, () -> {
                                                node(UnitTypes.conquer, Seq.with(new Objectives.OnSector(karst)), () -> {

                                                });
                                            });

                                            node(shipAssembler, Seq.with(new Objectives.OnSector(crossroads)), () -> {
                                                node(UnitTypes.quell, () -> {
                                                    node(UnitTypes.disrupt, Seq.with(new Objectives.OnSector(karst)), () -> {

                                                    });
                                                });
                                            });

                                            node(mechAssembler, Seq.with(new Objectives.OnSector(crossroads)), () -> {
                                                node(UnitTypes.tecta, () -> {
                                                    node(UnitTypes.collaris, Seq.with(new Objectives.OnSector(karst)), () -> {

                                                    });
                                                });
                                            });

                                            node(basicAssemblerModule, Seq.with(new Objectives.SectorComplete(karst)), () -> {

                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });

                node(onset, () -> {
                    node(aegis, Seq.with(new Objectives.SectorComplete(onset), new Objectives.Research(ductRouter), new Objectives.Research(ductBridge)), () -> {
                        node(lake, Seq.with(new Objectives.SectorComplete(aegis)), () -> {

                        });

                        node(intersect, Seq.with(new Objectives.SectorComplete(aegis), new Objectives.SectorComplete(lake), new Objectives.Research(ventCondenser), new Objectives.Research(shipFabricator)), () -> {
                            node(atlas, Seq.with(new Objectives.SectorComplete(intersect), new Objectives.Research(mechFabricator)), () -> {
                                node(split, Seq.with(new Objectives.SectorComplete(atlas), new Objectives.Research(reinforcedPayloadConveyor), new Objectives.Research(reinforcedContainer)), () -> {

                                });

                                node(basin, Seq.with(new Objectives.SectorComplete(atlas)), () -> {
                                    node(marsh, Seq.with(new Objectives.SectorComplete(basin)), () -> {
                                        node(ravine, Seq.with(new Objectives.SectorComplete(marsh), new Objectives.Research(Liquids.slag)), () -> {
                                            node(caldera, Seq.with(new Objectives.SectorComplete(peaks), new Objectives.Research(heatRedirector)), () -> {
                                                node(stronghold, Seq.with(new Objectives.SectorComplete(caldera), new Objectives.Research(coreCitadel)), () -> {
                                                    node(crevice, Seq.with(new Objectives.SectorComplete(stronghold)), () -> {
                                                        node(siege, Seq.with(new Objectives.SectorComplete(crevice)), () -> {
                                                            node(crossroads, Seq.with(new Objectives.SectorComplete(siege)), () -> {
                                                                node(karst, Seq.with(new Objectives.SectorComplete(crossroads), new Objectives.Research(coreAcropolis)), () -> {
                                                                    node(origin, Seq.with(new Objectives.SectorComplete(karst), new Objectives.Research(coreAcropolis), new Objectives.Research(UnitTypes.vanquish), new Objectives.Research(UnitTypes.disrupt), new Objectives.Research(UnitTypes.collaris), new Objectives.Research(malign), new Objectives.Research(basicAssemblerModule), new Objectives.Research(neoplasiaReactor)), () -> {

                                                                    });
                                                                });
                                                            });
                                                        });
                                                    });
                                                });
                                            });
                                        });

                                        node(peaks, Seq.with(new Objectives.SectorComplete(marsh), new Objectives.SectorComplete(split)), () -> {

                                        });
                                    });
                                });
                            });
                        });
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

                    node(navalFactory, Seq.with(new Objectives.SectorComplete(ruinousShores)), () -> {
                        node(risso, () -> {
                            node(minke, () -> {
                                node(bryde, () -> {
                                    node(sei, () -> {
                                        node(omura, () -> {

                                        });
                                    });
                                });
                            });

                            node(retusa, Seq.with(new Objectives.SectorComplete(windsweptIslands)), () -> {
                                node(oxynoe, Seq.with(new Objectives.SectorComplete(coastline)), () -> {
                                    node(cyerce, () -> {
                                        node(aegires, () -> {
                                            node(navanax, Seq.with(new Objectives.SectorComplete(navalFortress)), () -> {

                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });

                node(additiveReconstructor, Seq.with(new Objectives.SectorComplete(biomassFacility)), () -> {
                    node(multiplicativeReconstructor, () -> {
                        node(exponentialReconstructor, Seq.with(new Objectives.SectorComplete(overgrowth)), () -> {
                            node(tetrativeReconstructor, () -> {

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
                nodeProduce(Items.beryllium, () -> {

                    nodeProduce(Items.oxide, () -> {
                        nodeProduce(Items.fissileMatter, () -> {});
                    });


                    nodeProduce(Items.tungsten, () ->
                    {
                        nodeProduce(Liquids.arkycite, () ->
                        {

                        });

                        nodeProduce(Items.carbide, () ->
                        {
                                nodeProduce(Liquids.gallium, () -> {});
                        });

                    });
                });
            });
        });

        //No requirements
        /*TechNode root=nodeRoot(EC620Planets.planetName, coreShard, () -> {


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
//                nodeProduce(EC620Items.iron,()->{
//                    nodeProduce(EC620Items.aluminum,()->{
//                        nodeProduce(EC620Items.duralumin,()->{
//
//                        });
//                    });
//                });

            });
        });*/
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
