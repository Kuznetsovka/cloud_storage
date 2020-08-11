package com.geekbrains.cloud_storage.client;

import javafx.beans.property.*;

public class AppModel {

    private final StringProperty text1 = new SimpleStringProperty ();
    private final StringProperty text2 = new SimpleStringProperty ();

    public StringProperty textProperty1() {
        return text1 ;
    }

    public StringProperty textProperty2() {
        return text2 ;
    }

    public final void setText1(String text) {
        textProperty1().set(text);
    }

    public final void setText2(String text) {
        textProperty2().set(text);
    }

}
