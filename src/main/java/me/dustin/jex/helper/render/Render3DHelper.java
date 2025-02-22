package me.dustin.jex.helper.render;

import java.awt.Color;
import java.util.ArrayList;

import com.mojang.blaze3d.systems.RenderSystem;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.TestRender;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public enum Render3DHelper {
    INSTANCE;

    public Vec3d getEntityRenderPosition(Entity entity, double partial, MatrixStack matrixStack) {
        Matrix4f matrix = matrixStack.peek().getModel();
        double x = entity.prevX + ((entity.getX() - entity.prevX) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double y = entity.prevY + ((entity.getY() - entity.prevY) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        Vector4f vector4f = new Vector4f((float)x, (float)y, (float)z, 1.f);
        vector4f.transform(matrix);
        return new Vec3d(vector4f.getX(), vector4f.getY(), vector4f.getZ());
    }

    public Vec3d getRenderPosition(double x, double y, double z, MatrixStack matrixStack) {
        Matrix4f matrix = matrixStack.peek().getModel();
        double minX = x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double minY = y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        Vector4f vector4f = new Vector4f((float)minX, (float)minY, (float)minZ, 1.f);
        vector4f.transform(matrix);
        return new Vec3d(vector4f.getX(), vector4f.getY(), vector4f.getZ());
    }

    public Vec3d getRenderPosition(Vec3d vec3d, MatrixStack matrixStack) {
        Matrix4f matrix = matrixStack.peek().getModel();
        double minX = vec3d.getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double minY = vec3d.getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = vec3d.getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        Vector4f vector4f = new Vector4f((float)minX, (float)minY, (float)minZ, 1.f);
        vector4f.transform(matrix);
        return new Vec3d(vector4f.getX(), vector4f.getY(), vector4f.getZ());
    }

    public Vec3d getRenderPosition(BlockPos blockPos, MatrixStack matrixStack) {
        Matrix4f matrix = matrixStack.peek().getModel();
        double minX = blockPos.getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double minY = blockPos.getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = blockPos.getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        Vector4f vector4f = new Vector4f((float)minX, (float)minY, (float)minZ, 1.f);
        vector4f.transform(matrix);
        return new Vec3d(vector4f.getX(), vector4f.getY(), vector4f.getZ());
    }

    public Vec3d getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.prevX + ((entity.getX() - entity.prevX) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double y = entity.prevY + ((entity.getY() - entity.prevY) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(x, y, z);
    }

    public Vec3d getRenderPosition(double x, double y, double z) {
        double minX = x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double minY = y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public Vec3d getRenderPosition(Vec3d vec3d) {
        double minX = vec3d.getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double minY = vec3d.getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = vec3d.getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public Vec3d getRenderPosition(BlockPos blockPos) {
        double minX = blockPos.getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double minY = blockPos.getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = blockPos.getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public void fixCameraRots(MatrixStack matrixStack) {
        Camera camera = Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera;
        matrixStack.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
        matrixStack.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(camera.getPitch()));
    }

    public void applyCameraRots(MatrixStack matrixStack) {
        Camera camera = Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera;
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
    }

    public void setup3DRender(boolean disableDepth) {
        if (useNewRendering()) {
            ShaderHelper.INSTANCE.getPosColorShader().bind();
        } else {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (disableDepth)
            RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.enableCull();
    }

    public void end3DRender() {
        if (useNewRendering())
            ShaderHelper.INSTANCE.getPosColorShader().detach();
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    public void setup3DProj(float partialTicks) {
        double d = Wrapper.INSTANCE.getIGameRenderer().getFOV(partialTicks);
        Matrix4f projection = Wrapper.INSTANCE.getGameRenderer().getBasicProjectionMatrix(d);
        ShaderHelper.INSTANCE.setProjectionMatrix(Matrix4x4.copyFromColumnMajor(projection));
        ShaderHelper.INSTANCE.setModelViewMatrix(Matrix4x4.copyFromColumnMajor(RenderSystem.getModelViewMatrix()));
    }

    public void drawSphere(MatrixStack matrixStack, float radius, int gradation, int color, boolean testDepth, Vec3d pos) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        Color color1 = ColorHelper.INSTANCE.getColor(color);
        final float PI = 3.141592f;
        float x, y, z, alpha, beta;
        if (!testDepth)
            RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        for (alpha = 0.0f; alpha < Math.PI; alpha += PI / gradation) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (beta = 0.0f; beta < 2.01f * Math.PI; beta += PI / gradation) {
                x = (float) (pos.getX() +  (radius * Math.cos(beta) * Math.sin(alpha)));
                y = (float) (pos.getY() +  (radius * Math.sin(beta) * Math.sin(alpha)));
                z = (float) (pos.getZ() +  (radius * Math.cos(alpha)));
                Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                x = (float) (pos.getX() +  (radius * Math.cos(beta) * Math.sin(alpha + PI / gradation)));
                y = (float) (pos.getY() +  (radius * Math.sin(beta) * Math.sin(alpha + PI / gradation)));
                z = (float) (pos.getZ() +  (radius * Math.cos(alpha + PI / gradation)));
                renderPos = Render3DHelper.INSTANCE.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
            }
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
    }

    public void drawBoxWithDepthTest(MatrixStack matrixstack, Box bb, int color) {
        setup3DRender(false);

        drawFilledBox(matrixstack, bb, color & 0x70ffffff);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixstack, bb, color);

        end3DRender();
    }

    public void drawBox(MatrixStack matrixstack, Box bb, int color) {
        setup3DRender(true);

        if (useNewRendering())
            newFilledBox(matrixstack, bb, color & 0x70ffffff);
        else
            drawFilledBox(matrixstack, bb, color & 0x70ffffff);
        RenderSystem.lineWidth(1);
        if (useNewRendering())
            newOutlineBox(matrixstack, bb, color);
        else
            drawOutlineBox(matrixstack, bb, color);

        end3DRender();
    }

    public void drawBoxOutline(MatrixStack matrixstack, Box bb, int color) {
        setup3DRender(true);

        RenderSystem.lineWidth(1);
        if (useNewRendering())
            newOutlineBox(matrixstack, bb, color);
        else
            drawOutlineBox(matrixstack, bb, color);

        end3DRender();
    }

    public void drawBoxInside(MatrixStack matrixstack, Box bb, int color) {
        setup3DRender(true);

        if (useNewRendering())
            newFilledBox(matrixstack, bb, color & 0x70ffffff);
        else
            drawFilledBox(matrixstack, bb, color & 0x70ffffff);

        end3DRender();
    }

    public void drawEntityBox(MatrixStack matrixstack, Entity entity, float partialTicks, int color) {
        Vec3d renderPos = getEntityRenderPosition(entity, partialTicks);
        drawEntityBox(matrixstack, entity, renderPos.x, renderPos.y, renderPos.z, color);
    }

    public void drawEntityBox(MatrixStack matrixstack, Entity entity, double x, double y, double z, int color) {
        float yaw = EntityHelper.INSTANCE.getYaw(entity);
        setup3DRender(true);
        matrixstack.translate(x, y, z);
        matrixstack.multiply(new Quaternion(new Vec3f(0, -1, 0), yaw, true));
        matrixstack.translate(-x, -y, -z);

        Box bb = new Box(x - entity.getWidth() + 0.25, y, z - entity.getWidth() + 0.25, x + entity.getWidth() - 0.25, y + entity.getHeight() + 0.1, z + entity.getWidth() - 0.25);
        if (entity instanceof ItemEntity)
            bb = new Box(x - 0.15, y + 0.1f, z - 0.15, x + 0.15, y + 0.5, z + 0.15);


        if (useNewRendering())
            newFilledBox(matrixstack, bb, color & 0x60ffffff);
        else
            drawFilledBox(matrixstack, bb, color & 0x60ffffff);
        RenderSystem.lineWidth(1.5f);

        if (useNewRendering())
            newOutlineBox(matrixstack, bb, color);
        else
            drawOutlineBox(matrixstack, bb, color);

        end3DRender();
        matrixstack.translate(x, y, z);
        matrixstack.multiply(new Quaternion(new Vec3f(0, 1, 0), yaw, true));
        matrixstack.translate(-x, -y, -z);
    }

    public double interpolate(final double now, final double then, final double percent) {
        return (then + (now - then) * percent);
    }

    public void drawList(MatrixStack matrixStack, ArrayList<BoxStorage> list) {
		setup3DRender(true);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
    	list.forEach(blockStorage -> {
            Box box = blockStorage.box();
            int color = blockStorage.color();
            drawOutlineBox(matrixStack, box, color, false);
    	});
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        list.forEach(blockStorage -> {
            Box box = blockStorage.box();
            int color = blockStorage.color();
            drawFilledBox(matrixStack, box, color & 0x70ffffff, false);
    	});
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        end3DRender();
    }
    
    public void drawFilledBox(MatrixStack matrixStack, Box bb, int color) {
    	drawFilledBox(matrixStack, bb, color, true);
    }
    
    public void drawFilledBox(MatrixStack matrixStack, Box bb, int color, boolean draw) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        Color color1 = ColorHelper.INSTANCE.getColor(color);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        if (draw)
        	bufferBuilder.begin(VertexFormat.DrawMode.QUADS/*QUADS*/, VertexFormats.POSITION_COLOR);
        float minX = (float)bb.minX;
        float minY = (float)bb.minY;
        float minZ = (float)bb.minZ;
        float maxX = (float)bb.maxX;
        float maxY = (float)bb.maxY;
        float maxZ = (float)bb.maxZ;

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        if (draw) {
	        bufferBuilder.end();
	        BufferRenderer.draw(bufferBuilder);
        }
    }

    public void drawFadeBox(MatrixStack matrixStack, Box bb, int color) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        Color color1 = ColorHelper.INSTANCE.getColor(color);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS/*QUADS*/, VertexFormats.POSITION_COLOR);
        float minX = (float)bb.minX;
        float minY = (float)bb.minY;
        float minZ = (float)bb.minZ;
        float maxX = (float)bb.maxX;
        float maxY = (float)bb.maxY;
        float maxZ = (float)bb.maxZ;

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    public void doFadeBoxNoDraw(MatrixStack matrixStack, Box bb, int color) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        Color color1 = ColorHelper.INSTANCE.getColor(color);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        float minX = (float)bb.minX;
        float minY = (float)bb.minY;
        float minZ = (float)bb.minZ;
        float maxX = (float)bb.maxX;
        float maxY = (float)bb.maxY;
        float maxZ = (float)bb.maxZ;

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).next();
    }

    public void drawOutlineBox(MatrixStack matrixStack, Box bb, int color) {
    	drawOutlineBox(matrixStack, bb, color, true);
    }

    public void drawOutlineBox(MatrixStack matrixStack, Box bb, int color, boolean draw) {
        Color color1 = ColorHelper.INSTANCE.getColor(color);
        Matrix4f matrix4f = matrixStack.peek().getModel();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        if (draw)
        	bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES/*LINES*/, VertexFormats.POSITION_COLOR);

        VoxelShape shape = VoxelShapes.cuboid(bb);
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
            bufferBuilder.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        });
        if (draw) {
	        bufferBuilder.end();
	        BufferRenderer.draw(bufferBuilder);
        }
    }

    public boolean useNewRendering() {
        return Feature.get(TestRender.class) != null && Feature.get(TestRender.class).getState();
    }

    private void newFilledBox(MatrixStack matrixStack, Box bb, int color) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        Color color1 = ColorHelper.INSTANCE.getColor(color);

        float minX = (float)bb.minX;
        float minY = (float)bb.minY;
        float minZ = (float)bb.minZ;
        float maxX = (float)bb.maxX;
        float maxY = (float)bb.maxY;
        float maxZ = (float)bb.maxZ;

        VertexObjectList vertexObjectList = VertexObjectList.getMain();
        vertexObjectList.begin(VertexObjectList.DrawMode.QUAD, VertexObjectList.Format.POS_COLOR);
        /*0*/vertexObjectList.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        /*1*/vertexObjectList.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        /*2*/vertexObjectList.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        /*3*/vertexObjectList.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        /*4*/vertexObjectList.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        /*5*/vertexObjectList.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        /*6*/vertexObjectList.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        /*7*/vertexObjectList.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        vertexObjectList.index(0, 1, 2).index(2, 3, 0);//bottom
        vertexObjectList.index(0, 3, 7).index(7, 4, 0);//east face
        vertexObjectList.index(0, 4, 5).index(5, 1, 0);//south face
        vertexObjectList.index(2, 1, 5).index(5, 6, 2);//west face
        vertexObjectList.index(2, 6, 7).index(7, 3, 2);//north face
        vertexObjectList.index(4, 7, 6).index(6, 5, 4);//top
        vertexObjectList.end();
        vertexObjectList.draw();
    }

    private void newOutlineBox(MatrixStack matrixStack, Box bb, int color) {
        Color color1 = ColorHelper.INSTANCE.getColor(color);
        Matrix4f matrix4f = matrixStack.peek().getModel();

        VertexObjectList vertexObjectList = VertexObjectList.getMain();
        vertexObjectList.begin(VertexObjectList.DrawMode.LINE, VertexObjectList.Format.POS_COLOR);
        VoxelShape shape = VoxelShapes.cuboid(bb);
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            vertexObjectList.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
            vertexObjectList.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        });
        vertexObjectList.end();
        vertexObjectList.draw();
    }
    
    public record BoxStorage (Box box, int color) {}
}
