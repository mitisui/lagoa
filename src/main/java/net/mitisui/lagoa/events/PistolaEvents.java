package net.mitisui.lagoa.events;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.mitisui.lagoa.Config;
import net.mitisui.lagoa.Lagoa;

public class PistolaEvents {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        if (!item.hasTag() || !item.getTag().getBoolean("IsPistola")) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        if (!Config.ENABLE_PISTOLA.get()) {
            player.displayClientMessage(
                    Component.literal("§cPistola desabilitada no servidor!"),
                    true
            );
            return;
        }

        event.setCanceled(true);

        disparar((ServerPlayer) player);
    }

    private static void disparar(ServerPlayer shooter) {
        ServerLevel level = shooter.serverLevel();

        level.playSound(null, shooter.blockPosition(),
                SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS,
                0.5F, 1.5F);

        Vec3 eyePos = shooter.getEyePosition();
        Vec3 lookVec = shooter.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(50.0));

        BlockHitResult blockHit = level.clip(new ClipContext(
                eyePos, endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                shooter
        ));

        // Verifica colisão com entidades
        AABB searchBox = shooter.getBoundingBox()
                .expandTowards(lookVec.scale(50.0))
                .inflate(1.0);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, shooter, eyePos, blockHit.getLocation(),
                searchBox, entity -> !entity.isSpectator() && entity.isPickable()
        );

        // Efeitos visuais da trajetória
        spawnBulletTrail(level, eyePos,
                entityHit != null ? entityHit.getLocation() : blockHit.getLocation());

        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            float damage = Config.SHOT_DAMAGE.get().floatValue();
            target.hurt(level.damageSources().playerAttack(shooter), damage);
            if (target.isAlive()) {
                String effectName = Config.BULLET_EFFECT.get();
                if (!effectName.isEmpty()) {
                    applyEffect(target, effectName);
                }
            }

            shooter.displayClientMessage(
                    Component.literal(
                            "§cAcertou " + target.getName().getString() + "! §7(-" + damage + " HP)"
                    ), true
            );

            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    10, 0.2, 0.2, 0.2, 0.1
            );
        }

        shooter.getCooldowns().addCooldown(shooter.getMainHandItem().getItem(), 20);
    }

    private static void spawnBulletTrail(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        for (double i = 0; i < distance; i += 0.5) {
            Vec3 particlePos = start.add(direction.scale(i));
            level.sendParticles(ParticleTypes.CRIT,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0, 0, 0, 0
            );
        }
    }

    private static void applyEffect(LivingEntity target, String effectName) {
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(
                new net.minecraft.resources.ResourceLocation(effectName)
        );

        if (effect != null) {
            target.addEffect(new MobEffectInstance(effect, 200, 0));
        }
    }
}