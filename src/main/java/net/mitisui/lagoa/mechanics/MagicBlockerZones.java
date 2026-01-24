package net.mitisui.lagoa.mechanics;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "lagoa")
public class MagicBlockerZones {

    private static final Map<String, ZoneData> BLOCKED_ZONES = new HashMap<>();

    public static class ZoneData {
        public String zoneName;
        public BlockPos pos1;
        public BlockPos pos2;
        public String dimension;

        public ZoneData(String zoneName, BlockPos pos1, BlockPos pos2, String dimension) {
            this.zoneName = zoneName;
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.dimension = dimension;
        }

        public boolean isInZone(BlockPos pos, String playerDimension) {
            if (!this.dimension.equals(playerDimension)) {
                return false;
            }

            int minX = Math.min(pos1.getX(), pos2.getX());
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());

            return pos.getX() >= minX && pos.getX() <= maxX &&
                    pos.getY() >= minY && pos.getY() <= maxY &&
                    pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }
    }

    @SubscribeEvent
    public static void onSpellPreCast(SpellPreCastEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof ServerPlayer player && !player.level().isClientSide) {
            BlockPos playerPos = player.blockPosition();
            String playerDimension = player.level().dimension().location().toString();

            // Verifica se o player está em alguma zona bloqueada
            for (ZoneData zone : BLOCKED_ZONES.values()) {
                if (zone.isInZone(playerPos, playerDimension)) {
                    event.setCanceled(true);

                    MagicData magicData = MagicData.getPlayerMagicData(player);
                    if (magicData.getMana() > 0) {
                        magicData.setMana(0.0f);
                    }

                    player.displayClientMessage(
                            Component.literal("§cMagias estão bloqueadas nesta área: " + zone.zoneName),
                            true
                    );
                    return;
                }
            }
        }
    }

    public static void addZone(String zoneId, String zoneName, BlockPos pos1, BlockPos pos2, String dimension) {
        BLOCKED_ZONES.put(zoneId, new ZoneData(zoneName, pos1, pos2, dimension));
    }

    public static boolean removeZone(String zoneId) {
        return BLOCKED_ZONES.remove(zoneId) != null;
    }

    public static ZoneData getZone(String zoneId) {
        return BLOCKED_ZONES.get(zoneId);
    }

    public static Map<String, ZoneData> getAllZones() {
        return new HashMap<>(BLOCKED_ZONES);
    }

    public static void clearAllZones() {
        BLOCKED_ZONES.clear();
    }
}