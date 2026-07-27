package by.shakhau.ps.auth.messaging.consumer;

import by.shakhau.ps.auth.messaging.event.UserStatusUpdatedEvent;
import by.shakhau.ps.auth.service.UserCredentialService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UpdateUserStatusConsumer {

    private static final String TOPIC = "user.status.updated";

    private final UserCredentialService userCredentialService;

    @KafkaListener(topics = TOPIC, groupId = "user-service")
    public void consume(UserStatusUpdatedEvent event, Acknowledgment ack) {
        userCredentialService.updateActive(event.getUserId(), event.getActive());

        ack.acknowledge();
    }
}
