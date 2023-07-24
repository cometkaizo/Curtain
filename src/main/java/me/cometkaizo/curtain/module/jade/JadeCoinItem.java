package me.cometkaizo.curtain.module.jade;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class JadeCoinItem extends Item {
    public JadeCoinItem() {
        this(new Properties().stacksTo(1));
    }
    public JadeCoinItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            Player player = context.getPlayer();
            if (player != null) {
                player.sendSystemMessage(Component.literal(player.getName().getString() + " used a Jade Coin item"));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
