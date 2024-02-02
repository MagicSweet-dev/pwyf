package com.magicsweet.pwyf.mixin;

import com.github.alexdlaird.ngrok.protocol.Tunnel;
import com.magicsweet.pwyf.Pwyf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {

	Tunnel tunnel;

	@Inject(
		method = "openToLan",
		at = @At("RETURN")
	)
	void openToLan(GameMode gameMode, boolean cheatsAllowed, int port, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			MinecraftClient.getInstance().player.sendMessage(Text.translatable("pwyf.opening"));
			Pwyf.get().startTunnel(port).thenAcceptAsync(tunnel -> {
				this.tunnel = tunnel;
				var ip = tunnel.getPublicUrl().replace("tcp://", "");
				Pwyf.get().sendMessage(Text.translatable(
					"pwyf.ready",
					Text.literal(ip)
						.setStyle(Style.EMPTY
							.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ip))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("pwyf.copy_ip")))
							.withColor(Formatting.GREEN)
						)
				));
			}).exceptionally(e -> {
				Pwyf.get().sendMessage(Text.translatable("pwyf.error.not_open", e.getMessage()).formatted(Formatting.RED));
				e.printStackTrace();
				return null;
			});
		}
	}

	@Inject(
		method = "shutdown",
		at = @At("RETURN")
	)
	void shutdown(CallbackInfo ci) {
		Pwyf.LOGGER.info("Shutting down tunnel");
		Pwyf.get().stopTunnel(tunnel);
		tunnel = null;
	}

}
