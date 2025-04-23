package org.tkit.onecx.quarkus.parameter.deployment;

import org.jboss.jandex.ClassInfo;
import org.tkit.onecx.quarkus.parameter.Parameter;

public class EmptyAnnotationNameException extends RuntimeException {

    private static final String MESSAGE = "@" + Parameter.class.getName()
            + " annotations must have a non empty name attribute [class=%s]";

    private final ClassInfo classInfo;

    public EmptyAnnotationNameException(ClassInfo classInfo) {
        super(String.format(MESSAGE, classInfo.name()));
        this.classInfo = classInfo;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }
}
