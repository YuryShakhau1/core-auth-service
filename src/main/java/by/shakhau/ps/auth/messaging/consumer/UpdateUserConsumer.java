package by.shakhau.ps.auth.messaging.consumer;

import by.shakhau.ps.auth.messaging.event.UserUpdatedEvent;
import by.shakhau.ps.auth.messaging.mapper.UserEventMapper;
import by.shakhau.ps.auth.service.UserCredentialService;
import by.shakhau.ps.auth.service.model.UserInfo;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UpdateUserConsumer {

    private static final String TOPIC = "user.updated";

    private final UserEventMapper userEventMapper;
    private final UserCredentialService userCredentialService;

    @KafkaListener(topics = TOPIC, groupId = "user-service")
    public void consume(UserUpdatedEvent event, Acknowledgment ack) {
        UserInfo userInfo = userEventMapper.toUserInfo(event);
        userInfo.setPasswordActive(false);
        userCredentialService.update(userInfo);

        ack.acknowledge();
    }
}
