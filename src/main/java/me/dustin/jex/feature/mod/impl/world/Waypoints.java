package me.dustin.jex.feature.mod.impl.world;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

@Feature.Manifest(name = "Waypoints", category = Feature.Category.WORLD, description = "Display Waypoints to mark areas.")
public class Waypoints extends Feature {

	public static ArrayList<String> servers = new ArrayList<>();
	public static ArrayList<Waypoint> waypoints = new ArrayList<>();
	private static Map<Waypoint, Vec3d> waypointPositions = Maps.newHashMap();
	@Op(name = "FOV based Tag")
	public boolean fovBasedTag = true;
	@Op(name = "Distance", min = 20, max = 150)
	public int fovDistance = 35;
	@Op(name = "Distance on Tag")
	public boolean distance = true;
	@Op(name = "Last Death")
	public boolean lastDeath = true;
	int spin = 0;

	public static Waypoint get(String name, String server) {
		for (Waypoint waypoint : waypoints) {
			if (waypoint.getServer().equalsIgnoreCase(server) && waypoint.getName().equalsIgnoreCase(name))
				return waypoint;
		}
		return null;
	}

	public static ArrayList<Waypoint> getWaypoints(String server) {
		ArrayList<Waypoint> points = new ArrayList<>();
		for (Waypoint waypoint : waypoints) {
			if (waypoint.getServer().equalsIgnoreCase(server))
				points.add(waypoint);
		}
		return points;
	}

	public static ArrayList<String> getServers() {
		return servers;
	}

	@EventListener(events = { EventRender3D.class, EventRender3D.EventRender3DNoBob.class, EventRender3D.class, EventRender2D.class, EventTick.class })
	private void runMethod(Event event) {
		if (event instanceof EventTick) {
			spin++;
		}
		if (event instanceof EventRender3D eventRender3D) {
			String server = WorldHelper.INSTANCE.getCurrentServerName();
			if (!Wrapper.INSTANCE.getLocalPlayer().isAlive() && lastDeath) {
				Waypoint oldWaypoint = get("Last Death", server);
				if (oldWaypoint != null) {
					waypoints.remove(oldWaypoint);
				}
				waypoints.add(new Waypoint("Last Death", server, (float) Wrapper.INSTANCE.getLocalPlayer().getX(), (float) Wrapper.INSTANCE.getLocalPlayer().getY(), (float) Wrapper.INSTANCE.getLocalPlayer().getZ(), WorldHelper.INSTANCE.getDimensionID().toString(), ColorHelper.INSTANCE.getColorViaHue(0).getRGB()));
			}
			waypointPositions.clear();
			for (Waypoint waypoint : getWaypoints(server)) {
				if (waypoint.getDimension().equalsIgnoreCase(WorldHelper.INSTANCE.getDimensionID().toString())) {
					if (waypoint.hidden || !waypoint.drawBeacon)
						continue;
					float x = waypoint.getX();
					float y = waypoint.getY();
					float z = waypoint.getZ();
					float distance = ClientMathHelper.INSTANCE.getDistance2D(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(x, y, z));
					Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(x, waypoint.getY(), z));
					if (distance < 270) {
						Box box = new Box(renderPos.x - 0.2f, renderPos.y, renderPos.z - 0.2f, renderPos.x + 0.2f, (256 - waypoint.y), renderPos.z + 0.2f);
						Render3DHelper.INSTANCE.drawBox(((EventRender3D) event).getMatrixStack(), box, waypoint.getColor());
					} else {
						float yaw = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(x, y, z)).getYaw();
						x = (float) Wrapper.INSTANCE.getLocalPlayer().getX() + 250 * (float) Math.cos(Math.toRadians(yaw + 90));
						z = (float) Wrapper.INSTANCE.getLocalPlayer().getZ() + 250 * (float) Math.sin(Math.toRadians(yaw + 90));
					}
					Vec3d screenPos = Render2DHelper.INSTANCE.to2D(new Vec3d(x, waypoint.getY() + Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(EntityPose.STANDING), z), eventRender3D.getMatrixStack());
					waypointPositions.put(waypoint, screenPos);
				}
			}
		} else if (event instanceof EventRender3D.EventRender3DNoBob) {
			String server = WorldHelper.INSTANCE.getCurrentServerName();
			for (Waypoint waypoint : getWaypoints(server)) {
				if (waypoint.hidden || !waypoint.drawTracer)
					continue;
				if (waypoint.isHidden())
					continue;
				float x = waypoint.getX();
				float y = waypoint.getY();
				float z = waypoint.getZ();
				float distance = ClientMathHelper.INSTANCE.getDistance2D(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(x, y, z));
				if (distance > 270) {
					float yaw = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(x, y, z)).getYaw();
					x = (float) Wrapper.INSTANCE.getLocalPlayer().getX() + 250 * (float) Math.cos(Math.toRadians(yaw + 90));
					z = (float) Wrapper.INSTANCE.getLocalPlayer().getZ() + 250 * (float) Math.sin(Math.toRadians(yaw + 90));
				}
				Vec3d pos = new Vec3d(x, y, z);
				Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
				assert cameraEntity != null;
				Vec3d entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f));

				Color color1 = ColorHelper.INSTANCE.getColor(waypoint.getColor());

				Render3DHelper.INSTANCE.setup3DRender(true);
				RenderSystem.lineWidth(1.2f);
				RenderSystem.setShader(GameRenderer::getPositionColorShader);

				Vec3d eyes = new Vec3d(0, 0, 1).rotateX(-(float) Math.toRadians(PlayerHelper.INSTANCE.getPitch())).rotateY(-(float) Math.toRadians(PlayerHelper.INSTANCE.getYaw()));

				BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
				bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
				bufferBuilder.vertex(eyes.x, eyes.y, eyes.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
				bufferBuilder.vertex(entityPos.x, entityPos.y, entityPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
				bufferBuilder.end();
				BufferRenderer.draw(bufferBuilder);

				Render3DHelper.INSTANCE.end3DRender();
			}
		} else if (event instanceof EventRender2D) {
			waypointPositions.keySet().forEach(waypoint -> {
				if (waypoint.hidden || !waypoint.drawNametag)
					return;
				Vec3d renderPos = waypointPositions.get(waypoint);
				if (shouldRender(renderPos)) {
					String name = waypoint.getName();
					if (this.distance) {
						name = String.format("%s [%.1f]", waypoint.getName(), ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(waypoint.getX(), waypoint.getY(), waypoint.getZ())));
					}
					float x = (float) renderPos.x;
					float y = (float) renderPos.y;
					float crosshairFOV = ClientMathHelper.INSTANCE.getDistance2D(new Vec2f(x, y), new Vec2f(Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f));
					if (fovBasedTag && crosshairFOV > fovDistance)
						name = "[]";
					float width = FontHelper.INSTANCE.getStringWidth(name);
					Render2DHelper.INSTANCE.fill(((EventRender2D) event).getMatrixStack(), x - (width / 2) - 2, y - 11, x + (width / 2.f) + 2, y, 0x50000000);
					FontHelper.INSTANCE.drawCenteredString(((EventRender2D) event).getMatrixStack(), name, x, y - 9, waypoint.color);
				}
			});
		}
	}

	public boolean shouldRender(Vec3d pos) {
		return pos != null && (pos.getZ() > -1 && pos.getZ() < 1);
	}

	public static class Waypoint {
		private String name;
		private String server;
		private String dimension;
		private float x, y, z;
		private int color;

		private boolean hidden = false;
		private boolean drawNametag = true;
		private boolean drawBeacon = true;
		private boolean drawTracer = false;

		public Waypoint(String name, String server, float x, float y, float z, String dimension, int color) {
			if (servers.contains(server))
				servers.add(server);
			this.name = name;
			this.server = server;
			this.x = x;
			this.y = y;
			this.z = z;
			this.dimension = dimension;
			this.color = color;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		public String getDimension() {
			return dimension;
		}

		public void setDimension(String dimension) {
			this.dimension = dimension;
		}

		public float getX() {
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getY() {
			return y;
		}

		public void setY(float y) {
			this.y = y;
		}

		public float getZ() {
			return z;
		}

		public void setZ(float z) {
			this.z = z;
		}

		public int getColor() {
			return color;
		}

		public void setColor(int color) {
			this.color = color;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public boolean isDrawNametag() {
			return drawNametag;
		}

		public void setDrawNametag(boolean drawNametag) {
			this.drawNametag = drawNametag;
		}

		public boolean isDrawBeacon() {
			return drawBeacon;
		}

		public void setDrawBeacon(boolean drawBeacon) {
			this.drawBeacon = drawBeacon;
		}

		public boolean isDrawTracer() {
			return drawTracer;
		}

		public void setDrawTracer(boolean drawTracer) {
			this.drawTracer = drawTracer;
		}
	}
}
