#include "geometry.rsh"
#include "imod.rsh"

static float3 createResult(int layer, double2 value, double height) {
    float colorX = (float) value.x;
    colorX -= floor(colorX);

    float colorY = (float) value.y;
    colorY -= floor(colorY);

    // add layer to colorX
    colorX += layer;

    // correct rounding mistake
    if(colorX >= (float) (layer + 1)) {
        colorX = layer;
    }

    float3 retVal = (float3) {
        colorX,
        colorY,
        (float) height
    };

    return retVal;
}

int *code;
uint32_t codeSize;

static float3 valueAt(double2 pt) {
	uint32_t pc = 0;

	int data[256];

	float3 result;

	while(pc < codeSize) {
		switch(code[pc]) {
            // === Add ===
            // Add: [*Int, *Int]
            case 0: data[code[pc + 3]] = data[code[pc + 1]] + data[code[pc + 2]]; pc += 4; break;
            // Add: [Int, *Int]
            case 1: data[code[pc + 3]] = code[pc + 1] + data[code[pc + 2]]; pc += 4; break;
            // Add: [*Real, *Real]
            case 2: (*((double*) (&data[code[pc + 3]]))) = (*((double*) (&data[code[pc + 1]]))) + (*((double*) (&data[code[pc + 2]]))); pc += 4; break;
            // Add: [Real, *Real]
            case 3: (*((double*) (&data[code[pc + 4]]))) = (*((double*) (&code[pc + 1]))) + (*((double*) (&data[code[pc + 3]]))); pc += 5; break;
            // Add: [*Cplx, *Cplx]
            case 4: (*((double2*) (&data[code[pc + 3]]))) = (*((double2*) (&data[code[pc + 1]]))) + (*((double2*) (&data[code[pc + 2]]))); pc += 4; break;
            // Add: [Cplx, *Cplx]
            case 5: (*((double2*) (&data[code[pc + 6]]))) = ((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}) + (*((double2*) (&data[code[pc + 5]]))); pc += 7; break;
            // === Sub ===
            // Sub: [*Int, *Int]
            case 6: data[code[pc + 3]] = data[code[pc + 1]] - data[code[pc + 2]]; pc += 4; break;
            // Sub: [Int, *Int]
            case 7: data[code[pc + 3]] = code[pc + 1] - data[code[pc + 2]]; pc += 4; break;
            // Sub: [*Real, *Real]
            case 8: (*((double*) (&data[code[pc + 3]]))) = (*((double*) (&data[code[pc + 1]]))) - (*((double*) (&data[code[pc + 2]]))); pc += 4; break;
            // Sub: [Real, *Real]
            case 9: (*((double*) (&data[code[pc + 4]]))) = (*((double*) (&code[pc + 1]))) - (*((double*) (&data[code[pc + 3]]))); pc += 5; break;
            // Sub: [*Cplx, *Cplx]
            case 10: (*((double2*) (&data[code[pc + 3]]))) = (*((double2*) (&data[code[pc + 1]]))) - (*((double2*) (&data[code[pc + 2]]))); pc += 4; break;
            // Sub: [Cplx, *Cplx]
            case 11: (*((double2*) (&data[code[pc + 6]]))) = ((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}) - (*((double2*) (&data[code[pc + 5]]))); pc += 7; break;
            // === Mul ===
            // Mul: [*Int, *Int]
            case 12: data[code[pc + 3]] = data[code[pc + 1]] * data[code[pc + 2]]; pc += 4; break;
            // Mul: [Int, *Int]
            case 13: data[code[pc + 3]] = code[pc + 1] * data[code[pc + 2]]; pc += 4; break;
            // Mul: [*Real, *Real]
            case 14: (*((double*) (&data[code[pc + 3]]))) = (*((double*) (&data[code[pc + 1]]))) * (*((double*) (&data[code[pc + 2]]))); pc += 4; break;
            // Mul: [Real, *Real]
            case 15: (*((double*) (&data[code[pc + 4]]))) = (*((double*) (&code[pc + 1]))) * (*((double*) (&data[code[pc + 3]]))); pc += 5; break;
            // Mul: [*Cplx, *Cplx]
            case 16: (*((double2*) (&data[code[pc + 3]]))) = mul((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break;
            // Mul: [Cplx, *Cplx]
            case 17: (*((double2*) (&data[code[pc + 6]]))) = mul(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break;
            // === Div ===
            // Div: [*Real, *Real]
            case 18: (*((double*) (&data[code[pc + 3]]))) = (*((double*) (&data[code[pc + 1]]))) / (*((double*) (&data[code[pc + 2]]))); pc += 4; break;
            // Div: [Real, *Real]
            case 19: (*((double*) (&data[code[pc + 4]]))) = (*((double*) (&code[pc + 1]))) / (*((double*) (&data[code[pc + 3]]))); pc += 5; break;
            // Div: [*Cplx, *Cplx]
            case 20: (*((double2*) (&data[code[pc + 3]]))) = div((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break;
            // Div: [Cplx, *Cplx]
            case 21: (*((double2*) (&data[code[pc + 6]]))) = div(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break;
            // === Mod ===
            // Mod: [*Int, *Int]
            case 22: data[code[pc + 3]] = mod(data[code[pc + 1]], data[code[pc + 2]]); pc += 4; break;
            // Mod: [Int, *Int]
            case 23: data[code[pc + 3]] = mod(code[pc + 1], data[code[pc + 2]]); pc += 4; break;
            // Mod: [*Int, Int]
            case 24: data[code[pc + 3]] = mod(data[code[pc + 1]], code[pc + 2]); pc += 4; break;
            // === Pow ===
            // Pow: [*Real, *Int]
            case 25: (*((double*) (&data[code[pc + 3]]))) = pow((*((double*) (&data[code[pc + 1]]))), data[code[pc + 2]]); pc += 4; break;
            // Pow: [Real, *Int]
            case 26: (*((double*) (&data[code[pc + 4]]))) = pow((*((double*) (&code[pc + 1]))), data[code[pc + 3]]); pc += 5; break;
            // Pow: [*Real, Int]
            case 27: (*((double*) (&data[code[pc + 3]]))) = pow((*((double*) (&data[code[pc + 1]]))), code[pc + 2]); pc += 4; break;
            // Pow: [*Real, *Real]
            case 28: (*((double*) (&data[code[pc + 3]]))) = pow((*((double*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))); pc += 4; break;
            // Pow: [Real, *Real]
            case 29: (*((double*) (&data[code[pc + 4]]))) = pow((*((double*) (&code[pc + 1]))), (*((double*) (&data[code[pc + 3]])))); pc += 5; break;
            // Pow: [*Real, Real]
            case 30: (*((double*) (&data[code[pc + 4]]))) = pow((*((double*) (&data[code[pc + 1]]))), (*((double*) (&code[pc + 2])))); pc += 5; break;
            // Pow: [*Cplx, *Int]
            case 31: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), data[code[pc + 2]]); pc += 4; break;
            // Pow: [Cplx, *Int]
            case 32: (*((double2*) (&data[code[pc + 6]]))) = pow(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), data[code[pc + 5]]); pc += 7; break;
            // Pow: [*Cplx, Int]
            case 33: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), code[pc + 2]); pc += 4; break;
            // Pow: [*Cplx, *Real]
            case 34: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))); pc += 4; break;
            // Pow: [Cplx, *Real]
            case 35: (*((double2*) (&data[code[pc + 6]]))) = pow(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double*) (&data[code[pc + 5]])))); pc += 7; break;
            // Pow: [*Cplx, Real]
            case 36: (*((double2*) (&data[code[pc + 4]]))) = pow((*((double2*) (&data[code[pc + 1]]))), (*((double*) (&code[pc + 2])))); pc += 5; break;
            // Pow: [*Cplx, *Cplx]
            case 37: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break;
            // Pow: [Cplx, *Cplx]
            case 38: (*((double2*) (&data[code[pc + 6]]))) = pow(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break;
            // Pow: [*Cplx, Cplx]
            case 39: (*((double2*) (&data[code[pc + 6]]))) = pow((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))})); pc += 7; break;
            // === Neg ===
            // Neg: [*Int]
            case 40: data[code[pc + 2]] = -data[code[pc + 1]]; pc += 3; break;
            // Neg: [*Real]
            case 41: (*((double*) (&data[code[pc + 2]]))) = -(*((double*) (&data[code[pc + 1]]))); pc += 3; break;
            // Neg: [*Cplx]
            case 42: (*((double2*) (&data[code[pc + 2]]))) = -(*((double2*) (&data[code[pc + 1]]))); pc += 3; break;
            // === Recip ===
            // Recip: [*Real]
            case 43: (*((double*) (&data[code[pc + 2]]))) = 1.0 / (*((double*) (&data[code[pc + 1]]))); pc += 3; break;
            // Recip: [*Cplx]
            case 44: (*((double2*) (&data[code[pc + 2]]))) = div((double2) {1., 0.}, (*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Abs ===
            // Abs: [*Int]
            case 45: data[code[pc + 2]] = abs(data[code[pc + 1]]); pc += 3; break;
            // Abs: [*Real]
            case 46: (*((double*) (&data[code[pc + 2]]))) = abs((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Abs: [*Cplx]
            case 47: (*((double*) (&data[code[pc + 2]]))) = abs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Assign ===
            // Assign: [*Int, *Int]
            case 48: data[code[pc + 1]] = data[code[pc + 2]]; pc += 3; break;
            // Assign: [*Int, Int]
            case 49: data[code[pc + 1]] = code[pc + 2]; pc += 3; break;
            // Assign: [*Real, *Real]
            case 50: (*((double*) (&data[code[pc + 1]]))) = (*((double*) (&data[code[pc + 2]]))); pc += 3; break;
            // Assign: [*Real, Real]
            case 51: (*((double*) (&data[code[pc + 1]]))) = (*((double*) (&code[pc + 2]))); pc += 4; break;
            // Assign: [*Cplx, *Cplx]
            case 52: (*((double2*) (&data[code[pc + 1]]))) = (*((double2*) (&data[code[pc + 2]]))); pc += 3; break;
            // Assign: [*Cplx, Cplx]
            case 53: (*((double2*) (&data[code[pc + 1]]))) = ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}); pc += 6; break;
            // === Jump ===
            // Jump: [Int]
            case 54: pc = code[pc + 1]; break;
            // === Equal ===
            // Equal: [*Int, *Int]
            case 55: if(data[code[pc + 1]] == data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Equal: [Int, *Int]
            case 56: if(code[pc + 1] == data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // === Less ===
            // Less: [*Int, *Int]
            case 57: if(data[code[pc + 1]] < data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [Int, *Int]
            case 58: if(code[pc + 1] < data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [*Int, Int]
            case 59: if(data[code[pc + 1]] < code[pc + 2]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [*Real, *Real]
            case 60: if((*((double*) (&data[code[pc + 1]]))) < (*((double*) (&data[code[pc + 2]])))) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [Real, *Real]
            case 61: if((*((double*) (&code[pc + 1]))) < (*((double*) (&data[code[pc + 3]])))) pc = code[pc + 4]; else pc = code[pc + 5];break;
            // Less: [*Real, Real]
            case 62: if((*((double*) (&data[code[pc + 1]]))) < (*((double*) (&code[pc + 2])))) pc = code[pc + 4]; else pc = code[pc + 5];break;
            // === Next ===
            // Next: [*Int, *Int]
            case 63: if(++data[code[pc + 2]] < data[code[pc + 1]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Next: [Int, *Int]
            case 64: if(++data[code[pc + 2]] < code[pc + 1]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // === Switch ===
            // Switch: [*Int, Int]
            case 65: pc = code[pc + 3 + (data[code[pc + 1]] % code[pc + 2] + code[pc + 2]) % code[pc + 2]]; break;
            // === Sqrt ===
            // Sqrt: [*Real]
            case 66: (*((double*) (&data[code[pc + 2]]))) = sqrt((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Sqrt: [*Cplx]
            case 67: (*((double2*) (&data[code[pc + 2]]))) = sqrt((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Exp ===
            // Exp: [*Real]
            case 68: (*((double*) (&data[code[pc + 2]]))) = exp((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Exp: [*Cplx]
            case 69: (*((double2*) (&data[code[pc + 2]]))) = exp((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Log ===
            // Log: [*Real]
            case 70: (*((double*) (&data[code[pc + 2]]))) = log((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Log: [*Cplx]
            case 71: (*((double2*) (&data[code[pc + 2]]))) = log((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Sin ===
            // Sin: [*Real]
            case 72: (*((double*) (&data[code[pc + 2]]))) = sin((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Sin: [*Cplx]
            case 73: (*((double2*) (&data[code[pc + 2]]))) = sin((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Cos ===
            // Cos: [*Real]
            case 74: (*((double*) (&data[code[pc + 2]]))) = cos((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Cos: [*Cplx]
            case 75: (*((double2*) (&data[code[pc + 2]]))) = cos((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Tan ===
            // Tan: [*Real]
            case 76: (*((double*) (&data[code[pc + 2]]))) = tan((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Tan: [*Cplx]
            case 77: (*((double2*) (&data[code[pc + 2]]))) = tan((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Asin ===
            // Asin: [*Real]
            case 78: (*((double*) (&data[code[pc + 2]]))) = asin((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Asin: [*Cplx]
            case 79: (*((double2*) (&data[code[pc + 2]]))) = asin((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Acos ===
            // Acos: [*Real]
            case 80: (*((double*) (&data[code[pc + 2]]))) = acos((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Acos: [*Cplx]
            case 81: (*((double2*) (&data[code[pc + 2]]))) = acos((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Atan ===
            // Atan: [*Real]
            case 82: (*((double*) (&data[code[pc + 2]]))) = atan((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Atan: [*Cplx]
            case 83: (*((double2*) (&data[code[pc + 2]]))) = atan((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Sinh ===
            // Sinh: [*Real]
            case 84: (*((double*) (&data[code[pc + 2]]))) = sinh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Sinh: [*Cplx]
            case 85: (*((double2*) (&data[code[pc + 2]]))) = sinh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Cosh ===
            // Cosh: [*Real]
            case 86: (*((double*) (&data[code[pc + 2]]))) = cosh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Cosh: [*Cplx]
            case 87: (*((double2*) (&data[code[pc + 2]]))) = cosh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Tanh ===
            // Tanh: [*Real]
            case 88: (*((double*) (&data[code[pc + 2]]))) = tanh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Tanh: [*Cplx]
            case 89: (*((double2*) (&data[code[pc + 2]]))) = tanh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Asinh ===
            // Asinh: [*Real]
            case 90: (*((double*) (&data[code[pc + 2]]))) = asinh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Asinh: [*Cplx]
            case 91: (*((double2*) (&data[code[pc + 2]]))) = asinh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Acosh ===
            // Acosh: [*Real]
            case 92: (*((double*) (&data[code[pc + 2]]))) = acosh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Acosh: [*Cplx]
            case 93: (*((double2*) (&data[code[pc + 2]]))) = acosh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Atanh ===
            // Atanh: [*Real]
            case 94: (*((double*) (&data[code[pc + 2]]))) = atanh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Atanh: [*Cplx]
            case 95: (*((double2*) (&data[code[pc + 2]]))) = atanh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === ToReal ===
            // ToReal: [*Int]
            case 96: (*((double*) (&data[code[pc + 2]]))) = (double) data[code[pc + 1]]; pc += 3; break;
            // === Cons ===
            // Cons: [*Real, *Real]
            case 97: (*((double2*) (&data[code[pc + 3]]))) = (double2) {(*((double*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))}; pc += 4; break;
            // Cons: [Real, *Real]
            case 98: (*((double2*) (&data[code[pc + 4]]))) = (double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&data[code[pc + 3]])))}; pc += 5; break;
            // Cons: [*Real, Real]
            case 99: (*((double2*) (&data[code[pc + 4]]))) = (double2) {(*((double*) (&data[code[pc + 1]]))), (*((double*) (&code[pc + 2])))}; pc += 5; break;
            // === Arg ===
            // Arg: [*Cplx]
            case 100: (*((double*) (&data[code[pc + 2]]))) = arg((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === ArgNorm ===
            // ArgNorm: [*Cplx]
            case 101: (*((double*) (&data[code[pc + 2]]))) = argnorm((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === RealPart ===
            // RealPart: [*Cplx]
            case 102: (*((double*) (&data[code[pc + 2]]))) = (*((double2*) (&data[code[pc + 1]]))).x; pc += 3; break;
            // === ImagPart ===
            // ImagPart: [*Cplx]
            case 103: (*((double*) (&data[code[pc + 2]]))) = (*((double2*) (&data[code[pc + 1]]))).y; pc += 3; break;
            // === Conj ===
            // Conj: [*Cplx]
            case 104: (*((double2*) (&data[code[pc + 2]]))) = conj((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Cabs ===
            // Cabs: [*Cplx]
            case 105: (*((double2*) (&data[code[pc + 2]]))) = cabs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Rabs ===
            // Rabs: [*Cplx]
            case 106: (*((double2*) (&data[code[pc + 2]]))) = rabs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Iabs ===
            // Iabs: [*Cplx]
            case 107: (*((double2*) (&data[code[pc + 2]]))) = iabs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Norm ===
            // Norm: [*Cplx]
            case 108: (*((double2*) (&data[code[pc + 2]]))) = (*((double2*) (&data[code[pc + 1]]))) / abs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Point ===
            // Point: []
            case 109: (*((double2*) (&data[code[pc + 1]]))) = pt; pc += 2; break;
            // === SetResult ===
            // SetResult: [*Int, *Cplx, *Real]
            case 110: result = createResult(data[code[pc + 1]], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&data[code[pc + 3]])))); pc += 4; break;
            // SetResult: [Int, *Cplx, *Real]
            case 111: result = createResult(code[pc + 1], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&data[code[pc + 3]])))); pc += 4; break;
            // SetResult: [*Int, Cplx, *Real]
            case 112: result = createResult(data[code[pc + 1]], ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&data[code[pc + 6]])))); pc += 7; break;
            // SetResult: [Int, Cplx, *Real]
            case 113: result = createResult(code[pc + 1], ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&data[code[pc + 6]])))); pc += 7; break;
            // SetResult: [*Int, *Cplx, Real]
            case 114: result = createResult(data[code[pc + 1]], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&code[pc + 3])))); pc += 5; break;
            // SetResult: [Int, *Cplx, Real]
            case 115: result = createResult(code[pc + 1], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&code[pc + 3])))); pc += 5; break;
            // SetResult: [*Int, Cplx, Real]
            case 116: result = createResult(data[code[pc + 1]], ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&code[pc + 6])))); pc += 8; break;
            // SetResult: [Int, Cplx, Real]
            case 117: result = createResult(code[pc + 1], ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&code[pc + 6])))); pc += 8; break;
            // === Max ===
            // Max: [*Int, *Int]
            case 118: data[code[pc + 3]] = max(data[code[pc + 1]], data[code[pc + 2]]); pc += 4; break;
            // Max: [Int, *Int]
            case 119: data[code[pc + 3]] = max(code[pc + 1], data[code[pc + 2]]); pc += 4; break;
            // Max: [*Real, *Real]
            case 120: (*((double*) (&data[code[pc + 3]]))) = max((*((double*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))); pc += 4; break;
            // Max: [Real, *Real]
            case 121: (*((double*) (&data[code[pc + 4]]))) = max((*((double*) (&code[pc + 1]))), (*((double*) (&data[code[pc + 3]])))); pc += 5; break;
            // Max: [*Cplx, *Cplx]
            case 122: (*((double2*) (&data[code[pc + 3]]))) = max((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break;
            // Max: [Cplx, *Cplx]
            case 123: (*((double2*) (&data[code[pc + 6]]))) = max(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break;
            // === Min ===
            // Min: [*Int, *Int]
            case 124: data[code[pc + 3]] = min(data[code[pc + 1]], data[code[pc + 2]]); pc += 4; break;
            // Min: [Int, *Int]
            case 125: data[code[pc + 3]] = min(code[pc + 1], data[code[pc + 2]]); pc += 4; break;
            // Min: [*Real, *Real]
            case 126: (*((double*) (&data[code[pc + 3]]))) = min((*((double*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))); pc += 4; break;
            // Min: [Real, *Real]
            case 127: (*((double*) (&data[code[pc + 4]]))) = min((*((double*) (&code[pc + 1]))), (*((double*) (&data[code[pc + 3]])))); pc += 5; break;
            // Min: [*Cplx, *Cplx]
            case 128: (*((double2*) (&data[code[pc + 3]]))) = min((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break;
            // Min: [Cplx, *Cplx]
            case 129: (*((double2*) (&data[code[pc + 6]]))) = min(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break;
            // === Floor ===
            // Floor: [*Real]
            case 130: (*((double*) (&data[code[pc + 2]]))) = floor((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Floor: [*Cplx]
            case 131: (*((double2*) (&data[code[pc + 2]]))) = floor((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Fract ===
            // Fract: [*Real]
            case 132: (*((double*) (&data[code[pc + 2]]))) = fract((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Fract: [*Cplx]
            case 133: (*((double2*) (&data[code[pc + 2]]))) = fract((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === ArcOp ===
            // ArcOp: [*Cplx, *Cplx, *Real, *Cplx]
            case 134: (*((double*) (&data[code[pc + 5]]))) = arc((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&data[code[pc + 3]]))), (*((double2*) (&data[code[pc + 4]])))); pc += 6; break;
            // ArcOp: [Cplx, *Cplx, *Real, *Cplx]
            case 135: (*((double*) (&data[code[pc + 8]]))) = arc(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]]))), (*((double*) (&data[code[pc + 6]]))), (*((double2*) (&data[code[pc + 7]])))); pc += 9; break;
            // ArcOp: [*Cplx, Cplx, *Real, *Cplx]
            case 136: (*((double*) (&data[code[pc + 8]]))) = arc((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&data[code[pc + 6]]))), (*((double2*) (&data[code[pc + 7]])))); pc += 9; break;
            // ArcOp: [Cplx, Cplx, *Real, *Cplx]
            case 137: (*((double*) (&data[code[pc + 11]]))) = arc(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), ((double2) {(*((double*) (&code[pc + 5]))), (*((double*) (&code[pc + 7])))}), (*((double*) (&data[code[pc + 9]]))), (*((double2*) (&data[code[pc + 10]])))); pc += 12; break;
            // ArcOp: [*Cplx, *Cplx, Real, *Cplx]
            case 138: (*((double*) (&data[code[pc + 6]]))) = arc((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&code[pc + 3]))), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break;
            // ArcOp: [Cplx, *Cplx, Real, *Cplx]
            case 139: (*((double*) (&data[code[pc + 9]]))) = arc(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]]))), (*((double*) (&code[pc + 6]))), (*((double2*) (&data[code[pc + 8]])))); pc += 10; break;
            // ArcOp: [*Cplx, Cplx, Real, *Cplx]
            case 140: (*((double*) (&data[code[pc + 9]]))) = arc((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&code[pc + 6]))), (*((double2*) (&data[code[pc + 8]])))); pc += 10; break;
            // ArcOp: [Cplx, Cplx, Real, *Cplx]
            case 141: (*((double*) (&data[code[pc + 12]]))) = arc(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), ((double2) {(*((double*) (&code[pc + 5]))), (*((double*) (&code[pc + 7])))}), (*((double*) (&code[pc + 9]))), (*((double2*) (&data[code[pc + 11]])))); pc += 13; break;
            // ArcOp: [*Cplx, *Cplx, *Real, Cplx]
            case 142: (*((double*) (&data[code[pc + 8]]))) = arc((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&data[code[pc + 3]]))), ((double2) {(*((double*) (&code[pc + 4]))), (*((double*) (&code[pc + 6])))})); pc += 9; break;
            // ArcOp: [Cplx, *Cplx, *Real, Cplx]
            case 143: (*((double*) (&data[code[pc + 11]]))) = arc(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]]))), (*((double*) (&data[code[pc + 6]]))), ((double2) {(*((double*) (&code[pc + 7]))), (*((double*) (&code[pc + 9])))})); pc += 12; break;
            // ArcOp: [*Cplx, Cplx, *Real, Cplx]
            case 144: (*((double*) (&data[code[pc + 11]]))) = arc((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&data[code[pc + 6]]))), ((double2) {(*((double*) (&code[pc + 7]))), (*((double*) (&code[pc + 9])))})); pc += 12; break;
            // ArcOp: [Cplx, Cplx, *Real, Cplx]
            case 145: (*((double*) (&data[code[pc + 14]]))) = arc(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), ((double2) {(*((double*) (&code[pc + 5]))), (*((double*) (&code[pc + 7])))}), (*((double*) (&data[code[pc + 9]]))), ((double2) {(*((double*) (&code[pc + 10]))), (*((double*) (&code[pc + 12])))})); pc += 15; break;
            // ArcOp: [*Cplx, *Cplx, Real, Cplx]
            case 146: (*((double*) (&data[code[pc + 9]]))) = arc((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&code[pc + 3]))), ((double2) {(*((double*) (&code[pc + 5]))), (*((double*) (&code[pc + 7])))})); pc += 10; break;
            // ArcOp: [Cplx, *Cplx, Real, Cplx]
            case 147: (*((double*) (&data[code[pc + 12]]))) = arc(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]]))), (*((double*) (&code[pc + 6]))), ((double2) {(*((double*) (&code[pc + 8]))), (*((double*) (&code[pc + 10])))})); pc += 13; break;
            // ArcOp: [*Cplx, Cplx, Real, Cplx]
            case 148: (*((double*) (&data[code[pc + 12]]))) = arc((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&code[pc + 6]))), ((double2) {(*((double*) (&code[pc + 8]))), (*((double*) (&code[pc + 10])))})); pc += 13; break;
            // === LineOp ===
            // LineOp: [*Cplx, *Cplx, *Cplx]
            case 149: (*((double*) (&data[code[pc + 4]]))) = line((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]]))), (*((double2*) (&data[code[pc + 3]])))); pc += 5; break;
            // LineOp: [Cplx, *Cplx, *Cplx]
            case 150: (*((double*) (&data[code[pc + 7]]))) = line(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]]))), (*((double2*) (&data[code[pc + 6]])))); pc += 8; break;
            // LineOp: [*Cplx, Cplx, *Cplx]
            case 151: (*((double*) (&data[code[pc + 7]]))) = line((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double2*) (&data[code[pc + 6]])))); pc += 8; break;
            // LineOp: [Cplx, Cplx, *Cplx]
            case 152: (*((double*) (&data[code[pc + 10]]))) = line(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), ((double2) {(*((double*) (&code[pc + 5]))), (*((double*) (&code[pc + 7])))}), (*((double2*) (&data[code[pc + 9]])))); pc += 11; break;
            // LineOp: [*Cplx, *Cplx, Cplx]
            case 153: (*((double*) (&data[code[pc + 7]]))) = line((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]]))), ((double2) {(*((double*) (&code[pc + 3]))), (*((double*) (&code[pc + 5])))})); pc += 8; break;
            // LineOp: [Cplx, *Cplx, Cplx]
            case 154: (*((double*) (&data[code[pc + 10]]))) = line(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]]))), ((double2) {(*((double*) (&code[pc + 6]))), (*((double*) (&code[pc + 8])))})); pc += 11; break;
            // LineOp: [*Cplx, Cplx, Cplx]
            case 155: (*((double*) (&data[code[pc + 10]]))) = line((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), ((double2) {(*((double*) (&code[pc + 6]))), (*((double*) (&code[pc + 8])))})); pc += 11; break;
            // === CircleOp ===
            // CircleOp: [*Cplx, *Real, *Cplx]
            case 156: (*((double*) (&data[code[pc + 4]]))) = circle((*((double2*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]]))), (*((double2*) (&data[code[pc + 3]])))); pc += 5; break;
            // CircleOp: [Cplx, *Real, *Cplx]
            case 157: (*((double*) (&data[code[pc + 7]]))) = circle(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double*) (&data[code[pc + 5]]))), (*((double2*) (&data[code[pc + 6]])))); pc += 8; break;
            // CircleOp: [*Cplx, Real, *Cplx]
            case 158: (*((double*) (&data[code[pc + 5]]))) = circle((*((double2*) (&data[code[pc + 1]]))), (*((double*) (&code[pc + 2]))), (*((double2*) (&data[code[pc + 4]])))); pc += 6; break;
            // CircleOp: [Cplx, Real, *Cplx]
            case 159: (*((double*) (&data[code[pc + 8]]))) = circle(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double*) (&code[pc + 5]))), (*((double2*) (&data[code[pc + 7]])))); pc += 9; break;
            // CircleOp: [*Cplx, *Real, Cplx]
            case 160: (*((double*) (&data[code[pc + 7]]))) = circle((*((double2*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]]))), ((double2) {(*((double*) (&code[pc + 3]))), (*((double*) (&code[pc + 5])))})); pc += 8; break;
            // CircleOp: [Cplx, *Real, Cplx]
            case 161: (*((double*) (&data[code[pc + 10]]))) = circle(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double*) (&data[code[pc + 5]]))), ((double2) {(*((double*) (&code[pc + 6]))), (*((double*) (&code[pc + 8])))})); pc += 11; break;
            // CircleOp: [*Cplx, Real, Cplx]
            case 162: (*((double*) (&data[code[pc + 8]]))) = circle((*((double2*) (&data[code[pc + 1]]))), (*((double*) (&code[pc + 2]))), ((double2) {(*((double*) (&code[pc + 4]))), (*((double*) (&code[pc + 6])))})); pc += 9; break;
            // === RectOp ===
            // RectOp: [*Cplx, *Cplx, *Cplx]
            case 163: (*((double*) (&data[code[pc + 4]]))) = rect((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]]))), (*((double2*) (&data[code[pc + 3]])))); pc += 5; break;
            // RectOp: [Cplx, *Cplx, *Cplx]
            case 164: (*((double*) (&data[code[pc + 7]]))) = rect(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]]))), (*((double2*) (&data[code[pc + 6]])))); pc += 8; break;
            // RectOp: [*Cplx, Cplx, *Cplx]
            case 165: (*((double*) (&data[code[pc + 7]]))) = rect((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double2*) (&data[code[pc + 6]])))); pc += 8; break;
            // RectOp: [Cplx, Cplx, *Cplx]
            case 166: (*((double*) (&data[code[pc + 10]]))) = rect(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), ((double2) {(*((double*) (&code[pc + 5]))), (*((double*) (&code[pc + 7])))}), (*((double2*) (&data[code[pc + 9]])))); pc += 11; break;
            // RectOp: [*Cplx, *Cplx, Cplx]
            case 167: (*((double*) (&data[code[pc + 7]]))) = rect((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]]))), ((double2) {(*((double*) (&code[pc + 3]))), (*((double*) (&code[pc + 5])))})); pc += 8; break;
            // RectOp: [Cplx, *Cplx, Cplx]
            case 168: (*((double*) (&data[code[pc + 10]]))) = rect(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]]))), ((double2) {(*((double*) (&code[pc + 6]))), (*((double*) (&code[pc + 8])))})); pc += 11; break;
            // RectOp: [*Cplx, Cplx, Cplx]
            case 169: (*((double*) (&data[code[pc + 10]]))) = rect((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), ((double2) {(*((double*) (&code[pc + 6]))), (*((double*) (&code[pc + 8])))})); pc += 11; break;
		}
	}
	
	return result;
}