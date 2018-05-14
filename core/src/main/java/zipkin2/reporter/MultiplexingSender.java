package zipkin2.reporter;

import zipkin2.Call;
import zipkin2.Callback;
import zipkin2.codec.Encoding;

import java.io.IOException;
import java.util.List;

public class MultiplexingSender extends Sender {
    final List<Sender> senders;
    final int messageMaxBytes;
    final Encoding encoding;

    public MultiplexingSender(final List<Sender> senders) {
        final long encodings = senders.stream().map(sender -> sender.encoding()).count();
        if (encodings > 1) {
            throw new IllegalStateException("Multiplexing sender must use a single encoding type.");
        }
        this.encoding = senders.get(0).encoding();
        this.messageMaxBytes = senders.stream().mapToInt(sender -> sender.messageMaxBytes()).min().orElse(0);
        this.senders = senders;
    }

    @Override
    public Encoding encoding() {
        return encoding;
    }

    @Override
    public int messageMaxBytes() {
        return messageMaxBytes;
    }

    @Override
    public int messageSizeInBytes(final List<byte[]> encodedSpans) {
        return encoding.listSizeInBytes(encodedSpans);
    }

    @Override
    public Call<Void> sendSpans(final List<byte[]> encodedSpans) {
        return new MultiplexingCall(encodedSpans);
    }

    public class MultiplexingCall extends Call.Base<Void> {
        final List<byte[]> encodedSpans;

        public MultiplexingCall(final List<byte[]> encodedSpans) {
            this.encodedSpans = encodedSpans;
        }

        @Override
        protected Void doExecute() throws IOException {
            for (Sender sender : senders) {
                sender.sendSpans(encodedSpans).execute();
            }
            return null;
        }

        @Override
        protected void doEnqueue(final Callback<Void> callback) {

        }

        @Override
        public Call<Void> clone() {
            return null;
        }
    }
}
