// parameters:
float3 lightVector;
float ambientReflection;
float diffuseReflection;

float specularReflection;
uint32_t shininess;

static float4 getPhongRefectionModelValue(float3 normalVector, float4 color) {
    float cosineAlpha = max(0.f, dot(normalVector, lightVector));
    float diffuse = diffuseReflection * cosineAlpha;

    float3 reflectionDirection = 2 * cosineAlpha * normalVector - lightVector;
    float specularBase = max(0.f, reflectionDirection.z); // viewer direction = {0,0,1}
    float specular = pown(specularBase * specularReflection, shininess); // since the light is white, use it as white.

    // specular color is white
    float4 lightColor = color * (ambientReflection + diffuse);

    lightColor.s3 = color.s3; // keep original alpha.

    return lightColor + specular * (float4) {1.0f, 0.0f, 0.0f, 1.0f};
}

static float3 getNormalVectorOf(float3 xVec, float3 yVec) {
    return fast_normalize(cross(xVec, yVec));
}

/*
 * dzx and dzy are scaled up to screen size by the caller. In-color
 * is assumed to be RGB
 */
static float4 adjustLight(float4 color, double dzx, double dzy) {
    float3 xVec = (float3) { 1.0f, 0.0f, (float) dzx };
    float3 yVec = (float3) { 0.0f, 1.0f, (float) dzy };

    float3 normalVector = getNormalVectorOf(xVec, yVec);

    return getPhongRefectionModelValue(normalVector, color);
}

