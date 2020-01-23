package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.Request;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.TextMessage;
import com.dmytrobilokha.xmbt.persistence.PersistenceService;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScheduledMessageDao {

    private static final String SCHEDULED_MESSAGE_SELECT =
            "SELECT id"
                    + " , schedule_time"
                    + " , week_days"
                    + " , next_datetime"
                    + " , request_id"
                    + " , request_sender"
                    + " , request_receiver"
                    + " , sender_address"
                    + " , message_text"
                    + " FROM scheduled_message";

    @Nonnull
    private final PersistenceService persistenceService;

    public ScheduledMessageDao(@Nonnull PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    void insert(@Nonnull Collection<ScheduledMessage> messages) throws SQLException {
        if (messages.isEmpty()) {
            return;
        }
        persistenceService.executeTransaction(con -> executeInsert(con, messages));
    }

    void insert(@Nonnull ScheduledMessage message) throws SQLException {
        persistenceService.executeTransaction(con -> executeInsert(con, List.of(message)));
    }

    private void executeInsert(
            @Nonnull Connection connection, @Nonnull Collection<ScheduledMessage> messages) throws SQLException {
        try (var insertStatement = connection.prepareStatement(
                "INSERT INTO scheduled_message"
                        + " ( schedule_time"
                        + " , week_days"
                        + " , next_datetime"
                        + " , request_id"
                        + " , request_sender"
                        + " , request_receiver"
                        + " , sender_address"
                        + " , message_text"
                        + " ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        )) {
            for (ScheduledMessage message : messages) {
                int position = 0;
                insertStatement.setTime(++position, Time.valueOf(message.getSchedule().getTime()));
                insertStatement.setByte(++position, message.getSchedule().getDaysOfWeekEncoded());
                insertStatement.setTimestamp(++position, Timestamp.valueOf(message.getDateTime()));
                insertStatement.setLong(++position, message.getRequestMessage().getId());
                insertStatement.setString(++position, message.getRequestMessage().getSender());
                insertStatement.setString(++position, message.getRequestMessage().getReceiver());
                insertStatement.setString(++position, message.getRequestMessage().getTextMessage().getAddress());
                insertStatement.setString(++position, message.getRequestMessage().getTextMessage().getText());
                insertStatement.executeUpdate();
            }
        }
        connection.commit();
    }

    @Nonnull
    List<ScheduledMessage> fetchSubscriptionsByAddress(@Nonnull String address) throws SQLException {
        return persistenceService.executeQuery(con -> executeSelectByAddress(con, address));
    }

    @Nonnull
    private List<ScheduledMessage> executeSelectByAddress(
            @Nonnull Connection connection, @Nonnull String address) throws SQLException {
        try (var selectStatement = connection.prepareStatement(
                SCHEDULED_MESSAGE_SELECT + " WHERE sender_address=? ORDER BY id ASC"
        )) {
            selectStatement.setString(1, address);
            try (var resultSet = selectStatement.executeQuery()) {
                return mapScheduledMessages(resultSet);
            }
        }
    }

    @CheckForNull
    private Long findIdByAddressAndOrder(
            @Nonnull Connection connection
            , @Nonnull String address
            , int subscriptionNumber) throws SQLException {
        try (var selectStatement = connection.prepareStatement(
                "SELECT id FROM scheduled_message WHERE sender_address=? ORDER BY id ASC LIMIT ?, 1"
        )) {
            selectStatement.setString(1, address);
            selectStatement.setInt(2, subscriptionNumber - 1);
            try (var resultSet = selectStatement.executeQuery()) {
                var hasNext = resultSet.next();
                if (!hasNext) {
                    return null;
                }
                return resultSet.getLong("id");
            }
        }
    }

    @Nonnull
    int deleteSubscriptionByAddressAndOrder(@Nonnull String address, int subscriptionNumber) throws SQLException {
        Long id = persistenceService.executeQuery(con -> findIdByAddressAndOrder(con, address, subscriptionNumber));
        if (id == null) {
            return 0;
        }
        return persistenceService.executeUpdateAutoCommitted(con -> executeDeleteByAddressAndId(con, address, id));
    }

    private int executeDeleteByAddressAndId(
            @Nonnull Connection connection, @Nonnull String address, long id) throws SQLException {
        try (var deleteStatement = connection.prepareStatement(
                "DELETE FROM scheduled_message WHERE sender_address = ? AND id = ?"
        )) {
            deleteStatement.setString(1, address);
            deleteStatement.setLong(2, id);
            return deleteStatement.executeUpdate();
        }
    }

    @Nonnull
    private List<ScheduledMessage> mapScheduledMessages(@Nonnull ResultSet resultSet) throws SQLException {
        List<ScheduledMessage> messages = new ArrayList<>();
        while (resultSet.next()) {
            TextMessage textMessage = new TextMessage(
                    resultSet.getString("sender_address"), resultSet.getString("message_text"));
            RequestMessage requestMessage = new RequestMessage(
                    resultSet.getLong("request_id")
                    , resultSet.getString("request_sender")
                    , resultSet.getString("request_receiver")
                    , Request.RESPOND
                    , textMessage
            );
            Schedule schedule = new Schedule(
                    resultSet.getTime("schedule_time").toLocalTime(), resultSet.getByte("week_days"));
            LocalDateTime nextAlarm = resultSet.getTimestamp("next_datetime").toLocalDateTime();
            Long id = resultSet.getLong("id");
            messages.add(new ScheduledMessage(id, nextAlarm, schedule, requestMessage));
        }
        return messages;
    }

    @Nonnull
    List<ScheduledMessage> fetchScheduledMessagesBefore(@Nonnull LocalDateTime dateTime) throws SQLException {
        return persistenceService.executeQuery(con -> executeSelectByDateTime(con, dateTime));
    }

    @Nonnull
    private List<ScheduledMessage> executeSelectByDateTime(
            @Nonnull Connection connection, @Nonnull LocalDateTime dateTime) throws SQLException {
        try (var selectStatement = connection.prepareStatement(
                SCHEDULED_MESSAGE_SELECT + " WHERE next_datetime < ?"
        )) {
            selectStatement.setTimestamp(1, Timestamp.valueOf(dateTime));
            try (var resultSet = selectStatement.executeQuery()) {
                return mapScheduledMessages(resultSet);
            }
        }
    }

    int delete(@Nonnull ScheduledMessage message) throws SQLException {
        Long messageId = message.getId();
        if (messageId == null) {
            return 0;
        }
        return persistenceService.executeUpdateAutoCommitted(con -> executeDelete(con, messageId));
    }

    private int executeDelete(@Nonnull Connection connection, @Nonnull Long messageId) throws SQLException {
        try (var deleteStatement = connection.prepareStatement(
                "DELETE FROM scheduled_message WHERE id=?"
        )) {
            deleteStatement.setLong(1, messageId);
            return deleteStatement.executeUpdate();
        }
    }

    void updateDateTime(
            @Nonnull ScheduledMessage message, @Nonnull LocalDateTime dateTime) throws SQLException {
        Long messageId = message.getId();
        if (messageId == null) {
            throw new IllegalArgumentException("Cannot update entity " + message + " in the DB, it hasn't been saved");
        }
        persistenceService.executeUpdateAutoCommitted(con -> executeUpdateNextDateTime(con, messageId, dateTime));
    }

    private int executeUpdateNextDateTime(
            @Nonnull Connection connection
            , @Nonnull Long messageId
            , @Nonnull LocalDateTime dateTime
    ) throws SQLException {
        try (var updateStatement = connection.prepareStatement(
                "UPDATE scheduled_message SET next_datetime=? WHERE id=?"
        )) {
            updateStatement.setTimestamp(1, Timestamp.valueOf(dateTime));
            updateStatement.setLong(2, messageId);
            return updateStatement.executeUpdate();
        }
    }

}
