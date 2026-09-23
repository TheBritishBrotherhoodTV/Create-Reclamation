package com.reclamation.content.reclaimer;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;

public class ReclaimerDisplaySource extends PercentOrProgressBarDisplaySource {

    @Override
    protected Float getProgress(DisplayLinkContext context) {
        if (context.getSourceBlockEntity() instanceof MechanicalReclaimerBlockEntity be) {
            return be.getProcessingProgress();
        }
        return 0.0f;
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    public int getPassiveRefreshTicks() {
        return 5;
    }

    @Override
    protected String getTranslationKey() {
        return "reclaimer_progress";
    }
}
