package com.jodexindustries.donatecase.command.impl;

import com.jodexindustries.donatecase.api.Case;
import com.jodexindustries.donatecase.api.data.database.DatabaseStatus;
import com.jodexindustries.donatecase.api.manager.SubCommandManager;
import com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit;
import com.jodexindustries.donatecase.api.data.subcommand.SubCommandType;
import com.jodexindustries.donatecase.api.data.subcommand.SubCommand;
import com.jodexindustries.donatecase.api.data.subcommand.SubCommandExecutor;
import com.jodexindustries.donatecase.api.data.subcommand.SubCommandTabCompleter;
import com.jodexindustries.donatecase.command.GlobalCommand;
import com.jodexindustries.donatecase.tools.Tools;
import com.jodexindustries.donatecase.tools.ToolsBukkit;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.jodexindustries.donatecase.DonateCase.instance;
import static com.jodexindustries.donatecase.command.GlobalCommand.resolveSDGCompletions;

/**
 * Class for /dc setkey subcommand implementation
 */
public class SetKeyCommand implements SubCommandExecutor<CommandSender>, SubCommandTabCompleter<CommandSender> {

    public static void register(SubCommandManager<CommandSender> manager) {
        SetKeyCommand command = new SetKeyCommand();

        SubCommand<CommandSender> subCommand = manager.builder("setkey")
                .executor(command)
                .tabCompleter(command)
                .permission(SubCommandType.MODER.permission)
                .build();
        manager.registerSubCommand(subCommand);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (args.length >= 3) {
            String player = args[0];
            String caseName = args[1];
            Player target = Bukkit.getPlayer(player);
            int keys;
            try {
                keys = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                Tools.msgRaw(sender, Tools.rt(Case.getConfig().getLang().getString("number-format-exception"), "%string:" + args[3]));
                return;
            }
            if (instance.api.getCaseManager().hasCaseByType(caseName)) {
                CaseDataBukkit data = instance.api.getCaseManager().getCase(caseName);
                if (data == null) return;
                Case.getInstance().api.getCaseKeyManager().setKeys(caseName, player, keys).thenAcceptAsync(status -> {
                    if(status == DatabaseStatus.COMPLETE) {
                        ToolsBukkit.msg(sender, Tools.rt(Case.getConfig().getLang().getString("keys-sets"),
                                "%player:" + player, "%key:" + keys,
                                "%casetitle:" + data.getCaseTitle(), "%casedisplayname:" + data.getCaseDisplayName(), "%case:" + caseName));

                        if (args.length < 4 || !args[3].equalsIgnoreCase("-s")) {
                            ToolsBukkit.msg(target, Tools.rt(Case.getConfig().getLang().getString("keys-sets-target"),
                                    "%player:" + player, "%key:" + keys,
                                    "%casetitle:" + data.getCaseTitle(), "%casedisplayname:" + data.getCaseDisplayName(), "%case:" + caseName));
                        }
                    }
                });
            } else {
                ToolsBukkit.msg(sender, Tools.rt(Case.getConfig().getLang().getString("case-does-not-exist"),
                        "%case:" + caseName));
            }
        } else {
            GlobalCommand.sendHelp(sender, label);
        }
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        return resolveSDGCompletions(args);
    }

}
