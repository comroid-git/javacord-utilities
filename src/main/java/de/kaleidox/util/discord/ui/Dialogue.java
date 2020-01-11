// 
// Decompiled by Procyon v0.5.36
// 

package de.kaleidox.util.discord.ui;

import java.util.List;
import java.util.function.Supplier;

import de.kaleidox.util.discord.ui.response.ResponseElement;
import de.kaleidox.util.interfaces.Standalone;
import de.kaleidox.util.objects.NamedItem;

public class Dialogue<FirstBranchType> implements Standalone {
    private DialogueBranch<FirstBranchType> firstBranch;

    public DialogueBranch<FirstBranchType> setFirstBranch(final ResponseElement<FirstBranchType> firstElement) {
        final DialogueBranch<FirstBranchType> firstBranch = new DialogueBranch<FirstBranchType>(this, firstElement);
        return this.firstBranch = firstBranch;
    }

    public void start(final Supplier<List<NamedItem>> listSupplier) {
        this.firstBranch.start(listSupplier.get());
    }
}
