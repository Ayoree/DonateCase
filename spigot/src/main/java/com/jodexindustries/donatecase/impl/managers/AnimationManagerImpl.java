package com.jodexindustries.donatecase.impl.managers;

import com.jodexindustries.donatecase.api.Case;
import com.jodexindustries.donatecase.api.addon.Addon;
import com.jodexindustries.donatecase.api.data.*;
import com.jodexindustries.donatecase.api.data.animation.CaseAnimation;
import com.jodexindustries.donatecase.api.data.animation.JavaAnimationBukkit;
import com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit;
import com.jodexindustries.donatecase.api.data.casedata.CaseDataHistory;
import com.jodexindustries.donatecase.api.data.casedata.CaseDataItem;
import com.jodexindustries.donatecase.api.data.casedata.CaseDataMaterialBukkit;
import com.jodexindustries.donatecase.api.events.*;
import com.jodexindustries.donatecase.api.gui.CaseGui;
import com.jodexindustries.donatecase.api.manager.AnimationManager;
import com.jodexindustries.donatecase.api.tools.ProbabilityCollection;
import com.jodexindustries.donatecase.tools.Tools;
import com.jodexindustries.donatecase.tools.ToolsBukkit;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static com.jodexindustries.donatecase.DonateCase.instance;

/**
 * Animation control class, registration, playing
 */
public class AnimationManagerImpl implements AnimationManager<JavaAnimationBukkit, CaseDataMaterialBukkit,
        ItemStack, Player, Location, Block, CaseDataBukkit> {
    /**
     * Map of registered animations
     */
    public final static Map<String, CaseAnimation<JavaAnimationBukkit, CaseDataMaterialBukkit, ItemStack>> registeredAnimations = new HashMap<>();

    /**
     * Map of active cases
     */
    public final static Map<UUID, ActiveCase<Block>> activeCases = new HashMap<>();

    /**
     * Active cases, but by location
     */
    public final static Map<Block, UUID> activeCasesByBlock = new HashMap<>();

    private final Addon addon;

    /**
     * Default constructor
     *
     * @param addon An addon that will manage animations
     */
    public AnimationManagerImpl(@NotNull Addon addon) {
        this.addon = addon;
    }

    @NotNull
    @Override
    public CaseAnimation.Builder<JavaAnimationBukkit, CaseDataMaterialBukkit, ItemStack> builder(String name) {
        return new CaseAnimation.Builder<>(name, addon);
    }

    @Override
    public boolean registerAnimation(CaseAnimation<JavaAnimationBukkit, CaseDataMaterialBukkit, ItemStack> caseAnimation) {
        String name = caseAnimation.getName();
        if(!isRegistered(name)) {
            registeredAnimations.put(name, caseAnimation);
            AnimationRegisteredEvent animationRegisteredEvent = new AnimationRegisteredEvent(caseAnimation);
            Bukkit.getServer().getPluginManager().callEvent(animationRegisteredEvent);
            return true;
        } else {
            addon.getLogger().warning("Animation " + name + " already registered!");
        }
        return false;
    }

    @Override
    public void unregisterAnimation(String name) {
        if (isRegistered(name)) {
            registeredAnimations.remove(name);
            AnimationUnregisteredEvent animationUnRegisteredEvent = new AnimationUnregisteredEvent(name);
            Bukkit.getServer().getPluginManager().callEvent(animationUnRegisteredEvent);
        } else {
            addon.getLogger().warning("Animation with name " + name + " already unregistered!");
        }
    }

    @Override
    public void unregisterAnimations() {
        List<String> list = new ArrayList<>(registeredAnimations.keySet());
        list.forEach(this::unregisterAnimation);
    }

    @Override
    public CompletableFuture<Boolean> startAnimation(@NotNull Player player, @NotNull Location location, @NotNull CaseDataBukkit caseData) {
        return startAnimation(player, location, caseData, 0);
    }

    @Override
    public CompletableFuture<Boolean> startAnimation(@NotNull Player player, @NotNull Location location, @NotNull CaseDataBukkit caseData, int delay) {
        Block block = location.getBlock();

        if (instance.api.getAnimationManager().getActiveCasesByBlock().containsKey(block)) {
            addon.getLogger().log(Level.WARNING, "Player " + player.getName() + " trying to start animation while another animation is running!");
            return CompletableFuture.completedFuture(false);
        }

        if (caseData.getItems().isEmpty()) {
            addon.getLogger().log(Level.WARNING, "Player " + player.getName() + " trying to start animation without items in CaseData!");
            return CompletableFuture.completedFuture(false);
        }

        caseData = caseData.clone();
        caseData.setItems(Tools.sortItemsByIndex(caseData.getItems()));
        String animation = caseData.getAnimation();
        if (!isRegistered(animation)) {
            ToolsBukkit.msg(player, "&cAn error occurred while opening the case!");
            ToolsBukkit.msg(player, "&cContact the project administration!");
            addon.getLogger().log(Level.WARNING, "Case animation " + animation + " does not exist!");
            return CompletableFuture.completedFuture(false);
        }

        CaseDataItem<CaseDataMaterialBukkit, ItemStack> winItem = caseData.getRandomItem();
        winItem.getMaterial().setDisplayName(Case.getInstance().papi.setPlaceholders(player, winItem.getMaterial().getDisplayName()));
        AnimationPreStartEvent preStartEvent = new AnimationPreStartEvent(player, caseData, block, winItem);
        Bukkit.getPluginManager().callEvent(preStartEvent);

        ActiveCase<Block> activeCase = new ActiveCase<>(block, caseData.getCaseType());
        UUID uuid = UUID.randomUUID();

        if (instance.hologramManager != null && caseData.getHologram().isEnabled()) {
            instance.hologramManager.removeHologram(block);
        }

        CaseAnimation<JavaAnimationBukkit, CaseDataMaterialBukkit, ItemStack> caseAnimation = getRegisteredAnimation(animation);

        CompletableFuture<Boolean> animationCompletion = new CompletableFuture<>();
        if (caseAnimation != null) {
            Location caseLocation = location;

            Location tempLocation = Case.getCaseLocationByBlockLocation(block.getLocation());
            if (tempLocation != null) caseLocation = tempLocation;

            Class<? extends JavaAnimationBukkit> animationClass = caseAnimation.getAnimation();

            try {
                getActiveCasesByBlock().put(block, uuid);

                if (animationClass != null) {
                    ConfigurationSection settings = caseData.getAnimationSettings() != null ? caseData.getAnimationSettings() : Case.getConfig().getAnimations().getConfigurationSection(animation);

                    if (caseAnimation.isRequireSettings() && settings == null)
                        throw new IllegalArgumentException("Animation " + animation + " requires settings for starting!");

                    JavaAnimationBukkit javaAnimation = animationClass.getDeclaredConstructor().newInstance();
                    javaAnimation.init(player, caseLocation, uuid, caseData, preStartEvent.getWinItem(), settings);

                    Bukkit.getScheduler().runTaskLater(Case.getInstance(), () -> {
                        try {
                            javaAnimation.start();
                            animationCompletion.complete(true);
                        } catch (Throwable t) {
                            addon.getLogger().log(Level.WARNING, "Error with starting animation " + animation, t);
                            getActiveCasesByBlock().remove(block);
                            animationCompletion.complete(false);
                        }
                    }, delay);

                } else {
                    throw new IllegalArgumentException("Animation executable class does not exist!");
                }

            } catch (Throwable t) {
                addon.getLogger().log(Level.WARNING, "Error with starting animation " + animation, t);
                getActiveCasesByBlock().remove(block);
                animationCompletion.complete(false);
            }
        }

        for (CaseGui<Inventory, Location, Player, CaseDataBukkit, CaseDataMaterialBukkit> gui : instance.api.getGUIManager().getPlayersGUI().values()) {
            if (gui.getLocation().equals(block.getLocation())) {
                gui.getPlayer().closeInventory();
            }
        }

        activeCases.put(uuid, activeCase);

        // AnimationStart event
        AnimationStartEvent startEvent = new AnimationStartEvent(player, animation, caseData, block, preStartEvent.getWinItem(), uuid);
        Bukkit.getPluginManager().callEvent(startEvent);
        return animationCompletion;
    }

    @Override
    public void animationPreEnd(CaseDataBukkit caseData, Player player, UUID uuid, CaseDataItem<CaseDataMaterialBukkit, ItemStack> item) {
        ActiveCase<Block> activeCase = activeCases.get(uuid);
        Location location = activeCase != null ? activeCase.getBlock().getLocation() : null;
        animationPreEnd(caseData, player, location, item);
    }

    @Override
    public void animationPreEnd(CaseDataBukkit caseData, Player player, Location location, CaseDataItem<CaseDataMaterialBukkit, ItemStack> item) {
        World world = location != null ? location.getWorld() : null;
        if(world == null) world = Bukkit.getWorlds().get(0);

        String choice = "";
        Map<String, Integer> levelGroups = getDefaultLevelGroup();
        if(!caseData.getLevelGroups().isEmpty()) levelGroups = caseData.getLevelGroups();

        String playerGroup = getPlayerGroup(world.getName(), player);
        if(isAlternative(levelGroups, playerGroup, item.getGroup())) {
            executeActions(player, caseData, item, null, true);
        } else {
            if (item.getGiveType().equalsIgnoreCase("ONE")) {
                executeActions(player, caseData, item, null, false);
            } else {
                choice = getRandomActionChoice(item);
                executeActions(player, caseData, item, choice, false);
            }
        }

        saveOpenInfo(caseData, player, item, choice);
    }

    /**
     * Animation end method for custom animations is called to completely end the animation
     * @param item Item data
     * @param caseData Case data
     * @param player Player who opened (offline player)
     * @param uuid Active case uuid
     */
    @Override
    public void animationEnd(CaseDataBukkit caseData, Player player, UUID uuid, CaseDataItem<CaseDataMaterialBukkit, ItemStack> item) {
        ActiveCase<Block> activeCase = activeCases.get(uuid);
        if(activeCase == null) return;

        Block block = activeCase.getBlock();
        activeCasesByBlock.remove(block);
        activeCases.remove(uuid);
        if (instance.hologramManager != null && caseData.getHologram().isEnabled()) {
            instance.hologramManager.createHologram(block, caseData);
        }
        AnimationEndEvent animationEndEvent = new AnimationEndEvent(player, caseData, block, item);
        Bukkit.getServer().getPluginManager().callEvent(animationEndEvent);
    }

    @Override
    public boolean isRegistered(String name) {
        return registeredAnimations.containsKey(name);
    }

    @Nullable
    @Override
    public CaseAnimation<JavaAnimationBukkit, CaseDataMaterialBukkit, ItemStack> getRegisteredAnimation(String animation) {
        return registeredAnimations.get(animation);
    }

    @Override
    public Map<UUID, ActiveCase<Block>> getActiveCases() {
        return activeCases;
    }

    @Override
    public Map<Block, UUID> getActiveCasesByBlock() {
        return activeCasesByBlock;
    }

    private static void saveOpenInfo(CaseDataBukkit caseData, OfflinePlayer player, CaseDataItem<CaseDataMaterialBukkit, ItemStack> item, String choice) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            CaseDataHistory data = new CaseDataHistory(item.getItemName(), caseData.getCaseType(), player.getName(), System.currentTimeMillis(), item.getGroup(), choice);
            CaseDataHistory[] historyData = caseData.getHistoryData();

            List<CaseDataHistory> databaseData = instance.database.getHistoryDataByCaseType(caseData.getCaseType()).join();
            if(!databaseData.isEmpty()) historyData = databaseData.toArray(new CaseDataHistory[10]);

            System.arraycopy(historyData, 0, historyData, 1, historyData.length - 1);
            historyData[0] = data;

            for (int i = 0; i < historyData.length; i++) {
                CaseDataHistory tempData = historyData[i];
                if (tempData != null) {
                    instance.database.setHistoryData(caseData.getCaseType(), i, tempData);
                }
            }

            // Set history data in memory
            Objects.requireNonNull(instance.api.getCaseManager().getCase(caseData.getCaseType())).setHistoryData(historyData);

            instance.api.getCaseOpenManager().addOpenCount(caseData.getCaseType(), player.getName(), 1);
        });
    }

    /**
     * Get random choice from item random action list
     * @param item Case item
     * @return random action name
     */
    public static String getRandomActionChoice(CaseDataItem<CaseDataMaterialBukkit, ItemStack> item) {
        ProbabilityCollection<String> collection = new ProbabilityCollection<>();
        for (String name : item.getRandomActions().keySet()) {
            CaseDataItem.RandomAction randomAction = item.getRandomAction(name);
            if(randomAction == null) continue;
            collection.add(name, randomAction.getChance());
        }
        return collection.get();
    }

    /**
     * Execute actions after case opening
     * @param player Player, who opened
     * @param caseData Case that was opened
     * @param item The prize that was won
     * @param choice In fact, these are actions that were selected from the RandomActions section
     * @param alternative If true, the item's alternative actions will be selected. (Same as {@link CaseDataItem#getAlternativeActions()})
     */
    public static void executeActions(Player player, CaseDataBukkit caseData, CaseDataItem<CaseDataMaterialBukkit, ItemStack> item, String choice, boolean alternative) {
        final String[] replacementRegex = {
                "%player%:" + player.getName(),
                "%casename%:" + caseData.getCaseType(),
                "%casedisplayname%:" + caseData.getCaseDisplayName(),
                "%casetitle%:" + caseData.getCaseTitle(),
                "%group%:" + item.getGroup(),
                "%groupdisplayname%:" + item.getMaterial().getDisplayName()
        };

        List<String> actions = Tools.rt(getActionsBasedOnChoice(item, choice, alternative), replacementRegex);

        instance.api.getActionManager().executeActions(player, actions);
    }

    /**
     * Get player primary group from Vault or LuckPerms
     * @param world Player world
     * @param player Bukkit player
     * @return player primary group
     */
    public static String getPlayerGroup(String world, OfflinePlayer player) {
        String group = null;
        if(instance.permissionDriver == PermissionDriver.vault) if(instance.permission != null) group = instance.permission.getPrimaryGroup(world, player);
        if(instance.permissionDriver == PermissionDriver.luckperms) if(instance.luckPerms != null) {
            User user = instance.luckPerms.getUserManager().getUser(player.getUniqueId());
            if(user != null) group = user.getPrimaryGroup();
        }
        return group;
    }

    /**
     * Get map of default LevelGroup from Config.yml
     * @return map of LevelGroup
     */
    public static Map<String, Integer> getDefaultLevelGroup() {
        Map<String, Integer> levelGroup = new HashMap<>();
        boolean isEnabled = instance.config.getConfig().getBoolean("DonateCase.LevelGroup");
        if(isEnabled) {
            ConfigurationSection section = instance.config.getConfig().getConfigurationSection("DonateCase.LevelGroups");
            if (section != null) {
                for (String group : section.getKeys(false)) {
                    int level = section.getInt(group);
                    levelGroup.put(group, level);
                }
            }
        }
        return levelGroup;
    }

    /**
     * Check for alternative actions
     * @param levelGroups map of LevelGroups (can be from case config or default Config.yml)
     * @param playerGroup player primary group
     * @param winGroup player win group
     * @return boolean
     */
    public static boolean isAlternative(Map<String, Integer> levelGroups, String playerGroup, String winGroup) {
        if(levelGroups.containsKey(playerGroup) && levelGroups.containsKey(winGroup)) {
            int playerGroupLevel = levelGroups.get(playerGroup);
            int winGroupLevel = levelGroups.get(winGroup);
            return playerGroupLevel >= winGroupLevel;
        }
        return false;
    }

    /**
     * Get actions from case item
     * @param item Case item
     * @param choice In fact, these are actions that were selected from the RandomActions section
     * @param alternative If true, the item's alternative actions will be selected. (Same as {@link CaseDataItem#getAlternativeActions()})
     * @return list of selected actions
     */
    public static List<String> getActionsBasedOnChoice(CaseDataItem<CaseDataMaterialBukkit, ItemStack> item, String choice, boolean alternative) {
        if (choice != null) {
            CaseDataItem.RandomAction randomAction = item.getRandomAction(choice);
            if (randomAction != null) {
                return randomAction.getActions();
            }
        }
        return alternative ? item.getAlternativeActions() : item.getActions();
    }
}