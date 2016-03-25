package com.ai.cloud.skywalking.web.controller;

import com.ai.cloud.skywalking.web.common.BaseController;
import com.ai.cloud.skywalking.web.dao.inter.IUserMaintainDao;
import com.ai.cloud.skywalking.web.vo.LoginUserInfo;
import com.ai.cloud.skywalking.web.vo.SignInUserInfo;
import com.ai.cloud.skywalking.web.vo.UserInfo;
import com.ai.cloud.util.Constants;
import com.ai.cloud.util.common.StringUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by xin on 16-3-24.
 */
@RequestMapping("/usr")
@Controller
public class UserMaintainController extends BaseController {
    private Logger logger = LogManager.getLogger(UserMaintainController.class);

    @Autowired
    private IUserMaintainDao iUserMaintainDao;

    @RequestMapping("/login")
    public String loginPage() {
        return "usr/login";
    }

    @RequestMapping(value = "/doLogin", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String doLogin(LoginUserInfo loginInfo) {
        JSONObject result = new JSONObject();
        try {
            if (validateUserInfo(loginInfo, result)) {
                return result.toJSONString();
            }

            LoginUserInfo dbLoginInfo = iUserMaintainDao.queryUserInfoByName(loginInfo.getUserName());
            logger.info("{}", dbLoginInfo.getPassword());
            if (dbLoginInfo == null || !loginInfo.getPassword().equals(dbLoginInfo.getPassword())) {
                result.put("code", "400");
                result.put("message", "Username or password is not correct");
                return result.toJSONString();
            }

            result.put("code", "200");
            result.put("message", "Login success");
        } catch (Exception e) {
            logger.error("Failed to login the user[{}] and password[{}]", loginInfo.getUserName(),
                    loginInfo.getPassword(), e);
            result.put("code", "500");
            result.put("message", "fatal error. please try it again.");
        }
        return result.toJSONString();
    }

    private boolean validateUserInfo(UserInfo loginInfo, JSONObject result) {
        if (StringUtil.isBlank(loginInfo.getUserName()) || StringUtil.isBlank(loginInfo.getPassword())) {
            result.put("code", "400");
            result.put("message", "Username or password is null");
            return true;
        }
        return false;
    }

    @RequestMapping("/register")
    public String registerPage() {
        return "usr/register";
    }

    @RequestMapping(value = "/doRegister", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String doRegister(SignInUserInfo signInUserInfo) {
        JSONObject result = new JSONObject();
        try {

            if (validateUserInfo(signInUserInfo, result)) {
                return result.toJSONString();
            }
            signInUserInfo.setRoleType(Constants.ROLE_TYPE_USER);
            signInUserInfo.setSts(Constants.STR_VAL_A);
            iUserMaintainDao.addUser(signInUserInfo);
            if (StringUtil.isBlank(signInUserInfo.getUid())) {
                result.put("code", "500");
                result.put("message", "Failed to register user" + signInUserInfo.getUserName());
                return result.toJSONString();
            }

            result.put("code", "200");
            result.put("message", "register success");
        } catch (Exception e) {
            logger.error("Failed to register the user[{}]", signInUserInfo.getUserName(),
                    signInUserInfo.getPassword(), e);
            result.put("code", "500");
            result.put("message", "fatal error. please try it again.");
        }
        return result.toJSONString();
    }
}