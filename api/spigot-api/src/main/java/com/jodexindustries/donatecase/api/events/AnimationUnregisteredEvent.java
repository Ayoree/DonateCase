package com.jodexindustries.donatecase.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the animation is unregistered in DonateCase
 */
public class AnimationUnregisteredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String animationName;

    /**
     * Default constructor
     *
     * @param animationName Unregistered animation name
     */
    public AnimationUnregisteredEvent(String animationName) {
        this.animationName = animationName;
    }

    /**
     * Get animation name
     *
     * @return animation name
     */
    public String getAnimationName() {
        return animationName;
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
}
