package co.tala.example.specs.config

@Singleton(strict = false)
class Configuration {
    private ConfigObject properties

    private Configuration() {
        def configFile = "testConfig.groovy"
        def environment = System.properties.getProperty("environment") ?: "local"
        properties = new ConfigSlurper(environment).parse(new File(configFile).toURI().toURL())
    }

    String getValue(String key) {
        String value = properties.getProperty(key).toString()
        value == "[:]" ? null : value
    }
}
