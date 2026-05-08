package fr.traqueur.crates.api.registries;

import fr.traqueur.crates.api.models.animations.AnimationContext;

import java.util.function.Function;

/**
 * Registry for hook-provided action namespaces accessible from JavaScript animations.
 * <p>
 * Hooks register a factory under a namespace name. At animation execution time,
 * the factory receives the current {@link AnimationContext} and returns a bound
 * Java object whose public methods become callable from JavaScript via
 * {@code context.hook("namespaceName").method(...)}.
 * <p>
 * This registry is intentionally never cleared on hot-reload: hooks register
 * their factories once at startup via {@link fr.traqueur.crates.api.hooks.Hook#onEnable()}.
 */
public interface HookActionsRegistry extends Registry<String, Function<AnimationContext, Object>> {
}
