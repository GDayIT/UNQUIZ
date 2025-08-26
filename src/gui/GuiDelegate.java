package gui;

import guimodule.GuiModuleDelegate;

/**
 * Single facade delegate for the gui package.
 * Provides a minimal, composable surface for application bootstrapping.
 */
public final class GuiDelegate {
    private final GuiModuleDelegate gm;

    private GuiDelegate(GuiModuleDelegate gm) {
        this.gm = gm;
    }

    public static GuiDelegate createDefault() {
        return new GuiDelegate(GuiModuleDelegate.createDefault());
    }

    public GuiModuleDelegate modules() { return gm; }
}
