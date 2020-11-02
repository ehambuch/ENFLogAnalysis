package de.erichambuch.enflogfileanalyzer.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity
public class ExposureChecks {

    @PrimaryKey(autoGenerate = true)
    public long exposeCheckKey;

    // relation to 1
    public long enfEntryUid;

    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    public LocalDateTime enfTimestamp;

    public int keyCount;

    public int matchesCount;

    public String appName;

    public String hash;
}

