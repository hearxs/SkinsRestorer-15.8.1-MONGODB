/*
 * SkinsRestorer
 * Copyright (C) 2024  SkinsRestorer Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.skinsrestorer.shared.utils;

import lombok.RequiredArgsConstructor;
import net.skinsrestorer.builddata.BuildData;
import net.skinsrestorer.shared.codec.SRServerPluginMessage;
import net.skinsrestorer.shared.log.SRLogger;
import net.skinsrestorer.shared.plugin.SRPlatformAdapter;
import net.skinsrestorer.shared.subjects.SRProxyPlayer;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ProxyAckTracker {
    private final SRLogger logger;
    private final SRPlatformAdapter adapter;
    private final Set<String> verifiedServers = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> brokenServers = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Integer> serverNackCounts = new ConcurrentHashMap<>();

    public Optional<SRServerPluginMessage.SkinUpdateV3ChannelPayload.AckPayload> shouldAckPayload(SRProxyPlayer player) {
        var optionalServer = player.getCurrentServer();
        if (optionalServer.isEmpty()) {
            return Optional.empty();
        }

        var server = optionalServer.get();
        if (verifiedServers.contains(server)) {
            logger.debug("Backend server '%s' already verified. Skipping ACK payload.".formatted(server));
            return Optional.empty();
        }

        var ackId = UUID.randomUUID();
        logger.debug("Sending ACK payload to player '%s' with ACK id %s to backend server '%s'".formatted(player.getName(), ackId, server));

        adapter.runAsyncDelayed(() -> handleProxyServerState(server), 30, TimeUnit.SECONDS);

        return Optional.of(new SRServerPluginMessage.SkinUpdateV3ChannelPayload.AckPayload(ackId, BuildData.VERSION));
    }

    private void handleProxyServerState(String server) {
        if (verifiedServers.contains(server)) {
            logger.debug("Backend server '%s' is already verified, skipping state check.".formatted(server));
        } else if (brokenServers.contains(server)) {
            logger.debug("Backend server '%s' is already marked as broken, skipping state check.".formatted(server));
        } else if (serverNackCounts.compute(server, (key, count) -> count == null ? 1 : count + 1) >= 3) {
            logger.warning(("Backend server '%s' does likely not have SkinsRestorer installed or is not responding to ACK messages. " +
                    "Please make sure that the server has SkinsRestorer installed and is running the latest version. %s").formatted(server, SRHelpers.DOWNLOAD_URL));
            brokenServers.add(server);
            serverNackCounts.remove(server);
        } else {
            logger.debug(("Backend server '%s' did not respond to ACK message in time. " +
                    "This may indicate that the server is not running SkinsRestorer or is not responding to ACK messages.").formatted(server));
        }
    }

    public void receivedAck(SRProxyPlayer player, UUID ackId, String serverSrVersion) {
        logger.debug("Received ACK from player '%s' with ACK id %s".formatted(player.getName(), ackId));

        var optionalServer = player.getCurrentServer();
        if (optionalServer.isEmpty()) {
            return;
        }

        var server = optionalServer.get();
        if (!verifiedServers.add(server)) {
            logger.debug("Backend server '%s' already verified. Skipping version check.".formatted(server));
            return;
        }

        if (serverSrVersion.equalsIgnoreCase(BuildData.VERSION)) {
            logger.debug("Backend server '%s' is verified with SkinsRestorer version %s.".formatted(server, serverSrVersion));
        } else {
            logger.warning("Backend server '%s' is running a different version of SkinsRestorer (%s) than this proxy (%s). Make sure both server and proxy run the latest version of SkinsRestorer. %s"
                    .formatted(server, serverSrVersion, BuildData.VERSION, SRHelpers.DOWNLOAD_URL));
        }
    }
}
