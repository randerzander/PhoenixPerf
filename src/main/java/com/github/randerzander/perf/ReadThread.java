package com.github.randerzander.perf;

import java.sql.*;

import java.util.Properties;
import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ReadThread implements Runnable {
  public int threadId;
  public HashMap<String, String> props;

  Connection connection;
  int count;
  int repetitions;
  int commitInterval;

  boolean recordPerf;
  String testName;
  String metric;
  
  String[] queries;
  String query;

  public ReadThread(int threadId, HashMap<String, String> props){
    this.threadId = threadId;
    this.props = props;
    repetitions = Integer.parseInt(props.get("repetitions"));
    count = Integer.parseInt(props.get("count"));

    recordPerf = props.get("recordPerf").equalsIgnoreCase("true");
    testName = props.get("testName");

    try {
      queries = new Scanner(new File(props.get("readQueryFile"))).useDelimiter("\\Z").next().split(";");
      query = queries[threadId % queries.length];
      metric = query;
    } catch (java.io.FileNotFoundException e) { e.printStackTrace(); System.exit(-1); }

    try{
      connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
    } catch (SQLException e) { e.printStackTrace(); }
  }

  public void run(){
    System.out.println("READ THREAD " + threadId + " starting " + repetitions + " rounds of " + count + " queries = " + repetitions * count + " total queries");

    long start = System.nanoTime();
    for (int i = 0; i < repetitions; i++){
      long t1 = System.nanoTime();
      for (int j = 0; j < count; j++){
        try{
          connection.createStatement().execute(query);
        } catch (SQLException e) { e.printStackTrace(); System.exit(-1); }
      }
      perf(threadId, i, t1);
    }

    double seconds = (double)(System.nanoTime() - start) / 1000000000.0;
    System.out.println("READ DONE: " + threadId + ": " + repetitions * count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(repetitions*count/seconds)) + " qps");
  }

  private void write(String sql, boolean commit){
    try{
      connection.createStatement().execute(sql);
      if (commit) connection.commit();
    } catch (SQLException e) { e.printStackTrace(); }
  }

  private void perf(int threadId, int rep, long t1){
    long timeInMillis = System.currentTimeMillis();
    long timeInNanos = System.nanoTime();

    Timestamp ts = new Timestamp(timeInMillis);
    ts.setNanos((int) (timeInNanos % 1000000000));
    double seconds = (timeInNanos - t1) / 1000000000.0;
    if (recordPerf) write("upsert into perf values ('"+testName+"','"+metric+"',"+ threadId + "," + rep + ",'" + ts + "'," + String.valueOf(count/seconds)+")", true);
    System.out.println("READ THREAD " + threadId + " rep " + rep + " of " + repetitions + ": " + count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(count/seconds)) + " queries/sec");
  }
}
