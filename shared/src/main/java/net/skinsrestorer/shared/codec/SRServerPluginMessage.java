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
package net.skinsrestorer.shared.codec;

import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.shared.gui.SRInventory;
import net.skinsrestorer.shared.subjects.messages.ComponentString;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record SRServerPluginMessage(ChannelPayload<?> channelPayload) {
    public static final NetworkCodec<SRServerPluginMessage> CODEC = NetworkCodec.of(
            (out, msg) -> {
                ChannelType.CODEC.write(out, msg.channelPayload().getType());
                msg.channelPayload().writeCodec(out);
            },
            in -> new SRServerPluginMessage(ChannelType.CODEC.read(in).codec().read(in))
    );

    public record ChannelType<T extends ChannelPayload<T>>(String channelName,
                                                           NetworkCodec<T> codec) implements NetworkId {
        private static final Map<String, ChannelType<?>> ID_TO_VALUE = new HashMap<>();

        public static final ChannelType<GUIPageChannelPayload> OPEN_GUI = register(new ChannelType<>("openGUI", GUIPageChannelPayload.CODEC));
        public static final ChannelType<SkinUpdateV2ChannelPayload> SKIN_UPDATE_V2 = register(new ChannelType<>("SkinUpdateV2", SkinUpdateV2ChannelPayload.CODEC));
        public static final ChannelType<SkinUpdateV3ChannelPayload> SKIN_UPDATE_V3 = register(new ChannelType<>("skinUpdateV3", SkinUpdateV3ChannelPayload.CODEC));
        public static final ChannelType<GiveSkullChannelPayload> GIVE_SKULL = register(new ChannelType<>("giveSkull", GiveSkullChannelPayload.CODEC));
        public static final ChannelType<UnknownChannelPayload> UNKNOWN = register(new ChannelType<>("unknown", UnknownChannelPayload.CODEC));

        public static final NetworkCodec<ChannelType<?>> CODEC = NetworkCodec.ofNetworkIdDynamic(ID_TO_VALUE, UNKNOWN);

        private static <T extends ChannelPayload<T>> ChannelType<T> register(ChannelType<T> channelType) {
            ID_TO_VALUE.put(channelType.getId(), channelType);
            return channelType;
        }

        @Override
        public String getId() {
            return channelName;
        }
    }

    public sealed interface ChannelPayload<T extends ChannelPayload<T>> {
        ChannelType<T> getType();

        T cast();

        default void writeCodec(SROutputWriter out) {
            getType().codec().write(out, cast());
        }
    }

    public record GUIPageChannelPayload(SRInventory srInventory) implements ChannelPayload<GUIPageChannelPayload> {
        public static final NetworkCodec<GUIPageChannelPayload> CODEC = NetworkCodec.list(
                SRInventory.CODEC,
                GUIPageChannelPayload::srInventory,
                GUIPageChannelPayload::new
        );

        @Override
        public ChannelType<GUIPageChannelPayload> getType() {
            return ChannelType.OPEN_GUI;
        }

        @Override
        public GUIPageChannelPayload cast() {
            return this;
        }
    }

    public record SkinUpdateV2ChannelPayload(
            SkinProperty skinProperty) implements ChannelPayload<SkinUpdateV2ChannelPayload> {
        public static final NetworkCodec<SkinUpdateV2ChannelPayload> CODEC = NetworkCodec.list(
                BuiltInCodecs.SKIN_PROPERTY_CODEC,
                SkinUpdateV2ChannelPayload::skinProperty,
                SkinUpdateV2ChannelPayload::new
        );

        @Override
        public ChannelType<SkinUpdateV2ChannelPayload> getType() {
            return ChannelType.SKIN_UPDATE_V2;
        }

        @Override
        public SkinUpdateV2ChannelPayload cast() {
            return this;
        }
    }

    public record SkinUpdateV3ChannelPayload(
            SkinProperty skinProperty, Optional<AckPayload> ackPayload) implements ChannelPayload<SkinUpdateV3ChannelPayload> {
        public static final NetworkCodec<SkinUpdateV3ChannelPayload> CODEC = NetworkCodec.list(
                BuiltInCodecs.SKIN_PROPERTY_CODEC,
                SkinUpdateV3ChannelPayload::skinProperty,
                AckPayload.CODEC.optional(),
                SkinUpdateV3ChannelPayload::ackPayload,
                SkinUpdateV3ChannelPayload::new
        );

        @Override
        public ChannelType<SkinUpdateV3ChannelPayload> getType() {
            return ChannelType.SKIN_UPDATE_V3;
        }

        @Override
        public SkinUpdateV3ChannelPayload cast() {
            return this;
        }

        public record AckPayload(UUID ackId, String proxySrVersion) {
            public static final NetworkCodec<AckPayload> CODEC = NetworkCodec.list(
                    BuiltInCodecs.UUID_CODEC,
                    AckPayload::ackId,
                    BuiltInCodecs.STRING_CODEC,
                    AckPayload::proxySrVersion,
                    AckPayload::new
            );
        }
    }

    public record GiveSkullChannelPayload(
            ComponentString displayName,
            String textureHash) implements ChannelPayload<GiveSkullChannelPayload> {
        public static final NetworkCodec<GiveSkullChannelPayload> CODEC = NetworkCodec.list(
                ComponentString.CODEC,
                GiveSkullChannelPayload::displayName,
                BuiltInCodecs.STRING_CODEC,
                GiveSkullChannelPayload::textureHash,
                GiveSkullChannelPayload::new
        );

        @Override
        public ChannelType<GiveSkullChannelPayload> getType() {
            return ChannelType.GIVE_SKULL;
        }

        @Override
        public GiveSkullChannelPayload cast() {
            return this;
        }
    }

    public record UnknownChannelPayload() implements ChannelPayload<UnknownChannelPayload> {
        public static final NetworkCodec<UnknownChannelPayload> CODEC = NetworkCodec.unit(new UnknownChannelPayload());

        @Override
        public ChannelType<UnknownChannelPayload> getType() {
            return ChannelType.UNKNOWN;
        }

        @Override
        public UnknownChannelPayload cast() {
            return this;
        }
    }
}
