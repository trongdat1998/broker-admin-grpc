package io.bhex.broker.admin.mapper;

import io.bhex.broker.admin.model.TransferRecord;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.mapper
 * @Author: ming.xu
 * @CreateDate: 10/11/2018 7:23 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface TransferRecordMapper extends Mapper<TransferRecord> {

    String TABLE_NAME = " tb_asset_snapshot_record ";

    String COLUMNS = "id, broker_id, account_id, group_id, snapshot_time, token_id, asset_amount, created_at";

}
