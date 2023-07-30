package me.cometkaizo.curtain.module.enchant;

import me.cometkaizo.curtain.network.Packet;
import me.cometkaizo.curtain.util.ClientUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class S2CApplyShockwave implements Packet {

    public static final Logger LOGGER = LogManager.getLogger();
    public final int entityId;
    public final Vec3 hitPos;
    public final int shockwaveLevel;

    public S2CApplyShockwave(ThrownTrident trident, Vec3 hitPos, int shockwaveLevel) {
        this.entityId = trident.getId();
        this.hitPos = hitPos;
        this.shockwaveLevel = shockwaveLevel;
    }

    public S2CApplyShockwave(FriendlyByteBuf buffer) {
        entityId = buffer.readInt();
        hitPos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        shockwaveLevel = buffer.readInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeDouble(hitPos.x);
        buffer.writeDouble(hitPos.y);
        buffer.writeDouble(hitPos.z);
        buffer.writeInt(shockwaveLevel);
    }


    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            Level level = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientUtils::getClientLevel);
            if (level == null) return;

            Entity entity = level.getEntity(entityId);
            if (entity == null) LOGGER.error("Invalid action packet: no entity with id {}", entityId);
            if (!(entity instanceof ThrownTrident trident)) return;

            EnchantmentsModule.INSTANCE.shockwaveEnchantment.get().onTridentHit(trident, hitPos, shockwaveLevel);
        });
    }
}
