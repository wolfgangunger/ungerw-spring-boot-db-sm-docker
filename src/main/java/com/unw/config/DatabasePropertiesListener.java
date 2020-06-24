package com.unw.config;

import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabasePropertiesListener implements ApplicationListener<ApplicationPreparedEvent> {

  private final static String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
  private final static String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";
  private final static String SPRING_DATASOURCE_URL = "spring.datasource.url";

  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public void onApplicationEvent(ApplicationPreparedEvent event) {
    //1 with sdk
//    readingSecretsUsingSDK(event);

// secrets manager jdbc solution needs no code 

    //3 with env variables
    readEnvVariable(event);
  }

  /**
   * 1 approach, using the aws secrets manager sdk
   *
   * @param event
   */
  private void readingSecretsUsingSDK(ApplicationPreparedEvent event) {
    System.out.println("getting secrets");
    try {
      String secretJson = getSecret();
      String dbUser = getString(secretJson, "username");
      String dbPassword = getString(secretJson, "password");
      String dbHost = getString(secretJson, "host");
      String port = getString(secretJson, "port");
      String engine = getString(secretJson, "engine");
      String dbName = getString(secretJson, "dbname");
      System.out.println("db user " + dbUser);
      System.out.println("password " + dbPassword);
      System.out.println("dbHost " + dbHost);

      String connectionString = "jdbc:" + engine + "://" + dbHost + ":" + port + "/" + dbName + "?useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false";
      System.out.println(connectionString);
      ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
      Properties props = new Properties();
      props.put(SPRING_DATASOURCE_USERNAME, dbUser);
      props.put(SPRING_DATASOURCE_PASSWORD, dbPassword);
      props.put(SPRING_DATASOURCE_URL, connectionString);
      environment.getPropertySources().addFirst(new PropertiesPropertySource("aws.secret.manager", props));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getSecret() {
    // adapt to your secrets name
    String secretName = "dev/my-secret";
    String region = "eu-central-1";

    String secret = "";
    String decodedBinarySecret = "";
    System.out.println("getting secrets, loading secrets manager");
    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
    GetSecretValueResult getSecretValueResult = null;

    AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();
    try {
      getSecretValueResult = client.getSecretValue(getSecretValueRequest);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    if (getSecretValueResult.getSecretString() != null) {
      secret = getSecretValueResult.getSecretString();
    } else {
      decodedBinarySecret = new String(
              Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
    }
    return secret != null ? secret : decodedBinarySecret;
  }

  /**
   * 3rd apporach using env variables
   *
   * @param event
   */
  private void readEnvVariable(ApplicationPreparedEvent event) {
    System.out.println("reading env variabels");
    ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
    String dbSecret = environment.getProperty("dbSecret");
    System.out.println("env var master : " + dbSecret);

    String dbUser = getString(dbSecret, "username");
    String dbPassword = getString(dbSecret, "password");
    String dbHost = getString(dbSecret, "host");
    String port = getString(dbSecret, "port");
    String engine = getString(dbSecret, "engine");
    String dbName = getString(dbSecret, "dbname");
    System.out.println("db user " + dbUser);
    System.out.println("password " + dbPassword);
    System.out.println("dbHost " + dbHost);

    String connectionString = "jdbc:" + engine + "://" + dbHost + ":" + port + "/" + dbName + "?useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false";
    System.out.println(connectionString);

    Properties props = new Properties();
    props.put(SPRING_DATASOURCE_USERNAME, dbUser);
    props.put(SPRING_DATASOURCE_PASSWORD, dbPassword != null ? dbPassword : "--");
    props.put(SPRING_DATASOURCE_URL, connectionString);
    environment.getPropertySources().addFirst(new PropertiesPropertySource("aws.secret.manager", props));
  }

  private String getString(String json, String path) {
    try {
      JsonNode root = mapper.readTree(json);
      return root.path(path).asText();
    } catch (IOException e) {
      System.out.println(e.getMessage());
      return null;
    }
  }

}
