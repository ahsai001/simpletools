package com.ahsailabs.simpletools.models;

import android.provider.BaseColumns;
import android.support.annotation.RequiresPermission;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ahsai on 5/17/2018.
 */
@Table(name = "ReadQuranLog", id = BaseColumns._ID)
public class ReadQuranLogModel extends Model implements Serializable {

    @Column(name = "nomor")
    private int nomor;

    @Column(name = "surat")
    private String surat;

    @Column(name = "ayat")
    private int ayat;

    @Column(name = "timestamp", index = true)
    public Date timestamp;

    public ReadQuranLogModel saveWithTimeStamp(){
        timestamp = Calendar.getInstance().getTime();
        save();
        return this;
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
        return new Select().from(ReadQuranLogModel.class).orderBy("timestamp desc").execute();
    }

    public static void deleteAll(){
        new Delete().from(ReadQuranLogModel.class).execute();
    }
}
