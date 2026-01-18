package net.skinsrestorer.shared.storage.adapter.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBProvider {
    private MongoClient client;
    private String databaseName;

    public void init(String connectionString, String dbName) {
        this.databaseName = dbName;
        this.client = MongoClients.create(connectionString);
    }

    public MongoDatabase getDatabase() {
        return client.getDatabase(databaseName);
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
