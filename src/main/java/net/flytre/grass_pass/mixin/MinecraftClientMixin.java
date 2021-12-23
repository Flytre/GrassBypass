package net.flytre.grass_pass.mixin;

import net.flytre.Config;
import net.flytre.flytre_lib.api.config.ConfigRegistry;
import net.flytre.flytre_lib.api.config.reference.block.ConfigBlock;
import net.flytre.flytre_lib.api.config.reference.entity.ConfigEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;


@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {


    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Shadow
    @Nullable
    public ClientWorld world;

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;

    @SuppressWarnings("SameParameterValue")
    @Nullable
    private static EntityHitResult rayTraceEntity(PlayerEntity player, float partialTicks, double blockReachDistance) {
        Vec3d from = player.getCameraPosVec(partialTicks); //eye height
        Vec3d look = player.getRotationVec(partialTicks); //looking
        Vec3d to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

        return ProjectileUtil.getEntityCollision(
                player.world,
                player,
                from,
                to,
                new Box(from, to),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR
                        .and(e -> e != null
                                && e.collides()
                                && e instanceof LivingEntity
                                && !(ConfigEntity.contains(Config.HANDLER.getConfig().blacklistedEntities, e.getType(), player.world))
                                && !mountedEntities(player).contains(e)));
    }

    private static List<Entity> mountedEntities(PlayerEntity player) {
        List<Entity> ridingEntities = new ArrayList<>();
        Entity entity = player;
        while (entity != null && entity.hasPassengers()) {
            entity = entity.getPrimaryPassenger();
            ridingEntities.add(entity);
        }
        entity = player;
        while (entity != null && entity.hasVehicle()) {
            entity = entity.getVehicle();
            ridingEntities.add(entity);
        }
        return ridingEntities;
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;"))
    public void grass_bypass$registerConfig(RunArgs args, CallbackInfo ci) {
        ConfigRegistry.registerClientConfig(Config.HANDLER);
    }

    @Inject(method = "doAttack", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"))
    public void grass_bypass$bypass(CallbackInfo ci) {
        BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;
        if (blockHitResult == null)
            return;
        BlockPos blockPos = blockHitResult.getBlockPos();
        assert world != null;
        BlockState state = world.getBlockState(blockPos);

        if (ConfigBlock.contains(Config.HANDLER.getConfig().blacklistedBlocks, state.getBlock(), world))
            return;

        if (!state.getCollisionShape(world, blockPos).isEmpty())
            return;

        if (player == null)
            return;

        EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, 4.5D);

        if (rayTraceResult != null) {
            assert this.interactionManager != null;
            this.interactionManager.attackEntity(player, rayTraceResult.getEntity());
            ci.cancel();
        }
    }
}
