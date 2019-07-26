package controller;

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
 * File: FramePlayController.java
 * Name: 张袁峰
 * Student ID: 16301170
 * date: 2019/7/25
 */
public class FramePlayController implements IBaseController {

    @FXML
    private ListView<Video> videoList;

    @FXML
    private Text frameText;

    @FXML
    private Button playButton;

    @FXML
    private Button endButton;

    @FXML
    private Button addButton;

    @FXML
    private ImageView mediaView;

    @FXML
    private ProgressBar progressBar;

    private Main main;
    private boolean isPlay = false;
    private boolean isEnd = true;
    private VideoPlayerThread player;

    public FramePlayController() {
    }

    private void play(String url) {
        player = new VideoPlayerThread(url, this, 1.0);
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
                        pause();
                    } else {
                        play();
                        pause();
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
        isEnd = true;
        if (player != null)
            player.end();
    }

    @Override
    public Text getTimeText() {
        return frameText;
    }

    @Override
    public boolean isFrame() {
        return true;
    }

    @Override
    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
