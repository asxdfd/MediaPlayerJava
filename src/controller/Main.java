package controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Video;

import java.awt.*;
import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;
    private Pane rootLayout;
    private ObservableList<Video> onlineVideoList = FXCollections.observableArrayList();
    private ObservableList<Video> diskVideoList = FXCollections.observableArrayList();

    public Main() {
        onlineVideoList.add(new Video("CCTV1高清", "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8"));
        onlineVideoList.add(new Video("CCTV3高清", "http://ivi.bupt.edu.cn/hls/cctv3hd.m3u8"));
        onlineVideoList.add(new Video("CCTV5高清", "http://ivi.bupt.edu.cn/hls/cctv5hd.m3u8"));
        onlineVideoList.add(new Video("海康威视测试设备", "rtmp://rtmp01open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.hd"));
    }

    public ObservableList<Video> getOnlineVideoList() {
        return onlineVideoList;
    }

    public ObservableList<Video> getDiskVideoList() {
        return diskVideoList;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("视频播放器");
        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });
        this.primaryStage.setResizable(false);
        initRootLayout();
    }

    private void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/root.fxml"));
            rootLayout = loader.load();

            initTabPane((TabPane) rootLayout.getChildren().get(0));

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initTabPane(final TabPane tabPane) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/disk_play.fxml"));
            SplitPane sp = loader.load();
            Tab tab = tabPane.getTabs().get(0);
            tab.setContent(sp);
            final DiskPlayController diskController = loader.getController();
            diskController.setMain(this);

            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/online_play.fxml"));
            sp = loader.load();
            tab = tabPane.getTabs().get(1);
            tab.setContent(sp);
            final OnlinePlayController onlineController = loader.getController();
            onlineController.setMain(this);

            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/frame_play.fxml"));
            sp = loader.load();
            tab = tabPane.getTabs().get(2);
            tab.setContent(sp);
            final FramePlayController frameController = loader.getController();
            frameController.setMain(this);

            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/recognition_play.fxml"));
            sp = loader.load();
            tab = tabPane.getTabs().get(3);
            tab.setContent(sp);
            tab.setDisable(true);
            final RecognitionPlayController recognitionController = loader.getController();
            recognitionController.setMain(this);

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                    if (oldValue == tabPane.getTabs().get(0))
                        diskController.end();
                    else if (oldValue == tabPane.getTabs().get(1))
                        onlineController.end();
                    else if (oldValue == tabPane.getTabs().get(2))
                        frameController.end();
                    else if (oldValue == tabPane.getTabs().get(3))
                        recognitionController.end();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean showAddDialog(Video video) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/add.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("添加直播流");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            AddDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setVideo(video);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
