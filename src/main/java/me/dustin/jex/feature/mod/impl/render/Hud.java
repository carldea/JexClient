package me.dustin.jex.feature.mod.impl.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender2DItem;
import me.dustin.jex.event.render.EventRenderEffects;
import me.dustin.jex.gui.click.jex.JexGui;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.gui.click.window.impl.Window;
import me.dustin.jex.gui.tab.TabGui;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.TPSHelper;
import me.dustin.jex.helper.misc.Lagometer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.util.List;
import java.util.*;

@Feature.Manifest(name = "HUD", category = Feature.Category.VISUAL, description = "Renders an in-game HUD")
public class Hud extends Feature {

    @Op(name = "Client Color", isColor = true)
    public int clientColor = 0xff00a1ff;
    @OpChild(name = "Rainbow", parent = "Client Color")
    public boolean rainbowClientColor;
    @Op(name = "Watermark")
    public boolean watermark = true;
    @OpChild(name = "Jex Effect", all = {"Static", "Spin Only", "Flip Only", "SpinFlip"}, parent = "Watermark")
    public String watermarkMode = "Static";
    @Op(name = "Draw Face")
    public boolean drawFace = true;
    @Op(name = "Array List")
    public boolean showArrayList = true;
    @OpChild(name = "Color", parent = "Array List", all = {"Client Color", "Rainbow", "Category"})
    public String colorMode = "Client Color";
    @OpChild(name = "Rainbow Speed", parent = "Color", min = 1, max = 20, dependency = "Rainbow")
    public int rainbowSpeed = 3;
    @OpChild(name = "Start Pos", all = {"Top", "Bottom"}, parent = "Array List")
    public String arrayListStartPos = "Top";
    @OpChild(name = "Rainbow Saturation", parent = "Color", dependency = "Rainbow", inc = 0.1f)
    public float rainbowSaturation = 1;
    @Op(name = "Potion Effects")
    public boolean potionEffects = true;
    @OpChild(name = "Icons", parent = "Potion Effects")
    public boolean icons = true;
    @Op(name = "Lagometer")
    public boolean lagometer = true;
    @Op(name = "Coordinates")
    public boolean coords = false;
    @OpChild(name = "Nether Coords", parent = "Coordinates")
    public boolean netherCoords = true;
    @Op(name = "Armor")
    public boolean armor = true;
    @OpChild(name = "Damage", all = {"Bar", "Text"}, parent = "Armor")
    public String damageMode = "Bar";
    @OpChild(name = "Text Mode", all = {"Percent", "Damage Taken", "Damage Left"}, parent = "Damage", dependency = "Text")
    public String textMode = "Percent";
    @OpChild(name = "Draw Mode", all = {"Tall", "Cube"}, parent = "Armor")
    public String drawMode = "Tall";
    @Op(name = "Item Durability")
    public boolean itemDurability = true;
    @OpChild(name = "Static Color", parent = "Item Durability")
    public boolean staticColor = false;
    @Op(name = "Info")
    public boolean info = true;
    @Op(name = "TabGui")
    public boolean tabGui = true;
    @OpChild(name = "Hover Bar", parent = "TabGui")
    public boolean hoverBar;
    @OpChild(name = "TabGui Width", parent = "TabGui", min = 55, max = 200)
    public float tabGuiWidth = 75;
    @OpChild(name = "Button Height", parent = "TabGui", min = 10, max = 25)
    public float buttonHeight = 12;
    @OpChild(name = "Show Username", parent = "Info")
    public boolean showUsername = true;
    @OpChild(name = "Server", parent = "Info")
    public boolean serverName = true;
    @OpChild(name = "Ping", parent = "Info")
    public boolean ping = true;
    @OpChild(name = "TPS", parent = "Info")
    public boolean tps = true;
    @OpChild(name = "Show Instant", parent = "TPS")
    public boolean instantTPS = false;
    @OpChild(name = "FPS", parent = "Info")
    public boolean fps = true;
    @OpChild(name = "Biome", parent = "Info")
    public boolean biome = true;
    @OpChild(name = "Player Count", parent = "Info")
    public boolean playerCount = true;
    @OpChild(name = "Build Info", parent = "Info")
    public boolean buildInfo = false;
    @OpChild(name = "Yaw/Pitch", parent = "Info")
    public boolean yawAndPitch = true;
    @OpChild(name = "Direction", parent = "Info")
    public boolean direction = true;
    @OpChild(name = "Saturation", parent = "Info")
    public boolean saturation = false;
    @OpChild(name = "Speed", parent = "Info")
    public boolean speed = false;
    @OpChild(name = " ", parent = "Speed", all = {"Blocks", "Feet", "Miles", "KM"})
    public String distanceMode = "Blocks";
    @OpChild(name = "Per", parent = " ", all = {"Second", "Tick", "Minute", "Hour", "Day"})
    public String timeMode = "Second";

    private int num;
    private int spriteCount = 0;
    private int infoCount = 0;
    private int rainbowScroll = 0;
    private boolean flipRot;
    private int rot = 0;

    private float lagOMeterY = -11;
    private float coordsY = -999;
    private ArrayList<Feature> mods = new ArrayList<>();

    public Hud() {
        this.setState(true);
        this.setVisible(false);
    }

    @EventListener(events = {EventRender2D.class})
    private void runRenderMethod(EventRender2D eventRender2D) {
        if (Wrapper.INSTANCE.getOptions().debugEnabled)
            return;
        if (watermark)
            drawWatermark(eventRender2D);
        if (drawFace && Wrapper.INSTANCE.getMinecraft().getNetworkHandler() != null && Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerListEntry(Wrapper.INSTANCE.getMinecraft().getSession().getProfile().getId()) != null) {
            Render2DHelper.INSTANCE.drawFace(eventRender2D.getMatrixStack(), watermark ? 35 : 2, 2, 4, Objects.requireNonNull(Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerListEntry(Wrapper.INSTANCE.getMinecraft().getSession().getProfile().getId())).getSkinTexture());
        }
        drawPotionEffectsAndCoordinates(eventRender2D);
        if (lagometer)
            drawLagometer(eventRender2D);
        if (armor)
            drawArmor(eventRender2D);
        if (showArrayList)
            drawArrayList(eventRender2D);
        if (info)
            drawInfo(eventRender2D);
        if (tabGui) {
            TabGui.INSTANCE.setHoverBar(hoverBar);
            TabGui.INSTANCE.draw(eventRender2D.getMatrixStack(), 2, 35 + (10 * infoCount), tabGuiWidth, buttonHeight);
        }
        for (Window window : ClickGui.windows) {
            if (window.isPinned() && !(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ClickGui || Wrapper.INSTANCE.getMinecraft().currentScreen instanceof JexGui)) {
                window.draw(eventRender2D.getMatrixStack());
            }
        }
    }

    @Override
    public void onEnable() {
        if (!EventAPI.getInstance().alreadyRegistered(TabGui.INSTANCE))
            EventAPI.getInstance().register(TabGui.INSTANCE);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        while (EventAPI.getInstance().alreadyRegistered(TabGui.INSTANCE))
            EventAPI.getInstance().unregister(TabGui.INSTANCE);
        super.onDisable();
    }

    @EventListener(events = {EventTick.class})
    private void updatePositions(EventTick eventTick) {

        switch (watermarkMode) {
            case "Static":
                break;
            case "SpinFlip":
            case "Spin Only":
                rot+=2;
                if (rot > 360)
                    rot -= 360;
                break;
            case "Flip Only":
                if (flipRot) {
                    rot-=2;
                    if (rot <= 0)
                        flipRot = false;
                } else {
                    rot+=2;
                    if (rot >= 90)
                        flipRot = true;
                }
                break;
        }

        rainbowScroll += rainbowSpeed;

        float shouldBeY = Lagometer.INSTANCE.isServerLagging() ? 2 : -11;
        float distance = Math.abs(lagOMeterY - shouldBeY);

        if (distance > 30)
            lagOMeterY = shouldBeY;
        if (lagOMeterY < shouldBeY)
            lagOMeterY += distance * 0.5f;
        if (lagOMeterY > shouldBeY)
            lagOMeterY -= distance * 0.5f;

        shouldBeY = Render2DHelper.INSTANCE.getScaledHeight() - 10;
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen)
            shouldBeY = Render2DHelper.INSTANCE.getScaledHeight() - 23;
        distance = Math.abs(coordsY - shouldBeY);

        if (distance > 30)
            coordsY = shouldBeY;
        if (coordsY < shouldBeY)
            coordsY += distance * 0.5f;
        if (coordsY > shouldBeY)
            coordsY -= distance * 0.5f;
    }

    @EventListener(events = {EventRenderEffects.class})
    private void effects(EventRenderEffects eventRenderEffects) {
        if (this.potionEffects)
            eventRenderEffects.cancel();
    }

    public void drawWatermark(EventRender2D eventRender2D) {
        int x = 17;
        int y = 17;
        MatrixStack matrixStack = eventRender2D.getMatrixStack();
        matrixStack.push();
        matrixStack.translate(x, y, 0);
        switch (watermarkMode) {
            case "Static":
                break;
            case "Spin Only":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, 1.0F), rot, true));
                break;
            case "Flip Only":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 1F, 0F), rot, true));
                break;
            case "SpinFlip":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.5f, 1F), rot, true));
                break;
        }

        matrixStack.translate(-x, -y, 0);
        float newX = x - (FontHelper.INSTANCE.getStringWidth("Jex") / 2);
        Render2DHelper.INSTANCE.drawFullCircle(x, y, 15, 0x80252525, matrixStack);
        Render2DHelper.INSTANCE.drawArc(x, y, 15, ColorHelper.INSTANCE.getClientColor(), 0, 360, 1, matrixStack);
        FontHelper.INSTANCE.draw(eventRender2D.getMatrixStack(), "Jex", newX, y - 9, ColorHelper.INSTANCE.getClientColor());
        matrixStack.scale(0.75f, 0.75f, 1);
        float newX1 = x - (FontHelper.INSTANCE.getStringWidth(JexClient.INSTANCE.getVersion()) / (2 / 0.75f));
        FontHelper.INSTANCE.draw(eventRender2D.getMatrixStack(), JexClient.INSTANCE.getVersion(), newX1 / 0.75f, y / 0.75f + 1, ColorHelper.INSTANCE.getClientColor());
        matrixStack.scale(1 / 0.75f, 1 / 0.75f, 1);
        matrixStack.translate(x, y, 0);

        switch (watermarkMode) {
            case "Static":
                break;
            case "Spin Only":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, -1.0F), rot, true));
                break;
            case "Flip Only":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, -1F, 0F), rot, true));
                break;
            case "SpinFlip":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, -0.5f, -1F), rot, true));
                break;
        }
        matrixStack.pop();
    }

    public void drawLagometer(EventRender2D eventRender2D) {
        FontHelper.INSTANCE.drawCenteredString(eventRender2D.getMatrixStack(), String.format("Server is lagging: %.1f", (float) Lagometer.INSTANCE.getLagTime() / 1000), Render2DHelper.INSTANCE.getScaledWidth() / 2, lagOMeterY, ColorHelper.INSTANCE.getClientColor());
    }

    public void drawArrayList(EventRender2D eventRender2D) {
        if (mods.isEmpty())
            mods.addAll(FeatureManager.INSTANCE.getFeatures());

        int count = 0;
        if (showArrayList) {
            reorderArrayList(mods);
            num = 0;

            mods.forEach(mod -> {
                if (mod.isVisible() && mod.getState())
                    num++;
            });
            for (Feature mod : mods) {
                float x = Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(mod.getDisplayName()) - 2;
                float y = arrayListStartPos.equalsIgnoreCase("Top") ? 2 : coordsY - (bottomRightCount * 10);

                int color = getRainbowColor(count, num);
                if (colorMode.equalsIgnoreCase("Category"))
                    color = getCategoryColor(mod.getFeatureCategory());
                if (colorMode.equalsIgnoreCase("Client Color"))
                    color = ColorHelper.INSTANCE.getClientColor();
                if (mod.isVisible() && mod.getState()) {
                    FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), mod.getDisplayName(), x, y + (arrayListStartPos.equalsIgnoreCase("Top") ? (11 * count) : -(11 * count)), color);
                    count++;
                }
            }
        }
    }

    @EventListener(events = {EventRender2DItem.class})
    public void onRender2DItem(EventRender2DItem eventRender2DItem) {
        if (this.itemDurability)
            drawItemDurability(eventRender2DItem);
    }

    private void drawItemDurability(EventRender2DItem eventRender2DItem) {
        if (eventRender2DItem.getStack().getMaxDamage() > 0) {
            int maxDamage = eventRender2DItem.getStack().getMaxDamage();
            int damage = eventRender2DItem.getStack().getDamage();
            int durability = maxDamage - damage;
            float percent = (((float) eventRender2DItem.getStack().getMaxDamage() - (float) eventRender2DItem.getStack().getDamage()) / (float) eventRender2DItem.getStack().getMaxDamage()) * 100;


            /*int color = (int) ((float) damage / maxDamage * 0xFF) << 16;
            color += (int) ((float) durability / maxDamage * 0xFF) << 8;*/
            int color = Render2DHelper.INSTANCE.getPercentColor(percent);

            MatrixStack matrixStack = new MatrixStack();
            matrixStack.translate(0.0, 0.0, eventRender2DItem.getItemRenderer().zOffset + 200.0);
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            eventRender2DItem.getFontRenderer().draw(Integer.toString(durability), eventRender2DItem.getX() * 2.0f, eventRender2DItem.getY() * 2.0f, staticColor ? 0xFFFFFF : color, true, matrixStack.peek().getModel(), immediate, false, 0, 15728880);
            immediate.draw();
        }
    }

    private void drawArmor(EventRender2D eventRender2D) {
        if (!armor)
            return;
        int count = 0;
        float midX = Render2DHelper.INSTANCE.getScaledWidth() / 2;
        float y = coordsY - 5;
        float x = midX + 90;
        for (ItemStack armorItem : InventoryHelper.INSTANCE.getInventory().armor) {
            if (armorItem.getItem() != Items.AIR) {
                if (drawMode.equalsIgnoreCase("cube") && count == 2) {
                    x += 20;
                    y = coordsY - 5;
                }
                Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = -200.0F;
                Wrapper.INSTANCE.getMinecraft().getItemRenderer().renderInGui(armorItem, (int) x, (int) y);
                if (damageMode.equalsIgnoreCase("Bar"))
                    Wrapper.INSTANCE.getMinecraft().getItemRenderer().renderGuiItemOverlay(Wrapper.INSTANCE.getMinecraft().textRenderer, armorItem, (int) midX + 90, (int) y, "");
                Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = 0.0F;

                if (damageMode.equalsIgnoreCase("Text") && armorItem.isDamageable()) {
                    float xOffset = drawMode.equalsIgnoreCase("cube") ? 0 : 15;
                    if (drawMode.equalsIgnoreCase("cube")) {
                        MatrixStack matrixStack = eventRender2D.getMatrixStack();
                        matrixStack.push();
                        matrixStack.scale(0.75f, 0.75f, 1);
                        FontHelper.INSTANCE.drawCenteredString(eventRender2D.getMatrixStack(), getDamageText(armorItem), (x + 8) / 0.75f, (y + 8) / 0.75f, -1);
                        matrixStack.pop();
                    } else {
                        float percent = (((float) armorItem.getMaxDamage() - (float) armorItem.getDamage()) / (float) armorItem.getMaxDamage()) * 100;
                        FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), getDamageText(armorItem), x + xOffset, y + (drawMode.equalsIgnoreCase("cube") ? 8 : 5), Render2DHelper.INSTANCE.getPercentColor(percent));
                    }
                }
                y -= 15;
                count++;
            }
        }
    }

    private String getDamageText(ItemStack armorItem) {
        int percent = (int) ((float) (armorItem.getMaxDamage() - armorItem.getDamage()) / armorItem.getMaxDamage() * 100);
        switch (textMode) {
            case "Percent":
                return percent + "\247f%";
            case "Damage Taken":
                return String.valueOf(armorItem.getDamage());
            case "Damage Left":
                return String.valueOf(armorItem.getMaxDamage() - armorItem.getDamage());
        }
        return "";
    }

    int bottomRightCount = 0;
    public void drawPotionEffectsAndCoordinates(EventRender2D eventRender2D) {
        bottomRightCount = 0;
        if (coords) {
            if (netherCoords) {
                double coordScale = Wrapper.INSTANCE.getLocalPlayer().clientWorld.getDimension().getCoordinateScale();
                if (coordScale != 1.0D) {
                    String coordString = String.format("Overworld\247f: \2477%.2f\247f/\2477%.2f\247f/\2477%.2f", Wrapper.INSTANCE.getLocalPlayer().getX() * coordScale, Wrapper.INSTANCE.getLocalPlayer().getBoundingBox().minY, Wrapper.INSTANCE.getLocalPlayer().getZ() * coordScale);
                    FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), coordString, Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(coordString) - 2, coordsY - (bottomRightCount * 10), ColorHelper.INSTANCE.getClientColor());
                } else {
                    String coordString = String.format("Nether\247f: \2477%.2f\247f/\2477%.2f\247f/\2477%.2f", Wrapper.INSTANCE.getLocalPlayer().getX() / 8, Wrapper.INSTANCE.getLocalPlayer().getBoundingBox().minY, Wrapper.INSTANCE.getLocalPlayer().getZ() / 8);
                    FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), coordString, Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(coordString) - 2, coordsY - (bottomRightCount * 10), ColorHelper.INSTANCE.getClientColor());
                }
                bottomRightCount++;
            }
            String coordString = String.format("XYZ\247f: \2477%.2f\247f/\2477%.2f\247f/\2477%.2f", Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getBoundingBox().minY, Wrapper.INSTANCE.getLocalPlayer().getZ());
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), coordString, Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(coordString) - 2, coordsY - (bottomRightCount * 10), ColorHelper.INSTANCE.getClientColor());
            bottomRightCount++;
        }
        spriteCount = bottomRightCount;
        if (potionEffects) {
            List<Runnable> list_1 = Lists.newArrayListWithExpectedSize(Wrapper.INSTANCE.getLocalPlayer().getActiveStatusEffects().size());
            for (StatusEffectInstance effect : Wrapper.INSTANCE.getLocalPlayer().getActiveStatusEffects().values()) {
                String effectString = String.format("%s %s\247f: \2477%s", effect.getEffectType().getName().getString(), getAmpString(effect), StatusEffectUtil.durationToString(effect, 1.0F));
                FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), effectString, Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(effectString) - (icons ? 11 : 2), coordsY - (bottomRightCount * 10), effect.getEffectType().getColor());

                if (icons) {
                    Sprite sprite_1 = Wrapper.INSTANCE.getMinecraft().getStatusEffectSpriteManager().getSprite(effect.getEffectType());
                    list_1.add(() -> {
                        Render2DHelper.INSTANCE.bindTexture(sprite_1.getAtlas().getId());
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
                        DrawableHelper.drawSprite(eventRender2D.getMatrixStack(), Render2DHelper.INSTANCE.getScaledWidth() - 10, (int) coordsY - 1 - (spriteCount * 10), 50, 9, 9, sprite_1);
                        spriteCount++;
                    });
                }
                bottomRightCount++;
            }
            list_1.forEach(Runnable::run);
        }
    }

    private void drawInfo(EventRender2D eventRender2D) {
        infoCount = 0;
        float topX = drawFace ? 70 : 35;
        if (!watermark) {
            topX -= 33;
        }
        if (showUsername) {
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("Username\247f: \2477%s", Wrapper.INSTANCE.getMinecraft().getSession().getUsername()), topX, 2 + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (tps) {
            String tpsString = instantTPS ? String.format("TPS\247f: \2477%.2f \247rInstant\247f: \2477%.2f", TPSHelper.INSTANCE.getAverageTPS(), TPSHelper.INSTANCE.getTPS(2)) : String.format("TPS\247f: \2477%.2f", TPSHelper.INSTANCE.getAverageTPS());
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), tpsString, topX, 2 + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (fps) {
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("FPS\247f: \2477%s", Wrapper.INSTANCE.getMinecraft().fpsDebugString.split(" ")[0]), topX, 2 + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        infoCount = 0;
        float startY = 35;
        if (!info)
            return;
        if (serverName) {
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("Server\247f: \2477%s", WorldHelper.INSTANCE.getCurrentServerName() + " " + (Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry() == null ? SharedConstants.getGameVersion().getName() : Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry().version.getString())), 2, startY + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (ping) {
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("Ping\247f: \2477%d", Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerListEntry(Wrapper.INSTANCE.getLocalPlayer().getUuid()) == null ? 0 : Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerListEntry(Wrapper.INSTANCE.getLocalPlayer().getUuid()).getLatency()), 2, startY + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (yawAndPitch) {
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("Look\247f: \2477%s \2477%s", ClientMathHelper.INSTANCE.roundToPlace(MathHelper.wrapDegrees(PlayerHelper.INSTANCE.getYaw()), 1), ClientMathHelper.INSTANCE.roundToPlace(MathHelper.wrapDegrees(PlayerHelper.INSTANCE.getPitch()), 1)), 2, startY + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (biome) {
            String str = Wrapper.INSTANCE.getWorld().getRegistryManager().get(Registry.BIOME_KEY).getId(Wrapper.INSTANCE.getWorld().getBiome(Wrapper.INSTANCE.getLocalPlayer().getBlockPos())).getPath().replace("_", " ");
            str = WordUtils.capitalizeFully(str);
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("Biome\247f: \2477%s", str), 2, startY + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (speed) {
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("Speed\247f:\2477 %s", generateSpeedText()), 2, startY + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (direction) {
            Direction direction = Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing();
            String string7;
            switch (direction) {
                case NORTH:
                    string7 = "(-Z)";
                    break;
                case SOUTH:
                    string7 = "(+Z)";
                    break;
                case WEST:
                    string7 = "(-X)";
                    break;
                case EAST:
                    string7 = "(+X)";
                    break;
                default:
                    string7 = "";
            }
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("Direction\247f: \2477%s%s %s", Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing().getName().substring(0, 1).toUpperCase(), Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing().getName().substring(1), string7), 2, startY + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (saturation) {
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("Saturation\247f: \2477%s", Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getSaturationLevel()), 2, startY + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (playerCount) {
            String str = String.format("Player Count\247f: \2477%d", Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerList() == null ? 0 : Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerList().size());
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), str, 2, startY + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
        if (buildInfo) {
            String buildMetaData =  String.format("%s %s", JexClient.INSTANCE.getBuildMetaData().equals("${buildVersion}") ? "Built Improperly" : JexClient.INSTANCE.getBuildMetaData(), FabricLoader.getInstance().isDevelopmentEnvironment() ? "(Dev)" : "(Release)");
            FontHelper.INSTANCE.drawWithShadow(eventRender2D.getMatrixStack(), String.format("Build\247f: \2477%s", buildMetaData), 2, startY + (10 * infoCount), ColorHelper.INSTANCE.getClientColor());
            infoCount++;
        }
    }

    private String getAmpString(StatusEffectInstance effectInstance) {
        switch (effectInstance.getAmplifier()) {
            case -1:
                return ">120";
            case 0:
                return "I";
            case 1:
                return "II";
            case 2:
                return "III";
            case 3:
                return "IV";
            case 4:
                return "V";
            case 5:
                return "VI";
            case 6:
                return "VII";
            case 7:
                return "VIII";
            case 8:
                return "IX";
            case 9:
                return "X";
        }
        return effectInstance.getAmplifier() + "";
    }

    public int getRainbowColor(int count, int max) {
        if (max == 0)
            max = 1;
        int inc = 270 / max;
        int hue = rainbowScroll + count * inc;
        return ColorHelper.INSTANCE.getColorViaHue(hue % 270, rainbowSaturation).getRGB();
    }

    public static int getCategoryColor(Feature.Category category) {
        switch (category) {
            case MOVEMENT:
                return new Color(141, 95, 255).getRGB();
            case VISUAL:
                return new Color(255, 92, 252).getRGB();
            case PLAYER:
                return new Color(64, 255, 83).getRGB();
            case MISC:
                return new Color(247, 255, 65).getRGB();
            case WORLD:
                return new Color(74, 84, 255).getRGB();
            case COMBAT:
                return new Color(255, 61, 56).getRGB();
        }
        return -1;
    }

    private void reorderArrayList(ArrayList<Feature> mods) {
        Collections.sort(mods, new Comparator<Feature>() {
            public int compare(Feature mod, Feature mod1) {
                String name1 = mod.getDisplayName();
                String name2 = mod1.getDisplayName();
                if (FontHelper.INSTANCE.getStringWidth(name1) > FontHelper.INSTANCE.getStringWidth(name2)) {
                    return -1;
                }
                if (FontHelper.INSTANCE.getStringWidth(name1) < FontHelper.INSTANCE.getStringWidth(name2)) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private String generateSpeedText() {
        Vec3d move = new Vec3d(Wrapper.INSTANCE.getLocalPlayer().getX() - Wrapper.INSTANCE.getLocalPlayer().prevX, 0, Wrapper.INSTANCE.getLocalPlayer().getZ() - Wrapper.INSTANCE.getLocalPlayer().prevZ).multiply(20);
        switch (distanceMode) {
            case "Blocks":
                break;
            case "Feet":
                move = move.multiply(3.281);
                break;
            case "Miles":
                move = move.multiply(0.000621371);
                break;
            case "KM":
                move = move.multiply(0.001);
                break;

        }
        float time = 1;
        switch (timeMode) {
            case "Tick":
                time /= 20;
                break;
            case "Second":
                break;
            case "Minute":
                time *= 60;
                break;
            case "Hour":
                time *= 3600;
                break;
            case "Day":
                time *= 86400;
                break;
        }
        return String.format("%.2f %s/%s", (float) (Math.abs(length2D(move)) * time), distanceMode, timeMode);
    }

    public double length2D(Vec3d vec3d) {
        return MathHelper.sqrt((float)(vec3d.x * vec3d.x + vec3d.z * vec3d.z));
    }
}
