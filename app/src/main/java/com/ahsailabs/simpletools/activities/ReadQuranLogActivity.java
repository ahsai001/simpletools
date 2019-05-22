package com.ahsailabs.simpletools.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ahsailabs.simpletools.R;
import com.ahsailabs.simpletools.fragments.ReadQuranLogActivityFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.zaitunlabs.zlcore.constants.ZLCoreConstanta;
import com.zaitunlabs.zlcore.core.BaseActivity;
import com.zaitunlabs.zlcore.events.ReInitializeDatabaseEvent;
import com.zaitunlabs.zlcore.utils.CommonUtils;
import com.zaitunlabs.zlcore.utils.FileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ReadQuranLogActivity extends BaseActivity{
    ReadQuranLogActivityFragment fragment;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_quran_log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableUpNavigation();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(ReadQuranLogActivity.this);
                if (account != null) {
                    if (!GoogleSignIn.hasPermissions(account, Drive.SCOPE_APPFOLDER, Drive.SCOPE_FILE)) {
                        GoogleSignIn.requestPermissions(
                                ReadQuranLogActivity.this,
                                1013,
                                account,
                                Drive.SCOPE_APPFOLDER, Drive.SCOPE_FILE);
                    } else {
                        syncUI(account);
                    }
                } else {
                    signIn();
                }
            }
        });

        //Build Google Sign in options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestScopes(Drive.SCOPE_APPFOLDER, Drive.SCOPE_FILE)
                .build();
        //get Sign in client
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        fragment = showFragment(R.id.fragment,ReadQuranLogActivityFragment.class,null, savedInstanceState, "readquranlog");
    }


    //Method to signIn
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1013);
    }

    //method to sign out
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            syncUI(null);
                        }
                    }
                });
    }


    //Handle sign in results
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            syncUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

            syncUI(null);
        }
    }

    private void syncUI(GoogleSignInAccount account) {
        //Account is not null then user is logged in
        if (account != null) {
            // Get the app's Drive folder
            checkFileInDrive(account, ZLCoreConstanta.getDatabaseName(this));
        } else {
        }

    }

    private void checkFileInDrive(GoogleSignInAccount account, final String fileName){
        final DriveResourceClient client = Drive.getDriveResourceClient(ReadQuranLogActivity.this.getBaseContext(), account);
        final Task<DriveFolder> appFolderTask = client.getAppFolder();
        appFolderTask.addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {
                Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, fileName)).build();
                Task<MetadataBuffer> queryTask = client.queryChildren(appFolderTask.getResult(), query);
                queryTask.addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(final MetadataBuffer metadatas) {
                        if(metadatas.getCount() > 0) {
                            //file exist
                            CommonUtils.showDialog3Option(ReadQuranLogActivity.this,
                                    "Database Sync Option", "what will you do?",
                                    "backup", new Runnable() {
                                        @Override
                                        public void run() {
                                            client.delete(metadatas.get(0).getDriveId().asDriveResource())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            createFileInDrive(client, appFolderTask.getResult(), fileName);
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                        }
                                    }, "close", new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    }, "restore", new Runnable() {
                                        @Override
                                        public void run() {
                                            getFileFromDrive(client, metadatas, fileName);
                                        }
                                    });
                        } else {
                            //file not exist
                            CommonUtils.showDialog2Option(ReadQuranLogActivity.this,
                                    "Database Sync Option", "what will you do?",
                                    "backup", new Runnable() {
                                        @Override
                                        public void run() {
                                            createFileInDrive(client, appFolderTask.getResult(), fileName);
                                        }
                                    }, "close", new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                        }
                    }
                });
            }
        });
    }

    private void getFileFromDrive(final DriveResourceClient client, MetadataBuffer metadatas, final String fileName){
            client.openFile(metadatas.get(0).getDriveId().asDriveFile(), DriveFile.MODE_READ_ONLY)
                    .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                        @Override
                        public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                            DriveContents contents = task.getResult();

                            FileUtils.copyFile(new FileInputStream(contents.getParcelFileDescriptor().getFileDescriptor())
                                    , new FileOutputStream(getDatabasePath(fileName)));

                            EventBus.getDefault().post(new ReInitializeDatabaseEvent());
                            new android.os.Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    fragment.refreshList();
                                }
                            }, 500);


                            Task<Void> discardTask = client.discardContents(contents);
                            return discardTask;

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    CommonUtils.showSnackBar(ReadQuranLogActivity.this, "Failed");
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    CommonUtils.showSnackBar(ReadQuranLogActivity.this, "Success");
                }
            });
    }


    private void createFileInDrive(final DriveResourceClient client, final DriveFolder appFolder, final String fileName){
        final Task<DriveContents> createContentsTask = client.createContents();
        Tasks.whenAll(createContentsTask).continueWithTask(new Continuation<Void, Task<DriveFile>>() {
            @Override
            public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                DriveContents contents = createContentsTask.getResult();
                OutputStream outputStream = contents.getOutputStream();

                File dbFile = getDatabasePath(fileName);
                IOUtils.copyStream(new FileInputStream(dbFile), outputStream);
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(ZLCoreConstanta.getDatabaseName(ReadQuranLogActivity.this))
                        .setMimeType("application/x-sqlite3")
                        .build();

                return client.createFile(appFolder, changeSet, contents);
            }
        }).addOnSuccessListener(new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                CommonUtils.showSnackBar(ReadQuranLogActivity.this,"Backup Success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                CommonUtils.showSnackBar(ReadQuranLogActivity.this,e.getMessage());
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                CommonUtils.showSnackBar(ReadQuranLogActivity.this,"onCanceled");
            }
        }).addOnCompleteListener(new OnCompleteListener<DriveFile>() {
            @Override
            public void onComplete(@NonNull Task<DriveFile> task) {
                //CommonUtils.showSnackBar(ReadQuranLogActivity.this,"onComplete");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1013) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void start(Context context){
        Intent intent = new Intent(context,ReadQuranLogActivity.class);
        context.startActivity(intent);
    }
}
