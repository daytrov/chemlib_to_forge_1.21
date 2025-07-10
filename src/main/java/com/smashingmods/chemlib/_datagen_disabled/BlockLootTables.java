package com.smashingmods.chemlib._datagen_disabled;

import com.smashingmods.chemlib.registry.BlockRegistry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockLootTables extends BlockLootSubProvider implements DataProvider {

    public BlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags()); // пустой фильтр + все флаги
    }

    @Override
    protected void generate() {
        BlockRegistry.BLOCKS.getEntries().forEach((RegistryObject<Block> block) ->
                dropSelf(block.get())
        );
    }

    @Nonnull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BlockRegistry.BLOCKS.getEntries().stream()
                .map(RegistryObject::get)
                .collect(Collectors.toList());
    }

    @Override
    public void run(@Nonnull CachedOutput output) {
        super.run(output);
    }

    @Override
    public String getName() {
        return "ChemLib LootTables";
    }
}
