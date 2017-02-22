package com.example.guest.ghostydrop.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.guest.ghostydrop.Constants;
import com.example.guest.ghostydrop.Constructors.Picture;
import com.example.guest.ghostydrop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.guest.ghostydrop.R.mipmap.star;


public class FirebaseAllPhotosViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "Debug";
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;
    private SharedPreferences mSharedPreferences;
    private DatabaseReference PhotoOwnerRef;
    private DatabaseReference UserRef;
    private String mLat;
    private String mLong;
    View mView;
    Context mContext;

    public FirebaseAllPhotosViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        itemView.setOnClickListener(this);

    }

    public void bindPicture(final Picture picture) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLat = mSharedPreferences.getString(Constants.LATITUDE, null);
        mLong = mSharedPreferences.getString(Constants.LONGITUDE, null);
        Log.d(TAG, "bindPicture: " + mLat);
        Log.d(TAG, "bindPicture: " + mLong);

        //checking to see if user has already collected that photo
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        UserRef = FirebaseDatabase
                .getInstance()
                .getReference(Constants.FIREBASE_CHILD_USER)
                .child(uid);

        UserRef.child("collectedPhotos").orderByChild("caption").equalTo("hey there is garret").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if (dataSnapshot.exists()); {

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

            // ...
        });


        final TextView PhotoComment = (TextView) mView.findViewById(R.id. photoCommentTextView);
        TextView DistanceText= (TextView) mView.findViewById(R.id.distanceTextView);
        final TextView OwnerName= (TextView) mView.findViewById(R.id.postOwnerNameTextView);
        ImageView Image= (ImageView) mView.findViewById(R.id.photoImageView);
        ImageButton WhiteStar = (ImageButton) mView.findViewById(R.id.whiteStarImageButton);

        OwnerName.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d("Debug", "onClick: owner name");
            }
        });

        WhiteStar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                DatabaseReference pushRef = UserRef.child("collectedPhotos").push();
                String pushId = pushRef.getKey();
                picture.setPushId(pushId);
                pushRef.setValue(picture);

            }

        });
        Typeface typeface = Typeface.createFromAsset( PhotoComment.getContext().getAssets(),
                "fonts/Roboto-Regular.ttf");
        Typeface typeface2 = Typeface.createFromAsset( PhotoComment.getContext().getAssets(),
                "fonts/Walkway_Oblique_Bold.ttf");

        PhotoComment.setTypeface(typeface);
        OwnerName.setTypeface(typeface);
        DistanceText.setTypeface(typeface2);

        if (!picture.getImageUrl().contains("http")) {
            try {
                Bitmap imageBitmap = decodeFromFirebaseBase64(picture.getImageUrl());
                Image.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Picasso.with(mContext)
                    .load(picture.getImageUrl())
                    .resize(MAX_WIDTH, MAX_HEIGHT)
                    .centerCrop()
                    .into(Image);
        }

        PhotoComment.setText(picture.getCaption());
        String postUid = picture.getOwnerUid();
                //retreiving photo owner uid
                PhotoOwnerRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHILD_USER).child(postUid);
                PhotoOwnerRef.addValueEventListener(new ValueEventListener() {
                    @Override
                      public void onDataChange(DataSnapshot dataSnapshot) {
                        OwnerName.setText(dataSnapshot.child("displayName").getValue().toString());
                       }

                    @Override
                        public void onCancelled(DatabaseError databaseError) {

                    }
                });

        double lat1 = Double.parseDouble(picture.getLatitude());
        double lat2 = Double.parseDouble(mLat);

        double lon1 = Double.parseDouble(picture.getLongitude());
        double lon2 = Double.parseDouble(mLong);

        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        float distanceInMeters = loc1.distanceTo(loc2);
        double distanceInMiles=distanceInMeters * 0.000621371;
        double roundedMiles = (double)Math.round(distanceInMiles * 10d) / 10d;

        DistanceText.setText(roundedMiles + " miles away");
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);

    }



    @Override
    public void onClick(View v)  {
        final ArrayList<Picture> pictures = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_PHOTOS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    pictures.add(snapshot.getValue(Picture.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}



