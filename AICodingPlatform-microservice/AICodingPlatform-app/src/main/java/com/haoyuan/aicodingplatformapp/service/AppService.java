package com.haoyuan.aicodingplatformapp.service;

import com.haoyuan.aicodingplatform.model.dto.app.AppAddRequest;
import com.haoyuan.aicodingplatform.model.dto.app.AppQueryRequest;
import com.haoyuan.aicodingplatform.model.entity.App;
import com.haoyuan.aicodingplatform.model.entity.User;
import com.haoyuan.aicodingplatform.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;

/**
 * 应用 服务层。
 *
 * @author Haoyuan
 */
public interface AppService extends IService<App> {

    Long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 获取应用封装类
     *
     * @param app 应用
     * @return 应用封装类
     */

     AppVO getAppVO(App app);

     /**
      * 获取应用查询条件
      *
      * @param appQueryRequest 应用查询请求
      * @return 应用封装类列表
      */

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用封装类列表
     *
     * @param appList 应用列表
     * @return 应用封装类列表
     */

    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 聊天生成代码
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);


    /**
     * 部署应用
     * @param appId 应用 id
     * @param loginUser 登录用户
     * @return 应用部署路径
     */
    String deployApp(Long appId, User loginUser);


    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 删除应用
     *
     * @param id 应用ID
     * @return 是否删除成功
     */
    boolean removeById(Serializable id);
}
