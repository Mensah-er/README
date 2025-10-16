package programmingtheiot.data;

import java.io.Serializable;
import programmingtheiot.common.ConfigConst;

public class SensorData extends BaseIotData implements Serializable
{
    private static final long serialVersionUID = 1234567892L;

    // private variable
    private float value = ConfigConst.DEFAULT_VAL;

    // constructors
    public SensorData()
    {
        super();
    }

    public SensorData(int sensorType)
    {
        super();
        this.setTypeID(sensorType);
    }

    // public methods
    public float getValue()
    {
        return this.value;
    }

    public void setValue(float val)
    {
        updateTimeStamp();
        this.value = val;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(',');
        sb.append(ConfigConst.VALUE_PROP).append('=').append(this.getValue());
        return sb.toString();
    }

    @Override
    protected void handleUpdateData(BaseIotData data)
    {
        if (data instanceof SensorData) {
            SensorData sData = (SensorData) data;
            this.setValue(sData.getValue());
        }
    }
}
