package io.bhex.broker.admin.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ProjectName: broker-db-grpc
 * @Package: io.bhex.broker.model
 * @Author: ming.xu
 * @CreateDate: 19/08/2018 18:20 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_admin_user")
public class AdminUser {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;//序号

    private Long brokerId;//券商id

    private String brokerName;

    private String email;

    private String areaCode;//手机区号

    private String telephone; //手机号码

    private String username;//用户名

    private String password;//密码

    private Integer status;//状态

    private Long createdAt;//用户注册时间

    private String createdIp;//用户注册IP

    private Integer deleted;//逻辑删除: 1=删除 0=正常
}
