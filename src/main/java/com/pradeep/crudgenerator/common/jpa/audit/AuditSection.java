package com.pradeep.crudgenerator.common.jpa.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.OffsetDateTime;


@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditSection implements Serializable {

    private static final long serialVersionUID = 1L;

    @CreatedDate
    @Column(name = "CREATED_AT", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT", columnDefinition = "TIMESTAMP")
    private OffsetDateTime updatedAt;

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;

}
