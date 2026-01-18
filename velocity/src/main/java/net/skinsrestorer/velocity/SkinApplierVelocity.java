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
package net.skinsrestorer.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.GameProfile.Property;
import lombok.RequiredArgsConstructor;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.shared.api.SkinApplierAccess;
import net.skinsrestorer.shared.api.event.EventBusImpl;
import net.skinsrestorer.shared.api.event.SkinApplyEventImpl;
import net.skinsrestorer.shared.codec.SRServerPluginMessage;
import net.skinsrestorer.shared.subjects.SRProxyPlayer;
import net.skinsrestorer.shared.utils.ProxyAckTracker;
import net.skinsrestorer.velocity.wrapper.WrapperVelocity;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SkinApplierVelocity implements SkinApplierAccess<Player> {
    private final WrapperVelocity wrapper;
    private final EventBusImpl eventBus;
    private final ProxyAckTracker proxyAckTracker;

    @Override
    public void applySkin(Player player, SkinProperty property) {
        SkinApplyEventImpl applyEvent = new SkinApplyEventImpl(player, property);

        eventBus.callEvent(applyEvent);
        if (applyEvent.isCancelled()) {
            return;
        }

        SkinProperty appliedProperty = applyEvent.getProperty();

        player.setGameProfileProperties(updatePropertiesSkin(player.getGameProfileProperties(), appliedProperty));
        SRProxyPlayer srPlayer = wrapper.player(player);
        srPlayer.sendToMessageChannel(new SRServerPluginMessage(new SRServerPluginMessage.SkinUpdateV3ChannelPayload(
                property,
                proxyAckTracker.shouldAckPayload(srPlayer)
        )));
    }

    public GameProfile updateProfileSkin(GameProfile profile, SkinProperty property) {
        return new GameProfile(profile.getId(), profile.getName(), updatePropertiesSkin(profile.getProperties(), property));
    }

    private List<Property> updatePropertiesSkin(List<Property> original, SkinProperty property) {
        List<Property> properties = new ArrayList<>(original);

        properties.removeIf(property1 -> property1.getName().equals(SkinProperty.TEXTURES_NAME));
        properties.add(new Property(SkinProperty.TEXTURES_NAME, property.getValue(), property.getSignature()));

        return properties;
    }

    public Optional<SkinProperty> getSkinProperty(Player player) {
        return player.getGameProfileProperties().stream()
                .map(property -> SkinProperty.tryParse(
                        property.getName(),
                        property.getValue(),
                        property.getSignature()
                ))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
