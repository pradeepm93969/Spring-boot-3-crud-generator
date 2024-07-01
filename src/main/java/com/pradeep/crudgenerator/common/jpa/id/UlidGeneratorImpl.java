package com.pradeep.crudgenerator.common.jpa.id;

import com.github.f4b6a3.ulid.UlidCreator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import java.util.EnumSet;
import static org.hibernate.generator.EventTypeSets.INSERT_ONLY;

public class UlidGeneratorImpl implements BeforeExecutionGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner,
                           Object currentValue, EventType eventType) {
        return UlidCreator.getUlid().toString();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return INSERT_ONLY;
    }
}
