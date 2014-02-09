package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import extracells.container.ContainerBusIOFluid;
import extracells.gui.GuiBusIOFluid;
import extracells.network.packet.PacketBusIOFluid;
import extracells.util.ECPrivateInventory;
import extracells.util.FluidMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.io.IOException;
import java.util.Arrays;

public abstract class PartFluidIO extends PartECBase implements IGridTickable, ECPrivateInventory.IInventoryUpdateReceiver
{
	protected Fluid[] filterFluids = new Fluid[9];
	private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
	private FluidMode fluidMode = FluidMode.DROPS;
	protected byte filterSize;
	private ECPrivateInventory upgradeInventory = new ECPrivateInventory("", 4, 1, this)
	{
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			if (itemstack == null)
				return false;
			if (itemstack.isItemEqual(AEApi.instance().materials().materialCardCapacity.stack(1)))
				return true;
			else if (itemstack.isItemEqual(AEApi.instance().materials().materialCardSpeed.stack(1)))
				return true;
			else if (itemstack.isItemEqual(AEApi.instance().materials().materialCardRedstone.stack(1)))
				return true;
			return false;
		}
	};

	@Override
	public abstract void renderInventory(IPartRenderHelper rh, RenderBlocks renderer);

	@Override
	public abstract void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer);

	@Override
	public final void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{
	}

	public ECPrivateInventory getUpgradeInventory()
	{
		return upgradeInventory;
	}

	@Override
	public final void writeToNBT(NBTTagCompound data)
	{
		data.setInteger("fluidMode", fluidMode.ordinal());
		data.setInteger("redstoneMode", redstoneMode.ordinal());
		for (int i = 0; i < filterFluids.length; i++)
		{
			Fluid fluid = filterFluids[i];
			if (fluid != null)
				data.setString("FilterFluid#" + i, fluid.getName());
			else
				data.setString("FilterFluid#" + i, "");
		}
		data.setTag("upgradeInventory", upgradeInventory.writeToNBT());
	}

	@Override
	public final void readFromNBT(NBTTagCompound data)
	{
		redstoneMode = RedstoneMode.values()[data.getInteger("redstoneMode")];
		fluidMode = FluidMode.values()[data.getInteger("fluidMode")];
		for (int i = 0; i < filterFluids.length; i++)
		{
			filterFluids[i] = FluidRegistry.getFluid(data.getString("FilterFluid#" + i));
		}
		upgradeInventory.readFromNBT(data.getTagList("upgradeInventory", 10));// TODO
		onInventoryChanged();
	}

	@Override
	public final void writeToStream(ByteBuf data) throws IOException
	{
	}

	@Override
	public final boolean readFromStream(ByteBuf data) throws IOException
	{
		return false;
	}

	@Override
	public abstract void getBoxes(IPartCollsionHelper bch);

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public final TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest(1, 20, false, false);
	}

	@Override
	public final TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return doWork(250, TicksSinceLastCall) ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	public abstract boolean doWork(int rate, int TicksSinceLastCall);

	public final void setFilterFluid(int index, Fluid fluid, EntityPlayer player)
	{
		filterFluids[index] = fluid;
		new PacketBusIOFluid(Arrays.asList(filterFluids)).sendPacketToPlayer(player);
	}

	public FluidMode getFluidMode()
	{
		return fluidMode;
	}

	public RedstoneMode getRedstoneMode()
	{
		return redstoneMode;
	}

	public void loopRedstoneMode(EntityPlayer player)
	{
		if (redstoneMode.ordinal() + 1 < RedstoneMode.values().length)
			redstoneMode = RedstoneMode.values()[redstoneMode.ordinal() + 1];
		else
			redstoneMode = RedstoneMode.values()[0];
		new PacketBusIOFluid((byte) 0, (byte) redstoneMode.ordinal()).sendPacketToPlayer(player);
	}

	public void loopFluidMode(EntityPlayer player)
	{
		if (fluidMode.ordinal() + 1 < FluidMode.values().length)
			fluidMode = FluidMode.values()[fluidMode.ordinal() + 1];
		else
			fluidMode = FluidMode.values()[0];
		new PacketBusIOFluid((byte) 1, (byte) fluidMode.ordinal()).sendPacketToPlayer(player);
	}

	public Object getServerGuiElement(EntityPlayer player)
	{
		return new ContainerBusIOFluid(this, player);
	}

	public Object getClientGuiElement(EntityPlayer player)
	{
		return new GuiBusIOFluid(this, player);
	}

	public void sendInformation(EntityPlayer player)
	{
		new PacketBusIOFluid(Arrays.asList(filterFluids)).sendPacketToPlayer(player);
		new PacketBusIOFluid((byte) 0, (byte) redstoneMode.ordinal()).sendPacketToPlayer(player);
		new PacketBusIOFluid((byte) 1, (byte) fluidMode.ordinal()).sendPacketToPlayer(player);
		new PacketBusIOFluid(filterSize).sendPacketToPlayer(player);
	}

	@Override
	public void onInventoryChanged()
	{
		filterSize = 0;
		for (int i = 0; i < upgradeInventory.getSizeInventory(); i++)
		{
			ItemStack currentStack = upgradeInventory.getStackInSlot(i);
			if (currentStack != null && currentStack.isItemEqual(AEApi.instance().materials().materialCardCapacity.stack(1)))
				filterSize++;
		}
		new PacketBusIOFluid(filterSize).sendPacketToAllPlayers();
		// TODO add speed etc.
	}
}
