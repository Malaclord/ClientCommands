package com.malaclord.clientcommands.client.util;

public class Lerp3D {

    /**
     * Linearly interpolates between two 3D points.
     *
     * @param pointA The starting 3D point as an array [x, y, z].
     * @param pointB The ending 3D point as an array [x, y, z].
     * @param t      The interpolation factor (0.0 <= t <= 1.0).
     * @return A new array representing the interpolated 3D point.
     */
    public static double[] lerp3D(double[] pointA, double[] pointB, double t) {
        // Ensure t is clamped between 0 and 1
        if (t < 0.0 || t > 1.0) {
            throw new IllegalArgumentException("Interpolation factor t must be between 0.0 and 1.0");
        }

        // Validate input
        if (pointA.length != 3 || pointB.length != 3) {
            throw new IllegalArgumentException("Both points must have exactly 3 dimensions.");
        }

        // Perform linear interpolation
        double x = pointA[0] + t * (pointB[0] - pointA[0]);
        double y = pointA[1] + t * (pointB[1] - pointA[1]);
        double z = pointA[2] + t * (pointB[2] - pointA[2]);

        return new double[]{x, y, z};
    }

    public static void main(String[] args) {
        double[] pointA = {1.0, 2.0, 3.0};
        double[] pointB = {4.0, 5.0, 6.0};

        double t = 0.5; // Midpoint between A and B

        double[] interpolatedPoint = lerp3D(pointA, pointB, t);

        System.out.printf("Interpolated Point: (%.2f, %.2f, %.2f)%n",
                interpolatedPoint[0], interpolatedPoint[1], interpolatedPoint[2]);
    }
}
