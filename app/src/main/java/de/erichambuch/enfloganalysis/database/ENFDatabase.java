package de.erichambuch.enfloganalysis.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Local database to store all ENF logfiles for later analysis.
 */
@Database(entities = {ENFEntry.class, ExposureChecks.class}, version = 1, exportSchema = false)
@TypeConverters(TypeConverter.class)
public abstract class ENFDatabase extends RoomDatabase {
    public abstract ENFEntryDAO enfEntryDao();

    public abstract ExposureChecksDAO exposureChecksDao();
}
