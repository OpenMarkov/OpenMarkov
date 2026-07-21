package org.openmarkov.core.model.network;

import org.openmarkov.core.localize.ClassLocalizable;

import java.io.Serial;
import java.io.Serializable;

public class Point2D {
    /**
     * The {@code Double} class defines a point specified in
     * {@code double} precision.
     *
     * @since 1.2
     */
    public static class Double implements Serializable, ClassLocalizable {
        /**
         * The X coordinate of this {@code Point2D}.
         *
         * @serial
         * @since 1.2
         */
        public double x;
        
        /**
         * The Y coordinate of this {@code Point2D}.
         *
         * @serial
         * @since 1.2
         */
        public double y;
        
        /**
         * Constructs and initializes a {@code Point2D} with
         * coordinates (0,&nbsp;0).
         *
         * @since 1.2
         */
        public Double() {
        }
        
        /**
         * Constructs and initializes a {@code Point2D} with the
         * specified coordinates.
         *
         * @param x the X coordinate of the newly
         *          constructed {@code Point2D}
         * @param y the Y coordinate of the newly
         *          constructed {@code Point2D}
         *
         * @since 1.2
         */
        public Double(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        /**
         * {@inheritDoc}
         *
         * @since 1.2
         */
        public double getX() {
            return x;
        }
        
        /**
         * {@inheritDoc}
         *
         * @since 1.2
         */
        public double getY() {
            return y;
        }
        
        public void setX(double x) {
            this.x = x;
        }
        
        public void setY(double y) {
            this.y = y;
        }
        
        /**
         * {@inheritDoc}
         *
         * @since 1.2
         */
        public void setLocation(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        /**
         * Returns a {@code String} that represents the value
         * of this {@code Point2D}.
         *
         * @return a string representation of this {@code Point2D}.
         *
         * @since 1.2
         */
        public String toString() {
            return this.localize();
        }
        
        /**
         * Use serialVersionUID from JDK 1.6 for interoperability.
         */
        @Serial
        private static final long serialVersionUID = 6150783262733311327L;
        
        
        /**
         * Sets the location of this {@code Point2D} to the same
         * coordinates as the specified {@code Point2D} object.
         *
         * @param p the specified {@code Point2D} to which to set
         *          this {@code Point2D}
         *
         * @since 1.2
         */
        public void setLocation(Double p) {
            setLocation(p.getX(), p.getY());
        }
        
        /**
         * Returns the square of the distance between two points.
         *
         * @param x1 the X coordinate of the first specified point
         * @param y1 the Y coordinate of the first specified point
         * @param x2 the X coordinate of the second specified point
         * @param y2 the Y coordinate of the second specified point
         *
         * @return the square of the distance between the two
         * sets of specified coordinates.
         *
         * @since 1.2
         */
        public static double distanceSq(double x1, double y1,
                                        double x2, double y2) {
            x1 -= x2;
            y1 -= y2;
            return (x1 * x1 + y1 * y1);
        }
        
        /**
         * Returns the distance between two points.
         *
         * @param x1 the X coordinate of the first specified point
         * @param y1 the Y coordinate of the first specified point
         * @param x2 the X coordinate of the second specified point
         * @param y2 the Y coordinate of the second specified point
         *
         * @return the distance between the two sets of specified
         * coordinates.
         *
         * @since 1.2
         */
        public static double distance(double x1, double y1,
                                      double x2, double y2) {
            x1 -= x2;
            y1 -= y2;
            return Math.sqrt(x1 * x1 + y1 * y1);
        }
        
        /**
         * Returns the square of the distance from this
         * {@code Point2D} to a specified point.
         *
         * @param px the X coordinate of the specified point to be measured
         *           against this {@code Point2D}
         * @param py the Y coordinate of the specified point to be measured
         *           against this {@code Point2D}
         *
         * @return the square of the distance between this
         * {@code Point2D} and the specified point.
         *
         * @since 1.2
         */
        public double distanceSq(double px, double py) {
            px -= getX();
            py -= getY();
            return (px * px + py * py);
        }
        
        /**
         * Returns the square of the distance from this
         * {@code Point2D} to a specified {@code Point2D}.
         *
         * @param pt the specified point to be measured
         *           against this {@code Point2D}
         *
         * @return the square of the distance between this
         * {@code Point2D} to a specified {@code Point2D}.
         *
         * @since 1.2
         */
        public double distanceSq(Double pt) {
            double px = pt.getX() - this.getX();
            double py = pt.getY() - this.getY();
            return (px * px + py * py);
        }
        
        /**
         * Returns the distance from this {@code Point2D} to
         * a specified point.
         *
         * @param px the X coordinate of the specified point to be measured
         *           against this {@code Point2D}
         * @param py the Y coordinate of the specified point to be measured
         *           against this {@code Point2D}
         *
         * @return the distance between this {@code Point2D}
         * and a specified point.
         *
         * @since 1.2
         */
        public double distance(double px, double py) {
            px -= getX();
            py -= getY();
            return Math.sqrt(px * px + py * py);
        }
        
        /**
         * Returns the distance from this {@code Point2D} to a
         * specified {@code Point2D}.
         *
         * @param pt the specified point to be measured
         *           against this {@code Point2D}
         *
         * @return the distance between this {@code Point2D} and
         * the specified {@code Point2D}.
         *
         * @since 1.2
         */
        public double distance(Double pt) {
            double px = pt.getX() - this.getX();
            double py = pt.getY() - this.getY();
            return Math.sqrt(px * px + py * py);
        }
        
        /**
         * Creates a new object of the same class and with the
         * same contents as this object.
         *
         * @return a clone of this instance.
         *
         * @throws OutOfMemoryError if there is not enough memory.
         * @see Cloneable
         * @since 1.2
         */
        public Double clone() {
            return new Double(getX(), getY());
        }
        
        /**
         * Returns the hashcode for this {@code Point2D}.
         *
         * @return a hash code for this {@code Point2D}.
         */
        public int hashCode() {
            long bits = java.lang.Double.doubleToLongBits(getX());
            bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
            return (((int) bits) ^ ((int) (bits >> 32)));
        }
        
        /**
         * Determines whether or not two points are equal. Two instances of
         * {@code Point2D} are equal if the values of their
         * {@code x} and {@code y} member fields, representing
         * their position in the coordinate space, are the same.
         *
         * @param obj an object to be compared with this {@code Point2D}
         *
         * @return {@code true} if the object to be compared is
         * an instance of {@code Point2D} and has
         * the same values; {@code false} otherwise.
         *
         * @since 1.2
         */
        public boolean equals(Object obj) {
            if (obj instanceof Double p2d) {
                return (getX() == p2d.getX()) && (getY() == p2d.getY());
            }
            return super.equals(obj);
        }
    }
}
