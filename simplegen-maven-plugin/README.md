# SimpleGen Maven Plugin
The SimpleGen Maven Plugin allows you to run SimpleGen within your Maven build. You can use this to generate code and then compile it with Maven in a single step.

To run the plugin, simply add it to the `build -> plugins` section of your Maven project:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.ancientlightstudios</groupId>
            <artifactId>simplegen-maven-plugin</artifactId>
            <version>${simplegen-version}</version>
            <!-- This configuration section is optional, 
                 if you leave it out the plugin will use
                 the default values -->
            <configuration>
                <sourceDirectory>${project.basedir}/src/main/simplegen</sourceDirectory>
                <outputDirectory>${project.basedir}/target/generated-sources</outputDirectory>
                <configFileName>config.yml</configFileName>
                <forceUpdate>false</forceUpdate>
            </configuration>
            <executions>
                <execution>
                    <id>generate</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Settings
The plugin offers the same settings as the command-line runner.

* `sourceDirectory` - the where the config files are located. This is also the base path for all relative path locations you use in the config file. Defaults to `src/main/simplegen`. 
* `outputDirectory` - the place where the generated files will be placed. Again all relative output paths you specify in the config file are relative to this output directory. Defaults to `target/generated-sources/simplegen`.
* `configFileName` - the name of the configuration file. Defaults to `config.yml`. There is rarely a need to override this, but it can be useful if you have multiple code generation passes and need to use different configuration files for each pass (see section below for details).
* `forceUpdate` - a flag indicating whether you want to force regeneration of the generated files even if no input file has changed. This is useful if you use environment variables or system properties in your templates and want to re-generate the files based on the current value of these variables. Defaults to `false`.

## Advanced usage - multiple generation passes
You can run the plugin more than once with different settings if you have a more complex code generation setup. This allows you to generate intermediate data files (or templates) in a first step and then use these in the second step. 

```xml
<plugin>
    <groupId>com.ancientlightstudios</groupId>
    <artifactId>simplegen-maven-plugin</artifactId>
    <version>${simplegen.version}</version>
    <executions>
        <execution>
            <id>generate-1st-pass</id>
            <phase>generate-sources</phase>
            <configuration>
                <configFileName>config-1st-pass.yml</configFileName>
                <outputDirectory>${project.build.directory}/first-pass-intermediates</outputDirectory>
            </configuration>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
        <execution>
            <id>generate-2nd-pass</id>
            <phase>generate-sources</phase>
            <configuration>
                <configFileName>config-2nd-pass.yml</configFileName>
                <outputDirectory>${project.build.directory}/final-output</outputDirectory>
            </configuration>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```