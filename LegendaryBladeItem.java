package com.legendaryblades.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Dasar semua senjata legendaris: klik kanan = skill 1, Shift + klik kanan = skill 2.
 * Subclass cukup mengisi skill1() dan skill2() (dijalankan hanya di server).
 */
public abstract class LegendaryBladeItem extends Item {

    protected final int skill1Cooldown;
    protected final int skill2Cooldown;

    protected LegendaryBladeItem(Item.Properties properties, int skill1Cooldown, int skill2Cooldown) {
        super(properties);
        this.skill1Cooldown = skill1Cooldown;
        this.skill2Cooldown = skill2Cooldown;
    }

    protected abstract boolean skill1(ServerLevel level, ServerPlayer player);

    protected abstract boolean skill2(ServerLevel level, ServerPlayer player);

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer sp) {
            boolean second = sp.isShiftKeyDown();
            boolean ok = second ? skill2(serverLevel, sp) : skill1(serverLevel, sp);
            if (!ok) {
                return InteractionResult.FAIL;
            }
            sp.getCooldowns().addCooldown(stack, second ? skill2Cooldown : skill1Cooldown);
            stack.hurtAndBreak(2, sp,
                    hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }

        player.swing(hand, true);
        return InteractionResult.SUCCESS;
    }
}
