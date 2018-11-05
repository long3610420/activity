package com.exshell.ops.activity.job.task.index;

import com.exshell.bitex.general.helper.ProIndexHelper;
import com.exshell.bitex.general.model.IndexPointVO;
import com.exshell.bitex.general.service.ProIndexService;

/**
 * 计算统计数据
 */
public interface CalKlineHandler {

    /**
     * 初始化
     */
    void initFromCache();

    /**
     * 时间统计处理
     */
    void execute(IndexPointVO indexVO);

    void setProIndexService(ProIndexService proIndexService);

    void setProIndexHelper(ProIndexHelper proIndexHelper);

    void reset();
}
