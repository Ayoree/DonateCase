package com.jodexindustries.donatecase.tools;

import com.jodexindustries.donatecase.api.armorstand.ArmorStandEulerAngle;
import com.jodexindustries.donatecase.api.data.casedata.CaseDataItem;
import com.jodexindustries.donatecase.api.data.casedata.CaseDataMaterialBukkit;
import com.jodexindustries.donatecase.api.data.subcommand.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Tools {


    public static void msgRaw(CommandSender s, String msg) {
        if (s != null) {
            s.sendMessage(rc(msg));
        }
    }

    public static String rc(@Nullable String string) {
        if(string != null) string = hex(string);
        return string;
    }

    public static String rt(String text, String... repl) {
        if (text != null) {
            for (String s : repl) {
                if (s != null) {
                    int l = s.split(":")[0].length();
                    text = text.replace(s.substring(0, l), s.substring(l + 1));
                }
            }
        }
        return text;
    }

    public static List<String> rt(List<String> text, String... repl) {
        ArrayList<String> rt = new ArrayList<>();

        for (String t : text) {
            rt.add(rt(t, repl));
        }

        return rt;
    }


    public static List<String> rc(List<String> t) {
        ArrayList<String> a = new ArrayList<>();

        for (String s : t) {
            a.add(rc(s));
        }

        return a;
    }

    public static String getLocalPlaceholder(String string) {
        Pattern pattern = Pattern.compile("%(.*?)%");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            int startIndex = string.indexOf("%") + 1;
            int endIndex = string.lastIndexOf("%");
            return string.substring(startIndex, endIndex);
        } else {
            return "null";
        }
    }

    public static Color parseColor(String s) {
        Color color = fromRGBString(s, null);
        if(color == null) color = getColor(s);
        return color;
    }

    public static Color getColor(String color) {
        Field[] fields = Color.class.getFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())
                    && field.getType() == Color.class) {

                if (field.getName().equalsIgnoreCase(color)) {
                    try {
                        return (Color) field.get(null);
                    } catch (IllegalArgumentException | IllegalAccessException e1) {
                        throw new RuntimeException(e1);
                    }
                }

            }
        }
        return null;
    }

    @NotNull
    public static ItemStack createItem(String id) {
        ItemStack item = new ItemStack(Material.AIR);
        if (id == null) return item;

        String[] materialParts = id.split(":");

        Material ma = Material.getMaterial(materialParts[0]);

        byte data = (materialParts.length > 1) ? Byte.parseByte(materialParts[1]) : -1;

        if (ma == null) return item;
        if (data == -1) {
            item = new ItemStack(ma, 1);
        } else if (Bukkit.getVersion().contains("1.12.2")) {
            item = new ItemStack(ma, 1, (short) 1, data);
        } else {
            item = new ItemStack(ma, 1);
        }
        return item;
    }

    /**
     * Format string with Bukkit ChatColor and hex
     * @param message String, to be formated
     * @return String with format
     */
    public static String hex(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Parse version from string
     * @param version String, to be parsed
     * @return numbered version.
     * <br>
     * Example: <br>
     * Input text: <code>2.2.2</code> <br>
     * Output: <code>2220</code> <br>
     * Input text: <code>2.2.2.2</code> <br>
     * Output: <code>2222</code>
     */
    public static int getPluginVersion(String version) {
        StringBuilder builder = new StringBuilder();
        version = version.replaceAll("\\.", "");
        if(version.length() < 4) {
            for (int i = 0; i < 4 - version.length(); i++) builder.append(version).append("0");
        } else {
            builder.append(version);
        }
        return Integer.parseInt(builder.toString());
    }

    public static boolean isHasCommandForSender(CommandSender sender, Map<String, List<Map<String, SubCommand<CommandSender>>>> addonsMap, String addon) {
        List<Map<String, SubCommand<CommandSender>>> commands = addonsMap.get(addon);
        return isHasCommandForSender(sender, commands);
    }

    public static boolean isHasCommandForSender(CommandSender sender, Map<String, List<Map<String, SubCommand<CommandSender>>>> addonsMap) {
        return addonsMap.keySet().stream().map(addonsMap::get).anyMatch(commands -> isHasCommandForSender(sender, commands));
    }

    /**
     * Check sender for permission to executing commands
     * Checks only if sender has permission for one or more commands, not all
     * @param sender Player or Console
     * @param commands List of commands, that loaded in DonateCase
     * @return true, if sender has permission
     */
    public static boolean isHasCommandForSender(CommandSender sender, List<Map<String, SubCommand<CommandSender>>> commands) {
        return commands.stream().flatMap(command -> command.values().stream()).map(SubCommand::getPermission).anyMatch(permission -> permission == null || sender.hasPermission(permission));
    }

    public static String[] parseRGB(String string) {
        if(string == null) return null;
        return string.replaceAll(" ", "").split(",");
    }

    public static Color fromRGBString(String[] rgb, Color def) {
        if(rgb.length >= 3) {
            try {
                int red = Integer.parseInt(rgb[0]);
                int green = Integer.parseInt(rgb[1]);
                int blue = Integer.parseInt(rgb[2]);
                def = Color.fromRGB(red, green, blue);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    public static Color fromRGBString(String string, Color def) {
        if(string != null) def = fromRGBString(parseRGB(string), def);
        return def;
    }

    /**
     * Parse EulerAngle from string
     * @param angleString String to be parsed
     * @return Alright, its just default Bukkit EulerAngle
     */
    public static EulerAngle getEulerAngleFromString(String angleString) {
        String[] angle;
        if (angleString == null) return new EulerAngle(0,0,0);
        angle = angleString.replaceAll(" ", "").split(",");
        try {
            double x = Double.parseDouble(angle[0]);
            double y = Double.parseDouble(angle[1]);
            double z = Double.parseDouble(angle[2]);
            return new EulerAngle(x, y, z);
        } catch (NumberFormatException ignored) {
            return new EulerAngle(0,0,0);
        }
    }

    /**
     * Get euler angle from Animations.yml
     * @param section The section where the settings are located
     * @return EulerAngle, that used in animations
     */
    public static ArmorStandEulerAngle getArmorStandEulerAngle(ConfigurationSection section) {
         if(section == null) {
             EulerAngle angle = new EulerAngle(0,0,0);
             return new ArmorStandEulerAngle(angle, angle, angle, angle, angle, angle);
         }
         EulerAngle head = getEulerAngleFromString(section.getString("Head"));
         EulerAngle body = getEulerAngleFromString(section.getString("Body"));
         EulerAngle rightArm = getEulerAngleFromString(section.getString("RightArm"));
         EulerAngle leftArm = getEulerAngleFromString(section.getString("LeftArm"));
         EulerAngle rightLeg = getEulerAngleFromString(section.getString("RightLeg"));
         EulerAngle leftLeg = getEulerAngleFromString(section.getString("LeftLeg"));
         return new ArmorStandEulerAngle(head,body,rightArm, leftArm, rightLeg,leftLeg);
    }

    /**
     * Sort case items by index
     * @param items Map with Case items
     * @return New map with sorted items
     */
    public static Map<String, CaseDataItem<CaseDataMaterialBukkit, ItemStack>> sortItemsByIndex(Map<String, CaseDataItem<CaseDataMaterialBukkit, ItemStack>> items) {
        return items.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(CaseDataItem::getIndex)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Extract cooldown from action string
     * @param action Action string. Format [cooldown:int]
     * @return cooldown
     */
    public static int extractCooldown(String action) {
        Pattern pattern = Pattern.compile("\\[cooldown:(.*?)]");
        Matcher matcher = pattern.matcher(action);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

}
