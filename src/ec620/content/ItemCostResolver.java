package ec620.content;

import arc.struct.*;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.*;

public class ItemCostResolver {
    private static final ObjectMap<Item, Float> baseCosts = new ObjectMap<>();
    private static final ObjectMap<Item, Float> resolvedCosts = new ObjectMap<>();
    private static final ObjectMap<Item, Seq<Recipe>> recipes = new ObjectMap<>();
    private static final ObjectSet<Item> craftedItems = new ObjectSet<>();

    public static class Recipe {
        public Item output;
        public float outputAmount;
        public Seq<ItemStack> inputs;
        public Block producer;

        public Recipe(Item output, float outputAmount, Seq<ItemStack> inputs, Block producer) {
            this.output = output;
            this.outputAmount = outputAmount;
            this.inputs = inputs;
            this.producer = producer;
        }
    }

    public static void init(){
        extractRecipesAndMarkCrafted();
        detectAndAssignBaseItems();
        computeAllCosts();
        printResolvedCosts();
    }

    public static float get(Item item){
        return resolvedCosts.get(item, Float.POSITIVE_INFINITY);
    }

    private static void extractRecipesAndMarkCrafted(){
        for(Block b : Vars.content.blocks()){
            if(b instanceof GenericCrafter gc && gc.outputItem != null){
                ItemStack out = gc.outputItem;
                craftedItems.add(out.item);
                for(Consume cons : gc.consumers){
                    if(cons instanceof ConsumeItems ci){
                        Seq<ItemStack> inputs = new Seq<>(ci.items);
                        Recipe r = new Recipe(out.item, out.amount, inputs, gc);
                        recipes.get(out.item, new Seq<>()).add(r);
                    }
                }
            }
        }
    }

    private static void detectAndAssignBaseItems(){
        for(Item item : Vars.content.items()){
            if(!craftedItems.contains(item)){
                baseCosts.put(item, item.cost); // Use declared cost
            }
        }
    }

    private static void computeAllCosts(){
        for(Item item : Vars.content.items()){
            computeCost(item, new ObjectSet<>());
        }
    }

    private static float computeCost(Item item, ObjectSet<Item> seen){
        if(resolvedCosts.containsKey(item)) return resolvedCosts.get(item);
        if(baseCosts.containsKey(item)) {
            float base = baseCosts.get(item);
            resolvedCosts.put(item, base);
            return base;
        }

        if(seen.contains(item)) return Float.POSITIVE_INFINITY;
        seen.add(item);

        float minCost = Float.POSITIVE_INFINITY;
        Seq<Recipe> rList = recipes.get(item);

        if(rList != null){
            for(Recipe r : rList){
                float total = 0f;
                for(ItemStack in : r.inputs){
                    total += in.amount * computeCost(in.item, seen);
                }
                float costPerUnit = total / r.outputAmount;
                minCost = Math.min(minCost, costPerUnit);
            }
        }

        resolvedCosts.put(item, minCost);
        return minCost;
    }

    private static void printResolvedCosts(){
        Log.info("=== [ItemCostResolver] Resolved Item Costs ===");
        Seq<Item> sorted = Vars.content.items().copy();
        sorted.sort((a, b) -> Float.compare(get(a), get(b)));

        for(Item item : sorted){
            float cost = get(item);
            String marker = baseCosts.containsKey(item) ? "[BASE]" : "[CRAFTED]";
            String costStr = Float.isInfinite(cost) ? "∞" : String.format("%.3f", cost);
            Log.info("[@] @: @", marker, item.name, costStr);
        }

        Log.info("=== [ItemCostResolver] Total Items: @ ===", sorted.size);
    }

    public static float getBlockCost(Block block){
        if(block.requirements == null) return Float.POSITIVE_INFINITY;

        float cost = 0f;
        for(ItemStack stack : block.requirements){
            cost += stack.amount * get(stack.item);
        }
        return cost;
    }

}
