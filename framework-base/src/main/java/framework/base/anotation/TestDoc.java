package framework.base.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生成报告中的自定义的详细信息
 * @author James Guo
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface TestDoc {
	String[] testCaseID() default {};
	String[] storyJiraID() default {};
	String[] bugJiraID() default {};
	String testObjective() default "";
	String dateDocumented() default "";
}
