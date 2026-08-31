package by.shakhau.ps.auth.messaging.consumer;

import by.shakhau.ps.auth.messaging.event.UserCreatedEvent;
import by.shakhau.ps.auth.messaging.event.UserCredentialsCreatedEvent;
import by.shakhau.ps.auth.messaging.mapper.UserEventMapper;
import by.shakhau.ps.auth.messaging.producer.CreatedUserCredentialsProducer;
import by.shakhau.ps.auth.service.UserCredentialService;
import by.shakhau.ps.auth.service.model.UserInfo;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CreateUserConsumer {

    private static final String TOPIC = "user.created";

    private final CreatedUserCredentialsProducer createdUserCredentialsProducer;
    private final UserEventMapper userEventMapper;
    private final UserCredentialService userCredentialService;

    @KafkaListener(topics = TOPIC, groupId = "user-service")
    public void consume(UserCreatedEvent event, Acknowledgment ack) {
        UserInfo userInfo = userEventMapper.toUserInfo(event);
        userInfo.setPasswordActive(false);
        try {
            userCredentialService.registerExternalUser(userInfo, event.getRole());
            createdUserCredentialsProducer.send(new UserCredentialsCreatedEvent(userInfo.getUserId(), true));
            ack.acknowledge();
        } catch (Exception e) {
            createdUserCredentialsProducer.send(new UserCredentialsCreatedEvent(userInfo.getUserId(), false));
            ack.acknowledge();
        }
    }
}
