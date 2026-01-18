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

import net.skinsrestorer.shared.utils.SRFunction;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public record NetworkCodec<T>(Writer<T> writer, Reader<T> reader) {
    public static <T> NetworkCodec<T> of(Writer<T> writer, Reader<T> reader) {
        return new NetworkCodec<>(writer, reader);
    }

    public static <T, F1> NetworkCodec<T> list(
            NetworkCodec<F1> f1Codec,
            Function<T, F1> f1Getter,
            SRFunction.Function1<F1, T> constructor
    ) {
        return NetworkCodec.of(
                (out, t) -> f1Codec.write(out, f1Getter.apply(t)),
                in -> constructor.apply(f1Codec.read(in))
        );
    }

    public static <T, F1, F2> NetworkCodec<T> list(
            NetworkCodec<F1> f1Codec,
            Function<T, F1> f1Getter,
            NetworkCodec<F2> f2Codec,
            Function<T, F2> f2Getter,
            SRFunction.Function2<F1, F2, T> constructor
    ) {
        return NetworkCodec.of(
                (out, t) -> {
                    f1Codec.write(out, f1Getter.apply(t));
                    f2Codec.write(out, f2Getter.apply(t));
                },
                in -> constructor.apply(f1Codec.read(in), f2Codec.read(in))
        );
    }

    public static <T, F1, F2, F3> NetworkCodec<T> list(
            NetworkCodec<F1> f1Codec,
            Function<T, F1> f1Getter,
            NetworkCodec<F2> f2Codec,
            Function<T, F2> f2Getter,
            NetworkCodec<F3> f3Codec,
            Function<T, F3> f3Getter,
            SRFunction.Function3<F1, F2, F3, T> constructor
    ) {
        return NetworkCodec.of(
                (out, t) -> {
                    f1Codec.write(out, f1Getter.apply(t));
                    f2Codec.write(out, f2Getter.apply(t));
                    f3Codec.write(out, f3Getter.apply(t));
                },
                in -> constructor.apply(
                        f1Codec.read(in),
                        f2Codec.read(in),
                        f3Codec.read(in)
                )
        );
    }

    public static <T, F1, F2, F3, F4> NetworkCodec<T> list(
            NetworkCodec<F1> f1Codec,
            Function<T, F1> f1Getter,
            NetworkCodec<F2> f2Codec,
            Function<T, F2> f2Getter,
            NetworkCodec<F3> f3Codec,
            Function<T, F3> f3Getter,
            NetworkCodec<F4> f4Codec,
            Function<T, F4> f4Getter,
            SRFunction.Function4<F1, F2, F3, F4, T> constructor
    ) {
        return NetworkCodec.of(
                (out, t) -> {
                    f1Codec.write(out, f1Getter.apply(t));
                    f2Codec.write(out, f2Getter.apply(t));
                    f3Codec.write(out, f3Getter.apply(t));
                    f4Codec.write(out, f4Getter.apply(t));
                },
                in -> constructor.apply(
                        f1Codec.read(in),
                        f2Codec.read(in),
                        f3Codec.read(in),
                        f4Codec.read(in)
                )
        );
    }

    public static <T, F1, F2, F3, F4, F5> NetworkCodec<T> list(
            NetworkCodec<F1> f1Codec,
            Function<T, F1> f1Getter,
            NetworkCodec<F2> f2Codec,
            Function<T, F2> f2Getter,
            NetworkCodec<F3> f3Codec,
            Function<T, F3> f3Getter,
            NetworkCodec<F4> f4Codec,
            Function<T, F4> f4Getter,
            NetworkCodec<F5> f5Codec,
            Function<T, F5> f5Getter,
            SRFunction.Function5<F1, F2, F3, F4, F5, T> constructor
    ) {
        return NetworkCodec.of(
                (out, t) -> {
                    f1Codec.write(out, f1Getter.apply(t));
                    f2Codec.write(out, f2Getter.apply(t));
                    f3Codec.write(out, f3Getter.apply(t));
                    f4Codec.write(out, f4Getter.apply(t));
                    f5Codec.write(out, f5Getter.apply(t));
                },
                in -> constructor.apply(
                        f1Codec.read(in),
                        f2Codec.read(in),
                        f3Codec.read(in),
                        f4Codec.read(in),
                        f5Codec.read(in)
                )
        );
    }

    public static <T, F1, F2, F3, F4, F5, F6> NetworkCodec<T> list(
            NetworkCodec<F1> f1Codec,
            Function<T, F1> f1Getter,
            NetworkCodec<F2> f2Codec,
            Function<T, F2> f2Getter,
            NetworkCodec<F3> f3Codec,
            Function<T, F3> f3Getter,
            NetworkCodec<F4> f4Codec,
            Function<T, F4> f4Getter,
            NetworkCodec<F5> f5Codec,
            Function<T, F5> f5Getter,
            NetworkCodec<F6> f6Codec,
            Function<T, F6> f6Getter,
            SRFunction.Function6<F1, F2, F3, F4, F5, F6, T> constructor
    ) {
        return NetworkCodec.of(
                (out, t) -> {
                    f1Codec.write(out, f1Getter.apply(t));
                    f2Codec.write(out, f2Getter.apply(t));
                    f3Codec.write(out, f3Getter.apply(t));
                    f4Codec.write(out, f4Getter.apply(t));
                    f5Codec.write(out, f5Getter.apply(t));
                    f6Codec.write(out, f6Getter.apply(t));
                },
                in -> constructor.apply(
                        f1Codec.read(in),
                        f2Codec.read(in),
                        f3Codec.read(in),
                        f4Codec.read(in),
                        f5Codec.read(in),
                        f6Codec.read(in)
                )
        );
    }

    public static <T> NetworkCodec<T> unit(T instance) {
        return new NetworkCodec<>((out, t) -> {
        }, in -> instance);
    }

    private static <T, D extends T> NetworkCodec<T> ofMapBackedDynamic(Map<String, T> idToValue, Function<T, String> dynamicMapper, D defaultValue) {
        return BuiltInCodecs.STRING_CODEC.map(dynamicMapper, id -> idToValue.getOrDefault(id, defaultValue));
    }

    public static <T extends NetworkId, D extends T> NetworkCodec<T> ofNetworkIdDynamic(Map<String, T> idToValue, D defaultValue) {
        return ofMapBackedDynamic(
                idToValue,
                NetworkId::getId,
                defaultValue
        );
    }

    public static <T extends Enum<T>> NetworkCodec<T> ofEnumDynamic(Class<T> clazz, Function<T, String> dynamicMapper, T defaultValue) {
        return ofMapBackedDynamic(
                Arrays.stream(clazz.getEnumConstants()).collect(Collectors.toMap(dynamicMapper, Function.identity())),
                dynamicMapper,
                defaultValue
        );
    }

    public static <T extends Enum<T> & NetworkId> NetworkCodec<T> ofEnum(Class<T> clazz, T defaultValue) {
        return ofEnumDynamic(clazz, NetworkId::getId, defaultValue);
    }

    public void write(SROutputWriter buf, T t) {
        writer.write(buf, t);
    }

    public T read(SRInputReader buf) {
        return reader.read(buf);
    }

    public <O> NetworkCodec<O> map(Function<O, T> to, Function<T, O> from) {
        return NetworkCodec.of(
                (stream, o) -> writer.write(stream, to.apply(o)),
                stream -> from.apply(reader.read(stream))
        );
    }

    public NetworkCodec<Optional<T>> optional() {
        return NetworkCodec.of(
                (os, optional) -> {
                    BuiltInCodecs.BOOLEAN_CODEC.write(os, optional.isPresent());
                    optional.ifPresent(t -> this.write(os, t));
                },
                is -> BuiltInCodecs.BOOLEAN_CODEC.read(is) ? Optional.of(this.read(is)) : Optional.empty()
        );
    }

    public NetworkCodec<List<T>> list() {
        return NetworkCodec.of(
                (os, list) -> {
                    BuiltInCodecs.INT_CODEC.write(os, list.size());
                    for (T entry : list) {
                        this.write(os, entry);
                    }
                },
                is -> {
                    int size = BuiltInCodecs.INT_CODEC.read(is);
                    List<T> list = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        list.add(this.read(is));
                    }

                    return list;
                }
        );
    }

    public <V> NetworkCodec<Map<T, V>> mappedTo(NetworkCodec<V> valueCodec) {
        return NetworkCodec.of(
                (os, map) -> {
                    BuiltInCodecs.INT_CODEC.write(os, map.size());
                    for (Map.Entry<T, V> entry : map.entrySet()) {
                        this.write(os, entry.getKey());
                        valueCodec.write(os, entry.getValue());
                    }
                },
                is -> {
                    int size = BuiltInCodecs.INT_CODEC.read(is);
                    Map<T, V> map = new LinkedHashMap<>(size);
                    for (int i = 0; i < size; i++) {
                        T key = this.read(is);
                        V value = valueCodec.read(is);
                        map.put(key, value);
                    }

                    return map;
                }
        );
    }

    public NetworkCodec<T> compressed() {
        return NetworkCodec.of(
                (stream, t) -> {
                    try (GZIPOutputStream gzip = new GZIPOutputStream(stream.wrapper());
                         DataOutputStream outputStream = new DataOutputStream(gzip)) {
                        writer.write(new SROutputWriter(outputStream), t);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                stream -> {
                    try (GZIPInputStream gzip = new GZIPInputStream(stream.wrapper());
                         DataInputStream inputStream = new DataInputStream(gzip)) {
                        return reader.read(new SRInputReader(inputStream));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public interface Writer<T> {
        void write(SROutputWriter buf, T t);
    }

    public interface Reader<T> {
        T read(SRInputReader buf);
    }
}
