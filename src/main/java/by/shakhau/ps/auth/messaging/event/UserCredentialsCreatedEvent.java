package by.shakhau.ps.auth.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserCredentialsCreatedEvent {

    private UUID userId;
    private boolean created;
}
