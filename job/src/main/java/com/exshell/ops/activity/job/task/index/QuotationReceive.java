package com.exshell.ops.activity.job.task.index;

import com.exshell.bitex.general.model.IndexSymbolDataVO;
import java.util.List;

/**
 * 接收行情数据
 */

public interface QuotationReceive {

    /**
     * 收到请求数据
     */
    void quotationReceive(String symbol, List<IndexSymbolDataVO> list);

    /**
     * 收到订阅数据
     */
    void subDataReceive(String symbol, IndexSymbolDataVO indexSymbolDataVO);

    /**
     * 收到pong数据
     */
    void pongReceive();
}
