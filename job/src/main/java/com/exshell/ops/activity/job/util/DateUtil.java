package com.exshell.ops.activity.job.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class);

    /**
     * 字符串转换成日期
     *
     * @return date
     */
    public static Date strToDate(String str) {
        if(StringUtils.isBlank(str)) {
            LOG.warn("字符串转换成日期参数错误,param:" + str);
            return null;
        }
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (str != null && !str.contains(" ")) {
                format = new SimpleDateFormat("yyyy-MM-dd");
            }
            date = format.parse(str);
        } catch (ParseException e) {
            LOG.error("字符串转日期错误" + e);
        }

        return date;
    }

    /**
     * 字符串转换成日期
     *
     * @return date
     */
    public static Date strToDate(String str, String patten) {
        if(StringUtils.isBlank(str)) {
            LOG.warn("字符串转换成日期参数错误,param:" + str);
            return null;
        }

        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat(patten);
            date = format.parse(str);
        } catch (ParseException e) {
            LOG.error("字符串转日期错误" + e);
        }
        return date;
    }
}
