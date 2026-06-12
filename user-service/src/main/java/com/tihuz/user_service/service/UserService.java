package com.tihuz.user_service.service;

import com.tihuz.common.event.UserEvent;
import com.tihuz.common.exception.AppException;
import com.tihuz.common.exception.ErrorCode;
import com.tihuz.user_service.dto.request.UserUpdateRequest;
import com.tihuz.user_service.dto.response.UserResponse;
import com.tihuz.user_service.entity.User;
import com.tihuz.user_service.mapper.UserMapper;
import com.tihuz.user_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService
{

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    KafkaTemplate kafkaTemplate;

    public UserResponse updateUser(Long UserId, UserUpdateRequest request)
    {
        User user=userRepository.findByIdAndIsDeletedFalse(UserId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXITS));

        Optional.ofNullable(request.getUsername()).ifPresent(user::setUsername);
        Optional.ofNullable(request.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(request.getAvatarUrl()).ifPresent(user::setAvatarUrl);
        Optional.ofNullable(request.getPassword())
                .ifPresent(pw -> user.setPassword(passwordEncoder.encode(pw)));


        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setCompanyId(user.getCompanyId());
        event.setEmail(user.getEmail());
        event.setAction("UPDATE");
        kafkaTemplate.send(UserEvent.TOPIC, event);

        return userMapper.toUserResponse(userRepository.save(user));
    }



    public List<UserResponse> getAll()
    {
        return userRepository.findAllByIsDeletedFalse()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }



    public Page<UserResponse> getActiveUsers(Pageable pageable, String keyword)
    {
        Page<User> page;
        if (keyword != null && !keyword.isEmpty()) {
            page = userRepository.findByIsDeletedFalseAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            page = userRepository.findByIsDeletedFalse(pageable);
        }
        return page.map(userMapper::toUserResponse);
    }

    public Page<UserResponse> getDeletedUsers(Pageable pageable, String keyword) {
        Page<User> page;
        if (keyword != null && !keyword.isEmpty()) {
            page = userRepository.findByIsDeletedTrueAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            page = userRepository.findByIsDeletedTrue(pageable);
        }
        return page.map(userMapper::toUserResponse);
    }

    public void restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        user.setIsDeleted(false);
        userRepository.save(user);
    }




    public UserResponse getUserId(Long UserId)
    {
        var user=userRepository.findByIdAndIsDeletedFalse(UserId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXITS));

//        try {
//            Thread.sleep(6000); // sleep 6 giây
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return userMapper.toUserResponse(user);


    }

    public UserResponse getMyInfo()
    {
        var context= SecurityContextHolder.getContext().getAuthentication();

        User user=userRepository.findByIdAndIsDeletedFalse(Long.valueOf(context.getName()))
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXITS));


        return userMapper.toUserResponse(user);
    }


    public String softDeleteUserId(Long userId)
    {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));

        user.setIsDeleted(true);
        userRepository.save(user);

        // Send event user deleted
        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setCompanyId(user.getCompanyId());
        event.setAction("SOFT_DELETED");
        kafkaTemplate.send(UserEvent.TOPIC, event);

        return "User soft deleted";
    }


    public String hardDeleteUserId(Long userId)
    {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));

        userRepository.deleteById(user.getId());

        // Send event user deleted
        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setCompanyId(user.getCompanyId());
        event.setAction("HARD_DELETED");
        kafkaTemplate.send(UserEvent.TOPIC, event);

        return "User hard deleted";
    }


}
