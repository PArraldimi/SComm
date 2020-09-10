package com.exo.scomm.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.exo.scomm.data.models.Task;
import com.exo.scomm.data.models.User;
import com.exo.scomm.data.repository.FirebaseDatabaseRepository;
import com.exo.scomm.data.repository.TaskRepository;
import com.exo.scomm.data.repository.UserRepository;

import java.util.List;

public class DataViewModel extends ViewModel {
    private MutableLiveData<List<User>> mAllUsers;

    public LiveData<List<User>> getAllUsers() {
        if (mAllUsers == null) {
            mAllUsers = new MutableLiveData<>();
            new UserRepository().addListener(new FirebaseDatabaseRepository.FirebaseDatabaseRepositoryCallback() {
                @Override
                public void onSuccess(List result) {
                    mAllUsers.setValue(result);
                }
                @Override
                public void onError(Exception e) {
                    mAllUsers.setValue(null);
                }
            });
        }
        return mAllUsers;
    }
}
