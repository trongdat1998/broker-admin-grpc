package io.bhex.broker.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @ProjectName: broker-common
 * @Package: io.bhex.broker.common.model
 * @Author: ming.xu
 * @CreateDate: 19/08/2018 7:53 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_init_password_token")
public class InitPasswordToken {

    @Id
    private Long adminUserId;

    private String token;

    private Timestamp createdAt;

    private Timestamp expireAt;

    private Integer validateResult;
}
