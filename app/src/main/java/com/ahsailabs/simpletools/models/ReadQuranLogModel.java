package com.ahsailabs.simpletools.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.ahsailabs.simpletools.cores.BaseApp;
import com.ahsailabs.sqlitewrapper.SQLiteWrapper;
import com.ahsailabs.sqlwannotation.Column;
import com.ahsailabs.sqlwannotation.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import kotlinx.android.parcel.Parcelize;

/**
 * Created by ahsai on 5/17/2018.
 */
@Table()
public class ReadQuranLogModel extends SQLiteWrapper.TableClass implements Serializable, Parcelable {

    private String docId;

    @Column(name = "nomor")
    private int nomor;

    @Column(name = "surat")
    private String surat;

    @Column(name = "ayat")
    private int ayat;

    protected ReadQuranLogModel(Parcel in) {
        docId = in.readString();
        nomor = in.readInt();
        surat = in.readString();
        ayat = in.readInt();
        _created_at = (Date) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(docId);
        dest.writeInt(nomor);
        dest.writeString(surat);
        dest.writeInt(ayat);
        dest.writeSerializable(_created_at);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReadQuranLogModel> CREATOR = new Creator<ReadQuranLogModel>() {
        @Override
        public ReadQuranLogModel createFromParcel(Parcel in) {
            return new ReadQuranLogModel(in);
        }

        @Override
        public ReadQuranLogModel[] newArray(int size) {
            return new ReadQuranLogModel[size];
        }
    };

    @Override
    protected void getObjectData(List<Object> dataList) {
        ReadQuranLogModelSQLWHelper.getObjectData(dataList, this);
    }

    @Override
    protected void setObjectData(List<Object> dataList) {
        ReadQuranLogModelSQLWHelper.setObjectData(dataList, this);
    }

    @Override
    protected String getDatabaseName() {
        return BaseApp.DATABASE_NAME;
    }

    public ReadQuranLogModel(){
        super();
    }

    public ReadQuranLogModel(int nomor, String surat, int ayat) {
        super();
        this.nomor = nomor;
        this.surat = surat;
        this.ayat = ayat;
    }

    public int getNomor() {
        return nomor;
    }

    public void setNomor(int nomor) {
        this.nomor = nomor;
    }

    
    public String getSurat() {
        return surat;
    }

    public void setSurat(String surat) {
        this.surat = surat;
    }

    public int getAyat() {
        return ayat;
    }

    public void setAyat(int ayat) {
        this.ayat = ayat;
    }

    public static List<ReadQuranLogModel> getAllReadQuranLogList(){
        return SQLiteWrapper.of(BaseApp.DATABASE_NAME).findAll(null, ReadQuranLogModel.class,SQLiteWrapper.CREATED_AT+" desc", null, null);
    }

    public static void deleteAll(){
        SQLiteWrapper.of(BaseApp.DATABASE_NAME).deleteAll(null, ReadQuranLogModel.class);
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
