package cn.stylefeng.guns.controller;

import cn.stylefeng.guns.core.common.constant.DatasourceEnum;
import cn.stylefeng.roses.core.mutidatasource.annotion.DataSource;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.security.DefaultPrivileges;
import org.flowable.ui.common.security.SecurityUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * AppController
 * @author jinlei
 * @date 2019/3/13 15:34
 */
@RestController
@RequestMapping("/ciem")
public class DemoController {

    @RequestMapping(value = "/rest/account", method = RequestMethod.GET, produces = "application/json")
    public UserRepresentation getAccount() {

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("admin");
        userRepresentation.setEmail("admin@admin.com");
        userRepresentation.setFullName("Administrator");
        userRepresentation.setLastName("admin");
        userRepresentation.setFirstName("admin");

//        SecurityUtils.assumeUser(SecurityUtils.getCurrentUserObject());

        List<String> privileges = new ArrayList<>();
        privileges.add(DefaultPrivileges.ACCESS_MODELER);
        privileges.add(DefaultPrivileges.ACCESS_IDM);
        privileges.add(DefaultPrivileges.ACCESS_ADMIN);
        privileges.add(DefaultPrivileges.ACCESS_TASK);
        privileges.add(DefaultPrivileges.ACCESS_REST_API);
        userRepresentation.setPrivileges(privileges);
        return userRepresentation;
    }
}
