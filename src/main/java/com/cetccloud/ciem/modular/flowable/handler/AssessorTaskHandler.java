package com.cetccloud.ciem.modular.flowable.handler;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @Author KE HAO FEI
 * @Description //TODO 审批流程结点
 * @Date 10:42 2019/7/5
 * @Param
 * @return
 **/
public class AssessorTaskHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setAssignee("审核员");
    }

}
