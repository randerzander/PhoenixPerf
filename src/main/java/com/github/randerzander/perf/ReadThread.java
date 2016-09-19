package com.github.randerzander.perf;

import java.sql.*;

import java.util.Properties;
import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ReadThread implements Runnable {
  private int threadId;
  private HashMap<String, String> props;

  private Connection connection;
  private int count;
  private int repetitions;
  private int commitInterval;

  private boolean recordPerf;
  private String testName;
  private String metric;
  private PreparedStatement readStatement;
  private PreparedStatement perfStatement;

  public ReadThread(int threadId, HashMap<String, String> props){
    this.threadId = threadId;
    this.props = props;
    repetitions = Integer.parseInt(props.get("repetitions"));
    count = Integer.parseInt(props.get("readCount"));

    recordPerf = props.get("recordPerf").equalsIgnoreCase("true");
    testName = props.get("testName");

    try{
      connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
      int i = 0;
      for (File file : new File(props.get("readQueryDir")).listFiles()) {
        if (i++ % (threadId+1) == 0){
          try{
            readStatement = connection.prepareStatement(new Scanner(file).useDelimiter("\\Z").next());
          } catch (java.io.FileNotFoundException e) { e.printStackTrace(); System.exit(-1); }
          metric = file.getPath();
        }
      }

      perfStatement = connection.prepareStatement("upsert into perf values (?, ?, ?, ?, ?, ?)");
    } catch (SQLException e) { e.printStackTrace(); }
  }

  public void run(){
    System.out.println("READ THREAD " + threadId + " starting " + repetitions + " rounds of " + count + " queries = " + repetitions * count + " total queries");

    long start = System.nanoTime();
    for (int i = 0; i < repetitions; i++){
      long t1 = System.nanoTime();
      for (int j = 0; j < count; j++){
        try{
          readStatement.executeQuery();
        } catch (SQLException e) { e.printStackTrace(); System.exit(-1); }
      }
      perf(threadId, i, t1);
    }

    double seconds = (double)(System.nanoTime() - start) / 1000000000.0;
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    System.out.println("READ DONE: " + threadId + ": " + repetitions * count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(repetitions*count/seconds)) + " queries/sec");
  }

  private Timestamp now(){
    long timeInMillis = System.currentTimeMillis();
    long timeInNanos = System.nanoTime();

    Timestamp ts = new Timestamp(timeInMillis);
    ts.setNanos((int) (timeInNanos % 1000000000));
    return ts;
  }

  private void perf(int threadId, int rep, long t1){
    double seconds = (double)(System.nanoTime() - t1) / 1000000000.0;
    if (recordPerf){
      try{
        perfStatement.setString(1, testName);
        perfStatement.setString(2, metric);
        perfStatement.setInt(3, threadId);
        perfStatement.setInt(4, rep);
        perfStatement.setObject(5, now());
        perfStatement.setObject(6, count/seconds);
        perfStatement.executeUpdate();
        connection.commit();
      } catch (SQLException e) { e.printStackTrace(); }
    }
    System.out.println("READ THREAD " + threadId + " rep " + rep + " of " + repetitions + ": " + count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(count/seconds)) + " queries/sec");
  }
}
