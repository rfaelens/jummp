quartz {
    println 'executing the JUMMP quartz closure'
    autoStartup = true
    jdbcStore = false
    waitForJobsToCompleteOnShutdown = true
    exposeSchedulerInRepository = false
    threadPool.class =org.quartz.simpl.SimpleThreadPool
    threadPool.threadCount = 10
    threadPool.threadPriority = 5
    threadPool.threadsInheritContextClassLoaderOfInitializingThread = true
    jobStore.class = 'org.quartz.simpl.RAMJobStore'

    props {
        scheduler.skipUpdateCheck = true
    }
}

environments {
    test {
        quartz {
            autoStartup = false
        }
    }
}
