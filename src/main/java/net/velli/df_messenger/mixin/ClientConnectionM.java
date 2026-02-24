package net.velli.df_messenger.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.velli.df_messenger.MessageHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionM {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void DFMessenger$handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (MessageHandler.packet(packet)) {
            ci.cancel();
        }
    }
}
