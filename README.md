# Javacord Utilities [![Build Status](https://travis-ci.com/burdoto/javacord-utilities.svg?branch=master)](https://travis-ci.com/burdoto/javacord-utilities) [![Maven Central Release](https://maven-badges.herokuapp.com/maven-central/de.kaleidox/javacord-utilities/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.kaleidox/javacord-utilities) [![Development Release](https://jitpack.io/v/burdoto/javacord-utilities.svg)](https://jitpack.io/#burdoto/javacord-utilities)
#### Please note that this is a third party project and not officially created by the Javacord team.
Helpful classes and structures for Javacord

## Importing
### Maven
```xml
<dependency>
  <groupId>de.kaleidox</groupId>
  <artifactId>javacord-utilities</artifactId>
  <version>1.0.0</version>
</dependency>
```
### Gradle
```groovy
dependencies {
    implementation 'de.kaleidox:javacord-utilities:1.0.0'
}
```

## Usage
### Command Framework
The following is an example on how to use the included command framework:
```java
// Create an instance of the framework using your Javacord DiscordAPI object
CommandHandler framework = new CommandHandler(discordApi);

// Define the prefixes for the commands
framework.prefixes = new String[]{"dango!", "d!"};

// Register the predefined help-command, if needed
// The parameter defines a supplier for the EmbedBuilder base. If null, defaults to DefaultEmbedFactory.INSTANCE
framework.useDefaultHelp(null); 

// Register the command classes you want to use
// registering a class with static command methods
framework.registerCommands(StaticCommands.class); 
// registering an object with non-static commands
framework.registerCommands(new Commands()); 
// registering just one method, must be static
framework.registerCommands(StaticCommands.class.getMethod("staticMethod" /* define parameters for Class#getMethod */ )); 
```

Responding on Commands can be easily done by returning whatever you want to respond with. Take a look at the
 [Command annotation documentation](https://burdoto.github.io/javacord-utilities/) for information about what a command
 method can return, and what parameters are allowed.
 
If a command method throws any exception, this exception is caught and rethrown as a `RuntimeException`.
The command that caused the exception will also get a reaction that can be clicked to gather information about the exception thrown.
Exmple code: 
```java
@Command
public void exception() throws IOException {
    throw new IOException("Could not read file!");
}
```
![Example](http://kaleidox.de/share/img/bot/command-method-exception.png)

A `@Command` annotation does not need any parameters. If there are no `aliases` defined, the method name is being used as the only alias.
The following example will result in a command that can be called in our example via `dango!about`, which will send an Embed with only the Command author set as the Embed author:
```java
// Define a CommandGroup for the commands in this class. 
// This will cause the default help command to categorize commands by groups with the same names.
// If no name is defined in the CommandGroup annotation, the class name is used instead.
@CommandGroup(name = "Basic Commands", description = "Basic bot usage commands")
public class Commands {
    @Command
    public EmbedBuilder about(User author) {
        return new EmbedBuilder().setAuthor(author);
    }
}
```

### Server-based preference System
The artifact comes with a preference system that can be used to store properties per server; effectively enabling for simple per-server bot configuration.
The following is an example on how to use the preference system, and how to use the built-in per-server command prefix:
```java
// Create an instance of the system using a file object where the preferences should be stored
ServerPropertiesManager properties = new ServerPropertiesManager(new File("data/properties.json"));

// Register your properties
// In this example, we register a custom prefix property and tell the command framework to use this prefix
PropertyGroup customPrefixProperty = properties.register("bot.prefix", "dango!");

// Tell the command framework to use the registered property for prefixes as well
// The second argument will tell the command framework whether this custom prefix should be used exclusively. 
// If set to FALSE, the bot will also respond to the default command prefixes.
framework.useCustomPrefix(customPrefixProperty, false);

// Registering the property-command to enable the moderators of servers to modify the properties
// This command can be used by anyone in a server who effectively has the MANAGE_SERVER permission.
properties.usePropertyCommand(DefaultEmbedFactory.INSTANCE, framework);
```

