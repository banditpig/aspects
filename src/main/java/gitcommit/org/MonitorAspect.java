package gitcommit.org;

import com.google.common.collect.Sets;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
Aspect to monitor method execution times.
See {@link MethodStatistics} and {@link Monitor}

 */
@Aspect
public class MonitorAspect {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorAspect.class);
  private static ConcurrentHashMap<String, MethodStatistics> statsMap = new ConcurrentHashMap<>();
  //two threads or half of avaliable cores
  private static ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
  private static Set timerNames = Sets.newConcurrentHashSet();

  private StatsSaver statsSaver = new StatsSaver() {
    @Override
    public void saveStats(MethodStatistics methodStatistics) {
      System.out.println(methodStatistics);
    }
  };

  @Around("execution(* *(..)) && @annotation(monitor)")
  public Object around(ProceedingJoinPoint point, Monitor monitor) throws Throwable {
    final long startTimeNano = System.nanoTime();

    try {
      return point.proceed();
    }
    finally {

      final long endTimeNano = System.nanoTime();
      final long execTimeNano = endTimeNano - startTimeNano;
      MethodSignature signature = (MethodSignature) point.getSignature();
      String name = signature.getDeclaringTypeName()+ "." + signature.getName();

      MethodStatistics methodStatistics = statsMap.get(name);
      if(methodStatistics == null){
        methodStatistics = new MethodStatistics();
        statsMap.put(name, methodStatistics);
      }
      methodStatistics.updateValues(execTimeNano);

      //do we have a timer?
      if( ! timerNames.contains(name)){
        timerNames.add(name);
        StatsWriter writer = new StatsWriter(name);
        scheduler.scheduleWithFixedDelay(writer, monitor.writePeriod(), monitor.writePeriod(), TimeUnit.SECONDS);
      }
    }
  }
  private class StatsWriter implements Runnable{

    private final String methodName;

    public StatsWriter(String methodName) {

      this.methodName = methodName;
    }

    @Override
    public void run() {

      MethodStatistics methodStatistics = statsMap.remove(methodName);
      if(methodStatistics != null) {
        methodStatistics.fixValues(methodName);
        statsSaver.saveStats(methodStatistics);
      }
    }
  }
}
