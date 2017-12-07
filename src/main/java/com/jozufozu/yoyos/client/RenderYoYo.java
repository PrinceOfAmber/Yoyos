package com.jozufozu.yoyos.client;

import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.IYoyo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class RenderYoYo extends Render<EntityYoyo>
{
    private static EntityLivingBase RENDER_SLAVE;
    
    public RenderYoYo(RenderManager renderManager)
    {
        super(renderManager);
    }
    
    @SubscribeEvent
    public void onRenderSpecificHand(RenderSpecificHandEvent event)
    {
    }
    
    @Override
    public void doRender(@Nonnull EntityYoyo entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (RENDER_SLAVE == null) RENDER_SLAVE = new EntityCreeper(entity.world);
        
        Profiler mcProfiler = Minecraft.getMinecraft().mcProfiler;
        mcProfiler.startSection("renderYoyo");
        
        float ageInTicks = entity.ticksExisted + partialTicks;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2, z);
        GlStateManager.scale(.5, .5, .5);
        
        Vec3d pointer = entity.getPlayerHandPos(partialTicks).subtract(entity.posX, entity.posY + entity.height / 2, entity.posZ).normalize();
        
        float yaw = (float) (Math.atan2(pointer.x, pointer.z) * -180 / Math.PI);
        float multiplier = 35;
        
        if (entity.getDuration() != -1)
        {
            multiplier *= 2 - ageInTicks / ((float) entity.getDuration());
        }
        
        float pitch = ageInTicks * multiplier;
        
        //face away from player
        GlStateManager.rotate(90 - yaw, 0, 1, 0);
        //spin around
        GlStateManager.rotate(180 - pitch, 0, 0, 1);
        
        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }
        
        Minecraft.getMinecraft().getItemRenderer().renderItem(RENDER_SLAVE, entity.getYoyoStack(), ItemCameraTransforms.TransformType.NONE);
        
        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }
        
        GlStateManager.popMatrix();
        
        renderChord(entity, x, y, z, partialTicks);
        
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        
        mcProfiler.endSection();
    }
    
    public static void renderChord(@Nonnull EntityYoyo entity, double x, double y, double z, float partialTicks)
    {
        Entity thrower = entity.getThrower();
        
        if (thrower != null && thrower instanceof EntityPlayer)
        {
            y = y - (1.6D - (double) thrower.height) * 0.5D;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            
            Vec3d handPos = entity.getPlayerHandPos(partialTicks);
            double yoyoPosX = interpolateValue(entity.prevPosX, entity.posX, (double) partialTicks);
            double yoyoPosY = interpolateValue(entity.prevPosY, entity.posY, (double) partialTicks) - entity.height;
            double yoyoPosZ = interpolateValue(entity.prevPosZ, entity.posZ, (double) partialTicks);
            
            double xDiff = (double) ((float) (handPos.x - yoyoPosX));
            double yDiff = (double) ((float) (handPos.y - yoyoPosY));
            double zDiff = (double) ((float) (handPos.z - yoyoPosZ));
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            
            bufferBuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
            
            int color = 0xDDDDDD;
            if (entity.getYoyoStack().getItem() instanceof IYoyo)
            {
                color = ((IYoyo) entity.getYoyoStack().getItem()).getCordColor(entity.getYoyoStack());
            }
            
            float stringR = ((color >> 16) & 255) / 255F;
            float stringG = ((color >> 8) & 255) / 255F;
            float stringB = (color & 255) / 255F;
            
            for (int j = 0; j <= 24; ++j)
            {
                float r = stringR;
                float g = stringG;
                float b = stringB;
                
                if (j % 2 == 0)
                {
                    r *= 0.7F;
                    g *= 0.7F;
                    b *= 0.7F;
                }
                
                float segment = (float) j / 24.0F;
                bufferBuilder.pos(x + xDiff * (double) segment + 0.0D, y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment).color(r, g, b, 1.0F).endVertex();
                bufferBuilder.pos(x + xDiff * (double) segment + 0.025D, y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment).color(r, g, b, 1.0F).endVertex();
            }
            
            tessellator.draw();
            bufferBuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
            
            for (int k = 0; k <= 24; ++k)
            {
                float r = stringR;
                float g = stringG;
                float b = stringB;
                
                if (k % 2 == 0)
                {
                    r *= 0.7F;
                    g *= 0.7F;
                    b *= 0.7F;
                }
                
                float segment = (float) k / 24.0F;
                bufferBuilder.pos(x + xDiff * (double) segment + 0.0D, y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment).color(r, g, b, 1.0F).endVertex();
                bufferBuilder.pos(x + xDiff * (double) segment + 0.025D, y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment + 0.025D).color(r, g, b, 1.0F).endVertex();
            }
            
            tessellator.draw();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableCull();
        }
    }
    
    private static double interpolateValue(double start, double end, double pct)
    {
        return start + (end - start) * pct;
    }
    
    @Override
    protected ResourceLocation getEntityTexture(EntityYoyo entity)
    {
        return null;
    }
}
