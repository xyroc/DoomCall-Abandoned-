package xiroc.doomcall.tileentity;

import org.apache.logging.log4j.Level;

import doom.lib.LogHelper;
import doom.lib.machine.IMachineCore;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import xiroc.doomcall.DoomCall;
import xiroc.doomcall.recipe.EMFRecipe;
import xiroc.doomcall.recipe.EMFRecipes;
import xiroc.doomcall.recipe.RecipeHandler;

public class TileEntityEnergeticMassFabricator extends TileEntityBasicStorage implements ISidedInventory, IMachineCore {

	private int burnTime;
	private int maxBurnTime;
	private ItemStack output;
	private EMFRecipe field_0a;

	private int updateTicker = 0;

	// TileEntityHVGenerator
	// ContainerHVGenerator
	// TileEntityFurnace

	private ItemStack[] items = new ItemStack[7];

	public TileEntityEnergeticMassFabricator() {
		super(0, 1000000);
		this.burnTime = 0;
		this.maxBurnTime = 0;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		updateTicker++;
		if (burnTime <= 0) {
			if (updateTicker >= 5) {
				updateTicker = 0;
				if (output != null) {
					if (worldObj != null) {
						if (getStackInSlot(6) == null) {
							setInventorySlotContents(6, output);
						} else {
							incrStackSize(6, output.stackSize);
						}
					}
					burnTime = -1;
					output = null;
				} else {
					EMFRecipe recipe = DoomCall.recipes0.getEMFRecipe(getRecipeItems());
					if (recipe != null) {
						ItemStack s = getStackInSlot(6);
						if (s != null) {
							if (!((s.stackSize + recipe.getResult().stackSize) > s.getMaxStackSize())) {
								addRecipe(recipe);
							}
						} else {
							addRecipe(recipe);
						}
					}
				}
			}
		} else {
			if (getEnergyStored(ForgeDirection.DOWN) >= 0) {
				storage.modifyEnergyStored(-500);
				burnTime--;
			} else {
				if (burnTime < maxBurnTime && burnTime > -1) {
					burnTime++;
				}
			}
		}
	}

	private void addRecipe(EMFRecipe recipe) {
		boolean load = false;
		if (items[6] == null || items[6].getItem() == recipe.getResult().getItem()) {
			for (ItemStack stack : recipe.getRecipeItems()) {
				for (int i = 0; i < items.length; i++) {
					if (stack != null && items[i] != null) {
						load = true;
						if (stack.getItem() == items[i].getItem()) {
							decrStackSize(i, stack.stackSize);
						}
					}
				}
			}
		}
		if (load) {
			this.field_0a = recipe;
			this.burnTime = recipe.getBurnTime();
			this.maxBurnTime = recipe.getBurnTime();
			this.output = recipe.getResult();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("burnTime", burnTime);
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < this.items.length; ++i) {
			if (this.items[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.items[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
		NBTTagCompound tag = new NBTTagCompound();
		if (output != null) {
			nbt.setBoolean("outputSaved", true);
			output.writeToNBT(tag);
			nbttaglist.appendTag(tag);
		}else{
			nbt.setBoolean("outputSaved", false);
		}
		nbt.setTag("Items", nbttaglist);
		this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.burnTime = nbt.getInteger("burnTime");
		boolean loadOutput = nbt.getBoolean("outputSaved");
		NBTTagList nbttaglist = nbt.getTagList("Items", 10);
		this.items = new ItemStack[7];
		int tagCount = nbttaglist.tagCount();
		if(loadOutput) tagCount--;
		for (int i = 0; i < tagCount; ++i) {
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			byte b0 = nbttagcompound1.getByte("Slot");
			if (b0 >= 0 && b0 < this.items.length) {
				this.items[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
		this.output = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(nbttaglist.tagCount()));
		this.markDirty();
	}

	@Override
	public int getSizeInventory() {
		return items.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return items[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if (this.items[slot] != null) {
			ItemStack itemstack;
			if (this.items[slot].stackSize <= amount) {
				itemstack = this.items[slot];
				this.items[slot] = null;
				this.markDirty();
				return itemstack;
			} else {
				itemstack = this.items[slot].splitStack(amount);
				if (this.items[slot].stackSize == 0) {
					this.items[slot] = null;
				}
				this.markDirty();
				return itemstack;
			}
		} else {
			this.markDirty();
			return null;
		}
	}

	public ItemStack incrStackSize(int slot, int amount) {
		if (getStackInSlot(slot) != null) {
			ItemStack stack = getStackInSlot(slot);
			if (stack.stackSize + amount <= stack.getMaxStackSize()) {
				stack.stackSize += amount;
			}
			this.markDirty();
			return stack;
		} else {
			this.markDirty();
			return null;
		}
	}

	private ItemStack putStack(ItemStack stack, int amount) {
		ItemStack itemStack = new ItemStack(stack.getItem(), amount, 0);
		this.markDirty();
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
		if (this.items[p_70304_1_] != null) {
			ItemStack itemstack = this.items[p_70304_1_];
			this.items[p_70304_1_] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
		this.items[p_70299_1_] = p_70299_2_;

		if (p_70299_2_ != null && p_70299_2_.stackSize > this.getInventoryStackLimit()) {
			p_70299_2_.stackSize = this.getInventoryStackLimit();
		}
		this.markDirty();
	}

	@Override
	public String getInventoryName() {
		return "Mass Fabricator";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory() {

	}

	@Override
	public void closeInventory() {

	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack p_94041_2_) {
		return slot != 6;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		return null;
	}

	@Override
	public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
		return this.isItemValidForSlot(p_102007_1_, p_102007_2_);
	}

	@Override
	public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
		return false;
	}

	private ItemStack[] getRecipeItems() {
		ItemStack[] recipeItems = new ItemStack[6];
		for (int i = 0; i < 6; i++) {
			recipeItems[i] = items[i];
		}
		return recipeItems;
	}

	public double getField(int id) {
		switch (id) {
		case 0:
			if (burnTime <= -1)
				return 0;
			double maxBurnTime = this.maxBurnTime;
			double burntime = burnTime;
			return (maxBurnTime - burntime) / maxBurnTime;
		default:
			return 0.00;
		}
	}

	@Override
	public int getMachineID() {
		return 1;
	}

}
