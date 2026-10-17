import os
import requests
from flask import Flask, request, Response, jsonify, render_template
app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 50 * 1024 * 1024
@app.post('/convert')
def convert():
    key = os.getenv('ELEVENLABS_API_KEY')
    if not key: return jsonify(error='Backend API key not configured'), 503
    audio = request.files.get('audio')
    voice = request.args.get('voice_id', '')
    if not audio or not voice or not voice.replace('-', '').isalnum(): return jsonify(error='Audio and valid voice ID required'), 400
    try:
        result = requests.post(f'https://api.elevenlabs.io/v1/speech-to-speech/{voice}', headers={'xi-api-key': key}, files={'audio': (audio.filename or 'audio.m4a', audio.stream, audio.mimetype or 'application/octet-stream')}, data={'model_id':'eleven_multilingual_sts_v2'}, timeout=180)
        if not result.ok: return jsonify(error='Voice conversion failed', details=result.text[:300]), result.status_code
        return Response(result.content, mimetype='audio/mpeg')
    except requests.RequestException: return jsonify(error='Upstream connection failed'), 502
@app.get('/health')
def health(): return jsonify(status='ok')

@app.get('/')
def home():
    return render_template('index.html')
