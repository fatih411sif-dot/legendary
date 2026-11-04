package com.legendaryblades.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Yagura no Tachi
 *  Skill 1 (klik kanan)       : Crimson Wave  - gelombang tebasan jarak jauh.
 *  Skill 2 (Shift+klik kanan) : Shadow Step   - melesat ke depan, menebas semua musuh di jalur.
 */
public class YaguraNoTachiItem extends LegendaryBladeItem {

    // ---- Angka yang bisa kamu atur ----
    private static final float WAVE_DAMAGE = 12.0f;
    private static final double WAVE_RANGE = 14.0;
    private static final double WAVE_RADIUS = 1.4;
    private static final int WAVE_COOLDOWN = 50;      // ticks (20 tick = 1 detik)

    private static final float DASH_DAMAGE = 10.0f;
    private static final double DASH_DISTANCE = 9.0;
    private static final double DASH_RADIUS = 1.6;
    private static final int DASH_COOLDOWN = 120;

    private static final DustParticleOptions CRIMSON = new DustParticleOptions(0xBF0D40, 1.6f);
    private static final DustParticleOptions VIOLET = new DustParticleOptions(0x731AB3, 1.4f);

    public YaguraNoTachiItem(Item.Properties properties) {
        super(properties, WAVE_COOLDOWN, DASH_COOLDOWN);
    }

    // ------------------------------------------------------------------
    // Skill 1: Crimson Wave
    // ------------------------------------------------------------------
    @Override
    protected boolean skill1(ServerLevel level, ServerPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 dir = player.getLookAngle().normalize();
        Vec3 end = start.add(dir.scale(WAVE_RANGE));

        // Berhenti di dinding
        HitResult hit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.MISS) {
            end = hit.getLocation();
        }

        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.3f, 0.6f);
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS, 0.4f, 1.8f);

        double length = end.distanceTo(start);
        Set<LivingEntity> hitSet = new HashSet<>();
        Vec3 right = dir.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1.0E-4) right = new Vec3(1, 0, 0);
        right = right.normalize();
        Vec3 up = right.cross(dir).normalize();

        for (double d = 1.0; d <= length; d += 0.5) {
            Vec3 p = start.add(dir.scale(d));

            // Bentuk bulan sabit tegak: busur lebar yang melengkung ke depan
            for (int i = -6; i <= 6; i++) {
                double t = i / 6.0;
                Vec3 q = p.add(up.scale(t * 1.4)).add(dir.scale(-Math.abs(t) * 0.6));
                level.sendParticles(CRIMSON, q.x, q.y, q.z, 1, 0.03, 0.03, 0.03, 0);
            }
            level.sendParticles(VIOLET, p.x, p.y, p.z, 2, 0.15, 0.4, 0.15, 0);
            if (((int) (d * 2)) % 4 == 0) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 2, 0.2, 0.3, 0.2, 0.01);
            }

            AABB box = new AABB(p, p).inflate(WAVE_RADIUS);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive() && !e.isSpectator());
            for (LivingEntity target : targets) {
                if (hitSet.add(target)) {
                    target.hurtServer(level, level.damageSources().playerAttack(player), WAVE_DAMAGE);
                    target.knockback(0.6, -dir.x, -dir.z);
                    level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5), target.getZ(),
                            12, 0.3, 0.4, 0.3, 0.2);
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Skill 2: Shadow Step
    // ------------------------------------------------------------------
    @Override
    protected boolean skill2(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        // Dash dominan horizontal supaya tidak melempar pemain ke langit
        Vec3 dir = new Vec3(look.x, Math.max(-0.3, Math.min(0.3, look.y)), look.z).normalize();

        Vec3 origin = player.position();
        Vec3 current = origin;
        AABB base = player.getBoundingBox();

        // Maju per 0.5 blok, berhenti kalau tertahan blok
        for (double d = 0.5; d <= DASH_DISTANCE; d += 0.5) {
            Vec3 next = origin.add(dir.scale(d));
            AABB moved = base.move(next.subtract(origin));
            if (!level.noCollision(player, moved)) break;
            current = next;
        }

        if (current.distanceToSqr(origin) < 1.0) {
            return false; // terhalang, jangan buang cooldown
        }

        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0f, 1.4f);

        Set<LivingEntity> hitSet = new HashSet<>();
        double length = current.distanceTo(origin);
        for (double d = 0; d <= length; d += 0.4) {
            Vec3 p = origin.add(dir.scale(d)).add(0, player.getBbHeight() * 0.5, 0);
            level.sendParticles(VIOLET, p.x, p.y, p.z, 4, 0.25, 0.5, 0.25, 0);
            level.sendParticles(CRIMSON, p.x, p.y, p.z, 2, 0.2, 0.4, 0.2, 0);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.1, 0.3, 0.1, 0.01);

            AABB box = new AABB(p, p).inflate(DASH_RADIUS);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive() && !e.isSpectator());
            for (LivingEntity target : targets) {
                if (hitSet.add(target)) {
                    target.hurtServer(level, level.damageSources().playerAttack(player), DASH_DAMAGE);
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.5),
                            target.getZ(), 1, 0, 0, 0, 0);
                }
            }
        }

        player.connection.teleport(current.x, current.y, current.z, player.getYRot(), player.getXRot());
        player.resetFallDistance();
        // Tebasan penutup di titik akhir
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, current.x, current.y + 1, current.z, 3, 0.5, 0.3, 0.5, 0);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.2f, 0.8f);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.legendary_blades.yagura_no_tachi.skill1")
                .withStyle(ChatFormatting.RED));
        tooltip.accept(Component.translatable("tooltip.legendary_blades.yagura_no_tachi.skill2")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
