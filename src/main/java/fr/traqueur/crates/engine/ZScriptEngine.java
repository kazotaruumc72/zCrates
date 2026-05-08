package fr.traqueur.crates.engine;

import fr.traqueur.crates.api.Logger;
import org.mozilla.javascript.*;

import java.util.Set;

/**
 * Unified secure JavaScript engine for both animations and algorithms.
 * Provides a centralized, safe execution environment with security restrictions.
 * <p>
 * Security features:
 * <ul>
 *   <li>Uses {@code initSafeStandardObjects()} - no java.* access</li>
 *   <li>Blocks dangerous Object methods (getClass, notify, wait, clone, finalize, hashCode)</li>
 *   <li>Interpreter mode (optimization level -1) for safer execution</li>
 *   <li>Isolated scopes with null parent</li>
 * </ul>
 */
public class ZScriptEngine {

    /**
     * Set of blocked method names that should not be accessible from JavaScript.
     */
    private static final Set<String> BLOCKED_METHODS = Set.of(
            "getClass", "class",
            "notify", "notifyAll",
            "wait", "clone",
            "finalize", "hashCode"
    );

    private final Context rhinoContext;
    private final String engineName;

    /**
     * Creates a new secure script engine with the given name.
     *
     * @param engineName the name of this engine instance (for logging)
     */
    public ZScriptEngine(String engineName) {
        this.engineName = engineName;
        this.rhinoContext = Context.enter();
        rhinoContext.setOptimizationLevel(-1);
        rhinoContext.setLanguageVersion(Context.VERSION_ES6);
        rhinoContext.setWrapFactory(createSecureWrapFactory());
    }

    /**
     * Creates a WrapFactory that blocks access to dangerous Java methods.
     */
    private WrapFactory createSecureWrapFactory() {
        return new WrapFactory() {
            @Override
            public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
                return new NativeJavaObject(scope, javaObject, staticType) {
                    @Override
                    public Object get(String name, Scriptable start) {
                        if (BLOCKED_METHODS.contains(name)) {
                            return NOT_FOUND;
                        }
                        return super.get(name, start);
                    }
                };
            }
        };
    }

    /**
     * Creates a new secure scope for script execution.
     * Each scope is isolated with no parent scope.
     *
     * @return a new isolated Scriptable scope
     */
    public Scriptable createSecureScope() {
        Scriptable scope = rhinoContext.initSafeStandardObjects();
        scope.setParentScope(null);
        return scope;
    }

    /**
     * Executes a JavaScript function with arguments in a secure context.
     * This variant does not return a value (fire-and-forget).
     *
     * @param jsFunction the JavaScript function to execute
     * @param args       the arguments to pass to the function
     */
    public void executeFunction(Function jsFunction, Object... args) {
        executeFunctionWithResult(jsFunction, args);
    }

    /**
     * Executes a JavaScript function with arguments in a secure context
     * and returns the result.
     *
     * @param jsFunction the JavaScript function to execute
     * @param args       the arguments to pass to the function
     * @return the result of the function execution, or null if function is null or an error occurs
     */
    public Object executeFunctionWithResult(Function jsFunction, Object... args) {
        if (jsFunction == null) {
            return null;
        }

        try {
            Scriptable scope = this.createSecureScope();
            Object[] jsArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                jsArgs[i] = Context.javaToJS(args[i], scope);
            }
            return jsFunction.call(this.rhinoContext, scope, scope, jsArgs);

        } catch (Exception e) {
            Logger.warning("Error executing {} function: {}", engineName, e.getMessage());
            Logger.debug("{} function error:", engineName, e);
            return null;
        }
    }

    /**
     * Evaluates a JavaScript file content and executes it in a secure scope.
     * Used for loading script definition files.
     *
     * @param scriptContent the JavaScript code
     * @param fileName      the file name (for error messages)
     * @param scope         the scope to execute in
     */
    public void evaluateFile(String scriptContent, String fileName, Scriptable scope) {
        try {
            rhinoContext.evaluateString(scope, scriptContent, fileName, 1, null);
        } catch (Exception e) {
            Logger.warning("Error evaluating {} file {}: {}", engineName, fileName, e.getMessage());
        }
    }

    /**
     * Returns the underlying Rhino context.
     * Use with caution - prefer using the provided methods.
     *
     * @return the Rhino Context
     */
    public Context getContext() {
        return rhinoContext;
    }

    /**
     * Returns the name of this engine instance.
     *
     * @return the engine name
     */
    public String getEngineName() {
        return engineName;
    }

    /**
     * Closes the engine and releases resources.
     * Should be called on plugin disable.
     */
    public void close() {
        if (rhinoContext != null) {
            Logger.info("Closing {} engine...", engineName);
            Context.exit();
        }
    }
}