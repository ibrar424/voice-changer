# Voice Changer AI (Android)
Native Kotlin Android app plus Python Flask backend for ElevenLabs speech-to-speech conversion.

## Build APK
Push to GitHub main and open Actions > Android APK > Run workflow. Download the debug APK from the workflow artifact.

## Backend
Install `pip install -r backend/requirements.txt` and set `ELEVENLABS_API_KEY` as a secret environment variable on your HTTPS hosting provider. Start with `gunicorn --chdir backend main:app --bind 0.0.0.0:$PORT` (or use port 8000 if PORT is unset). Never put your key in the Android app or repository.

## Use
Install APK, enter deployed HTTPS backend URL and ElevenLabs voice ID, record or select audio, convert, then play the MP3.

## Notes
Requires an internet connection and ElevenLabs credits. Conversion is not real-time. Only process recordings you have permission to use. This is a starter project and has not yet been tested on a physical device. For production, add backend authentication, rate limits, user consent handling and stronger validation. Playback and conversion are the primary MVP flows.
