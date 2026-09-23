package com.reclamation.infrastructure.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ReclamationConfig {

    public static class Server {
        public final ModConfigSpec.DoubleValue mechanicalEfficiency;
        public final ModConfigSpec.DoubleValue precisionEfficiency;
        public final ModConfigSpec.DoubleValue industrialEfficiency;
        public final ModConfigSpec.DoubleValue advancedEfficiency;

        public final ModConfigSpec.DoubleValue mechanicalStress;
        public final ModConfigSpec.DoubleValue precisionStress;
        public final ModConfigSpec.DoubleValue industrialStress;
        public final ModConfigSpec.DoubleValue advancedStress;

        public final ModConfigSpec.BooleanValue enableCraftingFallback;
        public final ModConfigSpec.DoubleValue scrapConversionRate;

        public Server(ModConfigSpec.Builder builder) {
            builder.comment("Create: Reclamation Server Configuration").push("reclaimer_tiers");

            builder.comment("Machine Efficiency Ratings (0.01 to 0.98). No tier may reach 1.0 (100%).");
            mechanicalEfficiency = builder
                    .comment("Recovery efficiency of Tier 1 Mechanical Reclaimer (Default: 0.70 / 70%)")
                    .defineInRange("mechanicalEfficiency", 0.70, 0.05, 0.98);

            precisionEfficiency = builder
                    .comment("Recovery efficiency of Tier 2 Precision Reclaimer (Default: 0.85 / 85%)")
                    .defineInRange("precisionEfficiency", 0.85, 0.05, 0.98);

            industrialEfficiency = builder
                    .comment("Recovery efficiency of Tier 3 Industrial Reclaimer (Default: 0.94 / 94%)")
                    .defineInRange("industrialEfficiency", 0.94, 0.05, 0.98);

            advancedEfficiency = builder
                    .comment("Recovery efficiency of Tier 4 Advanced Reclaimer (Default: 0.98 / 98%)")
                    .defineInRange("advancedEfficiency", 0.98, 0.05, 0.98);

            builder.pop().push("stress_impacts");

            mechanicalStress = builder
                    .comment("Stress Capacity (SU per RPM) consumed by Mechanical Reclaimer.")
                    .defineInRange("mechanicalStress", 8.0, 0.0, 1024.0);

            precisionStress = builder
                    .comment("Stress Capacity (SU per RPM) consumed by Precision Reclaimer.")
                    .defineInRange("precisionStress", 12.0, 0.0, 1024.0);

            industrialStress = builder
                    .comment("Stress Capacity (SU per RPM) consumed by Industrial Reclaimer.")
                    .defineInRange("industrialStress", 16.0, 0.0, 1024.0);

            advancedStress = builder
                    .comment("Stress Capacity (SU per RPM) consumed by Advanced Reclaimer.")
                    .defineInRange("advancedStress", 24.0, 0.0, 1024.0);

            builder.pop().push("rules");

            enableCraftingFallback = builder
                    .comment("Whether items without explicit reclaiming recipes can be dynamically deconstructed from unambiguous crafting recipes.")
                    .define("enableCraftingFallback", true);

            scrapConversionRate = builder
                    .comment("Multiplier for Salvaged Scrap generated from unrecovered material loss.")
                    .defineInRange("scrapConversionRate", 1.0, 0.0, 5.0);

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
