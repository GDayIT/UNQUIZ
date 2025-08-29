/**
 * GUI-Module (guimodule) für Spiel, Statistik, Sorting, Leitner und Styling.
 *
 * Leitlinien und Architektur:
 * - Öffentliche API: genau eine Fassade {@link guimodule.GuiModuleDelegate}.
 * - Alle UI-Panels/Services werden über Factories und Functional-APIs bereitgestellt.
 * - Abhängigkeiten zu dbbl erfolgen ausschließlich über {@link guimodule.GuiModuleDelegate#business()}.
 * - Keine direkten new-Aufrufe für Business/Persistenz in UI-Klassen.
 *
 * Verantwortungen:
 * - Quiz-Gameplay (ModularQuizPlay)
 * - Statistiken (ModularStatisticsPanel, ModularQuizStatistics)
 * - Leitner-Lernsystem (AdaptiveLeitnerSystem)
 * - Sortierung/Filter (ModularSortingService)
 * - Styling (ModularStyleService)
 *
 * Thread-Sicherheit:
 * - UI-Operationen auf der EDT (Event Dispatch Thread); Persistenz/IO in Hintergrund-Threads.
 * 
 * Da dieser Package sehr umfangreich ist wird eine weitere ausfürliche package-info beschreibung ausgelassen.
 * Viel Spaß beim nutzen!
 *
 * Beispielnutzung:
 * <pre>
 *   GuiModuleDelegate gm = GuiModuleDelegate.createDefault();
 *   // Themen
 *   java.util.List<String> topics = gm.allTopics().get();
 *   // Quiz-Panel
 *   javax.swing.JComponent quiz = new ModularQuizPlay(gm);
 *   // Statistiken-Panel
 *   javax.swing.JComponent stats = new ModularStatisticsPanel(gm);
 * </pre>
 */
package guimodule;