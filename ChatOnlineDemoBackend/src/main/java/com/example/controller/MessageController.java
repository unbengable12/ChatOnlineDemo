package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.MessageSendVo;
import com.example.service.AccountService;
import com.example.service.MessageService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Resource
    MessageService messageService;
    @Resource
    AccountService accountService;
    @PostMapping("/send")
    public RestBean<String> sendMessage(@RequestBody MessageSendVo vo) {
        messageService.sendMessage(vo);
        return RestBean.success();
    }
    // 获取在线用户列表
    @GetMapping("/getOnlineUser/{userId}")
    public RestBean<String> getOnlineUser(@PathVariable("userId") @NotNull Integer userId) {
        return RestBean.success(this.accountService.getOnlineUser(userId));
    }
    // 获取双方的历史消息
    @GetMapping("/getHistoryMessage/{i1}/{i2}")
    public RestBean<String> getHistoryMessage(@PathVariable("i1") @NotNull Integer i1,
                                             @PathVariable("i2") @NotNull Integer i2) {
        return RestBean.success(this.messageService.getHistoryMessage(i1, i2));
    }
    @GetMapping("/updateStatus/{self}/{other}")
    public RestBean<String> updateStatus(@PathVariable("self") @NotNull Integer self,
                                         @PathVariable("other") @NotNull Integer other) {
        this.messageService.updateStatus(self, other);
        return RestBean.success();
    }
}
