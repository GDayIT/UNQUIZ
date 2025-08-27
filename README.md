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
- 🧠 **Quizfragen erstellen**: Benutzer können eigene Fragen mit Antwortmöglichkeiten definieren.
- 🖼️ **Modulare GUI**: Panels zur Frageerstellung, Listenanzeige und Navigation.
- 🗃️ **Business-Logik integriert**: Datenverarbeitung und Speicherung in einer Zwei-Tier-Architektur.
- 📦 **Java Modularisierung**: Verwendung von `module-info.java` zur Strukturierung.
- 📚 **Leitner-Lernsystem (Adaptive Learning)**:
  - Integriertes **Adaptive Leitner System** zum wiederholten Lernen mit Karteikarten.  
                                                   → Fragen wandern zwischen Stufen, abhängig davon, ob sie richtig oder falsch beantwortet wurden.  
                                                   → Unterstützt langfristiges Lernen durch Wiederholung in optimalen Intervallen.                                                                                              → Schwierigkeit anpassbar in den **Einstellungen**.
- 📊 **Statistik & Fortschrittsanzeige**:

   Umfangreiche Statistiken über Lernerfolge:  
  - Anzahl beantworteter Fragen  
  - Richtig/Falsch-Quoten  
  - Lernfortschritt pro Thema  
  - Übersicht über Leitner-Stufen   
- 🖥️ **Benutzerfreundliche Oberfläche (Swing GUI)**: Eine modulare Benutzeroberfläche mit klar strukturierten Panels für Quiz, Statistiken und Einstellungen.  
- 🎮 **Anpassbare Spielmodi**: Verschiedene Quiz-Modi, inkl. Zeitlimit, Filter nach Themen oder Schwierigkeitsgrad.  


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

---

> ⚠️ Hinweis:  
> Die aktuelle Version basiert auf Java und wird über die Entwicklungsumgebung gestartet.  
> In einer zukünftigen Version wird eine ausführbare `.exe`-Datei verfügbar sein, die alle Funktionen als eigenständige Windows-Anwendung bereitstellt – ohne zusätzliche Installation von Java oder IDE.

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
📧 GitHub-Profil  
📧 optional: email.bla@fake.de




