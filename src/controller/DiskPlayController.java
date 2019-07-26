package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import model.Video;
import utils.VideoPlayerThread;
import view.VideoNameCell;

import java.io.File;


/**
 * File: DiskPlayController.java
 * Name: 张袁峰
 * Student ID: 16301170
 * date: 2019/7/25
 */
public class DiskPlayController implements IBaseController {

    @FXML
    private ListView<Video> videoList;

    @FXML
    private Text rateText;

    @FXML
    private Text timeText;

    @FXML
    private Button slowButton;

    @FXML
    private Button playButton;

    @FXML
    private Button endButton;

    @FXML
    private Button fastButton;

    @FXML
    private Button addButton;

    @FXML
    private ImageView mediaView;

    @FXML
    private ProgressBar progressBar;

    private Main main;
    private double rate = 1.0;
    private boolean isPlay = false;
    private boolean isPause = false;
    private boolean isEnd = true;
    private VideoPlayerThread player;

    public DiskPlayController() {
    }

    private void play(String url) {
        player = new VideoPlayerThread(url, this, rate);
        player.start();
    }

    private void play() {
        player.play();
    }

    private void pause() {
        player.pause();
    }

    @FXML
    private void initialize() {
        videoList.setCellFactory(new Callback<ListView<Video>, ListCell<Video>>() {
            @Override
            public ListCell<Video> call(ListView<Video> param) {
                return new VideoNameCell();
            }
        });

        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();

                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("视频文件", "*.mp4", "*.avi", "*.wmv", "*.rmvb", "*.flv", "*.3gp"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("所有文件", "*.*"));
                fileChooser.setTitle("选择文件");//选择对话框的标题
                File choseFile = fileChooser.showOpenDialog(addButton.getScene().getWindow());
                if (choseFile != null) {
                    videoList.getItems().add(new Video(choseFile.getName(), choseFile.getAbsolutePath()));
                }
            }
        });

        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Video video = videoList.getSelectionModel().getSelectedItem();
                if (video != null) {
                    if (!isPlay) {
                        play(video.getUrl());
                        isPlay = true;
                        isEnd = false;
                        playButton.setText("暂停");
                    } else if (!isPause) {
                        pause();
                        isPause = true;
                        playButton.setText("播放");
                    } else {
                        play();
                        isPause = false;
                        playButton.setText("暂停");
                    }
                }
            }
        });

        endButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (isPlay) {
                    end();
                }
            }
        });

        fastButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (1.5-rate > 1e-6) {
                    rate += 0.25;
                }
                rateText.setText("x" + rate);
            }
        });

        slowButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (rate - 0.5 > 1e-6)
                    rate -= 0.25;
                rateText.setText("x" + rate);
            }
        });
    }

    public void setMain(Main main) {
        this.main = main;
    }

    @Override
    public ImageView getImageView() {
        return mediaView;
    }

    @Override
    public void end() {
        isPlay = false;
        isPause = false;
        isEnd = true;
        if (player != null) {
            player.end();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                playButton.setText("播放");
            }
        });
    }

    @Override
    public Text getTimeText() {
        return timeText;
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
