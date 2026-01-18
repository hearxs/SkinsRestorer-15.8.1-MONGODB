package net.skinsrestorer.shared.storage.adapter.mongodb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.property.SkinType;
import net.skinsrestorer.api.property.SkinVariant;
import net.skinsrestorer.shared.gui.GUIUtils;
import net.skinsrestorer.shared.storage.adapter.StorageAdapter;
import net.skinsrestorer.shared.storage.model.cache.MojangCacheData;
import net.skinsrestorer.shared.storage.model.player.FavouriteData;
import net.skinsrestorer.shared.storage.model.player.HistoryData;
import net.skinsrestorer.shared.storage.model.player.LegacyPlayerData;
import net.skinsrestorer.shared.storage.model.player.PlayerData;
import net.skinsrestorer.shared.storage.model.skin.*;
import net.skinsrestorer.shared.subjects.messages.ComponentHelper;
import net.skinsrestorer.shared.subjects.messages.ComponentString;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDBAdapter implements StorageAdapter {
    private static final Gson GSON = new Gson();
    private static final Type LIST_HISTORY_TYPE = new TypeToken<List<HistoryData>>(){}.getType();
    private static final Type LIST_FAVOURITE_TYPE = new TypeToken<List<FavouriteData>>(){}.getType();
    private static final Type LIST_STRING_TYPE = new TypeToken<List<String>>(){}.getType();

    private MongoCollection<Document> playerDataCol;
    private MongoCollection<Document> playerSkinDataCol;
    private MongoCollection<Document> urlSkinDataCol;
    private MongoCollection<Document> urlIndexDataCol;
    private MongoCollection<Document> customSkinDataCol;
    private MongoCollection<Document> legacySkinDataCol;
    private MongoCollection<Document> legacyPlayerDataCol;
    private MongoCollection<Document> mojangCacheCol;
    private MongoCollection<Document> cooldownCol;

    private final MongoDBProvider provider;

    @Inject
    public MongoDBAdapter(MongoDBProvider provider) {
        this.provider = provider;
    }

    @Override
    public void init() {
        var db = provider.getDatabase();
        this.playerDataCol = db.getCollection("player_data");
        this.playerSkinDataCol = db.getCollection("player_skin_data");
        this.urlSkinDataCol = db.getCollection("url_skin_data");
        this.urlIndexDataCol = db.getCollection("url_index_data");
        this.customSkinDataCol = db.getCollection("custom_skin_data");
        this.legacySkinDataCol = db.getCollection("legacy_skin_data");
        this.legacyPlayerDataCol = db.getCollection("legacy_player_data");
        this.mojangCacheCol = db.getCollection("mojang_cache");
        this.cooldownCol = db.getCollection("cooldowns");
    }

    //region PlayerData
    @Override
    public Optional<PlayerData> getPlayerData(UUID uuid) throws StorageException {
        try {
            Document doc = playerDataCol.find(Filters.eq("_id", uuid.toString())).first();
            if (doc == null) return Optional.empty();

            SkinIdentifier skinId = doc.containsKey("skinIdentifier")
                    ? deserializeSkinIdentifier(doc.getString("skinIdentifier"))
                    : null;

            List<HistoryData> history = GSON.fromJson(doc.getString("history"), LIST_HISTORY_TYPE);
            List<FavouriteData> favourites = GSON.fromJson(doc.getString("favourites"), LIST_FAVOURITE_TYPE);

            // ✅ 修正 1: 使用 static factory method 'of'
            return Optional.of(PlayerData.of(uuid, skinId, history, favourites));
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void setPlayerData(UUID uuid, PlayerData data) {
        Document doc = new Document("_id", uuid.toString())
                .append("skinIdentifier", data.getSkinIdentifier() != null ? serializeSkinIdentifier(data.getSkinIdentifier()) : null)
                .append("history", GSON.toJson(data.getHistory()))
                .append("favourites", GSON.toJson(data.getFavourites()));
        playerDataCol.replaceOne(Filters.eq("_id", uuid.toString()), doc, new ReplaceOptions().upsert(true));
    }
    //endregion

    //region PlayerSkinData
    @Override
    public Optional<PlayerSkinData> getPlayerSkinData(UUID uuid) throws StorageException {
        try {
            Document doc = playerSkinDataCol.find(Filters.eq("_id", uuid.toString())).first();
            if (doc == null) return Optional.empty();

            String lastKnownName = doc.getString("lastKnownName");
            SkinProperty property = GSON.fromJson(doc.getString("property"), SkinProperty.class);
            long timestamp = doc.getLong("timestamp");

            return Optional.of(PlayerSkinData.of(uuid, lastKnownName, property, timestamp));
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void removePlayerSkinData(UUID uuid) {
        playerSkinDataCol.deleteOne(Filters.eq("_id", uuid.toString()));
    }

    @Override
    public void setPlayerSkinData(UUID uuid, PlayerSkinData skinData) {
        Document doc = new Document("_id", uuid.toString())
                .append("lastKnownName", skinData.getLastKnownName())
                .append("property", GSON.toJson(skinData.getProperty()))
                .append("timestamp", skinData.getTimestamp());
        playerSkinDataCol.replaceOne(Filters.eq("_id", uuid.toString()), doc, new ReplaceOptions().upsert(true));
    }
    //endregion

    //region URLSkinData
    private String buildUrlKey(String url, SkinVariant variant) {
        return url + "|" + (variant != null ? variant.name() : "null");
    }

    @Override
    public Optional<URLSkinData> getURLSkinData(String url, SkinVariant skinVariant) throws StorageException {
        try {
            String key = buildUrlKey(url, skinVariant);
            Document doc = urlSkinDataCol.find(Filters.eq("_id", key)).first();
            if (doc == null) return Optional.empty();

            String mineSkinId = doc.getString("mineSkinId");
            SkinProperty property = GSON.fromJson(doc.getString("property"), SkinProperty.class);
            SkinVariant variant = SkinVariant.valueOf(doc.getString("skinVariant"));

            return Optional.of(URLSkinData.of(url, mineSkinId, property, variant));
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void removeURLSkinData(String url, SkinVariant skinVariant) {
        String key = buildUrlKey(url, skinVariant);
        urlSkinDataCol.deleteOne(Filters.eq("_id", key));
    }

    @Override
    public void setURLSkinData(String url, URLSkinData skinData) {
        String key = buildUrlKey(url, skinData.getSkinVariant());
        Document doc = new Document("_id", key)
                .append("url", url)
                .append("mineSkinId", skinData.getMineSkinId())
                .append("property", GSON.toJson(skinData.getProperty()))
                .append("skinVariant", skinData.getSkinVariant().name());
        urlSkinDataCol.replaceOne(Filters.eq("_id", key), doc, new ReplaceOptions().upsert(true));
    }
    //endregion

    //region URLIndexData
    @Override
    public Optional<URLIndexData> getURLSkinIndex(String url) throws StorageException {
        try {
            Document doc = urlIndexDataCol.find(Filters.eq("_id", url)).first();
            if (doc == null) return Optional.empty();

            SkinVariant variant = SkinVariant.valueOf(doc.getString("skinVariant"));
            return Optional.of(URLIndexData.of(url, variant));
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void removeURLSkinIndex(String url) {
        urlIndexDataCol.deleteOne(Filters.eq("_id", url));
    }

    @Override
    public void setURLSkinIndex(String url, URLIndexData skinData) {
        Document doc = new Document("_id", url)
                .append("url", url)
                .append("skinVariant", skinData.getSkinVariant().name());
        urlIndexDataCol.replaceOne(Filters.eq("_id", url), doc, new ReplaceOptions().upsert(true));
    }
    //endregion

    //region CustomSkinData
    @Override
    public Optional<CustomSkinData> getCustomSkinData(String skinName) throws StorageException {
        try {
            String key = skinName.toLowerCase(Locale.ROOT);
            Document doc = customSkinDataCol.find(Filters.eq("_id", key)).first();
            if (doc == null) return Optional.empty();

            ComponentString displayName = doc.containsKey("displayName")
                    ? GSON.fromJson(doc.getString("displayName"), ComponentString.class)
                    : null;
            SkinProperty property = GSON.fromJson(doc.getString("property"), SkinProperty.class);

            return Optional.of(CustomSkinData.of(key, displayName, property));
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void removeCustomSkinData(String skinName) {
        String key = skinName.toLowerCase(Locale.ROOT);
        customSkinDataCol.deleteOne(Filters.eq("_id", key));
    }

    @Override
    public void setCustomSkinData(String skinName, CustomSkinData skinData) {
        String key = skinName.toLowerCase(Locale.ROOT);
        Document doc = new Document("_id", key)
                .append("displayName", skinData.getDisplayName() != null ? GSON.toJson(skinData.getDisplayName()) : null)
                .append("property", GSON.toJson(skinData.getProperty()));
        customSkinDataCol.replaceOne(Filters.eq("_id", key), doc, new ReplaceOptions().upsert(true));
    }

    @Override
    public int getTotalCustomSkins() {
        return (int) customSkinDataCol.countDocuments();
    }

    @Override
    public List<GUIUtils.GUIRawSkinEntry> getCustomGUISkins(int offset, int limit) {
        return customSkinDataCol.find()
                .skip(offset)
                .limit(limit)
                .into(new ArrayList<>())
                .stream()
                .map(doc -> {
                    String skinName = doc.getString("_id");
                    SkinIdentifier id = SkinIdentifier.ofCustom(skinName);
                    // ✅ 使用 ComponentHelper 转换纯文本为 JSON 组件
                    ComponentString name = ComponentHelper.convertPlainToJson(skinName);
                    String texture = extractTextureHash(doc.getString("property"));
                    return new GUIUtils.GUIRawSkinEntry(id, name, texture, Collections.emptyList());
                })
                .collect(Collectors.toList());
    }
    //endregion

    //region Legacy (Minimal Support)
    @Override
    public Optional<LegacySkinData> getLegacySkinData(String skinName) throws StorageException {
        return Optional.empty(); // Not used in modern versions
    }

    @Override
    public void removeLegacySkinData(String skinName) {}

    @Override
    public Optional<LegacyPlayerData> getLegacyPlayerData(String playerName) throws StorageException {
        return Optional.empty();
    }

    @Override
    public void removeLegacyPlayerData(String playerName) {}
    //endregion

    //region Player GUI Skins
    @Override
    public int getTotalPlayerSkins() {
        return (int) playerSkinDataCol.countDocuments();
    }

    @Override
    public List<GUIUtils.GUIRawSkinEntry> getPlayerGUISkins(int offset, int limit) {
        return playerSkinDataCol.find()
                .skip(offset)
                .limit(limit)
                .into(new ArrayList<>())
                .stream()
                .map(doc -> {
                    UUID uuid = UUID.fromString(doc.getString("_id"));
                    String lastKnownName = doc.getString("lastKnownName");
                    SkinIdentifier id = SkinIdentifier.ofPlayer(uuid);
                    // ✅ 同样使用 ComponentHelper
                    ComponentString name = ComponentHelper.convertPlainToJson(lastKnownName);
                    String texture = extractTextureHash(doc.getString("property"));
                    return new GUIUtils.GUIRawSkinEntry(id, name, texture, Collections.emptyList());
                })
                .collect(Collectors.toList());
    }
    //endregion

    //region Cache & Cooldown
    @Override
    public void purgeStoredOldSkins(long targetPurgeTimestamp) throws StorageException {
        try {
            Bson filter = Filters.lt("timestamp", targetPurgeTimestamp);
            customSkinDataCol.deleteMany(filter);
            urlSkinDataCol.deleteMany(filter);
            playerSkinDataCol.deleteMany(filter);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public Optional<MojangCacheData> getCachedUUID(String playerName) throws StorageException {
        try {
            Document doc = mojangCacheCol.find(Filters.eq("_id", playerName.toLowerCase(Locale.ROOT))).first();
            if (doc == null) return Optional.empty();

            UUID uuid = doc.containsKey("uuid") ? UUID.fromString(doc.getString("uuid")) : null;
            long timestamp = doc.getLong("timestamp");
            return Optional.of(MojangCacheData.of(uuid, timestamp));
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void setCachedUUID(String playerName, MojangCacheData cache) {
        Document doc = new Document("_id", playerName.toLowerCase(Locale.ROOT))
                .append("uuid", cache.getUniqueId().orElse(null))
                .append("timestamp", cache.getTimestamp());
        mojangCacheCol.replaceOne(Filters.eq("_id", playerName.toLowerCase(Locale.ROOT)), doc, new ReplaceOptions().upsert(true));
    }

    @Override
    public List<UUID> getAllCooldownProfiles() throws StorageException {
        try {
            return cooldownCol.distinct("owner", String.class)
                    .into(new ArrayList<>())
                    .stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public List<StorageCooldown> getCooldowns(UUID owner) throws StorageException {
        try {
            return cooldownCol.find(Filters.eq("owner", owner.toString()))
                    .into(new ArrayList<>())
                    .stream()
                    .map(doc -> new StorageCooldown(
                            UUID.fromString(doc.getString("owner")),
                            doc.getString("groupName"),
                            Instant.ofEpochMilli(doc.getLong("creationTime")),
                            Duration.ofMillis(doc.getLong("duration"))
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void setCooldown(UUID owner, String groupName, Instant creationTime, Duration duration) {
        Document doc = new Document()
                .append("owner", owner.toString())
                .append("groupName", groupName)
                .append("creationTime", creationTime.toEpochMilli())
                .append("duration", duration.toMillis());
        cooldownCol.insertOne(doc);
    }

    @Override
    public void removeCooldown(UUID owner, String groupName) {
        cooldownCol.deleteOne(Filters.and(
                Filters.eq("owner", owner.toString()),
                Filters.eq("groupName", groupName)
        ));
    }
    //endregion

    //region Helper Methods
    private String serializeSkinIdentifier(SkinIdentifier id) {
        return GSON.toJson(Map.of(
                "identifier", id.getIdentifier(),
                "skinVariant", id.getSkinVariant(),
                "skinType", id.getSkinType().name()
        ));
    }

    private SkinIdentifier deserializeSkinIdentifier(String json) {
        Map<String, Object> map = GSON.fromJson(json, Map.class);
        String identifier = (String) map.get("identifier");
        String typeStr = (String) map.get("skinType");
        SkinType type = SkinType.valueOf(typeStr);
        String variantStr = (String) map.get("skinVariant");
        SkinVariant variant = variantStr != null ? SkinVariant.valueOf(variantStr) : null;

        return switch (type) {
            case PLAYER -> SkinIdentifier.ofPlayer(UUID.fromString(identifier));
            case URL -> SkinIdentifier.ofURL(identifier, variant);
            case CUSTOM -> SkinIdentifier.ofCustom(identifier);
            case LEGACY -> SkinIdentifier.of(identifier, variant, SkinType.LEGACY);
        };
    }

    private String extractTextureHash(String propertyJson) {
        try {
            SkinProperty prop = GSON.fromJson(propertyJson, SkinProperty.class);
            // ✅ 修正 4: 使用 getter
            return prop.getValue(); // Base64 encoded texture
        } catch (Exception e) {
            return "";
        }
    }
    //endregion
}
