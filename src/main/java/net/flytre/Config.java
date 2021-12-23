package net.flytre;

import com.google.gson.annotations.SerializedName;
import net.flytre.flytre_lib.api.config.ConfigHandler;
import net.flytre.flytre_lib.api.config.annotation.Description;
import net.flytre.flytre_lib.api.config.annotation.DisplayName;
import net.flytre.flytre_lib.api.config.reference.block.ConfigBlock;
import net.flytre.flytre_lib.api.config.reference.entity.ConfigEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;

import java.util.Set;

public class Config {

    public static ConfigHandler<Config> HANDLER = new ConfigHandler<>(new Config(), "grass_bypass");

    @SerializedName("blacklisted_entities")
    @DisplayName("Blacklisted Entities")
    @Description("Entities in this list cannot be hit through grass")
    public Set<ConfigEntity> blacklistedEntities = ConfigEntity.of(Set.of(EntityType.VILLAGER));

    @SerializedName("blacklisted_blocks")
    @DisplayName("Blacklisted Blocks")
    @Description("You cannot swing through blocks in this list")
    public Set<ConfigBlock> blacklistedBlocks = ConfigBlock.of(Set.of(Blocks.TORCH));
}
