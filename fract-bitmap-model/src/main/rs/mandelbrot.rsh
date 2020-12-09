static float3 mandelbrotValueAt(double2 pt) {

    int32_t maxIterationCount = 1000;
    float bailoutValue = 16.f;

    float logBailoutValue = native_log(bailoutValue);

    uint32_t i = 0;
    double2 z = (double2) {0.0, 0.0};

    while(i < maxIterationCount) {
        z = mul(z, z) + pt;
        float d = fast_length(convert_float2(z));

        if(d > bailoutValue) {
            float smooth = native_log2(native_log(d) / logBailoutValue);
            float value = native_log(i - smooth);
            return createResult(0, (double2) {value, 0.}, value);
        }

        i++;
    }

    return (float3) {1.f, 0.f, 0.f};
}

