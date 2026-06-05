package dataAnalysis.processing;

import dataAnalysis.model.DataProfile;
import dataAnalysis.model.ProcessingStrategy;

/**
 * 数据处理接口
 * 三层处理架构：概况 → 采样 → 聚合
 */
public interface DataProcessor {

    DataProfile getProfile();

    ProcessingStrategy selectStrategy();

    String prepareLLMContext(int maxTokens);
}
