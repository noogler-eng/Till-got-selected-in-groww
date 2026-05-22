// when we have a superclass with multiple subclasses and based on input, 
// we need to return one of the subclasses
// This pattern takes out the responsibility of the instantiation of a Class 
// from the client program to the factory class. We can apply a singleton pattern 
// on the factory class or make the factory method static
package design.creational;

// superclass
// mainly used to create an interface for the subclasses and to override the toString method
abstract class Factory {
    public abstract String getRAM();
    public abstract String getHDD();
    public abstract String getCPU();

    @Override
    public String toString() {
        return "RAM= "+this.getRAM()+", HDD="+this.getHDD()+", CPU="+this.getCPU();
    }
}


// defining multiple sub classes
class PC extends Factory {
    private String ram;
    private String hdd;
    private String cpu;

    public PC(String ram, String hdd, String cpu){
        this.ram = ram;
        this.hdd = hdd;
        this.cpu = cpu;
    }


    @Override
    public String getRAM(){
        return this.ram;
    }
    @Override
    public String getHDD(){
        return this.hdd;
    }
    @Override
    public String getCPU(){
        return this.cpu;
    }
}


class Server extends Factory {
    private String ram;
    private String hdd;
    private String cpu;

    public Server(String ram, String hdd, String cpu){
        this.ram = ram;
        this.hdd = hdd;
        this.cpu = cpu;
    }

    @Override
    public String getRAM(){
        return this.ram;
    }
    @Override
    public String getHDD(){
        return this.hdd;
    }
    @Override
    public String getCPU(){
        return this.cpu;
    }
}


// factory class...
// decide which subclass instance will be returned based on input
class ComputerFactory {
    public static Factory getComputer(String type, String ram, String hdd, String cpu){
        if("PC".equalsIgnoreCase(type)) return new PC(ram, hdd, cpu);
        return new Server(ram, hdd, cpu);
    }
}


// advantages 
// 1. Factory design pattern provides approach to code for interface rather than implementation.
// 2. Factory pattern provides abstraction between implementation and client classes through inheritance.