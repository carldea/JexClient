package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventGetGlintShaders;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.load.impl.IShader;
import net.minecraft.client.gl.GlUniform;

import java.awt.*;

@Feature.Manifest(name = "EnchantColor", category = Feature.Category.VISUAL, description = "Change the color of the enchanment glint (or make it rainbow!)")
public class EnchantColor extends Feature{

    @Op(name = "Mode", all = {"Shader Rainbow", "Customize"})
    public String mode = "Shader Rainbow";
    @OpChild(name = "Shader Mode", all = {"Rainbow", "TV", "Test"}, parent = "Mode", dependency = "Shader Rainbow")
    public String shaderMode = "Rainbow";
    @OpChild(name = "Saturation", min = 0.1f, inc = 0.05f, parent = "Mode", dependency = "Shader Rainbow")
    public float saturation = 0.75f;
    @OpChild(name = "Alpha", min = 0.1f, inc = 0.05f, parent = "Mode", dependency = "Shader Rainbow")
    public float alpha = 1f;
    @OpChild(name = "Color", isColor = true, parent = "Mode", dependency = "Customize")
    public int color = new Color(0, 255, 0).getRGB();
    @OpChild(name = "Rainbow", parent = "Color")
    public boolean rainbow = false;
    @OpChild(name = "Speed", min = 1, max = 10, parent = "Rainbow")
    public int rainbowSpeed = 1;

    private int col;
    private Timer timer = new Timer();
    private GlUniform glintColorU;
    private GlUniform crazyRainbowU;
    private GlUniform saturationU;
    private GlUniform alphaU;
    private GlUniform mathModeU;

    @EventListener(events = {EventGetGlintShaders.class})
    private void runMethod(EventGetGlintShaders eventGetGlintShaders) {
        if (glintColorU == null || crazyRainbowU == null || saturationU == null || mathModeU == null) {
            IShader iShader = (IShader) ShaderHelper.getRainbowEnchantShader();
            glintColorU = iShader.getCustomUniform("GlintColor");
            crazyRainbowU = iShader.getCustomUniform("CrazyRainbow");
            saturationU = iShader.getCustomUniform("Saturation");
            alphaU = iShader.getCustomUniform("Alpha");
            mathModeU = iShader.getCustomUniform("MathMode");
        }
        if (glintColorU != null) {
            Color setColor = rainbow ? ColorHelper.INSTANCE.getColorViaHue(col) : ColorHelper.INSTANCE.getColor(color);
            glintColorU.set(setColor.getRed() / 255.f, setColor.getGreen() / 255.f, setColor.getBlue() / 255.f, 1);
        }
        if (crazyRainbowU != null) {
            crazyRainbowU.set("Shader Rainbow".equalsIgnoreCase(mode) ? 1 : 0);
        }
        if (saturationU != null) {
            saturationU.set(saturation);
        }
        if (alphaU != null) {
            alphaU.set(alpha);
        }
        if (mathModeU != null) {
            mathModeU.set(getShaderMode());
        }
        eventGetGlintShaders.setShader(ShaderHelper.getRainbowEnchantShader());
        eventGetGlintShaders.cancel();

        if (timer.hasPassed(25)) {
            col+=rainbowSpeed;
            if (col > 270)
                col-=270;
            timer.reset();
        }
    }

    public int getShaderMode() {
        switch (shaderMode.toLowerCase()) {
            case "add" -> {return 0;}
            case "tv" -> {return 1;}
            case "test" -> {return 2;}
        }
        return 0;
    }
}
