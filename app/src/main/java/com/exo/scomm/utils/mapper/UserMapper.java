package com.exo.scomm.utils.mapper;

import com.exo.scomm.data.models.User;
import com.exo.scomm.data.repository.UserEntity;

public class UserMapper extends  FirebaseMapper<UserEntity, User> {
    @Override
    public User map(UserEntity userEntity) {
        User user = new User();
        user.setImage(userEntity.getImage());
        user.setStatus(userEntity.getStatus());
        user.setUsername(userEntity.getUsername());
        user.setUID(userEntity.getUID());
        user.setPhone(userEntity.getPhone());
        return user;
    }
}
