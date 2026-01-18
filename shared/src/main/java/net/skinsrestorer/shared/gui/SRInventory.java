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
package net.skinsrestorer.shared.gui;

import lombok.Getter;
import net.skinsrestorer.shared.codec.BuiltInCodecs;
import net.skinsrestorer.shared.codec.NetworkCodec;
import net.skinsrestorer.shared.codec.NetworkId;
import net.skinsrestorer.shared.codec.SRProxyPluginMessage;
import net.skinsrestorer.shared.subjects.messages.ComponentString;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record SRInventory(int rows, ComponentString title, Map<Integer, Item> items) {
    public static final NetworkCodec<SRInventory> CODEC = NetworkCodec.list(
            BuiltInCodecs.INT_CODEC,
            SRInventory::rows,
            ComponentString.CODEC,
            SRInventory::title,
            BuiltInCodecs.INT_CODEC.mappedTo(Item.CODEC),
            SRInventory::items,
            SRInventory::new
    ).compressed();

    @Getter
    public enum MaterialType implements NetworkId {
        DIRT,
        SKULL,
        ARROW,
        BARRIER,
        BOOKSHELF,
        ENDER_EYE,
        ENCHANTING_TABLE;

        public static final NetworkCodec<MaterialType> CODEC = NetworkCodec.ofEnum(MaterialType.class, MaterialType.DIRT);

        @Override
        public String getId() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record Item(
            MaterialType materialType,
            ComponentString displayName,
            List<ComponentString> lore,
            Optional<String> textureHash,
            boolean enchantmentGlow,
            Map<ClickEventType, ClickEventAction> clickHandlers
    ) {
        public static final NetworkCodec<Item> CODEC = NetworkCodec.list(
                MaterialType.CODEC,
                Item::materialType,
                ComponentString.CODEC,
                Item::displayName,
                ComponentString.CODEC.list(),
                Item::lore,
                BuiltInCodecs.STRING_CODEC.optional(),
                Item::textureHash,
                BuiltInCodecs.BOOLEAN_CODEC,
                Item::enchantmentGlow,
                ClickEventType.CODEC.mappedTo(ClickEventAction.CODEC),
                Item::clickHandlers,
                Item::new
        );
    }

    public record ClickEventAction(List<SRProxyPluginMessage.GUIActionChannelPayload> actionChannelPayload,
                                   boolean closeInventory) {
        public static final NetworkCodec<ClickEventAction> CODEC = NetworkCodec.list(
                SRProxyPluginMessage.GUIActionChannelPayload.CODEC.list(),
                ClickEventAction::actionChannelPayload,
                BuiltInCodecs.BOOLEAN_CODEC,
                ClickEventAction::closeInventory,
                ClickEventAction::new
        );

        public ClickEventAction(SRProxyPluginMessage.GUIActionChannelPayload actionChannelPayload,
                                boolean closeInventory) {
            this(List.of(actionChannelPayload), closeInventory);
        }
    }
}
