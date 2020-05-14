package divinerpg.dimensions.galaxy.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GalaxySkyRender extends IRenderHandler {
    private final VertexFormat vertexBufferFormat;

    private final ResourceLocation texture = new ResourceLocation("textures/environment/end_sky.png");
    private final List<Star> stars;
    private final boolean vboEnabled;
    private final Random random;

    private VertexBuffer starVBO;
    private int starGLCallList;

    public GalaxySkyRender() {
        vboEnabled = OpenGlHelper.useVbo();
        this.vertexBufferFormat = new VertexFormat();
        this.vertexBufferFormat.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));

        List<Color> colors = Arrays.asList(
                Color.RED,
                Color.MAGENTA,
                Color.blue,
                Color.GREEN,
                Color.yellow,
                Color.RED,
                Color.cyan,
                Color.white);

        stars = new ArrayList<>();
        random = new Random();

        for (int i = 0; i < 1500; i++) {
            Color color = colors.get(random.nextInt(colors.size()));
            stars.add(new Star(color));
        }
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        try {
//            renderSky(mc.renderEngine, texture);
//            renderStars();


        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void renderSky(TextureManager renderEngine, ResourceLocation texture) {
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.depthMask(false);
        renderEngine.bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        for (int k1 = 0; k1 < 6; ++k1) {
            GlStateManager.pushMatrix();

            if (k1 == 1) {
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (k1 == 2) {
                GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (k1 == 3) {
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            }

            if (k1 == 4) {
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            }

            if (k1 == 5) {
                GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            }

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            int red = 180;
            int green = 255;
            int blue = 255;

            double size = 100;

            bufferbuilder.pos(-size, -size, -size).tex(0.0D, 0.0D).color(red, green, blue, 255).endVertex();
            bufferbuilder.pos(-size, -size, size).tex(0.0D, 16.0D).color(red, green, blue, 255).endVertex();
            bufferbuilder.pos(size, -size, size).tex(16.0D, 16.0D).color(red, green, blue, 255).endVertex();
            bufferbuilder.pos(size, -size, -size).tex(16.0D, 0.0D).color(red, green, blue, 255).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();

        if (this.vboEnabled) {
            this.starVBO = new VertexBuffer(this.vertexBufferFormat);
            this.starVBO.bindBuffer();
            GlStateManager.glEnableClientState(32884);
            GlStateManager.glVertexPointer(3, 5126, 12, 0);
            this.starVBO.drawArrays(7);
            this.starVBO.unbindBuffer();
            GlStateManager.glDisableClientState(32884);
        } else {
            GlStateManager.callList(this.starGLCallList);
        }
    }

    private void renderStars() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        if (this.starVBO != null) {
            this.starVBO.deleteGlBuffers();
        }

        if (this.starGLCallList >= 0) {
            GLAllocation.deleteDisplayLists(this.starGLCallList);
            this.starGLCallList = -1;
        }

        if (this.vboEnabled) {
            this.starVBO = new VertexBuffer(this.vertexBufferFormat);
            this.renderStars(bufferbuilder);
            bufferbuilder.finishDrawing();
            bufferbuilder.reset();
            this.starVBO.bufferData(bufferbuilder.getByteBuffer());
        } else {
            this.starGLCallList = GLAllocation.generateDisplayLists(1);
            GlStateManager.pushMatrix();
            GlStateManager.glNewList(this.starGLCallList, 4864);
            this.renderStars(bufferbuilder);
            tessellator.draw();
            GlStateManager.glEndList();
            GlStateManager.popMatrix();
        }
    }

    private void renderStars(BufferBuilder builder) {
        stars.forEach(x -> x.renderInner(builder, random));
    }
}
