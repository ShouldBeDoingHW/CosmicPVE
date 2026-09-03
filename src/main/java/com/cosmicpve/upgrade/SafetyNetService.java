package com.cosmicpve.upgrade;
public final class SafetyNetService {private SafetyNetService(){}public static int effectiveDestroyRate(int storedRate,int tier){if(storedRate<0||storedRate>100||tier<0||tier>5)throw new IllegalArgumentException("Invalid Safety Net input");return Math.max(0,storedRate-tier*2);}}
