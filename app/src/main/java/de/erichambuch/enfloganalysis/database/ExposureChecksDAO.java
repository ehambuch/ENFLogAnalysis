package de.erichambuch.enfloganalysis.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExposureChecksDAO {

    @Insert
    public void insert(ExposureChecks... checks);

    @Query("select * from ExposureChecks where enfEntryUid = :enfUuid order by enfTimestamp desc")
    public List<ExposureChecks> readExposures(long enfUuid);
}
