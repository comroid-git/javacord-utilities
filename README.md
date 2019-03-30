# Javacord Utilities

This is a library used together with Javacord, providing lots of potentially useful extra objects and helper classes!
[![](https://jitpack.io/v/Kaleidox00/JavacordBotUtilities.svg)](https://jitpack.io/#Kaleidox00/JavacordBotUtilities)

## How to use:
### Maven:
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
	<dependency>
	    <groupId>com.github.Kaleidox00</groupId>
	    <artifactId>JavacordBotUtilities</artifactId>
	    <version>master-SNAPSHOT</version>
	</dependency>
```

### Gradle:
```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	dependencies {
	        implementation 'com.github.Kaleidox00:JavacordBotUtilities:master-SNAPSHOT'
	}
```

### Using the library:
Currently the library requires you to register a DiscordAPI using the following method:
```java
Registerer.initUtils(api)
```

## MultiShardHelper
The class [MultiShardHelper.java](https://kaleidox00.github.io/JavacordBotUtilities/de/kaleidox/util/MultiShardHelper.html) and [MultiShardBuilder.java](https://kaleidox00.github.io/JavacordBotUtilities/de/kaleidox/util/MultiShardBuilder.html) can be treated like Javacord's default `DiscordApiBuilder` and `DiscordApi`, as the `MultiShardHelper` implements the `DiscordApi` object.
