package com.jodexindustries.donatecase.api.addon.internal;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class InternalAddonLogger extends Logger {
    private final String addonName;

    /**
     * Creates a new PluginLogger that extracts the name from a plugin.
     *
     * @param context A reference to the plugin
     */
    public InternalAddonLogger(@NotNull InternalAddon context) {
        super(context.getDonateCase().getName(), null);
        addonName = "[" + context.getName() + "] ";
        setParent(context.getDonateCase().getLogger());
        setLevel(Level.ALL);
    }

    @Override
    public void log(@NotNull LogRecord logRecord) {
        logRecord.setMessage(addonName + logRecord.getMessage());
        super.log(logRecord);
    }

}
