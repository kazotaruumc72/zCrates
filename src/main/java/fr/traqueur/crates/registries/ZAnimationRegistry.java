package fr.traqueur.crates.registries;

import fr.traqueur.crates.animations.AnimationsRegistrar;
import fr.traqueur.crates.api.CratesPlugin;
import fr.traqueur.crates.api.Logger;
import fr.traqueur.crates.api.models.animations.Animation;
import fr.traqueur.crates.api.registries.AnimationsRegistry;
import fr.traqueur.crates.api.registries.HookActionsRegistry;
import fr.traqueur.crates.api.registries.Registry;
import fr.traqueur.crates.engine.ZScriptEngine;
import fr.traqueur.crates.models.wrappers.ArrayHelper;
import org.mozilla.javascript.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ZAnimationRegistry extends AnimationsRegistry {

    private final ZScriptEngine engine;

    public ZAnimationRegistry(CratesPlugin plugin, ZScriptEngine engine, String resourceFolder) {
        super(plugin, resourceFolder);
        this.engine = engine;
    }

    @Override
    protected Animation loadFile(Path file) {
        try {
            String scriptContent = Files.readString(file);
            Logger.debug("Loading animation from: {}", file.getFileName());

            // Create a capture context to intercept animation registrations
            HookActionsRegistry hookActionsRegistry = Registry.get(HookActionsRegistry.class);
            AnimationsRegistrar registrar = new AnimationsRegistrar(file.toString(), engine, hookActionsRegistry);

            // Create secure scope
            Scriptable scope = engine.createSecureScope();

            // Add the animation registration API
            ScriptableObject.putProperty(scope, "animations", Context.javaToJS(registrar, scope));

            // Add array helper utility for JavaScript array conversions
            ScriptableObject.putProperty(scope, "ArrayHelper",
                Context.javaToJS(new ArrayHelper(), scope));

            // Execute the script
            engine.evaluateFile(scriptContent, file.getFileName().toString(), scope);

            // Retrieve captured animation (should be exactly one per file)
            List<Animation> captured = registrar.animations();
            if (captured.isEmpty()) {
                Logger.warning("No animation registered in file: {}", file.getFileName());
                return null;
            }

            if (captured.size() > 1) {
                Logger.warning("Multiple animations registered in file: {}, using first one", file.getFileName());
            }
            Animation animation = captured.getFirst();
            this.storage.put(animation.id(), animation);
            return animation;
        } catch (Exception e) {
            Logger.severe("Failed to parse animation file {}", e, file.getFileName());
            return null;
        }
    }

}