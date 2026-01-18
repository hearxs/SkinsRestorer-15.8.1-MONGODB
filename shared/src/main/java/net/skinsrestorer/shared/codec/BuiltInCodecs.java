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
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.property.SkinType;
import net.skinsrestorer.api.property.SkinVariant;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class BuiltInCodecs {
    public static final NetworkCodec<String> STRING_CODEC = NetworkCodec.of(
            SROutputWriter::writeString,
            SRInputReader::readString
    );
    public static final NetworkCodec<Integer> INT_CODEC = NetworkCodec.of(
            SROutputWriter::writeInt,
            SRInputReader::readInt
    );
    public static final NetworkCodec<Boolean> BOOLEAN_CODEC = NetworkCodec.of(
            SROutputWriter::writeBoolean,
            SRInputReader::readBoolean
    );
    public static final NetworkCodec<UUID> UUID_CODEC = NetworkCodec.of(
            (out, uuid) -> {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            },
            in -> new UUID(in.readLong(), in.readLong())
    );
    public static final NetworkCodec<SkinProperty> SKIN_PROPERTY_CODEC = NetworkCodec.list(
            STRING_CODEC,
            SkinProperty::getValue,
            STRING_CODEC,
            SkinProperty::getSignature,
            SkinProperty::of
    );
    public static final NetworkCodec<SkinVariant> SKIN_VARIANT_CODEC = NetworkCodec.ofEnumDynamic(SkinVariant.class, t -> t.name().toLowerCase(Locale.ROOT), SkinVariant.CLASSIC);
    public static final NetworkCodec<SkinType> SKIN_TYPE_CODEC = NetworkCodec.ofEnumDynamic(SkinType.class, t -> t.name().toLowerCase(Locale.ROOT), SkinType.CUSTOM);
    public static final NetworkCodec<SkinIdentifier> SKIN_IDENTIFIER_CODEC = NetworkCodec.list(
            STRING_CODEC,
            SkinIdentifier::getIdentifier,
            SKIN_VARIANT_CODEC.optional(),
            skinIdentifier -> Optional.ofNullable(skinIdentifier.getSkinVariant()),
            SKIN_TYPE_CODEC,
            SkinIdentifier::getSkinType,
            (identifier, variant, type) -> SkinIdentifier.of(identifier, variant.orElse(null), type)
    );
}
