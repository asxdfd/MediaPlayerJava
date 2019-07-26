package utils;

import com.sun.javaws.progress.Progress;
import controller.IBaseController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.bytedeco.javacv.*;
import org.bytedeco.librealsense.frame;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * File: VideoCaptureThread.java
 * Name: 张袁峰
 * Student ID: 16301170
 * date: 2019/7/25
 */
public class VideoPlayerThread extends Thread {

    private boolean isPause = false;
    private boolean isStop = true;
    private String url;
    private ImageView mediaView;
    private IBaseController controller;
    private double rate = 1.0;
    private double fps;
    private Text timeText;
    private boolean isFrame;
    private FFmpegFrameGrabber ff;
    private ProgressBar progressBar;
    private long start;
    Process p;
    Runtime r = Runtime.getRuntime();

    public VideoPlayerThread(String url, IBaseController controller, double rate) {
        this.url = url;
        this.controller = controller;
        this.mediaView = controller.getImageView();
        this.isFrame = controller.isFrame();
        this.timeText = controller.getTimeText();
        this.rate = rate;
        this.progressBar = controller.getProgressBar();
    }

    @Override
    public void run() {
        isStop = false;
        try {
            String rated_url = null;
//            if (rate - 1.0 < 1e-6)
                ff = FFmpegFrameGrabber.createDefault(url);
//            else{
//                rated_url = getRatedUrl();
//            }
            ff.setImageWidth(480);
            ff.setImageHeight(360);
            ff.start();
            fps = ff.getFrameRate();//帧率
            int ffLength = ff.getLengthInFrames();
            AudioFormat audioFormat = new AudioFormat(ff.getSampleRate(), 16, ff.getAudioChannels(), true, true);
            final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            final SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(info);
            soundLine.open(audioFormat);
            soundLine.start();
            Frame f;
            String totalTime = getTotalTime(fps, ffLength, true);
            String now = isFrame ? "0" : "00:00";
            timeText.setText(now + "/" + totalTime);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            start = System.currentTimeMillis();
            int i = 0;
            while (!isStop) {
                System.out.print("");
                if (!isPause) {
                    if (isFrame)
                        f = ff.grabImage();
                    else f = ff.grab();
                    if (f == null)
                        break;

                    if (f.image != null) {
                        Java2DFrameConverter converter = new Java2DFrameConverter();
                        BufferedImage ii = converter.convert(f);
                        Image image = SwingFXUtils.toFXImage(ii, null);
                        Platform.runLater(new Runnable() {
                            public void run() {
                                mediaView.setImage(image);
                            }
                        });
                        i++;
                    } else if (f.samples != null) {
                        if (f != null) {
                            final ShortBuffer channelSamplesShortBuffer = (ShortBuffer) f.samples[0];
                            channelSamplesShortBuffer.rewind();

                            final ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);

                            for (int j = 0; j < channelSamplesShortBuffer.capacity(); j++) {
                                short val = channelSamplesShortBuffer.get(j);
                                outBuffer.putShort(val);
                            }

                            /**
                             * We need this because soundLine.write ignores
                             * interruptions during writing.
                             */
                            try {
                                executor.submit(new Runnable() {
                                    public void run() {
                                        soundLine.write(outBuffer.array(), 0, outBuffer.capacity());
                                        outBuffer.clear();
                                    }
                                }).get();
                            } catch (InterruptedException interruptedException) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                    if (isFrame)
                        now = getTotalTime(fps, i, false);
                    else now = getTotalTime(fps, ffLength, false);
                    timeText.setText(now + "/" + totalTime);
                }
            }
            if (rated_url != null) {
                File file = new File(rated_url);
                if (file.exists())
                    file.delete();
            }
            if (p != null && p.isAlive())
                p.destroy();
            ff.stop();
            mediaView.setImage(null);
            controller.end();
            ff.release();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            soundLine.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRatedUrl() {
        String cmd = "cmd /c ffmpeg -i " + url + " -filter_complex \"[0:v]setpts=" + (1 / rate) + "*PTS[v];[0:a]atempo=" + rate + "[a]\" -map \"[v]\" -map \"[a]\" ";
        String rated = url.substring(0, url.lastIndexOf('\\'));
        String name = url.substring(url.lastIndexOf('\\') + 1);
        rated = rated + "\\rated_" + name;
        cmd = cmd + rated;
        try {
            p = r.exec(cmd);
            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rated;
    }

    public void play() {
        isPause = false;
    }

    public void pause() {
        isPause = true;
    }

    public void end() {
        isStop = true;
        isPause = false;
    }

    private String getTotalTime(double fps, int length, boolean total) {
        if (isFrame)
            return String.valueOf(length);
        else if (total) {
            long sum = (long) (length / fps);
            long m = sum / 60;
            long s = sum - m * 60;
            return String.format("%02d:%02d", m, s);
        } else {
            long sum = System.currentTimeMillis() - start;
            sum = sum / 1000;
            long m = sum / 60;
            long s = sum - m * 60;
            progressBar.setProgress(1.0 * sum / (long) (length / fps));
            return String.format("%02d:%02d", m, s);
        }
    }

}
