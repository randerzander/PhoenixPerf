package com.github.randerzander.perf;

import com.github.randerzander.perf.WriteThread;
import com.github.randerzander.perf.ReadThread;

import java.sql.*;
import org.apache.phoenix.jdbc.PhoenixDriver;

import java.util.Properties;
import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PhoenixPerf {

  public static void main(String args[]){
    HashMap<String, String> props = getPropertiesMap(args[0]);

    try {
      System.out.println("Loading Phoenix JDBC Driver");
      Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
      System.out.println("Opening connection to: " + props.get("jdbc.url"));
      Connection connection = DriverManager.getConnection(props.get("jdbc.url"), "", "");
      //Load and run setup statements
      if (props.get("preQueryFile") != null){
        System.out.println("Loading setup queries from: " + props.get("preQueryFile"));
        String preQueries = new Scanner(new File(props.get("preQueryFile"))).useDelimiter("\\Z").next();
        for (String query : preQueries.split(";")){
          System.out.println("Running PreQuery: " + query);
          connection.createStatement().execute(query);
        }
      }
    } catch (Exception ex) {
      System.out.println("Error: unable to load driver and run preQueries: " + ex.toString());
      System.exit(1);
    }
    if (props.get("writeThreads") != null)
      for (int i = 0; i < Integer.parseInt(props.get("writeThreads")); i++){
        Runnable r = new WriteThread(i, props);
        new Thread(r).start();
      }
    if (props.get("readThreads") != null)
      for (int i = 0; i < Integer.parseInt(props.get("readThreads")); i++){
        Runnable r = new ReadThread(i, props);
        new Thread(r).start();
      }
  }

  public static HashMap<String, String> getPropertiesMap(String file){
    Properties props = new Properties();
    try{ props.load(new FileReader(file)); }
    catch(Exception e){ e.printStackTrace(); System.exit(-1); }

    HashMap<String, String> map = new HashMap<String, String>();
    for (final String name: props.stringPropertyNames()) map.put(name, (String)props.get(name));
    return map;
  }

}
