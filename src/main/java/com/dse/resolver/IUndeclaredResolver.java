package com.dse.resolver;

import java.util.List;

public interface IUndeclaredResolver {
    void resolve();

    List<ResolvedSolution> getSolutions();
}
