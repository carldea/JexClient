package me.dustin.jex.gui.click.window.impl;


import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.helper.file.files.FeatureFile;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.option.Option;
import me.dustin.jex.feature.option.enums.OpType;
import me.dustin.jex.feature.option.types.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

public class OptionButton extends Button {

    Timer timer = new Timer();
    int togglePos = 0;
    int cogSpin = 0;
    private Option option;
    private boolean isSliding;
    private OptionButton masterButton;
    private OptionButton parentButton;
    private Identifier colorSlider = new Identifier("jex", "gui/click/colorslider.png");
    private int buttonsHeight;

    public OptionButton(Window window, Option option, float x, float y, float width, float height) {
        super(window, option.getName(), x, y, width, height, null);
        this.option = option;
    }

    @Override
    public void draw(MatrixStack matrixStack) {
        updateOnOff();
        //Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x60000000);

        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);

        switch (this.getOption().getType()) {
            case BOOL:
                FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getOption().getName(), this.getX() + 3, this.getY() + 4, ((BoolOption) option).getValue() ? getWindow().getColor() : 0xffaaaaaa);
                break;
            case STRINGARRAY:
                FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getOption().getName() + ": \247f" + ((StringArrayOption) this.getOption()).getValue(), this.getX() + 3, this.getY() + 4, 0xffaaaaaa);
                break;
            case STRING:
                FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getOption().getName(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
                FontHelper.INSTANCE.drawCenteredString(matrixStack, ((StringOption)option).getValue(), this.getX() + (this.getWidth() / 2), this.getY() + 14, 0xffaaaaaa);
                if (EventAPI.getInstance().alreadyRegistered(this)) {
                    Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), getWindow().getColor(), 0x00ffffff, 1);
                }
                break;
            case COLOR:
            case INT:
            case FLOAT:
                drawSliders(this.getOption(), matrixStack);
                break;
        }
        if (hasChild()) {
            matrixStack.push();
            matrixStack.translate(this.getX() + this.getWidth() - 7, this.getY() + 7.5f, 0);
            matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, 1.0F), cogSpin, true));
            Render2DHelper.INSTANCE.drawArrow(matrixStack, 0, 0, this.isOpen(), !this.isOpen() ? 0xff999999 : getWindow().getColor());
            matrixStack.pop();
        }
        if (isOpen())
            this.getChildren().forEach(button -> {
                button.draw(matrixStack);
            });
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered()) {
            if (int_1 == 0) {
                if (this.getOption() instanceof BoolOption) {
                    ((BoolOption) this.getOption()).setValue(!((BoolOption) this.getOption()).getValue());
                    if (ClickGui.doesPlayClickSound())
                        Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                if (this.getOption() instanceof StringArrayOption) {
                    ((StringArrayOption) this.getOption()).inc();
                    if (this.isOpen())
                        this.close();
                    if (ClickGui.doesPlayClickSound())
                        Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                if (this.getOption() instanceof StringOption) {
                    if (!EventAPI.getInstance().alreadyRegistered(this))
                        EventAPI.getInstance().register(this);
                    if (ClickGui.doesPlayClickSound())
                        Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                if (this.getOption() instanceof FloatOption || this.getOption() instanceof IntOption || this.getOption() instanceof ColorOption) {
                    isSliding = true;
                }
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    FeatureFile.write();
                return;
            }
            if (int_1 == 1) {
                this.setOpen(!this.isOpen());
                if (this.isOpen())
                    this.open();
                else
                    this.close();
            }
        } else {
            if (this.getOption() instanceof StringOption) {
                while (EventAPI.getInstance().alreadyRegistered(this))
                    EventAPI.getInstance().unregister(this);
            }
        }
        getChildren().forEach(button -> {
            button.click(double_1, double_2, int_1);
        });
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (Button button : this.getChildren()) {
            if (button != this)
                button.keyTyped(typedChar, keyCode);
        }
    }

    private boolean hasChild() {
        if (this.getOption() instanceof StringArrayOption) {
            StringArrayOption sArrayOption = (StringArrayOption) this.getOption();
            for (Option option : this.getOption().getChildren()) {
                if (option.hasDependency()) {
                    if (option.getDependency().equalsIgnoreCase(sArrayOption.getValue()))
                        return true;
                } else {
                    return true;
                }
            }
        } else {
            return this.getOption().hasChild();
        }

        return false;
    }

    public void open() {
        buttonsHeight = 0;
        option.getChildren().forEach(option ->
        {
            OptionButton optionButton = new OptionButton(this.getWindow(), option, this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, option instanceof ColorOption ? 100 : 15);
            optionButton.masterButton = this.masterButton == null ? this : this.masterButton;
            optionButton.parentButton = this;

            if (option instanceof StringOption)
                optionButton.setHeight(this.getHeight() + 10);

            if (optionButton.getOption().hasDependency() && this.getOption() instanceof StringArrayOption) {
                if (optionButton.getOption().getDependency().equalsIgnoreCase(((StringArrayOption) this.getOption()).getValue())) {
                    this.getChildren().add(optionButton);
                    buttonsHeight += optionButton.getHeight();
                }
            } else {
                this.getChildren().add(optionButton);
                buttonsHeight += optionButton.getHeight();
            }
        });
        allButtonsAfter().forEach(button -> {
            button.move(0, buttonsHeight);
        });
    }

    @Override
    public ArrayList<Button> allButtonsAfter() {
        ArrayList<Button> buttons = new ArrayList<>();

        if (parentButton != null)
            parentButton.getChildren().forEach(button -> {
                if (parentButton.getChildren().indexOf(button) > parentButton.getChildren().indexOf(this)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            });

        getWindow().get(this.getOption().getFeature()).getChildren().forEach(button -> {
            if (this.masterButton == null) {
                if (getWindow().get(this.getOption().getFeature()).getChildren().indexOf(button) > getWindow().get(this.getOption().getFeature()).getChildren().indexOf(this)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            } else {
                if (getWindow().get(this.getOption().getFeature()).getChildren().indexOf(button) > getWindow().get(this.getOption().getFeature()).getChildren().indexOf(masterButton)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            }
        });
        buttons.addAll(super.allButtonsAfter(getWindow().get(this.getOption().getFeature())));
        return buttons;
    }

    public void close() {
        this.getChildren().forEach(button -> {
            if (button instanceof OptionButton) {
                if (button.isOpen())
                    ((OptionButton) button).close();
            }
        });
        allButtonsAfter().forEach(button -> {
            button.move(0, -buttonsHeight);
        });
        this.getChildren().clear();
        this.setOpen(false);
    }

    private void updateOnOff() {
        if (!timer.hasPassed(10))
            return;
        timer.reset();
        if (this.getOption().getType() == OpType.BOOL) {
            boolean enabled = ((BoolOption) this.getOption()).getValue();
            for (int i = 0; i < 2; i++) {
                if (enabled) {
                    if (togglePos < 20) {
                        togglePos++;
                    }
                } else {
                    if (togglePos > 0) {
                        togglePos--;
                    }
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            if (this.isOpen()) {
                if (cogSpin < 150) {
                    cogSpin++;
                }
            } else {
                if (cogSpin > 0) {
                    cogSpin--;
                }
            }
        }
    }

    @EventListener(events = {EventKeyPressed.class})
    private void handleKeys(EventKeyPressed eventKeyPressed) {
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ClickGui)) {
            while (EventAPI.getInstance().alreadyRegistered(this))
                EventAPI.getInstance().unregister(this);
            return;
        }
        int keyCode = eventKeyPressed.getKey();
        StringOption stringOption = (StringOption)option;
        if (Screen.isPaste(keyCode)) {
            stringOption.setValue(stringOption.getValue() + MinecraftClient.getInstance().keyboard.getClipboard());
            return;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_ESCAPE:
                while (EventAPI.getInstance().alreadyRegistered(this))
                    EventAPI.getInstance().unregister(this);
                break;
            case GLFW.GLFW_KEY_SPACE:
                stringOption.setValue(stringOption.getValue() + " ");
                break;
            case GLFW.GLFW_KEY_BACKSPACE:
                if (stringOption.getValue().isEmpty())
                    break;
                String str = stringOption.getValue().substring(0, stringOption.getValue().length() - 1);
                stringOption.setValue(str);
                break;
            default:
                String keyName = InputUtil.fromKeyCode(keyCode, eventKeyPressed.getScancode()).getTranslationKey().replace("key.keyboard.", "");
                if (keyName.length() == 1) {
                    if (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                        keyName = keyName.toUpperCase();
                        if (isInt(keyName))
                            keyName = getFromNumKey(Integer.parseInt(keyName));
                    }
                    stringOption.setValue(stringOption.getValue() + keyName);
                }
                break;
        }
    }

    private boolean isInt(String intStr) {
        try {
            Integer.parseInt(intStr);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    private String getFromNumKey(int i) {
        switch (i) {
            case 1:
                return "!";
            case 2:
                return "@";
            case 3:
                return "#";
            case 4:
                return "$";
            case 5:
                return "%";
            case 6:
                return "^";
            case 7:
                return "&";
            case 8:
                return "*";
            case 9:
                return "(";
            case 0:
                return ")";
        }
        return String.valueOf(i);
    }

    public void drawSliders(Option property, MatrixStack matrixStack) {
        if (property instanceof FloatOption) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    FeatureFile.write();
            }
            FloatOption v = (FloatOption) property;

            float startV = v.getValue() - v.getMin();

            float pos = ((float) (startV) / (v.getMax() - v.getMin())) * (this.getWidth());


            handleSliders(v);

            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + pos, this.getY() + this.getHeight(), Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(getWindow().getColor())).darker().getRGB());
            FontHelper.INSTANCE.drawCenteredString(matrixStack, property.getName() + ": " + ((FloatOption) property).getValue(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        }
        if (property instanceof IntOption) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
            }
            IntOption v = (IntOption) property;


            float startV = v.getValue() - v.getMin();

            float pos = ((float) (startV) / (v.getMax() - v.getMin())) * (this.getWidth());

            handleSliders(v);

            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + pos, this.getY() + this.getHeight(), Color.decode("0x" + Integer.toHexString(getWindow().getColor()).substring(2)).darker().getRGB());
            FontHelper.INSTANCE.drawCenteredString(matrixStack, property.getName() + ": " + ((IntOption) property).getValue(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        }

        if (property instanceof ColorOption) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
            }
            ColorOption v = (ColorOption) property;


            float huepos = (((float) v.getH() / 270)) * (80);

            float satpos = ((float) (v.getS())) * (80);
            float brightpos = ((float) ((1 - v.getB())) * 79);


            handleSliders(v);
            Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, -1, 0xff000000);
            drawGradientRect(matrixStack, this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, ColorHelper.INSTANCE.getColorViaHue(v.getH()).getRGB(), 0xff000000);
            Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, 0x20000000, 0xff000000);
            //color cursor
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 5 + satpos - 1, this.getY() + 15 + brightpos - 1, this.getX() + 5 + satpos + 1, this.getY() + 15 + brightpos + 1, -1);

            //hue slider
            Render2DHelper.INSTANCE.bindTexture(colorSlider);
            DrawableHelper.drawTexture(matrixStack, (int) this.getX() + (int) this.getWidth() - 10, (int) this.getY() + 15, 0, 0, 5, 80, 10, 80);
            //hue cursor
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + this.getWidth() - 10, this.getY() + 15 + huepos - 1, (this.getX() + this.getWidth() - 10) + 5, this.getY() + 15 + huepos + 1, -1);

            FontHelper.INSTANCE.drawWithShadow(matrixStack, property.getName(), this.getX() + 3, this.getY() + 3, v.getValue());
        }
    }


    protected void drawGradientRect(MatrixStack matrixStack, float left, float top, float right, float bottom, int startColor, int endColor) {
        Matrix4f matrix = matrixStack.peek().getModel();
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float g = (float) (startColor >> 16 & 255) / 255.0F;
        float h = (float) (startColor >> 8 & 255) / 255.0F;
        float i = (float) (startColor & 255) / 255.0F;
        float j = (float) (endColor >> 24 & 255) / 255.0F;
        float k = (float) (endColor >> 16 & 255) / 255.0F;
        float l = (float) (endColor >> 8 & 255) / 255.0F;
        float m = (float) (endColor & 255) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, (float) right, (float) top, (float) 0).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) top, (float) 0).color(1, 1, 1, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, (float) 0).color(0, 0, 0, j).next();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, (float) 0).color(k, l, m, j).next();

        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    void handleSliders(IntOption v) {
        if (MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
            float position = MouseHelper.INSTANCE.getMouseX() - this.getX();//.INSTANCE.INSTANCE.INSTANCE.INSTANCE.INSTANCE
            float percent = position / this.getWidth() * 100;
            float increment = v.getInc();
            if (percent > 100) {
                percent = 100;
            }
            if (percent < 0) {
                percent = 0;
            }
            float value = (percent / 100) * ((v.getMax() - v.getMin()) + increment);
            value += v.getMin();
            if (value > v.getMax()) {
                value = v.getMax();
            }
            if (value < v.getMin()) {
                value = v.getMin();
            }
            v.setValue((int) ((int) Math.round(value * (1.0D / increment)) / (1.0D / increment)));
            v.setValue((int) ClientMathHelper.INSTANCE.round(v.getValue(), 2));
        }
    }

    void handleSliders(FloatOption v) {
        if (MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
            float position = MouseHelper.INSTANCE.getMouseX() - this.getX();
            float percent = position / this.getWidth() * 100;
            float increment = v.getInc();
            if (percent > 100) {
                percent = 100;
            }
            if (percent < 0) {
                percent = 0;
            }
            float value = (percent / 100) * ((v.getMax() - v.getMin()) + increment);
            value += v.getMin();
            if (value > v.getMax()) {
                value = v.getMax();
            }
            if (value < v.getMin()) {
                value = v.getMin();
            }
            v.setValue((float) ((float) Math.round(value * (1.0D / increment)) / (1.0D / increment)));
            v.setValue((float) ClientMathHelper.INSTANCE.round(v.getValue(), 2));
        }
    }

    void handleSliders(ColorOption v) {
        if (MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
            if (MouseHelper.INSTANCE.getMouseX() > this.getX() + 100) {
                float position = MouseHelper.INSTANCE.getMouseY() - (this.getY() + 15);
                float percent = position / 79 * 100;
                float increment = 1;
                if (percent > 100) {
                    percent = 100;
                }
                if (percent < 0) {
                    percent = 0;
                }
                float value = (percent / 100) * ((270) + increment);
                if (value > 270) {
                    value = 270;
                }
                if (value < 0) {
                    value = 0;
                }
                v.setH((int) value);
            } else {
                float position = MouseHelper.INSTANCE.getMouseX() - (this.getX() + 5);
                float percent = position / 80 * 100;
                if (percent > 100) {
                    percent = 100;
                }
                if (percent < 0) {
                    percent = 0;
                }
                v.setS(percent / 100);

                position = MouseHelper.INSTANCE.getMouseY() - (this.getY() + 15);
                percent = position / 79 * 100;
                percent = 100 - percent;
                if (percent > 100) {
                    percent = 100;
                }
                if (percent < 0) {
                    percent = 0;
                }

                v.setB(percent / 100);
            }
            v.setValue(ColorHelper.INSTANCE.getColorViaHue(v.getH(), v.getS(), v.getB()).getRGB());
        }
    }

    public Option getOption() {
        return option;
    }
}
