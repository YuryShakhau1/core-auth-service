package by.shakhau.ps.auth.messaging.producer;

import by.shakhau.ps.auth.messaging.event.UserRegisteredEvent;
import by.shakhau.ps.auth.messaging.exception.KafkaConnectionException;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserRegistrationProducer {

    private static final String TOPIC = "user.registered";
    private final KafkaTemplate<String, UserRegisteredEvent> template;

    public void send(UserRegisteredEvent event) {
        try {
            template.send(TOPIC, event.getUserId().toString(), event).get();
        } catch (Exception e) {
            throw new KafkaConnectionException(e.getMessage(), e);
        }
    }
}
