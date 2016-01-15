package net.digitalid.service.core.identity.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.validation.meta.TargetType;

/**
 * This annotation indicates that a {@link SemanticType semantic type} denotes a {@link SemanticType#isRoleType() role type}.
 */
@Documented
@TargetType(SemanticType.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface RoleType {}
