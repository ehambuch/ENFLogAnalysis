package de.erichambuch.enfloganalysis;

import androidx.room.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.erichambuch.enfloganalysis.database.ENFDatabase;
import de.erichambuch.enfloganalysis.database.ENFEntry;
import de.erichambuch.enfloganalysis.database.ExposureChecks;

/**
 * Parser for ENF - Exposure Notification Framework Logfile as exported by Google settings.
 */
public class ENFParser {

    private final ENFDatabase enfDatabase;

    public ENFParser(ENFDatabase database) {
        enfDatabase = database;
    }

    /**
     * Add a new entry from the ENF logfile (JSON) to database.
     *
     * @param sharedJson the JSON data
     * @return new entry
     * @throws JSONException on any parse error
     */
    @Transaction
    public ENFEntry addNewEntry(String sharedJson) throws JSONException {
        List<ExposureChecks> newExposureChecks = parseENFLog(sharedJson);
        ENFEntry enfEntry = new ENFEntry();
        enfEntry.downloadDate = LocalDate.now();
        enfEntry.uid = enfEntry.downloadDate.toEpochDay(); // create a unique primary key
        enfDatabase.enfEntryDao().insertAll(enfEntry);
        for (ExposureChecks check : newExposureChecks)
            check.enfEntryUid = enfEntry.uid; // set foreign key relation (so we already need a valid key)
        enfDatabase.exposureChecksDao().insert(newExposureChecks.toArray(new ExposureChecks[0]));
        return enfEntry;

    }

    /**
     * Parse ENF logfile from Google export.
     *
     * @param json the JSON file data
     * @return parsed ExposureChecks
     * @throws JSONException on any parsing error
     */
    public static List<ExposureChecks> parseENFLog(String json) throws JSONException {
        List<ExposureChecks> newExposureChecks = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);
        final int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            ExposureChecks checks = new ExposureChecks();
            checks.enfTimestamp = parseJsonTimestamp(jsonObject.getString("timestamp"));
            checks.appName = jsonObject.getString("appName");
            checks.hash = jsonObject.getString("hash");
            checks.matchesCount = jsonObject.getInt("matchesCount");
            checks.keyCount = jsonObject.getInt("keyCount");
            newExposureChecks.add(checks);
        }

        return newExposureChecks;
    }

    /**
     * Der Export enthaelt verschiedene Datumsformate <code>30. Oktober 2020, 08:25</code> oder <code>August 2, 2020 08:31</code>.
     * TODO: unklares Format von Google: https://github.com/felixlen/ena_log/issues/2
     *
     * @param text
     * @return parsed date and time
     */
    public static LocalDateTime parseJsonTimestamp(String text) {
        String dateString = text.substring(0, text.indexOf(','));
        String timeString = text.substring(text.indexOf(',') + 2);
        try {
            LocalDate date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.GERMANY).parse(dateString, LocalDate::from);
            LocalTime time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.GERMANY).withZone(ZoneId.systemDefault()).parse(timeString, LocalTime::from);
            return LocalDateTime.of(date, time);
        } catch (DateTimeParseException e) {
            // https://github.com/felixlen/ena_log/issues/2
            LocalDate date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault()).parse(dateString, LocalDate::from);
            LocalTime time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault()).parse(timeString, LocalTime::from);
            return LocalDateTime.of(date, time);
        }
    }
}
