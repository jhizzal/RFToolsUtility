package mcjty.rftoolsutility.modules.teleporter.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.DimensionId;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.lib.builder.TooltipBuilder.*;

public class MatterReceiverBlock extends BaseBlock {

    public MatterReceiverBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsUtilityTOPDriver.DRIVER)
                .manualEntry(ManualHelper.create("rftoolsutility:machines/matter_receiver"))
                .infusable()
                .info(key("message.rftoolsutility.shiftmessage"))
                .infoShift(header(), gold(), parameter("info", MatterReceiverBlock::getName))
                .tileEntitySupplier(MatterReceiverTileEntity::new));
    }

    private static String getName(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            return tag.getString("tpName");
        } else {
            return "<unset>";
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        World world = context.getLevel();
        if (world.isClientSide) {
            return state;
        }
        TeleportDestinations destinations = TeleportDestinations.get(world);

        BlockPos pos = context.getClickedPos();
        GlobalCoordinate gc = new GlobalCoordinate(pos, world);

        destinations.getNewId(gc);
        destinations.addDestination(gc);
        destinations.save();

        return state;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // We don't want what BaseBlock does.
        // This is called AFTER onBlockPlaced below. Here we need to fix the destination settings.
        // @todo 1.14 check
//        restoreBlockFromNBT(world, pos, stack);
        if (!world.isClientSide) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) world.getBlockEntity(pos);
            matterReceiverTileEntity.getOrCalculateID();
            matterReceiverTileEntity.updateDestination();
        }
        setOwner(world, pos, placer);
    }

    @Override
    public void onRemove(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
        super.onRemove(state, world, pos, newstate, isMoving);
        if (world.isClientSide) {
            return;
        }
        TeleportDestinations destinations = TeleportDestinations.get(world);
        destinations.removeDestination(pos, DimensionId.fromWorld(world));
        destinations.save();
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }
}
