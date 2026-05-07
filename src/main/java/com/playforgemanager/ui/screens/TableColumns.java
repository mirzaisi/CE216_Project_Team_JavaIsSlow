package com.playforgemanager.ui.screens;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;

import java.util.function.Function;

final class TableColumns {
    private TableColumns() {}

    static <T> TableColumn<T, String> string(String title, Function<T, String> extractor) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(extractor.apply(cell.getValue())));
        return column;
    }

    static <T> TableColumn<T, String> integer(String title, Function<T, Integer> extractor) {
        return string(title, row -> String.valueOf(extractor.apply(row)));
    }
}
