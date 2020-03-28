package com.dmytrobilokha.xmbt.api.service.persistence;

import com.dmytrobilokha.xmbt.api.service.ThrowingConsumer;
import com.dmytrobilokha.xmbt.api.service.ThrowingFunction;
import com.dmytrobilokha.xmbt.api.service.ThrowingIntFunction;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;

public interface PersistenceService {

    <R> R executeQuery(@Nonnull ThrowingFunction<Connection, R, SQLException> query) throws SQLException;

    int executeUpdateAutoCommitted(
            @Nonnull ThrowingIntFunction<Connection, SQLException> operator) throws SQLException;

    void executeTransaction(
            @Nonnull ThrowingConsumer<Connection, SQLException> transactionConsumer) throws SQLException;

}
