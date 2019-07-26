package view;

import javafx.scene.control.ListCell;
import model.Video;

/**
 * File: VideoNameCell.java
 * Name: 张袁峰
 * Student ID: 16301170
 * date: 2019/7/25
 */
public class VideoNameCell extends ListCell<Video> {

    @Override
    public void updateItem(Video video, boolean empty) {
        super.updateItem(video, empty);
        if (video != null) {
            setText(video.getName());
        }
    }
}
