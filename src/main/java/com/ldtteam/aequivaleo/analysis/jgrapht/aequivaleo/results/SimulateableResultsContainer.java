package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.mediation.IMediationCandidate;
import com.ldtteam.aequivaleo.api.mediation.IMediationContext;
import com.ldtteam.aequivaleo.mediation.SimpleMediationCandidate;
import com.ldtteam.aequivaleo.mediation.SimpleMediationContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SimulateableResultsContainer implements IResultsContainer {

    private CompoundInstanceSet results = null;

    private final INode node;
    private final Deque<SimulationState> simulations = new LinkedList<>();

    public SimulateableResultsContainer(INode node) {
        this.node = node;
        push();
    }

    @Override
    public void push() {
        if (simulations.isEmpty()) {
            simulations.push(new SimulationState(Collections.emptySet()));
        } else {
            simulations.push(simulations.peek().fork());
        }
    }

    @Override
    public void pop() {
        if (simulations.isEmpty() || simulations.size() == 1) {
            throw new IllegalStateException("No simulation state to pop.");
        }

        if (hasResults()) {
            throw new IllegalStateException("Cannot pop simulation state with results.");
        }

        simulations.pop();
    }

    @Override
    public void complete() {
        final SimulationState state = simulations.peek();
        if (state == null) {
            throw new IllegalStateException("No simulation state to complete.");
        }

        if (simulations.size() > 1) {
            pop();
        }

        force(state.results());
    }

    @Override
    public void commit() {
        if (simulations.isEmpty()) {
            throw new IllegalStateException("No simulation state to commit.");
        }

        if (simulations.size() == 1) {
            throw new IllegalStateException("Cannot commit simulation state without a parent state.");
        }

        final SimulationState state = simulations.pop();
        Objects.requireNonNull(simulations.peek()).offer(state.results());
    }

    @Override
    public void offer(CompoundInstanceSet offer) {
        if (hasResults() && !results.equals(offer)) {
            throw new IllegalStateException("Cannot offer results to a simulation state with results.");
        }

        if (simulations.isEmpty()) {
            throw new IllegalStateException("No simulation state to offer results to.");
        }

        simulations.peek().offer(offer);
    }

    @Override
    public void base(CompoundInstanceSet base) {
        if (hasResults() && !results.equals(base)) {
            throw new IllegalStateException("Cannot base results to a simulation state with results.");
        }

        if (simulations.isEmpty()) {
            throw new IllegalStateException("No simulation state to set base values to.");
        }

        simulations.peek().base(base);
    }

    @Override
    public void force(CompoundInstanceSet force) {
        if (hasResults() && !results.equals(force)) {
            throw new IllegalStateException("Cannot force results to a simulation state with results.");
        }

        if (simulations.isEmpty()) {
            throw new IllegalStateException("No simulation state to force results to.");
        }

        simulations.peek().force(force);
    }

    @Override
    public CompoundInstanceSet simulate() {
        if (hasResults()) {
            return results;
        }

        if (simulations.isEmpty())
            throw new IllegalStateException("No simulation state to simulate.");

        return simulations.peek().simulate();
    }

    @Override
    public CompoundInstanceSet results() {
        results = simulate();
        return results;
    }

    @Override
    public boolean hasResults() {
        return results != null;
    }

    @Override
    public boolean requiresCalculation() {
        if (hasResults()) {
            return false;
        }

        return simulations.isEmpty() || simulations.peek().lastSimulationResult == null;
    }

    private static final class SimulationState {

        private final Set<CompoundInstanceSet> candidates;

        private CompoundInstanceSet result;

        private CompoundInstanceSet base;
        private CompoundInstanceSet force;

        private CompoundInstanceSet lastSimulationResult;

        private SimulationState(Set<CompoundInstanceSet> initialCandidates) {
            this.candidates = new HashSet<>(initialCandidates);
        }

        private void offer(CompoundInstanceSet offer) {
            if ((lastSimulationResult == null || !lastSimulationResult.equals(offer)) && candidates.add(offer)) {
                lastSimulationResult = null;
            }

            if (candidates.size() == 1 && lastSimulationResult == null) {
                lastSimulationResult = offer;
            }
        }

        private CompoundInstanceSet results() {
            if (result == null) {
                result = simulate();
            }

            return result;
        }

        private @NotNull IMediationContext buildContext() {
            final Set<IMediationCandidate> groupCandidates = new HashSet<>();
            for (Set<CompoundInstance> candidateValues : candidates) {
                if (candidateValues.isEmpty()) {
                    continue;
                }

                final SimpleMediationCandidate simpleMediationCandidate = new SimpleMediationCandidate(candidateValues, () -> false);
                groupCandidates.add(simpleMediationCandidate);
            }

            return new SimpleMediationContext(
                    groupCandidates,
                    () -> true
            );
        }

        private CompoundInstanceSet simulate() {
            if (result != null) {
                return result;
            }

            if (lastSimulationResult == null) {
                this.lastSimulationResult = determineResultFromForced();
            }

            return lastSimulationResult;
        }

        private CompoundInstanceSet determineResultFromForced() {
            if (force != null) {
                return force.combine(base);
            }

            return determineResultFromCandidates();
        }

        private CompoundInstanceSet determineResultFromCandidates() {
            if (candidates.isEmpty()) {
                return CompoundInstanceSet.of();
            }

            if (candidates.size() == 1) {
                return candidates.iterator().next().combine(base);
            }

            final ICompoundTypeGroup[] groups = candidates.stream()
                    .flatMap(Collection::stream)
                    .map(CompoundInstance::getType)
                    .map(ICompoundType::getGroup)
                    .distinct()
                    .toArray(ICompoundTypeGroup[]::new);

            final Set<CompoundInstance> mediatedResult = new HashSet<>();
            for (ICompoundTypeGroup group : groups) {
                final IMediationContext context = buildContext();
                final Optional<Set<CompoundInstance>> mediatedValue = group.getMediationEngine().determineMediationResult(context);
                mediatedValue.ifPresent(mediatedResult::addAll);
            }

            return CompoundInstanceSet.of(mediatedResult).combine(base);
        }

        private void base(CompoundInstanceSet base) {
            this.base = base;
            this.lastSimulationResult = null;
        }

        public void force(CompoundInstanceSet results) {
            this.force = results;
            this.lastSimulationResult = null;
        }

        private @NotNull SimulationState fork() {
            final SimulationState fork = new SimulationState(candidates);
            fork.base = base;
            fork.force = force;
            fork.lastSimulationResult = lastSimulationResult;
            return fork;
        }
    }
}
