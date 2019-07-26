package utils;

import controller.IBaseController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.bytedeco.ffmpeg.global.avutil.*;

/**
 * File: OnlinePlayerThread.java
 * Name: 张袁峰
 * Student ID: 16301170
 * date: 2019/7/25
 */
public class OnlinePlayerThreadNew extends Thread {

    private boolean isStop = true;
    private String url;
    private ImageView mediaView;
    private IBaseController controller;
    private Text timeText;
    private long start;
    private FFmpegFrameGrabber ff;

    public OnlinePlayerThreadNew(String url, IBaseController controller) {
        this.url = url;
        this.controller = controller;
        this.mediaView = controller.getImageView();
        this.timeText = controller.getTimeText();
    }

    @Override
    public void run() {
        isStop = false;
        try {
            ff = new FFmpegFrameGrabber(url);
            ff.setImageWidth(480);
            ff.setImageHeight(360);
            ff.start();
            AudioFormat audioFormat = new AudioFormat(ff.getSampleRate(), 16, 2, true, true);
            final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            final SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(info);
            soundLine.open(audioFormat);
            soundLine.start();
            Frame f;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            start = System.currentTimeMillis();
            while (!isStop) {
                System.out.print("");
                f = ff.grab();
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
                    timeText.setText("已观看：" + getTotalTime());
                } else if (f.samples != null) {
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
            ff.stop();
            mediaView.setImage(null);
            controller.end();
            isStop = true;
            ff.release();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            soundLine.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void end() {
        isStop = true;
    }

    public int convertPixelFormat(int format) {
        int f = format;
        switch (format) {
            case AV_PIX_FMT_YUVJ420P:
                f = AV_PIX_FMT_YUV420P;
                break;
            case AV_PIX_FMT_YUVJ422P:
                f = AV_PIX_FMT_YUV422P;
                break;
            case AV_PIX_FMT_YUVJ444P:
                f = AV_PIX_FMT_YUV444P;
                break;
            case AV_PIX_FMT_YUVJ440P:
                f = AV_PIX_FMT_YUV440P;
                break;
        }
        return f;
    }

    private String getTotalTime() {
        long current = System.currentTimeMillis();
        long duration = current - start;
        long total = duration / 1000;
        long h = total / 3600;
        long m = (total - h * 3600) / 60;
        long s = total - h * 3600 - m * 60;

        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
