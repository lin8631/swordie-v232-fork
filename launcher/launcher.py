import tkinter as tk
from tkinter import ttk, messagebox, filedialog
import requests
import subprocess
import threading
import os
import json
import socket

SERVER_IP = "127.0.0.1"
API_PORT = 3000

# Config file to remember game path between launches
CONFIG_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "config.json")

def load_config():
    if os.path.exists(CONFIG_FILE):
        try:
            with open(CONFIG_FILE) as f:
                return json.load(f)
        except:
            pass
    return {}

def save_config(data):
    with open(CONFIG_FILE, "w") as f:
        json.dump(data, f)

def get_game_path():
    return load_config().get("game_path", "")

def set_game_path(path):
    cfg = load_config()
    cfg["game_path"] = path
    save_config(cfg)

def get_api_base():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.settimeout(1)
        s.connect(("127.0.0.1", API_PORT))
        s.close()
        return f"http://127.0.0.1:{API_PORT}/api"
    except:
        return f"http://{SERVER_IP}:{API_PORT}/api"

API_BASE = get_api_base()

BG     = "#1a1a2e"
PANEL  = "#16213e"
ACCENT = "#e94560"
ACCENT2 = "#0f3460"
TEXT   = "#eaeaea"
SUBTEXT = "#a0a0b0"
GREEN  = "#4caf50"


class SwordieLauncher(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Swordie v232 Launcher")
        self.geometry("480x580")
        self.resizable(False, False)
        self.configure(bg=BG)
        self.current_frame = None
        # Show setup screen first if no game path saved
        if get_game_path():
            self.show_frame(LoginFrame)
        else:
            self.show_frame(SetupFrame)

    def show_frame(self, frame_class, **kwargs):
        if self.current_frame:
            self.current_frame.destroy()
        self.current_frame = frame_class(self, **kwargs)
        self.current_frame.pack(fill="both", expand=True)


class SetupFrame(tk.Frame):
    """First-time setup: locate MapleStory.exe"""
    def __init__(self, parent):
        super().__init__(parent, bg=BG)
        self.parent = parent
        self._build()

    def _build(self):
        tk.Label(self, text="⚔  SWORDIE", font=("Segoe UI", 26, "bold"),
                 bg=BG, fg=ACCENT).pack(pady=(40, 4))
        tk.Label(self, text="First-time setup", font=("Segoe UI", 10),
                 bg=BG, fg=SUBTEXT).pack(pady=(0, 30))

        card = tk.Frame(self, bg=PANEL, padx=30, pady=30)
        card.pack(padx=40, fill="x")

        tk.Label(card, text="Locate your MapleStory v232.2 client",
                 font=("Segoe UI", 11, "bold"), bg=PANEL, fg=TEXT).pack(anchor="w")
        tk.Label(card, text="Browse to the folder where MapleStory.exe is located.",
                 font=("Segoe UI", 9), bg=PANEL, fg=SUBTEXT, wraplength=380).pack(anchor="w", pady=(4, 16))

        path_row = tk.Frame(card, bg=PANEL)
        path_row.pack(fill="x", pady=(0, 6))

        self.path_var = tk.StringVar(value="No file selected")
        path_label = tk.Label(path_row, textvariable=self.path_var, font=("Segoe UI", 8),
                              bg=PANEL, fg=SUBTEXT, wraplength=280, anchor="w", justify="left")
        path_label.pack(side="left", fill="x", expand=True)

        tk.Button(path_row, text="Browse", font=("Segoe UI", 9),
                  bg=ACCENT2, fg=TEXT, relief="flat", cursor="hand2",
                  activebackground="#1a4a8a", activeforeground=TEXT,
                  command=self._browse).pack(side="right")

        self.status = tk.Label(card, text="", font=("Segoe UI", 9),
                               bg=PANEL, fg=ACCENT, wraplength=380)
        self.status.pack(fill="x", pady=(10, 10))

        self.continue_btn = tk.Button(card, text="CONTINUE", font=("Segoe UI", 12, "bold"),
                                      bg=ACCENT, fg="white", relief="flat",
                                      activebackground="#c73652", activeforeground="white",
                                      cursor="hand2", command=self._continue, height=2,
                                      state="disabled")
        self.continue_btn.pack(fill="x")

    def _browse(self):
        path = filedialog.askopenfilename(
            title="Select MapleStory.exe",
            filetypes=[("MapleStory Executable", "MapleStory.exe"), ("All files", "*.*")]
        )
        if path:
            self.selected_path = path.replace("/", "\\")
            display = os.path.dirname(self.selected_path)
            self.path_var.set(display)
            self.continue_btn.config(state="normal")
            self.status.config(text="")

    def _continue(self):
        if not hasattr(self, "selected_path") or not os.path.exists(self.selected_path):
            self.status.config(text="Please select a valid MapleStory.exe file.")
            return
        set_game_path(self.selected_path)
        self.parent.show_frame(LoginFrame)


class LoginFrame(tk.Frame):
    def __init__(self, parent):
        super().__init__(parent, bg=BG)
        self.parent = parent
        self._build()

    def _build(self):
        tk.Label(self, text="⚔  SWORDIE", font=("Segoe UI", 26, "bold"),
                 bg=BG, fg=ACCENT).pack(pady=(40, 4))
        tk.Label(self, text="MapleStory Private Server v232.2",
                 font=("Segoe UI", 10), bg=BG, fg=SUBTEXT).pack(pady=(0, 30))

        card = tk.Frame(self, bg=PANEL, padx=30, pady=30)
        card.pack(padx=40, fill="x")

        tk.Label(card, text="Username", font=("Segoe UI", 10, "bold"),
                 bg=PANEL, fg=TEXT, anchor="w").pack(fill="x")
        self.username = ttk.Entry(card, font=("Segoe UI", 11))
        self.username.pack(fill="x", pady=(4, 14), ipady=6)

        tk.Label(card, text="Password", font=("Segoe UI", 10, "bold"),
                 bg=PANEL, fg=TEXT, anchor="w").pack(fill="x")
        self.password = ttk.Entry(card, font=("Segoe UI", 11), show="•")
        self.password.pack(fill="x", pady=(4, 20), ipady=6)

        self.status = tk.Label(card, text="", font=("Segoe UI", 9),
                               bg=PANEL, fg=ACCENT, wraplength=380)
        self.status.pack(fill="x", pady=(0, 10))

        self.login_btn = tk.Button(card, text="PLAY", font=("Segoe UI", 12, "bold"),
                                   bg=ACCENT, fg="white", relief="flat",
                                   activebackground="#c73652", activeforeground="white",
                                   cursor="hand2", command=self._login, height=2)
        self.login_btn.pack(fill="x", pady=(0, 10))

        tk.Button(card, text="Create Account", font=("Segoe UI", 9),
                  bg=ACCENT2, fg=TEXT, relief="flat", cursor="hand2",
                  activebackground="#1a4a8a", activeforeground=TEXT,
                  command=lambda: self.parent.show_frame(RegisterFrame)).pack(fill="x", pady=(0, 6))

        tk.Button(card, text="⚙ Change game path", font=("Segoe UI", 8),
                  bg=PANEL, fg=SUBTEXT, relief="flat", cursor="hand2",
                  activebackground=PANEL, activeforeground=TEXT,
                  command=lambda: self.parent.show_frame(SetupFrame)).pack(fill="x")

        status_row = tk.Frame(self, bg=BG)
        status_row.pack(pady=20)
        self.dot = tk.Label(status_row, text="●", font=("Segoe UI", 10), bg=BG, fg=SUBTEXT)
        self.dot.pack(side="left")
        self.server_label = tk.Label(status_row, text="  Checking server...",
                                     font=("Segoe UI", 9), bg=BG, fg=SUBTEXT)
        self.server_label.pack(side="left")

        self.username.bind("<Return>", lambda e: self.password.focus())
        self.password.bind("<Return>", lambda e: self._login())
        self.username.focus()

        threading.Thread(target=self._check_server, daemon=True).start()

    def _check_server(self):
        try:
            r = requests.get(f"{API_BASE}/status", timeout=4)
            if r.status_code == 200:
                data = r.json()
                players = data.get("Playercount", 0)
                self.after(0, lambda: self.dot.config(fg=GREEN))
                self.after(0, lambda: self.server_label.config(
                    text=f"  Server online — {players} player{'s' if players != 1 else ''} online", fg=GREEN))
            else:
                raise Exception()
        except:
            self.after(0, lambda: self.dot.config(fg=ACCENT))
            self.after(0, lambda: self.server_label.config(
                text="  Server offline or unreachable", fg=ACCENT))

    def _login(self):
        username = self.username.get().strip()
        password = self.password.get().strip()
        if not username or not password:
            self.status.config(text="Please enter username and password.")
            return
        self.login_btn.config(state="disabled", text="Logging in...")
        self.status.config(text="")
        threading.Thread(target=self._do_login, args=(username, password), daemon=True).start()

    def _do_login(self, username, password):
        try:
            r = requests.post(f"{API_BASE}/login",
                              json={"username": username, "password": password},
                              timeout=8)
            if r.status_code == 200:
                token = r.json().get("token")
                self.after(0, lambda: self._launch(token))
            elif r.status_code == 403:
                self.after(0, lambda: self._error("Invalid username or password."))
            else:
                self.after(0, lambda: self._error(f"Server error: {r.status_code}"))
        except requests.ConnectionError:
            self.after(0, lambda: self._error("Cannot connect to server. Is it online?"))
        except Exception as e:
            self.after(0, lambda: self._error(f"Error: {e}"))

    def _launch(self, token):
        game_path = get_game_path()
        self.status.config(text="Launching game...", fg=GREEN)
        try:
            import ctypes
            ctypes.windll.shell32.ShellExecuteW(
                None, "open", game_path, f"WebStart {token}",
                os.path.dirname(game_path), 1
            )
            self.after(2000, self.parent.destroy)
        except FileNotFoundError:
            self._error("MapleStory.exe not found. Click '⚙ Change game path' to fix this.")

    def _error(self, msg):
        self.status.config(text=msg, fg=ACCENT)
        self.login_btn.config(state="normal", text="PLAY")


class RegisterFrame(tk.Frame):
    def __init__(self, parent):
        super().__init__(parent, bg=BG)
        self.parent = parent
        self._build()

    def _build(self):
        tk.Label(self, text="⚔  CREATE ACCOUNT", font=("Segoe UI", 22, "bold"),
                 bg=BG, fg=ACCENT).pack(pady=(40, 4))
        tk.Label(self, text="Join Swordie v232.2",
                 font=("Segoe UI", 10), bg=BG, fg=SUBTEXT).pack(pady=(0, 24))

        card = tk.Frame(self, bg=PANEL, padx=30, pady=30)
        card.pack(padx=40, fill="x")

        for label, attr, show in [
            ("Username", "username", ""),
            ("Password", "password", "•"),
            ("Email (optional)", "email", ""),
        ]:
            tk.Label(card, text=label, font=("Segoe UI", 10, "bold"),
                     bg=PANEL, fg=TEXT, anchor="w").pack(fill="x")
            entry = ttk.Entry(card, font=("Segoe UI", 11), show=show if show else "")
            entry.pack(fill="x", pady=(4, 14), ipady=6)
            setattr(self, attr, entry)

        self.status = tk.Label(card, text="", font=("Segoe UI", 9),
                               bg=PANEL, fg=ACCENT, wraplength=380)
        self.status.pack(fill="x", pady=(0, 10))

        self.register_btn = tk.Button(card, text="CREATE ACCOUNT",
                                      font=("Segoe UI", 11, "bold"),
                                      bg=GREEN, fg="white", relief="flat",
                                      activebackground="#388e3c", activeforeground="white",
                                      cursor="hand2", command=self._submit, height=2)
        self.register_btn.pack(fill="x", pady=(0, 10))

        tk.Button(card, text="← Back to Login", font=("Segoe UI", 9),
                  bg=ACCENT2, fg=TEXT, relief="flat", cursor="hand2",
                  activebackground="#1a4a8a", activeforeground=TEXT,
                  command=lambda: self.parent.show_frame(LoginFrame)).pack(fill="x")

    def _submit(self):
        username = self.username.get().strip()
        password = self.password.get().strip()
        email = self.email.get().strip()
        if not username or not password:
            self.status.config(text="Username and password are required.")
            return
        self.register_btn.config(state="disabled", text="Creating...")
        threading.Thread(target=self._do_register,
                         args=(username, password, email), daemon=True).start()

    def _do_register(self, username, password, email):
        try:
            payload = {"username": username, "password": password}
            if email:
                payload["email"] = email
            r = requests.post(f"{API_BASE}/users", json=payload, timeout=8)
            if r.status_code in (200, 201, 204):
                self.after(0, lambda: self._success())
            elif r.status_code == 409:
                self.after(0, lambda: self._error("Username already taken."))
            else:
                self.after(0, lambda: self._error(f"Error {r.status_code}: {r.text[:80]}"))
        except requests.ConnectionError:
            self.after(0, lambda: self._error("Cannot connect to server."))
        except Exception as e:
            self.after(0, lambda: self._error(str(e)))

    def _success(self):
        messagebox.showinfo("Account Created", "Account created! You can now log in.")
        self.parent.show_frame(LoginFrame)

    def _error(self, msg):
        self.status.config(text=msg, fg=ACCENT)
        self.register_btn.config(state="normal", text="CREATE ACCOUNT")


if __name__ == "__main__":
    app = SwordieLauncher()
    app.mainloop()
