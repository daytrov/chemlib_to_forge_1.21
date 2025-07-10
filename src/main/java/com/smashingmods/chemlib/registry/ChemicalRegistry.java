package com.smashingmods.chemlib.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smashingmods.chemlib.ChemLib;
import com.smashingmods.chemlib.api.ChemicalBlockType;
import com.smashingmods.chemlib.api.ChemicalItemType;
import com.smashingmods.chemlib.api.MatterState;
import com.smashingmods.chemlib.api.MetalType;
import com.smashingmods.chemlib.common.blocks.ChemicalBlock;
import com.smashingmods.chemlib.common.blocks.LampBlock;
import com.smashingmods.chemlib.common.items.CompoundItem;
import com.smashingmods.chemlib.common.items.ElementItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

import static net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS;

public class ChemicalRegistry {
    public static final JsonObject ELEMENTS_JSON = Registry.getStreamAsJsonObject("/data/chemlib/elements.json");
    public static final JsonObject COMPOUNDS_JSON = Registry.getStreamAsJsonObject("/data/chemlib/compounds.json");

    /*
        Elements are built from the Elements json and then everything is registered based on that information.
     */

    private static void registerElements() {
        for (JsonElement jsonElement : ELEMENTS_JSON.getAsJsonArray("elements")) {
            JsonObject object = jsonElement.getAsJsonObject();
            String elementName = object.get("name").getAsString();
            int atomicNumber = object.get("atomic_number").getAsInt();
            String abbreviation = object.get("abbreviation").getAsString();
            int group = object.get("group").getAsInt();
            int period = object.get("period").getAsInt();
            MatterState matterState = MatterState.valueOf(object.get("matter_state").getAsString().toUpperCase(Locale.ROOT));
            MetalType metalType = MetalType.valueOf(object.get("metal_type").getAsString().toUpperCase(Locale.ROOT));
            boolean artificial = object.has("artificial") && object.get("artificial").getAsBoolean();
            String color = object.get("color").getAsString();

            ItemRegistry.REGISTRY_ELEMENTS.register(elementName, () -> new ElementItem(elementName, atomicNumber, abbreviation, group, period, matterState, metalType, artificial, color, mobEffectsFactory(object)));
            RegistryObject<Item> registryObject = ItemRegistry.getRegistryObject(ItemRegistry.REGISTRY_ELEMENTS, elementName);

            if (!artificial) {
                switch (matterState) {
                    case SOLID -> {
                        boolean hasItem = object.has("has_item") && object.get("has_item").getAsBoolean();

                        if (metalType == MetalType.METAL) {
                            ItemRegistry.registerItemByType(registryObject, ChemicalItemType.PLATE);
                            if (!hasItem) {
                                ItemRegistry.registerItemByType(registryObject, ChemicalItemType.NUGGET);
                                ItemRegistry.registerItemByType(registryObject, ChemicalItemType.INGOT);
                                BlockRegistry.BLOCKS.register(String.format("%s_metal_block", elementName), () -> new ChemicalBlock(new ResourceLocation(ChemLib.MODID, elementName), ChemicalBlockType.METAL, BlockRegistry.METAL_BLOCKS, BlockRegistry.METAL_PROPERTIES));
                                BlockRegistry.getRegistryObjectByName(String.format("%s_metal_block", elementName)).ifPresent(block -> ItemRegistry.fromChemicalBlock(block, new Item.Properties()));
                            }
                        }
                        ItemRegistry.registerItemByType(registryObject, ChemicalItemType.DUST);
                    }
                    case LIQUID, GAS -> {
                        boolean hasFluid = object.has("has_fluid") && object.get("has_fluid").getAsBoolean();
                        if (!hasFluid) {
                            JsonObject properties = object.get("fluid_properties").getAsJsonObject();
                            int slopeFindDistance = properties.has("slope_find_distance") ? properties.get("slope_find_distance").getAsInt() : 4;
                            int decreasePerBlock = properties.has("decrease_per_block") ? properties.get("decrease_per_block").getAsInt() : 1;

                            if (group == 18) {
                                BlockRegistry.BLOCKS.register(String.format("%s_lamp_block", elementName), () -> new LampBlock(new ResourceLocation(ChemLib.MODID, elementName), ChemicalBlockType.LAMP, BlockRegistry.LAMP_BLOCKS, BlockRegistry.LAMP_PROPERTIES));
                                BlockRegistry.getRegistryObjectByName(String.format("%s_lamp_block", elementName)).ifPresent(block -> ItemRegistry.fromChemicalBlock(block, new Item.Properties()));
                            }
                            FluidRegistry.registerFluid(elementName, fluidTypePropertiesFactory(properties, ChemLib.MODID, elementName), Integer.parseInt(color, 16) | 0xFF000000, slopeFindDistance, decreasePerBlock);
                        }
                    }
                }
            }
        }
    }

    /*
        Compounds are built from the Compounds json and then everything is registered based on that information.
     */

    private static void registerCompounds() {

        for (JsonElement jsonElement : COMPOUNDS_JSON.getAsJsonArray("compounds")) {
            JsonObject object = jsonElement.getAsJsonObject();
            String compoundName = object.get("name").getAsString();
            MatterState matterState = MatterState.valueOf(object.get("matter_state").getAsString().toUpperCase(Locale.ROOT));
            String description = object.has("description") ? object.get("description").getAsString() : "";
            String color = object.get("color").getAsString();

            JsonArray components = object.getAsJsonArray("components");
            HashMap<String, Integer> componentMap = new LinkedHashMap<>();
            for (JsonElement component : components) {
                JsonObject componentObject = component.getAsJsonObject();
                String componentName = componentObject.get("name").getAsString();
                int count = componentObject.has("count") ? componentObject.get("count").getAsInt() : 1;
                componentMap.put(componentName, count);
            }

            ItemRegistry.REGISTRY_COMPOUNDS.register(compoundName, () -> new CompoundItem(compoundName, matterState, componentMap, description, color, mobEffectsFactory(object)));

            switch (matterState) {
                case SOLID -> {
                    boolean hasItem = object.get("has_item").getAsBoolean();
                    if (!hasItem) {
                        ItemRegistry.registerItemByType(ItemRegistry.getRegistryObject(ItemRegistry.REGISTRY_COMPOUNDS, compoundName), ChemicalItemType.COMPOUND);
                        if (compoundName.equals("polyvinyl_chloride")) {
                            ItemRegistry.registerItemByType(ItemRegistry.getRegistryObject(ItemRegistry.REGISTRY_COMPOUNDS, compoundName), ChemicalItemType.PLATE);
                        }
                    }
                }
                case LIQUID, GAS -> {
                    boolean hasFluid = object.has("has_fluid") && object.get("has_fluid").getAsBoolean();
                    if (!hasFluid) {
                        JsonObject properties = object.get("fluid_properties").getAsJsonObject();
                        int slopeFindDistance = properties.has("slope_find_distance") ? properties.get("slope_find_distance").getAsInt() : 4;
                        int decreasePerBlock = properties.has("decrease_per_block") ? properties.get("decrease_per_block").getAsInt() : 1;

                        switch (matterState) {
                            case LIQUID, GAS -> FluidRegistry.registerFluid(compoundName, fluidTypePropertiesFactory(properties, ChemLib.MODID, compoundName), Integer.parseInt(color, 16) | 0xFF000000, slopeFindDistance, decreasePerBlock);
                        }
                    }
                }
            }
        }
    }

    public static List<MobEffectInstance> mobEffectsFactory(JsonObject object) {
        List<MobEffectInstance> effectsList = new ArrayList<>();
        JsonArray effects = object.getAsJsonArray("effect");
        if (effects != null) {
            for (JsonElement effect : effects) {
                JsonObject effectObject = effect.getAsJsonObject();
                String effectLocation = effectObject.get("location").getAsString();
                int effectDuration = effectObject.get("duration").getAsInt();
                int effectAmplifier = effectObject.get("amplifier").getAsInt();
                MobEffect mobEffect = MOB_EFFECTS.getValue(new ResourceLocation(effectLocation));
                if (mobEffect != null) {
                    effectsList.add(new MobEffectInstance(mobEffect, effectDuration, effectAmplifier));
                }
            }
        }
        return effectsList;
    }

    public static FluidType.Properties fluidTypePropertiesFactory(JsonObject pObject, String pNamespace, String pName) {
        int density = pObject.has("density") ? pObject.get("density").getAsInt() : 1000;
        int lightLevel = pObject.has("light_level") ? pObject.get("light_level").getAsInt() : 0;
        int viscosity = pObject.has("viscosity") ? pObject.get("viscosity").getAsInt() : 1000;
        int temperature = pObject.has("temperature") ? pObject.get("temperature").getAsInt() : 300;
        float motionScale = pObject.has("motion_scale") ? pObject.get("motion_scale").getAsFloat() : 0.014f;
        int fallDistanceModifier = pObject.has("fall_distance_modifier") ? pObject.get("fall_distance_modifier").getAsInt() : 0;
        BlockPathTypes pathType = pObject.has("path_type") ? BlockPathTypes.valueOf(pObject.get("path_type").getAsString().toUpperCase(Locale.ROOT)) : BlockPathTypes.WATER;
        boolean pushEntity = !pObject.has("push_entity") || pObject.get("push_entity").getAsBoolean();
        boolean canSwim = !pObject.has("can_swim") || pObject.get("can_swim").getAsBoolean();
        boolean canDrown = pObject.has("can_drown") && pObject.get("can_drown").getAsBoolean();
        boolean canHydrate = pObject.has("can_hydrate") && pObject.get("can_hydrate").getAsBoolean();
        boolean canExtinguish = pObject.has("can_extinguish") && pObject.get("can_extinguish").getAsBoolean();
        boolean supportsBoating = pObject.has("supports_boating") && pObject.get("supports_boating").getAsBoolean();
        boolean canConvertToSource = pObject.has("can_convert_to_source") && pObject.get("can_convert_to_source").getAsBoolean();

        return FluidType.Properties.create()
                .descriptionId(String.format("block.%s.%s",pNamespace, pName))
                .density(density)
                .lightLevel(lightLevel)
                .viscosity(viscosity)
                .temperature(temperature)
                .motionScale(motionScale)
                .fallDistanceModifier(fallDistanceModifier)
                .pathType(pathType)
                .canPushEntity(pushEntity)
                .canSwim(canSwim)
                .canDrown(canDrown)
                .canHydrate(canHydrate)
                .canExtinguish(canExtinguish)
                .canConvertToSource(canConvertToSource)
                .supportsBoating(supportsBoating)
                .rarity(Rarity.COMMON)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH);
    }

    public static void register() {
        registerElements();
        registerCompounds();
    }
}
