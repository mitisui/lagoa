package net.mitisui.lagoa.mecanicas.pistola;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.mitisui.lagoa.Config;

import java.util.Objects;

public class PistolaEvents {

    // -------------------------------------------------------------------------
    // Utilitário NBT — 1.21.1
    // -------------------------------------------------------------------------

    private static CompoundTag readNbt(ItemStack item) {
        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static boolean isPistola(ItemStack item) {
        return readNbt(item).getBoolean("IsPistola");
    }

    // -------------------------------------------------------------------------
    // Evento de uso
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;
        if (!isPistola(event.getItemStack())) return;

        if (!Config.ENABLE_PISTOLA.get()) {
            event.getEntity().displayClientMessage(
                    Component.literal("§cPistola desabilitada no servidor!"), true);
            return;
        }

        event.setCanceled(true);
        disparar((ServerPlayer) event.getEntity());
    }

    // -------------------------------------------------------------------------
    // Lógica de disparo
    // -------------------------------------------------------------------------
    private static void disparar(ServerPlayer shooter) {
        ServerLevel level = shooter.serverLevel();

        level.playSound(null, shooter.blockPosition(),
                SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS, 0.5F, 1.5F);

        Vec3 eyePos  = shooter.getEyePosition();
        Vec3 lookVec = shooter.getLookAngle();
        Vec3 endPos  = eyePos.add(lookVec.scale(50.0));

        BlockHitResult blockHit = level.clip(new ClipContext(
                eyePos, endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                shooter
        ));

        AABB searchBox = shooter.getBoundingBox()
                .expandTowards(lookVec.scale(50.0))
                .inflate(1.0);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, shooter, eyePos, blockHit.getLocation(),
                searchBox, entity -> !entity.isSpectator() && entity.isPickable()
        );

        spawnBulletTrail(level, eyePos,
                entityHit != null ? entityHit.getLocation() : blockHit.getLocation());

        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            float damage = Config.SHOT_DAMAGE.get().floatValue();
            target.hurt(level.damageSources().playerAttack(shooter), damage);

            if (target.isAlive()) {
                String effectName = Config.BULLET_EFFECT.get();
                if (!effectName.isEmpty()) applyEffect(target, effectName);
            }

            level.sendParticles(ParticleTypes.FLASH,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    10, 0.2, 0.2, 0.2, 0.1);
        }

        shooter.getCooldowns().addCooldown(shooter.getMainHandItem().getItem(), 20);
    }

    private static void spawnBulletTrail(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3    direction = end.subtract(start).normalize();
        double  distance  = start.distanceTo(end);

        for (double i = 0; i < distance; i += 0.5) {
            Vec3 pos = start.add(direction.scale(i));
            level.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private static void applyEffect(LivingEntity target, String effectName) {
        MobEffect effect = Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT
                        .get(ResourceLocation.parse(effectName)));

        target.addEffect(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), 200, 0));
    }
}