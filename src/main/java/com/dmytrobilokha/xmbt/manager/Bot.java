package com.dmytrobilokha.xmbt.manager;

public interface Bot extends Runnable {

    String getName();

    void setMessageQueueClient(DuplexMessageQueueClient messageQueueClient);

}
