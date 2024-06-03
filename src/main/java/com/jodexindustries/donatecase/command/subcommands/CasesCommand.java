package com.jodexindustries.donatecase.command.subcommands;

import com.jodexindustries.donatecase.api.Case;
import com.jodexindustries.donatecase.api.data.CaseData;
import com.jodexindustries.donatecase.api.data.SubCommand;
import com.jodexindustries.donatecase.api.data.SubCommandType;
import com.jodexindustries.donatecase.tools.Tools;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.jodexindustries.donatecase.DonateCase.casesConfig;
import static com.jodexindustries.donatecase.DonateCase.customConfig;

public class CasesCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        int num = 0;
        for (String caseName : casesConfig.getCases().keySet()) {
            num++;
            CaseData data = Case.getCase(caseName);
            if(data == null) return;
            String caseTitle = data.getCaseTitle();
            String caseDisplayName = data.getCaseDisplayName();

            Tools.msg_(sender, Tools.rt(customConfig.getLang().getString("CasesList"), "%casename:" + caseName, "%num:" + num, "%casedisplayname:" + caseDisplayName, "%casetitle:" + caseTitle ));
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public SubCommandType getType() {
        return SubCommandType.MODER;
    }
}