package com.ldtteam.aequivaleo.analysis;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.rules.TestName;

import java.io.File;
import java.util.Objects;

import static org.mockito.Mockito.mock;

public final class TestAnalysisOwner implements IAnalysisOwner
{
    private final TestName testName;
    private final ResourceKey<Level> key;

    public TestAnalysisOwner(final TestName testName, final ResourceKey<Level> key) {
        this.testName = testName;
        this.key = key;
    }

    @Override
    public ResourceKey<Level> getIdentifier()
    {
        return key;
    }

    @Override
    public File getCacheDirectory()
    {
        return new File(new File("./"), "build/tests/caches/aequivaleo/analysis/" + testName().getMethodName());
    }

    public TestName testName() {return testName;}

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TestAnalysisOwner) obj;
        return Objects.equals(this.testName, that.testName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(testName);
    }

    @Override
    public String toString()
    {
        return "TestAnalysisOwner[" +
                 "testName=" + testName + ']';
    }
}
