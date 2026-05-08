package fr.traqueur.crates;

import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.loader.NoneLoader;
import fr.maxlego08.sarah.DatabaseConnection;
import fr.maxlego08.sarah.MigrationManager;
import fr.maxlego08.sarah.RequestHelper;
import fr.traqueur.commands.spigot.CommandManager;
import fr.traqueur.crates.api.CratesPlugin;
import fr.traqueur.crates.api.Logger;
import fr.traqueur.crates.api.managers.CratesManager;
import fr.traqueur.crates.api.managers.UsersManager;
import fr.traqueur.crates.api.models.algorithms.RandomAlgorithm;
import fr.traqueur.crates.api.models.animations.Animation;
import fr.traqueur.crates.api.models.crates.Crate;
import fr.traqueur.crates.api.models.crates.Key;
import fr.traqueur.crates.api.models.crates.Condition;
import fr.traqueur.crates.api.models.crates.Reward;
import fr.traqueur.crates.api.models.placedcrates.DisplayType;
import fr.traqueur.crates.api.registries.*;
import fr.traqueur.crates.api.serialization.Keys;
import fr.traqueur.crates.api.services.MessagesService;
import fr.traqueur.crates.api.settings.Settings;
import fr.traqueur.crates.api.settings.models.DatabaseSettings;
import fr.traqueur.crates.commands.ZCratesCommand;
import fr.traqueur.crates.commands.arguments.AnimationArgument;
import fr.traqueur.crates.commands.arguments.CrateArgument;
import fr.traqueur.crates.commands.arguments.DisplayTypeArgument;
import fr.traqueur.crates.commands.handler.CommandsMessageHandler;
import fr.traqueur.crates.engine.ZScriptEngine;
import fr.traqueur.crates.managers.ZCratesManager;
import fr.traqueur.crates.managers.ZUsersManager;
import fr.traqueur.crates.models.conditions.CooldownCondition;
import fr.traqueur.crates.models.conditions.PermissionCondition;
import fr.traqueur.crates.models.keys.PhysicKey;
import fr.traqueur.crates.models.keys.VirtualKey;
import fr.traqueur.crates.models.placedcrates.BlockCrateDisplayFactory;
import fr.traqueur.crates.models.placedcrates.EntityCrateDisplayFactory;
import fr.traqueur.crates.models.rewards.CommandReward;
import fr.traqueur.crates.models.rewards.CommandsListReward;
import fr.traqueur.crates.models.rewards.ItemReward;
import fr.traqueur.crates.models.rewards.ItemsListReward;
import fr.traqueur.crates.registries.*;
import fr.traqueur.crates.serialization.ZPlacedCrateDataType;
import fr.traqueur.crates.settings.PluginSettings;
import fr.traqueur.crates.settings.models.SQLSettings;
import fr.traqueur.crates.settings.models.SQLiteSettings;
import fr.traqueur.crates.settings.readers.AnimationReader;
import fr.traqueur.crates.settings.readers.RandomAlgorithmReader;
import fr.traqueur.crates.storage.repositories.UserRepository;
import fr.traqueur.crates.views.buttons.AnimationButton;
import fr.traqueur.crates.views.buttons.PreviewButton;
import fr.traqueur.crates.views.buttons.RerollButton;
import fr.traqueur.crates.views.loaders.PreviewLoader;
import fr.traqueur.structura.api.Structura;
import fr.traqueur.structura.exceptions.StructuraException;
import fr.traqueur.structura.registries.CustomReaderRegistry;
import fr.traqueur.structura.registries.PolymorphicRegistry;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class zCrates extends CratesPlugin {

    private static final String CONFIG_FILE = "config.yml";
    private static final String MESSAGES_FILE = "messages.yml";
    private static final String ANIMATIONS_FOLDER = "animations";
    private static final String ALGORITHMS_FOLDER = "algorithms";
    private static final String CRATES_FOLDER = "crates";

    private final org.slf4j.Logger logger = LoggerFactory.getLogger("zCrates");

    private InventoryManager inventoryManager;
    private ButtonManager buttonManager;
    private ZScriptEngine scriptEngine;
    private DatabaseConnection databaseConnection;

    @Override
    public void onLoad() {
        this.scriptEngine = new ZScriptEngine("zcrates-scripts");
        this.registerRegistries();
        this.injectPolymorphismAdapters();
        this.injectReaders();
    }

    @Override
    public void onEnable() {
        long enableTime = System.currentTimeMillis();
        this.saveDefaultConfig();

        PluginSettings settings = this.createSettings(CONFIG_FILE, PluginSettings.class);
        Logger.init(logger, settings.debug());

        Logger.info("<yellow>=== ENABLE START ===");
        Logger.info("<gray>Plugin Version V<red>{}", this.getDescription().getVersion());

        int pluginId = 28262;
        Metrics metrics = new Metrics(this, pluginId);
        this.addCustomCharts(metrics);

        ZPlacedCrateDataType.initialize();
        Keys.initialize(this);
        MessagesService.initialize(this);

        this.reloadConfig();

        this.populateInventoriesRelatedStuffs();

        this.injectButtons();

        this.populateRegistries();

        this.databaseConnection = settings.database().connection(settings.debug());
        if (!databaseConnection.isValid()) {
            Logger.severe("Unable to connect to database !");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        RequestHelper requestHelper = new RequestHelper(databaseConnection, Logger::info);
        UserRepository userRepository = new UserRepository(requestHelper);

        UsersManager usersManager = this.registerManager(UsersManager.class, new ZUsersManager(userRepository));
        CratesManager cratesManager = this.registerManager(CratesManager.class, new ZCratesManager(inventoryManager));

        usersManager.init();
        cratesManager.init();
        MigrationManager.execute(this.databaseConnection, Logger::info);

        this.registerCommands(settings);

        Logger.info("<yellow>=== ENABLE DONE <gray>(<gold>" + Math.abs(enableTime - System.currentTimeMillis()) + "ms<gray>) <yellow>===");

    }

    private void populateInventoriesRelatedStuffs() {
        var inventoryProvider = getServer().getServicesManager().getRegistration(InventoryManager.class);
        var buttonProvider = getServer().getServicesManager().getRegistration(ButtonManager.class);
        if (inventoryProvider == null) {
            Logger.severe("zMenu InventoryManager not found! Is zMenu installed?");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (buttonProvider == null) {
            Logger.severe("zMenu ButtonManager not found! Is zMenu installed?");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.buttonManager = buttonProvider.getProvider();
        this.inventoryManager = inventoryProvider.getProvider();
    }

    private void populateRegistries() {
        HooksRegistry hooksRegistry = Registry.get(HooksRegistry.class);
        hooksRegistry.scanPackage(this, "fr.traqueur.crates");
        hooksRegistry.enableAll();

        CrateDisplayFactoriesRegistry crateDisplayFactoriesRegistry = Registry.get(CrateDisplayFactoriesRegistry.class);
        crateDisplayFactoriesRegistry.registerGeneric(DisplayType.BLOCK, new BlockCrateDisplayFactory());
        crateDisplayFactoriesRegistry.registerGeneric(DisplayType.ENTITY, new EntityCrateDisplayFactory());

        Registry.get(AnimationsRegistry.class).loadFromFolder();
        Registry.get(RandomAlgorithmsRegistry.class).loadFromFolder();

        CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
        cratesRegistry.loadFromFolder();
        if (cratesRegistry.getAll().isEmpty()) {
            Logger.warning("<yellow>No crates loaded! Please create crates in the 'crates' folder. Animation debug command will not work without crates.</yellow>");
        }
    }

    private void registerRegistries() {
        Registry.register(HookActionsRegistry.class, new ZHookActionsRegistry());
        Registry.register(AnimationsRegistry.class, new ZAnimationRegistry(this, this.scriptEngine, ANIMATIONS_FOLDER));
        Registry.register(RandomAlgorithmsRegistry.class, new ZRandomAlgorithmRegistry(this, this.scriptEngine, ALGORITHMS_FOLDER));
        Registry.register(CratesRegistry.class, new ZCratesRegistry(this, CRATES_FOLDER));
        Registry.register(ItemsProvidersRegistry.class, new ZItemsProviderRegistry());
        Registry.register(HooksRegistry.class, new ZHooksRegistry());
        Registry.register(CrateDisplayFactoriesRegistry.class, new ZCrateDisplayFactoriesRegistry());
        Registry.register(RewardSortersRegistry.class, new ZRewardSortersRegistry());
    }

    private void injectPolymorphismAdapters() {
        PolymorphicRegistry.create(Reward.class, registry -> {
            registry.register("ITEM", ItemReward.class);
            registry.register("ITEMS", ItemsListReward.class);
            registry.register("COMMAND", CommandReward.class);
            registry.register("COMMANDS", CommandsListReward.class);
        });

        PolymorphicRegistry.create(DatabaseSettings.class, registry -> {
            registry.register("MARIADB", SQLSettings.class);
            registry.register("MYSQL", SQLSettings.class);
            registry.register("SQLITE", SQLiteSettings.class);
        });

        PolymorphicRegistry.create(Key.class, registry -> {
            registry.register("VIRTUAL", VirtualKey.class);
            registry.register("PHYSIC", PhysicKey.class);
        });

        PolymorphicRegistry.create(Condition.class, registry -> {
            registry.register("PERMISSION", PermissionCondition.class);
            registry.register("COOLDOWN", CooldownCondition.class);
        });
    }

    private void injectReaders() {
        CustomReaderRegistry.getInstance().register(Animation.class, new AnimationReader());
        CustomReaderRegistry.getInstance().register(RandomAlgorithm.class, new RandomAlgorithmReader());
    }

    private void injectButtons() {
        if(this.buttonManager != null) {
            this.buttonManager.register(new NoneLoader(this, AnimationButton.class, "ZCRATES_ANIMATION"));
            this.buttonManager.register(new PreviewLoader(this, "ZCRATES_PREVIEW"));
            this.buttonManager.register(new NoneLoader(this, RerollButton.class, "ZCRATES_REROLL"));
        }
    }

    @Override
    public void onDisable() {
        long disableTime = System.currentTimeMillis();
        Logger.info("<yellow>=== DISABLE START ===");
        Logger.info("<gray>Plugin Version V<red>{}", this.getDescription().getVersion());

        this.scriptEngine.close();
        MessagesService.close();

        CratesManager cratesManager = this.getManager(CratesManager.class);
        if (cratesManager != null) {
            cratesManager.stopAllOpening();
            cratesManager.unloadAllPlacedCrates();
        }

        if (this.databaseConnection != null) {
            this.databaseConnection.disconnect();
        }

        Logger.info("<yellow>=== DISABLE DONE <gray>(<gold>" + Math.abs(disableTime - System.currentTimeMillis()) + "ms<gray>) <yellow>===");
    }

    @Override
    public void saveDefaultConfig() {
        List.of(CONFIG_FILE, MESSAGES_FILE).forEach(this::saveIfNotExits);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        PluginSettings settings = this.createSettings(CONFIG_FILE, PluginSettings.class);
        Logger.setDebug(settings.debug());
        try {
            Structura.loadEnum(this.getDataFolder().toPath().resolve(MESSAGES_FILE), Messages.class);
        } catch (StructuraException e) {
            logger.error("Failed to load messages configuration.", e);
        }

        AnimationsRegistry animationsRegistry = Registry.get(AnimationsRegistry.class);
        if (animationsRegistry != null) {
            animationsRegistry.loadFromFolder();
        }

        RandomAlgorithmsRegistry randomAlgorithmsRegistry = Registry.get(RandomAlgorithmsRegistry.class);
        if (randomAlgorithmsRegistry != null) {
            randomAlgorithmsRegistry.loadFromFolder();
        }

        CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
        if (cratesRegistry != null) {
            cratesRegistry.loadFromFolder();
        }

        CratesManager cratesManager = this.getManager(CratesManager.class);
        if (cratesManager != null) {
            cratesManager.ensureInventoriesExist();
        }

    }

    @Override
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    private void registerCommands(PluginSettings settings) {
        CommandManager<CratesPlugin> commandManager = new CommandManager<>(this);
        commandManager.setLogger(new fr.traqueur.commands.api.logging.Logger() {
            @Override
            public void error(String s) {
                Logger.severe(s);
            }

            @Override
            public void info(String s) {
                Logger.info(s);
            }
        });
        commandManager.setDebug(settings.debug());
        commandManager.setMessageHandler(new CommandsMessageHandler());

        commandManager.registerConverter(Animation.class, new AnimationArgument());
        commandManager.registerConverter(Crate.class, new CrateArgument());
        commandManager.registerConverter(DisplayType.class, new DisplayTypeArgument());

        commandManager.registerCommand(new ZCratesCommand(this));
    }

    private void saveIfNotExits(String fileName) {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }
        File file = new File(this.getDataFolder(), fileName);
        if (!file.exists()) {
            this.saveResource(fileName, false);
        }
    }

    private <T extends Settings> T createSettings(String path, Class<T> clazz) {
        File file = new File(this.getDataFolder(), path);
        if (!file.exists()) {
            throw new IllegalArgumentException("File " + path + " does not exist.");
        }
        T instance = Structura.load(file, clazz);
        Settings.register(clazz, instance);
        return instance;
    }

    private void addCustomCharts(@NotNull Metrics metrics) {

        // Number of crates configured
        metrics.addCustomChart(new SimplePie("crates_count", () -> {
            CratesRegistry registry = Registry.get(CratesRegistry.class);
            if (registry == null) return "0";
            int count = registry.getAll().size();
            if (count == 0) return "0";
            if (count <= 5) return "1-5";
            if (count <= 10) return "6-10";
            if (count <= 20) return "11-20";
            if (count <= 50) return "21-50";
            return "50+";
        }));

        // Reward types distribution
        metrics.addCustomChart(new AdvancedPie("reward_types", () -> {
            CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
            if (cratesRegistry == null) return new HashMap<>();

            Map<String, Integer> rewardCounts = new HashMap<>();
            rewardCounts.put("ITEM", 0);
            rewardCounts.put("ITEMS", 0);
            rewardCounts.put("COMMAND", 0);
            rewardCounts.put("COMMANDS", 0);

            for (Crate crate : cratesRegistry.getAll()) {
                for (Reward reward : crate.rewards()) {
                    String type = reward.getClass().getSimpleName()
                            .replace("Reward", "")
                            .replace("ListReward", "S")
                            .toUpperCase();
                    rewardCounts.put(type, rewardCounts.getOrDefault(type, 0) + 1);
                }
            }

            return rewardCounts;
        }));

        // Key types distribution
        metrics.addCustomChart(new AdvancedPie("key_types", () -> {
            CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
            if (cratesRegistry == null) return new HashMap<>();

            Map<String, Integer> keyCounts = new HashMap<>();
            keyCounts.put("VIRTUAL", 0);
            keyCounts.put("PHYSIC", 0);

            for (Crate crate : cratesRegistry.getAll()) {
                String type = crate.key().getClass().getSimpleName()
                        .replace("Key", "")
                        .toUpperCase();
                keyCounts.put(type, keyCounts.getOrDefault(type, 0) + 1);
            }

            return keyCounts;
        }));

        // Most used animations
        metrics.addCustomChart(new AdvancedPie("animations_used", () -> {
            CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
            if (cratesRegistry == null) return new HashMap<>();

           Map<String, Integer> animationCounts = new HashMap<>();
            for (Crate crate : cratesRegistry.getAll()) {
                String animationId = crate.animation().id();
                animationCounts.put(animationId, animationCounts.getOrDefault(animationId, 0) + 1);
            }

            return animationCounts;
        }));

        // Most used algorithms
        metrics.addCustomChart(new AdvancedPie("algorithms_used", () -> {
            CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
            if (cratesRegistry == null) return new HashMap<>();

            Map<String, Integer> algorithmCounts = new HashMap<>();
            for (Crate crate : cratesRegistry.getAll()) {
                String algorithmId = crate.algorithm().id();
                algorithmCounts.put(algorithmId, algorithmCounts.getOrDefault(algorithmId, 0) + 1);
            }

            return algorithmCounts;
        }));

        // Crates with rerolls enabled
        metrics.addCustomChart(new SimplePie("rerolls_enabled", () -> {
            CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
            if (cratesRegistry == null) return "None";

            long cratesWithRerolls = cratesRegistry.getAll().stream()
                    .filter(crate -> crate.maxRerolls() > 0)
                    .count();
            long totalCrates = cratesRegistry.getAll().size();

            if (totalCrates == 0) return "None";
            if (cratesWithRerolls == 0) return "None";
            if (cratesWithRerolls == totalCrates) return "All";

            double percentage = (cratesWithRerolls * 100.0) / totalCrates;
            if (percentage < 25) return "< 25%";
            if (percentage < 50) return "25-50%";
            if (percentage < 75) return "50-75%";
            return "75-100%";
        }));
    }
}
