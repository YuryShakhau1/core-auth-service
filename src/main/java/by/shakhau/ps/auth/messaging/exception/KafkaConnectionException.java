package by.shakhau.ps.auth.messaging.exception;

public class KafkaConnectionException extends RuntimeException {
    public KafkaConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
