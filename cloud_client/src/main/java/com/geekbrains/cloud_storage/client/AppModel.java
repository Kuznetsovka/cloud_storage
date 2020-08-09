package com.geekbrains.cloud_storage.client;

import javafx.beans.property.*;

public class AppModel {

    private final StringProperty text = new SimpleStringProperty ();

    public StringProperty textProperty() {
        return text ;
    }

    public final String getText() {
        return textProperty().get();
    }

    public final void setText(String text) {
        textProperty().set(text);
    }

}
