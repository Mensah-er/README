# Constrained Device Application (Connected Devices)

## Lab Module 06

Be sure to implement all the PIOT-CDA-* issues (requirements) listed at [PIOT-INF-06-001 - Lab Module 06](https://github.com/orgs/programming-the-iot/projects/1#column-10488434).

### Description

NOTE: Include two full paragraphs describing your implementation approach by answering the questions listed below.

What does your implementation do? 
Lab Module 06 implementation enables the Constrained Device Application (CDA) to exchange telemetry and control data with the Gateway Device Application (GDA) using MQTT. 
It connects to a local MQTT broker, publishes system performance metrics, and receives actuator command messages for processing.

How does your implementation work?
The system uses DeviceDataManager to coordinate data flow between the SystemPerformanceManager, ActuatorAdapterManager, and MqttClientConnector. 
The CDA periodically collects CPU, memory, and disk usage data and sends it through MQTT topics, while also handling incoming actuator commands. Configuration values are dynamically loaded from PiotConfig.props, and logging ensures proper tracking of connections, data exchange, and shutdown events.

### Code Repository and Branch

NOTE: Be sure to include the branch (e.g. https://github.com/programming-the-iot/python-components/tree/alpha001).

URL: https://github.com/Mensah-er/cda-python-components/tree/labmodule06

### UML Design Diagram(s)

NOTE: Include one or more UML designs representing your solution. It's expected each
diagram you provide will look similar to, but not the same as, its counterpart in the
book [Programming the IoT](https://learning.oreilly.com/library/view/programming-the-internet/9781492081401/).
![alt text](CDA-UML-LAB6-1.PNG)

### Unit Tests Executed

NOTE: TA's will execute your unit tests. You only need to list each test case below
(e.g. ConfigUtilTest, DataUtilTest, etc). Be sure to include all previous tests, too,
since you need to ensure you haven't introduced regressions.

- 
- 
- 

### Integration Tests Executed

NOTE: TA's will execute most of your integration tests using their own environment, with
some exceptions (such as your cloud connectivity tests). In such cases, they'll review
your code to ensure it's correct. As for the tests you execute, you only need to list each
test case below (e.g. SensorSimAdapterManagerTest, DeviceDataManagerTest, etc.)

- ConstrainedDeviceAppTest
- MqttClientConnectorTest
- 

EOF.
