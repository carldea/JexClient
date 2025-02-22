//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.dustin.jex.gui.minecraft;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import me.dustin.jex.JexClient;
import me.dustin.jex.addon.Addon;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.CustomMainMenu;
import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.files.ClientSettingsFile;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.update.UpdateManager;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class JexTitleScreen extends Screen {
    public static final CubeMapRenderer PANORAMA_CUBE_MAP = new CubeMapRenderer(new Identifier("textures/gui/title/background/panorama"));
    private static final Identifier PANORAMA_OVERLAY = new Identifier("textures/gui/title/background/panorama_overlay.png");
    private static final Identifier MINECRAFT_TITLE_TEXTURE = new Identifier("textures/gui/title/minecraft.png");
    private static final Identifier JEX_TITLE_TEXTURE = new Identifier("jex", "gui/mc/jex-logo.png");
    private static final Identifier EDITION_TITLE_TEXTURE = new Identifier("textures/gui/title/edition.png");
    public static int background = 0;
    private static ArrayList<Background> backgrounds = new ArrayList<>();
    private final boolean isMinceraft;
    private final RotatingCubeMapRenderer backgroundRenderer;
    private final boolean doBackgroundFade;
    @Nullable
    private String splashText;
    private Screen realmsNotificationGui;
    private long backgroundFadeStart;
    private ArrayList<MainMenuButton> customButtons = new ArrayList<>();

    private CustomMainMenu customMainMenu;
    private Timer timer = new Timer();
    private boolean isDonator;

    public JexTitleScreen() {
        this(false);
    }

    public JexTitleScreen(boolean doBackgroundFade) {
        super(new TranslatableText("narrator.screen.title"));
        this.backgroundRenderer = new RotatingCubeMapRenderer(PANORAMA_CUBE_MAP);
        this.doBackgroundFade = doBackgroundFade;
        this.isMinceraft = (double) (new Random()).nextFloat() < 1.0E-4D;
        customMainMenu = (CustomMainMenu) Feature.get(CustomMainMenu.class);

        MCAPIHelper.INSTANCE.downloadPlayerSkin(Wrapper.INSTANCE.getMinecraft().getSession().getProfile().getId());
    }

    public static CompletableFuture<Void> loadTexturesAsync(TextureManager textureManager, Executor executor) {
        return CompletableFuture.allOf(textureManager.loadTextureAsync(MINECRAFT_TITLE_TEXTURE, executor), textureManager.loadTextureAsync(EDITION_TITLE_TEXTURE, executor), textureManager.loadTextureAsync(PANORAMA_OVERLAY, executor), PANORAMA_CUBE_MAP.loadTexturesAsync(textureManager, executor));
    }

    private boolean areRealmsNotificationsEnabled() {
        return this.client.options.realmsNotifications && this.realmsNotificationGui != null;
    }

    public void tick() {
        if (this.areRealmsNotificationsEnabled()) {
            //this.realmsNotificationGui.tick();
        }

    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        this.customButtons.clear();
        try {
            loadBackgrounds();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (this.splashText == null) {
            this.splashText = this.client.getSplashTextLoader().get();
        }

        this.textRenderer.getWidth("Copyright Mojang AB. Do not distribute!");
        int j = this.height / 4 + 48;

        this.initWidgetsNormal(j, 24);

        if (customMainMenu.customBackground) {
            if (!backgrounds.isEmpty()) {
                this.customButtons.add(new MainMenuButton(">", this.width - 22, this.height - 22, 20, 20, new ButtonListener() {
                    @Override
                    public void invoke() {
                        JexTitleScreen.background += 1;
                        if (JexTitleScreen.background > backgrounds.size() - 1) {
                            JexTitleScreen.background = 0;
                        }
                        ClientSettingsFile.write();
                    }
                }));
                this.customButtons.add(new MainMenuButton("<", this.width - 44, this.height - 22, 20, 20, new ButtonListener() {
                    @Override
                    public void invoke() {
                        JexTitleScreen.background -= 1;
                        if (JexTitleScreen.background < 0) {
                            JexTitleScreen.background = backgrounds.size() - 1;
                        }
                        ClientSettingsFile.write();
                    }
                }));
            } else {
                this.customButtons.add(new MainMenuButton("Open Backgrounds Folder", this.width - 152, this.height - 22, 150, 20, new ButtonListener() {
                    @Override
                    public void invoke() {
                        Util.getOperatingSystem().open(new File(ModFileHelper.INSTANCE.getJexDirectory(), "backgrounds"));
                    }
                }));
            }
        }
        if (JexTitleScreen.background < 0) {
            JexTitleScreen.background = backgrounds.size() - 1;
        } else if (JexTitleScreen.background > backgrounds.size() - 1) {
            JexTitleScreen.background = 0;
        }
        this.client.setConnectedToRealms(false);
    }

    private void initWidgetsNormal(int y, int spacingY) {
        JexTitleScreen titleScreen = this;
        this.customButtons.add(new MainMenuButton("Singleplayer", 2, y, 200, 20, new ButtonListener() {
            @Override
            public void invoke() {
                Wrapper.INSTANCE.getMinecraft().openScreen(new SelectWorldScreen(titleScreen));
            }
        }));

        this.customButtons.add(new MainMenuButton("Multiplayer", 2, y + spacingY * 1, 175, 20, new ButtonListener() {
            @Override
            public void invoke() {
                Wrapper.INSTANCE.getMinecraft().openScreen(new MultiplayerScreen(titleScreen));
            }
        }));

        this.customButtons.add(new MainMenuButton("Realms", 2, y + spacingY * 2, 150, 20, new ButtonListener() {
            @Override
            public void invoke() {
                titleScreen.switchToRealms();
            }
        }));
        this.customButtons.add(new MainMenuButton("Options", 2, y + spacingY * 3, 125, 20, new ButtonListener() {
            @Override
            public void invoke() {
                Wrapper.INSTANCE.getMinecraft().openScreen(new OptionsScreen(titleScreen, Wrapper.INSTANCE.getOptions()));
            }
        }));
        this.customButtons.add(new MainMenuButton("Quit Game", 2, y + spacingY * 4, 100, 20, new ButtonListener() {
            @Override
            public void invoke() {
                Wrapper.INSTANCE.getMinecraft().scheduleStop();
            }
        }));
    }

    private void switchToRealms() {
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }
        if (customMainMenu.scroll && timer.hasPassed(customMainMenu.scrollDelay * 1000L)) {
            background++;
            if (background < 0) {
                background = backgrounds.size() - 1;
            } else if (background > backgrounds.size() - 1) {
                background = 0;
            }
            timer.reset();
        }
        isDonator = Addon.isDonator(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", ""));
        int midX = Render2DHelper.INSTANCE.getScaledWidth() / 2;
        float f = this.doBackgroundFade ? (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
        fill(matrices, 0, 0, this.width, this.height, -1);
        this.backgroundRenderer.render(delta, MathHelper.clamp(f, 0.0F, 1.0F));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.doBackgroundFade ? (float) MathHelper.ceil(MathHelper.clamp(f, 0.0F, 1.0F)) : 1.0F);
        drawTexture(matrices, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = MathHelper.ceil(g * 255.0F) << 24;

        if (!JexTitleScreen.backgrounds.isEmpty() && customMainMenu.customBackground) {
            Background currentBackground = backgrounds.get(background);
            Render2DHelper.INSTANCE.bindTexture(currentBackground.identifier);
            DrawableHelper.drawTexture(matrices, (int) 0, (int) 0, 0, 0, width, height, width, height);
        }

        if ((l & -67108864) != 0) {
            RenderSystem.setShaderTexture(0, JEX_TITLE_TEXTURE);
            int j1 = this.height / 4 - 10;
            Render2DHelper.INSTANCE.shaderColor(ColorHelper.INSTANCE.getClientColor());
            drawTexture(matrices, 2, (int) j1, 0.0F, 0.0F, 250, 50, 250, 50);

            this.splashText = isMinceraft ? "Minceraft" : "Build " + JexClient.INSTANCE.getVersion() + " for MC" + SharedConstants.getGameVersion().getName();
            matrices.push();
            float h = 1.8F - MathHelper.abs(MathHelper.sin((float)(Util.getMeasuringTimeMs() % 1000L) / 1000.0F * 6.2831855F) * 0.1F);
            h = h * 100.0F / (float)(this.textRenderer.getWidth(this.splashText) + 32);
            matrices.scale(h, h, h);
            FontHelper.INSTANCE.drawWithShadow(matrices, splashText, 2 / h, (j1 + 44) / h, ColorHelper.INSTANCE.getClientColor());
            matrices.pop();


            if (UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED || UpdateManager.INSTANCE.getStatus() == UpdateManager.Status.OUTDATED_BOTH) {
                String updateString = "Jex Client is outdated. You can open the Jex Options screen in Options to update to Build " + UpdateManager.INSTANCE.getLatestVersion();
                float strWidth = FontHelper.INSTANCE.getStringWidth(updateString);
                Render2DHelper.INSTANCE.fillAndBorder(matrices, (midX) - (strWidth / 2) - 2, -1, (midX) + (strWidth / 2) + 2, 15, ColorHelper.INSTANCE.getClientColor(), 0x80000000, 1);
                FontHelper.INSTANCE.drawCenteredString(matrices, updateString, midX, 2, ColorHelper.INSTANCE.getClientColor());
            }

            if (customMainMenu.customBackground) {
                if (backgrounds.isEmpty()) {
                    String backgroundString = "You don't have any backgrounds yet.";
                    FontHelper.INSTANCE.drawWithShadow(matrices, backgroundString, width - FontHelper.INSTANCE.getStringWidth(backgroundString) - 2, height - 30, -1);
                } else {
                    String backgroundString = "Background: (" + (background + 1) + "/" + backgrounds.size() + ")";
                    FontHelper.INSTANCE.drawWithShadow(matrices, backgroundString, width - FontHelper.INSTANCE.getStringWidth(backgroundString) - 2, height - 30, -1);
                }
            }


            Iterator<?> var12 = this.children().iterator();

            while (var12.hasNext()) {
                ClickableWidget abstractButtonWidget = (ClickableWidget) var12.next();
                abstractButtonWidget.setAlpha(g);
            }
            float top = this.height / 4 + 45;
            float bottom = top + (24 * 5) + 2;
            float left = -1;
            float right = 205;

            Render2DHelper.INSTANCE.drawFace(matrices, 2, (int)bottom + 2, 4, MCAPIHelper.INSTANCE.getPlayerSkin(Wrapper.INSTANCE.getMinecraft().getSession().getProfile().getId()));
            FontHelper.INSTANCE.drawWithShadow(matrices, "\2477Welcome, " + (isDonator ? "\247r" : (Addon.isLinkedToAccount(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", "")) ? "\247a" : "\247f")) + Wrapper.INSTANCE.getMinecraft().getSession().getUsername(), 37, bottom + 2, ColorHelper.INSTANCE.getRainbowColor());
            if (Addon.isLinkedToAccount(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", ""))) {
                FontHelper.INSTANCE.drawWithShadow(matrices, "\2477Jex Utility Client", 37, bottom + 12, -1);
                Addon.AddonResponse response = Addon.getResponse(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", ""));
                try {
                    if (response.getCape() != null && !response.getCape().isEmpty() && !response.getCape().equalsIgnoreCase("null")) {
                        Render2DHelper.INSTANCE.bindTexture(new Identifier("jex", "capes/" + Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", "")));
                    } else {
                        Render2DHelper.INSTANCE.bindTexture(new Identifier("jex", "cape/jex_cape.png"));
                    }
                    DrawableHelper.drawTexture(matrices, 2, (int) bottom + 35, 2.5f, 4, 32, 64, 198, 128);
                }catch (Exception e) {}
            } else {
                FontHelper.INSTANCE.drawWithShadow(matrices, "\2477Account not linked. You can link your account for a free Jex Cape", 37, bottom + 12, -1);
                FontHelper.INSTANCE.drawWithShadow(matrices, "\2477Also join the Discord!", 37, bottom + 22, -1);
            }
            Render2DHelper.INSTANCE.fillAndBorder(matrices, left, top, right, bottom, ColorHelper.INSTANCE.getClientColor(), 0x40000000, 1);

            this.customButtons.forEach(button -> {
                button.draw(matrices);
            });
            super.render(matrices, mouseX, mouseY, delta);

        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.customButtons.forEach(button1 -> {
            button1.click(mouseX, mouseY, button);
        });
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return false;
    }

    public void removed() {
        if (this.realmsNotificationGui != null) {
            this.realmsNotificationGui.removed();
        }

    }

    public boolean backgroundExists(String name) {
        for (Background background : JexTitleScreen.backgrounds) {
            if (background.name.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public void loadBackgrounds() throws IOException {
        File backgroundsFolder = new File(ModFileHelper.INSTANCE.getJexDirectory(), "backgrounds");
        if (!backgroundsFolder.exists()) {
            backgroundsFolder.mkdir();
            return;
        }
        for (File file : backgroundsFolder.listFiles()) {
            if (backgroundExists(file.getName().replaceAll("-", "").replaceAll(" ", "").toLowerCase()))
                continue;
            if (!file.isDirectory()) {
                byte[] fileContent = FileUtils.readFileToByteArray(file);
                String encodedString = Base64.encodeBase64String(fileContent);
                try {
                    parseImage(encodedString, file.getName().replaceAll("-", "").replaceAll(" ", "").toLowerCase());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void parseImage(String image, String name) {
        NativeImage image1 = readTexture(image);
        int imageWidth = image1.getWidth();
        int imageHeight = image1.getHeight();

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < image1.getWidth(); x++) {
            for (int y = 0; y < image1.getHeight(); y++) {
                imgNew.setPixelColor(x, y, image1.getPixelColor(x, y));
            }
        }

        image1.close();
        Identifier id = new Identifier("jex", "background/" + name);
        applyTexture(id, imgNew);
        JexTitleScreen.backgrounds.add(new Background(name, imageWidth, imageHeight, id));
    }

    private NativeImage readTexture(String textureBase64) {
        try {
            byte[] imgBytes = Base64.decodeBase64(textureBase64);
            ByteArrayInputStream bias = new ByteArrayInputStream(imgBytes);
            return NativeImage.read(bias);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    private void applyTexture(Identifier identifier, NativeImage nativeImage) {
        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(nativeImage)));
    }


    public class Background {
        private String name;
        private int width, height;
        private Identifier identifier;

        public Background(String name, int width, int height, Identifier identifier) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public class MainMenuButton extends Button {

        public MainMenuButton(String name, float x, float y, float width, float height, ButtonListener listener) {
            super(null, name, x, y, width, height, listener);
        }

        @Override
        public void draw(MatrixStack matrixStack) {
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + this.getWidth(), getY() + this.getHeight(), 0x80000000);
            FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getName(), this.getX() + (this.getWidth() / 2), this.getY() + (this.getHeight() / 2) - 4, isEnabled() ? -1 : 0xff676767);
            if (isHovered() && this.isEnabled())
                Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x35ffffff);
            this.getChildren().forEach(button -> {
                button.draw(matrixStack);
            });
        }
    }
}
