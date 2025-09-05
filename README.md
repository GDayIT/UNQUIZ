# 📘 UNQUIZ

What is it?! 
>It's a Game!
> 
>It's a learn Experience..
>
>It's UNQUIZ!

---


## Worin sich mein Code Auszeichnet?!

1.
- 🧩**Modulare Architektur**
2.
- 📊**Datengetriebene Features**
3.
- 🎯**Klare Verantwortlichkeiten**
4.
- 🚀**Potenzial für Erweiterungen**

---

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

- 📚 **Leitner-Lernsystem (Adaptive Learning(NeroLeitner)**  
  - Integriertes **Adaptive Leitner System/NeroLeitner** zum wiederholten Lernen mit Karteikarten.
  - Das Leitner System wurde von mir angepasst, sodass es sich nach modernen neuropsychologischen und lernpsychologischen Konzepten richtet.
  - Fragen wandern zwischen Stufen, abhängig von richtigen oder falschen Antworten.  
  - Unterstützt langfristiges Lernen durch Wiederholung in optimalen Intervallen.  
  - Schwierigkeit kann in den **Einstellungen** angepasst werden (dynamisch anpassbar in drei Stufen).
  -  📦 Boxenstruktur & Intervalle
    
    | Box | Zustand          | Wiederholungsintervall | Lernziel                         |
    |-----|------------------|-------------------------|----------------------------------|
    | 1   | Neu / Falsch     | 1 Tag                  | Erste Festigung                  |
    | 2   | Unsicher         | 2–3 Tage               | Wiederholung mit Feedback        |
    | 3   | Teilweise sicher | 5–6 Tage               | Konsolidierung                   |
    | 4   | Sicher           | 10–12 Tage             | Automatisierung                  |
    | 5   | Sehr sicher      | 20–25 Tage             | Langzeitgedächtnis               |
    | 6   | Mastered         | 40–60 Tage             | Erhalt & Transfer                |

👉 Durch Aktivieren des **Leitner-Modus** werden die Inhalte automatisch nach dem **NeroLeitner-System** angepasst und spielbar gemacht – entweder für ein ausgewähltes Thema oder für alle Themen.

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
├── ProjektDocumentation/Projektmanagement/Storyboards/Use Cases/Klassendiagramm(ERD)/Qualitätssicherung         (Beinhaltet alle Projekt Dokumente((ohne Pflichtenheft!)  (QL-UC-0-3 + Prj-UC-0-1, UML, Storyboards, Qualitätssicherung(Vollwertige und Umfängliche Tests),(Allgemeines zum Projekt, Projektmanagement, Status und Tage))))
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
├── DatabankStruc.txt
├── LICENSE
├── README.md
├── SECURITY.md
├── quiz_questions.dat
└── sorting_config.dat
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
Wähle den geklonten Ordner UNQUIZ.
Bestätige mit Finish.
▶️ Starten
Öffne die Datei Frame.java im Paket gui.
Führe die Datei aus (Run As → Java Application).
Die grafische Oberfläche startet und du kannst Quizfragen erstellen und verwalten.

```

## 🧪 Tests

Das Projekt enthält verschiedene Testebenen, um Stabilität und Funktionalität sicherzustellen. 
Es wird **JUnit 5 (JUnit Jupiter)** für automatisierte Tests genutz.  

```

Alle Tests liegen im Package: org.junit.jupiter.java
Die zentrale Testklasse lautet: Test.java
```
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
---

- **Gradle**:
  ```bash
  ./gradlew test
---

> ⚠️ Hinweis 
> ⚠️ Hinweis 
> ⚠️ Hinweis:  
> - Die aktuelle Version basiert auf Java und wird über die Entwicklungsumgebung gestartet.  
> - In einer zukünftigen Version wird eine ausführbare `.exe`-Datei verfügbar sein, die alle Funktionen als eigenständige Windows-Anwendung bereitstellt – ohne zusätzliche Installation von Java oder IDE.
> - Eine Dynamische Web anbindung und eine funktionierende Datenbank sollen in zukunft beinhaltet sein gesondert in die dafür vorgesehenen Ordner.
> - Die Funktionalitäten sind für ein zukunftigen Release vorgesehen.
---
---
> ⚠️ Hinweis
> - Release in progress
 
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
---
## 📬 Kontakt

Bei Fragen, Ideen oder Feedback kannst du dich gerne melden:

**D.Georgiou**  
📧 GitHub-Profil: GDayIT
---
📧 optional: email.bla@fake.de
---
---



<details>
  <summary>▶️ Mehr Infos anzeigen</summary>

  Hier steht nix **EasterEgg** running gag.  
  :

  - Schau
  - 🥁 👇​ 🥁
  - 🥁 👌​ 🥁(Bongoloch)

  ```java
  // wenn es zu viel war..
  System.out.println("Hello Security!");
```
---

#For English User


<details>
  <summary>▶️ English version!</summary>
# 📘 UNQUIZ

What is it?!  
> It's a Game!  
> It's a Learning Experience...  
> It's UNQUIZ!

---

## What makes my code special?!

1. 🧩 **Modular Architecture**
2. 📊 **Data-Driven Features**
3. 🎯 **Clear Responsibilities**
4. 🚀 **Potential for Extensions**

---

## UNQUIZ

***** UNQUIZ is a modular Java application for creating and managing quiz questions. It provides an intuitive graphical user interface (GUI) and simple data logic for storing and processing questions. The statistics help you keep track of your progress.  

---

## 🎯 Features

- 🧠 **Create Quiz Questions**  
  Users can define their own questions with answer options.  

- 🖼️ **Modular GUI**  
  Panels for question creation, list display, and navigation.  

- 🗃️ **Integrated Business Logic**  
  Data processing and storage in a two-tier architecture.  

- 📦 **Java Modularization**  
  Uses `module-info.java` for clean structure.  
  - Implemented via **Delegation** and **Lambda Assignments** to clearly separate logic.  
  - Classes access services via delegation interfaces → promotes testability and extensibility.  

- 📚 **Leitner Learning System (Adaptive Learning / NeroLeitner)**  
  - Integrated **Adaptive Leitner System / NeroLeitner** for repeated learning with flashcards.  
  - Adapted according to modern neuropsychological and learning psychology concepts.  
  - Questions move between levels depending on correct or wrong answers.  
  - Supports long-term learning via optimal repetition intervals.  
  - Difficulty adjustable in **Settings** (three dynamic levels).  
  - 📦 Box structure & intervals  

    | Box | Status           | Repetition Interval | Learning Goal                  |
    |-----|-----------------|-------------------|--------------------------------|
    | 1   | New / Wrong     | 1 Day             | Initial reinforcement           |
    | 2   | Unsure          | 2–3 Days          | Repetition with feedback        |
    | 3   | Partially sure  | 5–6 Days          | Consolidation                   |
    | 4   | Sure            | 10–12 Days        | Automation                      |
    | 5   | Very Sure       | 20–25 Days        | Long-term memory                |
    | 6   | Mastered        | 40–60 Days        | Retention & transfer            |

👉 By enabling **Leitner Mode**, content is automatically adapted to the **NeroLeitner System** — playable for a selected topic or all topics.

- 📊 **Statistics & Progress Tracking**  
  Comprehensive stats about learning progress:  
  - Number of answered questions  
  - Correct/incorrect ratios  
  - Progress per topic  
  - Overview of Leitner levels  

- 🖥️ **User-Friendly Interface (Swing GUI)**  
  Modular GUI with clearly structured panels for quizzes, statistics, and settings.

- 🎮 **Customizable Game Modes**  
  Various quiz modes, including filtering by topic or difficulty level.

---

## 🛠️ Project Structure

```plaintext
UNQUIZ/
├──────────── .github/workflows/
│                       ├── blocking_excel_change.yml
│                       └── Admin-only.yml
│ 
├── ProjektDocumentation/Projektmanagement/Storyboards/Use Cases/Klassendiagramm(ERD)/Qualitätssicherung         (Beinhaltet alle Projekt Dokumente((ohne Pflichtenheft!)  (QL-UC-0-3 + Prj-UC-0-1, UML, Storyboards, Qualitätssicherung(Vollwertige und Umfängliche Tests),(Allgemeines zum Projekt, Projektmanagement, Status und Tage))))
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
├── DatabankStruc.txt
├── LICENSE
├── README.md
├── SECURITY.md
├── quiz_questions.dat
└── sorting_config.dat
```

---

> [!WARNING]
> ⚠️ Note:  
> This project is still under development. Features, interfaces, and data structures may change.  
> Please use the application carefully and report unexpected behavior via an issue.

## 🚀 Installation & Execution

### 📥 Clone the Project

```bash
git clone https://github.com/your-username/UNQUIZ.git
```

> ⚠️ Note:
> This project requires **Java JDK 24** and Eclipse IDE.
>
> ## Prerequisites
>
> - [Eclipse IDE](https://www.eclipse.org/downloads/) (version 2024.xx or newer)
> - [JDK 24](https://jdk.java.net/24/) (official OpenJDK builds)
>
> ## Install JDK 24
>
> 1. Download JDK 24 for your operating system:  
>    👉 [https://jdk.java.net/24/](https://jdk.java.net/24/)
>
> 2. Extract the downloaded file (e.g., `C:\java\jdk-24` or `/usr/lib/jvm/jdk-24`).
>
> 3. Set the environment variable `JAVA_HOME`:  
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
> 4. Verify the installation:
>    ```bash
>    java -version
>    ```
> The development environment is available for Linux, macOS, and Windows.

### If not already done!
```bash
🧩 Import into Eclipse
Open Eclipse.
Go to File → Import....
Select Existing Projects into Workspace.
Choose the cloned UNQUIZ folder.
Confirm with Finish.
▶️ Run
Open the file Frame.java in the gui package.
Run the file (Run As → Java Application).
The GUI starts, and you can create and manage quiz questions.

```

## 🧪 Tests

The project includes multiple test levels to ensure stability and functionality.  
**JUnit 5 (JUnit Jupiter)** is used for automated tests.
```

All tests are located in the package: org.junit.jupiter.java
The main test class is: Test.java

---

### ✅ Test Types

- **Unit Tests**  
  Test individual classes/methods in isolation.  
  → Goal: Correctness of the smallest functional units (`Test.java` contains example tests).   

- **Integration Tests**  
  Check the interaction between multiple modules (e.g., GUI ↔ Logic ↔ Database).  
  → Goal: Ensure components interact correctly.  

- **Functional Tests**  
  Verify the application meets functional requirements (e.g., start a quiz, learn flashcards).  
  → Example: "Can a quiz be created and started?"  

- **Smoke Tests**  
  Short, superficial tests after a build.  
  → Goal: Ensure the application starts and has no critical errors.  

- **End-to-End Tests (E2E)**  
  Simulate the workflow from the user's perspective (e.g., start a quiz, answer questions, view results).  
  → Goal: Ensure the entire workflow functions as expected.  

---

### ▶️ Running Tests

If using **JUnit 5** in Eclipse:

1. Right-click the `test` folder or a test class.  
2. Select **Run As → JUnit Test**.  
3. Results appear in the **JUnit View**.  
4. Results appear in the **JUnit view** (green = passed, red = failed).  

#### 🔹 Via Console (Maven/Gradle):

- **Maven**:
```bash
mvn test
---

All tests are located in the package: org.junit.jupiter.java  
The main test class is: Test.java

---

- **Gradle**:
```bash
./gradlew test
---

All tests are located in the package: org.junit.jupiter.java  
The main test class is: Test.java

---

> ⚠️ Note  
> ⚠️ Note  
> ⚠️ Note:  
> - The current version is Java-based and runs via the IDE.  
> - In a future version, a standalone `.exe` will be available, providing all features as a Windows application without requiring Java or IDE.  
> - Dynamic web connectivity and a working database are planned for future releases in the designated folders.  
> - Features are intended for a future release.
---
---
> ⚠️ Note
> - Release in progress

---

## 🤝 Contributing

Pull requests are welcome!  

For major changes, please open an issue first to discuss.

I welcome contributions in the following areas:

- New features or panels
- GUI improvements
- Bug fixes and refactoring
- Documentation and examples

---
---
## 📬 Contact

For questions, ideas, or feedback, feel free to contact:

**D.Georgiou**  
📧 GitHub Profile: GDayIT
---
📧 Optional: email.bla@fake.de
---
---

<details>
  <summary>▶️ More Info</summary>

  Nothing special here **EasterEgg** running gag.  

  - Look
  - 🥁 👇 🥁
  - 🥁 👌 🥁 (Bongoloch)

  ```java
  // If it was too much..
  System.out.println("Hello Security!");


---
