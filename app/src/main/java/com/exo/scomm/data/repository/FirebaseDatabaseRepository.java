package com.exo.scomm.data.repository;

import com.exo.scomm.utils.BaseValueEventListener;
import com.exo.scomm.utils.mapper.FirebaseMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public abstract class FirebaseDatabaseRepository<Model> {
   protected DatabaseReference databaseReference;
   private BaseValueEventListener listener;
   private FirebaseMapper mapper;

   protected abstract String getRootNode();

   public FirebaseDatabaseRepository(FirebaseMapper mapper) {
      databaseReference = FirebaseDatabase.getInstance().getReference(getRootNode());
      this.mapper = mapper;
   }

   public void addListener(FirebaseDatabaseRepositoryCallback<Model> firebaseCallback) {
      listener = new BaseValueEventListener(mapper, firebaseCallback);
      databaseReference.addValueEventListener(listener);
   }

   public void removeListener() {
      databaseReference.removeEventListener(listener);
   }

   public interface FirebaseDatabaseRepositoryCallback<T> {
      void onSuccess(List<T> result);
      void onError(Exception e);
   }
}
