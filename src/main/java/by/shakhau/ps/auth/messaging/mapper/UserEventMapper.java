package by.shakhau.ps.auth.messaging.mapper;

import by.shakhau.ps.auth.messaging.event.UserCreatedEvent;
import by.shakhau.ps.auth.messaging.event.UserRegisteredEvent;
import by.shakhau.ps.auth.messaging.event.UserUpdatedEvent;
import by.shakhau.ps.auth.service.model.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserEventMapper {

    @Mapping(expression = "java(event.getTempPassword().toCharArray())", target = "password")
    UserInfo toUserInfo(UserCreatedEvent event);
    UserInfo toUserInfo(UserUpdatedEvent event);
    UserRegisteredEvent toUserRegisteredEvent(UserInfo userInfo);
}
