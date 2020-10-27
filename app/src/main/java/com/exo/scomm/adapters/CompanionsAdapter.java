package com.exo.scomm.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exo.scomm.ui.activities.Contacts;
import com.exo.scomm.ui.activities.HomeActivity;
import com.exo.scomm.ui.activities.MessageActivity;
import com.exo.scomm.ui.activities.Profile;
import com.exo.scomm.R;
import com.exo.scomm.data.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompanionsAdapter extends RecyclerView.Adapter<CompanionsAdapter.MyViewHolder> {
  private Context mCtxt;
  private Set<User> companionsSet;

  public CompanionsAdapter(Context context, Set<User> companionsList) {
    this.mCtxt = context;
    this.companionsSet = companionsList;

  }

  @NonNull
  @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    View itemView;
    if (viewType == R.layout.companion_item) {
      itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.companion_item, parent, false);
    } else {
      itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.button, parent, false);
    }
    return new MyViewHolder(itemView);

  }

  @Override
  public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    List<User> usersList = new ArrayList<>(companionsSet);
    if (position == usersList.size()) {
      holder.button.setOnClickListener(view -> mCtxt.startActivity(new Intent(mCtxt.getApplicationContext(), Contacts.class)));
    } else {
      final User user = usersList.get(position);
      holder.username.setText(user.getUsername());
      Picasso.get().load(user.getImage()).placeholder(R.drawable.scomm_user_placeholder_white).into(holder.profile);

      holder.itemView.setOnClickListener(v -> {
        CharSequence[] options = new CharSequence[]{"Open Profile", "Send Message"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtxt);
        builder.setTitle("Select Options");
        builder.setItems(options, (dialog, i) -> {
          if (i == 0) {
            Intent profileIntent = new Intent(mCtxt, Profile.class);
            profileIntent.putExtra("uid", user.getId());
            mCtxt.startActivity(profileIntent);
          } else if (i == 1) {
            Log.e("User Key", user.getId());
            Intent intent = new Intent(mCtxt, MessageActivity.class);
            intent.putExtra("fromTaskDetails", "1");
            intent.putExtra("username", user.getUsername());
            intent.putExtra("user_id", user.getId());
            mCtxt.startActivity(intent);


//            final HomeActivity activity = (HomeActivity) mCtxt;
//            activity.uid = user.getUID();
//            activity.username = user.getUsername();
//            activity.mainBottomNav.setSelectedItemId(R.id.bottom_chat_room);
//            activity.add_task.setVisibility(View.GONE);
          }
        });
        builder.show();
      });
    }
  }

  @Override
  public int getItemViewType(int position) {
    return (position == companionsSet.size()) ? R.layout.button : R.layout.companion_item;
  }

  @Override
  public int getItemCount() {
    return companionsSet.size() + 1;
  }

  public static class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView username;
    CircleImageView profile;
    Button button;

    MyViewHolder(View view) {
      super(view);
      profile = (CircleImageView) view.findViewById(R.id.companion_profile_image);
      username = (TextView) view.findViewById(R.id.companion_user_name);
      button = (Button) view.findViewById(R.id.companion_button);
    }
  }
}
