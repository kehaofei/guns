package com.cetccloud.ciem.modular.flowable.warpper;

import com.cetccloud.ciem.utils.state.ExpenseState;
import cn.stylefeng.roses.core.base.warpper.BaseControllerWrapper;
import cn.stylefeng.roses.kernel.model.page.PageResult;
import com.baomidou.mybatisplus.plugins.Page;

import java.util.List;
import java.util.Map;

/**
 * 报销列表的包装
 *
 * @author fengshuonan
 * @date 2017年12月4日21:56:06
 */
public class ExpenseWarpper extends BaseControllerWrapper {

    public ExpenseWarpper(Map<String, Object> single) {
        super(single);
    }

    public ExpenseWarpper(List<Map<String, Object>> multi) {
        super(multi);
    }

    public ExpenseWarpper(Page<Map<String, Object>> page) {
        super(page);
    }

    public ExpenseWarpper(PageResult<Map<String, Object>> pageResult) {
        super(pageResult);
    }

    @Override
    protected void wrapTheMap(Map<String, Object> map) {
        Integer state = (Integer) map.get("state");
        map.put("stateName", ExpenseState.valueOf(state));
    }
}
