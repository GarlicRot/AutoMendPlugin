package com.garlicrot.automend;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class AutoMendPlugin extends Plugin {

    @Override
    public void onLoad() {
        AutoMendModule module = new AutoMendModule();
        RusherHackAPI.getModuleManager().registerFeature(module);
        logger.info("Loaded AutoMendPlugin");
    }

    @Override
    public void onUnload() {
        logger.info("Unloaded AutoMendPlugin");
    }
}
