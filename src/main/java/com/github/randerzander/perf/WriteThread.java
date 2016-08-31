package com.github.randerzander.perf;

import java.sql.*;
import org.apache.phoenix.jdbc.PhoenixDriver;

import java.util.Properties;
import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WriteThread implements Runnable {
  public int threadId;
  public HashMap<String, String> props;

  public WriteThread(int threadId, HashMap<String, String> props){
    this.threadId = threadId; this.props = props;
  }

  public void run(){
    int repetitions = Integer.parseInt(props.get("repetitions"));
    int count = Integer.parseInt(props.get("count"));
    int commitInterval = Integer.parseInt(props.get("commitInterval"));
    String sql = "upsert into " + props.get("writeTable") + " values (";

    try{
      Connection connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
      connection.setAutoCommit(props.get("autocommit").equals("true"));
      System.out.println("WRITE THREAD " + threadId + " starting " + repetitions + " rounds of " + count + " writes  = " + repetitions * count + " upserts, committing every " + commitInterval);

      long start = System.nanoTime();
      for (int i = 0; i < repetitions; i++){
        long t1 = System.nanoTime();
        for (int j = 0; j < count; j++){
          connection.createStatement().execute(sql + threadId + "," + i + "," + j + "," + t1 + ")");
          if (j % commitInterval == 0) connection.commit();
        }
        double seconds = (double)(System.nanoTime() - t1) / 1000000000.0;
        System.out.println("WRITE THREAD " + threadId + " rep " + i + " of " + repetitions + ": " + count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(count/seconds)) + " upserts/sec");
      }
      double seconds = (double)(System.nanoTime() - start) / 1000000000.0;

      Thread.sleep(5000);
      System.out.println("WRITE DONE: " + threadId + ": " + repetitions * count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(repetitions*count/seconds)) + " upserts/sec");
    } catch (Exception e) { e.printStackTrace(); }
  }
}
