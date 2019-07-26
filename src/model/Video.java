package model;

import com.sun.deploy.panel.IProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * File: Video.java
 * Name: 张袁峰
 * Student ID: 16301170
 * date: 2019/7/25
 */
public class Video {

    private static int i = 0;
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty url;

    public Video(String name, String url) {
        this.id = new SimpleIntegerProperty(i);
        this.name = new SimpleStringProperty(name);
        this.url = new SimpleStringProperty(url);
        i++;
    }

    public Video() {
        this(null, null);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty getIdProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty getNameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty getUrlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }
}
