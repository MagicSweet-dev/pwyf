package com.magicsweet.pwyf.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@NoArgsConstructor @AllArgsConstructor @Getter
public enum TunnelStatus {
	NOT_INSTALLED(Formatting.RED),
	NOT_CONFIGURED(Formatting.YELLOW),
	TESTING(Formatting.AQUA),
	TEST_FAILED(Formatting.RED),
	READY(Formatting.GREEN),
	RUNNING(Formatting.DARK_PURPLE);

	Formatting formatting = Formatting.AQUA;

	public Text getText() {
		return Text.translatable("pwyf.status." + name().toLowerCase());
	}

	public Text toText() {
		return getText().copy().formatted(formatting);
	}
}
