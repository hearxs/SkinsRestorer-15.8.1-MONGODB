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
package net.skinsrestorer.bukkit.utils;

import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class SkullUtil {
    public static void setSkull(ItemStack skullItem, Server server, String hash) {
        try {
            SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
            if (skullMeta == null) {
                throw new IllegalArgumentException("ItemStack does not have a SkullMeta");
            }

            PlayerProfile profile = server.createPlayerProfile(UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8)));
            profile.getTextures().setSkin(URI.create("https://textures.minecraft.net/texture/" + hash).toURL());
            skullMeta.setOwnerProfile(profile);

            skullItem.setItemMeta(skullMeta);
        } catch (Throwable t) {
            XSkull.of(skullItem)
                    .profile(Profileable.of(Objects.requireNonNull(ProfileInputType.typeOf(hash), "Unknown input"), hash))
                    .apply();
        }
    }
}
