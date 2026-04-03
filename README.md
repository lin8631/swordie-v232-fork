<div align="center">

# ⚔️ Swordie v232 — Private Server Setup

**MapleStory v232.2 private server with custom rates, GUI launcher, and DuckDNS auto-updater.**

*Based on the original [Swordie source](https://gitlab.com/swordiemen/swordie-232) by the Swordie team.*

![Platform](https://img.shields.io/badge/platform-Windows-blue?style=flat-square)
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![MapleStory](https://img.shields.io/badge/MapleStory-v232.2-red?style=flat-square)
![License](https://img.shields.io/badge/license-personal%20use-lightgrey?style=flat-square)

</div>

---

## ✨ Features

- 🗡️ **Version 232.2** — All jobs working (Kain, HoYoung, Lara, Adele, ...)
- 👥 Up to 400 players, stable with minimal crashes
- 🏆 Bosses implemented up to Will
- 🎯 Custom rates (EXP, Meso, Drop — easily configurable)
- 🏪 Auction House, Arcane River quests, full item progression
- 💬 Social features — guilds, friends, smegas
- 🖥️ **GUI Launcher** with login, account creation & server status
- 🌐 **DuckDNS auto-updater** — stable hostname even with dynamic IP

---

## 📋 Requirements

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 21 (Eclipse Temurin) | [adoptium.net](https://adoptium.net) |
| Maven | 3.9+ | [maven.apache.org](https://maven.apache.org/install.html) |
| MySQL Server | 8.x | [dev.mysql.com](https://dev.mysql.com/downloads/installer/) |
| MySQL Workbench | Latest | [dev.mysql.com](https://dev.mysql.com/downloads/workbench/) |
| Python | 3.11+ | [python.org](https://www.python.org/downloads/) |
| MapleStory Client | v232.2 | Steam depot (see below) |

---

## 🚀 Installation

### 1. Get the Source

```bash
git clone https://gitlab.com/swordiemen/swordie-232.git
cd swordie-232
```

### 2. Get the Game Client

Open Steam console (`Win+R` → `steam://open/console`) and run:

```
download_depot 216150 216151 4440651722208802215
```

Or use the [alternative Google Drive download](https://drive.google.com/drive/folders/12Ol6tlBjiGsuYwIkTBz7jFk1ZZFGRYof).

Also download the [client DLL pack](https://www.mediafire.com/file/v742p25aimk1n8u/Swordie-client-files.zip/file) and extract into your client folder.

### 3. Database Setup

Open MySQL Workbench and run:

```sql
CREATE SCHEMA swordie232;
```

Then run the following SQL files from the `/sql` folder **in this order**:

```
InitTables_characters.sql
InitTables_indexes.sql
InitTables_MonsterCollection.sql
Init_Admin_accounts.sql
InitTable_equip_drops.sql
InitTable_npc.sql
InitTables_cashshop.sql
InitTables_drops.sql
InitTables_shops.sql
Init_Migration_at_V79.sql
```

### 4. Configure the Server

**Database connection** — open `HikariCPDataSource.java` and set your credentials:

```java
private static final String URL = "jdbc:mysql://127.0.0.1:3306/swordie232";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_password";
```

**WZ files** — open `ServerConstants.java` and point to your game client:

```java
public static final String WZ_DIR = "C:/Path/To/Maplestory V232.2";
```

**Server IP** — open `GameConstants.java` and set your public IP:

```java
public static final byte[] SERVER_IP = new byte[]{0, 0, 0, 0}; // your public IP
```

### 5. Build

```bash
mvn clean install -DskipTests
```

### 6. Port Forwarding

Forward these ports to your server machine (TCP):

| Ports | Purpose |
|-------|---------|
| `8484` | Login server |
| `8585–8594` | Game channels (1–10) |
| `3000` | Web API |

---

## ▶️ Running the Server

```bash
# Windows
start-server.bat
```

First launch parses all WZ files — this takes a few minutes. Subsequent launches are near-instant.

When you see `Finished loading server`, the server is ready.

---

## 🎮 Launcher (for players)

The `launcher/` folder contains a Python GUI launcher for players to connect.

### Setup

1. Install Python 3.11+ (tick **"Add to PATH"** during install)
2. Open `launcher/launcher.py` and configure:

```python
SERVER_IP = "your-subdomain.duckdns.org"
GAME_PATH = r"C:\Path\To\Maplestory V232.2\MapleStory.exe"
```

3. Double-click **`Launch Swordie.bat`** — dependencies install automatically

### Features
- 🟢 Live server status indicator
- 🔐 Login with username & password
- 📝 Account registration built-in
- 🚀 Auto-launches game with auth token

---

## 🌐 DuckDNS Auto-Updater

Keeps your hostname pointing to your current IP even when it changes.

1. Sign up at [duckdns.org](https://www.duckdns.org)
2. Create a subdomain and copy your token
3. Edit `duckdns/update-ip.ps1`:

```powershell
$SUBDOMAIN = "your-subdomain"
$TOKEN      = "your-token-here"
```

4. Right-click `duckdns/install-task.bat` → **Run as Administrator**

This registers a Windows task that auto-updates every 5 minutes.

---

## ⚙️ Custom Rates

Edit `GameConstants.java`:

```java
public static final int COMBO_ORB_EXP_RATE = 10;  // EXP orb multiplier
public static final int MOB_DROP_RATE       = 10;  // Drop rate multiplier
public static final int MOB_MESO_RATE       = 10;  // Meso multiplier
```

Edit `MobExpConstants.java` for mob EXP:

```java
public static final int MOB_EXP_BASE_RATE = 10;
```

Rebuild after any changes:
```bash
mvn clean install -DskipTests
```

---

## 🛠️ Admin Commands

Log in with the `admin` account (userId 1). Type `!help` in-game for the full command list, or browse `AdminCommands.java` in the source.

---

## 📁 Repository Structure

```
├── launcher/               # Python GUI launcher for players
│   ├── launcher.py
│   ├── Launch Swordie.bat
│   └── requirements.txt
├── duckdns/                # DuckDNS IP auto-updater
│   ├── update-ip.ps1
│   └── install-task.bat
├── start-server.bat        # Server startup script
└── README.md
```

---

## ⚠️ Disclaimer

This is based on the publicly released [Swordie source](https://gitlab.com/swordiemen/swordie-232) for personal/educational use only. MapleStory is a trademark of Nexon. This project is not affiliated with or endorsed by Nexon.

---

<div align="center">

Made with ❤️ for the MapleStory community

</div>
