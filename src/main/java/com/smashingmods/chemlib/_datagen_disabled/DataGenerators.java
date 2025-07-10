package com.smashingmods.chemlib._datagen_disabled;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        BlockTagGenerator blockTags = new BlockTagGenerator(packOutput, lookupProvider, fileHelper);

        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeClient(), new BlockStateGenerator(packOutput, fileHelper));
        generator.addProvider(event.includeClient(), new ItemModelGenerator(packOutput, fileHelper));
        generator.addProvider(event.includeServer(), new ItemTagGenerator(packOutput, lookupProvider, blockTags.contentsGetter(), fileHelper));
        generator.addProvider(event.includeServer(), new RecipeGenerator(packOutput));
        generator.addProvider(event.includeServer(), new BlockLootTables());
        generator.addProvider(event.includeClient(), new LocalizationGenerator(packOutput, "en_us"));
    }
}
