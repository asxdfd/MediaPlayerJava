package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Callback;
import model.Video;
import utils.OnlinePlayerThread;
import view.VideoNameCell;

/**
 * File: RecognitionPlayController.java
 * Name: 张袁峰
 * Student ID: 16301170
 * date: 2019/7/25
 */
public class RecognitionPlayController implements IBaseController {

    @FXML
    private ListView<Video> videoList;

    @FXML
    private Text timeText;

    @FXML
    private ImageView mediaView;

    private boolean isPlay = false;
    private boolean isEnd = true;
    private OnlinePlayerThread player;

    // Reference to the main application.
    private Main main;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public RecognitionPlayController() {

        player = new OnlinePlayerThread(null, this);
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        videoList.setCellFactory(new Callback<ListView<Video>, ListCell<Video>>() {
            @Override
            public ListCell<Video> call(ListView<Video> param) {
                return new VideoNameCell();
            }
        });

    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param main
     */
    public void setMain(Main main) {
        this.main = main;

        // Add observable list data to the table
        videoList.setItems(main.getOnlineVideoList());
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
        return timeText;
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    public ProgressBar getProgressBar() {
        return null;
    }
}
