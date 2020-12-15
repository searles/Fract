package at.searles.commons;

import at.searles.commons.math.Cplx;
import org.junit.Test;

public class MathTest {
    @Test
    public void asinTest() {
        Cplx c = new Cplx();
        
        c.asin(0, 0);
        c.asin(1, 0); // 1.5707963267948966
        c.asin(0, 1); // 0.0:0.8813735870195428
        c.asin(-1, 0); // -1.5707963267948966
        c.asin(0, -1); // 0.0:-0.8813735870195429
    }

    @Test
    public void acosTest() {
        Cplx c = new Cplx();

        c.acos(0, 0); // 1.5707963267948966
        c.acos(1, 0); // 0.0
        c.acos(0, 1); // 1.5707963267948966:-0.8813735870195429
        c.acos(-1, 0); // 3.141592653589793
        c.acos(0, -1); // 1.5707963267948966:0.8813735870195428
    }

    @Test
    public void asinhTest() {
        Cplx c = new Cplx();

        c.asinh(0, 0); // 0.0
        c.asinh(1, 0); // 0.8813735870195429
        c.asinh(0, 1); // 0.0:1.5707963267948966
        c.asinh(-1, 0); // -0.8813735870195428
        c.asinh(0, -1); // 0.0:-1.5707963267948966
    }

    @Test
    public void acoshTest() {
        Cplx c = new Cplx();

        c.acosh(0, 0); // 0.0:1.0
        c.acosh(1, 0); // 1.0
        c.acosh(0, 1); // 0.0:2.414213562373095
        c.acosh(-1, 0); // -1.0
        c.acosh(0, -1); // 0.0:0.41421356237309515
    }
}
