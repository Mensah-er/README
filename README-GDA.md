# Gateway Device Application (Connected Devices)

## Lab Module 02

Be sure to implement all the PIOT-GDA-* issues (requirements) listed at [PIOT-INF-02-001 - Lab Module 02](https://github.com/orgs/programming-the-iot/projects/1#column-9974938).

### Description

NOTE: Include two full paragraphs describing your implementation approach by answering the questions listed below.

What does your implementation do? 
The implementation provides a Gateway Device Application (GDA) that monitors and manages system performance metrics on constrained or IoT devices. Specifically, it continuously collects telemetry data regarding CPU and memory utilization at scheduled intervals and logs these metrics for observation and analysis. The application is structured to be modular and extendable, allowing additional telemetry metrics or listeners to be added in the future. The system ensures that performance monitoring can run autonomously and that any failures in starting or stopping the monitoring services are logged clearly for debugging purposes. The unit and integration tests provided validate the functionality of the CPU and memory utilization tasks as well as the proper lifecycle management of the GDA.

How does your implementation work?
The GDA implementation is based on a modular, object-oriented architecture. The main class, GatewayDeviceApp, initializes the application, parses command-line arguments (currently unused), and starts the SystemPerformanceManager. The SystemPerformanceManager uses a scheduled executor service to periodically run SystemCpuUtilTask and SystemMemUtilTask, which retrieve CPU and memory utilization respectively. Each task extends a common BaseSystemUtilTask that defines the interface for telemetry retrieval. The telemetry values are logged using Java’s logging framework. Additionally, the manager supports the registration of a data message listener, allowing telemetry data to be sent to external systems. The application lifecycle is controlled via startApp() and stopApp() methods, which ensure proper startup, scheduled execution, and clean shutdown of all tasks. Unit tests and integration tests verify telemetry accuracy and application stability during operation

### Code Repository and Branch

NOTE: Be sure to include the branch (e.g. https://github.com/programming-the-iot/python-components/tree/alpha001).

URL: https://github.com/Mensah-er/gda-java-components/tree/labmodule02

### UML Design Diagram(s)

NOTE: Include one or more UML designs representing your solution. It's expected each
diagram you provide will look similar to, but not the same as, its counterpart in the
book [Programming the IoT](https://learning.oreilly.com/library/view/programming-the-internet/9781492081401/).


### Unit Tests Executed

NOTE: TA's will execute your unit tests. You only need to list each test case below
(e.g. ConfigUtilTest, DataUtilTest, etc). Be sure to include all previous tests, too,
since you need to ensure you haven't introduced regressions.

- SystemCpuUtilTaskTest.java
- SystemMemUtilTaskTest.java
- ConfigUtilCustomTest.java
- ConfigUtilDefaultTest.java
- 

### Integration Tests Executed

NOTE: TA's will execute most of your integration tests using their own environment, with
some exceptions (such as your cloud connectivity tests). In such cases, they'll review
your code to ensure it's correct. As for the tests you execute, you only need to list each
test case below (e.g. SensorSimAdapterManagerTest, DeviceDataManagerTest, etc.)

- GatewayDeviceAppTest.java
- SystemPerformanceManagerTest.java

EOF.
