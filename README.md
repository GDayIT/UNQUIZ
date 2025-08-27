# 📘 UNQUIZ

What is it?! 
>It's a Game!
> 
>It's a learn Experience..
>
>It's UNQUIZ!


## UNQUIZ

***** UNQUIZ ist eine modulare Java-Anwendung zur Erstellung und Verwaltung von Quizfragen. Sie bietet eine intuitive grafische Benutzeroberfläche (GUI) und eine einfache Datenlogik zur Speicherung und Verarbeitung von Fragen. Die Statistik hilft dir den überblick über deine erfolge zu erhalten. 

---

## 🎯 Features
- 🧠 **Quizfragen erstellen**  
  Benutzer können eigene Fragen mit Antwortmöglichkeiten definieren.  

- 🖼️ **Modulare GUI**  
  Panels zur Frageerstellung, Listenanzeige und Navigation.  

- 🗃️ **Business-Logik integriert**  
  Datenverarbeitung und Speicherung in einer Zwei-Tier-Architektur.  

- 📦 **Java Modularisierung**  
  Verwendung von `module-info.java` zur sauberen Strukturierung.  
  - Umsetzung durch **Delegation** und **Lambda-Zuweisungen**, um Logik klar voneinander zu trennen.  
  - Klassen greifen über Delegations-Interfaces auf Services zu → fördert Testbarkeit und Erweiterbarkeit.  

- 📚 **Leitner-Lernsystem (Adaptive Learning)**  
  - Integriertes **Adaptive Leitner System** zum wiederholten Lernen mit Karteikarten.  
  - Fragen wandern zwischen Stufen, abhängig von richtigen oder falschen Antworten.  
  - Unterstützt langfristiges Lernen durch Wiederholung in optimalen Intervallen.  
  - Schwierigkeit kann in den **Einstellungen** angepasst werden.  

- 📊 **Statistik & Fortschrittsanzeige**  
  Umfangreiche Statistiken über Lernerfolge:  
  - Anzahl beantworteter Fragen  
  - Richtig/Falsch-Quoten  
  - Lernfortschritt pro Thema  
  - Übersicht über Leitner-Stufen   

- 🖥️ **Benutzerfreundliche Oberfläche (Swing GUI)**  
  Eine Modulare Benutzeroberflächen mit klar strukturierten Panels für Quiz, Statistiken und Einstellungen. 

- 🎮 **Anpassbare Spielmodi**  
  Verschiedene Quiz-Modi, inkl. Filter nach Themen oder Schwierigkeitsgrad.  

---


## 🛠️ Projektstruktur

```plaintext
UNQUIZ/
├──────────── .github/workflows/
│                       ├── blocking_excel_change.yml
│                       └── Admin-only.yml
│  
├──────────── src/
│   ├── dbbl/
│   │   ├── BusinesslogicalDelegation.java
│   │   ├── DatenBankDAOxDTO.java
│   │   ├── DbblDelegation.java
│   │   ├── ModularBusinessController.java
│   │   ├── ModularPersistenceService.java
│   │   ├── package-info.java
│   │   ├── PersistenceDelegation.java
│   │   └── RepoQuizeeQuestions.java
│   │
│   ├── dbbl.migration/
│   │   └──DataMergeService.java
│   │
│   ├── gui/
│   │   ├── Frame.java
│   │   ├── GuiDelegation.java
│   │   └── package-info.java
│   │
│   │── guimodule/
│   │   ├── AdaptiveLeitnerCard.java
│   │   ├── AdaptiveLeitnerSystem.java
│   │   ├── AppConfigService.java
│   │   ├── ConsolenCommandService.java
│   │   ├── CreateQuizQuestionListPanel.java
│   │   ├── CreateQuizQuestionsPanel.java
│   │   ├── DateRange.java
│   │   ├── FilterChangeEvent.java
│   │   ├── FilterCriteria.java
│   │   ├── GuiDelegation.java
│   │   ├── GuiModuleDelegation.java
│   │   ├── ModularLookAndFeelService.java
│   │   ├── ModularQuizPlay.java
│   │   ├── ModularQuizStatistics.java
│   │   ├── ModularSortingService.java
│   │   ├── ModularStatisticsPanel.java
│   │   ├── ModularStyleService.java
│   │   ├── package-info.java
│   │   ├── PnlForming.java
│   │   ├── QuizApplicationDemo.java
│   │   ├── QuizApplicationManager.java
│   │   ├── QuizDataMapper.java
│   │   ├── QuizFormData.java
│   │   ├── QuizQuestion.java
│   │   ├── SortCriteria.java
│   │   ├── SortDirection.java
│   │   ├── SortingChangeEvent.java
│   │   ├── SortingConfiguration.java
│   │   ├── SortingDelegate.java
│   │   ├── SortType.java
│   │   ├── StyleDelegation.java
│   │   └── Theme.java
│   │
│   │── guimodule.achievements/
│   │   └── AchievementsService.java
│   │
│   ├── guimodule.tests/
│   │   └── TestSuite.java
│   │
│   ├── guimoduleComponents/
│   │   └── Statistics.java
│   │
│   └── module-info.java
├── bin/ (vom Tracking ausgeschlossen(Daher im .gitignore))
├── .classpath/
├── .gitignore/
├── .project / .settings/
├── CODEOWNERS
├── LICENSE
├── ProjektDocumentation/ (Beinhaltet alle im laufe des Projekts Dokumentierten Inhalte(QL-UC-0-3 + Prj-UC-0-1, UML, Storyboards, Qualitätssicherung(Vollwertige und Umfängliche Tests), (Allgemeines zum Projekt, Projektmanagement, Status und Tage)
├── README.md
└── SECURITY.md
```

---

> [!WARNING]
> ⚠️ [Achtung / Hinweis] 
> Dieses Projekt befindet sich noch in der Entwicklung. Funktionen, Schnittstellen und Datenstrukturen können sich ändern.  
> Bitte verwende die Anwendung mit Vorsicht und melde unerwartetes Verhalten über ein Issue.



## 🚀 Installation & Ausführung

### 📥 Projekt klonen

```bash
git clone https://github.com/dein-nutzername/UNQUIZ.git
```
> ⚠️ Hinweis:
> Dieses Projekt benötigt **Java JDK 24** und Eclipse als Entwicklungsumgebung.
>
> ## Voraussetzungen
>
> - [Eclipse IDE](https://www.eclipse.org/downloads/) (mind. Version 2024-xx oder neuer)
> - [JDK 24](https://jdk.java.net/24/) (offizielle OpenJDK-Builds)
>
> ## Installation JDK 24
>
> 1. Lade das JDK 24 passend zu deinem Betriebssystem herunter:  
>    👉 [https://jdk.java.net/24/](https://jdk.java.net/24/)
>
> 2. Entpacke die heruntergeladene Datei (z. B. nach `C:\java\jdk-24` oder `/usr/lib/jvm/jdk-24`).
>
> 3. Setze die Umgebungsvariable `JAVA_HOME`:  
>    - **Windows (PowerShell)**:
>      ```powershell
>      setx JAVA_HOME "C:\java\jdk-24"
>      setx PATH "%JAVA_HOME%\bin;%PATH%"
>      ```
>    - **Linux / macOS (bash/zsh)**:
>      ```bash
>      export JAVA_HOME=/usr/lib/jvm/jdk-24
>      export PATH=$JAVA_HOME/bin:$PATH
>      ```
> 
> 4. Prüfe die Installation:
>    ```bash
>    java -version
>    ```
> Die Entwicklungsumgebung steht für Linux, macOS, Windows zuverfügung.

### Danach falls nicht schon vorhande!
```bash

🧩 In Eclipse importieren
Öffne Eclipse.
Gehe zu File → Import....
Wähle Existing Projects into Workspace.
Wähle den geklonten Ordner ProtoQuizz.
Bestätige mit Finish.
▶️ Starten
Öffne die Datei Frame.java im Paket gui.
Führe die Datei aus (Run As → Java Application).
Die grafische Oberfläche startet und du kannst Quizfragen erstellen und verwalten.

```

## 🧪 Tests

Das Projekt enthält verschiedene Testebenen, um Stabilität und Funktionalität sicherzustellen. 
Es wird **JUnit 5 (JUnit Jupiter)** für automatisierte Tests genutz.  
Alle Tests liegen im Package: org.junit.jupiter.java
Die zentrale Testklasse lautet: Test.java

---

### ✅ Testarten

- **Unit Tests (Unittests)**  
  Testen einzelne Klassen/Methoden isoliert.  
  → Ziel: Korrektheit der kleinsten Funktionseinheiten (`Test.java` enthält Beispieltests).   

- **Integration Tests**  
  Überprüfen das Zusammenspiel mehrerer Module (z. B. GUI ↔ Logik ↔ Datenbank/Persistenz).  
  → Ziel: Sicherstellen, dass Komponenten korrekt interagieren.  

- **Functional Tests (Funktionstests)**  
  Testen, ob die Anwendung die fachlichen Anforderungen erfüllt (z. B. Quiz starten, Karteikarten lernen).
  → Beispiel: „Kann ein Quiz erstellt und gestartet werden?“  

- **Smoke Tests**  
  Kurze, oberflächliche Tests nach einem Build.  
  → Ziel: Prüfen, ob die Anwendung grundsätzlich startet und keine kritischen Fehler enthält.  

- **End-to-End Tests (E2E)**  
  Simulieren den Ablauf aus Benutzersicht (z. B. Quiz starten, Fragen beantworten, Ergebnisse sehen).  
  → Ziel: Sicherstellen, dass der gesamte Workflow wie erwartet funktioniert.  

---

### ▶️ Tests ausführen

Falls du **JUnit 5** in Eclipse verwendest:

1. Rechtsklick auf den `test`-Ordner oder eine Testklasse.  
2. Wähle **Run As → JUnit Test**.  
3. Ergebnisse werden in der **JUnit-View** angezeigt.
4. Ergebnisse erscheinen in der **JUnit-Ansicht** (grün = bestanden, rot = fehlgeschlagen).  

#### 🔹 Über die Konsole (Maven/Gradle):

- **Maven**:
  ```bash
  mvn test
  
  ./gradlew test


---

> ⚠️ Hinweis:  
> Die aktuelle Version basiert auf Java und wird über die Entwicklungsumgebung gestartet.  
> In einer zukünftigen Version wird eine ausführbare `.exe`-Datei verfügbar sein, die alle Funktionen als eigenständige Windows-Anwendung bereitstellt – ohne zusätzliche Installation von Java oder IDE.
> 

---


## 🤝 Mitwirken

Pull Requests sind willkommen!  

Wenn du größere Änderungen vorschlagen möchtest, eröffne bitte zuerst ein Issue, um darüber zu diskutieren.


Ich rfeue mich über Beiträge in folgenden Bereichen:
- Neue Features oder Panels
- Verbesserungen der GUI
- Bugfixes und Refactoring
- Dokumentation und Beispiele

---

## 📬 Kontakt

Bei Fragen, Ideen oder Feedback kannst du dich gerne melden:

**D.Georgiou**  
📧 GitHub-Profil: GDayIT
📧 optional: email.bla@fake.de




