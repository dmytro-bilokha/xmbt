package com.dmytrobilokha.xmbt.command.subscribe;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.Locale;
import java.util.stream.Collectors;

public class Schedule implements Serializable {

    @Nonnull
    private final LocalTime time;
    @Nonnull
    private final EnumSet<DayOfWeek> dayOfWeeks;

    Schedule(@Nonnull LocalTime time, byte encodedDaysOfWeek) {
        this.time = time;
        this.dayOfWeeks = decodeDaysOfWeek(encodedDaysOfWeek);
    }

    Schedule(@Nonnull LocalTime time, @Nonnull EnumSet<DayOfWeek> dayOfWeeks) {
        this.time = time;
        this.dayOfWeeks = dayOfWeeks;
    }

    @CheckForNull
    LocalDateTime getNext() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime todaySchedule = LocalDateTime.of(today, time);
        if (dayOfWeeks.isEmpty()) {
            if (todaySchedule.isBefore(now)) {
                return null;
            }
            return todaySchedule;
        }
        if (dayOfWeeks.contains(today.getDayOfWeek()) && now.isBefore(todaySchedule)) {
            return todaySchedule;
        }
        for (int i = 1; i < 6; i++) {
            LocalDate incomingDay = today.plusDays(i);
            if (dayOfWeeks.contains(incomingDay.getDayOfWeek())) {
                return LocalDateTime.of(incomingDay, time);
            }
        }
        return null;
    }

    @Nonnull
    LocalTime getTime() {
        return time;
    }

    private EnumSet<DayOfWeek> decodeDaysOfWeek(byte encoded) {
        int encodedDaysSchedule = Byte.toUnsignedInt(encoded);
        DayOfWeek[] allDays = DayOfWeek.values();
        EnumSet<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
        for (int i = 0; i < 7; i++) {
            if ((encodedDaysSchedule & 1) != 0) {
                result.add(allDays[i]);
            }
            encodedDaysSchedule = encodedDaysSchedule >>> 1;
        }
        return result;
    }

    byte getDaysOfWeekEncoded() {
        var result = 0;
        for (DayOfWeek day : DayOfWeek.values()) {
            if (dayOfWeeks.contains(day)) {
                result = result | 0b10000000;
            }
            result = result >>> 1;
        }
        return (byte) result;
    }

    @Nonnull
    String getDisplayString() {
        String daysString = dayOfWeeks
                .stream()
                .sorted()
                .map(d -> d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .collect(Collectors.joining(" "));
        return daysString.isEmpty() ? time.toString() : time + " " + daysString;
    }

}
