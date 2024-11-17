package com.jodexindustries.donatecase.impl.actions;

import com.jodexindustries.donatecase.api.Case;
import com.jodexindustries.donatecase.api.data.action.ActionExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BroadcastActionExecutorImpl implements ActionExecutor<Player> {
    /**
     * Send broadcast message for all players on the server with specific cooldown<br>
     * {@code - "[broadcast] (message)"}
     *
     * @param context Broadcast message
     * @param cooldown Cooldown in seconds
     */
    @Override
    public void execute(@Nullable Player player, @NotNull String context, int cooldown) {
        Bukkit.getScheduler().runTaskLater(Case.getInstance(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(context);
            }
        }, 20L * cooldown);
    }
}
