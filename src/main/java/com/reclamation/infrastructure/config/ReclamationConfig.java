package com.reclamation.infrastructure.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ReclamationConfig {

    public static class Server {
        public final ModConfigSpec.DoubleValue stressImpact;
        public final ModConfigSpec.DoubleValue defaultSalvageRate;
        public final ModConfigSpec.BooleanValue enableCraftingFallback;
        public final ModConfigSpec.DoubleValue minOperatingSpeed;

        public Server(ModConfigSpec.Builder builder) {
            builder.comment("Create: Reclamation Server Configuration").push("reclaimer");

            stressImpact = builder
                    .comment("Kinetic stress capacity consumed per RPM by the Mechanical Reclaimer.")
                    .defineInRange("stressImpact", 8.0, 0.0, 1024.0);

            defaultSalvageRate = builder
                    .comment("Chance (0.0 to 1.0) that a component is recovered intact during dismantling.",
                            "Failed rolls produce Salvaged Scrap.")
                    .defineInRange("defaultSalvageRate", 0.85, 0.0, 1.0);

            enableCraftingFallback = builder
                    .comment("Whether items without explicit reclaiming recipes can be dynamically deconstructed from crafting recipes.")
                    .define("enableCraftingFallback", true);

            minOperatingSpeed = builder
                    .comment("Minimum rotational speed (RPM) required for the Mechanical Reclaimer to operate.")
                    .defineInRange("minOperatingSpeed", 16.0, 0.0, 256.0);

            builder.pop();
        }
    }

    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        Pair<Server, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }
}
