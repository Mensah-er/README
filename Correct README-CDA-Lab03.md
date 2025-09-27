# Constrained Device Application (Connected Devices)

## Lab Module 03

Be sure to implement all the PIOT-CDA-\* issues (requirements) listed at
[PIOT-INF-03-001 - Lab Module
03](https://github.com/orgs/programming-the-iot/projects/1#column-10488379).

### Description
1.	What does your implementation do? 
The implementation of the Constrained Device Application (CDA) for Lab
Module 03 extends the foundation built in the previous modules by
integrating and coordinating multiple system-level managers.
Specifically, the CDA now leverages the **DeviceDataManager** as the
central orchestrator for system performance monitoring, simulated sensor
data collection, and actuator control. This ensures a unified
architecture where all data and control flows are consistently managed
and processed, while still remaining modular enough to support
extensibility. Through this integration, the CDA can gather telemetry
(CPU, memory, and sensor metrics), forward actuation commands, and
maintain a reliable runtime execution cycle, thereby meeting the
requirements defined in the PIOT-CDA issues.

2.	How does implementation work? 
The CDA works by instantiating a `DeviceDataManager` object within the
`ConstrainedDeviceApp`, which serves as the core entry point for device
operations. The `DeviceDataManager` internally manages the
**SystemPerformanceManager**, **SensorAdapterManager**, and
**ActuatorAdapterManager**, wiring them together via the
`setDataMessageListener()` callback mechanism to ensure proper data
flow. When the CDA is started, the `DeviceDataManager.startManager()`
method activates each sub-manager, enabling periodic telemetry
reporting, simulated sensor data generation, and actuator simulation.
Likewise, stopping the CDA ensures graceful shutdown through
`DeviceDataManager.stopManager()`. This modular, event-driven
architecture allows the application to run continuously if configured,
or for a bounded period, fulfilling the design requirements for
constrained devices in IoT environments.

### Code Repository and Branch

URL:
<https://github.com/Mensah-er/cda-python-components/tree/labmodule03>\
<https://github.com/Mensah-er/README/tree/labmodule03>\

*(Update to your actual repo/branch if different.)*

### UML Design Diagram(s)

-   UML diagram of CDA with DeviceDataManager, SystemPerformanceManager,
    SensorAdapterManager, and ActuatorAdapterManager.\
    *(Include the diagram image you created earlier, e.g.,
    `uml_cda_lab03.png`.)*

### Unit Tests Executed

-	test_SensorData.py
-	test_SystemPerformanceData.py
-	test_HumiditySensorSimTask.py
-	test_PressureSensorSimTask.py
-	test_TemperatureSensorSimTask.py
-	test_HumidifierActuatorSimTask.py
-	test_HvacActuatorSimTask.py

### Integration Tests Executed

-	test_SensorAdapterManager.py
-	test_ActuatorAdapterManager.py
-	test_DeviceDataManagerNoComms.py
-	ConstrainedDeviceAppTest.py

## UML Diagram

The following UML diagram shows the architecture of the CDA system with all sensors and actuators:

![CDA UML Diagram](/mnt/data/A_UML_class_diagram_depicts_the_architecture_of_a_.png)
<img width="1024" height="1536" alt="UML" src="https://github.com/user-attachments/assets/755b3740-d62e-4a5e-8ced-c2dab8c08061" />
