package com.reclamation.infrastructure.ponder;

import com.reclamation.content.reclaimer.MechanicalReclaimerBlockEntity;
import com.reclamation.registry.ModBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ReclamationPonders {

    public static void reclaimerTutorial(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_reclaimer", "Reclaiming Manufactured Items");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos reclaimerPos = util.grid().at(2, 1, 2);
        BlockPos shaftPos = util.grid().at(2, 1, 3);
        BlockPos crankPos = util.grid().at(2, 1, 4);

        BlockState reclaimerState = ModBlocks.MECHANICAL_RECLAIMER.getDefaultState()
                .setValue(HorizontalKineticBlock.HORIZONTAL_FACING, Direction.NORTH);
        scene.world().setBlock(reclaimerPos, reclaimerState, false);

        BlockState shaftState = AllBlocks.SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z);
        scene.world().setBlock(shaftPos, shaftState, false);

        BlockState crankState = AllBlocks.HAND_CRANK.getDefaultState()
                .setValue(BlockStateProperties.FACING, Direction.SOUTH);
        scene.world().setBlock(crankPos, crankState, false);

        Selection reclaimerSelection = util.select().position(reclaimerPos);
        Selection kineticSelection = util.select().fromTo(shaftPos, crankPos);

        scene.idle(5);
        ElementLink<WorldSectionElement> reclaimerElement = scene.world().showIndependentSection(reclaimerSelection, Direction.DOWN);
        scene.world().moveSection(reclaimerElement, util.vector().of(0, 0, 0), 0);
        scene.idle(10);

        scene.overlay().showText(70)
                .text("The Mechanical Reclaimer dismantles crafted items into their constituent ingredients.")
                .pointAt(util.vector().topOf(reclaimerPos))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(kineticSelection, Direction.NORTH);
        scene.world().setKineticSpeed(kineticSelection, 32.0f);
        scene.world().setKineticSpeed(reclaimerSelection, 32.0f);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("It requires rotational power supplied to its rear shaft socket to operate.")
                .pointAt(util.vector().centerOf(shaftPos))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Right-click or supply manufactured items (such as a Mechanical Press) to begin dismantling.")
                .pointAt(util.vector().topOf(reclaimerPos))
                .placeNearTarget();
        scene.idle(70);

        ItemStack testPress = AllBlocks.MECHANICAL_PRESS.asStack();
        scene.world().modifyBlockEntity(reclaimerPos, MechanicalReclaimerBlockEntity.class, be -> {
            be.setInputStack(testPress);
        });
        scene.idle(30);

        scene.overlay().showText(60)
                .colored(PonderPalette.GREEN)
                .text("Higher rotational speeds (RPM) accelerate the dismantling process.")
                .pointAt(util.vector().topOf(reclaimerPos))
                .placeNearTarget();
        scene.idle(70);

        scene.world().modifyBlockEntity(reclaimerPos, MechanicalReclaimerBlockEntity.class, be -> {
            be.setInputStack(ItemStack.EMPTY);
            be.outputInv.setStackInSlot(0, AllItems.ANDESITE_ALLOY.asStack(1));
            be.outputInv.setStackInSlot(1, AllBlocks.SHAFT.asStack(1));
            be.outputInv.setStackInSlot(2, new ItemStack(Items.IRON_BLOCK));
        });

        scene.overlay().showText(70)
                .colored(PonderPalette.OUTPUT)
                .text("Recovered ingredients can be extracted manually or automatically using Create Funnels, Chutes, and Hoppers.")
                .pointAt(util.vector().topOf(reclaimerPos))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    public static void reclaimerAutomation(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("reclaimer_automation", "Automating Mechanical Reclamation");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos reclaimerPos = util.grid().at(2, 1, 2);
        BlockPos shaftPos = util.grid().at(2, 1, 3);
        BlockPos chutePos = util.grid().at(2, 2, 2);
        BlockPos funnelPos = util.grid().at(2, 1, 1);

        BlockState reclaimerState = ModBlocks.MECHANICAL_RECLAIMER.getDefaultState()
                .setValue(HorizontalKineticBlock.HORIZONTAL_FACING, Direction.NORTH);
        scene.world().setBlock(reclaimerPos, reclaimerState, false);

        BlockState shaftState = AllBlocks.SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z);
        scene.world().setBlock(shaftPos, shaftState, false);

        BlockState chuteState = AllBlocks.CHUTE.getDefaultState();
        scene.world().setBlock(chutePos, chuteState, false);

        BlockState funnelState = AllBlocks.ANDESITE_FUNNEL.getDefaultState()
                .setValue(BlockStateProperties.FACING, Direction.NORTH);
        scene.world().setBlock(funnelPos, funnelState, false);

        Selection machineSelection = util.select().position(reclaimerPos);
        Selection chuteSelection = util.select().position(chutePos);
        Selection funnelSelection = util.select().position(funnelPos);
        Selection shaftSelection = util.select().position(shaftPos);

        scene.world().showSection(machineSelection, Direction.DOWN);
        scene.world().showSection(shaftSelection, Direction.NORTH);
        scene.world().setKineticSpeed(shaftSelection, 64.0f);
        scene.world().setKineticSpeed(machineSelection, 64.0f);
        scene.idle(10);

        scene.world().showSection(chuteSelection, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(70)
                .text("Chutes and Funnels mounted on the Reclaimer automatically feed items into the input slot.")
                .pointAt(util.vector().topOf(chutePos))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(funnelSelection, Direction.SOUTH);
        scene.idle(10);

        scene.overlay().showText(70)
                .colored(PonderPalette.OUTPUT)
                .text("Funnels placed on the sides or front automatically pull completed recovered ingredients out.")
                .pointAt(util.vector().centerOf(funnelPos))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
