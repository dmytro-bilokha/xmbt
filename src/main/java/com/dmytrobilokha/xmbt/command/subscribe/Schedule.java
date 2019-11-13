package com.dmytrobilokha.xmbt.command.subscribe;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;

public class Schedule implements Serializable {

    @Nonnull
    private final LocalTime time;
    @Nonnull
    private final EnumSet<DayOfWeek> dayOfWeeks;

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

}
