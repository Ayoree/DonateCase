package com.jodexindustries.donatecase.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the player interacts with the case block on the mouse's right button
 */
public class CaseInteractEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    /**
     * true if you wish to cancel this event
     */
    protected boolean cancel;
    private final Block block;
    private final String caseType;
    private final Action action;

    /**
     * Default constructor
     *
     * @param who      Player who interact
     * @param block    Block to interact
     * @param caseType Case type
     * @param action   Interact action
     */
    public CaseInteractEvent(@NotNull final Player who, @NotNull final Block block, @NotNull final String caseType, @NotNull final Action action) {
        super(who);
        this.block = block;
        this.caseType = caseType;
        this.cancel = false;
        this.action = action;
    }

    /**
     * Can be only LEFT_CLICK_BLOCK and RIGHT_CLICK_BLOCK
     *
     * @return click block action
     */
    @NotNull
    public Action getAction() {
        return action;
    }

    /**
     * Get clicked block
     *
     * @return block
     */
    @NotNull
    public Block getClickedBlock() {
        return block;
    }

    /**
     * Get case type
     *
     * @return case type
     */
    @NotNull
    public String getCaseType() {
        return caseType;
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

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

}
