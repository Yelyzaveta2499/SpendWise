function fn() {
  // Global Karate config.
  var config = {};

  // Default to local Spring Boot port.
  var baseUrl = karate.properties['karate.baseUrl'];
  if (!baseUrl) {
    baseUrl = 'http://localhost:1111'; // Updated to match actual backend port
  }

  config.baseUrl = baseUrl;

  return config;
}
