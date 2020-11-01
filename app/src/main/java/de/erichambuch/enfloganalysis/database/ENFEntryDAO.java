package de.erichambuch.enfloganalysis.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface ENFEntryDAO {

    @Insert
    void insertAll(ENFEntry... enfEntries);

    @Query("select * from ENFEntry where downloadDate = :today"
    )
    public ENFEntry findByDate(LocalDate today);

    @Query("select * from ENFEntry order by downloadDate desc limit 1")
    public ENFEntry findLatest();

    @Query("select * from ENFEntry order by downloadDate desc")
    public List<ENFEntry> listLatestEntries();
}


