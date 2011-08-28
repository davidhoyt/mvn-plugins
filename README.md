If you want to build/use these maven plugins, you'll need to add a repository to your pom:

```xml
<repositories>
	<repository>
		<id>github-davidhoyt-release-repo</id>
		<name>Github David Hoyt Maven Repository</name>
		<url>http://davidhoyt.github.com/mvn-repo/2/releases/</url>
	</repository>
</repositories>
```

In your plugins section, you'll need to add the very basics such as: 

```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.davidhoyt.maven.plugins</groupId>
			<artifactId>perforce</artifactId>
			<version>0.0.1-RC1</version>
		</plugin>
	</plugins>
</build>
```

The perforce plugin allows you to read the current changelist from your perforce repository and use it to populate a maven property or write it to a properties file.

Why write this? Well the build number plugin doesn't support perforce -- it currently only supports subversion, git, and mercurial. Perhaps down the road we can get this merged with the buildnumber plugin upstream.

For now, there are 2 goals:
<ul>
	<li>read-changelist-project-property</li>
	<li>write-changelist-properties</li>
</ul>

"read-changelist-project-property" will query the perforce server for the most recent submitted changelist id and then place it into a maven property. The default property name is "p4.changelist".

"write-changelist-properties" again queries the perforce server for the most recent submitted changelist, but it takes an output file and a property name to write the changelist id to. The default property name is "P4_CHANGELIST".

Here's an example of using read-changelist-project-property to read in the changelist number and then writing it to the manifest:
  
```xml
<build>
	<plugins>
		
		...
		
		<plugin>
			<groupId>com.github.davidhoyt.maven.plugins</groupId>
			<artifactId>perforce</artifactId>
			<version>0.0.1-RC1</version>
			<executions>
				<execution>
					<id>read-changelist</id>
					<phase>initialize</phase>
					<goals>
						<goal>read-changelist-project-property</goal>
					</goals>
					<configuration>
						<propertyName>vcs.revision</propertyName>
					</configuration>
				</execution>
			</executions>
		</plugin>
		
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-jar-plugin</artifactId>
			<version>2.3.1</version>
			<configuration>
				<archive>
					<manifestEntries>
						<VCS-Revision>${vcs.revision}</VCS-Revision>
					</manifestEntries>
				</archive>
			</configuration>
		</plugin>
		
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-scm-plugin</artifactId>
			<version>1.5</version>
			<dependencies>
				<dependency>
					<groupId>com.perforce</groupId>
					<artifactId>p4maven</artifactId>
					<version>[2011,2012)</version>
				</dependency>
			</dependencies>
			<configuration>
				<connectionType>connection</connectionType>
				<username>test</username>
				<password>test_password</password>
				<includes>**</includes>
			</configuration>
		</plugin>
		<plugin>
			<groupId>com.perforce</groupId>
			<artifactId>p4maven-mojo</artifactId>
			<version>[2011,2012)</version>
			<configuration>
				<connectionType>connection</connectionType>
				<username>test</username>
				<password>test_password</password>
				<includes>**</includes>
			</configuration>
		</plugin>
	</plugins>
</build>
```

And another example using write-changelist to write the changelist number to an embedded resource, META-INF/VCS.properties:

```xml
<build>
	<plugins>
		
		...
		
		<plugin>
			<groupId>com.github.davidhoyt.maven.plugins</groupId>
			<artifactId>perforce</artifactId>
			<version>0.0.1-RC1</version>
			<executions>
				<execution>
					<id>write-changelist</id>
					<phase>generate-resources</phase>
					<goals>
						<goal>write-changelist-properties</goal>
					</goals>
					<configuration>
						<propertyName>VCS.Revision</propertyName>
						<outputFile>${project.basedir}/src/main/resources/META-INF/VCS.properties</outputFile>
					</configuration>
				</execution>
			</executions>
		</plugin>
		
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-scm-plugin</artifactId>
			<version>1.5</version>
			<dependencies>
				<dependency>
					<groupId>com.perforce</groupId>
					<artifactId>p4maven</artifactId>
					<version>[2011,2012)</version>
				</dependency>
			</dependencies>
			<configuration>
				<connectionType>connection</connectionType>
				<username>test</username>
				<password>test_password</password>
				<includes>**</includes>
			</configuration>
		</plugin>
		<plugin>
			<groupId>com.perforce</groupId>
			<artifactId>p4maven-mojo</artifactId>
			<version>[2011,2012)</version>
			<configuration>
				<connectionType>connection</connectionType>
				<username>test</username>
				<password>test_password</password>
				<includes>**</includes>
			</configuration>
		</plugin>
	</plugins>
</build>
```

META-INF/VCS.properties will then look like:

```
#Mon Aug 29 13:36:35 PDT 2011
VCS.Revision=12345
```

