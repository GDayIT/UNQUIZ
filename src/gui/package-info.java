/**
 * GUI-Einstieg (gui) – Anwendungsrahmen und Bootstrapping.
 *
 * Leitlinien und Architektur:
 * - Öffentliche API: genau eine Fassade {@link gui.GuiDelegate} und der Einstieg {@link gui.Frame}.
 * - Frame erstellt Delegates (GuiDelegate → GuiModuleDelegate) und verdrahtet Panels.
 * - Keine direkten Instanzen der Business/Persistenz in diesem Paket.
 *
 * Beispielnutzung (Main):
 * <pre>
 *   public static void main(String[] args) {
 *     javax.swing.SwingUtilities.invokeLater(Frame::new);
 *   }
 * </pre>
 */
package gui;