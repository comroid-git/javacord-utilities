// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.commands;

public enum CommandGroup
{
    BASIC("Basic Commands"), 
    MAIN_COMMANDS("Main Bot Commands"), 
    ADVANCED_COMMANDS("Advanced Bot Commands"), 
    AUTH_COMMANDS("Authorization Commands"), 
    BOT_SETUP("Bot Preferences"), 
    NONE("");
    
    public String name;
    
    private CommandGroup(final String name) {
        this.name = name;
    }
}
