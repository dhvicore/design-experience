"""Restart only the existing Colab ACE-Step API in float32, retaining its API key."""
import os
import pathlib
import secrets
import subprocess
import time

import psutil
import requests

MARKER = '# Song Studio precision override'


def patch_loader(source):
    anchor = '            self.quantization = normalized_quantization'
    if MARKER in source:
        return source
    if source.count(anchor) != 1:
        raise RuntimeError('The model loader version changed. Stop and share this message; no process has been restarted.')
    override = ('            # Song Studio precision override\n'
                '            if os.environ.get("ACESTEP_DTYPE", "").strip().lower() == "float32":\n'
                '                self.dtype = torch.float32\n'
                '                logger.info("[Song Studio] precision override active: float32")\n')
    patched = source.replace(anchor, override + anchor)
    compile(patched, 'init_service_orchestrator.py', 'exec')
    return patched


def restart():
    root = pathlib.Path('/content/ACE-Step-1.5')
    loader = root / 'acestep/core/generation/handler/init_service_orchestrator.py'
    executable = root / '.venv/bin/acestep-api'
    if not loader.is_file() or not executable.is_file():
        raise RuntimeError('ACE-Step installation is missing. Run notebook cell 1 first.')
    patched = patch_loader(loader.read_text())
    processes = {}
    key = None
    for connection in psutil.net_connections(kind='tcp'):
        if connection.status != 'LISTEN' or not connection.laddr or connection.laddr.port != 8001 or not connection.pid:
            continue
        process = psutil.Process(connection.pid)
        command = ' '.join(process.cmdline()).lower()
        if 'acestep' not in command and 'ace-step' not in command:
            raise RuntimeError('Port 8001 belongs to another service. It has not been stopped.')
        processes[process.pid] = process
        key = process.environ().get('ACESTEP_API_KEY') or key
    key_file = pathlib.Path('/content/.song-studio-api-key')
    key = key or (key_file.read_text().strip() if key_file.exists() else '') or secrets.token_urlsafe(32)
    loader.write_text(patched)
    key_file.write_text(key)
    key_file.chmod(0o600)
    for process in processes.values():
        process.terminate()
    for process in processes.values():
        try:
            process.wait(timeout=30)
        except psutil.TimeoutExpired:
            process.kill()
            process.wait(timeout=10)
    environment = os.environ.copy()
    environment.update(ACESTEP_INIT_LLM='false', ACESTEP_DEVICE='cuda', ACESTEP_DTYPE='float32',
                       ACESTEP_OFFLOAD_TO_CPU='true', ACESTEP_OFFLOAD_DIT_TO_CPU='false',
                       ACESTEP_USE_FLASH_ATTENTION='false', ACESTEP_CONFIG_PATH='acestep-v15-turbo',
                       ACESTEP_API_KEY=key, MPLBACKEND='Agg', PYTHONUNBUFFERED='1')
    log_path = pathlib.Path('/content/ace-server.log')
    with log_path.open('w') as log:
        engine = subprocess.Popen([str(executable), '--host', '127.0.0.1', '--port', '8001'],
                                  cwd=root, env=environment, stdout=log, stderr=subprocess.STDOUT)
    headers = {'Authorization': 'Bearer ' + key}
    for attempt in range(600):
        if engine.poll() is not None:
            print(log_path.read_text(errors='replace').replace(key, '[REDACTED]')[-8000:])
            raise RuntimeError('The float32 model process stopped. Share the redacted traceback above.')
        try:
            response = requests.post('http://127.0.0.1:8001/query_result', json={'task_id_list': []}, headers=headers, timeout=3)
            if response.ok and response.json().get('code') == 200:
                print('MODEL READY — FLOAT32 configured, CPU offload enabled. Rerun cell 3, then cell 4. The API key was retained.')
                return dict(ROOT=root, API_KEY=key, engine=engine, env=environment, LOG_PATH=log_path, headers=headers)
        except requests.RequestException:
            pass
        if attempt % 15 == 0:
            print('Reloading model in float32…', attempt * 2, 'seconds', flush=True)
        time.sleep(2)
    raise TimeoutError('Float32 startup exceeded 20 minutes. Check /content/ace-server.log.')


if __name__ == '__main__':
    globals().update(restart())
