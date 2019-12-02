package bittorrent.util;

public class ChordException extends Exception {
    private static final long serialVersionUID = 1L;
    String message;

    public ChordException(String message) {
        this.message = message;
    }
}