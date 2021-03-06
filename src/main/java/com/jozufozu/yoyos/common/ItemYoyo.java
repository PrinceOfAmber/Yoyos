package com.jozufozu.yoyos.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.network.MessageRetractYoYo;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ItemYoyo extends ItemSword implements IYoyo
{
    protected final ToolMaterial material;
    protected final boolean gardening;
    
    public ItemYoyo(String name, ToolMaterial material)
    {
        this(name, material, false);
    }
    
    public ItemYoyo(String name, ToolMaterial material, boolean gardening)
    {
        super(material);
        this.material = material;
        this.gardening = gardening;
        
        this.setUnlocalizedName(String.format("%s.%s", Yoyos.MODID, name));
        this.setRegistryName(Yoyos.MODID, name);
        
        if (!gardening) this.setCreativeTab(CreativeTabs.COMBAT);
    }
    
    @Override
    public Set<String> getToolClasses(ItemStack stack)
    {
        return ImmutableSet.of("yoyo");
    }
    
    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return super.hasEffect(stack) || this == Yoyos.CREATIVE_YOYO;
    }
    
    public int getItemEnchantability()
    {
        return this.material.getEnchantability();
    }
    
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
    {
        ItemStack mat = this.material.getRepairItemStack();
        return OreDictionary.itemMatches(mat, repair, false) || super.getIsRepairable(toRepair, repair);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (!worldIn.isRemote)
        {
            EntityYoyo entityYoyo = EntityYoyo.CASTERS.get(playerIn);
            
            if (entityYoyo != null && entityYoyo.isEntityAlive())
            {
                entityYoyo.setRetracting(!entityYoyo.isRetracting());
                YoyoNetwork.INSTANCE.sendToAll(new MessageRetractYoYo(entityYoyo));
                playerIn.swingArm(hand);
            }
            else if (itemStack.getItemDamage() <= itemStack.getMaxDamage() || this == Yoyos.CREATIVE_YOYO)
            {
                worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                
                worldIn.spawnEntity(new EntityYoyo(worldIn, playerIn));
                
                playerIn.swingArm(hand);
            }
        }
        
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("info.weight.name", getWeight(stack)));
        tooltip.add(I18n.format("info.length.name", getLength(stack)));
    
        int duration = getDuration(stack);
        Object arg = (duration < 0 ? I18n.format("stat.yoyo.infinite.name") : ((float) duration) / 20F);
        tooltip.add(I18n.format("info.duration.name", arg));
        
        if (stack.isItemEnchanted())
            tooltip.add("");
    }
    
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack)
    {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
    
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            double damage = 0.0;
    
            if (this == Yoyos.SHEAR_YOYO) damage = ModConfig.vanillaYoyos.shearYoyo.damage;
            if (this == Yoyos.STICKY_YOYO) damage =  ModConfig.vanillaYoyos.stickyYoyo.damage;
            if (this == Yoyos.DIAMOND_YOYO) damage =  ModConfig.vanillaYoyos.diamondYoyo.damage;
            if (this == Yoyos.GOLD_YOYO) damage =  ModConfig.vanillaYoyos.goldYoyo.damage;
            if (this == Yoyos.IRON_YOYO) damage =  ModConfig.vanillaYoyos.ironYoyo.damage;
            if (this == Yoyos.STONE_YOYO) damage =  ModConfig.vanillaYoyos.stoneYoyo.damage;
            if (this == Yoyos.WOODEN_YOYO) damage =  ModConfig.vanillaYoyos.woodenYoyo.damage;
            if (this == Yoyos.CREATIVE_YOYO) damage =  ModConfig.vanillaYoyos.creativeYoyo.damage;
            
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }
    
        return multimap;
    }
    
    @Override
    public float getWeight(ItemStack yoyo)
    {
        if (this == Yoyos.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.weight;
        if (this == Yoyos.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.weight;
        if (this == Yoyos.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.weight;
        if (this == Yoyos.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.weight;
        if (this == Yoyos.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.weight;
        if (this == Yoyos.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.weight;
        if (this == Yoyos.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.weight;
        if (this == Yoyos.CREATIVE_YOYO) return ModConfig.vanillaYoyos.creativeYoyo.weight;
        
        return 1.0f;
    }
    
    @Override
    public float getLength(ItemStack yoyo)
    {
        if (this == Yoyos.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.length;
        if (this == Yoyos.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.length;
        if (this == Yoyos.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.length;
        if (this == Yoyos.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.length;
        if (this == Yoyos.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.length;
        if (this == Yoyos.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.length;
        if (this == Yoyos.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.length;
        if (this == Yoyos.CREATIVE_YOYO) return ModConfig.vanillaYoyos.creativeYoyo.length;
    
        return 1.0f;
    }
    
    @Override
    public int getDuration(ItemStack yoyo)
    {
        if (this == Yoyos.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.duration;
        if (this == Yoyos.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.duration;
        if (this == Yoyos.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.duration;
        if (this == Yoyos.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.duration;
        if (this == Yoyos.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.duration;
        if (this == Yoyos.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.duration;
        if (this == Yoyos.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.duration;
        if (this == Yoyos.CREATIVE_YOYO) return ModConfig.vanillaYoyos.creativeYoyo.duration;
    
        return 10;
    }
    
    @Override
    public int getAttackSpeed(ItemStack yoyo)
    {
        return 10;
    }
    
    @Override
    public boolean gardening(ItemStack yoyo)
    {
        return gardening;
    }
    
    @Override
    public boolean collecting(ItemStack yoyo)
    {
        return this == Yoyos.CREATIVE_YOYO || EnchantmentHelper.getEnchantmentLevel(Yoyos.COLLECTING, yoyo) > 0;
    }
    
    @Override
    public void damageItem(ItemStack yoyo, EntityLivingBase player)
    {
        yoyo.damageItem(1, player);
    }
    
    @Override
    public void attack(Entity targetEntity, ItemStack stack, EntityPlayer attacker, EntityYoyo yoyoEntity)
    {
        if (!ForgeHooks.onPlayerAttackTarget(attacker, targetEntity))
            return;
        
        if (targetEntity.canBeAttackedWithItem())
        {
            if (!targetEntity.hitByEntity(attacker))
            {
                float damage = (float) attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float attackModifier;
                
                if (targetEntity instanceof EntityLivingBase)
                {
                    attackModifier = EnchantmentHelper.getModifierForCreature(attacker.getHeldItemMainhand(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
                }
                else
                {
                    attackModifier = EnchantmentHelper.getModifierForCreature(attacker.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
                }
                
                float attackStrength = attacker.getCooledAttackStrength(0.5F);
                damage = damage * (0.2F + attackStrength * attackStrength * 0.8F);
                attackModifier = attackModifier * attackStrength;
                attacker.resetCooldown();
                
                if (damage > 0.0F || attackModifier > 0.0F)
                {
                    boolean flag = attackStrength > 0.9F;
                    int knockbackModifier = 0;
                    knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackModifier(attacker);
                    
                    boolean critical = flag && attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isOnLadder() && !attacker.isInWater() && !attacker.isPotionActive(MobEffects.BLINDNESS) && !attacker.isRiding() && targetEntity instanceof EntityLivingBase;
                    critical = critical && !attacker.isSprinting();
                    
                    if (critical)
                    {
                        damage *= 1.5F;
                    }
                    
                    damage = damage + attackModifier;
                    
                    float targetHealth = 0.0F;
                    boolean setEntityOnFire = false;
                    int fireAspect = EnchantmentHelper.getFireAspectModifier(attacker);
                    
                    if (targetEntity instanceof EntityLivingBase)
                    {
                        targetHealth = ((EntityLivingBase) targetEntity).getHealth();
                        
                        if (fireAspect > 0 && !targetEntity.isBurning())
                        {
                            setEntityOnFire = true;
                            targetEntity.setFire(1);
                        }
                    }
                    
                    double motionX = targetEntity.motionX;
                    double motionY = targetEntity.motionY;
                    double motionZ = targetEntity.motionZ;
                    boolean didDamage = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(attacker), damage);
                    
                    if (didDamage)
                    {
                        if (knockbackModifier > 0)
                        {
                            if (targetEntity instanceof EntityLivingBase)
                            {
                                ((EntityLivingBase) targetEntity).knockBack(attacker, (float) knockbackModifier * 0.5F, (double) MathHelper.sin(attacker.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(attacker.rotationYaw * 0.017453292F)));
                            }
                            else
                            {
                                targetEntity.addVelocity((double) (-MathHelper.sin(attacker.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F), 0.1D, (double) (MathHelper.cos(attacker.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F));
                            }
                        }
                        
                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged)
                        {
                            ((EntityPlayerMP) targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = motionX;
                            targetEntity.motionY = motionY;
                            targetEntity.motionZ = motionZ;
                        }
                        
                        if (critical)
                        {
                            attacker.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, attacker.getSoundCategory(), 1.0F, 1.0F);
                            attacker.onCriticalHit(targetEntity);
                        }
                        
                        if (!critical)
                        {
                            if (flag)
                            {
                                attacker.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, attacker.getSoundCategory(), 1.0F, 1.0F);
                            }
                            else
                            {
                                attacker.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, attacker.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }
                        
                        if (attackModifier > 0.0F)
                        {
                            attacker.onEnchantmentCritical(targetEntity);
                        }
                        
                        attacker.setLastAttackedEntity(targetEntity);
                        
                        if (targetEntity instanceof EntityLivingBase)
                        {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, attacker);
                        }
                        
                        EnchantmentHelper.applyArthropodEnchantments(attacker, targetEntity);
                        ItemStack itemstack1 = attacker.getHeldItemMainhand();
                        Entity entity = targetEntity;
                        
                        if (targetEntity instanceof IEntityMultiPart)
                        {
                            IEntityMultiPart ientitymultipart = (IEntityMultiPart) targetEntity;
                            
                            if (ientitymultipart instanceof EntityLivingBase)
                            {
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }
                        
                        if (itemstack1 != ItemStack.EMPTY && entity instanceof EntityLivingBase)
                        {
                            itemstack1.hitEntity((EntityLivingBase) entity, attacker);
                            
                            if (itemstack1.getCount() <= 0)
                            {
                                attacker.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                                ForgeEventFactory.onPlayerDestroyItem(attacker, itemstack1, EnumHand.MAIN_HAND);
                            }
                        }
                        
                        if (targetEntity instanceof EntityLivingBase)
                        {
                            float f5 = targetHealth - ((EntityLivingBase) targetEntity).getHealth();
                            attacker.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                            
                            if (fireAspect > 0)
                            {
                                targetEntity.setFire(fireAspect * 4);
                            }
                            
                            if (attacker.world instanceof WorldServer && f5 > 2.0F)
                            {
                                int k = (int) ((double) f5 * 0.5D);
                                ((WorldServer) attacker.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double) (targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }
                        
                        attacker.addExhaustion(0.3F);
                    }
                    else
                    {
                        attacker.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, attacker.getSoundCategory(), 1.0F, 1.0F);
                        
                        if (setEntityOnFire)
                        {
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
    }
}
