package gui;

import guimodule.GuiModuleDelegate;

/**
 * Facade delegate for the GUI package.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Provides a simplified, composable interface for accessing GUI module services.</li>
 *   <li>Encapsulates the creation and lifecycle of {@link GuiModuleDelegate}.</li>
 *   <li>Supports bootstrapping of the application GUI with minimal coupling.</li>
 *   <li>Acts as a central access point for all modular GUI services (styling, panels, configurations).</li>
 * </ul>
 *
 * <p>Design:
 * <ul>
 *   <li>Immutable after construction (final field).</li>
 *   <li>Delegates all module access to the internal {@link GuiModuleDelegate} instance.</li>
 *   <li>Supports default creation via {@link #createDefault()}.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * GuiDelegate gd = GuiDelegate.createDefault();
 * GuiModuleDelegate modules = gd.modules();
 * </pre>
 *
 * <p>Thread-safety:
 * <ul>
 *   <li>Immutable internal state ensures safe read access across threads.</li>
 *   <li>Delegated services may have their own concurrency guarantees.</li>
 * </ul>
 * 
 * Author: D.Georgiou
 * Version: 1.0
 */
public final class GuiDelegate {

    /**
     * Internal delegate providing access to all GUI modules.
     * <p>
     * This field is final to ensure immutability and encapsulation of module services.
     */
    private final GuiModuleDelegate gm;

    /**
     * Private constructor. Initializes the internal GUI module delegate.
     *
     * @param gm the {@link GuiModuleDelegate} to delegate module access to
     */
    private GuiDelegate(GuiModuleDelegate gm) {
        this.gm = gm;
    }

    /**
     * Factory method for creating a default {@link GuiDelegate}.
     *
     * <p>This method initializes all underlying GUI modules using
     * {@link GuiModuleDelegate#createDefault()} and returns a new {@link GuiDelegate}.
     *
     * @return a fully initialized {@link GuiDelegate} with default modules
     */
    public static GuiDelegate createDefault() {
        return new GuiDelegate(GuiModuleDelegate.createDefault());
    }

    /**
     * Accessor for the underlying GUI module delegate.
     *
     * <p>All modular GUI services (styling, panels, configuration, etc.) are
     * accessed via this delegate. This allows decoupling of the application frame
     * from individual module implementations.
     *
     * @return the internal {@link GuiModuleDelegate} instance
     */
    public GuiModuleDelegate modules() { 
        return gm; 
    }
}