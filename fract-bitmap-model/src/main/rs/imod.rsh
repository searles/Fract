static int mod(int a, int b) {
    if(b == 0) {
        return 0;
    }

    if(a >= 0 && b > 0) {
        return a % b;
    }

    if(a > 0) {
        return -((-b - a % (-b)) % (-b));
    }

    if(b > 0) {
        return (b - (-a) % b) % b;
    }

    return -((-a) % (-b));
}