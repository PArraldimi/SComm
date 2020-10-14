package com.exo.scomm.data.repository;

import com.exo.scomm.data.models.Task;
import com.exo.scomm.data.models.User;
import com.exo.scomm.utils.mapper.FirebaseMapper;
import com.exo.scomm.utils.mapper.UserMapper;

public class UserRepository extends FirebaseDatabaseRepository<User> {
    public UserRepository() {
        super(new UserMapper());
    }

    @Override
    protected String getRootNode() {
        return "Users" + "/";
    }
}
