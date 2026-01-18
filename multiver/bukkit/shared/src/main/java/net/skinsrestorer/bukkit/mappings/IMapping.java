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
package net.skinsrestorer.bukkit.mappings;

import net.skinsrestorer.viaversion.ExceptionSupplier;
import net.skinsrestorer.viaversion.ViaPacketData;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.function.Predicate;

public interface IMapping {
    void accept(Player player, Predicate<ExceptionSupplier<ViaPacketData>> viaFunction);

    void resendInfoPackets(Player toResend, Player toSendTo);

    /**
     * Format as in ServerBuildInfo#minecraftVersionId.
     *
     * @return The supported paper minecraft version ids versions of the mapping
     */
    Set<String> getPaperMinecraftVersionIds();

    /**
     * Can be found at <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/util/CraftMagicNumbers.java">SpigotMC</a>
     *
     * @return The supported spigot mapping versions of the mapping
     */
    Set<String> getSpigotMappingVersions();

    @SuppressWarnings("deprecation")
    static ViaPacketData newViaPacketData(Player player, long seed, int gamemodeId, boolean isFlat) {
        return new ViaPacketData(player.getUniqueId(), player.getWorld().getEnvironment().getId(), seed, gamemodeId, isFlat);
    }
}
