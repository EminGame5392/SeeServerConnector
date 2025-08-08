# SeeServerConnector

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.16.5-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

Плагин для интеграции Minecraft сервера (1.16.5+) с веб-сайтом на Flask через локальную сеть.

## 📌 Возможности

- Реальная статистика онлайна на сайте
- Автоматическая синхронизация данных
- Защищенное соединение с секретным ключом
- Гибкая настройка через конфиг
- Поддержка локальных сетей

## ⚙️ Установка

1. Скачайте последнюю версию плагина из [Releases](https://github.com/your-username/SeeServerConnector/releases)
2. Поместите файл `SeeServerConnector.jar` в папку `plugins` вашего сервера
3. Перезапустите сервер

## 🔧 Настройка плагина

После первого запуска появится файл `plugins/SeeServerConnector/config.yml`:

```yaml
server-url: "http://localhost:5000"  # URL вашего Flask-сайта
secret-key: "your-secret-key"       # Должен совпадать с ключом на сайте
debug-mode: false                   # Режим отладки (логирование запросов)
```

## 🖥️ Настройка Flask сервера

1. Добавьте в ваш `app.py`:

```python
from flask import Flask, request, jsonify

app = Flask(__name__)
app.config['SERVER_SECRET_KEY'] = 'your-secret-key'  # Должен совпадать с ключом в config.yml

@app.route('/api/heartbeat', methods=['POST'])
def api_heartbeat():
    data = request.get_json()
    if data.get('secret_key') != app.config['SERVER_SECRET_KEY']:
        return jsonify({'status': 'error'}), 403
    
    # Здесь можно сохранять статистику онлайна
    online_players = data.get('online', 0)
    return jsonify({'status': 'success'})

@app.route('/api/player_update', methods=['POST'])
def api_player_update():
    data = request.get_json()
    if data.get('secret_key') != app.config['SERVER_SECRET_KEY']:
        return jsonify({'status': 'error'}), 403
    
    # Обработка входа/выхода игроков
    player_name = data.get('player')
    action = data.get('action')  # "join" или "quit"
    return jsonify({'status': 'success'})
```

2. Убедитесь что:
   - Сайт доступен по указанному в `config.yml` URL
   - Порт не заблокирован фаерволом
   - Секретные ключи совпадают

## 🌐 Работа в локальной сети

1. Найдите локальный IP компьютера с сайтом:
   - Windows: `ipconfig` → IPv4-адрес
   - Linux/macOS: `ifconfig` или `ip a`

2. В `config.yml` укажите правильный IP:
```yaml
server-url: "http://192.168.1.100:5000"  # Пример для локальной сети
```

3. Откройте порт 5000 (или другой используемый) в настройках фаервола

## 📊 Пример использования данных

В вашем Flask приложении можно хранить статистику:

```python
online_players = 0
players_log = []

@app.route('/api/heartbeat', methods=['POST'])
def api_heartbeat():
    global online_players
    data = request.get_json()
    online_players = data.get('online', 0)
    return jsonify({'status': 'success'})

@app.route('/stats')
def stats():
    return f"Online: {online_players} players"
```

## 🐛 Решение проблем

**Плагин не подключается к сайту:**
1. Проверьте `server-url` в config.yml
2. Убедитесь что сайт запущен и доступен
3. Проверьте секретный ключ
4. Включите `debug-mode: true` для детальных логов

**Ошибка 403 Forbidden:**
- Убедитесь что `secret-key` совпадает на сервере и в плагине

## 📜 License

MIT License. Подробнее в файле [LICENSE](LICENSE).

---

💡 **Совет:** Для production используйте Nginx + uWSGI/Gunicorn вместо встроенного сервера Flask
```

Этот README включает:
1. Четкие инструкции по установке
2. Примеры конфигурации для обеих сторон
3. Решение частых проблем
4. Советы по работе в локальной сети
5. Лицензионную информацию

Вы можете дополнить его:
- Скриншотами
- Более сложными примерами использования API
- Информацией о совместимости с другими плагинами
- Чейнджлогом версий
