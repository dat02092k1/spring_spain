package com.example.gramble.domain.user.service;

import com.example.gramble.domain.user.dto.UserDto;

public interface UserService {
    UserDto register(final UserDto.Registration registration);

    UserDto login(final UserDto.Login login);

    UserDto update(final UserDto.Update update, final UserDto.Update authUserDetails);
}
