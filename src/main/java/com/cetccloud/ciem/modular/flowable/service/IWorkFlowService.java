package com.cetccloud.ciem.modular.flowable.service;

import com.baomidou.mybatisplus.service.IService;
import com.cetccloud.ciem.modular.flowable.model.Expense;
import com.cetccloud.ciem.modular.flowable.model.TaskVo;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 报销表 服务类
 * </p>
 *
 * @author stylefeng
 * @since 2017-12-04
 */
public interface IWorkFlowService{

    /**
     * 新增一个报销单
     */
    void add(Expense expense);

    /**
     * 删除一个报销单
     */
    void delete(String processId);

    /**
     * 通过审批
     */
    void pass(String taskId);

    /**
     * 通过审批
     */
    void unPass(String taskId);

    /**
     * 获取审批列表
     */
    List<TaskVo> getProcessList();

    /**
     * 绘画当前流程图
     */
    void printProcessImage(String processId) throws IOException;

}
