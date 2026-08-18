package by.shakhau.ps.auth.mapper;

import by.shakhau.ps.auth.controller.dto.request.CreateUserRequest;
import by.shakhau.ps.auth.controller.dto.response.UserResponse;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.service.model.UserInfo;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserCredentialMapper {

    @Mapping(target = "active", source = "active")
    UserCredential toUserCredential(Boolean active, UserInfo userInfo);
    UserResponse toGetUserResponse(UserCredential userCredential);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "passwordActive", target = "passwordActive", ignore = true)
    void updateUserCredential(UserInfo userInfo, @MappingTarget UserCredential credential);
}
