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

import org.junit.Test;
import zipkin2.Span;

import java.util.ArrayList;
import java.util.List;

import static zipkin2.reporter.TestObjects.BACKEND;
import static zipkin2.reporter.TestObjects.FRONTEND;

public class NewRelicSpanSenderTest {
    private final String LICENSE_KEY = System.getenv("NEW_RELIC_LICENSE_KEY");
    private final NewRelicSpanSender newRelicSpanSender = new NewRelicSpanSender(LICENSE_KEY, 1);

    @Test
    public void sendSpan() throws Exception {
        final NewRelicBytesEncoder encoder = new NewRelicBytesEncoder();
        final List<byte[]> spans = new ArrayList();
        final Span span = Span.newBuilder()
                .traceId("7180c278b62e8f6a216a2aea45d08fc9")
                .parentId("6b221d5bc9e6496c")
                .id("5b4185666d50f68b")
                .name("get")
                .kind(Span.Kind.CLIENT)
                .localEndpoint(FRONTEND)
                .remoteEndpoint(BACKEND)
                .timestamp(System.currentTimeMillis() * 1000)
                .duration(207000L)
                .addAnnotation(1472470996238000L, "foo")
                .putTag("http.path", "/api")
                .putTag("clnt/finagle.version", "6.45.0")
                .build();
        spans.add(encoder.encode(span));
        newRelicSpanSender.sendSpans(spans).execute();
    }
}
