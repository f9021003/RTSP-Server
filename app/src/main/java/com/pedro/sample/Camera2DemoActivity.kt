package com.pedro.sample

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.encoder.utils.gl.AspectRatioMode
import com.pedro.library.view.LightOpenGlView

import com.pedro.rtspserver.RtspServerCamera2
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Camera2DemoActivity : AppCompatActivity(), ConnectChecker, View.OnClickListener,
    SurfaceHolder.Callback {


  
  private lateinit var rtspServerCamera2: RtspServerCamera2
  private lateinit var button: Button
  private lateinit var bRecord: Button
  private lateinit var bSwitchCamera: Button
  private lateinit var surfaceView: LightOpenGlView
  private lateinit var tvUrl: TextView

  private var currentDateAndTime = ""
  private lateinit var folder: File

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    setContentView(R.layout.activity_camera_demo)
    val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
    folder = File(storageDir.absolutePath + "/RootEncoder")
    tvUrl = findViewById(R.id.tv_url)
    button = findViewById(R.id.b_start_stop)
    button.setOnClickListener(this)
    bRecord = findViewById(R.id.b_record)
    bRecord.setOnClickListener(this)
    bSwitchCamera = findViewById(R.id.switch_camera)
    bSwitchCamera.setOnClickListener(this)
    surfaceView = findViewById(R.id.surfaceView)
    surfaceView.setAspectRatioMode(AspectRatioMode.Adjust)


    rtspServerCamera2 = RtspServerCamera2(surfaceView, this, 1935)
//    rtspServerCamera2.setVideoCodec(VideoCodec.H265)
//    rtspServerCamera2.setAuthorization("admin", "admin")

    surfaceView.holder.addCallback(this)
  }

  override fun onNewBitrate(bitrate: Long) {

  }

  override fun onConnectionSuccess() {
    runOnUiThread {
      Toast.makeText(this@Camera2DemoActivity, "Connection success", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onConnectionFailed(reason: String) {
    runOnUiThread {
      Toast.makeText(this@Camera2DemoActivity, "Connection failed. $reason", Toast.LENGTH_SHORT)
          .show()
      rtspServerCamera2.stopStream()
      button.setText(R.string.start_button)
    }
  }

  override fun onConnectionStarted(rtspUrl: String) {
  }

  override fun onDisconnect() {
    runOnUiThread {
      Toast.makeText(this@Camera2DemoActivity, "Disconnected", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onAuthError() {
    runOnUiThread {
      Toast.makeText(this@Camera2DemoActivity, "Auth error", Toast.LENGTH_SHORT).show()
      rtspServerCamera2.stopStream()
      button.setText(R.string.start_button)
      tvUrl.text = ""
    }
  }

  override fun onAuthSuccess() {
    runOnUiThread {
      Toast.makeText(this@Camera2DemoActivity, "Auth success", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onClick(view: View) {
    when (view.id) {
      R.id.b_start_stop -> if (!rtspServerCamera2.isStreaming) {
        if (rtspServerCamera2.isRecording || rtspServerCamera2.prepareAudio() && rtspServerCamera2.prepareVideo()) {
          button.setText(R.string.stop_button)
          rtspServerCamera2.startStream()
          tvUrl.text = rtspServerCamera2.getEndPointConnection()
        } else {
          Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT)
              .show()
        }
      } else {
        button.setText(R.string.start_button)
        rtspServerCamera2.stopStream()
        tvUrl.text = ""
      }
      R.id.switch_camera -> try {
        rtspServerCamera2.switchCamera()
      } catch (e: CameraOpenException) {
        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
      }

      R.id.b_record -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
          if (!rtspServerCamera2.isRecording) {
            try {
              if (!folder.exists()) {
                folder.mkdir()
              }
              val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
              currentDateAndTime = sdf.format(Date())
              if (!rtspServerCamera2.isStreaming) {
                if (rtspServerCamera2.prepareAudio() && rtspServerCamera2.prepareVideo()) {
                  rtspServerCamera2.startRecord(folder.absolutePath + "/" + currentDateAndTime + ".mp4")
                  bRecord.setText(R.string.stop_record)
                  Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(
                    this, "Error preparing stream, This device cant do it",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              } else {
                rtspServerCamera2.startRecord(folder.absolutePath + "/" + currentDateAndTime + ".mp4")
                bRecord.setText(R.string.stop_record)
                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
              }
            } catch (e: IOException) {
              rtspServerCamera2.stopRecord()
              bRecord.setText(R.string.start_record)
              Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
          } else {
            rtspServerCamera2.stopRecord()
            bRecord.setText(R.string.start_record)
            Toast.makeText(
              this, "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
              Toast.LENGTH_SHORT
            ).show()
          }
        } else {
          Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...", Toast.LENGTH_SHORT).show()
        }
      }
      else -> {
      }
    }
  }

  override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
  }

  override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
    rtspServerCamera2.startPreview()
  }

  override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      if (rtspServerCamera2.isRecording) {
        rtspServerCamera2.stopRecord()
        bRecord.setText(R.string.start_record)
        Toast.makeText(this, "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath, Toast.LENGTH_SHORT).show()
        currentDateAndTime = ""
      }
    }
    if (rtspServerCamera2.isStreaming) {
      rtspServerCamera2.stopStream()
      button.text = resources.getString(R.string.start_button)
      tvUrl.text = ""
    }
    rtspServerCamera2.stopPreview()
  }
}
