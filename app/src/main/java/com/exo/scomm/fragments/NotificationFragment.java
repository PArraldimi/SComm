package com.exo.scomm.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.Companions;
import com.exo.scomm.HomeActivity;
import com.exo.scomm.R;
import com.exo.scomm.model.Notification;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NotificationFragment extends Fragment {
   private RecyclerView mFriendsRecycler;
   private DatabaseReference mNotificationsRef;
   private DatabaseReference mUsersDatabase, mTaskRef;
   private FirebaseAuth mAuth;
   Button clearAll;
   private String mCurrentUserId;
   private DatabaseReference mRootRef;

   public NotificationFragment() {
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {

      View mMainView = inflater.inflate(R.layout.fragment_notification, container, false);
      mFriendsRecycler = mMainView.findViewById(R.id.notification_recycler);
      clearAll = mMainView.findViewById(R.id.action_clear_all);
      mAuth = FirebaseAuth.getInstance();
      mCurrentUserId = mAuth.getCurrentUser().getUid();
      mRootRef = FirebaseDatabase.getInstance().getReference();
      mNotificationsRef = mRootRef.child("Notifications").child(mCurrentUserId);
      mUsersDatabase = mRootRef.child("Users");
      mTaskRef = mRootRef.child("Tasks");
      mFriendsRecycler.setHasFixedSize(true);
      mFriendsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

      clearAll.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            mNotificationsRef.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
               @Override
               public void onComplete(@NonNull Task<Void> task) {
                  if (task.isSuccessful()) {
                     Toast.makeText(getContext(), "Notifications cleared successfully", Toast.LENGTH_SHORT).show();
                  }
               }
            });
         }
      });
      return mMainView;
   }

   @Override
   public void onStart() {
      super.onStart();

      FirebaseRecyclerOptions<Notification> options =
              new FirebaseRecyclerOptions.Builder<Notification>()
                      .setQuery(mNotificationsRef, Notification.class)
                      .build();

      FirebaseRecyclerAdapter<Notification, NotificationFragment.FriendsReqViewHolder> adapter =
              new FirebaseRecyclerAdapter<Notification, NotificationFragment.FriendsReqViewHolder>(options) {
                 @Override
                 protected void onBindViewHolder(@NonNull final NotificationFragment.FriendsReqViewHolder holder, int position, @NonNull final Notification model) {

                    final String noteKey = getRef(position).getKey();
                    final String req_type = model.getType();
                    final String task_id = model.getTask_id();
                    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
                    final String date = dateFormat.format(model.getDate());
                    holder.mViewSchommers.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                          Intent usersIntent = new Intent(getActivity(), Companions.class);
                          startActivity(usersIntent);
                       }
                    });

                    switch (req_type) {
                       case "received": {
                          assert noteKey != null;
                          mNotificationsRef.child(noteKey).addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String user_id = dataSnapshot.child("fromUser").getValue().toString();
                                mRootRef.child("TaskCompanions").child(mCurrentUserId).child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                      if (dataSnapshot.child("task_id").getValue().toString().equals(task_id)) {
                                         holder.decline.setEnabled(false);
                                         holder.accept.setEnabled(false);
                                         holder.chat.setEnabled(true);
                                      } else {
                                         holder.decline.setEnabled(true);
                                         holder.accept.setEnabled(true);
                                         holder.chat.setEnabled(false);
                                      }

                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                                });
                                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                      final String userName = dataSnapshot.child("username").getValue().toString();
                                      mTaskRef.child(user_id).child(task_id).addValueEventListener(new ValueEventListener() {
                                         @Override
                                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            final String taskName = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
                                            final String taskDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();
                                            String text = userName + " sent you a invite request to task " + taskName + " on " + taskDate;
                                            holder.setText(text);
                                            holder.setDate(date);
                                            acceptInvite(holder, user_id, task_id);
                                            holder.chat.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                  openChat(user_id);
                                               }
                                            });
                                            holder.decline.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                  revokeInvite(user_id, task_id, holder);
                                               }
                                            });
                                         }

                                         @Override
                                         public void onCancelled(@NonNull DatabaseError databaseError) {

                                         }
                                      });
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                                });
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                          });


                          break;
                       }
                       case "sent": {
                          assert noteKey != null;
                          mNotificationsRef.child(noteKey).addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String user_id = dataSnapshot.child("toUser").getValue().toString();
                                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                      final String userName = dataSnapshot.child("username").getValue().toString();
                                      mTaskRef.child(mCurrentUserId).child(task_id).addValueEventListener(new ValueEventListener() {
                                         @Override
                                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String taskName = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
                                            String taskDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();

                                            String text = "You sent " + userName + " an invite request to task " + taskName + " on " + taskDate;
                                            holder.setText(text);
                                            holder.setDate(date);
                                            holder.decline.setEnabled(true);
                                            holder.accept.setEnabled(false);
                                            holder.chat.setEnabled(false);
                                            holder.decline.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                  declineInvite(user_id, holder);
                                               }
                                            });

                                         }

                                         @Override
                                         public void onCancelled(@NonNull DatabaseError databaseError) {

                                         }
                                      });


                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                                });
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                          });

                          break;
                       }
                       case "accepted":
                          holder.accept.setEnabled(false);
                          holder.chat.setEnabled(true);
                          assert noteKey != null;
                          mNotificationsRef.child(noteKey).addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String user_id = dataSnapshot.child("user").getValue().toString();
                                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                      final String userName = dataSnapshot.child("username").getValue().toString();
                                      mTaskRef.child(mCurrentUserId).child(task_id).addValueEventListener(new ValueEventListener() {
                                         @Override
                                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String taskName = Objects.requireNonNull(dataSnapshot.child("title").getValue()).toString();
                                            String taskDate = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();
                                            if (user_id.equals(mCurrentUserId)) {
                                               holder.decline.setEnabled(true);
                                               holder.decline.setText("Revoke");
                                               String text = userName + " accepted your invitation request to task " + taskName + " on " + taskDate;
                                               holder.setText(text);
                                               holder.setDate(date);
                                            } else {
                                               holder.decline.setEnabled(false);
                                               String text = "You accepted " + userName + "'s invitation to task " + taskName + " on " + taskDate;
                                               holder.setText(text);
                                               holder.setDate(date);
                                            }

                                            holder.chat.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                  final HomeActivity activity = (HomeActivity) getContext();
                                                  assert activity != null;
                                                  activity.uid = user_id;
                                                  activity.username = userName;
                                                  activity.mainBottomNav.setSelectedItemId(R.id.bottom_chat_room);
                                                  activity.add_task.setVisibility(View.GONE);

                                               }
                                            });

                                         }

                                         @Override
                                         public void onCancelled(@NonNull DatabaseError databaseError) {

                                         }
                                      });
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                                });
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                          });

                          break;
                       case "deleteTask":
                          holder.decline.setEnabled(false);
                          holder.accept.setEnabled(false);
                          holder.chat.setEnabled(false);
                          mNotificationsRef.child(noteKey).addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String user_id = Objects.requireNonNull(dataSnapshot.child("user").getValue()).toString();
                                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                      final String userName = dataSnapshot.child("username").getValue().toString();
                                      if (user_id.equals(mCurrentUserId)) {
                                         holder.decline.setEnabled(true);
                                         holder.decline.setText("Revoke");
                                         String text = userName + " deleted a your were in task ";
                                         holder.setText(text);
                                         holder.setDate(date);
                                      } else {
                                         holder.decline.setEnabled(false);
                                         String text = "You deleted task successfully" ;
                                         holder.setDate(date);
                                      }
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                                });
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                          });
                    }
                 }

                 @NonNull
                 @Override
                 public NotificationFragment.FriendsReqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.notification_item_layout, parent, false);
                    return new NotificationFragment.FriendsReqViewHolder(view);
                 }
              };
      mFriendsRecycler.setAdapter(adapter);
      adapter.startListening();
   }

   private void declineInvite(String user_id, @NonNull final FriendsReqViewHolder holder) {
      Map declineInviteMap = new HashMap();
      declineInviteMap.put("TaskCompanions/" + mCurrentUserId + "/" + user_id + "/" + "task_id", null);
      declineInviteMap.put("TaskCompanions/" + user_id + "/" + mCurrentUserId + "/" + "task_id", null);

      mRootRef.updateChildren(declineInviteMap, new DatabaseReference.CompletionListener() {
         @Override
         public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
            if (databaseError == null) {
               holder.decline.setEnabled(true);
               holder.accept.setEnabled(false);
               holder.chat.setEnabled(false);
            } else {
               String error = databaseError.getMessage();
               Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
         }
      });
   }

   private void revokeInvite(String user_id, String task_id, @NonNull final FriendsReqViewHolder holder) {
      Map unfriendsMap = new HashMap();
      unfriendsMap.put("TaskInviteRequests/" + mCurrentUserId + "/" + user_id + "/" + task_id, null);
      unfriendsMap.put("TaskInviteRequests/" + user_id + "/" + mCurrentUserId + "/" + task_id, null);
      unfriendsMap.put("TaskCompanions/" + mCurrentUserId + "/" + user_id + "/" + "task_id", null);
      unfriendsMap.put("TaskCompanions/" + user_id + "/" + mCurrentUserId + "/" + "task_id", null);
      mRootRef.updateChildren(unfriendsMap, new DatabaseReference.CompletionListener() {
         @Override
         public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
            if (databaseError == null) {
               holder.decline.setEnabled(false);
               holder.accept.setEnabled(false);
               holder.chat.setEnabled(false);
            } else {
               String error = databaseError.getMessage();
               Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
         }
      });
   }

   private void openChat(String user_id) {
      FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
      FragmentTransaction transaction = fragmentManager.beginTransaction();
      transaction.addToBackStack(null).add(R.id.main_container, ChatroomFragment.newInstance(user_id)).commit();
   }

   private void acceptInvite(@NonNull final FriendsReqViewHolder holder, final String user_id, final String task_id) {
      holder.accept.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mRootRef.child("Tasks").child(user_id).child(task_id).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  if (dataSnapshot.getChildren().equals("")) {
                     Toast.makeText(getContext(), "Unable to complete request", Toast.LENGTH_SHORT).show();
                  }
                  String title = dataSnapshot.child("title").getValue().toString();
                  String description = dataSnapshot.child("description").getValue().toString();
                  String type = dataSnapshot.child("type").getValue().toString();
                  String date = dataSnapshot.child("date").getValue().toString();
                  String taskOwn = dataSnapshot.child("taskOwner").getValue().toString();

                  final Map<String, String> taskMap = new HashMap<>();
                  taskMap.put("taskOwner", taskOwn);
                  taskMap.put("title", title);
                  taskMap.put("description", description);
                  taskMap.put("type", type);
                  taskMap.put("task_id", task_id);
                  taskMap.put("date", date);

                  String noteKey = mRootRef.child("Notifications").child(mCurrentUserId).push().getKey();

                  Map recipientNote = new HashMap<>();
                  recipientNote.put("user", mCurrentUserId);
                  recipientNote.put("type", "accepted");
                  recipientNote.put("task_id", task_id);
                  recipientNote.put("date", ServerValue.TIMESTAMP);

                  Map senderNote = new HashMap<>();
                  senderNote.put("user", user_id);
                  senderNote.put("type", "accepted");
                  senderNote.put("task_id", task_id);
                  senderNote.put("date", ServerValue.TIMESTAMP);

                  Map companionsMap = new HashMap();
                  companionsMap.put("TaskCompanions/" + mCurrentUserId + "/" + user_id + "/" + "task_id", task_id);
                  companionsMap.put("TaskCompanions/" + user_id + "/" + mCurrentUserId + "/" + "task_id", task_id);
                  companionsMap.put("Notifications/" + user_id + "/" + noteKey, recipientNote);
                  companionsMap.put("Notifications/" + mCurrentUserId + "/" + noteKey, senderNote);
                  companionsMap.put("Tasks/" + mCurrentUserId + "/" + task_id + "/", taskMap);
                  companionsMap.put("TaskInviteRequests/" + mCurrentUserId + "/" + user_id + "/" + task_id + "/" + "accepted", "true");
                  companionsMap.put("TaskInviteRequests/" + user_id + "/" + mCurrentUserId + "/" + task_id + "/" + "accepted", "true");
                  mRootRef.updateChildren(companionsMap, new DatabaseReference.CompletionListener() {
                     @Override
                     public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                           holder.decline.setEnabled(false);
                           holder.accept.setEnabled(false);
                           holder.chat.setEnabled(true);
                        } else {
                           String error = databaseError.getMessage();
                           Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        }
                     }

                  });
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
            });
         }
      });
   }

   public static class FriendsReqViewHolder extends RecyclerView.ViewHolder {
      View mView;
      TextView mDate;
      Button accept, decline, chat, mViewSchommers;

      FriendsReqViewHolder(@NonNull View itemView) {
         super(itemView);
         mView = itemView;
         chat = mView.findViewById(R.id.notification_chat_btn);
         accept = mView.findViewById(R.id.notification_accept_btn);
         decline = mView.findViewById(R.id.notification_decline_btn);
         mDate = mView.findViewById(R.id.notification_date);
         mViewSchommers = mView.findViewById(R.id.notification_view_scommers);

      }

      void setText(String text) {
         TextView user_name = mView.findViewById(R.id.notification_text_message);
         user_name.setText(text);
      }

      void setDate(String text) {
         TextView date = mView.findViewById(R.id.notification_date);
         date.setText(text);
      }
   }

   @Override
   public void onAttach(@NonNull Context context) {
      super.onAttach(context);

   }

   @Override
   public void onDetach() {
      super.onDetach();
   }

}
