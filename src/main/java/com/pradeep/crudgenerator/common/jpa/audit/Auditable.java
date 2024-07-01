package com.pradeep.crudgenerator.common.jpa.audit;

import java.io.Serializable;

public interface Auditable extends Serializable {

    AuditSection getAuditSection();

    void setAuditSection(AuditSection audit);
}
