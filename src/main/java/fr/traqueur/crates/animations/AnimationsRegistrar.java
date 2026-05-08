package fr.traqueur.crates.animations;

import fr.traqueur.crates.api.Logger;
import fr.traqueur.crates.api.models.animations.Animation;
import fr.traqueur.crates.api.models.animations.AnimationContext;
import fr.traqueur.crates.api.models.animations.AnimationPhase;
import fr.traqueur.crates.api.registries.HookActionsRegistry;
import fr.traqueur.crates.engine.ZScriptEngine;
import fr.traqueur.crates.models.ZAnimation;
import fr.traqueur.crates.models.wrappers.JSAnimationContext;
import org.mozilla.javascript.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AnimationsRegistrar {

        private final String sourceFile;
        private final ZScriptEngine engine;
        private final HookActionsRegistry hookActionsRegistry;
        private final List<Animation> animations;

        public AnimationsRegistrar(String sourceFile, ZScriptEngine engine, HookActionsRegistry hookActionsRegistry) {
            this.sourceFile = sourceFile;
            this.engine = engine;
            this.hookActionsRegistry = hookActionsRegistry;
            this.animations = new ArrayList<>();
        }

        public List<Animation> animations() {
            return animations;
        }

        public void register(String id, NativeObject config) {
            try {
                // Extract phases array
                Object phasesObj = config.get("phases");
                if (!(phasesObj instanceof NativeArray phasesArray)) {
                    Logger.warning("Animation {} has no phases array", id);
                    return;
                }

                List<AnimationPhase> phases = new ArrayList<>();
                for (Object phaseObj : phasesArray) {
                    if (phaseObj instanceof NativeObject phaseConfig) {
                        AnimationPhase phase = parsePhase(phaseConfig);
                        if (phase != null) {
                            phases.add(phase);
                        }
                    }
                }

                // Extract callbacks
                Consumer<AnimationContext> onComplete = extractConsumer(config, "onComplete");
                Consumer<AnimationContext> onCancel = extractConsumer(config, "onCancel");

                // Create animation using the ZAnimation record
                Animation animation = new ZAnimation(id, sourceFile, phases, onComplete, onCancel);
                animations.add(animation);

                Logger.debug("Captured animation: {} with {} phases", id, phases.size());

            } catch (Exception e) {
                Logger.warning("Failed to parse animation {}: {}", id, e.getMessage());
                Logger.debug("Parse error:", e);
            }
        }

        private AnimationPhase parsePhase(NativeObject phaseConfig) {
            try {
                String name = (String) phaseConfig.get("name");
                Number durationNum = (Number) phaseConfig.get("duration");
                long duration = durationNum != null ? durationNum.longValue() : 1000;

                long interval = 0;
                Object intervalObj = phaseConfig.get("interval");
                if (intervalObj instanceof Number num) {
                    interval = num.longValue();
                }

                AnimationPhase.SpeedCurve speedCurve = AnimationPhase.SpeedCurve.LINEAR;
                Object speedCurveObj = phaseConfig.get("speedCurve");
                if (speedCurveObj instanceof String speedCurveName) {
                    speedCurve = AnimationPhase.SpeedCurve.valueOf(speedCurveName);
                }

                Consumer<AnimationContext> onStart = extractConsumer(phaseConfig, "onStart");
                BiConsumer<AnimationContext, AnimationPhase.TickData> onTick = extractBiConsumer(phaseConfig, "onTick");
                Consumer<AnimationContext> onComplete = extractConsumer(phaseConfig, "onComplete");

                return new AnimationPhase(name, duration, interval, speedCurve, onStart, onTick, onComplete);

            } catch (Exception e) {
                Logger.warning("Failed to parse phase: {}", e.getMessage());
                return null;
            }
        }

        private Consumer<AnimationContext> extractConsumer(Scriptable obj, String key) {
            Object value = ScriptableObject.getProperty(obj, key);
            if (value != Scriptable.NOT_FOUND && value instanceof Function jsFunction) {
                return animCtx -> {
                    JSAnimationContext jsCtx = new JSAnimationContext(animCtx, hookActionsRegistry);
                    engine.executeFunction(jsFunction, jsCtx);
                };
            }
            return null;
        }

        private BiConsumer<AnimationContext, AnimationPhase.TickData> extractBiConsumer(Scriptable obj, String key) {
            Object value = ScriptableObject.getProperty(obj, key);
            if (value != Scriptable.NOT_FOUND && value instanceof Function jsFunction) {
                return (animCtx, tickData) -> {
                    JSAnimationContext jsCtx = new JSAnimationContext(animCtx, hookActionsRegistry);
                    engine.executeFunction(jsFunction, jsCtx, tickData);
                };
            }
            return null;
        }
    }