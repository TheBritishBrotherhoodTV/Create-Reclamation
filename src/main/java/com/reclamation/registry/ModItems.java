package com.reclamation.registry;

import com.reclamation.CreateReclamation;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final ItemEntry<Item> SALVAGED_SCRAP = CreateReclamation.REGISTRATE
            .item("salvaged_scrap", Item::new)
            .register();

    public static void register() {}
}
