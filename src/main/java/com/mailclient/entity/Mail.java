package com.mailclient.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.activation.DataSource;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class Mail {
    private Integer number;
    private String fromName;
    private String fromEmail;
    private String subject;
    private String content;
    private Date date;
    boolean seen;
    private List<DataSource> attachments;
}
