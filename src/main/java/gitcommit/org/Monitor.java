package gitcommit.org;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to measure the execution times of methods. Apply the annotation to the method and supply
 * and supply a value for the writePerios and the execution results - held in MethodStatistics will appear
 * in mongo every writePeriod seconds.
 * See {@link MethodStatistics}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitor{

  public int writePeriod();   //in seconds
}
