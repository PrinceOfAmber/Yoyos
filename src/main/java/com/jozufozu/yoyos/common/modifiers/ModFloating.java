package com.jozufozu.yoyos.common.modifiers;

import com.jozufozu.yoyos.TinkersYoyos;
import com.jozufozu.yoyos.common.materials.YoyoNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.Tags;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

/**
 * Makes yoyos lighter
 * Pufferfish or something
 */
public class ModFloating extends ToolModifier {

    public ModFloating(int max) {
        super("floating", 0x00FFD9);

        addAspects(new ModifierAspect.LevelAspect(this, max), new ModifierAspect.DataAspect(this), ModifierAspect.freeModifier);
    }

    @Override
    protected boolean canApplyCustom(ItemStack stack) throws TinkerGuiException {
        if (stack.getItem() != TinkersYoyos.YOYO)
            throw new TinkerGuiException(Util.translateFormatted("gui.error.not_a_yoyo", Util.translate("modifier.floating.name")));

        YoyoNBT toolData = new YoyoNBT(TagUtil.getTagSafe(stack.getTagCompound(), Tags.TOOL_DATA));
        if (toolData.weight <= 0.1) {
            throw new TinkerGuiException(Util.translateFormatted("gui.error.too_light"));
        }
        return true;
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
        ModifierNBT.IntegerNBT data = ModifierNBT.readInteger(modifierTag);

        YoyoNBT toolData = new YoyoNBT(TagUtil.getTagSafe(rootCompound, Tags.TOOL_DATA));

        for (int i = data.level; i > 0 ; i--) {
            toolData.weight -= 0.5F / i;
        }

        toolData.weight = Math.max(toolData.weight, 0.01F);

        TagUtil.setToolTag(rootCompound, toolData.get());
    }

    @Override
    public String getTooltip(NBTTagCompound modifierTag, boolean detailed) {
        return getLeveledTooltip(modifierTag, detailed);
    }
}
