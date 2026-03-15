function fn() {
  // Global Karate config.
  var config = {};

  // Default to local Spring Boot port.
  var baseUrl = karate.properties['karate.baseUrl'];
  if (!baseUrl) {
    baseUrl = 'http://localhost:8080';
  }

  config.baseUrl = baseUrl;

  return config;
}

