package com.magicsweet.pwyf;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.installer.NgrokVersion;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import com.magicsweet.pwyf.status.TunnelStatus;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.NetworkUtils;
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
import java.util.concurrent.ThreadLocalRandom;

public class Pwyf implements ModInitializer {

	@Getter static Pwyf instance;
	public static final Logger LOGGER = LoggerFactory.getLogger("pwyf");
	// to leave tunnels issued my this minecraft instances untouched
	public static final int UID = ThreadLocalRandom.current().nextInt();

	@Getter NgrokClient ngrokClient;
	@Getter TunnelStatus tunnelStatus = TunnelStatus.NOT_INSTALLED;

	@Override
	public void onInitialize() {
		instance = this;
		ngrokClient = new NgrokClient.Builder().build();

		if (!getNgrokClient().getJavaNgrokConfig().getConfigPath().toFile().exists()) {
			tunnelStatus = TunnelStatus.NOT_INSTALLED;
		} else {
			var token =  getNgrokToken();
			if (token == null || token.isBlank()) {
				tunnelStatus = TunnelStatus.NOT_CONFIGURED;
			} else {
				tunnelStatus = TunnelStatus.READY;
			}
		}
	}

	public String getNgrokToken() {
		var config = getNgrokClient().getNgrokProcess().getNgrokInstaller().getNgrokConfig(getNgrokClient().getJavaNgrokConfig().getConfigPath());
		return (String) config.get("authtoken");
	}

	public void setNgrokToken(String token) {
		getNgrokClient().setAuthToken(token);
		ngrokClient = new NgrokClient.Builder().build();
	}

	public CompletableFuture<Tunnel> startTunnel(int port) {
		return CompletableFuture.supplyAsync(() -> {
			disconnectServerTunnels();
			var tunnel = ngrokClient.connect(
				new CreateTunnel.Builder()
					.withName("pwyf-server" + UID)
					.withProto(Proto.TCP)
					.withAddr(port)
					.build()
			);
			this.tunnelStatus = TunnelStatus.RUNNING;
			return tunnel;
		});
	}

	private void disconnectTestTunnels() {
		// sometimes it just doesnt disconnect them. idk y
		for (var tunnel: ngrokClient.getTunnels()) {
			if (tunnel.getName().equals("pwyf-test" + UID)) {
				ngrokClient.disconnect(tunnel.getPublicUrl());
			}
		}
	}

	private void disconnectServerTunnels() {
		// sometimes it just doesnt disconnect them. idk y
		for (var tunnel: ngrokClient.getTunnels()) {
			if (tunnel.getName().equals("pwyf-server" + UID)) {
				ngrokClient.disconnect(tunnel.getPublicUrl());
			}
		}
	}


	public void stopTunnel(Tunnel tunnel) {
		this.tunnelStatus = TunnelStatus.READY;
		ngrokClient.disconnect(tunnel.getPublicUrl());
	}

	public CompletableFuture<Boolean> testTunnelConnection() {
		int port = NetworkUtils.findLocalPort();
		return CompletableFuture.supplyAsync(() -> {
			try {
				disconnectTestTunnels();
				var tunnel = ngrokClient.connect(
					new CreateTunnel.Builder()
						.withName("pwyf-test" + UID)
						.withProto(Proto.TCP)
						.withAddr(port)
						.build()
				);
				ngrokClient.disconnect(tunnel.getPublicUrl());
				this.tunnelStatus = TunnelStatus.READY;
				return true;
			} catch (Exception e) {
				// this is a wierd shit i don't wanna deal with now
				if (e.getMessage().contains("account is limited to 1 simultaneous ngrok agent session")) {
					this.tunnelStatus = TunnelStatus.READY;
					return true;
				}
				e.printStackTrace();
				this.tunnelStatus = TunnelStatus.NOT_CONFIGURED;
				return false;
			}
		});
	}

	public void sendMessage(Text text) {
		var player = MinecraftClient.getInstance().player;
		if (player != null) player.sendMessage(text);
	}

	public static Pwyf get() {
		return getInstance();
	}
}