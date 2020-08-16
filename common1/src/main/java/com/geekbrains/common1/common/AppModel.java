package com.geekbrains.common1.common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppModel {

    private final StringProperty text1 = new SimpleStringProperty ();
    private final StringProperty text2 = new SimpleStringProperty ();
    private final StringProperty text3 = new SimpleStringProperty ();
    private final StringProperty text4 = new SimpleStringProperty ();

    public StringProperty textNameFile() {
        return text1 ;
    }

    public StringProperty textPathSelected() {
        return text2 ;
    }

    public StringProperty textPathUpdate() {
        return text3;
    }

    public StringProperty textLogin() {
        return text4;
    }

    public final void setText1(String text) {
        textNameFile().set(text);
    }

    public final void setText2(String text) {
        textPathSelected().set(text);
    }

    public final void setText3(String text) {
        textPathUpdate().set(text);
    }

    public final void setText4(String text) {
        textLogin ().set(text);
    }
}
