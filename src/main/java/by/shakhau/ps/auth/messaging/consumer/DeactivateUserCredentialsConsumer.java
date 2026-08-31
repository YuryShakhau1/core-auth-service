package by.shakhau.ps.auth.messaging.consumer;

import by.shakhau.ps.auth.messaging.event.DeactivateUserCredentialsEvent;
import by.shakhau.ps.auth.service.UserCredentialService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DeactivateUserCredentialsConsumer {

    private static final String TOPIC = "user.credentials.deactivate";

    private final UserCredentialService userCredentialService;

    @KafkaListener(topics = TOPIC, groupId = "auth-service")
    public void consume(DeactivateUserCredentialsEvent event, Acknowledgment ack) {
        userCredentialService.updateActive(event.getUserId(), false);

        ack.acknowledge();
    }
}
