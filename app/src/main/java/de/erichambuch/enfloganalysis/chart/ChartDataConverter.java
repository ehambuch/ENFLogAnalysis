package de.erichambuch.enfloganalysis.chart;

import android.content.res.Resources;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import de.erichambuch.enfloganalysis.R;
import de.erichambuch.enfloganalysis.database.ENFEntry;
import de.erichambuch.enfloganalysis.database.ENFEntryDAO;
import de.erichambuch.enfloganalysis.database.ExposureChecks;
import de.erichambuch.enfloganalysis.database.ExposureChecksDAO;

/**
 * Convert ENF Log data to chart data to be displayed. This class also contains the logic to determinate the risk contacts.
 */
public class ChartDataConverter {

    public static final class ChartData {
        public LocalDate date;
        // number of matches
        public int matchesCount;
        // attentation: #contacts could be the same person!
        public float riskContacts;
        // optional: explanation of data point
        public String explanation;

        public ChartData(LocalDate d) {
            this.date = d;
        }

        /**
         * X axis label.
         * @return the label
         */
        public String label() {
            return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + "\n" +
                    date.getDayOfMonth() + "." + date.getMonthValue() + ".";
        }
    }

    /**
     * Display up to 30 days to the past.
     */
    public static final int DAYS_BACK_IN_HISTORY = 30;

    private final ENFEntryDAO enfEntryDAO;
    private final ExposureChecksDAO exposureChecksDAO;
    private final Resources resources;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withZone(ZoneId.systemDefault());

    public ChartDataConverter(Resources resources, ENFEntryDAO entryDAO, ExposureChecksDAO exposureChecksDAO) {
        this.enfEntryDAO = entryDAO;
        this.resources = resources;
        this.exposureChecksDAO = exposureChecksDAO;
    }

    public Map<LocalDate, ChartData> convertToChartData() {
        Map<LocalDate, ChartData> fullExposureMap = new TreeMap<>();

        // Step 1: Read and condense all ENF log history
        final LocalDate maxDateInThePast = LocalDate.now().minusDays(DAYS_BACK_IN_HISTORY);
        for (ENFEntry enfEntry : enfEntryDAO.listLatestEntries()) {
            if (enfEntry.downloadDate.isBefore(maxDateInThePast))
                break; // only scan the last xx days, rest of history isn't analyzed anymore
            for (ChartData exposures : condenseExposureChecks(enfEntry).values()) {
                ChartData chartData = fullExposureMap.get(exposures.date);
                if (chartData == null) {
                    fullExposureMap.put(exposures.date, exposures);
                } else {
                    if (exposures.matchesCount >= chartData.matchesCount) { // take maximum value
                        chartData.matchesCount = exposures.matchesCount;
                    }
                }
            }
        }

        // Step 2: fill up history of last 30 days in case one day is missing
        LocalDate now = LocalDate.now();
        for (int i = 0; i <= DAYS_BACK_IN_HISTORY; i++) {
            LocalDate date = now.minusDays(i);
            if (!fullExposureMap.containsKey(date)) {
                fullExposureMap.put(date, new ChartData(date)); // fill with dummy value
            }
        }

        return fullExposureMap;
    }

    /**
     * Calculate from the exposure history the risk contact dates.
     *
     * @param fullExposureMap {@link #DAYS_BACK_IN_HISTORY} entries backwards sorted
     * @return updated exposure history - same List and Objects as input is updated
     */
    public List<ChartData> calculateRiskHistory(Map<LocalDate, ChartData> fullExposureMap) {
        // Step 3: Now setup the complete exposure history sorted from today back in history
        ArrayList<ChartData> exposureHistory = new ArrayList<>(fullExposureMap.values());
        Collections.sort(exposureHistory, new Comparator<ChartData>() {
            @Override
            public int compare(ChartData o1, ChartData o2) {
                return o2.date.compareTo(o1.date);
            }
        });
        // Step 4: now try to find out when risk contacts really occured
        int length = exposureHistory.size();
        for (int i = 0; i < length - 1; i++) {
            // compare current day with yesterday and try to guess what happened
            ChartData todayChartData = exposureHistory.get(i);
            ChartData yesterdayChartData = exposureHistory.get(i + 1);
            // Case 1: matches has decreased, so a risk contact must have been 14 days before!
            // see https://github.com/corona-warn-app/cwa-app-android/issues/1302
            if (todayChartData.matchesCount < yesterdayChartData.matchesCount
                    && yesterdayChartData.matchesCount > 0) {
                LocalDate dateOfRiskContact = todayChartData.date.minusDays(14);
                ChartData riskContactChartData = fullExposureMap.get(dateOfRiskContact);
                if (riskContactChartData == null)
                    continue; // out of our range of days back
                riskContactChartData.riskContacts += (yesterdayChartData.matchesCount - todayChartData.matchesCount);
                riskContactChartData.explanation = resources.getString(R.string.text_riskcontact14days) +
                        dateFormatter.format(todayChartData.date);
            }
            // Cast 2: TODO: possible case not covered: value stays constant for more than 14 days, then we had new matches but also a risk contact
            // Case 3: matches has increased, but we are not sure when this happened in the past...
            // so we add a kind of "probability distribution" to our findings related the past days
            // see https://github.com/corona-warn-app/cwa-backlog/issues/23
            // and https://github.com/corona-warn-app/cwa-wishlist/issues/100
            if (todayChartData.matchesCount > yesterdayChartData.matchesCount) {
                float riskContacts = (todayChartData.matchesCount - yesterdayChartData.matchesCount);
                float[] factors = {0.0f, 0.3f, 0.5f, 0.7f, 1.0f, 0.8f, 0.6f, 0.4f, 0.2f}; // probability distribution of contact
                for (int f = 0; f < factors.length && (f + i + 1) < length; f++) {
                    ChartData riskyDays = exposureHistory.get(f + i + 1);
                    riskyDays.riskContacts += (riskContacts * factors[f]); // sum up the contacts
                    if (riskyDays.explanation == null)
                        riskyDays.explanation = resources.getString(R.string.text_riskcontactprobability) +
                                dateFormatter.format(todayChartData.date);
                }
            }
        }

        return exposureHistory;
    }

    /**
     * Condense different ENF queries per day, as multiple request for one day may occure.
     *
     * @param enfEntry ENF Log Entry
     * @return condensed map condensed exposure checks for that entry
     */
    private Map<LocalDate, ChartData> condenseExposureChecks(ENFEntry enfEntry) {
        Map<LocalDate, ChartData> exposureMap = new TreeMap<>();
        for (ExposureChecks checks : exposureChecksDAO.readExposures(enfEntry.uid)) {
            LocalDate date = checks.enfTimestamp.toLocalDate();
            ChartData chartData = exposureMap.get(date);
            if (chartData == null)
                exposureMap.put(date, chartData = new ChartData(date));
            chartData.matchesCount += checks.matchesCount;
        }
        return exposureMap;
    }
}
