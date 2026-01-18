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
package net.skinsrestorer.shared.listeners;

import lombok.RequiredArgsConstructor;
import net.skinsrestorer.shared.codec.SRInputReader;
import net.skinsrestorer.shared.codec.SRProxyPluginMessage;
import net.skinsrestorer.shared.listeners.event.SRProxyMessageEvent;
import net.skinsrestorer.shared.log.SRLogger;
import net.skinsrestorer.shared.plugin.SRPlatformAdapter;
import net.skinsrestorer.shared.utils.ProxyAckTracker;
import net.skinsrestorer.shared.utils.SRHelpers;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class SRProxyMessageAdapter {
    private final SRLogger logger;
    private final SRPlatformAdapter adapter;
    private final GUIActionListener guiActionListener;
    private final ProxyAckTracker proxyAckTracker;

    public void handlePluginMessage(SRProxyMessageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.getChannel().equals(SRHelpers.MESSAGE_CHANNEL)) {
            return;
        }

        if (!event.isSenderServerConnection() || !event.isReceiverProxyPlayer()) {
            event.setCancelled(true);
            return;
        }

        adapter.runAsync(() -> {
            SRInputReader in = new SRInputReader(event.getData());
            SRProxyPluginMessage.ChannelPayload<?> msg = SRProxyPluginMessage.CODEC.read(in).channelPayload();
            SRHelpers.mustSupply(() -> switch (msg) {
                case SRProxyPluginMessage.GUIActionListChannelPayload(var actions) ->
                        () -> guiActionListener.handle(event.getPlayer(), actions);
                case SRProxyPluginMessage.AckChannelPayload(var ackId, var serverSrVersion) ->
                        () -> proxyAckTracker.receivedAck(event.getPlayer(), ackId, serverSrVersion);
                case SRProxyPluginMessage.UnknownChannelPayload ignored ->
                        () -> logger.warning("Received unknown channel payload from server (Make sure the server and proxy are running the same version of SkinsRestorer) %s".formatted(SRHelpers.DOWNLOAD_URL));
            });
        });
    }
}
