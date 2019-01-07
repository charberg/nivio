package de.bonndan.nivio.landscape;

/**
 * One specific
 */
public interface StatusItem {

    String HEALTH = "HEALTH";
    String LIFECYCLE = "lifecycle";
    String SECURITY = "security";
    String STABILITY = "stability";
    String BUSINESS_CAPABILITY = "business_capability";

    /**
     * The label / name, unique for a service.
     */
    String getLabel();

    Status getStatus();

    String getMessage();
}