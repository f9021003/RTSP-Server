package com.pedro.rtspserver

import android.content.Context
import android.media.MediaCodec
import android.os.Build
import android.view.SurfaceView
import android.view.TextureView
import androidx.annotation.RequiresApi
import com.pedro.common.AudioCodec
import com.pedro.encoder.utils.CodecUtil
import com.pedro.library.base.Camera1Base
import com.pedro.library.view.LightOpenGlView
import com.pedro.library.view.OpenGlView
import com.pedro.common.ConnectChecker
import com.pedro.common.VideoCodec
import com.pedro.library.util.streamclient.StreamBaseClient
import java.nio.ByteBuffer

/**
 * Created by pedro on 13/02/19.
 */
open class RtspServerCamera1 : Camera1Base {

  private val rtspServer: RtspServer

  constructor(surfaceView: SurfaceView, connectChecker: ConnectChecker, port: Int) : super(
    surfaceView) {
    rtspServer = RtspServer(connectChecker, port)
  }

  constructor(textureView: TextureView, connectCheckerRtsp: ConnectChecker, port: Int) : super(
    textureView) {
    rtspServer = RtspServer(connectCheckerRtsp, port)
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  constructor(openGlView: OpenGlView, connectChecker: ConnectChecker, port: Int) : super(
    openGlView) {
    rtspServer = RtspServer(connectChecker, port)
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  constructor(lightOpenGlView: LightOpenGlView, connectCheckerRtsp: ConnectChecker,
              port: Int) : super(lightOpenGlView) {
    rtspServer = RtspServer(connectCheckerRtsp, port)
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  constructor(context: Context, ConnectChecker: ConnectChecker, port: Int) : super(
    context) {
    rtspServer = RtspServer(ConnectChecker, port)
  }

  override fun setVideoCodec(videoCodec: VideoCodec) {
    videoEncoder.type =
      if (videoCodec == VideoCodec.H265) CodecUtil.H265_MIME else CodecUtil.H264_MIME
  }


  fun getNumClients(): Int = rtspServer.getNumClients()

  fun getEndPointConnection(): String = "rtsp://${rtspServer.serverIp}:${rtspServer.port}/"


  fun startStream() {
    super.startStream("")
    rtspServer.startServer()
  }

  override fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int, audioCodec: AudioCodec) {
    rtspServer.setAudioInfo(sampleRate, isStereo, audioCodec)
  }

  override fun startStreamRtp(url: String) { //unused
  }

  override fun stopStreamRtp() {
    rtspServer.stopServer()
  }

  override fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtspServer.sendAudio(aacBuffer, info)
  }

  override fun onSpsPpsVpsRtp(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
    val newSps = sps.duplicate()
    val newPps = pps.duplicate()
    val newVps = vps?.duplicate()
    rtspServer.setVideoInfo(newSps, newPps, newVps)
  }

  override fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtspServer.sendVideo(h264Buffer, info)
  }

  override fun getStreamClient(): StreamBaseClient {
    return streamClient;
  }

  override fun setVideoCodecImp(codec: VideoCodec) {
    videoEncoder.type =
      if (codec == VideoCodec.H265) CodecUtil.H265_MIME else CodecUtil.H264_MIME
  }


}