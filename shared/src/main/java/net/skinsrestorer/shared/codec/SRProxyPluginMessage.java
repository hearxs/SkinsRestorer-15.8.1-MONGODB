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

import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.shared.gui.PageType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SRProxyPluginMessage(ChannelPayload<?> channelPayload) {
    public static final NetworkCodec<SRProxyPluginMessage> CODEC = NetworkCodec.of(
            (out, msg) -> {
                ChannelType.CODEC.write(out, msg.channelPayload().getType());
                msg.channelPayload().writeCodec(out);
            },
            in -> new SRProxyPluginMessage(ChannelType.CODEC.read(in).codec().read(in))
    );

    public record ChannelType<T extends ChannelPayload<T>>(String channelName,
                                                           NetworkCodec<T> codec) implements NetworkId {
        private static final Map<String, ChannelType<?>> ID_TO_VALUE = new HashMap<>();

        public static final ChannelType<GUIActionListChannelPayload> GUI_ACTION_LIST = register(new ChannelType<>("guiActionList", GUIActionListChannelPayload.CODEC));
        public static final ChannelType<AckChannelPayload> ACK = register(new ChannelType<>("ack", AckChannelPayload.CODEC));
        public static final ChannelType<UnknownChannelPayload> UNKNOWN_CHANNEL = register(new ChannelType<>("unknownChannel", UnknownChannelPayload.CODEC));

        public static final NetworkCodec<ChannelType<?>> CODEC = NetworkCodec.ofNetworkIdDynamic(ID_TO_VALUE, UNKNOWN_CHANNEL);

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

    public record GUIActionListChannelPayload(List<GUIActionChannelPayload> actions) implements ChannelPayload<GUIActionListChannelPayload> {
        public static final NetworkCodec<GUIActionListChannelPayload> CODEC = NetworkCodec.list(
                GUIActionChannelPayload.CODEC.list(),
                GUIActionListChannelPayload::actions,
                GUIActionListChannelPayload::new
        );

        @Override
        public ChannelType<GUIActionListChannelPayload> getType() {
            return ChannelType.GUI_ACTION_LIST;
        }

        @Override
        public GUIActionListChannelPayload cast() {
            return this;
        }
    }

    public record GUIActionChannelPayload(GUIActionPayload<?> payload) {
        public static final NetworkCodec<GUIActionChannelPayload> CODEC = NetworkCodec.of(
                (out, msg) -> {
                    GUIActionType.CODEC.write(out, msg.payload().getType());
                    msg.payload().writeCodec(out);
                },
                in -> new GUIActionChannelPayload(GUIActionType.CODEC.read(in).codec().read(in))
        );

        public record GUIActionType<T extends GUIActionPayload<T>>(String channelName,
                                                                   NetworkCodec<T> codec) implements NetworkId {
            private static final Map<String, GUIActionType<?>> ID_TO_VALUE = new HashMap<>();

            public static final GUIActionType<OpenPagePayload> OPEN_PAGE = register(new GUIActionType<>("openPage", OpenPagePayload.CODEC));
            public static final GUIActionType<ClearSkinPayload> CLEAR_SKIN = register(new GUIActionType<>("clearSkin", ClearSkinPayload.CODEC));
            public static final GUIActionType<SetSkinPayload> SET_SKIN = register(new GUIActionType<>("setSkin", SetSkinPayload.CODEC));
            public static final GUIActionType<AddFavouritePayload> ADD_FAVOURITE = register(new GUIActionType<>("addFavourite", AddFavouritePayload.CODEC));
            public static final GUIActionType<RemoveFavouritePayload> REMOVE_FAVOURITE = register(new GUIActionType<>("removeFavourite", RemoveFavouritePayload.CODEC));
            public static final GUIActionType<UnknownActionPayload> UNKNOWN_ACTION = register(new GUIActionType<>("unknownAction", UnknownActionPayload.CODEC));

            public static final NetworkCodec<GUIActionType<?>> CODEC = NetworkCodec.ofNetworkIdDynamic(ID_TO_VALUE, UNKNOWN_ACTION);

            private static <T extends GUIActionPayload<T>> GUIActionType<T> register(GUIActionType<T> guiActionType) {
                ID_TO_VALUE.put(guiActionType.getId(), guiActionType);
                return guiActionType;
            }

            @Override
            public String getId() {
                return channelName;
            }
        }

        public sealed interface GUIActionPayload<T extends GUIActionPayload<T>> {
            GUIActionType<T> getType();

            T cast();

            default void writeCodec(SROutputWriter out) {
                getType().codec().write(out, cast());
            }
        }

        public record OpenPagePayload(int page, PageType type) implements GUIActionPayload<OpenPagePayload> {
            public static final NetworkCodec<OpenPagePayload> CODEC = NetworkCodec.list(
                    BuiltInCodecs.INT_CODEC,
                    OpenPagePayload::page,
                    PageType.CODEC,
                    OpenPagePayload::type,
                    OpenPagePayload::new
            );

            @Override
            public GUIActionType<OpenPagePayload> getType() {
                return GUIActionType.OPEN_PAGE;
            }

            @Override
            public OpenPagePayload cast() {
                return this;
            }
        }

        public record ClearSkinPayload() implements GUIActionPayload<ClearSkinPayload> {
            public static final ClearSkinPayload INSTANCE = new ClearSkinPayload();
            public static final NetworkCodec<ClearSkinPayload> CODEC = NetworkCodec.unit(INSTANCE);

            @Override
            public GUIActionType<ClearSkinPayload> getType() {
                return GUIActionType.CLEAR_SKIN;
            }

            @Override
            public ClearSkinPayload cast() {
                return this;
            }
        }

        public record SetSkinPayload(SkinIdentifier skinIdentifier) implements GUIActionPayload<SetSkinPayload> {
            public static final NetworkCodec<SetSkinPayload> CODEC = NetworkCodec.list(
                    BuiltInCodecs.SKIN_IDENTIFIER_CODEC,
                    SetSkinPayload::skinIdentifier,
                    SetSkinPayload::new
            );

            @Override
            public GUIActionType<SetSkinPayload> getType() {
                return GUIActionType.SET_SKIN;
            }

            @Override
            public SetSkinPayload cast() {
                return this;
            }
        }

        public record AddFavouritePayload(
                SkinIdentifier skinIdentifier) implements GUIActionPayload<AddFavouritePayload> {
            public static final NetworkCodec<AddFavouritePayload> CODEC = NetworkCodec.list(
                    BuiltInCodecs.SKIN_IDENTIFIER_CODEC,
                    AddFavouritePayload::skinIdentifier,
                    AddFavouritePayload::new
            );

            @Override
            public GUIActionType<AddFavouritePayload> getType() {
                return GUIActionType.ADD_FAVOURITE;
            }

            @Override
            public AddFavouritePayload cast() {
                return this;
            }
        }

        public record RemoveFavouritePayload(
                SkinIdentifier skinIdentifier) implements GUIActionPayload<RemoveFavouritePayload> {
            public static final NetworkCodec<RemoveFavouritePayload> CODEC = NetworkCodec.list(
                    BuiltInCodecs.SKIN_IDENTIFIER_CODEC,
                    RemoveFavouritePayload::skinIdentifier,
                    RemoveFavouritePayload::new
            );

            @Override
            public GUIActionType<RemoveFavouritePayload> getType() {
                return GUIActionType.REMOVE_FAVOURITE;
            }

            @Override
            public RemoveFavouritePayload cast() {
                return this;
            }
        }

        public record UnknownActionPayload() implements GUIActionPayload<UnknownActionPayload> {
            public static final NetworkCodec<UnknownActionPayload> CODEC = NetworkCodec.unit(new UnknownActionPayload());

            @Override
            public GUIActionType<UnknownActionPayload> getType() {
                return GUIActionType.UNKNOWN_ACTION;
            }

            @Override
            public UnknownActionPayload cast() {
                return this;
            }
        }
    }

    public record AckChannelPayload(UUID ackId, String serverSrVersion) implements ChannelPayload<AckChannelPayload> {
        public static final NetworkCodec<AckChannelPayload> CODEC = NetworkCodec.list(
                BuiltInCodecs.UUID_CODEC,
                AckChannelPayload::ackId,
                BuiltInCodecs.STRING_CODEC,
                AckChannelPayload::serverSrVersion,
                AckChannelPayload::new
        );

        @Override
        public ChannelType<AckChannelPayload> getType() {
            return ChannelType.ACK;
        }

        @Override
        public AckChannelPayload cast() {
            return this;
        }
    }

    public record UnknownChannelPayload() implements ChannelPayload<UnknownChannelPayload> {
        public static final NetworkCodec<UnknownChannelPayload> CODEC = NetworkCodec.unit(new UnknownChannelPayload());

        @Override
        public ChannelType<UnknownChannelPayload> getType() {
            return ChannelType.UNKNOWN_CHANNEL;
        }

        @Override
        public UnknownChannelPayload cast() {
            return this;
        }
    }
}
