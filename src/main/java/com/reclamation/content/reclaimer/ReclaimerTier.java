package com.reclamation.content.reclaimer;

import com.reclamation.infrastructure.config.ReclamationConfig;

import java.util.function.Supplier;

public enum ReclaimerTier {
    MECHANICAL("mechanical", "Mechanical Reclaimer", 0.70f, 8.0f, 16.0f, 1.00f),
    PRECISION("precision", "Precision Reclaimer", 0.85f, 12.0f, 32.0f, 1.25f),
    INDUSTRIAL("industrial", "Industrial Reclaimer", 0.94f, 16.0f, 32.0f, 1.50f),
    ADVANCED("advanced", "Advanced Reclaimer", 0.98f, 24.0f, 64.0f, 2.00f);

    private final String id;
    private final String displayName;
    private final float defaultEfficiency;
    private final float defaultStress;
    private final float minSpeed;
    private final float processingSpeedMultiplier;

    ReclaimerTier(String id, String displayName, float defaultEfficiency, float defaultStress,
                  float minSpeed, float processingSpeedMultiplier) {
        this.id = id;
        this.displayName = displayName;
        this.defaultEfficiency = defaultEfficiency;
        this.defaultStress = defaultStress;
        this.minSpeed = minSpeed;
        this.processingSpeedMultiplier = processingSpeedMultiplier;
    }


    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getEfficiency() {
        float eff = defaultEfficiency;
        try {
            if (ReclamationConfig.SERVER_SPEC.isLoaded()) {
                eff = switch (this) {
                    case MECHANICAL -> ReclamationConfig.SERVER.mechanicalEfficiency.get().floatValue();
                    case PRECISION -> ReclamationConfig.SERVER.precisionEfficiency.get().floatValue();
                    case INDUSTRIAL -> ReclamationConfig.SERVER.industrialEfficiency.get().floatValue();
                    case ADVANCED -> ReclamationConfig.SERVER.advancedEfficiency.get().floatValue();
                };
            }
        } catch (Throwable ignored) {}

        // Enforce hard cap: no tier may ever reach 100% (max 0.98)
        return Math.min(0.98f, Math.max(0.01f, eff));
    }

    public float getStressImpact() {
        try {
            if (ReclamationConfig.SERVER_SPEC.isLoaded()) {
                return switch (this) {
                    case MECHANICAL -> ReclamationConfig.SERVER.mechanicalStress.get().floatValue();
                    case PRECISION -> ReclamationConfig.SERVER.precisionStress.get().floatValue();
                    case INDUSTRIAL -> ReclamationConfig.SERVER.industrialStress.get().floatValue();
                    case ADVANCED -> ReclamationConfig.SERVER.advancedStress.get().floatValue();
                };
            }
        } catch (Throwable ignored) {}
        return defaultStress;
    }

    public float getMinOperatingSpeed() {
        return minSpeed;
    }

    public float getProcessingSpeedMultiplier() {
        return processingSpeedMultiplier;
    }
}

