package extracells.block;

import extracells.Extracells;
import extracells.tileentity.TileEntityWalrus;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockWalrus extends Block implements ITileEntityProvider {

    public BlockWalrus() {
        super(Material.clay);
        setCreativeTab(Extracells.ModTab);
        setBlockName("extracells.block.walrus");
        setHardness(2.0F);
        setResistance(10.0F);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemstack) {
        int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

        if (l == 0) {
            world.setBlockMetadataWithNotify(x, y, z, 2, 2);
        }

        if (l == 1) {
            world.setBlockMetadataWithNotify(x, y, z, 5, 2);
        }

        if (l == 2) {
            world.setBlockMetadataWithNotify(x, y, z, 3, 2);
        }

        if (l == 3) {
            world.setBlockMetadataWithNotify(x, y, z, 4, 2);
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAcces, int x, int y, int z) {
        switch (ForgeDirection.getOrientation(blockAcces.getBlockMetadata(x, y, z))) {
            case NORTH:
                setBlockBounds(0.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F);
                break;
            case EAST:
                setBlockBounds(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F);
                break;
            case SOUTH:
                setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2.0F);
                break;
            case WEST:
                setBlockBounds(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                break;
            default:
                setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                break;
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWalrus();
    }
}
