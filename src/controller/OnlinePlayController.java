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
import javafx.util.Callback;
import model.Video;
import utils.OnlinePlayerThread;
import utils.OnlinePlayerThreadNew;
import view.VideoNameCell;

public class OnlinePlayController implements IBaseController {

    @FXML
    private ListView<Video> videoList;

    @FXML
    private Text timeText;

    @FXML
    private ImageView mediaView;

    @FXML
    private Button addButton;

    @FXML
    private Button playButton;

    @FXML
    private Button endButton;

    private boolean isPlay = false;
    private boolean isEnd = true;
    private OnlinePlayerThreadNew player;

    // Reference to the main application.
    private Main main;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public OnlinePlayController() {
        player = new OnlinePlayerThreadNew(null, this);
    }

    private void play(String url) {
        player = new OnlinePlayerThreadNew(url, this);
        player.start();
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

        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleNewOnline();
            }
        });

        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Video video = videoList.getSelectionModel().getSelectedItem();
                if (video != null && !isPlay) {
                    play(video.getUrl());
                    isPlay = true;
                    isEnd = false;
                    playButton.setDisable(true);
                }
            }
        });

        endButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                end();
                playButton.setDisable(false);
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

    private void handleNewOnline() {
        Video tempVideo = new Video();
        boolean okClicked = main.showAddDialog(tempVideo);
        if (okClicked) {
            main.getOnlineVideoList().add(tempVideo);
        }
    }
}
