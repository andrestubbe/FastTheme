# FastTheme v2.0 - Roadmap & TODO

## Was DEFINITIV zu FastTheme gehört

### 1) Dark/Light Mode Detection
- Windows 10/11 Registry-based
- Echtzeit-Events (Theme changed)
- System Accent Awareness

**Das ist der Kern.**

### 2) Titlebar Coloring
- Custom Titlebar Color
- Automatic Light/Dark Titlebar
- Accent-Color-Integration
- Border Color (Win11)
- Mica / Acrylic / Tabbed Titlebar (optional später)

**Das ist das sichtbare UI-Thema.**

### 3) Window Chrome Styling
- Titlebar Buttons (hover/pressed colors)
- Titlebar Text Color
- Border Thickness / Border Color
- Rounded Corners (Win11)
- Immersive Dark Mode Flag

**Das ist der "Chrome Layer".**

### 4) System Accent Color
- Lesen der Accent Color
- Lesen der Accent Color Variants (Light/Dark)
- Events bei Accent-Änderung

**Viele Apps wollen das.**

### 5) High-Level Theme API
Einfacher Zugriff für Java-Apps:
```java
FastTheme.isDarkMode();
FastTheme.getAccentColor();
FastTheme.applyTitlebar(window);
FastTheme.onThemeChanged(callback);
```
**Das ist der Developer-Ergonomie-Teil.**

---

## Optionale, aber sinnvolle Erweiterungen

Diese passen thematisch, sind aber nicht zwingend:

### 6) Mica / Acrylic / Transparent Titlebars
- Mica (Win11)
- Mica Alt
- Acrylic
- Transparent Titlebar

**Das ist "UI-Theme-Material".**

### 7) Window Corner Preferences
- Default
- Rounded
- Sharp

**Win11 API.**

### 8) System UI Theme Sync
- Auto-apply theme to your app
- Auto-apply accent
- Auto-apply titlebar style

**Das ist Komfort.**

---

## Was NICHT zu FastTheme gehört

Damit du die Grenze klar siehst:

| Feature | Gehört zu | Warum |
|---------|-----------|-------|
| DPI | FastDisplay | Hardware-abhängig |
| Resolution | FastDisplay | Monitor-abhängig |
| Refresh Rate | FastDisplay | Display-Eigenschaft |
| Orientation | FastDisplay | Display-Eigenschaft |
| Monitor-Events | FastDisplay | System-Layer |
| Window-Monitor Mapping | FastDisplay | Display-Layer |
| HDR / ICC Profile | FastDisplay | Hardware-Layer |

**FastTheme bleibt UI-Theme.**
**FastDisplay bleibt Display-System.**

---

## Fazit

FastTheme sollte enthalten:
- Theme Detection
- Accent Color
- Titlebar Coloring
- Window Chrome Styling
- Theme Events
- Optional: Mica/Acrylic, Rounded Corners

FastDisplay übernimmt:
- DPI
- Resolution
- Hz
- Orientation
- Monitor-Mapping
- Display Events

Damit hast du eine perfekte, moderne, modulare Architektur.

---

## Aktuelle Aufgaben (v2.0 Migration)

- [ ] Monitoring-Code entfernen (geht zu FastDisplay)
- [ ] ThemeListener entfernen (geht zu FastDisplay)
- [ ] JNI Methoden bereinigen
- [ ] README.md aktualisieren
- [ ] Examples aktualisieren
- [ ] Pom.xml bereinigen
- [ ] Push zu GitHub
- [ ] JitPack Release v2.0

---

## Abgrenzung zu FastDisplay

```
┌─────────────────────────────────────┐
│         FASTTHEME v2.0              │
│     UI Theme & Window Chrome          │
├─────────────────────────────────────┤
│ • Dark/Light Mode                   │
│ • Accent Color                      │
│ • Titlebar Coloring                 │
│ • Window Styling                    │
│ • Theme Events                      │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│         FASTDISPLAY                 │
│     Display & Monitor API           │
├─────────────────────────────────────┤
│ • DPI / Scaling                     │
│ • Resolution                        │
│ • Refresh Rate (Hz)                 │
│ • Orientation                       │
│ • Monitor Detection                 │
│ • Display Events                    │
└─────────────────────────────────────┘
```

**Beide Module arbeiten zusammen, bleiben aber klar getrennt.**
