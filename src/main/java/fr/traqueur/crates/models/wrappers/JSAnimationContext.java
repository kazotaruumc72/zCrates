package fr.traqueur.crates.models.wrappers;

import fr.traqueur.crates.api.Logger;
import fr.traqueur.crates.api.models.Wrapper;
import fr.traqueur.crates.api.models.animations.AnimationContext;
import fr.traqueur.crates.api.models.crates.Crate;
import fr.traqueur.crates.api.registries.HookActionsRegistry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JavaScript-facing wrapper for {@link AnimationContext} that additionally exposes
 * hook-provided action namespaces.
 * <p>
 * Delegates {@code player()}, {@code inventory()}, {@code crate()} to the underlying
 * context so existing animation scripts continue to work unchanged.
 * <p>
 * Hook namespaces are accessed via {@code context.hook("namespaceName")} and return
 * a bound Java object whose public methods are directly callable from JavaScript.
 * Instances are cached per JS execution to avoid redundant factory calls.
 */
public class JSAnimationContext {

    private final AnimationContext delegate;
    private final HookActionsRegistry registry;
    private final Map<String, Object> cache = new HashMap<>();

    public JSAnimationContext(AnimationContext delegate, HookActionsRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    public Wrapper<Player> player() {
        return delegate.player();
    }

    public Wrapper<Inventory> inventory() {
        return delegate.inventory();
    }

    public Wrapper<Crate> crate() {
        return delegate.crate();
    }

    /**
     * Returns the bound action object for the given hook namespace.
     * The object is created once per JS execution and cached for subsequent calls.
     *
     * @param namespace the namespace registered by the hook (e.g. {@code "mythicmobs"})
     * @return the bound action object, or {@code null} if the namespace is unknown
     */
    public Object hook(String namespace) {
        return cache.computeIfAbsent(namespace, ns -> {
            Function<AnimationContext, Object> factory = registry.getById(ns);
            if (factory == null) {
                Logger.warning("Unknown hook namespace: '{}'", ns);
                return null;
            }
            return factory.apply(delegate);
        });
    }
}
