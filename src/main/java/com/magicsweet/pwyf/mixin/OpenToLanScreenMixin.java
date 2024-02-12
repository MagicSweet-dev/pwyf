package com.magicsweet.pwyf.mixin;

import com.magicsweet.pwyf.Pwyf;
import com.magicsweet.pwyf.screen.TunnelConfigScreen;
import com.magicsweet.pwyf.status.TunnelStatus;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public abstract class OpenToLanScreenMixin extends Screen {

	@Unique ButtonWidget tunnelButton;

	protected OpenToLanScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("HEAD"), method = "init")
	void init(CallbackInfo ci) {
		tunnelButton = ButtonWidget.builder(
			Text.translatable("pwyf.status", Pwyf.get().getTunnelStatus().toText()),
			button -> {
				client.setScreen(new TunnelConfigScreen(this));
			}
		).dimensions(this.width / 2 - 75, 190, 150, 20).build();

		this.addDrawableChild(tunnelButton);
	}
}
