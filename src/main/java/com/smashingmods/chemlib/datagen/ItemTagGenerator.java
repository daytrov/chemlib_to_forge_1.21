package com.smashingmods.chemlib.datagen;

import com.smashingmods.chemlib.ChemLib;
import com.smashingmods.chemlib.api.ChemicalItemType;
import com.smashingmods.chemlib.api.MatterState;
import com.smashingmods.chemlib.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {

    public ItemTagGenerator(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, TagsProvider<Block> pBlockTagProvider, ExistingFileHelper pFileHelper) {
        super(pOutput, pLookupProvider, pBlockTagProvider.contentsGetter(), ChemLib.MODID, pFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        ItemRegistry.getChemicalItems().forEach(item -> {
            String type = item.getItemType().getSerializedName();
            String name = item.getChemicalName();
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("forge", String.format("%ss/%s", type, name));
            TagKey<Item> key = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(rl);
            tag(key).add(item);
        });

        ItemRegistry.getChemicalBlockItems().forEach(item -> {
            if (item.getMatterState().equals(MatterState.SOLID)) {
                String name = item.getChemicalName();
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("forge", String.format("storage_blocks/%s", name));
                TagKey<Item> key = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(rl);
                tag(key).add(item);
            }
        });

        ItemRegistry.getChemicalItemByNameAndType("potassium_nitrate", ChemicalItemType.COMPOUND).ifPresent(compound -> {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("forge", "dusts/niter");
            TagKey<Item> key = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(rl);
            tag(key).add(compound);
        });

        ItemRegistry.getChemicalItemByNameAndType("hydroxylapatite", ChemicalItemType.COMPOUND).ifPresent(compound -> {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("forge", "dusts/apatite");
            TagKey<Item> key = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(rl);
            tag(key).add(compound);
        });

        ItemRegistry.getChemicalItemByNameAndType("cellulose", ChemicalItemType.COMPOUND).ifPresent(compound -> {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("forge", "sawdust");
            TagKey<Item> key = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(rl);
            tag(key).add(compound);
        });

        ItemRegistry.getChemicalItemByNameAndType("mercury_sulfide", ChemicalItemType.COMPOUND).ifPresent(compound -> {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("forge", "dusts/cinnabar");
            TagKey<Item> key = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(rl);
            tag(key).add(compound);
        });
    }

    @Override
    @Nonnull
    public String getName() {
        return ChemLib.MODID + ":tags";
    }
}
