// Copyright 2015-2018 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.benchmark;

import static io.nats.benchmark.Utils.humanBytes;

import io.nats.client.Connection;
import io.nats.client.Statistics;
import java.text.DecimalFormat;

public class Sample {
    int jobMsgCnt;
    long msgCnt;
    long msgBytes;
    long ioBytes;
    long start;
    long end;

    static final double BILLION = 1000000000.0;

    Sample() {
    }

    /**
     * Constructs a new benchmark sample.
     *
     * @param jobCount the number of messages that were sent or received
     * @param msgSize  the size in bytes of each messages
     * @param start    the start time in nanoseconds
     * @param end      the end time in nanoseconds
     * @param nc       the NATs connection
     */
    public Sample(int jobCount, int msgSize, long start, long end, Connection nc) {
        this.jobMsgCnt = jobCount;
        this.start = start;
        this.end = end;
        this.msgBytes = (long) msgSize * jobCount;
        Statistics stats = nc.getStats();
        this.msgCnt = stats.getOutMsgs() + stats.getInMsgs();
        this.ioBytes = stats.getOutBytes() + stats.getInBytes();
    }

    /**
     * Duration that the sample was active (ns).
     *
     * @return Duration that the sample was active (ns)
     */
    public long duration() {
        return end - start;
    }

    /**
     * Throughput of bytes per second.
     *
     * @return throughput of bytes per second
     */
    public double throughput() {
        return (double) msgBytes / (duration() / BILLION);
    }

    /**
     * Rate of messages in the job per second.
     *
     * @return rate of messages in the job per second.
     */
    public long rate() {
        return (long) ((double) jobMsgCnt / (duration() / BILLION));
    }

    public double seconds() {
        return (double) duration() / BILLION;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String toString() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String rate = formatter.format(rate());
        String throughput = humanBytes(throughput(), false);
        return String.format("%s msgs/sec ~ %s/sec", rate, throughput);
    }

    final int getJobMsgCnt() {
        return jobMsgCnt;
    }

    final void setJobMsgCnt(int jobMsgCnt) {
        this.jobMsgCnt = jobMsgCnt;
    }

    final long getMsgCnt() {
        return msgCnt;
    }

    final void setMsgCnt(long msgCnt) {
        this.msgCnt = msgCnt;
    }

    final long getMsgBytes() {
        return msgBytes;
    }

    final void setMsgBytes(long msgBytes) {
        this.msgBytes = msgBytes;
    }

    final long getIoBytes() {
        return ioBytes;
    }

    final void setIoBytes(long ioBytes) {
        this.ioBytes = ioBytes;
    }

    final long getStart() {
        return start;
    }

    final void setStart(long start) {
        this.start = start;
    }

    final long getEnd() {
        return end;
    }

    final void setEnd(long end) {
        this.end = end;
    }
}
