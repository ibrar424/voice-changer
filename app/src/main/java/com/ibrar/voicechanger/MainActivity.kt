package com.ibrar.voicechanger

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.widget.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class MainActivity : Activity() {
 private var audio: File? = null
 private var output: File? = null
 private var recorder: MediaRecorder? = null
 private var player: MediaPlayer? = null
 private lateinit var status: TextView
 private lateinit var server: EditText
 private lateinit var voice: EditText
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
  val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(30,40,30,20) }
  fun button(t:String, action:()->Unit) { layout.addView(Button(this).apply { text=t; setOnClickListener { action() } }) }
  layout.addView(TextView(this).apply { text="Voice Changer AI"; textSize=26f })
  server=EditText(this).apply { hint="Backend HTTPS URL (https://...)"; setSingleLine(true) }; layout.addView(server)
  voice=EditText(this).apply { hint="ElevenLabs voice ID"; setText("JBFqnCBsd6RMkjVDRZzb"); setSingleLine(true) }; layout.addView(voice)
  button("Choose audio file") { startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply { type="audio/*" }, 101) }
  button("Start recording") { if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 20) else startRecording() }
  button("Stop recording") { try { recorder?.stop(); recorder?.release(); recorder=null; show("Recording saved") } catch(e:Exception){ show(e.message?:"Recording error") } }
  button("Convert voice") { convert() }
  button("Play converted audio") { output?.let { f -> try { player?.release(); player=MediaPlayer().apply { setDataSource(f.path); prepare(); start() } } catch(e:Exception){show(e.message?:"Playback failed")} } ?: show("Convert audio first") }
  status=TextView(this).apply { text="Ready" }; layout.addView(status)
  setContentView(ScrollView(this).apply { addView(layout) })
 }
 private fun show(s:String){runOnUiThread{status.text=s}}
 private fun startRecording(){if(recorder!=null)return show("Already recording"); try { val f=File(cacheDir,"recording_${UUID.randomUUID()}.m4a"); recorder=MediaRecorder().apply { setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); setAudioEncoder(MediaRecorder.AudioEncoder.AAC); setOutputFile(f.path); prepare(); start() }; audio=f; show("Recording...") }catch(e:Exception){show(e.message?:"Cannot record")}}
 override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data); if(requestCode==101&&resultCode==RESULT_OK){data?.data?.let { uri -> val f=File(cacheDir,"selected_${UUID.randomUUID()}.audio"); contentResolver.openInputStream(uri)?.use{input->f.outputStream().use{input.copyTo(it)}}; audio=f; show("Audio selected") }}}
 private fun convert(){if(recorder!=null)return show("Stop recording first"); val f=audio ?: return show("Select or record audio first"); val base=server.text.toString().trim().trimEnd('/'); val id=voice.text.toString().trim(); if(!base.startsWith("https://")||!id.matches(Regex("[A-Za-z0-9_-]+")))return show("Enter HTTPS backend URL and voice ID"); show("Converting..."); Thread { try { val boundary="----VoiceChanger${UUID.randomUUID()}"; val c=(URL("$base/convert?voice_id=$id").openConnection() as HttpURLConnection).apply { requestMethod="POST"; doOutput=true; connectTimeout=20000; readTimeout=180000; setRequestProperty("Content-Type","multipart/form-data; boundary=$boundary") }; c.outputStream.use { os -> os.write("--$boundary\r\nContent-Disposition: form-data; name=\"audio\"; filename=\"audio.m4a\"\r\nContent-Type: application/octet-stream\r\n\r\n".toByteArray()); f.inputStream().use { it.copyTo(os) }; os.write("\r\n--$boundary--\r\n".toByteArray()) }; if(c.responseCode!=200)throw Exception("Server ${c.responseCode}: ${c.errorStream?.bufferedReader()?.readText()?.take(180)}"); val out=File(getExternalFilesDir(Environment.DIRECTORY_MUSIC),"converted_${System.currentTimeMillis()}.mp3"); c.inputStream.use{input->out.outputStream().use{input.copyTo(it)}}; output=out; show("Saved: ${out.name}"); c.disconnect() }catch(e:Exception){show("Error: ${e.message}")} }.start() }
 override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){super.onRequestPermissionsResult(requestCode,permissions,grantResults); if(requestCode==20){if(grantResults.firstOrNull()==PackageManager.PERMISSION_GRANTED)startRecording() else show("Microphone permission denied")}}
 override fun onDestroy(){recorder?.release();player?.release();super.onDestroy()}
}
