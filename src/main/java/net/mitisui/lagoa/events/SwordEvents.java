package net.mitisui.lagoa.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mitisui.lagoa.Config;
import net.mitisui.lagoa.Lagoa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SwordEvents {

    private static final Map<BlockPos, BlockState> SHIELD_BLOCKS = new HashMap<>();
    private static final Map<BlockPos, Long> SHIELD_TIMERS = new HashMap<>();
    private static final long SHIELD_DURATION = 60;

    private static final Map<UUID, PrisonData> IMPRISONED_PLAYERS = new HashMap<>();

    // Som customizado para o beacon
    private static final SoundEvent BEACON_SOUND = SoundEvent.createVariableRangeEvent(
            new ResourceLocation("minecraft", "beacon_massive_attack")
    );

    // Classe para armazenar dados da prisão
    private static class PrisonData {
        Vec3 prisonCenter;
        long startTime;
        long endTime;
        int damageInterval;
        int damageCounter;

        PrisonData(Vec3 center, long start, long end) {
            this.prisonCenter = center;
            this.startTime = start;
            this.endTime = end;
            this.damageInterval = 10;
            this.damageCounter = 0;
        }
    }

    @SubscribeEvent
    public static void onSwordSpecial(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) return;

        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        if (item.hasCustomHoverName() && item.getHoverName().getString().contains("Divisor de Almas")) {
            EntityHitResult hit = getPlayerPOVHitResult(player, Config.E1_RAYCAST_RANGE.get());

            if (hit != null && hit.getEntity() instanceof LivingEntity target) {
                performSoulDivision(player, target, event.getLevel());
                player.getCooldowns().addCooldown(item.getItem(), Config.E1_HOMING_COOLDOWN.get());
            } else {
                performMassiveAttack(player, event.getLevel());
                player.getCooldowns().addCooldown(item.getItem(), Config.E1_AOE_COOLDOWN.get());
            }
        }
    }

    private static void performSoulDivision(Player attacker, LivingEntity target, Level level) {
        ServerLevel serverLevel = (ServerLevel) level;

        Vec3 attackerPos = attacker.position();
        Vec3 targetPos = target.position();
        Vec3 pullDirection = attackerPos.subtract(targetPos).normalize();
        double pullStrength = 2.5;

        target.setDeltaMovement(pullDirection.scale(pullStrength));
        target.hurtMarked = true;

        for (int i = 0; i < 20; i++) {
            double t = i / 20.0;
            Vec3 particlePos = targetPos.add(pullDirection.scale(t * targetPos.distanceTo(attackerPos)));
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    particlePos.x, particlePos.y + 1, particlePos.z,
                    2, 0.1, 0.1, 0.1, 0.02);
        }

        level.playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP,
                SoundSource.HOSTILE, 1.5F, 0.6F);

        level.getServer().execute(() -> {
            try {
                Thread.sleep(1000);
                level.getServer().execute(() -> imprisonTarget(target, level));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static void imprisonTarget(LivingEntity target, Level level) {
        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 prisonCenter = target.position();
        long currentTime = level.getGameTime();
        long duracaoPrisao = 100;

        // Registrar prisioneiro
        IMPRISONED_PLAYERS.put(target.getUUID(), new PrisonData(prisonCenter, currentTime, currentTime + duracaoPrisao));

        // Aplicar efeitos de controle
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int)duracaoPrisao, 10, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int)duracaoPrisao, 5, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false)); // 2 segundos de cegueira

        if (target instanceof Player) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int)duracaoPrisao, 5, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.UNLUCK, (int)duracaoPrisao, 5, false, false));

        }

        createSoulPrison(prisonCenter, serverLevel);

        level.playSound(null, target.blockPosition(), SoundEvents.WITHER_AMBIENT,
                SoundSource.HOSTILE, 2.0F, 0.5F);
        level.playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE, 1.5F, 0.8F);
    }

    private static void createSoulPrison(Vec3 center, ServerLevel level) {
        int radius = 2;

        for (double theta = 0; theta < Math.PI * 2; theta += 0.3) {
            for (double phi = 0; phi < Math.PI; phi += 0.3) {
                double x = center.x + radius * Math.sin(phi) * Math.cos(theta);
                double y = center.y + radius * Math.cos(phi);
                double z = center.z + radius * Math.sin(phi) * Math.sin(theta);

                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z,
                        1, 0, 0, 0, 0);
            }
        }

        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            for (double y = 0; y <= radius * 2; y += 0.3) {
                level.sendParticles(ParticleTypes.SMOKE,
                        x, center.y - radius + y, z,
                        1, 0, 0, 0, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide || event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;

        long currentTime = event.level.getGameTime();
        ServerLevel serverLevel = (ServerLevel) event.level;

        IMPRISONED_PLAYERS.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            PrisonData data = entry.getValue();

            LivingEntity prisoner = (LivingEntity) serverLevel.getEntity(playerId);
            if (prisoner == null) return true;

            if (currentTime >= data.endTime) {
                return true;
            }

            Vec3 currentPos = prisoner.position();
            double distance = currentPos.distanceTo(data.prisonCenter);
            if (distance > 0.5) {
                Vec3 pullBack = data.prisonCenter.subtract(currentPos).normalize().scale(0.3);
                prisoner.setDeltaMovement(pullBack);
                prisoner.hurtMarked = true;
            }

            data.damageCounter++;
            if (data.damageCounter >= data.damageInterval) {
                prisoner.hurt(event.level.damageSources().magic(), 2.0F);
                data.damageCounter = 0;

                // Efeito visual de dano
                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        prisoner.getX(), prisoner.getY() + 1, prisoner.getZ(),
                        5, 0.3, 0.5, 0.3, 0.0);
            }

            if (currentTime % 5 == 0) {
                createSoulPrison(data.prisonCenter, serverLevel);
            }

            return false;
        });

        SHIELD_TIMERS.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                BlockPos pos = entry.getKey();
                BlockState originalState = SHIELD_BLOCKS.get(pos);
                if (originalState != null) {
                    event.level.setBlock(pos, originalState, 3);
                    SHIELD_BLOCKS.remove(pos);
                }
                return true;
            }
            return false;
        });
    }

    private static void performMassiveAttack(Player player, Level level) {
        float dano = Config.E1_ATAQUE_AOE_DANO.get().floatValue();
        double areaRadius = Config.E1_ATAQUE_AOE_AREA.get();

        AABB area = new AABB(player.blockPosition()).inflate(areaRadius, 100, areaRadius);

        // Dano contínuo E efeitos
        level.getEntities(player, area).forEach(entity -> {
            if (entity instanceof Player target) {
                target.hurt(level.damageSources().magic(), dano);
                target.addEffect(new MobEffectInstance(
                        MobEffects.DARKNESS,
                        400,
                        4,
                        false,
                        true,
                        true
                ));

                target.addEffect(new MobEffectInstance(
                        MobEffects.UNLUCK,
                        1200,
                        0,
                        false,
                        true,
                        true
                ));

                ((ServerLevel)level).sendParticles(ParticleTypes.FLASH,
                        target.getX(), target.getY() + 1, target.getZ(),
                        5, 0.2, 0.2, 0.2, 0.0);
                ((ServerLevel)level).sendParticles(ParticleTypes.LARGE_SMOKE,
                        target.getX(), target.getY() + 1, target.getZ(),
                        10, 0.3, 0.5, 0.3, 0.02);
            }
        });

        BlockPos playerPos = player.blockPosition();
        replaceBlockWithBlackConcrete(level, playerPos.below());

        ServerLevel serverLevel = (ServerLevel) level;
        int worldHeight = level.getMaxBuildHeight();
        int bedrockLevel = level.getMinBuildHeight();

        for (int y = bedrockLevel; y <= worldHeight; y += 2) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), y, player.getZ(),
                    8, 0.1, 0.1, 0.1, 0.01);

            double scale = 1.5;
            for (int i = 0; i < 4; i++) {
                double angle = (Math.PI * i) / 2;
                double offsetX = Math.cos(angle) * scale;
                double offsetZ = Math.sin(angle) * scale;

                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        player.getX() + offsetX, y, player.getZ() + offsetZ,
                        2, 0.2, 0.2, 0.2, 0.02);
            }

            if (y % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), y, player.getZ(),
                        12, scale, 0.5, scale, 0.05);
            }
        }

        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double radius = 3.0;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;

            serverLevel.sendParticles(ParticleTypes.FLASH,
                    x, player.getY(), z,
                    1, 0, 0, 0, 0);
        }


        level.playSound(null, player.blockPosition(), BEACON_SOUND,
                SoundSource.PLAYERS, 3.0F, 0.8F);

        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN,
                SoundSource.HOSTILE, 2.0F, 0.5F);
    }

    private static void replaceBlockWithBlackConcrete(Level level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);
        Block currentBlock = currentState.getBlock();

        if (currentState.isAir() || currentBlock == Blocks.BEDROCK ||
                currentBlock == Blocks.BARRIER || currentBlock == Blocks.COMMAND_BLOCK) {
            return;
        }

        level.setBlock(pos, Blocks.BLACK_CONCRETE.defaultBlockState(), 3);
    }

    // Proteção contra projéteis com escudo circular
    @SubscribeEvent
    public static void onProjectileHit(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.hasCustomHoverName() ||
                !mainHand.getHoverName().getString().contains("Divisor de Almas")) {
            return;
        }

        // Verificar se é ataque de projétil
        if (event.getSource().getDirectEntity() instanceof Projectile projectile) {
            event.setCanceled(true);

            // Criar escudo circular de concreto
            createCircularShield(player, projectile);

            // Efeito visual
            ((ServerLevel)player.level()).sendParticles(ParticleTypes.ENCHANTED_HIT,
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);

            // Som de bloqueio
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.5F);

            // Remover projétil
            projectile.discard();
        }
    }

    private static void createCircularShield(Player player, Projectile projectile) {
        Level level = player.level();
        Vec3 projectilePos = projectile.position();
        Vec3 playerPos = player.position();
        Vec3 direction = projectilePos.subtract(playerPos).normalize();

        int shieldDistance = 5;
        BlockPos centerPos = player.blockPosition().offset(
                (int)(direction.x * shieldDistance),
                (int)(direction.y * shieldDistance),
                (int)(direction.z * shieldDistance)
        );

        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);

                    if (distance <= radius && distance >= radius - 0.5) {
                        BlockPos pos = centerPos.offset(x, y, z);
                        BlockState currentState = level.getBlockState(pos);

                        if (currentState.isAir() || currentState.getBlock() == Blocks.BLACK_CONCRETE) {
                            SHIELD_BLOCKS.put(pos, currentState);
                            SHIELD_TIMERS.put(pos, level.getGameTime() + SHIELD_DURATION);
                            level.setBlock(pos, Blocks.BLACK_CONCRETE.defaultBlockState(), 3);

                            ((ServerLevel)level).sendParticles(ParticleTypes.ENCHANT,
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    3, 0.2, 0.2, 0.2, 0.0);
                        }
                    }
                }
            }
        }
    }

    private static EntityHitResult getPlayerPOVHitResult(Player player, double reach) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(reach));

        AABB searchBox = new AABB(eyePos, endPos).inflate(2.0);

        EntityHitResult closestHit = null;
        double closestDistance = reach;

        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (entity == player) continue;

            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Vec3 hit = entityBox.clip(eyePos, endPos).orElse(null);

            if (hit != null) {
                double distance = eyePos.distanceTo(hit);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestHit = new EntityHitResult(entity, hit);
                }
            }
        }

        return closestHit;
    }
}