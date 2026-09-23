package com.reclamation.registry;

import com.reclamation.CreateReclamation;
import com.reclamation.content.reclaimer.ReclaimerDisplaySource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class ModDisplaySources {

    public static final RegistryEntry<DisplaySource, ReclaimerDisplaySource> RECLAIMER_PROGRESS =
            CreateReclamation.REGISTRATE
                    .displaySource("reclaimer_progress", ReclaimerDisplaySource::new)
                    .register();

    public static void register() {}
}
