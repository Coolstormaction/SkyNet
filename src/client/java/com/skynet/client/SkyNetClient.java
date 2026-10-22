
package com.skynet.client;

import net.fabricmc.api.ClientModInitializer;

public class SkyNetClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		SkyNetCommands.register();
	}
}
