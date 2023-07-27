package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import net.minecraft.ReportedException;
import net.minecraft.server.Bootstrap;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for processing data in parallel.
 */
public class StreamUtils
{

    private static final Logger        LOGGER              = LogManager.getLogger(StreamUtils.class);
    private static final AtomicInteger POOL_THREAD_COUNTER = new AtomicInteger();
    private static       ForkJoinPool  POOL                = null;
    private StreamUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: StreamUtils. This is a utility class");
    }

    /**
     * Sets up the fork join pool to execute parallel Streams on.
     * @param api The api to run from. Uses the given instances class loader as the class loader for the pool.
     */
    public static void setup(final IAequivaleoAPI api)
    {
        final ClassLoader classLoader = api.getClass().getClassLoader();
        POOL = new ForkJoinPool(
          Math.max(4, Runtime.getRuntime().availableProcessors() - 4),
          forkJoinPool -> {
              final ForkJoinWorkerThread thread = new ForkJoinWorkerThread(forkJoinPool) {};
              thread.setContextClassLoader(classLoader);
              thread.setName(String.format("Aequivaleo parallel executor: %s", POOL_THREAD_COUNTER.incrementAndGet()));
              return thread;
          },
          StreamUtils::onThreadException, true
        );
    }

    /**
     * Handles an exception on the worker thread.
     * @param thread The thread.
     * @param cause The exception.
     */
    private static void onThreadException(Thread thread, Throwable cause) {
        if (cause instanceof CompletionException) {
            cause = cause.getCause();
        }

        if (cause instanceof ReportedException) {
            Bootstrap.realStdoutPrintln(((ReportedException)cause).getReport().getFriendlyReport());
            System.exit(-1);
        }

        LOGGER.error(String.format("Caught exception in thread %s", thread), cause);
    }

    /**
     * Executes a task on the pool.
     * This needs to be used if a parallel stream is involved, since else class loading can break.
     *
     * @param runnable The task.
     */
    public static void execute(final Runnable runnable) {
        if (POOL == null)
            throw new IllegalStateException("Tried to run a task in parallel before aequivaleo has been initialized!");

        POOL.invoke(new RunnableExecuteAction(runnable));
    }

    private static final class RunnableExecuteAction extends ForkJoinTask<Void>
    {
        final Runnable runnable;

        private RunnableExecuteAction(Runnable runnable) {
            Validate.notNull(runnable);
            this.runnable = runnable;
        }

        public Void getRawResult() {
            return null;
        }

        public void setRawResult(Void v) {
        }

        public boolean exec() {
            this.runnable.run();
            return true;
        }
    }
}
