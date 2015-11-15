package moze_intel.projecte.gameObjs.tiles;

import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.SyncPedestalPKT;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Random;

public class DMPedestalTile extends TileEmc implements IInventory
{
	private boolean isActive = false;
	private ItemStack[] inventory = new ItemStack[1];
	private AxisAlignedBB effectBounds;
	private int particleCooldown = 10;
	private int activityCooldown = 0;
	public double centeredX, centeredY, centeredZ;
	private EntityItem ghost;


	public DMPedestalTile()
	{
		super();
	}

	@Override
	public void update()
	{
		if (worldObj.isRemote)
		{
			if (!worldObj.isBlockLoaded(pos, false))
			{
				// Handle condition where this method is called even after the clientside chunk has unloaded.
				// This will make IPedestalItems below crash with an NPE since the TE they get back is null
				// Don't you love vanilla???
				return;
			}
		}

		centeredX = pos.getX() + 0.5;
		centeredY = pos.getY() + 0.5;
		centeredZ = pos.getZ() + 0.5;

		if (effectBounds == null)
		{
			effectBounds = new AxisAlignedBB(centeredX - 4.5, centeredY - 4.5, centeredZ - 4.5,
					centeredX + 4.5, centeredY + 4.5, centeredZ + 4.5);
		}

		if (worldObj.isRemote)
		{
			checkGhostItem();
		}

		if (getActive())
		{
			if (getItemStack() != null)
			{
				Item item = getItemStack().getItem();
				if (item instanceof IPedestalItem)
				{
					((IPedestalItem) item).updateInPedestal(worldObj, getPos());
				}
				if (particleCooldown <= 0)
				{
					spawnParticles();
					particleCooldown = 10;
				}
				else
				{
					particleCooldown--;
				}
			}
			else
			{
				setActive(false);
			}
		}
	}

	public void checkGhostItem()
	{
		if (!worldObj.isRemote)
		{
			return;
		}
		if (inventory[0] == null && ghost != null)
		{
			ghost.setDead();
			ghost = null;
		}
		if (inventory[0] != null && ghost == null)
		{
			ghost = new EntityItemUnmoving(worldObj, pos.getX() + 0.5, pos.getY() + 0.751, pos.getZ() + 0.5, inventory[0].copy());
			ghost.setNoDespawn();
			ghost.setInfinitePickupDelay();
			ghost.motionX = 0;
			ghost.motionY = 0;
			ghost.motionZ = 0;
			worldObj.spawnEntityInWorld(ghost);
		}
	}

	@Override
	public void invalidate()
	{
		if (ghost != null && worldObj.isRemote)
		{
			ghost.setDead();
			ghost = null;
		}
	}

	private void spawnParticles()
	{
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		worldObj.spawnParticle(EnumParticleTypes.FLAME, x + 0.2, y + 0.3, z + 0.2, 0, 0, 0);
		worldObj.spawnParticle(EnumParticleTypes.FLAME, x + 0.2, y + 0.3, z + 0.5, 0, 0, 0);
		worldObj.spawnParticle(EnumParticleTypes.FLAME, x + 0.2, y + 0.3, z + 0.8, 0, 0, 0);
		worldObj.spawnParticle(EnumParticleTypes.FLAME, x + 0.5, y + 0.3, z + 0.2, 0, 0, 0);
		worldObj.spawnParticle(EnumParticleTypes.FLAME, x + 0.5, y + 0.3, z + 0.8, 0, 0, 0);
		worldObj.spawnParticle(EnumParticleTypes.FLAME, x + 0.8, y + 0.3, z + 0.2, 0, 0, 0);
		worldObj.spawnParticle(EnumParticleTypes.FLAME, x + 0.8, y + 0.3, z + 0.5, 0, 0, 0);
		worldObj.spawnParticle(EnumParticleTypes.FLAME, x + 0.8, y + 0.3, z + 0.8, 0, 0, 0);

		Random rand = worldObj.rand;
		for (int i = 0; i < 3; ++i)
		{
			int j = rand.nextInt(2) * 2 - 1;
			int k = rand.nextInt(2) * 2 - 1;
			double d0 = (double)pos.getX() + 0.5D + 0.25D * (double)j;
			double d1 = (double)((float)pos.getY() + rand.nextFloat());
			double d2 = (double)pos.getZ() + 0.5D + 0.25D * (double)k;
			double d3 = (double)(rand.nextFloat() * (float)j);
			double d4 = ((double)rand.nextFloat() - 0.5D) * 0.125D;
			double d5 = (double)(rand.nextFloat() * (float)k);
			worldObj.spawnParticle(EnumParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
		}
	}

	public int getActivityCooldown()
	{
		return activityCooldown;
	}

	public void setActivityCooldown(int i)
	{
		activityCooldown = i;
	}

	public void decrementActivityCooldown()
	{
		activityCooldown--;
	}

	public ItemStack getItemStack()
	{
		return getStackInSlot(0);
	}

	public AxisAlignedBB getEffectBounds()
	{
		if (effectBounds == null)
		{
			// Chunk is still loading weirdness, return an empty box just for this tick.
			return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		}
		return effectBounds;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);

		inventory = new ItemStack[getSizeInventory()];
		NBTTagList tagList = tag.getTagList("Items", 10);
		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			NBTTagCompound compound = tagList.getCompoundTagAt(i);
			byte slot = compound.getByte("Slot");
			if (slot >= 0 && slot < inventory.length)
			{
				inventory[slot] = ItemStack.loadItemStackFromNBT(compound);
			}
		}
		setActive(tag.getBoolean("isActive"));
		activityCooldown = tag.getInteger("activityCooldown");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);

		NBTTagList tagList = new NBTTagList();

		for (int i = 0; i < this.inventory.length; ++i)
		{
			if (this.inventory[i] != null)
			{
				NBTTagCompound compound = new NBTTagCompound();
				compound.setByte("Slot", (byte)i);
				this.inventory[i].writeToNBT(compound);
				tagList.appendTag(compound);
			}
		}

		tag.setTag("Items", tagList);
		tag.setBoolean("isActive", getActive());
		tag.setInteger("activityCooldown", activityCooldown);
	}

	@Override
	public int getSizeInventory()
	{
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amt)
	{
		ItemStack result = inventory[slot];
		if (inventory[slot] != null)
		{
			if (amt > inventory[slot].stackSize)
			{
				setInventorySlotContents(slot, null);
			}
			else
			{
				result = inventory[slot].splitStack(amt);
				if (inventory[slot].stackSize <= 0)
				{
					setInventorySlotContents(slot, null);
				}
			}
		}
		return result;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return inventory[slot];
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack)
	{
		inventory[slot] = itemStack;

		if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit())
		{
			itemStack.stackSize = this.getInventoryStackLimit();
		}
		this.markDirty();
	}

	@Override
	public String getCommandSenderName()
	{
		return "pe.pedestal.shortname";
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public IChatComponent getDisplayName()
	{
		return new ChatComponentTranslation(getCommandSenderName());
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return this.worldObj.getTileEntity(this.pos) != this ? false : var1.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) { }

	@Override
	public void closeInventory(EntityPlayer player)
	{
		this.markDirty();
	}

	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_)
	{
		return true;
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		Arrays.fill(inventory, null);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketHandler.getMCPacket(new SyncPedestalPKT(this));
	}

	public boolean getActive()
	{
		return isActive;
	}

	public void setActive(boolean newState)
	{
		if (newState != this.getActive() && worldObj != null)
		{
			if (newState)
			{
				worldObj.playSoundEffect(centeredX, centeredY, centeredZ, "projecte:item.pecharge", 1.0F, 1.0F);
				for (int i = 0; i < worldObj.rand.nextInt(35) + 10; ++i)
				{
					this.worldObj.spawnParticle(EnumParticleTypes.SPELL_WITCH, centeredX + worldObj.rand.nextGaussian() * 0.12999999523162842D,
							getPos().getY() + 1 + worldObj.rand.nextGaussian() * 0.12999999523162842D,
							centeredZ + worldObj.rand.nextGaussian() * 0.12999999523162842D,
							0.0D, 0.0D, 0.0D);
				}
			}
			else
			{
				worldObj.playSoundEffect(centeredX, centeredY, centeredZ, "projecte:item.peuncharge", 1.0F, 1.0F);
				for (int i = 0; i < worldObj.rand.nextInt(35) + 10; ++i)
				{
					this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, centeredX + worldObj.rand.nextGaussian() * 0.12999999523162842D,
							getPos().getY() + 1 + worldObj.rand.nextGaussian() * 0.12999999523162842D,
							centeredZ + worldObj.rand.nextGaussian() * 0.12999999523162842D,
							0.0D, 0.0D, 0.0D);
				}
			}
		}
		this.isActive = newState;
	}

	public static class EntityItemUnmoving extends EntityItem
	{
		private final double x, y, z;

		public EntityItemUnmoving(World worldIn, double x, double y, double z, ItemStack stack)
		{
			super(worldIn, x, y, z, stack);
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public void onUpdate()
		{
			super.onUpdate();
			motionX = 0; motionY = 0; motionZ = 0;
			posX = x; posY = y; posZ = z;
		}
	}
}
