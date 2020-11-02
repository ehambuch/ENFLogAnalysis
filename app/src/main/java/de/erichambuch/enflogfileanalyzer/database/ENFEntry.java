package de.erichambuch.enflogfileanalyzer.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity
public class ENFEntry {
    @PrimaryKey
    public long uid;

    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    public LocalDate downloadDate;
}
