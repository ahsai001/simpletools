package com.ahsailabs.simpletools.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahsailabs.simpletools.R;
import com.ahsailabs.simpletools.activities.ProgressActivity;
import com.ahsailabs.simpletools.adapters.ReadQuranLogListAdapter;
import com.ahsailabs.simpletools.databinding.FragmentReadQuranLogBinding;
import com.ahsailabs.simpletools.models.ReadQuranLogModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opencsv.CSVReader;
import com.zaitunlabs.zlcore.core.BaseFragment;
import com.zaitunlabs.zlcore.core.BaseRecyclerViewAdapter;
import com.zaitunlabs.zlcore.customs.DataList;
import com.zaitunlabs.zlcore.utils.CommonUtil;
import com.zaitunlabs.zlcore.utils.FileUtil;
import com.zaitunlabs.zlcore.utils.FormCommonUtil;
import com.zaitunlabs.zlcore.views.CustomRecylerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReadQuranLogActivityFragment extends BaseFragment {
    private static final int RC_SIGN_IN = 10012;
    private List<String> suratList = new ArrayList<>();
    private ArrayList<Integer> ayatList = new ArrayList<>();
    private List<ReadQuranLogModel> logModelList = new ArrayList<>();
    private ReadQuranLogListAdapter logReadQuranListAdapter;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore firebaseFirestore;
    private String userId;

    private FragmentReadQuranLogBinding binding;

    public ReadQuranLogActivityFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logReadQuranListAdapter = new ReadQuranLogListAdapter(logModelList);
        setHasOptionsMenu(true);
    }

    private void updateUI(GoogleSignInAccount account) {
        if(account != null){
            userId = account.getId();
            firebaseFirestore.collection("quranreadinglogs")
                    .document(userId).collection("logs")
                    .orderBy("_created_at", Query.Direction.DESCENDING)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        logModelList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            ReadQuranLogModel model = document.toObject(ReadQuranLogModel.class);
                            model.setDocId(document.getId());
                            logModelList.add(model);
                        }
                        logReadQuranListAdapter.notifyDataSetChanged();

                        if(logModelList.size() > 0){
                            binding.suratView.setSelection(logModelList.get(0).getNomor()-1);
                            binding.ayatView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    CommonUtil.hideKeyboard(binding.ayatView.getContext(), binding.ayatView);
                                    binding.ayatView.setValue(logModelList.get(0).getAyat());
                                }
                            }, 500);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        } else {
            signIn();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReadQuranLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            CsvReader csvReader = new CsvReader();
            CsvParser csvParser = csvReader.parse(FileUtil.getReaderFromRawFile(getActivity(), R.raw.alquran));
            CsvRow row;
            while ((row = csvParser.nextRow()) != null) {
                suratList.add(row.getField(0) + "." + row.getField(1));
                ayatList.add(Integer.parseInt(row.getField(3)));
            }
        } catch (NoClassDefFoundError e){
            try {
                CSVReader reader = new CSVReader(FileUtil.getReaderFromRawFile(getActivity(), R.raw.alquran));
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    suratList.add(nextLine[0] + "." + nextLine[1]);
                    ayatList.add(Integer.parseInt(nextLine[3]));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        logReadQuranListAdapter.setAyatList(ayatList);

        FormCommonUtil.setSpinnerList(getActivity(), binding.suratView, new DataList<String>().addAll(suratList), new DataList<String>(),
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        binding.ayatView.setMinValue(1);
                        binding.ayatView.setMaxValue(ayatList.get(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });


        binding.logButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nomor = binding.suratView.getSelectedItemPosition()+1;
                String surat = (String)binding.suratView.getSelectedItem();
                int ayat = binding.ayatView.getValue();
                final ReadQuranLogModel newReadModel = new ReadQuranLogModel(nomor, surat, ayat);
                newReadModel._created_at = new Date(System.currentTimeMillis());
                //newReadModel.save(); //cara sqlite
                //cara firestore
                if(userId != null) {
                    firebaseFirestore.collection("quranreadinglogs")
                            .document(userId).collection("logs")
                            .add(newReadModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "DocumentSnapshot written with ID: " + task.getResult().getId());
                                newReadModel.setDocId(task.getResult().getId());

                                logModelList.add(0, newReadModel);
                                logReadQuranListAdapter.notifyItemInserted(0);
                                binding.logRecylerView.smoothScrollToPosition(0);
                            }
                        }
                    });
                } else {
                    signIn();
                }
            }
        });


        binding.logRecylerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        binding.logRecylerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        binding.logRecylerView.addItemDecoration(itemDecoration);
        binding.logRecylerView.setItemAnimator(new DefaultItemAnimator());

        binding.logRecylerView.setEmptyView(binding.logEmptyView);
        binding.logRecylerView.setAdapter(logReadQuranListAdapter);

        /*
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), recyclerView, new RecyclerViewTouchListener.RecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, final int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));*/

        /* //load data cara sqlite
        logModelList.addAll(ReadQuranLogModel.getAllReadQuranLogList());
        logReadQuranListAdapter.notifyDataSetChanged();

        if(logModelList.size() > 0){
            suratView.setSelection(logModelList.get(0).getNomor()-1);
            ayatView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CommonUtil.hideKeyboard(ayatView.getContext(), ayatView);
                    ayatView.setValue(logModelList.get(0).getAyat());
                }
            }, 500);
        }
         */


        logReadQuranListAdapter.addOnChildViewClickListener(new BaseRecyclerViewAdapter.OnChildViewClickListener() {
            @Override
            public void onClick(View view, Object dataModel, final int position) {
                if(view.getId() == R.id.item_row_optionView){
                    CommonUtil.showPopupMenu(view.getContext(), R.menu.menu_item_read_quran_log, view, null,
                            new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    if(item.getItemId() == R.id.action_delete){
                                        ReadQuranLogModel readQuranLogModel = logModelList.get(position);
                                        //readQuranLogModel.delete();
                                        //cara firestore
                                        firebaseFirestore.collection("quranreadinglogs")
                                                .document(userId).collection("logs")
                                                .document(readQuranLogModel.getDocId())
                                                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    logModelList.remove(position);
                                                    logReadQuranListAdapter.notifyItemRemoved(position);
                                                }
                                            }
                                        });


                                    }
                                    return true;
                                }
                            });
                } else if(view instanceof CardView){

                }
            }

            @Override
            public void onLongClick(View view, Object dataModel, int position) {

            }
        });




        firebaseFirestore = FirebaseFirestore.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        updateUI(account);

    }

    public void refreshList(){
        logModelList.clear();
        logModelList.addAll(ReadQuranLogModel.getAllReadQuranLogList());
        logReadQuranListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_read_quran_log, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete_all:
                CommonUtil.showDialog2Option(getActivity(), "Delete log confirmation", "are you sure?",
                        "delete", new Runnable() {
                            @Override
                            public void run() {
                                //ReadQuranLogModel.deleteAll();

                                firebaseFirestore.collection("quranreadinglogs")
                                        .document(userId).collection("logs")
                                        .orderBy("_created_at", Query.Direction.DESCENDING)
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                            document.getReference().delete();
                                        }
                                    }
                                });

                                logModelList.clear();
                                logReadQuranListAdapter.notifyDataSetChanged();
                                CommonUtil.showSnackBar(getActivity(),"delete all log successfully");
                            }
                        }, "cancel", new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                return true;
            case R.id.action_show_progress:
                ProgressActivity.Companion.start(getActivity(), (ArrayList<ReadQuranLogModel>) logModelList, ayatList);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
