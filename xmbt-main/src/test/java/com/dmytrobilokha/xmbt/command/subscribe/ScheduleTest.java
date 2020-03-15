package com.dmytrobilokha.xmbt.command.subscribe;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalTime;

@Test(groups = "unit")
public class ScheduleTest {

    @DataProvider(name = "daysOfWeekPermutations")
    public Object[][] generateDaysOfWeekPermutations() {
        Object[][] result = new Object[128][1];
        for (int i = 0; i < 128; i++) {
            result[i] = new Object[]{(byte) i};
        }
        return result;
    }

    @Test(dataProvider = "daysOfWeekPermutations")
    public void encodingMatchesDecoding(byte encodedDaysOfWeek) {
        Schedule schedule = new Schedule(LocalTime.MIDNIGHT, encodedDaysOfWeek);
        Assert.assertEquals(schedule.getDaysOfWeekEncoded(), encodedDaysOfWeek
                , "Encode -> decode -> encode operation doesn't give the initial encode value");
    }

}
