package com.magicsweet.pwyf.screen;

import com.magicsweet.pwyf.Pwyf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class TunnelConfigScreen extends Screen {

	@Nullable Screen parent;

	TextFieldWidget tokenField;
	ButtonWidget cancelButton;
	ButtonWidget saveButton;

	PressableTextWidget clickableText;

	public TunnelConfigScreen(@Nullable Screen parent) {
		super(Text.empty());
		this.parent = parent;
	}

	public TunnelConfigScreen() {
		this(null);
	}

	@Override
	protected void init() {
		// field
		this.tokenField = new TextFieldWidget(
			this.textRenderer,
			this.width / 2 - 150, 140,
			300, 20,
			Text.translatable("pwyf.token")
		);
		// buttons
		this.cancelButton = ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
			client.setScreen(this.parent);
		}).dimensions(this.width / 2 + 5, 190, 150, 20).build();
		this.saveButton = ButtonWidget.builder(ScreenTexts.DONE, button -> {
			button.active = false;
			button.setMessage(Text.translatable("pwyf.testing"));
			// set token, then test connection
			Pwyf.get().setNgrokToken(tokenField.getText());
			Pwyf.get().testTunnelConnection().thenAcceptAsync(result -> {
				if (result) {
					MinecraftClient.getInstance().execute(() -> {
						MinecraftClient.getInstance().getToastManager().add(
							new SystemToast(
								SystemToast.Type.PERIODIC_NOTIFICATION,
								Text.translatable("pwyf.success"),
								Text.translatable("pwyf.success.message")
							)
						);
						client.setScreen(this.parent);
					});
				} else {
					MinecraftClient.getInstance().execute(() -> {
						MinecraftClient.getInstance().getToastManager().add(
							new SystemToast(
								SystemToast.Type.PACK_LOAD_FAILURE,
								Text.translatable("pwyf.failed"),
								Text.translatable("pwyf.failed.message")
							)
						);
						button.active = true;
						button.setMessage(ScreenTexts.DONE);
					});

				}
			});
		}).dimensions(this.width / 2 - 155, 190, 150, 20).build();
		this.clickableText = new PressableTextWidget(
			(this.width - textRenderer.getWidth(Text.translatable("pwyf.token_hint.click"))) / 2, 100, textRenderer.getWidth(Text.translatable("pwyf.token_hint.click")), 10,
			Text.translatable("pwyf.token_hint.click"), button -> {
				ConfirmLinkScreen.open(this, "https://dashboard.ngrok.com/tunnels/authtokens");
			}, this.textRenderer
		);

		// settings
		this.tokenField.setMaxLength(49);

		// defaults
		this.tokenField.setText(Pwyf.get().getNgrokToken());

		// listeners
		this.tokenField.setChangedListener(string -> {
			if (string.length() == 49) {
				this.tokenField.setEditableColor(0xFFFFFF);
				this.tokenField.setTooltip(null);
				this.saveButton.active = true;
			} else {
				this.tokenField.setEditableColor(0xE0E0E0);
				this.tokenField.setTooltip(Tooltip.of(Text.translatable("pwyf.token_length", string.length())));
				this.saveButton.active = false;
			}
		});


		// registration
		this.addDrawableChild(tokenField);
		this.addDrawableChild(cancelButton);
		this.addDrawableChild(saveButton);
		this.addDrawableChild(clickableText);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		// field title
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("pwyf.token"), this.width / 2, 125, 0xFFFFFF);
		// hints
		context.drawCenteredTextWithShadow(this.textRenderer,
			Text.translatable("pwyf.token_hint.1"),
			this.width / 2, 50, 0xFFFFFF
		);
		context.drawCenteredTextWithShadow(this.textRenderer,
			Text.translatable("pwyf.token_hint.2"),
			this.width / 2, 70, 0xFFFFFF
		);
		context.drawCenteredTextWithShadow(this.textRenderer,
			Text.translatable("pwyf.token_hint.3"),
			this.width / 2, 80, 0xFFFFFF
		);
	}
}
