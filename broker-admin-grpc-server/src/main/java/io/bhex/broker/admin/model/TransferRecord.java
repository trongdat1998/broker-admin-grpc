package io.bhex.broker.admin.model;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.model
 * @Author: ming.xu
 * @CreateDate: 10/11/2018 6:49 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_transfer_record")
public class TransferRecord {

    private Long id;
    private Long brokerId;
    private Long airdropId;
    private Long groupId;
    private Long snapshotTime;
    private Long accountId;
    private String tokenId;
    private BigDecimal tokenAmount;
    private Integer status;
    private Long createdAt;
}
