package com.exshell.ops.activity.job.task.index;

import static com.exshell.bitex.commons.Log.format;
import static com.exshell.bitex.commons.Log.kv;

import com.exshell.bitex.general.enums.IndexKlineType;
import com.exshell.bitex.general.enums.IndexName;
import com.exshell.bitex.general.helper.ProIndexHelper;
import com.exshell.bitex.general.model.IndexKlineVO;
import com.exshell.bitex.general.model.IndexPointVO;
import com.exshell.bitex.general.service.ProIndexService;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统计数据计算处理类
 */
public class CalKlineRealtimeHandler implements CalKlineHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CalKlineRealtimeHandler.class);

    private ProIndexService proIndexService;
    private ProIndexHelper proIndexHelper;

    // 结束时间，单位秒
    private long endTime;
    private IndexKlineVO data;
    private IndexKlineType type;
    private IndexName indexName;

    public CalKlineRealtimeHandler(IndexKlineType type, IndexName indexName) {
        this.type = type;
        this.indexName = indexName;
    }

    /**
     * 初始化
     */
    public void initFromCache() {
//        data = proIndexHelper.getKlineData(indexName, type);
        long startTime = type.getStartTime(Instant.now().getEpochSecond());
        endTime = type.getEndTime(startTime);
//        if (data == null) {
        data = proIndexService.getKlineByTime(indexName.getName(), startTime, endTime).getData();
        if (data != null) {
            proIndexHelper.saveKlineData(indexName, type, data);
        }
//        }
    }

    /**
     * 统计处理
     * -不存在data，则表示刚刚运行，创建一个kline数据
     * -获取到的数据时间小于开始时间，说明数据是迟到的，基本不会出现这种情况，抛弃掉这种数据
     * -获取到的数据时间大于或等于结束时间，说明已经进入下一个kline，当前kline落库
     * -其他情况，更新kline数据，更新实时kline数据缓存
     */
    public void execute(IndexPointVO indexVO) {
        // 处理数据不存在情况
        if (data == null) {
            data = createNewData(indexVO);
            proIndexHelper.saveKlineData(indexName, type, data);
            return;
        }

        // 处理新到指数数据超前情况
        if (indexVO.dataTime < data.startTime) {
            LOG.warn(format("指数计算统计数据时发现数据颠倒情况", kv("indexTime", indexVO.dataTime),
                    kv("dataTime", data.startTime), kv("index", indexVO.value)));
            return;
        }

        // 处理更换统计信息情况，即数据时间超过结束时间
        if (indexVO.dataTime >= endTime) {
            LOG.info(format("统计数据-新建", kv("time", data.startTime), kv("type", type.name()), kv("open", data.openValue),
                    kv("close", data.closeValue), kv("min", data.minValue), kv("max", data.maxValue)));
            data = createNewData(indexVO);
            proIndexHelper.saveKlineData(indexName, type, data);
            return;
        }

        // 处理正常数据更新
        boolean update = false;
        if (indexVO.value.compareTo(data.maxValue) > 0) {
            data.maxValue = indexVO.value;
            update = true;
        }

        if (indexVO.value.compareTo(data.minValue) < 0) {
            data.minValue = indexVO.value;
            update = true;
        }

        if (!update && indexVO.value.compareTo(data.closeValue) != 0) {
            data.closeValue = indexVO.value;
            update = true;
        }

        // 统计数据有变化，需要处理缓存
        if (update) {
            proIndexHelper.saveKlineData(indexName, type, data);
        }
    }


    private IndexKlineVO createNewData(IndexPointVO indexVO) {
        long startTime = type.getStartTime(indexVO.dataTime);
        endTime = type.getEndTime(startTime);
        IndexKlineVO data = new IndexKlineVO();
        data.startTime = startTime;
        data.openValue = indexVO.value;
        data.closeValue = indexVO.value;
        data.minValue = indexVO.value;
        data.maxValue = indexVO.value;
        return data;
    }


    public void setProIndexService(ProIndexService proIndexService) {
        this.proIndexService = proIndexService;
    }

    public void setProIndexHelper(ProIndexHelper proIndexHelper) {
        this.proIndexHelper = proIndexHelper;
    }

    @Override
    public void reset() {
        data = null;
    }

}
