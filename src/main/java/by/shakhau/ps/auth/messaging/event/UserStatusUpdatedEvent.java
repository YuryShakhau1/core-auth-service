package by.shakhau.ps.auth.messaging.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class UserStatusUpdatedEvent {

    private UUID userId;
    private Boolean active;
}
