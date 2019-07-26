package utils;

import controller.IBaseController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;

import static org.bytedeco.ffmpeg.global.avutil.*;

/**
 * File: OnlinePlayerThread.java
 * Name: 张袁峰
 * Student ID: 16301170
 * date: 2019/7/25
 */
public class OnlinePlayerThread extends Thread {

    private boolean isStop = true;
    private String url;
    private ImageView mediaView;
    private IBaseController controller;
    private Text timeText;
    private long start;

    public OnlinePlayerThread(String url, IBaseController controller) {
        this.url = url;
        this.controller = controller;
        this.mediaView = controller.getImageView();
        this.timeText = controller.getTimeText();
    }

    @Override
    public void run() {
        isStop = false;
        try {
            FFmpegFrameGrabber ff = new FFmpegFrameGrabber(url);
            ff.setImageWidth(480);
            ff.setImageHeight(360);
//            ff.setPixelFormat(AV_PIX_FMT_YUV420P);
            ff.start();
            Frame f;
            this.start = System.currentTimeMillis();
            while ((f = ff.grabImage()) != null && !isStop) {
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage ii = converter.convert(f);
                Image image = SwingFXUtils.toFXImage(ii, null);
                Platform.runLater(new Runnable() {
                    public void run() {
                        mediaView.setImage(image);
                    }
                });
                timeText.setText("已观看：" + getTotalTime());
            }
            ff.stop();
            mediaView.setImage(null);
            controller.end();
            isStop = true;
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
