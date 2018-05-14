/**
 * Copyright 2016-2018 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin2.reporter.newrelic;

import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.Encoding;
import zipkin2.internal.JsonCodec;

import java.util.List;

public class NewRelicBytesEncoder implements BytesEncoder<Span> {
    final NewRelicSpanWriter writer = new NewRelicSpanWriter();

    @Override
    public Encoding encoding() {
        return Encoding.JSON;
    }

    @Override
    public int sizeInBytes(final Span span) {
        return writer.sizeInBytes(span);
    }

    @Override
    public byte[] encode(final Span span) {
        return JsonCodec.write(this.writer, span);
    }

    @Override
    public byte[] encodeList(final List<Span> list) {
        return JsonCodec.writeList(this.writer, list);
    }
}
