package com.exo.scomm.utils.mapper;

import com.exo.scomm.data.models.User;
import com.exo.scomm.data.repository.UserEntity;

public class UserMapper extends  FirebaseMapper<UserEntity, User> {
    @Override
    public User map(UserEntity userEntity) {
        User user = new User();
        user.setId(userEntity.getId());
        user.setImage(userEntity.getImage());
        user.setStatus(userEntity.getStatus());
        user.setUsername(userEntity.getUsername());
        user.setPhone(userEntity.getPhone());
        return user;
    }
}
