package org.openmarkov.io.probmodel.reader;

/**
 * Enum to define the parser version labels
 */
public enum Version {
    V02,
    V10;
    
    public String toString() {
        return switch (this) {
            case V02 -> "0.2";
            case V10 -> "1.0";
        };
	}
}