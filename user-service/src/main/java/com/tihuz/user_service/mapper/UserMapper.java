package com.tihuz.user_service.mapper;

import com.tihuz.user_service.dto.request.UserCreatedRequest;
import com.tihuz.user_service.dto.response.UserResponse;
import com.tihuz.user_service.entity.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {


    User toUser(UserCreatedRequest request);

    UserResponse toUserResponse( User user);
}
