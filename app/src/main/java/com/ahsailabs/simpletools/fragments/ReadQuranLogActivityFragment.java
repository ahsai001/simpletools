package com.ahsailabs.simpletools.fragments;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.ahsailabs.simpletools.R;
import com.ahsailabs.simpletools.adapters.ReadQuranLogListAdapter;
import com.ahsailabs.simpletools.models.ReadQuranLogModel;
import com.zaitunlabs.zlcore.core.BaseFragment;
import com.zaitunlabs.zlcore.core.BaseRecyclerViewAdapter;
import com.zaitunlabs.zlcore.customs.DataList;
import com.zaitunlabs.zlcore.listeners.SwipeDragCallback;
import com.zaitunlabs.zlcore.utils.CommonUtils;
import com.zaitunlabs.zlcore.utils.FileUtils;
import com.zaitunlabs.zlcore.utils.FormCommonUtils;
import com.zaitunlabs.zlcore.views.CustomRecylerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReadQuranLogActivityFragment extends BaseFragment {
    private Spinner suratView;
    private NumberPicker ayatView;
    private Button logButton;
    private CustomRecylerView recyclerView;
    private View emptyView;
    private List<String> suratList = new ArrayList<>();
    private List<Integer> ayatList = new ArrayList<>();
    private List<ReadQuranLogModel> logModelList = new ArrayList<>();
    private ReadQuranLogListAdapter logReadQuranListAdapter;


    public ReadQuranLogActivityFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logReadQuranListAdapter = new ReadQuranLogListAdapter(logModelList);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_read_quran_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        suratView = view.findViewById(R.id.suratView);
        ayatView = view.findViewById(R.id.ayatView);
        logButton = view.findViewById(R.id.logButtonView);

        recyclerView = (CustomRecylerView) view.findViewById(R.id.logRecylerView);
        emptyView = view.findViewById(R.id.log_empty_view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CsvReader csvReader = new CsvReader();
        CsvParser csvParser = null;
        try {
            csvParser = csvReader.parse(FileUtils.getReaderFromRawFile(getActivity(), R.raw.alquran));
            CsvRow row;
            while ((row = csvParser.nextRow()) != null) {
                suratList.add(row.getField(0)+"."+row.getField(1));
                ayatList.add(Integer.parseInt(row.getField(3)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        logReadQuranListAdapter.setAyatList(ayatList);

        FormCommonUtils.setSpinnerList(getActivity(), suratView, new DataList<String>().addAll(suratList), new DataList<String>(),
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        ayatView.setMinValue(1);
                        ayatView.setMaxValue(ayatList.get(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });


        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nomor = suratView.getSelectedItemPosition()+1;
                String surat = (String)suratView.getSelectedItem();
                int ayat = ayatView.getValue();
                ReadQuranLogModel newReadModel = new ReadQuranLogModel(nomor, surat, ayat).saveWithTimeStamp();
                logModelList.add(0, newReadModel);
                logReadQuranListAdapter.notifyItemInserted(0);
                recyclerView.smoothScrollToPosition(0);
            }
        });


        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setEmptyView(emptyView);
        recyclerView.setAdapter(logReadQuranListAdapter);

        /*
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), recyclerView, new RecyclerViewTouchListener.RecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, final int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));*/

        logModelList.addAll(ReadQuranLogModel.getAllReadQuranLogList());
        logReadQuranListAdapter.notifyDataSetChanged();

        if(logModelList.size() > 0){
            suratView.setSelection(logModelList.get(0).getNomor()-1);
            ayatView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CommonUtils.hideKeyboard(ayatView.getContext(), ayatView);
                    ayatView.setValue(logModelList.get(0).getAyat());
                }
            }, 500);
        }

        logReadQuranListAdapter.setOnChildViewClickListener(new BaseRecyclerViewAdapter.OnChildViewClickListener() {
            @Override
            public void onClick(View view, Object dataModel, final int position) {
                if(view.getId() == R.id.item_row_optionView){
                    CommonUtils.showPopup(view.getContext(), R.menu.menu_item_read_quran_log, view, null,
                            new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    if(item.getItemId() == R.id.action_delete){
                                        ReadQuranLogModel readQuranLogModel = logModelList.get(position);
                                        readQuranLogModel.delete();
                                        logModelList.remove(position);
                                        logReadQuranListAdapter.notifyItemRemoved(position);
                                    }
                                    return true;
                                }
                            });
                } else if(view instanceof CardView){

                }
            }
        });

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
        if(item.getItemId() == R.id.action_delete_all){
            CommonUtils.showDialog2Option(getActivity(), "Delete log confirmation", "are you sure?",
                    "delete", new Runnable() {
                        @Override
                        public void run() {
                            ReadQuranLogModel.deleteAll();
                            logModelList.clear();
                            logReadQuranListAdapter.notifyDataSetChanged();
                            CommonUtils.showSnackBar(getActivity(),"delete all log successfully");
                        }
                    }, "cancel", new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }
}
