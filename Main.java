import java.util.*;
import java.time.*;
import java.math.*;
public class Main {
 static void check(boolean condition){if(!condition)throw new AssertionError();}

 static class Job {String owner;Instant expiry=Instant.MIN;int attempts;boolean done,dead;}
 static class Scheduler {
  final Map<String,Job> jobs=new HashMap<>();final int max;
  Scheduler(int max){if(max<1)throw new IllegalArgumentException();this.max=max;}
  synchronized void add(String id){if(jobs.putIfAbsent(id,new Job())!=null)throw new IllegalArgumentException("duplicate");}
  synchronized boolean claim(String id,String worker,Instant now,Duration lease){Job j=jobs.get(id);if(j==null||j.done||j.dead)return false;if(worker.isBlank()||lease.isNegative()||lease.isZero())throw new IllegalArgumentException();if(j.expiry.isAfter(now))return false;if(j.attempts>=max){j.dead=true;return false;}j.owner=worker;j.expiry=now.plus(lease);j.attempts++;return true;}
  synchronized void finish(String id,String worker,Instant now,boolean success){Job j=jobs.get(id);if(j==null||!worker.equals(j.owner)||!j.expiry.isAfter(now))throw new IllegalStateException("stale lease");j.done=success;j.dead=!success&&j.attempts>=max;j.owner=null;j.expiry=Instant.MIN;}
 }
 public static void main(String[] args){Scheduler s=new Scheduler(2);s.add("x");Instant now=Instant.parse("2026-01-01T00:00:00Z");check(s.claim("x","a",now,Duration.ofSeconds(5)));check(!s.claim("x","b",now,Duration.ofSeconds(5)));check(s.claim("x","b",now.plusSeconds(6),Duration.ofSeconds(5)));try{s.finish("x","a",now.plusSeconds(7),true);throw new AssertionError();}catch(IllegalStateException expected){}s.finish("x","b",now.plusSeconds(7),false);check(s.jobs.get("x").dead);System.out.println("PASS: exclusive lease, expiry, stale owner and dead letter");}
}
