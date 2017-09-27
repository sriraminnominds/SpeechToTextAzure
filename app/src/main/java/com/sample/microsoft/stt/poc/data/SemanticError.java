package com.sample.microsoft.stt.poc.data;

import java.util.List;

/**
 * Created by sgarimella on 27/09/17.
 */

public class SemanticError {
    private String message;
    private List<String> options;
    private int offset;
    private int length;

    public SemanticError(String message, List<String> options, int offset, int length) {
        this.message = message;
        this.options = options;
        this.offset = offset;
        this.length = length;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "SemanticError{" +
                "message='" + message + '\'' +
                ", options=" + options +
                ", offset=" + offset +
                ", length=" + length +
                '}';
    }
}
