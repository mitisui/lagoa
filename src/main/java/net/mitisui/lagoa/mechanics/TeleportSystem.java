package net.mitisui.lagoa.mechanics;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportSystem {

    private static final Map<UUID, TeleportData> ACTIVE_TELEPORTS = new HashMap<>();

    // Sons customizados (use seus próprios no resourcepack)
    private static final SoundEvent SOUND_PHASE_1 = SoundEvent.createVariableRangeEvent(
            new ResourceLocation("minecraft", "teleport_1")
    );
    private static final SoundEvent SOUND_PHASE_2 = SoundEvent.createVariableRangeEvent(
            new ResourceLocation("minecraft", "teleport_2")
    );
    private static final SoundEvent SOUND_PHASE_3 = SoundEvent.createVariableRangeEvent(
            new ResourceLocation("minecraft", "teleport_3")
    );

    private static DustParticleOptions getParticle(TeleportData data, float scale) {
        return new DustParticleOptions(data.particleColor, scale);
    }

    // Cores padrão (RGB 0-1)
    private static final Map<String, Vector3f> COLOR_MAP = new HashMap<>();
    static {
        COLOR_MAP.put("branco", new Vector3f(1.0f, 1.0f, 1.0f));
        COLOR_MAP.put("vermelho", new Vector3f(1.0f, 0.0f, 0.0f));
        COLOR_MAP.put("verde", new Vector3f(0.0f, 1.0f, 0.0f));
        COLOR_MAP.put("azul", new Vector3f(0.0f, 0.0f, 1.0f));
        COLOR_MAP.put("amarelo", new Vector3f(1.0f, 1.0f, 0.0f));
        COLOR_MAP.put("roxo", new Vector3f(0.8f, 0.0f, 1.0f));
        COLOR_MAP.put("rosa", new Vector3f(1.0f, 0.4f, 0.8f));
        COLOR_MAP.put("laranja", new Vector3f(1.0f, 0.6f, 0.0f));
        COLOR_MAP.put("ciano", new Vector3f(0.0f, 1.0f, 1.0f));
        COLOR_MAP.put("preto", new Vector3f(0.1f, 0.1f, 0.1f));
    }

    public static void startTeleporte(ServerPlayer target, Vec3 destination, String color) {
        if (ACTIVE_TELEPORTS.containsKey(target.getUUID())) {
            return;
        }

        Vector3f particleColor = COLOR_MAP.getOrDefault(color.toLowerCase(), COLOR_MAP.get("branco"));

        TeleportData data = new TeleportData(
                target,
                target.position(),
                destination,
                particleColor
        );

        ACTIVE_TELEPORTS.put(target.getUUID(), data);
        startFase1(data);
    }

    private static void startFase1(TeleportData data) {
        data.phase = 1;
        data.phaseTick = 0;

        ServerLevel level = (ServerLevel) data.target.level();
        Vec3 pos = data.target.position();
        level.playSound(null, pos.x, pos.y, pos.z, SOUND_PHASE_1, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void startFase2(TeleportData data) {
        data.phase = 2;
        data.phaseTick = 0;

        ServerLevel level = (ServerLevel) data.target.level();
        Vec3 startPos = data.target.position();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SOUND_PHASE_2, SoundSource.PLAYERS, 1.5f, 1.0f);

        data.startY = startPos.y;
        data.target.setNoGravity(true);
        data.target.setInvulnerable(true);
        data.target.noPhysics = true;

        data.wasFlying = data.target.getAbilities().flying;
        if (!data.wasFlying && data.target.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            data.target.getAbilities().mayfly = true;
            data.target.getAbilities().flying = true;
            data.target.onUpdateAbilities();
        }
    }

    private static void startFase3(TeleportData data) {
        data.phase = 3;
        data.phaseTick = 0;

        ServerLevel level = (ServerLevel) data.target.level();

        double finalY = data.destination.y;
        data.target.teleportTo(data.destination.x, finalY + 20, data.destination.z);

        data.startY = finalY + 20;
        data.target.noPhysics = true;
    }

    public static void tick() {
        ACTIVE_TELEPORTS.values().removeIf(data -> {
            if (!data.target.isAlive() || data.target.isRemoved()) {
                cleanup(data);
                return true;
            }

            data.phaseTick++;

            switch (data.phase) {
                case 1 -> {
                    tickFase1(data);
                    if (data.phaseTick >= 60) {
                        startFase2(data);
                    }
                }
                case 2 -> {
                    tickFase2(data);
                    if (data.phaseTick >= 40) {
                        startFase3(data);
                    }
                }
                case 3 -> {
                    tickFase3(data);
                    if (data.target.onGround() || data.phaseTick >= 100) {
                        terminaTeleporte(data);
                        return true;
                    }
                }
            }

            return false;
        });
    }

    private static void tickFase1(TeleportData data) {
        ServerLevel level = (ServerLevel) data.target.level();
        Vec3 pos = data.target.position();

        double progress = data.phaseTick / 60.0;
        int particleCount = (int) (progress * 10);

        for (int i = 0; i < particleCount; i++) {
            double angle = (data.phaseTick * 0.3 + i * 30) * Math.PI / 180.0;
            double radius = 0.5 + progress * 1.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = i * 0.1;

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0, 0, 0, 0.0
            );
        }

        // Rastro vertical
        if (data.phaseTick % 2 == 0) {
            for (int y = 0; y < 3; y++) {
                level.sendParticles(
                        ParticleTypes.END_ROD,
                        pos.x, pos.y + y * 0.5, pos.z,
                        2, 0.2, 0, 0.2, 0.0
                );
            }
        }
    }

    private static void tickFase2(TeleportData data) {
        ServerLevel level = (ServerLevel) data.target.level();
        Vec3 currentPos = data.target.position();

        double liftSpeed = 1.5;
        data.target.setDeltaMovement(0, liftSpeed, 0);
        data.target.hurtMarked = true;

        data.target.noPhysics = true;

        DustParticleOptions dust = getParticle(data, 1.2f);
        for (int i = 0; i < 5; i++) {
            double angle = (i * 72 + data.phaseTick * 20) * Math.PI / 180.0;
            level.sendParticles(dust, currentPos.x + Math.cos(angle), currentPos.y, currentPos.z + Math.sin(angle), 1, 0, 0, 0, 0.0);
        }
    }

    private static void tickFase3(TeleportData data) {
        ServerLevel level = (ServerLevel) data.target.level();
        Vec3 pos = data.target.position();

        // Descer suavemente
        double targetY = data.destination.y;
        if (pos.y > targetY + 0.5) {
            data.target.setDeltaMovement(0, -0.5, 0);
            data.target.hurtMarked = true;
        } else {
            data.target.setDeltaMovement(0, 0, 0);
        }

        DustParticleOptions dustOptions = new DustParticleOptions(data.particleColor, 1.5f);

        int circleCount = 16;
        double circleRadius = 2.0;

        for (int i = 0; i < circleCount; i++) {
            double angle = (i * (360.0 / circleCount) + data.phaseTick * 5) * Math.PI / 180.0;
            double offsetX = Math.cos(angle) * circleRadius;
            double offsetZ = Math.sin(angle) * circleRadius;

            level.sendParticles(
                    dustOptions,
                    pos.x + offsetX, targetY + 0.1, pos.z + offsetZ,
                    1, 0, 0, 0, 0.0
            );

            double spiralY = (data.phaseTick % 20) * 0.2;
            level.sendParticles(
                    dustOptions,
                    pos.x + offsetX * 0.7, targetY + spiralY, pos.z + offsetZ * 0.7,
                    1, 0, 0, 0, 0.0
            );
        }

        if (data.phaseTick % 2 == 0) {
            for (int i = 0; i < 5; i++) {
                double randomAngle = Math.random() * 2 * Math.PI;
                double randomRadius = Math.random() * 2.5;
                level.sendParticles(
                        dustOptions,
                        pos.x + Math.cos(randomAngle) * randomRadius,
                        targetY + 0.1,
                        pos.z + Math.sin(randomAngle) * randomRadius,
                        1, 0, 0, 0, 0.0
                );
            }
        }
    }

    private static void terminaTeleporte(TeleportData data) {
        ServerLevel level = (ServerLevel) data.target.level();

        data.target.teleportTo(data.destination.x, data.destination.y, data.destination.z);

        level.playSound(null, data.destination.x, data.destination.y, data.destination.z,
                SOUND_PHASE_3, SoundSource.PLAYERS, 1.5f, 1.2f);

        DustParticleOptions dustOptions = new DustParticleOptions(data.particleColor, 2.0f);
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * 3;
            double offsetY = Math.random() * 2;

            level.sendParticles(
                    dustOptions,
                    data.destination.x + Math.cos(angle) * radius,
                    data.destination.y + offsetY,
                    data.destination.z + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0.0
            );
        }

        // Flash de luz
        level.sendParticles(
                ParticleTypes.FLASH,
                data.destination.x, data.destination.y + 1, data.destination.z,
                5, 0.5, 0.5, 0.5, 0.0
        );

        cleanup(data);
    }

    private static void cleanup(TeleportData data) {
        data.target.noPhysics = false;
        data.target.setInvulnerable(false);
        data.target.setNoGravity(false);

        if (!data.wasFlying && data.target.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            data.target.getAbilities().mayfly = false;
            data.target.getAbilities().flying = false;
            data.target.onUpdateAbilities();
        }

        data.target.setDeltaMovement(0, 0, 0);
    }

    public static boolean isPlayerTeleporting(UUID playerUUID) {
        return ACTIVE_TELEPORTS.containsKey(playerUUID);
    }

    public static void cancelTeleport(UUID playerUUID) {
        TeleportData data = ACTIVE_TELEPORTS.remove(playerUUID);
        if (data != null) {
            cleanup(data);
        }
    }

    private static class TeleportData {
        final ServerPlayer target;
        final Vec3 startPosition;
        final Vec3 destination;
        final Vector3f particleColor;

        int phase = 1;
        int phaseTick = 0;
        double startY = 0;
        boolean wasFlying = false;

        TeleportData(ServerPlayer target, Vec3 startPosition, Vec3 destination, Vector3f particleColor) {
            this.target = target;
            this.startPosition = startPosition;
            this.destination = destination;
            this.particleColor = particleColor;
        }
    }
}