package io.bhex.broker.admin.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.model
 * @Author: ming.xu
 * @CreateDate: 13/11/2018 10:43 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_transfer_group_info")
public class TransferGroupInfo {

    public final static Integer STATUS_INIT = 0; //初始
    public final static Integer STATUS_SUCCESS = 1; //成功
    public final static Integer STATUS_AIRDOP = 2; //发送中
    public final static Integer STATUS_FAILED = 3; //失败

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;
    private Long brokerId;
    private Long airdropId;
    private Integer status;
    private Long transferCount;
    private String tokenId;
    private String transferAssetAmount;
    private Long createdAt;
}
