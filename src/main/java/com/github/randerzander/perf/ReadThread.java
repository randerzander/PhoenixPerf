package com.github.randerzander.perf;

import java.sql.*;
import org.apache.phoenix.jdbc.PhoenixDriver;

import java.util.Properties;
import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ReadThread implements Runnable {
  public int threadId;
  public HashMap<String, String> props;

  public ReadThread(int threadId, HashMap<String, String> props){
    this.threadId = threadId; this.props = props;
  }

  public void run(){
    int repetitions = Integer.parseInt(props.get("repetitions"));
    int count = Integer.parseInt(props.get("count"));

    try{
      String[] queries = new Scanner(new File(props.get("readQueryFile"))).useDelimiter("\\Z").next().split(";");
      Connection connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
      System.out.println("READ THREAD " + threadId + " starting " + repetitions + " rounds of " + count + " queries = " + repetitions * count + " total queries");

      long start = System.nanoTime();
      for (int i = 0; i < repetitions; i++){
        long t1 = System.nanoTime();
        for (int j = 0; j < count; j++) connection.createStatement().execute(queries[i % queries.length]);
        double seconds = (double)(System.nanoTime() - t1) / 1000000000.0;
        System.out.println("READ THREAD " + threadId + " rep " + i + " of " + repetitions + ": " + count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(count/seconds)) + " qps");
      }
      double seconds = (double)(System.nanoTime() - start) / 1000000000.0;

      Thread.sleep(5000);
      System.out.println("READ DONE: " + threadId + ": " + repetitions * count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(repetitions*count/seconds)) + " qps");
    } catch (Exception e) { e.printStackTrace(); }
  }
}
