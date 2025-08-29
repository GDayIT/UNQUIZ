/**
 * Business- und Persistenzschicht (dbbl).
 *
 * Leitlinien und Architektur:
 * - Öffentliche API des Pakets: genau eine Fassade (Delegate) {@link dbbl.DbblDelegate}.
 * - Interne Implementierungen sind paket-intern (package-private), z. B.
 *   ModularPersistenceService und ModularBusinessController. Auf diese wird
 *   außerhalb des Pakets nicht direkt zugegriffen.
 * - Funktionale Schnittstellen (Supplier/Function/BiFunction/Runnable) werden
 *   genutzt, um Lambdas und testbare Komposition zu ermöglichen.
 * - Persistenz: Snapshot-basierte Serialisierung mit atomarem Write (.tmp → rename)
 *   und Abwärtskompatibilität (Legacy-Fallback beim Laden).
 * - Datenmodelle: RepoQuizeeQuestions u. a. bilden die Quiz-Domain-Objekte ab.
 *
 * Verantwortungen:
 * - Themenverwaltung (Anlegen/Löschen/Laden von Themen und Beschreibungen)
 * - Fragenverwaltung (CRUD über funktionale Endpunkte)
 * - Persistenzintegration (Speichern/Laden/Backups, Ereignisbenachrichtigung)
 * - Delegation in die Businesslogik (ModularBusinessController)
 *
 * Sichtbarkeit und Kapselung:
 * - Klassen in diesem Paket sind standardmäßig paket-intern; nur DbblDelegate ist public.
 * - Andere Pakete (guimodule, gui) interagieren ausschließlich über DbblDelegate.
 *
 * Thread-Sicherheit:
 * - Kernsammlungen verwenden ConcurrentHashMap; Schreibvorgänge erfolgen sequentiell
 *   über Lambda-Pipelines.(Sodass es die Datenbank nicht dauerhaft geöffnet ist und nur bei nutzung) Snapshot-Schreiben erfolgt atomar.
 *
 * Beispielnutzung:
 * <pre>
 *   DbblDelegate db = DbblDelegate.createDefault();
 *   // Alle Themen abrufen
 *   java.util.List<String> themen = db.uiAllTopics().get();
 *   // Frage speichern
 *   db.questionSave().apply(new PersistenceDelegate.QuestionData(
 *       "Mathe", "Pythagoras", "a^2 + b^2 = ?", "", 
 *       java.util.List.of("c^2", "2ab"), java.util.List.of(true, false)
 *   ));
 *   // Persistieren
 *   db.persistAll().run();
 * </pre>
 */
package dbbl;