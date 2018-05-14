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

import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;
import zipkin2.reporter.okhttp3.RequestBodyMessageEncoder;

import java.util.List;

public class NewRelicSpanSender extends Sender {
    private static final String DEFAULT_ENDPOINT = "https://collector.newrelic.com/agent_listener/invoke_raw_method?method=external_span_data&license_key=%s&protocol_version=%d";
    private static final int MAX_MESSAGE_BYTES = 1048576;
    private static final Encoding ENCODING = Encoding.JSON;
    private final OkHttpSender okHttpSender;

    public NewRelicSpanSender(final String licenseKey, final int protocol) {
        final String endpoint = String.format(DEFAULT_ENDPOINT, licenseKey, protocol);
        this.okHttpSender = OkHttpSender.newBuilder()
                .encoding(ENCODING)
                .encoder(RequestBodyMessageEncoder.NEW_RELIC_JSON)
                .endpoint(endpoint)
                .build();
    }

    @Override
    public Encoding encoding() {
        return ENCODING;
    }

    @Override
    public int messageMaxBytes() {
        return MAX_MESSAGE_BYTES;
    }

    @Override
    public int messageSizeInBytes(final List<byte[]> encodedSpans) {
        return ENCODING.listSizeInBytes(encodedSpans);
    }

    @Override
    public int messageSizeInBytes(final int encodedSizeInBytes) {
        return super.messageSizeInBytes(encodedSizeInBytes);
    }

    @Override
    public Call<Void> sendSpans(final List<byte[]> encodedSpans) {
        return okHttpSender.sendSpans(encodedSpans);
    }
}
