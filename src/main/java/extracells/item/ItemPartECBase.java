package extracells.item;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemPartECBase extends Item implements IPartItem
{
	private static List<Class<? extends IPart>> ecParts = new ArrayList<Class<? extends IPart>>();

	public ItemPartECBase()
	{
		AEApi.instance().partHelper().setItemBusRenderer(this);
	}

	@Override
	public IPart createPartFromItemStack(ItemStack is)
	{
		try
		{
			return ecParts.get(is.getItemDamage()).newInstance();
		} catch (Throwable e)
		{
			return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}

	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList)
	{
		for (int i = 0; i < ecParts.size(); i++)
		{
			if (ecParts.get(i) != null)
				itemList.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return AEApi.instance().partHelper().placeBus(is, x, y, z, side, player, world);
	}

	public static void registerPart(Class<? extends IPart> part)
	{
		ecParts.add(part);
	}

	public static int getPartId(Class<? extends IPart> part)
	{
		return ecParts.indexOf(part);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		switch (itemstack.getItemDamage())
		{
		case 0:
			return "part.fluid.export";
		case 1:
			return "part.fluid.import";
		case 2:
			return "part.fluid.storage";
		case 3:
			return "part.fluid.terminal";
		default:
			return "INVALID PART!";
		}
	}
}
