// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.ui.response;

import de.kaleidox.util.objects.NamedItem;
import java.util.concurrent.CompletableFuture;
import de.kaleidox.util.Bot;
import javax.annotation.Nullable;
import de.kaleidox.util.discord.ui.DialogueBranch;
import java.util.concurrent.TimeUnit;
import org.javacord.api.entity.message.Message;
import java.util.ArrayList;
import org.javacord.api.entity.user.User;
import java.util.function.Predicate;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import java.util.function.Supplier;
import org.javacord.api.entity.message.Messageable;

public abstract class ResponseElement<ResultType>
{
    protected final String name;
    protected final Messageable parent;
    protected final Supplier<EmbedBuilder> embedBaseSupplier;
    protected final Predicate<User> userCanRespond;
    protected final ArrayList<Message> affiliateMessages;
    protected long duration;
    protected TimeUnit timeUnit;
    protected boolean deleteLater;
    private DialogueBranch parentBranch;
    
    public ResponseElement(final String name, final Messageable parent, @Nullable final Supplier<EmbedBuilder> embedBaseSupplier, @Nullable final Predicate<User> userCanRespond) {
        this.duration = 5L;
        this.timeUnit = TimeUnit.MINUTES;
        this.deleteLater = false;
        this.name = name;
        this.parent = parent;
        this.embedBaseSupplier = ((embedBaseSupplier == null) ? Bot::getEmbed : embedBaseSupplier);
        this.userCanRespond = ((userCanRespond == null) ? (user -> true) : userCanRespond);
        this.affiliateMessages = new ArrayList<Message>();
    }
    
    public ResponseElement<ResultType> setParentBranch(final DialogueBranch parentBranch) {
        this.parentBranch = parentBranch;
        return this;
    }
    
    public ResponseElement<ResultType> setTimeout(final long duration, final TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
        return this;
    }
    
    public ResponseElement<ResultType> deleteLater() {
        return this.deleteLater(!this.deleteLater);
    }
    
    public ResponseElement<ResultType> deleteLater(final boolean bool) {
        this.deleteLater = bool;
        return this;
    }
    
    public CompletableFuture<ResultType> build(final long duration, final TimeUnit timeUnit) {
        this.setTimeout(duration, timeUnit);
        return CompletableFuture.supplyAsync((NamedItem<ResultType>)this.build().join()::getItem);
    }
    
    public abstract CompletableFuture<NamedItem<ResultType>> build();
    
    public String getName() {
        return this.name;
    }
}
