// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import de.kaleidox.util.discord.ui.response.ResponseElement;
import de.kaleidox.util.interfaces.Subclass;
import de.kaleidox.util.objects.NamedItem;

import org.javacord.api.util.logging.ExceptionLogger;

public class DialogueBranch<A> extends Dialogue {
    private final Dialogue previousBranch;
    private final ResponseElement<A> questionElement;
    private final ArrayList<Option> options;

    public DialogueBranch(final ResponseElement<A> questionElement) {
        this(null, questionElement);
    }

    public DialogueBranch(final Dialogue previousBranch, final ResponseElement<A> questionElement) {
        this.previousBranch = previousBranch;
        this.questionElement = questionElement;
        this.options = new ArrayList<Option>();
    }

    public <B> DialogueBranch<A> addOption(final Predicate<A> tester, final DialogueBranch<B> followingBranch) {
        return this.addOption(new Option<B>(tester, this, followingBranch));
    }

    public <B> DialogueBranch<A> addOption(final Option<B> option) {
        this.options.add(option);
        return this;
    }

    protected void start(final List<NamedItem> collectedItems) throws NullPointerException {
        this.questionElement.setParentBranch(this).build().thenAcceptAsync(response -> this.options.stream().filter(option -> option.tester.test(response.getItem())).map((Function<? super Object, ?>) Option::getGoToBranch).forEachOrdered(branch -> {
            collectedItems.add(response);
            if (branch.getClass() == DialogueEndpoint.class) {
                branch.runEndpoint(collectedItems);
            } else {
                branch.start(collectedItems);
            }
        })).exceptionally((Function<Throwable, ? extends Void>) ExceptionLogger.get(new Class[0]));
    }

    protected CompletableFuture<Void> runEndpoint(final List<NamedItem> collectedItems) {
        throw new AbstractMethodError("Abstract method; used by DialogueEndpoint.");
    }

    public class Option<B> implements Subclass {
        private final Predicate<A> tester;
        private final DialogueBranch<A> parentBranch;
        private final DialogueBranch<B> goToBranch;

        public Option(final Predicate<A> tester, final DialogueBranch<A> parentBranch, final DialogueBranch<B> goToBranch) {
            this.tester = tester;
            this.parentBranch = parentBranch;
            this.goToBranch = goToBranch;
        }

        public Predicate<A> getTester() {
            return this.tester;
        }

        public DialogueBranch<A> getParentBranch() {
            return this.parentBranch;
        }

        public DialogueBranch<B> getGoToBranch() {
            return this.goToBranch;
        }
    }
}
