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
import zipkin2.internal.Buffer;
import zipkin2.internal.JsonEscaper;

import java.util.Iterator;
import java.util.Map;

public class NewRelicSpanWriter implements Buffer.Writer<Span> {
    @Override
    public int sizeInBytes(final Span span) {
        int sizeInBytes = 1;
        // traceId
        sizeInBytes += 13 + span.traceId().length();
        // guid
        sizeInBytes += 10 + span.id().length();
        // entityName
        sizeInBytes += 16 + span.localServiceName().length();
        // parentId
        if (span.parentId() != null) {
            sizeInBytes += 14 + span.parentId().length();
        }
        // name
        sizeInBytes += 10 + span.name().length();
        // timestamp
        final long timestampMs = span.timestamp() / 1000;
        sizeInBytes += 13 + Buffer.asciiSizeInBytes(timestampMs);
        // durationMs
        final double durationMs = (span.duration() != null) ? span.duration() / 1000.0 : 0.0;
        sizeInBytes += 13 + String.format("%.3f", durationMs).length();
        if (!span.tags().isEmpty()) {
            // tags
            sizeInBytes += 10;
            final Iterator i = span.tags().entrySet().iterator();

            while (i.hasNext()) {
                final Map.Entry<String, String> entry = (Map.Entry) i.next();
                sizeInBytes += 5 + entry.getKey().length() + entry.getValue().length();
                if (i.hasNext()) {
                    sizeInBytes += 1;
                }
            }
        }
        sizeInBytes += 1;
        return sizeInBytes;
    }

    @Override
    public void write(final Span span, final Buffer buffer) {
        buffer.writeAscii("{\"traceId\":\"").writeAscii(span.traceId()).writeByte(34);
        buffer.writeAscii(",\"guid\":\"").writeAscii(span.id()).writeByte(34);
        buffer.writeAscii(",\"entityName\":\"").writeAscii(span.localServiceName()).writeByte(34);
        if (span.parentId() != null) {
            buffer.writeAscii(",\"parentId\":\"").writeAscii(span.parentId()).writeByte(34);
        }
        buffer.writeAscii(",\"name\":\"").writeAscii(span.name()).writeByte(34);
        final long timestampMs = span.timestampAsLong() / 1000;
        buffer.writeAscii(",\"timestamp\":").writeAscii(timestampMs);
        final Double durationMs = (span.duration() != null) ? span.duration() / 1000.0 : 0.0;
        buffer.writeAscii(",\"durationMs\":").writeAscii(String.format("%.3f", durationMs));
        if (!span.tags().isEmpty()) {
            buffer.writeAscii(",\"tags\":{");
            final Iterator i = span.tags().entrySet().iterator();

            while (i.hasNext()) {
                final Map.Entry<String, String> entry = (Map.Entry) i.next();
                buffer.writeByte(34).writeUtf8(JsonEscaper.jsonEscape(entry.getKey())).writeAscii("\":\"");
                buffer.writeUtf8(JsonEscaper.jsonEscape(entry.getValue())).writeByte(34);
                if (i.hasNext()) {
                    buffer.writeByte(44);
                }
            }
            buffer.writeByte(125);
        }
        buffer.writeByte(125);
    }
}
