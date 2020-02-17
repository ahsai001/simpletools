package com.ahsailabs.simpletools.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ahsailabs.simpletools.R;
import com.ahsailabs.simpletools.models.ReadQuranLogModel;
import com.opencsv.CSVReader;
import com.zaitunlabs.zlcore.core.BaseRecyclerViewAdapter;
import com.zaitunlabs.zlcore.utils.CommonUtils;
import com.zaitunlabs.zlcore.utils.DateStringUtils;
import com.zaitunlabs.zlcore.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

/**
 * Created by ahsai on 3/18/2018.
 */

public class ReadQuranLogListAdapter extends BaseRecyclerViewAdapter<ReadQuranLogModel, ReadQuranLogListAdapter.ReadQuranLogListViewHolder>{
    private int totalAyat = 0;
    private List<Integer> ayatInJuzList;
    private List<Integer> ayatList;

    public ReadQuranLogListAdapter(List<ReadQuranLogModel> modelList) {
        super(modelList);
    }

    public class ReadQuranLogListViewHolder extends RecyclerView.ViewHolder {
        CardView rootView;
        TextView titleView;
        TextView bodyView;
        ImageButton optionView;

        private ReadQuranLogListViewHolder(View view) {
            super(view);
            rootView = (CardView) view;
            titleView = (TextView) view.findViewById(R.id.item_row_titleView);
            bodyView = (TextView) view.findViewById(R.id.item_row_descView);
            optionView = (ImageButton) view.findViewById(R.id.item_row_optionView);
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.readquranlog_list_item_row;
    }

    @Override
    protected ReadQuranLogListViewHolder getViewHolder(View rootView) {
        return new ReadQuranLogListViewHolder(rootView);
    }

    @Override
    protected void doSettingViewWithModel(ReadQuranLogListViewHolder holder, ReadQuranLogModel dataModel, int position) {
        holder.titleView.setText(dataModel.getSurat()+" "+dataModel.getAyat()+" ( juz "+getCurrentJuz(holder.titleView.getContext(),dataModel)+")");
        holder.bodyView.setText(DateStringUtils.getDateTimeInString(dataModel.timestamp, CommonUtils.getIndonesianLocale()));
        setViewClickable(holder, holder.optionView);
        setViewClickable(holder, holder.rootView);
    }

    public void setAyatList(List<Integer> ayatList) {
        this.ayatList = ayatList;
    }

    private int getCurrentJuz(Context context, ReadQuranLogModel model){
       if(ayatInJuzList == null){
           ayatInJuzList = new ArrayList<>();
           try {
               CsvReader csvReader = new CsvReader();
               CsvParser csvParser = csvReader.parse(FileUtils.getReaderFromRawFile(context, R.raw.ayatinjuz));
               CsvRow row;
               while ((row = csvParser.nextRow()) != null) {
                   ayatInJuzList.add(Integer.parseInt(row.getField(1)));
               }
           } catch (NoClassDefFoundError e){
                try {
                    CSVReader reader = new CSVReader(FileUtils.getReaderFromRawFile(context, R.raw.ayatinjuz));
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        ayatInJuzList.add(Integer.parseInt(nextLine[1]));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
           } catch (IOException e) {
               e.printStackTrace();
           }
       }

        /*
        if(totalAyat == 0){
            for (Integer ayat : ayatList){
                totalAyat += ayat;
            }
        }
        */

        //totalAyat = 0;
        if(totalAyat == 0){
            for (Integer ayat : ayatInJuzList){
                totalAyat += ayat;
            }
        }


        int readedAyat = 0;
        int suratPos = model.getNomor();
        for(int i = 1; i< suratPos; i++){
            readedAyat += ayatList.get(i-1);
        }
        readedAyat += model.getAyat();

        int totalPerJuz = 0;
        for (int x = 0; x < ayatInJuzList.size(); x++){
            totalPerJuz += ayatInJuzList.get(x);
            if(readedAyat <= totalPerJuz){
                return x+1;
            }
        }
        return 0;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int swipeLeftColor() {
        return Color.GREEN;
    }

    @Override
    public String swipeLeftTextString() {
        return "delete";
    }

    @Override
    public int swipeLeftTextColor() {
        return Color.WHITE;
    }

    @Override
    public int swipeFlags() {
        return ItemTouchHelper.LEFT|ItemTouchHelper.START;
    }
}
