package com.jodexindustries.donatecase.api.addon.internal;

import com.jodexindustries.donatecase.api.DCAPIBukkit;
import com.jodexindustries.donatecase.api.addon.Addon;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class InternalJavaAddonBukkit extends InternalJavaAddon implements InternalAddonBukkit {
    private DCAPIBukkit api;

    @Override
    void init(InternalAddonDescription description, File file, InternalAddonClassLoader loader, Addon donateCase) {
        super.init(description, file, loader, donateCase);
        this.api = DCAPIBukkit.get(this);
    }

    @Override
    @NotNull
    public DCAPIBukkit getDCAPI() {
        return api;
    }
}
