package cgramodel;

import util.SimpleMath;

/**
 * General purpose context mask which is used for the
 * Sensor context and serves as base class for other context implementations.
 *
 * The context holds an enable bit and provides the user with an bits for putting out
 * an address.
 *
 * Using this mask for the SensorInterface means that the addr part of this context
 * selects the sensor to read from, while the enable bit is the read enable.
 *
 * Currently, this context mask is also used for building additional contexts used
 * for writing internal PE log buffers. This is actually not required but necessary in the
 * current state of the hardware. In future versions, the only required entry of a PE Log
 * context will be the enable bit, not additional address.
 */
public class InterfaceContext extends ContextMask {
    private static final long serialVersionUID = 1L;

    protected int addr;
    protected int addrWidth;
    protected int enable;

    public InterfaceContext(int entityCount) {
        super();
        createMask(entityCount);
    }

    private void createMask(int entityCount) {
        addr = 0;
        addrWidth = SimpleMath.checkedLog(entityCount);
        enable = addrWidth;
        setContextWidth(enable + 1);
    }

    public int getAddr() {
        return addr;
    }

    public int getAddrWidth() {
        return addrWidth;
    }

    public int getEnable() {
        return enable;
    }

    public long setAddr(long context, int value) { return writeBitSet(context, value, addr, addrWidth); }

    public long setEnable(long context, int value) { return writeBitSet(context, value, enable, 1); }
}
