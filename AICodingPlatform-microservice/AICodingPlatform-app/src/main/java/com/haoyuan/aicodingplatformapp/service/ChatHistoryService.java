package com.haoyuan.aicodingplatformapp.service;

import com.haoyuan.aicodingplatform.model.dto.chathistory.ChatHistoryQueryRequest;
import com.haoyuan.aicodingplatform.model.entity.ChatHistory;
import com.haoyuan.aicodingplatform.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author Haoyuan
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 加载应用下的对话历史到内存中
     *
     * @param appId 应用ID
     * @param chatMemory 对话历史内存对象
     * @param maxCount 最大加载数量
     * @return 加载的对话历史数量
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 添加对话消息。
     *
     * @param appId 应用ID
     * @param message 消息内容
     * @param messageType 消息类型
     * @param userId 创建用户ID
     * @return 是否添加成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 删除应用下的所有对话历史,同步删除数据库中的数据
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);


    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);
}
