package com.microsoft.smartalarm;
import com.microsoft.mimickeralarm.model.Alarm;
import org.junit.Test;
import static org.junit.Assert.*;

public class AlarmTest {
    @Test
    //@PrepareForTest(GeneralUtilities.class)
    public void testAlarm_enableSnooze() throws Exception {
        /*PowerMockito.mockStatic(GeneralUtilities.class);
        when(GeneralUtilities.defaultRingtone()).thenReturn(null);
        when(GeneralUtilities.deviceHasFrontFacingCamera()).thenReturn(true);
        when(GeneralUtilities.deviceHasRearFacingCamera()).thenReturn(false);*/

        //GeneralUtilities util = Mockito.mock(GeneralUtilities.class);
        /*when(util.defaultRingtone()).thenReturn(null);
        when(util.deviceHasFrontFacingCamera()).thenReturn(true);
        when(util.deviceHasRearFacingCamera()).thenReturn(false);*/

        Alarm alarm = new Alarm(null);
        //Alarm alarm = mock(Alarm.class);
        alarm.setSnooze(true);
        assertEquals(true, alarm.shouldSnooze());
    }

    @Test
    public void testAlarm_disableSnooze() throws Exception {
        Alarm alarm = new Alarm(null);
        //Alarm alarm = mock(Alarm.class);
        alarm.setSnooze(false);
        assertEquals(false, alarm.shouldSnooze());
    }
}