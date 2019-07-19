package com.cetccloud.ciem.modular.flowable.service.impl;

import cn.stylefeng.guns.core.common.constant.DatasourceEnum;
import cn.stylefeng.guns.core.shiro.ShiroKit;
import cn.stylefeng.roses.core.mutidatasource.annotion.DataSource;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cetccloud.ciem.modular.flowable.mapper.ExpenseMapper;
import com.cetccloud.ciem.modular.flowable.model.Expense;
import com.cetccloud.ciem.modular.flowable.model.TaskVo;
import com.cetccloud.ciem.modular.flowable.service.IExpenseService;
import com.cetccloud.ciem.utils.HttpKit;
import com.cetccloud.ciem.utils.state.ExpenseState;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * <p>
 * 报销表 服务实现类
 * </p>
 *
 * @author stylefeng
 * @since 2017-12-04
 */
@Service
public class ExpenseServiceImpl extends ServiceImpl<ExpenseMapper, Expense> implements IExpenseService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessEngine processEngine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Expense expense) {
        //保存业务数据
        expense.setUserid(ShiroKit.getUser().getId());
        expense.setState(ExpenseState.SUBMITING.getCode());
        expense.setCreatetime(new Date());

        //启动流程
        HashMap<String, Object> map = new HashMap<>();
        map.put("taskUser", ShiroKit.getUser().getName());
        map.put("money", expense.getMoney());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Element", map);
        expense.setProcessId(processInstance.getId());
        this.insert(expense);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer expenseId) {
        Expense expense = this.selectById(expenseId);
        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().processInstanceId(expense.getProcessId()).list();
        for (ProcessInstance processInstance : list) {
            if (processInstance.getId().equals(expense.getProcessId())) {
                runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "取消报销");
            }
        }
        this.deleteById(expenseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pass(String taskId) {
        //使用任务ID，查询任务对象，获取流程流程实例ID
        Task task = taskService.createTaskQuery()//
                .taskId(taskId)
                .singleResult();

        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "通过");
        taskService.complete(taskId, map);

        //判断流程是否结束,结束之后修改状态
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                .processInstanceId(task.getProcessInstanceId())//使用流程实例ID查询
                .singleResult();
        Wrapper<Expense> wrapper = new EntityWrapper<Expense>().eq("processId", task.getProcessInstanceId());
        Expense expense = this.selectOne(wrapper);

        //审核通过修改为通过状态
        if (pi == null) {
            expense.setState(ExpenseState.PASS.getCode());
            expense.updateById();
        } else {
            //审核通过如果还有记录则为经理或boss审核中
            expense.setState(ExpenseState.CHECKING.getCode());
            expense.updateById();
        }
    }

    @Override
//    @DataSource(name = DatasourceEnum.DATA_SOURCE_BIZ)
    @Transactional(rollbackFor = Exception.class)
    public void unPass(String taskId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "驳回");
        taskService.complete(taskId, map);
    }

    @Override
    public List<TaskVo> getProcessList() {
        String name = ShiroKit.getUser().getName();
        List<Task> list = taskService.createTaskQuery()//获取流程列表
                .taskAssignee(name)
                .orderByTaskCreateTime().desc()
                .list();
        ArrayList<TaskVo> taskVos = new ArrayList<>();
        for (Task task : list) {
            Object money = runtimeService.getVariable(task.getExecutionId(), "money");
            String taskUser = (String) taskService.getVariable(task.getId(), "taskUser");
            boolean flag = false;
            if (name.equals(taskUser)) {
                flag = false;
            } else {
                flag = true;
            }
            taskVos.add(new TaskVo(task.getId(), task.getName(), task.getCreateTime(), taskUser, money, flag));
        }
        return taskVos;
    }

    @Override
    public void printProcessImage(Integer expenseId) throws IOException {
        Expense expense = this.selectById(expenseId);
        String processId = expense.getProcessId();
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }

        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();

        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
//        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0);
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, Collections.emptyList(),engconf.getActivityFontName(),engconf.getLabelFontName(),engconf.getAnnotationFontName(),null,1.0, false);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = HttpKit.getResponse().getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}

