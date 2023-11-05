package com.dmytrobilokha.xmbt.xmpp;

import org.jivesoftware.smack.ConnectionListener;

import javax.annotation.CheckForNull;

public class XmppConnectionListener implements ConnectionListener {

    private volatile Exception connectionException;
    @Override
    public void connectionClosedOnError(Exception e) {
        connectionException = e;
    }

    @CheckForNull
    public Exception getConnectionException() {
        return connectionException;
    }

}
