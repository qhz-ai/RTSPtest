package com.tsc;

import java.util.TimerTask;

import javax.swing.JFrame;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;

/**
 * 按帧录制视频,并保存在本地
 *
 * @author xiaoyutongxue6
 * @date 2021-03-01
 * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
 * @param outputFile-该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
 *
 *  考虑一下多线程的问题，这样就可以实现预览和存储同时进行
 */
public class test {

    public static void main(String[] args) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        // 此处使用的是外网地址
        String inputFile = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
        String outputFile = "D://recorde.avi";
        frameRecord(inputFile, outputFile, 1);
    }

    public static void frameRecord(String inputFile, String outputFile, int audioChannel)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        // 设置为全局控制变量，用于控制录制结束
        boolean isStart = true;
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        // 如果不设置成tcp连接时，默认使用UDP，丢包现象比较严重
        grabber.setOption("rtsp_transport", "tcp"); // 设置成tcp以后比较稳定
        // 流媒体输出地址，分辨率（长，高），是否录制音频（0：不录制/1：录制）
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1280, 720, audioChannel);
        // 不进行转码时，编码格式默认为HFYU,使用VLC播放器时无法播放下载的视频 --可能和海康的摄像头有关
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);// avcodec.AV_CODEC_ID_H264，编码

        // 开始取视频源
        recordByFrame(grabber, recorder, isStart);
    }

    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        try {
            // 建议在线程中使用该方法
            grabber.start();
            recorder.start();
            Frame frame = null;

            // 此处仅为本地预览
            // CanvasFrame cframe = new CanvasFrame("欢迎来到直播间", CanvasFrame.getDefaultGamma() /
            // grabber.getGamma());
            // cframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 窗口关闭，则程序关闭
            // cframe.setAlwaysOnTop(true);

            while (status && (frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
                // cframe.showImage(frame);
            }
            recorder.stop();
            grabber.stop();
        } finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
    }
}
