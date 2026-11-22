package com.theuninvited;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod(TheUninvited.MODID)
public class TheUninvited {
    public static final String MODID = "theuninvited";
    private final Random random = new Random();
    private final Map<UUID, Haunting> hauntings = new HashMap<>();
    private int ticks = 0;

    public TheUninvited() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static class Haunting {
        int nextEvent;
        boolean joined;
        UUID stalker;
        Haunting(int nextEvent) { this.nextEvent = nextEvent; }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;
        ticks++;
        if (ticks % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) continue;
            Haunting h = hauntings.computeIfAbsent(player.getUUID(), id -> new Haunting(45 + random.nextInt(100)));
            if (--h.nextEvent > 0) continue;
            h.nextEvent = 35 + random.nextInt(115);
            ServerLevel level = player.serverLevel();
            if (!h.joined) {
                h.joined = true;
                server.getPlayerList().broadcastSystemMessage(Component.literal("Unknown joined the game").withStyle(ChatFormatting.YELLOW), false);
                level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.8f, 0.7f);
                continue;
            }
            int eventType = random.nextInt(5);
            if (eventType == 0) {
                player.sendSystemMessage(Component.literal("<Unknown> I can see you.").withStyle(ChatFormatting.DARK_GRAY));
            } else if (eventType == 1) {
                level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.7f, 0.45f);
            } else if (eventType == 2) {
                level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1f, 0.6f);
            } else {
                spawnStalker(player, level, eventType == 4);
            }
        }
        hauntings.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
    }

    private void spawnStalker(ServerPlayer player, ServerLevel level, boolean scare) {
        Vec3 look = player.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        double distance = scare ? 3.5 : 15 + random.nextInt(13);
        Vec3 pos = player.position().add(look.scale(scare ? -distance : distance)).add(side.scale(random.nextInt(13) - 6));
        BlockPos base = BlockPos.containing(pos);
        BlockPos target = null;
        for (int dy = 6; dy >= -9; dy--) {
            BlockPos feet = base.offset(0, dy, 0);
            if (level.getBlockState(feet.below()).isSolidRender() && level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()) {
                target = feet; break;
            }
        }
        if (target == null) return;
        EnderMan entity = EntityType.ENDERMAN.create(level);
        if (entity == null) return;
        entity.setCustomName(Component.literal("Unknown"));
        entity.setCustomNameVisible(false);
        entity.setPersistenceRequired();
        entity.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, random.nextFloat() * 360, 0);
        level.addFreshEntity(entity);
        level.playSound(null, target, SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.8f, 0.5f);
        if (scare) {
            player.sendSystemMessage(Component.literal("<Unknown> DON'T TURN AROUND").withStyle(ChatFormatting.DARK_RED));
            level.playSound(null, player.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1f, 0.5f);
        }
    }
}
