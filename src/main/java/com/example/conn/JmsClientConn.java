package com.example.conn;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.Map;

public class JmsClientConn {

    static final Logger LOGGER = LogManager.getLogger(JmsClientConn.class);

    private final Map<String, Object> jmsConnMap;
    private final PulsarConnectionFactory factory;
    private final JMSContext dftJmsContext;
    private final Connection dftJmsConnection;
    private final Destination dftDestination;

    private final int sessionMode = Session.AUTO_ACKNOWLEDGE;
    //private final int sessionMode = Session.CLIENT_ACKNOWLEDGE;

    private final boolean isQueue;
    private final String destName;

    public JmsClientConn(boolean isQueue, String destName, Map<String, Object> jmsConnMap) {
        this.isQueue = isQueue;
        this.destName = destName;
        this.jmsConnMap = jmsConnMap;

        this.factory = new PulsarConnectionFactory(jmsConnMap);
        LOGGER.trace("PulsarConnectionFactory created with config {}",
                this.jmsConnMap);

        this.dftJmsContext = newJmsContext();
        this.dftJmsConnection = newJmsConnection();
        this.dftDestination = newJmsDestination(dftJmsContext);
    }

    public void close() throws  JMSException {
        if (dftJmsConnection != null) dftJmsConnection.close();
        if (dftJmsContext != null) dftJmsContext.close();
    }

    public JMSContext getDftJmsContext() { return dftJmsContext; }
    public Connection getDftJmsConnection() { return dftJmsConnection; }
    public Destination getDftDestination() { return dftDestination; }

    public JMSContext newJmsContext() {
        JMSContext jmsContext = null;
        if (factory != null) {
            jmsContext = factory.createContext(sessionMode);
            LOGGER.trace("New JMS Context created with session mode {}: {}",
                    sessionMode, jmsContext.toString());
        }
        return jmsContext;
    }

    public Connection newJmsConnection() {
        Connection connection = null;
        if (factory != null) {
            try {
                if (isQueue) {
                    connection = factory.createQueueConnection();
                } else {
                    connection = factory.createTopicConnection();
                }
                LOGGER.trace("New JMS {} Connection created: {})",
                        (isQueue) ? "Queue" : "Topic",
                        connection.toString());
            }
            catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public Destination newJmsDestination(JMSContext jmsContext) {
        Destination destination;

        if (isQueue) {
            destination = jmsContext.createQueue(this.destName);
        } else {
            destination = jmsContext.createTopic(this.destName);
        }

        LOGGER.trace("New JMS {} destination created: {})",
                (isQueue) ? "Queue" : "Topic", this.destName);

        return destination;
    }
}