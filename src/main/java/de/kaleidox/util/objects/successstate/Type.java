// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.objects.successstate;

import de.kaleidox.util.Bot;
import javax.annotation.Nullable;

public enum Type
{
    NONE(-1, ""), 
    NOT_RUN(-1, ""), 
    SUCCESSFUL(0, "\u2705"), 
    SILENT(0, "\u2705"), 
    INFO(1, "\u2139"), 
    UNIMPLEMENTED(1, "\ud83d\udd1a"), 
    SERVER_ONLY(1, "\ud83d\udeab"), 
    UNAUTHORIZED(2, "\u26d4"), 
    UNSUCCESSFUL(3, "\u2757"), 
    ERRORED(4, "\u274c");
    
    String reaction;
    int severity;
    
    private Type(final int severity, final String reaction) {
        this.severity = severity;
        this.reaction = reaction;
    }
    
    protected MessageRepresentation getStandardMessage(@Nullable final String text) {
        String putTitle = "";
        String putText = "";
        switch (this) {
            case SUCCESSFUL:
            case SILENT: {
                putTitle = "Everything's fine!";
                putText = ((text == null) ? "It's fine." : text);
                break;
            }
            case UNIMPLEMENTED: {
                putTitle = "This is not implemented yet!";
                putText = "Try again soon\u2122.";
                break;
            }
            case SERVER_ONLY: {
                putTitle = "This can't be used in Private Chat!";
                putText = "Try using it from a Server.";
                break;
            }
            case NOT_RUN: {
                putTitle = "Internal error occurred.";
                putText = "There was an internal error. Please contact the developer and tell them what you tried to do -> " + Bot.discordInvite();
                break;
            }
            case UNAUTHORIZED: {
                putTitle = "You are not authorized to do this!";
                putText = "If you think this is a mistake, please contact an Administrator.";
                break;
            }
            case ERRORED: {
                putTitle = "An error occurred:";
                break;
            }
            case UNSUCCESSFUL: {
                putTitle = "Something went wrong:";
                break;
            }
        }
        return new MessageRepresentation(this, putTitle, putText);
    }
}
