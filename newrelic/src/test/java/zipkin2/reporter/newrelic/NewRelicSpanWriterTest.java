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

import org.junit.Before;
import org.junit.Test;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.internal.Buffer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

public class NewRelicSpanWriterTest {
    private final String traceId = "e9ee51b0b6664988bcfbb6664988bcfb";
    private final String guid = "b45b6816a4974432";
    private final String entityName = "my service";
    private final String parentId = "0b6664988bcfb29d";
    private final String name = "some span";
    private final Long timestampMs = System.currentTimeMillis();
    private final Double durationMs = ThreadLocalRandom.current().nextDouble(0.0, 100.0);
    private final long timestampUs = timestampMs * 1000;
    private final long durationUs = Math.round(durationMs * 1000L);
    final Map<String, String> tags = new HashMap<>();
    private final NewRelicSpanWriter writer = new NewRelicSpanWriter();

    @Before
    public void before() {
        tags.put("tag", "value");
    }

    @Test
    public void simple() throws Exception {
        final String testJson = createNRSpanJson(parentId, tags);
        final Span testSpan = createZipkinSpan(parentId, tags);

        writeAndValidateSpan(testSpan, testJson);
    }

    @Test
    public void noParent() throws Exception {
        final String noParentJson = createNRSpanJson(null, tags);
        final Span testSpan = createZipkinSpan(null, tags);

        writeAndValidateSpan(testSpan, noParentJson);
    }

    @Test
    public void noTags() throws Exception {
        final String noTagsJson = createNRSpanJson(parentId, new HashMap<>());
        final Span testSpan = createZipkinSpan(parentId, new HashMap<>());

        writeAndValidateSpan(testSpan, noTagsJson);
    }

    @Test
    public void multipleTags() throws Exception {
        final Map<String, String> multipleTags = new HashMap<>(tags);
        multipleTags.put("custId", "12345");
        final String multipleTagsJson = createNRSpanJson(parentId, multipleTags);
        final Span testSpan = createZipkinSpan(parentId, multipleTags);

        writeAndValidateSpan(testSpan, multipleTagsJson);
    }

    private void writeAndValidateSpan(final Span span, final String json) {
        final int sizeInBytes = writer.sizeInBytes(span);
        final Buffer dest = new Buffer(sizeInBytes);
        writer.write(span, dest);
        final String newRelicJson = new String(dest.toByteArray());
        assertThat(sizeInBytes).isEqualTo(json.length());
        assertThat(newRelicJson).isEqualTo(json);
    }

    private String createNRSpanJson(final String parentId, final Map<String, String> tags) {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("{\"traceId\":\"%s\",\"guid\":\"%s\",\"entityName\":\"%s\",", traceId, guid, entityName));
        if (parentId != null) {
            builder.append(String.format("\"parentId\":\"%s\",", parentId));
        }
        builder.append(String.format("\"name\":\"%s\",\"timestamp\":%d,\"durationMs\":%.3f,", name, timestampMs, durationMs));
        if (tags != null && !tags.isEmpty()) {
            builder.append("\"tags\":{");
            tags.entrySet().forEach(entry -> {
                builder.append(String.format("\"%s\":\"%s\",", entry.getKey(), entry.getValue()));
            });
            builder.replace(builder.length() - 1, builder.length(), "},");
        }
        builder.replace(builder.length() - 1, builder.length(), "}");
        return builder.toString();
    }

    private Span createZipkinSpan(final String parentId, final Map<String, String> tags) {
        final Span.Builder builder = Span.newBuilder();
        builder.traceId(traceId);
        builder.id(guid);
        builder.localEndpoint(Endpoint.newBuilder().serviceName(entityName).build());
        if (parentId != null) {
            builder.parentId(parentId);
        }
        builder.name(name);
        builder.timestamp(timestampUs);
        builder.duration(durationUs);
        for (final Map.Entry<String, String> entry : tags.entrySet()) {
            builder.putTag(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }
}
