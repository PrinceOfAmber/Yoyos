package com.jozufozu.yoyos.tinkers.modifiers;

import com.jozufozu.yoyos.tinkers.TinkersYoyos;
import com.jozufozu.yoyos.tinkers.materials.YoyoNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.IToolMod;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.Tags;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

/**
 * Sticks to blocks
 */
public class ModGluey extends ToolModifier
{
    public ModGluey()
    {
        super("gluey", 0x63BD74);
        
        addAspects(new ModifierAspect.DataAspect(this), new ModifierAspect.SingleAspect(this), ModifierAspect.freeModifier);
    }
    
    @Override
    public boolean canApplyTogether(IToolMod otherModifier)
    {
        return otherModifier != TinkersYoyos.STICKY;
    }
    
    @Override
    protected boolean canApplyCustom(ItemStack stack) throws TinkerGuiException
    {
        if (stack.getItem() != TinkersYoyos.YOYO)
            throw new TinkerGuiException(Util.translateFormatted("gui.error.not_a_yoyo", Util.translate("modifier.gluey.name")));
        return true;
    }
    
    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag)
    {
        YoyoNBT toolData = new YoyoNBT(TagUtil.getTagSafe(rootCompound, Tags.TOOL_DATA));
    
        toolData.attack *= 0.7f;
    
        TagUtil.setToolTag(rootCompound, toolData.get());
    }
}
