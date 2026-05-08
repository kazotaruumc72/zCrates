package fr.traqueur.crates.registries;

import fr.traqueur.crates.api.models.animations.AnimationContext;
import fr.traqueur.crates.api.registries.HookActionsRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ZHookActionsRegistry implements HookActionsRegistry {

    private final Map<String, Function<AnimationContext, Object>> storage = new HashMap<>();

    @Override
    public void register(String namespace, Function<AnimationContext, Object> factory) {
        storage.put(namespace, factory);
    }

    @Override
    public Function<AnimationContext, Object> getById(String namespace) {
        return storage.get(namespace);
    }

    @Override
    public List<Function<AnimationContext, Object>> getAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void clear() {
        // Intentional no-op: hook namespaces are registered once at startup
        // and must survive hot-reloads (/zcrates reload).
    }
}
