package com.exshell.ops.activity.job.task;

import com.exshell.bitex.general.enums.IndexKlineType;
import java.util.Date;

public class KlineTypeTest {

    public static void main(String[] args) {
        out("minTs", IndexKlineType.minTs());
        out("maxTs", IndexKlineType.maxTs());


        out("now 1d ", IndexKlineType.DAY1.getStartTime(now()));

        out("now 1d ", IndexKlineType.DAY1.getStartTime(1527095400));

        out("now 1d ", IndexKlineType.DAY1.getStartTime(1527174600));

        out("now 1d ", IndexKlineType.DAY1.getStartTime(now()));

        out("now 1d ", IndexKlineType.DAY1.getStartTime(now()));

    }

    private static long now() {
        return (long) (System.currentTimeMillis() / 1000);
    }

    private static void out(String name, long ts) {
        System.out.println(name + " : " + new Date(ts * 1000L));
    }
}
