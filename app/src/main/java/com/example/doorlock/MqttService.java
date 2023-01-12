package com.example.doorlock;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Log;

import com.example.doorlock.database.Message;
import com.example.doorlock.database.MqttDatabase;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;

public class MqttService {

    final private String host = "efce362f5796485196e48e43b08e5e80.s2.eu.hivemq.cloud"; //devel.ceelabs.com
    final private String username = "DoorLockAppClient";        //zaverecneprace
    final private String passwordMQTT = "Tuke12345";            //igieK4Thu6dia6x

    // TODO: UUID GENERATION AND SAVE IT TO SHARED PREFERENCES
    final private String topic = "hivemq/test/doorlockapp/test";    // "HomeAutomation/UUID" + getter from shared perferences
    final private String topicState = "/State";
    final private String topicEvent = "/Event";

    final private String lwtMessage = "{\"status\":\"OFFLINE\", \"reason\":\"Connection closed unexpectedly\"}";
    final private String connectMessage = "{\"status\":\"ONLINE\", \"version\":x.x}";

    private MqttDatabase database;

    private Mqtt3AsyncClient clientV3;

    public MqttService(MqttDatabase db){
        database = db;
    }

    public void startService(){
        buildClientV3();
        connectClientV3();
        subscribeClientV3();
    }

    private void buildClientV3(){
        clientV3 = MqttClient.builder()
                .useMqttVersion3()
                .serverHost(host)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .automaticReconnectWithDefaultConfig()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(passwordMQTT))
                .applySimpleAuth()
                .buildAsync();
    }


    private void connectClientV3(){
        clientV3.connectWith()
                .willPublish()
                    .topic(topic + topicState)
                    .qos(MqttQos.AT_LEAST_ONCE)     //At most once (0)  At least once (1)  Exactly once (2). TODO: QOS WAL ?
                    .payload(lwtMessage.getBytes())
                    .retain(true)   // RETAIN PRI LWT TODO: TRUE/FALSE ?
                .applyWillPublish()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(passwordMQTT))
                .applySimpleAuth()
                .send();
    }


    private void subscribeClientV3(){
        clientV3.subscribeWith()
                .topicFilter(topic)
                .send();
        // set a callback that is called when a message is received (using the async API style)
        clientV3.toAsync().publishes(ALL, publish -> {

            Message message = new Message();
            message.message = String.valueOf(UTF_8.decode(publish.getPayload().get()));
            database.messageDao().insertMessage(message);

            Log.e("MQTT","Received message: " +
                    publish.getTopic() + " -> " +
                    UTF_8.decode(publish.getPayload().get()));
        });
    }

    public void ConnectionMessageV3(){
        clientV3.publishWith()
                .topic(topic  + topicEvent)
                .payload(UTF_8.encode(connectMessage))
                .retain(true)
                .send();
    }

    public void disconnectClientV3(){
        try {
            clientV3.disconnect();
        }catch (Exception e){
            Log.e("MQTT","CLIENT ALREADY DISCONNECTED");
        }
    }
}


/*
Co sa tyka pristupu k MQTT:
Server: devel.ceelabs.com, broker bezi aj na TCP/1883 nesifrovane (skor koli testovaniu) a jednak s TLS na TCP/8883.
Pri produkcnom rieseni budeme pouzivat broker.merito.tech iba s TLS na TCP/8883.
Login: zaverecneprace
Pass: igieK4Thu6dia6x

Zapol som RW pristup do HomeAutomation/#, mozeme si teda vymysliet nejaky topic v ktorom posleme vyzvu na dialog OPEN/CLOSE. Navrh je nasledovny:
a) topic v tvare HomeAutomation/[deviceID]/State
- ked sa mobilna aplikacia pripoji, nech tam posle spravu s priznakom RETAINED v tvare: {"status":"ONLINE", "version":x.x}
- zaroven do connectu nech definuje Last Will Testament, aby sa do toho isteho topicu poslalo ako retained: {"status":"OFFLINE", "reason":"Connection closed unexpectly"}
- tym padom pri subscribnuti sa na State budeme vzdy vediet aktualny stav klienta, ci je online alebo offline


b) topic v tvare HomeAutomation/[deviceID]/Notify
- sem by sme mohli pushnut nejaky message, na ktory bude reagovat mobilna aplikacia (navrhnite format)


c) topic v tvare HomeAutomation/[deviceID]/Event
- sem by sme mohli pushnut event code a skusit to spravit nejak univerzalnejsie, aby to vygenerovalo systemovy event
- na to by vedel byt potom naviazany napr. tasker,alebo ine systemove udalosti.
/to si este prejdeme/


 */
