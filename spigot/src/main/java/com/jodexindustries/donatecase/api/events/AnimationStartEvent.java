package com.jodexindustries.donatecase.api.events;

import com.jodexindustries.donatecase.api.data.CaseData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when the animation starts
 */
public class AnimationStartEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final CaseData caseData;
    private final Block block;
    private final String animation;
    private final CaseData.Item winItem;
    private final UUID uuid;

    /**
     * Default constructor
     *
     * @param who       Player who opened case
     * @param animation Animation name
     * @param caseData  Case data
     * @param block     Block where opened
     * @param winItem   Win item
     * @param uuid Animation UUID
     */
    public AnimationStartEvent(@NotNull Player who, @NotNull String animation, @NotNull CaseData caseData,
                               @NotNull Block block, @NotNull CaseData.Item winItem, @NotNull UUID uuid) {
        super(who);
        this.caseData = caseData;
        this.block = block;
        this.animation = animation;
        this.winItem = winItem;
        this.uuid = uuid;
    }

    /**
     * Get case location
     *
     * @return case location
     */
    @NotNull
    public Location getLocation() {
        return block.getLocation();
    }

    /**
     * Get case block
     *
     * @return case block
     * @since 2.2.5.8
     */
    @NotNull
    public Block getBlock() {
        return block;
    }

    /**
     * Get case data
     *
     * @return case data
     */
    @NotNull
    public CaseData getCaseData() {
        return caseData;
    }

    /**
     * Get case animation
     *
     * @return case animation
     */
    @NotNull
    public String getAnimation() {
        return animation;
    }

    /**
     * Get the win item
     *
     * @return win item
     */
    @NotNull
    public CaseData.Item getWinItem() {
        return winItem;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Get handlers
     *
     * @return handlers list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UUID getUniqueId() {
        return uuid;
    }
}
