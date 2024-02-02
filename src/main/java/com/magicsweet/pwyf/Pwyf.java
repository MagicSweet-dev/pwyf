package com.magicsweet.pwyf;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.installer.NgrokVersion;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class Pwyf implements ModInitializer {
	@Getter static Pwyf instance;
    public static final Logger LOGGER = LoggerFactory.getLogger("pwyf");

	@Getter NgrokClient ngrokClient;

	@Override
	public void onInitialize() {
		instance = this;
		ngrokClient = new NgrokClient.Builder().build();

		if (!getNgrokClient().getJavaNgrokConfig().getConfigPath().toFile().exists()) {
			openNgrokInstaller();
		} else {
			var config = getNgrokClient().getNgrokProcess().getNgrokInstaller().getNgrokConfig(getNgrokClient().getJavaNgrokConfig().getConfigPath());
			var token = (String) config.get("authtoken");
			if (token == null || token.isBlank()) openNgrokInstaller();
		}

	}

	public CompletableFuture<Tunnel> startTunnel(int port) {
		return CompletableFuture.supplyAsync(() -> ngrokClient.connect(
			new CreateTunnel.Builder()
				.withProto(Proto.TCP)
				.withAddr(port)
				.build()
		));
	}

	public void stopTunnel(Tunnel tunnel) {
		ngrokClient.disconnect(tunnel.getPublicUrl());
	}

	public void sendMessage(Text text) {
		var player = MinecraftClient.getInstance().player;
		if (player != null) player.sendMessage(text);
	}

	private void openNgrokInstaller() {
		Pwyf.LOGGER.info("Opening ngrok install prompt");
		System.setProperty("java.awt.headless", "false");

		var text = String.join("<br/>",
			"Ngrok is not installed",
			"Please sign up at ngrok.com and enter your auth token below",
			"",
			"After you sign up, click 'Add Tunnel Authtoken', then 'Save' and 'Copy & Close'",
			"Then paste the thing you just copied to the box below and hit 'OK'",
			"",
			"(click on this message to open ngrok.com)"
		);
		var hyperlink = getLabel(text);
		var answer = JOptionPane.showInputDialog(hyperlink);
		if (answer != null) {
			ngrokClient.setAuthToken(answer);
		}
	}

	@NotNull
	private static JLabel getLabel(String text) {
		var hyperlink = new JLabel("<html>" + text + "</html>");

		hyperlink.setCursor(new Cursor(Cursor.HAND_CURSOR));

		hyperlink.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("https://dashboard.ngrok.com/tunnels/authtokens"));
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hyperlink.setText("<html>" + text + "</html>");
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				hyperlink.setText("<html><a href=''>" + text + "</a></html>");
			}
		});
		return hyperlink;
	}

	public static Pwyf get() {
		return getInstance();
	}
}