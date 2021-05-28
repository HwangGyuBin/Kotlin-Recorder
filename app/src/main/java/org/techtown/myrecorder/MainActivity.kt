package org.techtown.myrecorder

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }

    private val soundVisualizerView:SoundVisualizerView by lazy {
        findViewById<SoundVisualizerView>(R.id.soundVisializerView)
    }
    private val timeText:timeText by lazy{
        findViewById<org.techtown.myrecorder.timeText>(R.id.timeText)
    }

    private val recordButton:RecordButton by lazy{
        findViewById<RecordButton>(R.id.recordButton)
    }

    private val resetButton: Button by lazy{
        findViewById<Button>(R.id.resetButton)
    }

    private val requiredPermissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)

    private val recordingFilePath:String by lazy{
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    private var recorder:MediaRecorder? = null
    private var player:MediaPlayer? = null
    private var state = State.BEFORE_RECORDING
        set(value){
            field = value
            resetButton.isEnabled = (value == State.AFTER_RECORDING) || (value == State.ON_PLAYING)
            recordButton.updateIconWithState(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioPermission()
        initViews()
        bindViews()
        initVariables()

    }

    @SuppressLint("NewApi")
    private fun requestAudioPermission(){
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun initViews(){
        recordButton.updateIconWithState(state)
    }

    private fun bindViews(){
        soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude?:0
        }

        resetButton.setOnClickListener {
            stopPlaying()
            soundVisualizerView.clearVisualization()
            timeText.clearCountTime()
            state = State.BEFORE_RECORDING
        }

        recordButton.setOnClickListener {
            when(state){
                State.BEFORE_RECORDING->    startRecording()
                State.ON_RECORDING->    stopRecording()
                State.AFTER_RECORDING-> startPlaying()
                State.ON_PLAYING-> stopPlaying()
            }
        }
    }


    private fun startRecording(){
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recordingFilePath)
            prepare()
        }

        recorder?.start()
        soundVisualizerView.startVisualizing(false)
        timeText.startCountUp()
        state = State.ON_RECORDING
    }

    private fun stopRecording(){
        recorder?.run{
            stop()
            release()
        }
        recorder = null
        soundVisualizerView.stopVisualizing()
        timeText.stopCountUp()
        state=State.AFTER_RECORDING
    }

    private fun startPlaying(){
        player = MediaPlayer().apply {
            setDataSource(recordingFilePath)
            prepare()
        }

        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECORDING
        }

        player?.start()
        soundVisualizerView.startVisualizing(true)
        timeText.startCountUp()
        state = State.ON_PLAYING
    }


    private fun stopPlaying(){
        player?.release()
        player = null
        soundVisualizerView.stopVisualizing()
        timeText.stopCountUp()
        state = State.AFTER_RECORDING
    }

    private fun initVariables() {
        state = State.BEFORE_RECORDING
    }
}