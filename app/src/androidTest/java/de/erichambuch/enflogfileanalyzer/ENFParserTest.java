package de.erichambuch.enflogfileanalyzer;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.List;

import de.erichambuch.enflogfileanalyzer.database.ExposureChecks;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ENFParserTest {

    @Test
    public void testDateParsing() {
        assertEquals(LocalDateTime.of(2020, 10, 31, 8, 10), ENFParser.parseJsonTimestamp("31. Oktober 2020, 08:10"));
        // TODO assertEquals(LocalDateTime.of(2020, 8, 2, 8, 31), ENFParser.parseJsonTimestamp("August 2, 2020 08:31"));
    }

    @Test
    public void testParseENF() throws JSONException {
        List<ExposureChecks> checksList = ENFParser.parseENFLog("[{\"timestamp\":\"31. Oktober 2020, 08:10\",\"keyCount\":226660,\"matchesCount\":2,\"appName\":\"Warn-App\",\"hash\":\"qqTC4zpbUaAMfK\\/pSoikjdfLwCs4IRWFf3yPSy78Z6U=\"},{\"timestamp\":\"30. Oktober 2020, 08:25\",\"keyCount\":244428,\"matchesCount\":2,\"appName\":\"Warn-App\",\"hash\":\"ORxtIYsKyYVflcyFPS6S+TBAaS11wzj4HGqcqpfiMu4=\"}]");
        assertEquals(2, checksList.size());
        assertEquals(226660, checksList.get(0).keyCount);
        // TODO ...
    }
}
