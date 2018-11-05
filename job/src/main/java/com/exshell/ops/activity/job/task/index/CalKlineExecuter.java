package com.exshell.ops.activity.job.task.index;

import com.exshell.bitex.general.helper.ProIndexHelper;
import com.exshell.bitex.general.model.IndexPointVO;
import com.exshell.bitex.general.service.ProIndexService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计算统计处理类
 */
public class CalKlineExecuter {

    private static final Logger LOG = LoggerFactory.getLogger(CalKlineExecuter.class);

    private ProIndexService proIndexService;
    private ProIndexHelper proIndexHelper;

    private List<CalKlineHandler> handlers = new ArrayList<>();

    public void execute(IndexPointVO indexPointVO) {
        try {
            for (CalKlineHandler handler : handlers) {
                handler.execute(indexPointVO);
            }
        } catch (Exception e) {
            LOG.error("执行计算统计数据时发生异常", e);
        }
    }

    public void add(CalKlineHandler handler) {
        handler.setProIndexHelper(proIndexHelper);
        handler.setProIndexService(proIndexService);
        handler.initFromCache();
        handlers.add(handler);
    }

    public void reset() {
        for (CalKlineHandler handler : handlers) {
            handler.reset();
        }
    }

    public void setProIndexService(ProIndexService proIndexService) {
        this.proIndexService = proIndexService;
    }

    public void setProIndexHelper(ProIndexHelper proIndexHelper) {
        this.proIndexHelper = proIndexHelper;
    }
}
