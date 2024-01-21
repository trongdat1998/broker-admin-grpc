package io.bhex.broker.admin.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_mq_offset")
public class MqOffset {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    @Column(name="broker_name")
    private String brokerName;

    @Column(name="topic")
    private String topic;

    @Column(name="queue_id")
    private Integer queueId;

    @Column(name="offset")
    private Long offset;

    @Column(name="created_at")
    private Long createdAt;

    public long nextOffset(){
        return offset+1;
    }
}
