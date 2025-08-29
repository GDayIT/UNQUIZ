# 🔒 Security Policy

## 📌 Supported Versions

Diese Anwendung befindet sich aktuell in aktiver Entwicklung.  
Unterstützt wird derzeit **nur die lokale Version (v1.x)** mit **MariaDB** als Standard-Datenbank.  
Eine **.exe-Distribution** ist für die Zukunft geplant, befindet sich jedoch noch **nicht** im Release-Zyklus.

| Version | Unterstützt | Hinweise |
|---------|-------------|----------|
| v1.x (lokale DB-Version mit MariaDB) | ✅ | Aktive Wartung, Sicherheitsfixes |
| v0.x (Prototyp / Vorversion) | ❌ | Keine Unterstützung |
| .exe-Version | 🚧 | Geplant, noch nicht verfügbar |

---

## 🛡 Sicherheitsgrundsätze

Die Anwendung nutzt aktuell **MariaDB** als Standarddatenbank, kann jedoch technisch auch mit **SQLite, MySQL oder Postgres** betrieben werden.  
Der Betrieb erfolgt **lokal und passwortlos**, um Entwicklungs- und Testzwecke zu vereinfachen.  
Dennoch sollten folgende Grundsätze berücksichtigt werden:

1. **Passwortlose Konfiguration (aktuell)**  
   - Die Anwendung nutzt zurzeit **keine Datenbank-Authentifizierung**.  
   - Dies ist für den reinen **lokalen Betrieb** akzeptabel.  
   - Für produktive oder verteilte Nutzung muss künftig **eine sichere Authentifizierung** eingerichtet werden.

2. **Minimale Berechtigungen**  
   - Auch ohne Passwort sollten dedizierte DB-Benutzer eingerichtet werden, sobald Authentifizierung unterstützt wird.  
   - Keine Nutzung von `root` oder System-Admin-Konten in späteren Versionen.

3. **Eingaben validieren**  
   - Alle Benutzereingaben müssen geprüft werden, um SQL-Injection zu verhindern.  
   - Nutzung von Prepared Statements wird dringend empfohlen.

4. **Fehler- und Logging-Handling**  
   - Logs dürfen keine sensiblen Daten enthalten.  
   - Debugging sollte im Release-Build deaktiviert werden.

5. **Künftige .exe-Variante**  
   - Geplante Sicherheitsmaßnahmen:  
     - **Code-Signing** zur Verifikation der Authentizität  
     - Schutz vor Manipulation durch **Hash-Prüfung**  
     - **Update-Mechanismus** mit sicherheitsrelevanten Patches  

---

## Bekannte Einschränkungen

- Aktuell läuft die Anwendung **ohne Passwortschutz**.  
- Physischer Zugriff auf das System bedeutet vollen Zugriff auf Daten.  
- Keine Verschlüsselung gespeicherter Daten. Nutzer:innen sollten ggf. eigenständig Verschlüsselung (z. B. über Festplatten- oder Container-Encryption) einsetzen.  
- Sicherheitsfixes müssen manuell eingespielt werden (keine Auto-Updates).  

---

## Meldung von Sicherheitslücken

Falls Sie eine Sicherheitslücke finden, **bitte nicht öffentlich posten**.  
Melden Sie Probleme verantwortungsvoll über:

- GitHub [Issues](../../issues) mit Label `security` => (https://github.com/USERNAME/REPO/issues)
- oder per E-Mail an: **[quiz@security.bla]**

Wir garantieren:
1. Eingang der Meldung innerhalb von 48h  
2. Einschätzung & Zeitplan für die Behebung  
3. Veröffentlichung eines Fixes über ein Sicherheits-Release  

---

## Versionshinweise

- **v1.0.0 (aktuell)**  
  - Lokale Datenbanknutzung (MariaDB, alternativ SQLite/MySQL/Postgres).  
  - Passwortloser Betrieb.  
  - Fokus: Funktionalität, erste Sicherheitsprinzipien.  

- **Geplant: v2.0.0 (.exe-Distribution)**  
  - Einführung von **Datenbank-Authentifizierung** und Benutzerrollen.  
  - Verbesserte Sicherheit durch **Code-Signing und Distributionskontrolle**.  
  - Optionale Datenverschlüsselung und Update-Mechanismen.  
