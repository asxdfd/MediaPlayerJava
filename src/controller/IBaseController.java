package controller;

import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

/**
 * File: IBaseController.java
 * Name: 张袁峰
 * Student ID: 16301170
 * date: 2019/7/25
 */
public interface IBaseController {

    ImageView getImageView();

    void end();

    Text getTimeText();

    boolean isFrame();

    ProgressBar getProgressBar();
}
