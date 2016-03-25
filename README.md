1.Program struct:  
    
    $ root/
      `--rx
         |--annotations                             -->注解
         |  |--Beta.java
         |  `--Experimental.java
         |--exceptions                              -->异常
         |  |--CompositeException
         |  |--Exception
         |  |--MissingBackpressureException
         |  |--OnCompletedFailedException
         |  |--OnErrorFailedException
         |  |--OnErrorNotImplementedException
         |  |--OnErrorThrowable
         |  `--UnsubscribeFailedException
         |--functions
         |--internal
         |  |--atomic                               ---->开源框架JCTools，处理并发操作 
         |  |--unsafe                               -/
         |--observables
         |--obserbers
         |--plugins
         |--schedulers
         |--singles
         |--subjects
         |--subscriptions
         |--Notification.java
         |--Observable.java
         |--Observer.java
         |--Producer.java
         |--Scheduler.java
         |--Single.java
         |--SingleSubscribler.java
         |--Subscriber.java
         `--Subcription.java
