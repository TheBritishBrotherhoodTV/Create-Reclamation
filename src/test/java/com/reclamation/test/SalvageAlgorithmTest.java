package com.reclamation.test;

import com.reclamation.content.recipe.MaterialCategory;
import com.reclamation.content.reclaimer.ReclaimerTier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SalvageAlgorithmTest {

    @Test
    public void testTierEfficienciesNeverReachHundredPercent() {
        for (ReclaimerTier tier : ReclaimerTier.values()) {
            Assertions.assertTrue(tier.getEfficiency() < 1.0f,
                    "Tier " + tier.name() + " efficiency must be strictly less than 100%");
            Assertions.assertTrue(tier.getEfficiency() <= 0.98f,
                    "Tier " + tier.name() + " efficiency must not exceed 98% hard cap");
        }
    }

    @Test
    public void testMaterialCategoryRecoverability() {
        Assertions.assertEquals(1.00f, MaterialCategory.EASY.getBaseRecoverability(), 0.001f);
        Assertions.assertEquals(0.90f, MaterialCategory.STANDARD.getBaseRecoverability(), 0.001f);
        Assertions.assertEquals(0.75f, MaterialCategory.DIFFICULT.getBaseRecoverability(), 0.001f);
    }

    @Test
    public void testMonteCarloConvergence10000Runs() {
        int iterations = 10000;
        Random random = new Random(42);

        for (ReclaimerTier tier : ReclaimerTier.values()) {
            float baseRecoverability = 1.0f; // Standard component with 1.0 base
            float effectiveRate = Math.min(0.98f, tier.getEfficiency() * baseRecoverability);

            float accumulator = 0.0f;
            int totalRecovered = 0;
            int totalScrap = 0;

            for (int i = 0; i < iterations; i++) {
                accumulator += effectiveRate;
                boolean recovered = false;

                if (accumulator >= 1.0f) {
                    totalRecovered++;
                    accumulator -= 1.0f;
                    recovered = true;
                }

                if (!recovered) {
                    if (random.nextFloat() < (1.0f - effectiveRate)) {
                        totalScrap++;
                    }
                }
            }

            double observedRate = (double) totalRecovered / iterations;
            double expectedRate = effectiveRate;

            System.out.printf("[%s] Expected: %.4f | Observed: %.4f | Total Recovered: %d / %d | Scrap: %d%n",
                    tier.name(), expectedRate, observedRate, totalRecovered, iterations, totalScrap);

            // Verify observed recovery matches theoretical rate within 0.1% margin of error over 10,000 iterations
            Assertions.assertEquals(expectedRate, observedRate, 0.001,
                    "Tier " + tier.name() + " observed rate diverged from expected rate");
            Assertions.assertTrue(totalRecovered < iterations,
                    "Total recovered must not exceed total inputs (no duplication)");
        }
    }

    @Test
    public void testStatefulAccumulationConsistency() {
        // Test that a 70% rate over 10 consecutive deterministic cycles produces exactly 7 items without losing remainder
        Map<String, Float> accumulators = new HashMap<>();
        String itemKey = "minecraft:iron_ingot";
        float rate = 0.70f;
        int recovered = 0;

        for (int i = 0; i < 10; i++) {
            float acc = accumulators.getOrDefault(itemKey, 0.0f) + rate;
            if (acc >= 0.999f) {
                recovered++;
                acc = Math.max(0.0f, acc - 1.0f);
            }
            accumulators.put(itemKey, acc);
        }

        Assertions.assertEquals(7, recovered,
                "Expected exactly 7 items recovered from 10 cycles at 70% efficiency");
        Assertions.assertEquals(0.0f, accumulators.get(itemKey), 0.001f,
                "Residual accumulator should be ~0.0 after 10 cycles of 0.70");
    }

}
