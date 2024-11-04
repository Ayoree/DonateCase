package com.jodexindustries.dcblockanimations.utils;

import com.jodexindustries.dcblockanimations.bootstrap.Main;
import com.jodexindustries.dcblockanimations.config.Config;
import com.jodexindustries.donatecase.api.events.AnimationEndEvent;
import com.jodexindustries.donatecase.api.events.DonateCaseReloadEvent;
import com.jodexindustries.donatecase.api.events.OpenCaseEvent;
import org.bukkit.block.Block;
import org.bukkit.block.Lidded;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;


public class Tools implements Listener {
    private final Main main;
    private final Config config;
    private static final Map<Block, Lidded> openedBlocks = new HashMap<>();

    public Tools(Main main) {
        this.main = main;
        this.config = new Config(main);
    }

    public void load() {
        main.getPlugin().getServer().getPluginManager().registerEvents(this, main.getPlugin());
    }

    public void unload() {
        HandlerList.unregisterAll(this);
    }

    public void reloadConfig() {
        config.reloadConfig();
        main.getLogger().info("Config reloaded");
    }

    @EventHandler
    public void onCaseOpen(OpenCaseEvent e) {
        String caseType = e.getCaseType();
        if (!config.getConfig().getStringList("enabled-types").contains(caseType)) return;

        openBlock(e.getBlock());
    }

    @EventHandler
    public void onAnimationEnd(AnimationEndEvent e) {
        closeBlock(e.getLocation().getBlock());
    }

    @EventHandler
    public void onConfigReload(DonateCaseReloadEvent e) {
        if (e.getType() == DonateCaseReloadEvent.Type.CONFIG) reloadConfig();
    }

    private void openBlock(Block block) {
        if (block.getState() instanceof Lidded) {
            Lidded lidded = (Lidded) block.getState();
            lidded.open();
            openedBlocks.put(block, lidded);
        }
    }

    private void closeBlock(Block block) {
        Lidded openedBlock = openedBlocks.get(block);
        if (openedBlock == null) return;

        openedBlocks.remove(block);
        openedBlock.close();
    }
}