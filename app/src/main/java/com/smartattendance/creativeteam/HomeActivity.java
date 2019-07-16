package com.smartattendance.creativeteam;
import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class HomeActivity extends AppCompatActivity {

    private String ID;
    private String Name;
    private boolean mInstance = false;
    private String mClassCode;
    private FirebaseAuth mAuth;
    private TextView mInfo;
    private TextView mProfileId;
    private TextView mProfileName;
    private LinearLayout mNoItemView;
    private Dialog mAttendanceDialog;
    private LinearLayout mLogoutBtn;
    private LinearLayout mQrCodeBtn;
    private ProgressDialog mLoadingDialog;
    private RecyclerView mRecycleView;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootReference;
    private CircularImageView mProfileImage;
    private SwipeRefreshLayout mSwipeRefresh;
    private ShimmerFrameLayout mShimmer;
    public EasyLocationProvider easyLocationProvider;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseRecyclerOptions<ClassModel> options;
    private FirebaseRecyclerAdapter<ClassModel, ClassViewHolder> adapter;

    public WifiManager mWifiManager;
    public WifiP2pManager mManager;
    public WifiP2pManager.Channel mChannel;
    public BroadcastReceiver mReceiver;


    public IntentFilter mIntentFilter;
    public List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    public String[] deviceNameArray;
    public WifiP2pDevice[] deviceArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        mInfo = (TextView)findViewById(R.id.infoid);
        mProfileId = (TextView)findViewById(R.id.profileid);
        mNoItemView = (LinearLayout)findViewById(R.id.noitem);
        mProfileName = (TextView)findViewById(R.id.profilename);
        mLogoutBtn = (LinearLayout)findViewById(R.id.logoutbtn);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mShimmer = (ShimmerFrameLayout)findViewById(R.id.shimmerid);
        mSwipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipeid);
        mProfileImage = (CircularImageView)findViewById(R.id.profileimage);
        mRecycleView = (RecyclerView)findViewById(R.id.recycleview);
        mQrCodeBtn = (LinearLayout)findViewById(R.id.qrcode);

        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleView.setHasFixedSize(true);
        mRootReference = FirebaseDatabase.getInstance().getReference();
        mWifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager,mChannel,this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);



        logutBtn();
        qrcodeBtn();
        getLocation();
        swiperefresh();
        takepermission();
        checkAuthState();
        setloadingdialog();
        setUserInformation();
        checkValidId();


    }

    private void showSuccessdialog() {
        mInstance = true;
        final SweetAlertDialog mDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        mDialog.setTitle("Successfull");
        mDialog.setContentText("Your attendance has been accepted!");
        mDialog.setConfirmText("Done");
        mDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        stopPeerDiscovery();
                        mDialog.dismissWithAnimation();
                    }
                });
        mDialog.show();

    }

    private void qrcodeBtn() {
        mQrCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String main = "Jam"+"_"+ID+"_"+Name;
                showQrCode(main);
            }
        });
    }

    private void showQrCode(String main) {
        mQrCodeBtn.setEnabled(false);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(main,BarcodeFormat.QR_CODE,
                    800,800);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            showQrDialog(bitmap);


        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    private void showQrDialog(Bitmap bitmap) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_qrcode);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ImageView qrView = (ImageView)dialog.findViewById(R.id.qrview);
        qrView.setImageBitmap(bitmap);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        mQrCodeBtn.setEnabled(true);
    }
    private void checkValidId() {
        if(!ID.equals(""))
        {
            checkclassExistOrNot();
        }
        else
        {
            noitemanimation();
        }
    }
    private void swiperefresh() {
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                checkValidId();

            }
        });
    }
    private void checkclassExistOrNot() {
        DatabaseReference reference = mRootReference.child("studentclasses").child(ID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {

                    mNoItemView.setVisibility(View.INVISIBLE);
                    mSwipeRefresh.setRefreshing(false);
                    loadRecycleviewData();
                }
                else
                {

                    noitemanimation();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void loadRecycleviewData() {
        mRecycleView.setVisibility(View.VISIBLE);
        DatabaseReference reference = mRootReference.child("studentclasses").child(ID);
        options = new FirebaseRecyclerOptions.Builder<ClassModel>().setQuery(reference,ClassModel.class).build();
        adapter = new FirebaseRecyclerAdapter<ClassModel, ClassViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ClassViewHolder holder, final int position, @NonNull final ClassModel model) {
                holder.mTitle.setText(model.getTitle());
                holder.mSection.setText(model.getSection());
                holder.mSemester.setText(model.getSemester());
                holder.mCode.setText(model.getCode());
                holder.mRipple.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mClassCode = null;
                        mClassCode= (model.getSection()+model.getCode()).trim();
                        startAttendance();
                        return false;
                    }
                });



                if (position%4 == 0){
                    holder.mRipple.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.random1));
                } else if (position%4 == 1){
                    holder.mRipple.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.random2));
                } else if (position%4 == 2){
                    holder.mRipple.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.random3));
                } else if (position%4 == 3){
                    holder.mRipple.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.random4));
                }
            }

            @NonNull
            @Override
            public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.class_layout,viewGroup,false);
                return new ClassViewHolder(view);
            }
        };
        adapter.startListening();
        mRecycleView.setAdapter(adapter);
    }
    private void startAttendance() {

        if(mWifiManager.isWifiEnabled())
        {
            waitingforwifi();
        }
        else
        {
            mWifiManager.setWifiEnabled(true);
            waitingforwifi();
        }

    }
    private void waitingforwifi() {
        mLoadingDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mWifiManager.isWifiEnabled())
                {
                    changeDeviceName();
                }

            }
        }, 1000);
    }
    private void changeDeviceName() {
        int numberOfParams = 3;
        Class[] methodParameters = new Class[numberOfParams];
        methodParameters[0] = WifiP2pManager.Channel.class;
        methodParameters[1] = String.class;
        methodParameters[2] = WifiP2pManager.ActionListener.class;

        Object arglist[] = new Object[numberOfParams];
        arglist[0] = mChannel;
        arglist[1] = ID;
        arglist[2] = new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                startdicover();
            }

            public void onFailure(int reason) {
                String resultString = "Fail reason: " + String.valueOf(reason);
                Toast.makeText(getApplicationContext(), resultString,Toast.LENGTH_LONG).show();
            }
        };

        ReflectionUtils.executePrivateMethod(mManager,WifiP2pManager.class,"setDeviceName",methodParameters,arglist);

    }
    private void startdicover() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mLoadingDialog.dismiss();
                showAttendanceOnDialog();
            }

            @Override
            public void onFailure(int reason) {
                mLoadingDialog.dismiss();
                Toasty.error(HomeActivity.this,"Attendance start failed!",Toasty.LENGTH_SHORT).show();
            }
        });
    }
    private void showAttendanceOnDialog() {
        mAttendanceDialog = new Dialog(this);
        mAttendanceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        mAttendanceDialog.setContentView(R.layout.attendance_dialog);
        mAttendanceDialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(mAttendanceDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final LottieAnimationView mWifiAnimation = (LottieAnimationView)mAttendanceDialog.findViewById(R.id.wifianimation);
        mWifiAnimation.playAnimation();



        ((AppCompatButton) mAttendanceDialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiAnimation.cancelAnimation();
                mAttendanceDialog.dismiss();
                stopPeerDiscovery();
            }
        });

        mAttendanceDialog.show();
        mAttendanceDialog.setCancelable(false);
        mAttendanceDialog.getWindow().setAttributes(lp);
    }
    public void stopPeerDiscovery(){
        mLoadingDialog.show();
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mLoadingDialog.dismiss();
                mInstance = false;
            }

            @Override
            public void onFailure(int reason) {
                mLoadingDialog.dismiss();
                mInstance = false;
                Toasty.error(HomeActivity.this,"Attendance stop failed!",Toasty.LENGTH_SHORT).show();
            }
        });
    }
    private void setUserInformation() {
        if(mCurrentUser!=null)
        {
            String name = mCurrentUser.getDisplayName();
            Uri photoUrl = mCurrentUser.getPhotoUrl();
            String profileid = name.replaceAll("([a-z A-Z .])", "");
            ID = profileid.trim();
            String profilename = name.replaceAll("([0-9])", "");
            Name = profilename.replaceAll("-", "");
            mProfileName.setText(Name);
            mProfileId.setText(profileid);
            Picasso.get().load(photoUrl).placeholder(R.drawable.avatar).into(mProfileImage);
        }
    }
    private void logutBtn() {
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }
    private void checkAuthState() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser()==null)
                {
                    changeActivity();
                }
            }
        };
    }
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to logout?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        signOut();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
    private void signOut() {
        GoogleSignInClient mGoogleSignInClient ;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getBaseContext(), gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {  //signout Google
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut(); //signout firebase
                    }
                });
    }
    private void changeActivity() {
        Intent intent = new Intent(HomeActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void noitemanimation() {
        mNoItemView.setVisibility(View.VISIBLE);
        mShimmer.startShimmer();
        mInfo.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mShimmer.stopShimmer();
                mInfo.setVisibility(View.VISIBLE);
                mSwipeRefresh.setRefreshing(false);
            }
        }, 3000);
    }
    private void getLocation() {

        easyLocationProvider = new EasyLocationProvider.Builder(HomeActivity.this)
                .setInterval(5000)
                .setFastestInterval(2000)
                //.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setListener(new EasyLocationProvider.EasyLocationCallback() {
                    @Override
                    public void onGoogleAPIClient(GoogleApiClient googleApiClient, String message) {

                    }

                    @Override
                    public void onLocationUpdated(double latitude, double longitude) {

                    }

                    @Override
                    public void onLocationUpdateRemoved() {

                    }
                }).build();

        getLifecycle().addObserver(easyLocationProvider);
    }
    private void setloadingdialog() {
        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setMessage("Please wait...");
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setCancelable(false);
    }
    private void checkCode(String classcode) {

        if(mClassCode.equals(classcode.trim()))
        {

            if(mInstance==false)
            {
                mInstance = true;
                mAttendanceDialog.dismiss();
                showSuccessdialog();
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    protected void onDestroy() {
        easyLocationProvider.removeUpdates();
        getLifecycle().removeObserver(easyLocationProvider);
        super.onDestroy();
    }
    @Override
    public void onStop() {
        if(adapter!=null)
        {
            adapter.stopListening();
        }
        super.onStop();
    }
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
        if(adapter!=null)
        {
            adapter.startListening();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    @AfterPermissionGranted(123)
    private void takepermission() {
        String[] perms = {android.Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {

        } else {
            EasyPermissions.requestPermissions(this, "We need permissions because this and that",
                    123, perms);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers))
            {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;
                //[Phone]GG

                for(WifiP2pDevice device : peerList.getDeviceList())
                {
                    if(device.deviceName.contains("]"))
                    {
                        String[] separated = device.deviceName.split("]");
                        String classcode = separated[1];
                        checkCode(classcode);
                    }
                    else
                    {
                        checkCode(device.deviceName);
                    }

                }

                if(peers.size()==0)
                {

                }

            }
        }
    };

}
