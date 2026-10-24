"""Run in a Colab cell to reconnect to your existing ACE-Step server; prints no keys."""
import pathlib
import requests
import psutil

API_KEY = None
for connection in psutil.net_connections(kind="tcp"):
    if connection.status != "LISTEN" or not connection.laddr or connection.laddr.port != 8001 or not connection.pid:
        continue
    process = psutil.Process(connection.pid)
    # Limit credential recovery to the user's own ACE-Step process on the configured port.
    command = " ".join(process.cmdline()).lower()
    if "acestep" not in command and "ace-step" not in command:
        continue
    candidate = process.environ().get("ACESTEP_API_KEY")
    if not candidate:
        continue
    response = requests.post("http://127.0.0.1:8001/query_result", json={"task_id_list": []},
                             headers={"Authorization": "Bearer " + candidate}, timeout=15)
    if response.status_code == 200 and response.json().get("code") == 200:
        API_KEY = candidate
        break
if API_KEY is None:
    raise RuntimeError("No authenticated ACE-Step server found on port 8001. Rerun cell 2 and wait for MODEL READY.")
headers = {"Authorization": "Bearer " + API_KEY}
key_file = pathlib.Path("/content/.song-studio-api-key")
key_file.write_text(API_KEY)
key_file.chmod(0o600)
print("Authentication restored. Rerun cell 3, then cell 4 and download the fresh connection file. No model restart was needed.")
