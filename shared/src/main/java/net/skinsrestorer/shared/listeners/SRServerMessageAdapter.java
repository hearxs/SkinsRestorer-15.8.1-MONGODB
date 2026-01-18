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
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.builddata.BuildData;
import net.skinsrestorer.shared.api.SharedSkinApplier;
import net.skinsrestorer.shared.codec.SRInputReader;
import net.skinsrestorer.shared.codec.SRProxyPluginMessage;
import net.skinsrestorer.shared.codec.SRServerPluginMessage;
import net.skinsrestorer.shared.gui.SRInventory;
import net.skinsrestorer.shared.listeners.event.SRServerMessageEvent;
import net.skinsrestorer.shared.log.SRLogger;
import net.skinsrestorer.shared.plugin.SRServerAdapter;
import net.skinsrestorer.shared.utils.RunOnce;
import net.skinsrestorer.shared.utils.SRHelpers;

import javax.inject.Inject;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class SRServerMessageAdapter {
    private static final RunOnce UPDATE_V2_PROXY_WARNING = new RunOnce();
    private final SRLogger logger;
    private final SRServerAdapter serverAdapter;
    private final SharedSkinApplier<Object> skinApplier;

    public void handlePluginMessage(SRServerMessageEvent event) {
        if (!event.getChannel().equals(SRHelpers.MESSAGE_CHANNEL)) {
            return;
        }

        serverAdapter.runAsync(() -> {
            SRServerPluginMessage message = SRServerPluginMessage.CODEC.read(new SRInputReader(event.getData()));
            SRServerPluginMessage.ChannelPayload<?> channelPayload = message.channelPayload();
            SRHelpers.mustSupply(() -> switch (channelPayload) {
                case SRServerPluginMessage.GUIPageChannelPayload(SRInventory srInventory) ->
                        () -> serverAdapter.openGUI(event.getPlayer(), srInventory);
                case SRServerPluginMessage.SkinUpdateV2ChannelPayload(SkinProperty skinProperty) -> () -> {
                    UPDATE_V2_PROXY_WARNING.run(() ->
                            logger.warning("The proxy is running an outdated version of SkinsRestorer. Please update the proxy to the latest version. %s".formatted(SRHelpers.DOWNLOAD_URL)));
                    skinApplier.applySkin(event.getPlayer().getAs(Object.class), skinProperty);
                };
                case SRServerPluginMessage.SkinUpdateV3ChannelPayload(
                        SkinProperty skinProperty,
                        Optional<SRServerPluginMessage.SkinUpdateV3ChannelPayload.AckPayload> ackPayload
                ) -> () -> {
                    skinApplier.applySkin(event.getPlayer().getAs(Object.class), skinProperty);
                    ackPayload.ifPresent(value -> {
                        if (value.proxySrVersion().equalsIgnoreCase(BuildData.VERSION)) {
                            logger.debug("Proxy version %s matches server version %s.".formatted(value.proxySrVersion(), BuildData.VERSION));
                        } else {
                            logger.warning("The proxy is running a different version of SkinsRestorer (%s) than this server (%s). Make sure both proxy and server run the latest version of SkinsRestorer. %s"
                                    .formatted(value.proxySrVersion(), BuildData.VERSION, SRHelpers.DOWNLOAD_URL));
                        }

                        event.getPlayer().sendToMessageChannel(new SRProxyPluginMessage(
                                new SRProxyPluginMessage.AckChannelPayload(value.ackId(), BuildData.VERSION)));
                    });
                };
                case SRServerPluginMessage.GiveSkullChannelPayload payload ->
                        () -> serverAdapter.giveSkullItem(event.getPlayer(), payload);
                case SRServerPluginMessage.UnknownChannelPayload ignored ->
                        () -> logger.warning("Received unknown channel payload from proxy (Make sure the server and proxy are running the same version of SkinsRestorer) %s".formatted(SRHelpers.DOWNLOAD_URL));
            });
        });
    }
}
