package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxContainerItem;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.entities.EntityRailgunShot;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import cofh.api.energy.IEnergyContainerItem;
import com.google.common.base.Optional;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.List;

public class ItemRailgun extends ItemUpgradeableTool implements IFluxContainerItem,IEnergyContainerItem, IZoomTool, ITool, IOBJModelCallback<ItemStack>
{
	public ItemRailgun()
	{
		super("railgun", 1, "RAILGUN");
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 2+1;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem)
	{
		return new Slot[]
				{
						new IESlot.Upgrades(container, invItem,0, 80,32, "RAILGUN", stack, true),
						new IESlot.Upgrades(container, invItem,1,100,32, "RAILGUN", stack, true)
				};
	}
	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}
	@Override
	public void recalculateUpgrades(ItemStack stack)
	{
		super.recalculateUpgrades(stack);
		if(this.getEnergyStored(stack)>this.getMaxEnergyStored(stack))
			ItemNBTHelper.setInt(stack, "energy", this.getMaxEnergyStored(stack));
	}
	@Override
	public void clearUpgrades(ItemStack stack)
	{
		super.clearUpgrades(stack);
		if(this.getEnergyStored(stack)>this.getMaxEnergyStored(stack))
			ItemNBTHelper.setInt(stack, "energy", this.getMaxEnergyStored(stack));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged)
			return true;
		if(oldStack.hasCapability(CapabilityShader.SHADER_CAPABILITY,null) && newStack.hasCapability(CapabilityShader.SHADER_CAPABILITY,null))
		{
			ShaderWrapper wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY,null);
			ShaderWrapper wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY,null);
			if(!ItemStack.areItemStacksEqual(wrapperOld.getShaderItem(), wrapperNew.getShaderItem()))
				return true;
		}
		return super.shouldCauseReequipAnimation(oldStack,newStack,slotChanged);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		return new ICapabilityProvider()
		{
			ShaderWrapper_Item shaders = new ShaderWrapper_Item("immersiveengineering:railgun", stack);
			@Override
			public boolean hasCapability(Capability<?> capability, EnumFacing facing)
			{
				return capability== CapabilityShader.SHADER_CAPABILITY;
			}
			@Override
			public <T> T getCapability(Capability<T> capability, EnumFacing facing)
			{
				if(capability==CapabilityShader.SHADER_CAPABILITY)
					return (T)shaders;
				return null;
			}
		};
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
		list.add(I18n.format(Lib.DESC+"info.energyStored", stored));
	}
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		//		if(stack.getItemDamage()!=1)
		//		{
		//			String tag = getRevolverDisplayTag(stack);
		//			if(!tag.isEmpty())
		//				return this.getUnlocalizedName()+"."+tag;
		//		}
		return super.getUnlocalizedName(stack);
	}
	@Override
	public boolean isFull3D()
	{
		return true;
	}

	//	@Override
	//	public Multimap getAttributeModifiers(ItemStack stack)
	//	{
	//		Multimap multimap = super.getAttributeModifiers(stack);
	//		double melee = getUpgrades(stack).getDouble("melee");
	//		if(melee!=0)
	//			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", melee, 0));
	//		double speed = getUpgrades(stack).getDouble("speed");
	//		if(speed!=0)
	//			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", speed, 1));
	//		return multimap;
	//	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.NONE;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		//		if(!world.isRemote && stack.getItemDamage()!=1 && ent!=null && ItemNBTHelper.hasKey(stack, "blocked"))
		//		{
		//			int l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "casings", ent);
		//			if(l==0)
		//				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderFill", "tile.piston.in",.3f,3, 1,6,1);
		//			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderFill", ent);
		//			if(l==0)
		//				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderClose", "fire.ignite",.6f,5, 1,6,1);
		//			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderClose", ent);
		//			if(l==0)
		//				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderSpin", "note.hat",.1f,5, 5,8,1);
		//			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderSpin", ent);
		//			if(l==0)
		//				ItemNBTHelper.remove(stack, "blocked");
		//		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
	{
		int energy = IEConfig.Tools.railgun_consumption;
		float energyMod = 1 + this.getUpgrades(stack).getFloat("consumption");
		energy = (int)(energy*energyMod);
		if(this.extractEnergy(stack, energy, true)==energy && findAmmo(player)!=null)
		{
			player.setActiveHand(hand);
			player.playSound(getChargeTime(stack) <= 20 ? IESounds.chargeFast : IESounds.chargeSlow, 1.5f, 1);
			return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult(EnumActionResult.PASS, stack);
	}
	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase user, int count)
	{
		int inUse = this.getMaxItemUseDuration(stack)-count;
		if(inUse>getChargeTime(stack) && inUse%20 == user.getRNG().nextInt(20))
			user.playSound(IESounds.spark, .8f+(.2f*user.getRNG().nextFloat()), .5f+(.5f*user.getRNG().nextFloat()));
	}
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase user, int timeLeft)
	{
		if(user instanceof EntityPlayer)
		{
			int inUse = this.getMaxItemUseDuration(stack) - timeLeft;
			ItemNBTHelper.remove(stack, "inUse");
			if (inUse < getChargeTime(stack))
				return;
			int energy = IEConfig.Tools.railgun_consumption;
			float energyMod = 1 + this.getUpgrades(stack).getFloat("consumption");
			energy = (int) (energy * energyMod);
			if (this.extractEnergy(stack, energy, true) == energy)
			{
				ItemStack ammo = findAmmo((EntityPlayer)user);
				if(ammo!=null)
				{
					Vec3d vec = user.getLookVec();
					float speed = 20;
					EntityRailgunShot shot = new EntityRailgunShot(user.worldObj, user, vec.xCoord * speed, vec.yCoord * speed, vec.zCoord * speed, Utils.copyStackWithAmount(ammo, 1));
					ammo.stackSize--;
					if(ammo.stackSize<=0)
						((EntityPlayer)user).inventory.deleteStack(ammo);
					user.playSound(IESounds.railgunFire, 1, .5f + (.5f * user.getRNG().nextFloat()));
					this.extractEnergy(stack, energy, false);
					if (!world.isRemote)
						user.worldObj.spawnEntityInWorld(shot);
				}
			}
		}
	}

	public static ItemStack findAmmo(EntityPlayer player)
	{
		if(isAmmo(player.getHeldItem(EnumHand.OFF_HAND)))
			return player.getHeldItem(EnumHand.OFF_HAND);
		else if(isAmmo(player.getHeldItem(EnumHand.MAIN_HAND)))
			return player.getHeldItem(EnumHand.MAIN_HAND);
		else
			for(int i=0; i<player.inventory.getSizeInventory(); i++)
			{
				ItemStack itemstack = player.inventory.getStackInSlot(i);
				if(isAmmo(itemstack))
					return itemstack;
			}
		return null;
	}
	public static boolean isAmmo(ItemStack stack)
	{
		if(stack == null)
			return false;
		RailgunHandler.RailgunProjectileProperties prop = RailgunHandler.getProjectileProperties(stack);
		return prop!=null;
	}

	public int getChargeTime(ItemStack railgun)
	{
		return (int)(40/(1+this.getUpgrades(railgun).getFloat("speed")));
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
		ItemStack[] contents = this.getContainedItems(stack);
		//		if(contents[18]!=null&&contents[19]!=null)
		//			player.triggerAchievement(IEAchievements.upgradeRailgun);
	}

	@Override
	public int receiveEnergy(ItemStack container, int energy, boolean simulate)
	{
		return ItemNBTHelper.insertFluxItem(container, energy, getMaxEnergyStored(container), simulate);
	}
	@Override
	public int extractEnergy(ItemStack container, int energy, boolean simulate)
	{
		return ItemNBTHelper.extractFluxFromItem(container, energy, simulate);
	}
	@Override
	public int getEnergyStored(ItemStack container)
	{
		return ItemNBTHelper.getFluxStoredInItem(container);
	}
	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return 8000+this.getUpgrades(container).getInteger("capacity");
	}


	public String[] compileRender(ItemStack stack)
	{
		HashSet<String> render = new HashSet<String>();
		render.add("frame");
		render.add("barrel");
		render.add("grip");
		render.add("capacitors");
		render.add("sled");
		render.add("wires");
		NBTTagCompound upgrades = this.getUpgrades(stack);
		if(upgrades.getDouble("speed")>0)
			render.add("upgrade_speed");
		if(upgrades.getBoolean("scope"))
			render.add("upgrade_scope");
		return render.toArray(new String[render.size()]);
	}

	@Override
	public boolean canZoom(ItemStack stack, EntityPlayer player)
	{
		return this.getUpgrades(stack).getBoolean("scope");
	}
	float[] zoomSteps = new float[]{.1f,.15625f,.2f,.25f, .3125f, .4f, .5f,.625f};
	@Override
	public float[] getZoomSteps(ItemStack stack, EntityPlayer player)
	{
		return zoomSteps;
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack stack, String material)
	{
		return null;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if (group.equals("upgrade_scope"))
			return getUpgrades(stack).getBoolean("scope");
		if (group.equals("upgrade_speed"))
			return getUpgrades(stack).getDouble("speed")>0;
		return true;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public Optional<TRSRTransformation> applyTransformations(ItemStack stack, String group, Optional<TRSRTransformation> transform)
	{
		//		if(transform.isPresent())
		//		{
		//			NBTTagCompound upgrades = this.getUpgrades(stack);
		//			Matrix4 mat = new Matrix4(transform.get().getMatrix());
		////			mat.translate(.41f,2,0);
		//			return Optional.of(new TRSRTransformation(mat.toMatrix4f()));
		//		}
		return transform;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public Matrix4 handlePerspective(ItemStack stack, TransformType cameraTransformType, Matrix4 perspective)
	{
		//		if(stack.)
//		if(ItemNBTHelper.getBoolean(stack, "inUse"))
//		{
//			//ToDo: Accoutn for hands
//			if (cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND)
//				perspective = perspective.translate(-.75, -2, -.5).rotate(Math.toRadians(-78), 0, 0, 1);
//			else
//				perspective = perspective.translate(0, -.5, -.375).rotate(Math.toRadians(8), 0, 1, 0).rotate(Math.toRadians(-12), 1, 0, 0).rotate(Math.toRadians(8), 0, 0, 1);
//		}
		return perspective;
	}
}