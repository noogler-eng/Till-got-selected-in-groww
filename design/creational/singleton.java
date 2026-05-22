// The singleton pattern restricts the instantiation of a Class 
// and ensures that only one instance of the class exists in the 
// Java Virtual Machine

// 1. GOF creational pattern
// 2. Only one instance of the class
// 3. Must have global access point to get the instance of the class

// where they are used ?
// Singleton pattern is used for logging, drivers objects, caching, 
// and thread pool.


package design.creational;

// 1. Eager initialization
class EagerInitializedSingleton {

    // the instance of the singleton class is created at the time of class loading.
    // drawback to eager initialization is that the method is created even though 
    // the client application might not be using it
    private static final EagerInitializedSingleton instance = new EagerInitializedSingleton();
    private EagerInitializedSingleton() {}

    // We should avoid the instantiation unless the client calls the getInstance method
    public static EagerInitializedSingleton getInstance(){
        return instance;
    }
}


// 2. static block initialization
class StaticBlockSingleton {

    // instance of the class is created in the static block that provides the option 
    // for exception handling.
    private static StaticBlockSingleton instance;
    private StaticBlockSingleton() {}

    // static block we define all the static variables here
    static {
        try{
            instance = new StaticBlockSingleton();
        }catch(Exception e){
            System.out.println("Exception occurred in creating singleton instance");
            // as this is weerking in the runtime so throwing the runtime exception
            throw new RuntimeException("Exception occurred in creating singleton instance");
        }
    }

    public static StaticBlockSingleton getInstance(){
        return instance;
    }
}


// 3. Lazy Initialization
// this will werk fine in the single threaded environment like javascript.
// suppose in multithreaded language like two or more thread access the 
// getInstance method at the same time, they both will see that instance is 
// null and create the new instance of the class.
class LazyInitializedSingleton {
    // by default this instance is null
    private static LazyInitializedSingleton instance;
    private LazyInitializedSingleton() {}

    public static LazyInitializedSingleton getInstance(){
        if(instance == null){
            instance = new LazyInitializedSingleton();
        }
        return instance;
    }
}


// 4. Thread Safe Singleton
class ThreadSafeSingleton {
    private static ThreadSafeSingleton instance;
    private ThreadSafeSingleton() {}

    // synchronized:
    // making global access point to be synchronized, so that multiple threads 
    // can't access it at the same time.
    // drawback of this approach is that it reduces the performance because of the cost
    // overhead of synchronized method. Every time a thread tries to access the getInstance 
    // method, it has to acquire the lock on the class, which can lead to contention and 
    // reduced performance in a multi-threaded environment.
    // public static synchronized ThreadSafeSingleton getInstance(){
    //     if(instance == null){
    //         instance = new ThreadSafeSingleton();
    //     }
    //     return instance;
    // }

    // advanced version 
    public static ThreadSafeSingleton getInstance(){
        // remove the extra overhead of synchronized method by using double check locking
        if(instance == null){
            // only one thread can enter here at a time
            // suppose Thread A and Thread B reach here 
            // THread A enters, Thread B wait
            synchronized(ThreadSafeSingleton.class){
                if(instance == null){
                    instance = new ThreadSafeSingleton();
                }
            }
            // Thread A leaves with instance
            // Thread B inside synchronized function should check aagin the null thing
        }
        return instance;
    }
}



// 5. Bill Pugh Singleton Implementation
class BillPughSingleton {
    private BillPughSingleton() {}

    // inner static helper class, which is loaded on the first execution of getInstance method
    private static class SingletonHelper{
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
    }

    // only when someone calls the getInstance method then only the SingletonHelper class will be 
    // loaded and the instance will be created
    // it doesn't require synchronization because the class loading mechanism in Java is thread safe
    public static BillPughSingleton getInstance(){
        return SingletonHelper.INSTANCE;
    }
}