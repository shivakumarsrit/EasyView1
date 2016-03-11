package io.evercam.androidapp.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.Commons;
import io.intercom.com.squareup.picasso.Picasso;

public class AccountNavAdapter extends ArrayAdapter<AppUser> {

    private ArrayList<AppUser> mAppUsers;
    private Context mContext;
    private int mLayoutId;

    public AccountNavAdapter(Context context, int resource, int textViewResourceId, ArrayList<AppUser> appUsers) {
        super(context, resource, textViewResourceId, appUsers);
        updateUserList(appUsers);
        this.mContext = context;
        this.mLayoutId = resource;
    }

    public void updateUserList(ArrayList<AppUser> appUsers) {
        this.mAppUsers = appUsers;
        //Default user shouldn't show in the list
        mAppUsers.remove(AppData.defaultUser);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final AppUser appUser = mAppUsers.get(position);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(mLayoutId, null);
        }

        if (appUser != null) {
            TextView emailTextView = (TextView) view.findViewById(R.id.drawer_account_user_textView);
            CircleImageView profileImageView = (CircleImageView) view.findViewById(R.id.drawer_account_user_image_view);
            final String email = appUser.getEmail();
            String gravatarUrl = Commons.getGravatarUrl(email);
            emailTextView.setText(email);
            Picasso.with(mContext).load(gravatarUrl)
                    .noFade()
                    .placeholder(R.drawable.ic_profile_grey)
                    .into(profileImageView);
        }

        return view;
    }
}
