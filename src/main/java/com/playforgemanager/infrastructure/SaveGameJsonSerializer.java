package com.playforgemanager.infrastructure;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Iterator;

final class SaveGameJsonSerializer {
    String serialize(Object value) {
        StringBuilder json = new StringBuilder();
        writeValue(value, json, 0);
        json.append(System.lineSeparator());
        return json.toString();
    }

    private void writeValue(Object value, StringBuilder json, int indentLevel) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof String text) {
            writeString(text, json);
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value);
        } else if (value instanceof Enum<?> enumValue) {
            writeString(enumValue.name(), json);
        } else if (value instanceof Iterable<?> values) {
            writeArray(values.iterator(), json, indentLevel);
        } else if (value.getClass().isArray()) {
            writeArray(arrayIterator(value), json, indentLevel);
        } else if (value.getClass().isRecord()) {
            writeRecord(value, json, indentLevel);
        } else {
            throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass().getName());
        }
    }

    private void writeRecord(Object record, StringBuilder json, int indentLevel) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        json.append('{');

        if (components.length > 0) {
            json.append(System.lineSeparator());
        }

        for (int index = 0; index < components.length; index++) {
            RecordComponent component = components[index];
            indent(json, indentLevel + 1);
            writeString(component.getName(), json);
            json.append(": ");
            writeValue(readComponent(record, component), json, indentLevel + 1);

            if (index < components.length - 1) {
                json.append(',');
            }
            json.append(System.lineSeparator());
        }

        if (components.length > 0) {
            indent(json, indentLevel);
        }
        json.append('}');
    }

    private void writeArray(Iterator<?> iterator, StringBuilder json, int indentLevel) {
        json.append('[');

        if (iterator.hasNext()) {
            json.append(System.lineSeparator());
        }

        int index = 0;
        while (iterator.hasNext()) {
            Object item = iterator.next();
            indent(json, indentLevel + 1);
            writeValue(item, json, indentLevel + 1);

            if (iterator.hasNext()) {
                json.append(',');
            }
            json.append(System.lineSeparator());
            index++;
        }

        if (index > 0) {
            indent(json, indentLevel);
        }
        json.append(']');
    }

    private Iterator<?> arrayIterator(Object array) {
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < Array.getLength(array);
            }

            @Override
            public Object next() {
                return Array.get(array, index++);
            }
        };
    }

    private Object readComponent(Object record, RecordComponent component) {
        try {
            return component.getAccessor().invoke(record);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalArgumentException("Could not read JSON field: " + component.getName(), exception);
        }
    }

    private void writeString(String text, StringBuilder json) {
        json.append('"');
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            switch (character) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
                }
            }
        }
        json.append('"');
    }

    private void indent(StringBuilder json, int indentLevel) {
        json.append("  ".repeat(Math.max(0, indentLevel)));
    }
}
