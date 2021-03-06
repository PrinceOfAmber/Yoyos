package com.jozufozu.yoyos.tinkers.modifiers;

import com.jozufozu.yoyos.tinkers.TinkersYoyos;
import com.jozufozu.yoyos.tinkers.materials.YoyoNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.Tags;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

/**
 * Allows for yoyos to cut down grass and flowers and such
 */
public class ModGardening extends ToolModifier
{
    public ModGardening()
    {
        super("gardening", 0x8b4336);
        
        addAspects(new ModifierAspect.DataAspect(this), new ModifierAspect.SingleAspect(this), ModifierAspect.freeModifier);
    }
    
    @Override
    protected boolean canApplyCustom(ItemStack stack) throws TinkerGuiException
    {
        if (stack.getItem() != TinkersYoyos.YOYO)
        {
            throw new TinkerGuiException(Util.translateFormatted("gui.error.not_a_yoyo", Util.translate("modifier.gardening.name")));
        }
        
        return true;
    }
    
    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag)
    {
        YoyoNBT toolData = new YoyoNBT(TagUtil.getTagSafe(rootCompound, Tags.TOOL_DATA));
        toolData.attack += 0.5;
    
        TagUtil.setToolTag(rootCompound, toolData.get());
    }
}
