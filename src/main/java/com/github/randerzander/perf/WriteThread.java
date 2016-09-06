package com.github.randerzander.perf;

import java.sql.*;

import java.util.Properties;
import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WriteThread implements Runnable {
  int threadId;
  HashMap<String, String> props;

  Connection connection;
  int count;
  int repetitions;
  int commitInterval;
  PreparedStatement statement;
  ResultSetMetaData columns;

  boolean recordPerf;
  String testName;
  String metric;

  public WriteThread(int threadId, HashMap<String, String> props){
    this.threadId = threadId;
    this.props = props;

    repetitions = Integer.parseInt(props.get("repetitions"));
    count = Integer.parseInt(props.get("count"));
    commitInterval = Integer.parseInt(props.get("commitInterval"));

    recordPerf = props.get("recordPerf").equalsIgnoreCase("true");
    testName = props.get("testName");
    metric = "writes/sec";

    try{
      connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
      connection.setAutoCommit(props.get("autocommit").equals("true"));
    } catch (SQLException e) { e.printStackTrace(); System.exit(-1); }

    String writeTable = props.get("writeTable");
    String writeColumns = props.get("writeColumns");
    String columnNames = "(";
    String values = "(";
    try {
      String prefix = (writeColumns == null) ? "select *" : "select " + columns;
      columns = connection.createStatement().executeQuery(prefix + " from " + writeTable + " limit 0").getMetaData();
      for (int i = 1; i <= columns.getColumnCount(); i++){
        columnNames += columns.getColumnName(i) + ",";
        values += "?,";
      }
      columnNames = columnNames.substring(0, columnNames.length() - 1) + ")";
      values = values.substring(0, values.length() - 1) + ")";
      statement = connection.prepareStatement("upsert into " + writeTable + " " + columnNames + " values " + values);
    } catch (SQLException e) { e.printStackTrace(); System.exit(-1); }
  }

  public void run(){
    System.out.println("WRITE THREAD " + threadId + " starting " + repetitions + " rounds of " + count + " writes = " + repetitions * count + " upserts, committing every " + commitInterval);

    long start = System.nanoTime();
    for (int i = 0; i < repetitions; i++){
      long t1 = System.nanoTime();
      for (int j = 0; j < count; j++) write(j % commitInterval == 0);
      perf(threadId, i, t1);
    }

    double seconds = (double)(System.nanoTime() - start) / 1000000000.0;
    System.out.println("WRITE DONE: " + threadId + ": " + repetitions * count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(repetitions*count/seconds)) + " upserts/sec");
  }

  private static String CharSet = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ234567890!@#$";
  static public String randString(int len) {
    String str = "";
    for (int a = 1; a <= len; a++) {
      str += CharSet.charAt(new java.util.Random().nextInt(CharSet.length()));
    }
    return str;
  }

  private int maxLen = 1000;
  private int min = 1;
  private int max = 10000;
  private int randInt(){
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }

  private void fillStatement(){
    try{
      for (int i = 1; i <= columns.getColumnCount(); i++){
        Object tmp = null;
        switch (columns.getColumnType(i)){
          case Types.VARCHAR: tmp = randString(randInt()); break;
          case Types.INTEGER: tmp = randInt(); break;
          case Types.DOUBLE: tmp = randInt(); break;
          case Types.TIMESTAMP: now(); break;
          default: break;
        }
        statement.setObject(i, tmp);
      }
    } catch (SQLException e) { e.printStackTrace(); System.exit(-1); }
  }

  private void write(boolean commit){
    try{
      fillStatement();
      if (commit) statement.executeUpdate();
    } catch (SQLException e) { e.printStackTrace(); }
  }

  private Timestamp now(){
    long timeInMillis = System.currentTimeMillis();
    long timeInNanos = System.nanoTime();

    Timestamp ts = new Timestamp(timeInMillis);
    ts.setNanos((int) (timeInNanos % 1000000000));
    return ts;
  }

  private void perf(int threadId, int rep, long t1){
    double seconds = (System.nanoTime() - t1) / 1000000000.0;
    if (recordPerf){
      String sql = "upsert into perf values ('"+testName+"','"+metric+"',"+ threadId + "," + rep + ",'" + now() + "'," + String.valueOf(count/seconds)+")";
      try{
        connection.createStatement().execute(sql);
        connection.commit();
      } catch (SQLException e) { e.printStackTrace(); }
    }

    System.out.println("WRITE THREAD " + threadId + " rep " + rep + " of " + repetitions + ": " + count + " / " + String.valueOf(seconds) + " = " + String.valueOf(Math.round(count/seconds)) + " upserts/sec");
  }

}
