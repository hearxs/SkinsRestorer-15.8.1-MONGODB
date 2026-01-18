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
package net.skinsrestorer.api.connections;

import net.skinsrestorer.api.Base64Utils;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.SkinVariant;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Generate Value and Signature for a skin image url using the <a href="https://mineskin.org/">MineSkin</a> API
 */
public interface MineSkinAPI {
    /**
     * Generates a skin using the <a href="https://mineskin.org/">MineSkin</a> API
     * [WARNING] MineSkin api key might be REQUIRED in the future.
     *
     * @param url         pointing to a skin image url
     *                    can be a direct link to a PNG file on a website or a data URI (data:image/png;base64,...)
     *                    use {@link net.skinsrestorer.api.Base64Utils#encodePNGAsUrl(byte[])} to convert a PNG file to a data URI
     * @param skinVariant can be null, steve or slim
     * @return Custom skin property containing "value" and "signature"
     * @throws DataRequestException on error
     * @throws MineSkinException    when there was a MineSkin specific error
     */
    MineSkinResponse genSkin(String url, @Nullable SkinVariant skinVariant) throws DataRequestException, MineSkinException;

    /**
     * Shorthand for {@link #genSkin(String, SkinVariant)} with a call to
     * {@link Base64Utils#encodePNGAsUrl(File)} to convert a PNG file to a data URI.
     *
     * @param file        pointing to a PNG file
     * @param skinVariant can be null, steve or slim
     * @return Custom skin property containing "value" and "signature"
     * @throws DataRequestException on error
     * @throws MineSkinException    when there was a MineSkin specific error
     * @throws IOException          if the file cannot be read
     * @see #genSkin(String, SkinVariant)
     */
    default MineSkinResponse genSkin(File file, @Nullable SkinVariant skinVariant) throws DataRequestException, MineSkinException, IOException {
        return genSkin(Base64Utils.encodePNGAsUrl(file), skinVariant);
    }

    /**
     * Shorthand for {@link #genSkin(String, SkinVariant)} with a call to
     * {@link Base64Utils#encodePNGAsUrl(File)} to convert a PNG file to a data URI.
     *
     * @param file        pointing to a PNG file
     * @param skinVariant can be null, steve or slim
     * @return Custom skin property containing "value" and "signature"
     * @throws DataRequestException on error
     * @throws MineSkinException    when there was a MineSkin specific error
     * @throws IOException          if the file cannot be read
     * @see #genSkin(String, SkinVariant)
     */
    default MineSkinResponse genSkin(Path file, @Nullable SkinVariant skinVariant) throws DataRequestException, MineSkinException, IOException {
        return genSkin(Base64Utils.encodePNGAsUrl(file), skinVariant);
    }

    /**
     * Shorthand for {@link #genSkin(String, SkinVariant)} with a call to
     * {@link Base64Utils#encodePNGAsUrl(BufferedImage)} to convert a BufferedImage to a data URI.
     *
     * @param image       BufferedImage of a PNG image
     * @param skinVariant can be null, steve or slim
     * @return Custom skin property containing "value" and "signature"
     * @throws DataRequestException on error
     * @throws MineSkinException    when there was a MineSkin specific error
     * @throws IOException          if the image cannot be converted to PNG
     * @see #genSkin(String, SkinVariant)
     */
    default MineSkinResponse genSkin(BufferedImage image, @Nullable SkinVariant skinVariant) throws DataRequestException, MineSkinException, IOException {
        return genSkin(Base64Utils.encodePNGAsUrl(image), skinVariant);
    }

    /**
     * Shorthand for {@link #genSkin(String, SkinVariant)} with a call to
     * {@link Base64Utils#encodePNGAsUrl(byte[])} to convert a PNG byte array to a data URI.
     *
     * @param pngData     byte array of a PNG image
     * @param skinVariant can be null, steve or slim
     * @return Custom skin property containing "value" and "signature"
     * @throws DataRequestException on error
     * @throws MineSkinException    when there was a MineSkin specific error
     * @see #genSkin(String, SkinVariant)
     */
    default MineSkinResponse genSkin(byte[] pngData, @Nullable SkinVariant skinVariant) throws DataRequestException, MineSkinException {
        return genSkin(Base64Utils.encodePNGAsUrl(pngData), skinVariant);
    }
}
