package com.ahsailabs.simpletools.fragments;

import android.os.Bundle;
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
import com.ahsailabs.simpletools.models.ReadQuranLogModel;
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
import java.util.Date;
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
    private ArrayList<Integer> ayatList = new ArrayList<>();
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

        recyclerView = view.findViewById(R.id.logRecylerView);
        emptyView = view.findViewById(R.id.log_empty_view);
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

        FormCommonUtil.setSpinnerList(getActivity(), suratView, new DataList<String>().addAll(suratList), new DataList<String>(),
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
                ReadQuranLogModel newReadModel = new ReadQuranLogModel(nomor, surat, ayat);
                newReadModel.save();
                newReadModel._created_at = new Date(System.currentTimeMillis());
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
                    CommonUtil.hideKeyboard(ayatView.getContext(), ayatView);
                    ayatView.setValue(logModelList.get(0).getAyat());
                }
            }, 500);
        }

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

            @Override
            public void onLongClick(View view, Object dataModel, int position) {

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
        switch (item.getItemId()){
            case R.id.action_delete_all:
                CommonUtil.showDialog2Option(getActivity(), "Delete log confirmation", "are you sure?",
                        "delete", new Runnable() {
                            @Override
                            public void run() {
                                ReadQuranLogModel.deleteAll();
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
                ProgressActivity.Companion.start(getActivity(), ayatList);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
